package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DiagnosisCyst
import com.example.ui.theme.DiagnosisDisagreement
import com.example.ui.theme.DiagnosisNormal
import com.example.ui.theme.DiagnosisStone
import com.example.ui.theme.DiagnosisTumor

enum class KidneyDiseaseClass(
    val displayName: String,
    val description: String,
    val radiologicSigns: String,
    val color: Color
) {
    NORMAL(
        displayName = "Normal",
        description = "Renal parenchyma appears homogeneous with normal corticomedullary differentiation and no focal lesions or nephrolithiasis.",
        radiologicSigns = "Smooth renal outline, normal parenchyma density (30-40 HU), patent pelvicalyceal system without ectasia or calculi.",
        color = DiagnosisNormal
    ),
    KIDNEY_STONE(
        displayName = "Kidney Stone",
        description = "Hyperdense focal calcification (nephrolithiasis / urolithiasis) identified within the renal calyces, pelvis, or pelviureteric junction.",
        radiologicSigns = "High CT attenuation (>200-800 HU) with acoustic shadowing on ultrasound; dense white radio-opacity on non-contrast CT.",
        color = DiagnosisStone
    ),
    KIDNEY_CYST(
        displayName = "Kidney Cyst",
        description = "Well-circumscribed, homogeneous hypodense fluid-filled lesion adhering to Bosniak Category I / II benign cyst criteria.",
        radiologicSigns = "Near-water attenuation (-10 to +20 HU), imperceptible smooth thin walls, absence of internal septations or contrast enhancement.",
        color = DiagnosisCyst
    ),
    KIDNEY_TUMOR(
        displayName = "Kidney Tumor",
        description = "Heterogeneous soft-tissue renal parenchymal mass lesion requiring clinical histopathological oncological evaluation.",
        radiologicSigns = "Deformity of renal contour, irregular soft tissue attenuation (>20 HU), heterogeneous internal architecture, potential necrosis.",
        color = DiagnosisTumor
    )
}

enum class AgreementStatus(
    val title: String,
    val subtitle: String,
    val isConsistent: Boolean,
    val color: Color
) {
    AGREEMENT(
        title = "Model Agreement",
        subtitle = "Both SVM and Decision Tree algorithms converged on the exact same category.",
        isConsistent = true,
        color = DiagnosisNormal
    ),
    DISAGREEMENT(
        title = "Models Disagree – Further Expert Review Required",
        subtitle = "Support Vector Machine and Decision Tree classifications diverged. Conservative safety protocol flags this case.",
        isConsistent = false,
        color = DiagnosisDisagreement
    ),
    INDETERMINATE(
        title = "Indeterminate / Out-of-Distribution",
        subtitle = "Image texture or contrast statistics do not match accepted renal CT/MRI distributions.",
        isConsistent = false,
        color = DiagnosisDisagreement
    )
}

data class ExtractedFeatures(
    val meanIntensity: Float,
    val stdDev: Float,
    val skewness: Float,
    val kurtosis: Float,
    val glcmContrast: Float,
    val glcmEnergy: Float,
    val glcmHomogeneity: Float,
    val glcmEntropy: Float,
    val calcificationIndex: Float,
    val hypodenseRatio: Float,
    val edgeRoughness: Float
)

data class ModelPrediction(
    val modelName: String,
    val predictedClass: KidneyDiseaseClass,
    val confidence: Float, // Statistically calibrated probability 0.0 - 1.0
    val classProbabilities: Map<KidneyDiseaseClass, Float>,
    val decisionPathSummary: String
)

data class DiagnosticResult(
    val id: Long = 0,
    val patientRef: String,
    val scanName: String,
    val scanType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val svmPrediction: ModelPrediction,
    val dtPrediction: ModelPrediction,
    val agreementStatus: AgreementStatus,
    val finalCategory: KidneyDiseaseClass?,
    val extractedFeatures: ExtractedFeatures,
    val imageUri: String? = null,
    val samplePresetName: String? = null,
    val processingDurationMs: Long = 1250L
)

data class SampleScanPreset(
    val id: String,
    val title: String,
    val description: String,
    val expectedClass: KidneyDiseaseClass,
    val drawableResId: Int,
    val patientId: String
)

data class UserProfile(
    val fullName: String = "Dr. Alexander Wright, MD",
    val email: String = "a.wright@radiology.med.org",
    val institution: String = "Biomedical Imaging & ML Research Laboratory",
    val role: String = "Senior Clinical Research Radiologist"
)
