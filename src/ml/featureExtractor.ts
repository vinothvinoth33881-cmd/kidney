import { ExtractedFeatures } from '../types';

export function extractRadiomicsFeatures(pixels: Uint8ClampedArray, width: number, height: number): ExtractedFeatures {
  const totalPixels = width * height;
  const grayValues = new Float64Array(totalPixels);
  const GLCM_LEVELS = 16;
  const quantized = new Int32Array(totalPixels);

  let sum = 0;
  let sumSq = 0;
  let calcificationCount = 0;
  let hypodenseCount = 0;

  for (let i = 0; i < totalPixels; i++) {
    const gray = pixels[i * 4];
    grayValues[i] = gray;
    quantized[i] = Math.min(GLCM_LEVELS - 1, Math.floor((gray / 256) * GLCM_LEVELS));

    sum += gray;
    sumSq += gray * gray;

    // High attenuation > 210 HU indicates dense calculus
    if (gray > 210) {
      calcificationCount++;
    }
    // Low attenuation fluid [20, 75] indicates fluid-filled cystic region
    if (gray >= 20 && gray <= 75) {
      hypodenseCount++;
    }
  }

  const mean = sum / totalPixels;
  const variance = Math.max(0.0001, (sumSq / totalPixels) - (mean * mean));
  const stdDev = Math.sqrt(variance);

  // Skewness and Kurtosis
  let m3 = 0;
  let m4 = 0;
  for (let i = 0; i < totalPixels; i++) {
    const diff = grayValues[i] - mean;
    m3 += Math.pow(diff, 3);
    m4 += Math.pow(diff, 4);
  }
  const skewness = (m3 / totalPixels) / Math.pow(stdDev, 3);
  const kurtosis = (m4 / totalPixels) / Math.pow(variance, 2);

  // GLCM calculation (dx=1, dy=0 and dx=0, dy=1 symmetric)
  const glcm = Array.from({ length: GLCM_LEVELS }, () => new Float64Array(GLCM_LEVELS));
  let pairCount = 0;

  for (let y = 0; y < height; y++) {
    const rowOffset = y * width;
    for (let x = 0; x < width - 1; x++) {
      const g1 = quantized[rowOffset + x];
      const g2 = quantized[rowOffset + x + 1];
      glcm[g1][g2]++;
      glcm[g2][g1]++;
      pairCount += 2;
    }
  }

  for (let y = 0; y < height - 1; y++) {
    for (let x = 0; x < width; x++) {
      const g1 = quantized[y * width + x];
      const g2 = quantized[(y + 1) * width + x];
      glcm[g1][g2]++;
      glcm[g2][g1]++;
      pairCount += 2;
    }
  }

  const invPairs = 1.0 / Math.max(1, pairCount);
  let contrast = 0;
  let energy = 0;
  let homogeneity = 0;
  let entropy = 0;

  for (let i = 0; i < GLCM_LEVELS; i++) {
    for (let j = 0; j < GLCM_LEVELS; j++) {
      const p = glcm[i][j] * invPairs;
      if (p > 0) {
        const diff = Math.abs(i - j);
        contrast += diff * diff * p;
        energy += p * p;
        homogeneity += p / (1.0 + diff);
        entropy -= p * (Math.log(p) / Math.LN2);
      }
    }
  }

  // Edge roughness via gradient magnitude variance
  let edgeSum = 0;
  let edgeCount = 0;
  for (let y = 1; y < height - 1; y++) {
    for (let x = 1; x < width - 1; x++) {
      const p = (dx: number, dy: number) => grayValues[(y + dy) * width + (x + dx)];
      const gx = -p(-1, -1) + p(1, -1) - 2 * p(-1, 0) + 2 * p(1, 0) - p(-1, 1) + p(1, 1);
      const gy = -p(-1, -1) - 2 * p(0, -1) - p(1, -1) + p(-1, 1) + 2 * p(0, 1) + p(1, 1);
      edgeSum += Math.sqrt(gx * gx + gy * gy);
      edgeCount++;
    }
  }

  return {
    meanIntensity: mean,
    stdDev,
    skewness,
    kurtosis,
    glcmContrast: contrast,
    glcmEnergy: energy,
    glcmHomogeneity: homogeneity,
    glcmEntropy: entropy,
    calcificationIndex: (calcificationCount / totalPixels) * 100,
    hypodenseRatio: (hypodenseCount / totalPixels) * 100,
    edgeRoughness: edgeCount > 0 ? edgeSum / edgeCount : 0
  };
}

export function toFeatureVector(f: ExtractedFeatures): number[] {
  return [
    (f.meanIntensity - 90) / 35,
    (f.stdDev - 25) / 12,
    f.skewness,
    (f.glcmContrast - 4.5) / 3.0,
    (f.glcmHomogeneity - 0.45) / 0.15,
    (f.glcmEntropy - 3.5) / 1.0,
    f.calcificationIndex / 1.5,
    (f.edgeRoughness - 20) / 10
  ];
}
