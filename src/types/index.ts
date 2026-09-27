export type KidneyDiseaseClass = 'NORMAL' | 'KIDNEY_STONE' | 'KIDNEY_CYST' | 'KIDNEY_TUMOR';

export type AgreementStatus = 'AGREEMENT' | 'DISAGREEMENT' | 'INDETERMINATE';

export interface ExtractedFeatures {
  meanIntensity: number;
  stdDev: number;
  skewness: number;
  kurtosis: number;
  glcmContrast: number;
  glcmEnergy: number;
  glcmHomogeneity: number;
  glcmEntropy: number;
  calcificationIndex: number;
  hypodenseRatio: number;
  edgeRoughness: number;
}

export interface ModelPrediction {
  modelName: string;
  predictedClass: KidneyDiseaseClass;
  confidence: number; // Statistically calibrated 0.0 - 1.0
  classProbabilities: Record<KidneyDiseaseClass, number>;
  decisionPathSummary: string;
}

export interface DiagnosticReport {
  id: string;
  patientRef: string;
  scanName: string;
  scanType: string;
  timestamp: number;
  imageUrl: string;
  svmPrediction: ModelPrediction;
  dtPrediction: ModelPrediction;
  agreementStatus: AgreementStatus;
  finalCategory: KidneyDiseaseClass | null;
  extractedFeatures: ExtractedFeatures;
  processingDurationMs: number;
  notes?: string;
}

export interface SampleCase {
  id: string;
  title: string;
  description: string;
  expectedClass: KidneyDiseaseClass;
  imagePath: string;
  patientRef: string;
}

export interface User {
  fullName: string;
  email: string;
  institution: string;
  role: string;
}
