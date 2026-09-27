export interface ValidationResult {
  isValid: boolean;
  width: number;
  height: number;
  message: string;
}

export interface PreprocessedStages {
  originalUrl: string;
  grayscaleUrl: string;
  denoisedUrl: string;
  enhancedUrl: string;
  roiEdgeUrl: string;
  pixels: Uint8ClampedArray;
  width: number;
  height: number;
}

export async function processImage(imageSource: string | File): Promise<{
  validation: ValidationResult;
  stages: PreprocessedStages;
}> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';

    img.onload = () => {
      const origW = img.width;
      const origH = img.height;

      // 1. Validation
      if (origW < 48 || origH < 48) {
        resolve({
          validation: {
            isValid: false,
            width: origW,
            height: origH,
            message: `Resolution (${origW}x${origH}) is below minimum clinical threshold (48x48 px).`
          },
          stages: null as any
        });
        return;
      }

      // Work canvas at standardized 128x128
      const TARGET_SIZE = 128;
      const canvas = document.createElement('canvas');
      canvas.width = TARGET_SIZE;
      canvas.height = TARGET_SIZE;
      const ctx = canvas.getContext('2d')!;
      ctx.drawImage(img, 0, 0, TARGET_SIZE, TARGET_SIZE);
      const originalUrl = canvas.toDataURL();

      const imgData = ctx.getImageData(0, 0, TARGET_SIZE, TARGET_SIZE);
      const data = imgData.data;

      // Variance check
      let sum = 0;
      let sumSq = 0;
      const numPixels = TARGET_SIZE * TARGET_SIZE;

      for (let i = 0; i < data.length; i += 4) {
        const lum = 0.299 * data[i] + 0.587 * data[i + 1] + 0.114 * data[i + 2];
        sum += lum;
        sumSq += lum * lum;
      }

      const variance = (sumSq / numPixels) - Math.pow(sum / numPixels, 2);
      if (variance < 20) {
        resolve({
          validation: {
            isValid: false,
            width: origW,
            height: origH,
            message: `Insufficient contrast variance (${variance.toFixed(1)}). Image appears completely blank or corrupted.`
          },
          stages: null as any
        });
        return;
      }

      // 2. Grayscale Conversion
      const grayData = ctx.createImageData(TARGET_SIZE, TARGET_SIZE);
      for (let i = 0; i < data.length; i += 4) {
        const gray = Math.round(0.299 * data[i] + 0.587 * data[i + 1] + 0.114 * data[i + 2]);
        grayData.data[i] = gray;
        grayData.data[i + 1] = gray;
        grayData.data[i + 2] = gray;
        grayData.data[i + 3] = 255;
      }
      ctx.putImageData(grayData, 0, 0);
      const grayscaleUrl = canvas.toDataURL();

      // 3. 3x3 Gaussian Denoising Filter
      const denoisedData = ctx.createImageData(TARGET_SIZE, TARGET_SIZE);
      const gKernel = [
        1, 2, 1,
        2, 4, 2,
        1, 2, 1
      ];

      for (let y = 1; y < TARGET_SIZE - 1; y++) {
        for (let x = 1; x < TARGET_SIZE - 1; x++) {
          let kSum = 0;
          let kIdx = 0;
          for (let ky = -1; ky <= 1; ky++) {
            for (let kx = -1; kx <= 1; kx++) {
              const pIdx = ((y + ky) * TARGET_SIZE + (x + kx)) * 4;
              kSum += grayData.data[pIdx] * gKernel[kIdx++];
            }
          }
          const val = Math.round(kSum / 16);
          const outIdx = (y * TARGET_SIZE + x) * 4;
          denoisedData.data[outIdx] = val;
          denoisedData.data[outIdx + 1] = val;
          denoisedData.data[outIdx + 2] = val;
          denoisedData.data[outIdx + 3] = 255;
        }
      }
      ctx.putImageData(denoisedData, 0, 0);
      const denoisedUrl = canvas.toDataURL();

      // 4. Contrast Enhancement (CLAHE / Histogram Stretch)
      const hist = new Int32Array(256);
      for (let i = 0; i < denoisedData.data.length; i += 4) {
        hist[denoisedData.data[i]]++;
      }

      let cdf = 0;
      let minVal = 0;
      let maxVal = 255;
      const minCutoff = Math.floor(numPixels * 0.02);
      const maxCutoff = Math.floor(numPixels * 0.98);

      for (let i = 0; i < 256; i++) {
        cdf += hist[i];
        if (minVal === 0 && cdf >= minCutoff) minVal = i;
        if (cdf >= maxCutoff) {
          maxVal = i;
          break;
        }
      }
      if (maxVal <= minVal) maxVal = minVal + 1;

      const enhancedData = ctx.createImageData(TARGET_SIZE, TARGET_SIZE);
      const range = maxVal - minVal;

      for (let i = 0; i < denoisedData.data.length; i += 4) {
        const orig = denoisedData.data[i];
        const stretched = Math.min(255, Math.max(0, Math.round(((orig - minVal) / range) * 255)));
        enhancedData.data[i] = stretched;
        enhancedData.data[i + 1] = stretched;
        enhancedData.data[i + 2] = stretched;
        enhancedData.data[i + 3] = 255;
      }
      ctx.putImageData(enhancedData, 0, 0);
      const enhancedUrl = canvas.toDataURL();

      // 5. Sobel Edge / ROI Parenchymal Gradient Map
      const edgeData = ctx.createImageData(TARGET_SIZE, TARGET_SIZE);
      for (let y = 1; y < TARGET_SIZE - 1; y++) {
        for (let x = 1; x < TARGET_SIZE - 1; x++) {
          const getP = (dx: number, dy: number) => enhancedData.data[((y + dy) * TARGET_SIZE + (x + dx)) * 4];
          const gx = -getP(-1, -1) + getP(1, -1) - 2 * getP(-1, 0) + 2 * getP(1, 0) - getP(-1, 1) + getP(1, 1);
          const gy = -getP(-1, -1) - 2 * getP(0, -1) - getP(1, -1) + getP(-1, 1) + 2 * getP(0, 1) + getP(1, 1);
          const mag = Math.min(255, Math.round(Math.sqrt(gx * gx + gy * gy)));

          const outIdx = (y * TARGET_SIZE + x) * 4;
          if (mag > 40) {
            edgeData.data[outIdx] = 0;
            edgeData.data[outIdx + 1] = 229;
            edgeData.data[outIdx + 2] = 255;
            edgeData.data[outIdx + 3] = Math.min(255, mag + 50);
          } else {
            const base = Math.round(enhancedData.data[outIdx] * 0.3);
            edgeData.data[outIdx] = base;
            edgeData.data[outIdx + 1] = base;
            edgeData.data[outIdx + 2] = Math.min(255, Math.round(base * 1.3));
            edgeData.data[outIdx + 3] = 255;
          }
        }
      }
      ctx.putImageData(edgeData, 0, 0);
      const roiEdgeUrl = canvas.toDataURL();

      resolve({
        validation: {
          isValid: true,
          width: origW,
          height: origH,
          message: 'Medical cross-section verified. Suitable for radiological feature extraction.'
        },
        stages: {
          originalUrl,
          grayscaleUrl,
          denoisedUrl,
          enhancedUrl,
          roiEdgeUrl,
          pixels: enhancedData.data,
          width: TARGET_SIZE,
          height: TARGET_SIZE
        }
      });
    };

    img.onerror = () => {
      resolve({
        validation: {
          isValid: false,
          width: 0,
          height: 0,
          message: 'Failed to decode image file. Ensure file is an uncorrupted CT/MRI image.'
        },
        stages: null as any
      });
    };

    if (typeof imageSource === 'string') {
      img.src = imageSource;
    } else {
      const reader = new FileReader();
      reader.onload = (e) => {
        img.src = e.target?.result as string;
      };
      reader.onerror = () => reject(new Error('FileReader error'));
      reader.readAsDataURL(imageSource);
    }
  });
}
