package com.example.ml

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

data class ValidationResult(
    val isValid: Boolean,
    val width: Int,
    val height: Int,
    val message: String
)

data class PreprocessedStages(
    val original: Bitmap,
    val grayscale: Bitmap,
    val denoised: Bitmap,
    val enhanced: Bitmap,
    val roiEdge: Bitmap
)

object ImagePreprocessor {

    fun validate(bitmap: Bitmap): ValidationResult {
        val w = bitmap.width
        val h = bitmap.height

        if (w < 48 || h < 48) {
            return ValidationResult(
                isValid = false,
                width = w,
                height = h,
                message = "Image dimensions ($w x $h) too low for radiological analysis. Minimum required: 48x48."
            )
        }

        // Check variance to ensure image is not a blank single-color canvas
        var sum = 0.0
        var sumSq = 0.0
        val sampleStep = max(1, (w * h) / 1000)
        var count = 0

        for (y in 0 until h step max(1, h / 32)) {
            for (x in 0 until w step max(1, w / 32)) {
                val pixel = bitmap.getPixel(x, y)
                val lum = (0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel))
                sum += lum
                sumSq += lum * lum
                count++
            }
        }

        val variance = if (count > 0) (sumSq / count) - (sum / count) * (sum / count) else 0.0
        if (variance < 25.0) {
            return ValidationResult(
                isValid = false,
                width = w,
                height = h,
                message = "Image has insufficient contrast/variance (variance: ${String.format("%.1f", variance)}). Likely a blank or corrupted scan."
            )
        }

        return ValidationResult(
            isValid = true,
            width = w,
            height = h,
            message = "Scan verified. Suitable for radiological feature extraction."
        )
    }

    fun resize(bitmap: Bitmap, targetSize: Int = 128): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
    }

    fun toGrayscale(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val grayBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        for (i in pixels.indices) {
            val p = pixels[i]
            val r = Color.red(p)
            val g = Color.green(p)
            val b = Color.blue(p)
            val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt().coerceIn(0, 255)
            pixels[i] = Color.rgb(gray, gray, gray)
        }

        grayBitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return grayBitmap
    }

    fun applyDenoising(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val src = IntArray(w * h)
        val dst = IntArray(w * h)
        bitmap.getPixels(src, 0, w, 0, 0, w, h)

        // 3x3 Gaussian-like spatial smoothing filter: [1 2 1; 2 4 2; 1 2 1] / 16
        val kernel = intArrayOf(
            1, 2, 1,
            2, 4, 2,
            1, 2, 1
        )

        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                var sum = 0
                var k = 0
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val px = Color.red(src[(y + ky) * w + (x + kx)])
                        sum += px * kernel[k++]
                    }
                }
                val filtered = (sum / 16).coerceIn(0, 255)
                dst[y * w + x] = Color.rgb(filtered, filtered, filtered)
            }
        }

        // Fill borders
        for (x in 0 until w) {
            dst[x] = src[x]
            dst[(h - 1) * w + x] = src[(h - 1) * w + x]
        }
        for (y in 0 until h) {
            dst[y * w] = src[y * w]
            dst[y * w + (w - 1)] = src[y * w + (w - 1)]
        }

        output.setPixels(dst, 0, w, 0, 0, w, h)
        return output
    }

    fun enhanceContrast(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        // Calculate histogram
        val hist = IntArray(256)
        for (p in pixels) {
            val gray = Color.red(p)
            hist[gray]++
        }

        // Min-Max clipping (1% and 99% percentile stretch for radiological contrast)
        val total = w * h
        var cdf = 0
        var minVal = 0
        var maxVal = 255
        val minCutoff = (total * 0.02).toInt()
        val maxCutoff = (total * 0.98).toInt()

        for (i in 0 until 256) {
            cdf += hist[i]
            if (minVal == 0 && cdf >= minCutoff) minVal = i
            if (cdf >= maxCutoff) {
                maxVal = i
                break
            }
        }
        if (maxVal <= minVal) maxVal = minVal + 1

        val range = maxVal - minVal
        for (i in pixels.indices) {
            val gray = Color.red(pixels[i])
            val stretched = (((gray - minVal).toFloat() / range) * 255f).toInt().coerceIn(0, 255)
            pixels[i] = Color.rgb(stretched, stretched, stretched)
        }

        output.setPixels(pixels, 0, w, 0, 0, w, h)
        return output
    }

    fun extractRoiEdge(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val src = IntArray(w * h)
        val dst = IntArray(w * h)
        bitmap.getPixels(src, 0, w, 0, 0, w, h)

        // Sobel edge gradient computation
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val p00 = Color.red(src[(y - 1) * w + (x - 1)])
                val p01 = Color.red(src[(y - 1) * w + x])
                val p02 = Color.red(src[(y - 1) * w + (x + 1)])
                val p10 = Color.red(src[y * w + (x - 1)])
                val p12 = Color.red(src[y * w + (x + 1)])
                val p20 = Color.red(src[(y + 1) * w + (x - 1)])
                val p21 = Color.red(src[(y + 1) * w + x])
                val p22 = Color.red(src[(y + 1) * w + (x + 1)])

                val gx = (-p00 + p02 - 2 * p10 + 2 * p12 - p20 + p22)
                val gy = (-p00 - 2 * p01 - p02 + p20 + 2 * p21 + p22)

                val mag = kotlin.math.sqrt((gx * gx + gy * gy).toDouble()).toInt().coerceIn(0, 255)

                // Cyan highlighted edges over dark background for medical futuristic look
                if (mag > 45) {
                    val alpha = mag.coerceIn(100, 255)
                    dst[y * w + x] = Color.argb(alpha, 0, 229, 255)
                } else {
                    val base = (Color.red(src[y * w + x]) * 0.35).toInt()
                    dst[y * w + x] = Color.rgb(base, base, (base * 1.2).toInt().coerceIn(0, 255))
                }
            }
        }

        output.setPixels(dst, 0, w, 0, 0, w, h)
        return output
    }
}
