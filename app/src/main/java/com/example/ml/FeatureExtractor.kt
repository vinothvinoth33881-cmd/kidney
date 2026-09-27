package com.example.ml

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.ExtractedFeatures
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

object FeatureExtractor {

    private const val GLCM_LEVELS = 16 // Quantized levels for fast, robust co-occurrence matrix

    fun extract(bitmap: Bitmap): ExtractedFeatures {
        val w = bitmap.width
        val h = bitmap.height
        val totalPixels = w * h
        val pixels = IntArray(totalPixels)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        var sum = 0.0
        var sumSq = 0.0
        var calcificationCount = 0
        var hypodenseCount = 0

        val grayValues = DoubleArray(totalPixels)
        val quantized = IntArray(totalPixels)

        for (i in 0 until totalPixels) {
            val p = pixels[i]
            val gray = Color.red(p).toDouble()
            grayValues[i] = gray
            quantized[i] = (gray / 256.0 * GLCM_LEVELS).toInt().coerceIn(0, GLCM_LEVELS - 1)

            sum += gray
            sumSq += gray * gray

            // High CT attenuation > 210 (typical of renal calcification / stones)
            if (gray > 210) {
                calcificationCount++
            }
            // Low attenuation fluid range [20..75] typical of cystic structures
            if (gray in 20.0..75.0) {
                hypodenseCount++
            }
        }

        val mean = sum / totalPixels
        val variance = (sumSq / totalPixels) - (mean * mean)
        val stdDev = sqrt(variance.coerceAtLeast(0.0001))

        // Higher-order statistics (Skewness & Kurtosis)
        var m3 = 0.0
        var m4 = 0.0
        for (i in 0 until totalPixels) {
            val diff = grayValues[i] - mean
            m3 += diff.pow(3)
            m4 += diff.pow(4)
        }
        val skewness = (m3 / totalPixels) / (stdDev.pow(3).coerceAtLeast(0.0001))
        val kurtosis = (m4 / totalPixels) / (variance.pow(2).coerceAtLeast(0.0001))

        // Gray-Level Co-occurrence Matrix (GLCM) at distance 1, horizontal & vertical
        val glcm = Array(GLCM_LEVELS) { DoubleArray(GLCM_LEVELS) }
        var pairCount = 0

        // Horizontal pairs
        for (y in 0 until h) {
            val rowOffset = y * w
            for (x in 0 until w - 1) {
                val g1 = quantized[rowOffset + x]
                val g2 = quantized[rowOffset + x + 1]
                glcm[g1][g2]++
                glcm[g2][g1]++ // Symmetric GLCM
                pairCount += 2
            }
        }

        // Vertical pairs
        for (y in 0 until h - 1) {
            for (x in 0 until w) {
                val g1 = quantized[y * w + x]
                val g2 = quantized[(y + 1) * w + x]
                glcm[g1][g2]++
                glcm[g2][g1]++
                pairCount += 2
            }
        }

        // Normalize GLCM to probabilities
        val invPairCount = 1.0 / pairCount.coerceAtLeast(1)
        var contrast = 0.0
        var energy = 0.0
        var homogeneity = 0.0
        var entropy = 0.0

        for (i in 0 until GLCM_LEVELS) {
            for (j in 0 until GLCM_LEVELS) {
                val p = glcm[i][j] * invPairCount
                if (p > 0.0) {
                    val diff = (i - j).toDouble()
                    contrast += diff * diff * p
                    energy += p * p
                    homogeneity += p / (1.0 + kotlin.math.abs(diff))
                    entropy -= p * (ln(p) / ln(2.0))
                }
            }
        }

        // Sobel Edge roughness calculation
        var edgeSum = 0.0
        var edgePixels = 0
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val gx = -grayValues[(y - 1) * w + (x - 1)] + grayValues[(y - 1) * w + (x + 1)] -
                        2 * grayValues[y * w + (x - 1)] + 2 * grayValues[y * w + (x + 1)] -
                        grayValues[(y + 1) * w + (x - 1)] + grayValues[(y + 1) * w + (x + 1)]
                val gy = -grayValues[(y - 1) * w + (x - 1)] - 2 * grayValues[(y - 1) * w + x] -
                        grayValues[(y - 1) * w + (x + 1)] + grayValues[(y + 1) * w + (x - 1)] +
                        2 * grayValues[(y + 1) * w + x] + grayValues[(y + 1) * w + (x + 1)]
                val mag = sqrt(gx * gx + gy * gy)
                edgeSum += mag
                edgePixels++
            }
        }
        val edgeRoughness = if (edgePixels > 0) (edgeSum / edgePixels) else 0.0

        return ExtractedFeatures(
            meanIntensity = mean.toFloat(),
            stdDev = stdDev.toFloat(),
            skewness = skewness.toFloat(),
            kurtosis = kurtosis.toFloat(),
            glcmContrast = contrast.toFloat(),
            glcmEnergy = energy.toFloat(),
            glcmHomogeneity = homogeneity.toFloat(),
            glcmEntropy = entropy.toFloat(),
            calcificationIndex = (calcificationCount.toFloat() / totalPixels) * 100f,
            hypodenseRatio = (hypodenseCount.toFloat() / totalPixels) * 100f,
            edgeRoughness = edgeRoughness.toFloat()
        )
    }

    /**
     * Converts ExtractedFeatures into a standardized normalized 8-dimensional feature vector:
     * [0] Normalized Mean Intensity
     * [1] Normalized StdDev
     * [2] Normalized Skewness
     * [3] Normalized GLCM Contrast
     * [4] Normalized GLCM Homogeneity
     * [5] Normalized GLCM Entropy
     * [6] Calcification Index
     * [7] Edge Roughness
     */
    fun toFeatureVector(features: ExtractedFeatures): FloatArray {
        return floatArrayOf(
            (features.meanIntensity - 90f) / 35f,
            (features.stdDev - 25f) / 12f,
            features.skewness,
            (features.glcmContrast - 4.5f) / 3.0f,
            (features.glcmHomogeneity - 0.45f) / 0.15f,
            (features.glcmEntropy - 3.5f) / 1.0f,
            features.calcificationIndex / 1.5f,
            (features.edgeRoughness - 20f) / 10f
        )
    }
}
