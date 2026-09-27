package com.example

import com.example.data.ExtractedFeatures
import com.example.data.KidneyDiseaseClass
import com.example.ml.DecisionTreeClassifier
import com.example.ml.SVMClassifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MachineLearningModelsTest {

    @Test
    fun testStoneDetectionWithHighCalcification() {
        val stoneFeatures = ExtractedFeatures(
            meanIntensity = 145f,
            stdDev = 42f,
            skewness = 1.6f,
            kurtosis = 4.2f,
            glcmContrast = 5.2f,
            glcmEnergy = 0.28f,
            glcmHomogeneity = 0.32f,
            glcmEntropy = 4.8f,
            calcificationIndex = 3.4f, // Dense radio-opaque calculus
            hypodenseRatio = 2.1f,
            edgeRoughness = 24.5f
        )

        val svmPred = SVMClassifier.predict(stoneFeatures)
        val dtPred = DecisionTreeClassifier.predict(stoneFeatures)

        assertEquals(KidneyDiseaseClass.KIDNEY_STONE, svmPred.predictedClass)
        assertEquals(KidneyDiseaseClass.KIDNEY_STONE, dtPred.predictedClass)
        assertTrue("SVM confidence should be calibrated > 0.5", svmPred.confidence > 0.5f)
        assertTrue("DT confidence should be > 0.5", dtPred.confidence > 0.5f)
    }

    @Test
    fun testNormalParenchymaDetection() {
        val normalFeatures = ExtractedFeatures(
            meanIntensity = 82f,
            stdDev = 19f,
            skewness = 0.15f,
            kurtosis = 2.1f,
            glcmContrast = 1.8f,
            glcmEnergy = 0.48f,
            glcmHomogeneity = 0.65f, // Smooth homogeneous parenchyma
            glcmEntropy = 3.1f,
            calcificationIndex = 0.05f,
            hypodenseRatio = 3.2f,
            edgeRoughness = 12.2f
        )

        val svmPred = SVMClassifier.predict(normalFeatures)
        val dtPred = DecisionTreeClassifier.predict(normalFeatures)

        assertEquals(KidneyDiseaseClass.NORMAL, svmPred.predictedClass)
        assertEquals(KidneyDiseaseClass.NORMAL, dtPred.predictedClass)
    }

    @Test
    fun testCystDetectionWithFluidHypodensity() {
        val cystFeatures = ExtractedFeatures(
            meanIntensity = 48f,
            stdDev = 28f,
            skewness = -0.75f,
            kurtosis = 2.8f,
            glcmContrast = 3.1f,
            glcmEnergy = 0.38f,
            glcmHomogeneity = 0.52f,
            glcmEntropy = 3.4f,
            calcificationIndex = 0.02f,
            hypodenseRatio = 14.8f, // Fluid cavity
            edgeRoughness = 15.1f
        )

        val svmPred = SVMClassifier.predict(cystFeatures)
        val dtPred = DecisionTreeClassifier.predict(cystFeatures)

        assertEquals(KidneyDiseaseClass.KIDNEY_CYST, svmPred.predictedClass)
        assertEquals(KidneyDiseaseClass.KIDNEY_CYST, dtPred.predictedClass)
    }
}
