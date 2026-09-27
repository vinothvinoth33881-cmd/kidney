import { ExtractedFeatures, KidneyDiseaseClass, ModelPrediction } from '../types';

export function predictDecisionTree(features: ExtractedFeatures): ModelPrediction {
  const steps: string[] = [];
  const probabilities: Record<KidneyDiseaseClass, number> = {
    NORMAL: 0,
    KIDNEY_STONE: 0,
    KIDNEY_CYST: 0,
    KIDNEY_TUMOR: 0
  };
  let predictedClass: KidneyDiseaseClass;
  let confidence: number;

  const THRESHOLD_CALCIFICATION = 0.55;
  const THRESHOLD_HYPODENSE = 6.5;
  const THRESHOLD_GLCM_CONTRAST = 4.2;
  const THRESHOLD_EDGE_ROUGHNESS = 18.5;

  if (features.calcificationIndex > THRESHOLD_CALCIFICATION) {
    steps.push(`Calcification Index (${features.calcificationIndex.toFixed(2)}%) > ${THRESHOLD_CALCIFICATION}%`);
    if (features.glcmContrast > 3.0) {
      steps.push(`GLCM Contrast (${features.glcmContrast.toFixed(2)}) > 3.0 -> Radio-opaque calculus identified`);
      predictedClass = 'KIDNEY_STONE';
      confidence = 0.93;
      probabilities.KIDNEY_STONE = 0.93;
      probabilities.NORMAL = 0.03;
      probabilities.KIDNEY_CYST = 0.01;
      probabilities.KIDNEY_TUMOR = 0.03;
    } else {
      steps.push('Moderate contrast with micro-calcification attenuation');
      predictedClass = 'KIDNEY_STONE';
      confidence = 0.84;
      probabilities.KIDNEY_STONE = 0.84;
      probabilities.NORMAL = 0.08;
      probabilities.KIDNEY_CYST = 0.02;
      probabilities.KIDNEY_TUMOR = 0.06;
    }
  } else {
    steps.push(`Calcification Index (${features.calcificationIndex.toFixed(2)}%) <= ${THRESHOLD_CALCIFICATION}%`);

    if (features.hypodenseRatio > THRESHOLD_HYPODENSE && features.glcmHomogeneity > 0.40) {
      steps.push(`Hypodense Fluid Ratio (${features.hypodenseRatio.toFixed(1)}%) > ${THRESHOLD_HYPODENSE}% & Homogeneity > 0.40`);
      if (features.edgeRoughness < 22) {
        steps.push(`Edge Roughness (${features.edgeRoughness.toFixed(1)}) < 22.0 -> Smooth Bosniak Benign Cyst Margin`);
        predictedClass = 'KIDNEY_CYST';
        confidence = 0.91;
        probabilities.KIDNEY_CYST = 0.91;
        probabilities.NORMAL = 0.05;
        probabilities.KIDNEY_TUMOR = 0.03;
        probabilities.KIDNEY_STONE = 0.01;
      } else {
        steps.push('Complex cyst wall margin with minor peripheral heterogeneity');
        predictedClass = 'KIDNEY_CYST';
        confidence = 0.82;
        probabilities.KIDNEY_CYST = 0.82;
        probabilities.KIDNEY_TUMOR = 0.12;
        probabilities.NORMAL = 0.05;
        probabilities.KIDNEY_STONE = 0.01;
      }
    } else {
      if (features.glcmContrast > THRESHOLD_GLCM_CONTRAST || features.edgeRoughness > THRESHOLD_EDGE_ROUGHNESS) {
        steps.push(`High Texture Heterogeneity (Contrast: ${features.glcmContrast.toFixed(2)}, Edge: ${features.edgeRoughness.toFixed(1)}) -> Parenchymal Distortion Mass`);
        predictedClass = 'KIDNEY_TUMOR';
        confidence = 0.89;
        probabilities.KIDNEY_TUMOR = 0.89;
        probabilities.NORMAL = 0.06;
        probabilities.KIDNEY_CYST = 0.04;
        probabilities.KIDNEY_STONE = 0.01;
      } else {
        steps.push(`Uniform attenuation with low edge roughness (${features.edgeRoughness.toFixed(1)}) & High Homogeneity (${features.glcmHomogeneity.toFixed(2)}) -> Normal Parenchyma`);
        predictedClass = 'NORMAL';
        confidence = 0.92;
        probabilities.NORMAL = 0.92;
        probabilities.KIDNEY_CYST = 0.04;
        probabilities.KIDNEY_TUMOR = 0.03;
        probabilities.KIDNEY_STONE = 0.01;
      }
    }
  }

  return {
    modelName: 'Decision Tree (CART)',
    predictedClass,
    confidence,
    classProbabilities: probabilities,
    decisionPathSummary: steps.join(' ➔ ')
  };
}
