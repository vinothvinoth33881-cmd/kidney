import { DiagnosticReport, PreprocessedStages } from '../types';
import { processImage } from './preprocessor';
import { extractRadiomicsFeatures } from './featureExtractor';
import { predictSVM } from './svm';
import { predictDecisionTree } from './decisionTree';

export interface PipelineProgressState {
  step: number;
  total: number;
  title: string;
  detail: string;
  stages?: PreprocessedStages;
}

export async function runDiagnosticPipeline(
  imageSource: string | File,
  scanName: string,
  scanType: string,
  patientRef: string,
  onProgress: (p: PipelineProgressState) => void
): Promise<DiagnosticReport> {
  const startTime = Date.now();

  // 1. Validation
  onProgress({
    step: 1,
    total: 10,
    title: 'Validating Scan File Integrity',
    detail: 'Checking dimensions, pixel format, and contrast variance...'
  });
  await new Promise(r => setTimeout(r, 150));

  const { validation, stages } = await processImage(imageSource);
  if (!validation.isValid) {
    throw new Error(validation.message);
  }

  // 2. Spatial Resizing
  onProgress({
    step: 2,
    total: 10,
    title: 'Spatial Resizing & Alignment',
    detail: 'Standardizing resolution to 128x128 for tensor operations...',
    stages
  });
  await new Promise(r => setTimeout(r, 120));

  // 3. Normalization (Grayscale)
  onProgress({
    step: 3,
    total: 10,
    title: 'Grayscale Luminance Mapping',
    detail: 'Normalizing radio-density attenuation values...',
    stages
  });
  await new Promise(r => setTimeout(r, 120));

  // 4. Noise Reduction
  onProgress({
    step: 4,
    total: 10,
    title: 'Spatial Noise Reduction',
    detail: 'Applying 3x3 Gaussian smoothing filter...',
    stages
  });
  await new Promise(r => setTimeout(r, 130));

  // 5. Contrast Enhancement
  onProgress({
    step: 5,
    total: 10,
    title: 'Adaptive Contrast Enhancement',
    detail: 'Executing CLAHE histogram equalization...',
    stages
  });
  await new Promise(r => setTimeout(r, 130));

  // 6. ROI Segmentation
  onProgress({
    step: 6,
    total: 10,
    title: 'Region of Interest (ROI) Processing',
    detail: 'Segmenting renal parenchyma and boundary contours...',
    stages
  });
  await new Promise(r => setTimeout(r, 130));

  // 7. Feature Extraction
  onProgress({
    step: 7,
    total: 10,
    title: 'Radiomics Feature Extraction',
    detail: 'Computing GLCM texture matrix, entropy, and intensity moments...',
    stages
  });
  await new Promise(r => setTimeout(r, 160));
  const features = extractRadiomicsFeatures(stages.pixels, stages.width, stages.height);

  // 8. SVM Inference
  onProgress({
    step: 8,
    total: 10,
    title: 'Running Support Vector Machine (SVM)',
    detail: 'Projecting onto RBF kernel hyperplane with Platt scaling...',
    stages
  });
  await new Promise(r => setTimeout(r, 140));
  const svmPrediction = predictSVM(features);

  // 9. Decision Tree Inference
  onProgress({
    step: 9,
    total: 10,
    title: 'Running Decision Tree (CART)',
    detail: 'Evaluating hierarchical Gini entropy split conditions...',
    stages
  });
  await new Promise(r => setTimeout(r, 140));
  const dtPrediction = predictDecisionTree(features);

  // 10. Agreement & Safety Logic
  onProgress({
    step: 10,
    total: 10,
    title: 'Evaluating Model Consistency',
    detail: 'Applying conservative clinical safety agreement check...',
    stages
  });
  await new Promise(r => setTimeout(r, 120));

  const isAgreement = svmPrediction.predictedClass === dtPrediction.predictedClass;
  const agreementStatus = isAgreement ? 'AGREEMENT' : 'DISAGREEMENT';
  // Conservative protocol: Assign final category ONLY when models agree!
  const finalCategory = isAgreement ? svmPrediction.predictedClass : null;

  return {
    id: `REP-${Date.now().toString().slice(-6)}`,
    patientRef,
    scanName,
    scanType,
    timestamp: Date.now(),
    imageUrl: stages.originalUrl,
    svmPrediction,
    dtPrediction,
    agreementStatus,
    finalCategory,
    extractedFeatures: features,
    processingDurationMs: Date.now() - startTime
  };
}
