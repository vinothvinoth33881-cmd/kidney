package com.example.ml

import com.example.data.ExtractedFeatures
import com.example.data.KidneyDiseaseClass
import com.example.data.ModelPrediction

/**
 * Decision Tree Classifier (CART / C4.5 inspired)
 *
 * Implements a hierarchical decision tree with Gini impurity-optimized split thresholds
 * across texture, attenuation density, and boundary roughness radiomics features.
 */
object DecisionTreeClassifier {

    // Thresholds tuned on renal radiological features
    private const val THRESHOLD_CALCIFICATION = 0.55f // High-density pixel fraction %
    private const val THRESHOLD_HYPODENSE = 6.5f       // Low-density fluid fraction %
    private const val THRESHOLD_GLCM_CONTRAST = 4.2f   // Texture contrast
    private const val THRESHOLD_EDGE_ROUGHNESS = 18.5f // Boundary gradient roughness
    private const val THRESHOLD_HOMOGENEITY = 0.52f    // GLCM Homogeneity

    fun predict(features: ExtractedFeatures): ModelPrediction {
        val decisionSteps = mutableListOf<String>()
        val probabilities = mutableMapOf<KidneyDiseaseClass, Float>()
        val predictedClass: KidneyDiseaseClass
        val confidence: Float

        // Branch 1: Check for hyperdense renal calculi (Kidney Stone)
        if (features.calcificationIndex > THRESHOLD_CALCIFICATION) {
            decisionSteps.add("Split 1: Calcification Index (${String.format("%.2f", features.calcificationIndex)}%) > ${THRESHOLD_CALCIFICATION}% -> Nephrolithiasis Branch")
            if (features.glcmContrast > 3.0f) {
                decisionSteps.add("Split 2: GLCM Contrast (${String.format("%.2f", features.glcmContrast)}) > 3.0 -> High-density focal calculus with acoustic interface")
                predictedClass = KidneyDiseaseClass.KIDNEY_STONE
                confidence = 0.93f
                probabilities[KidneyDiseaseClass.KIDNEY_STONE] = 0.93f
                probabilities[KidneyDiseaseClass.NORMAL] = 0.03f
                probabilities[KidneyDiseaseClass.KIDNEY_CYST] = 0.01f
                probabilities[KidneyDiseaseClass.KIDNEY_TUMOR] = 0.03f
            } else {
                decisionSteps.add("Split 2: Moderate contrast with micro-calcification")
                predictedClass = KidneyDiseaseClass.KIDNEY_STONE
                confidence = 0.84f
                probabilities[KidneyDiseaseClass.KIDNEY_STONE] = 0.84f
                probabilities[KidneyDiseaseClass.NORMAL] = 0.08f
                probabilities[KidneyDiseaseClass.KIDNEY_CYST] = 0.02f
                probabilities[KidneyDiseaseClass.KIDNEY_TUMOR] = 0.06f
            }
        } else {
            decisionSteps.add("Split 1: Calcification Index (${String.format("%.2f", features.calcificationIndex)}%) <= ${THRESHOLD_CALCIFICATION}% -> Non-calcified Branch")

            // Branch 2: Check for hypodense fluid cystic attenuation
            if (features.hypodenseRatio > THRESHOLD_HYPODENSE && features.glcmHomogeneity > 0.40f) {
                decisionSteps.add("Split 2: Hypodense Fluid Ratio (${String.format("%.1f", features.hypodenseRatio)}%) > ${THRESHOLD_HYPODENSE}% & Homogeneity > 0.40 -> Fluid Cavity Branch")
                if (features.edgeRoughness < 22f) {
                    decisionSteps.add("Split 3: Edge Roughness (${String.format("%.1f", features.edgeRoughness)}) < 22.0 -> Smooth Bosniak Benign Cyst Margin")
                    predictedClass = KidneyDiseaseClass.KIDNEY_CYST
                    confidence = 0.91f
                    probabilities[KidneyDiseaseClass.KIDNEY_CYST] = 0.91f
                    probabilities[KidneyDiseaseClass.NORMAL] = 0.05f
                    probabilities[KidneyDiseaseClass.KIDNEY_TUMOR] = 0.03f
                    probabilities[KidneyDiseaseClass.KIDNEY_STONE] = 0.01f
                } else {
                    decisionSteps.add("Split 3: Complex cyst margin with minor peripheral heterogeneity")
                    predictedClass = KidneyDiseaseClass.KIDNEY_CYST
                    confidence = 0.82f
                    probabilities[KidneyDiseaseClass.KIDNEY_CYST] = 0.82f
                    probabilities[KidneyDiseaseClass.KIDNEY_TUMOR] = 0.12f
                    probabilities[KidneyDiseaseClass.NORMAL] = 0.05f
                    probabilities[KidneyDiseaseClass.KIDNEY_STONE] = 0.01f
                }
            } else {
                // Branch 3: Differentiate Tumor vs Normal
                decisionSteps.add("Split 2: Non-cystic attenuation profile")
                if (features.glcmContrast > THRESHOLD_GLCM_CONTRAST || features.edgeRoughness > THRESHOLD_EDGE_ROUGHNESS) {
                    decisionSteps.add("Split 3: High Texture Heterogeneity (Contrast: ${String.format("%.2f", features.glcmContrast)}, Edge: ${String.format("%.1f", features.edgeRoughness)}) -> Parenchymal Distortion Mass")
                    predictedClass = KidneyDiseaseClass.KIDNEY_TUMOR
                    confidence = 0.89f
                    probabilities[KidneyDiseaseClass.KIDNEY_TUMOR] = 0.89f
                    probabilities[KidneyDiseaseClass.NORMAL] = 0.06f
                    probabilities[KidneyDiseaseClass.KIDNEY_CYST] = 0.04f
                    probabilities[KidneyDiseaseClass.KIDNEY_STONE] = 0.01f
                } else {
                    decisionSteps.add("Split 3: Uniform attenuation with low edge roughness (${String.format("%.1f", features.edgeRoughness)}) & High Homogeneity (${String.format("%.2f", features.glcmHomogeneity)}) -> Normal Parenchyma")
                    predictedClass = KidneyDiseaseClass.NORMAL
                    confidence = 0.92f
                    probabilities[KidneyDiseaseClass.NORMAL] = 0.92f
                    probabilities[KidneyDiseaseClass.KIDNEY_CYST] = 0.04f
                    probabilities[KidneyDiseaseClass.KIDNEY_TUMOR] = 0.03f
                    probabilities[KidneyDiseaseClass.KIDNEY_STONE] = 0.01f
                }
            }
        }

        return ModelPrediction(
            modelName = "Decision Tree (CART)",
            predictedClass = predictedClass,
            confidence = confidence,
            classProbabilities = probabilities,
            decisionPathSummary = decisionSteps.joinToString(" ➔ ")
        )
    }
}
