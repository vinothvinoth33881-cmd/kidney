package com.example.ml

import android.graphics.Bitmap
import com.example.data.AgreementStatus
import com.example.data.DiagnosticResult
import com.example.data.ExtractedFeatures
import com.example.data.KidneyDiseaseClass
import com.example.data.ModelPrediction
import kotlinx.coroutines.delay

sealed class PipelineStep(val stepNumber: Int, val title: String, val detail: String) {
    object Validation : PipelineStep(1, "Validating scan integrity", "Checking dimensions, format, contrast variance...")
    object Resizing : PipelineStep(2, "Spatial resizing", "Standardizing resolution to 128x128 for matrix processing...")
    object Denoising : PipelineStep(3, "Spatial noise reduction", "Applying 3x3 Gaussian smoothing filter...")
    object Normalization : PipelineStep(4, "Grayscale normalization", "Luminance mapping and intensity scaling...")
    object Enhancement : PipelineStep(5, "Contrast enhancement", "Executing adaptive histogram equalization...")
    object RoiProcessing : PipelineStep(6, "Region-of-interest processing", "Segmenting renal parenchyma and boundary gradients...")
    object FeatureExtraction : PipelineStep(7, "Radiomic feature extraction", "Computing GLCM matrix, texture entropy, and density statistics...")
    object SvmInference : PipelineStep(8, "Running Support Vector Machine", "Projecting onto RBF kernel hyperplane with Platt calibration...")
    object DecisionTreeInference : PipelineStep(9, "Running Decision Tree", "Evaluating hierarchical entropy and Gini split rules...")
    object ResultGeneration : PipelineStep(10, "Verifying model agreement", "Executing conservative clinical safety protocol...")
}

data class PipelineProgress(
    val currentStep: PipelineStep,
    val stepIndex: Int,
    val totalSteps: Int = 10,
    val stages: PreprocessedStages? = null
)

object DiagnosisPipelineEngine {

    suspend fun executePipeline(
        bitmap: Bitmap,
        scanName: String,
        scanType: String,
        patientRef: String,
        samplePresetName: String? = null,
        imageUri: String? = null,
        onProgress: (PipelineProgress) -> Unit
    ): DiagnosticResult {
        val startTime = System.currentTimeMillis()

        // 1. Validation
        onProgress(PipelineProgress(PipelineStep.Validation, 1))
        delay(120)
        val valResult = ImagePreprocessor.validate(bitmap)
        if (!valResult.isValid) {
            val emptyFeatures = ExtractedFeatures(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
            val fallbackPrediction = ModelPrediction(
                modelName = "Support Vector Machine (SVM)",
                predictedClass = KidneyDiseaseClass.NORMAL,
                confidence = 0f,
                classProbabilities = emptyMap(),
                decisionPathSummary = "Aborted: " + valResult.message
            )
            return DiagnosticResult(
                patientRef = patientRef,
                scanName = scanName,
                scanType = scanType,
                svmPrediction = fallbackPrediction,
                dtPrediction = fallbackPrediction.copy(modelName = "Decision Tree (CART)"),
                agreementStatus = AgreementStatus.INDETERMINATE,
                finalCategory = null,
                extractedFeatures = emptyFeatures,
                imageUri = imageUri,
                samplePresetName = samplePresetName,
                processingDurationMs = System.currentTimeMillis() - startTime
            )
        }

        // 2. Resizing
        onProgress(PipelineProgress(PipelineStep.Resizing, 2))
        delay(100)
        val resized = ImagePreprocessor.resize(bitmap, 128)

        // 3. Normalization (Grayscale)
        onProgress(PipelineProgress(PipelineStep.Normalization, 3))
        delay(100)
        val grayscale = ImagePreprocessor.toGrayscale(resized)

        // 4. Noise reduction
        onProgress(PipelineProgress(PipelineStep.Denoising, 4))
        delay(110)
        val denoised = ImagePreprocessor.applyDenoising(grayscale)

        // 5. Contrast enhancement
        onProgress(PipelineProgress(PipelineStep.Enhancement, 5))
        delay(120)
        val enhanced = ImagePreprocessor.enhanceContrast(denoised)

        // 6. Region of Interest & Edge Map
        onProgress(PipelineProgress(PipelineStep.RoiProcessing, 6))
        delay(120)
        val roiEdge = ImagePreprocessor.extractRoiEdge(enhanced)

        val stages = PreprocessedStages(
            original = resized,
            grayscale = grayscale,
            denoised = denoised,
            enhanced = enhanced,
            roiEdge = roiEdge
        )

        // 7. Feature Extraction
        onProgress(PipelineProgress(PipelineStep.FeatureExtraction, 7, stages = stages))
        delay(150)
        val features = FeatureExtractor.extract(enhanced)

        // 8. SVM Inference
        onProgress(PipelineProgress(PipelineStep.SvmInference, 8, stages = stages))
        delay(140)
        val svmPred = SVMClassifier.predict(features)

        // 9. Decision Tree Inference
        onProgress(PipelineProgress(PipelineStep.DecisionTreeInference, 9, stages = stages))
        delay(140)
        val dtPred = DecisionTreeClassifier.predict(features)

        // 10. Agreement & Safety Logic
        onProgress(PipelineProgress(PipelineStep.ResultGeneration, 10, stages = stages))
        delay(120)

        val agreementStatus = if (svmPred.predictedClass == dtPred.predictedClass) {
            AgreementStatus.AGREEMENT
        } else {
            AgreementStatus.DISAGREEMENT
        }

        // Conservative logic: Final category is ONLY assigned if models agree
        val finalCategory = if (agreementStatus == AgreementStatus.AGREEMENT) {
            svmPred.predictedClass
        } else {
            null // Disagreement requires expert human review
        }

        return DiagnosticResult(
            patientRef = patientRef,
            scanName = scanName,
            scanType = scanType,
            timestamp = System.currentTimeMillis(),
            svmPrediction = svmPred,
            dtPrediction = dtPred,
            agreementStatus = agreementStatus,
            finalCategory = finalCategory,
            extractedFeatures = features,
            imageUri = imageUri,
            samplePresetName = samplePresetName,
            processingDurationMs = System.currentTimeMillis() - startTime
        )
    }
}
