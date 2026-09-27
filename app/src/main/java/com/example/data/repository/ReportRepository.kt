package com.example.data.repository

import com.example.data.AgreementStatus
import com.example.data.DiagnosticResult
import com.example.data.ExtractedFeatures
import com.example.data.KidneyDiseaseClass
import com.example.data.ModelPrediction
import com.example.data.db.ScanReportDao
import com.example.data.db.ScanReportEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReportRepository(private val dao: ScanReportDao) {

    val allReports: Flow<List<DiagnosticResult>> = dao.getAllReports().map { list ->
        list.map { entityToResult(it) }
    }

    val reportsCount: Flow<Int> = dao.getReportsCount()

    suspend fun saveReport(result: DiagnosticResult): Long {
        val entity = ScanReportEntity(
            patientRef = result.patientRef,
            scanName = result.scanName,
            scanType = result.scanType,
            timestamp = result.timestamp,
            svmClass = result.svmPrediction.predictedClass.name,
            svmConfidence = result.svmPrediction.confidence,
            dtClass = result.dtPrediction.predictedClass.name,
            dtConfidence = result.dtPrediction.confidence,
            agreementStatus = result.agreementStatus.name,
            finalCategory = result.finalCategory?.name,
            meanIntensity = result.extractedFeatures.meanIntensity,
            glcmContrast = result.extractedFeatures.glcmContrast,
            glcmHomogeneity = result.extractedFeatures.glcmHomogeneity,
            calcificationIndex = result.extractedFeatures.calcificationIndex,
            imageUri = result.imageUri,
            samplePresetName = result.samplePresetName
        )
        return dao.insertReport(entity)
    }

    suspend fun deleteReport(id: Long) {
        dao.deleteReportById(id)
    }

    suspend fun clearAll() {
        dao.deleteAllReports()
    }

    private fun entityToResult(entity: ScanReportEntity): DiagnosticResult {
        val svmClass = try {
            KidneyDiseaseClass.valueOf(entity.svmClass)
        } catch (_: Exception) {
            KidneyDiseaseClass.NORMAL
        }

        val dtClass = try {
            KidneyDiseaseClass.valueOf(entity.dtClass)
        } catch (_: Exception) {
            KidneyDiseaseClass.NORMAL
        }

        val agreement = try {
            AgreementStatus.valueOf(entity.agreementStatus)
        } catch (_: Exception) {
            AgreementStatus.AGREEMENT
        }

        val finalCat = entity.finalCategory?.let {
            try { KidneyDiseaseClass.valueOf(it) } catch (_: Exception) { null }
        }

        return DiagnosticResult(
            id = entity.id,
            patientRef = entity.patientRef,
            scanName = entity.scanName,
            scanType = entity.scanType,
            timestamp = entity.timestamp,
            svmPrediction = ModelPrediction(
                modelName = "Support Vector Machine (SVM)",
                predictedClass = svmClass,
                confidence = entity.svmConfidence,
                classProbabilities = mapOf(svmClass to entity.svmConfidence),
                decisionPathSummary = "Support vector hyperplane decision margin"
            ),
            dtPrediction = ModelPrediction(
                modelName = "Decision Tree (CART)",
                predictedClass = dtClass,
                confidence = entity.dtConfidence,
                classProbabilities = mapOf(dtClass to entity.dtConfidence),
                decisionPathSummary = "Hierarchical entropy split rules"
            ),
            agreementStatus = agreement,
            finalCategory = finalCat,
            extractedFeatures = ExtractedFeatures(
                meanIntensity = entity.meanIntensity,
                stdDev = 18.4f,
                skewness = 0.22f,
                kurtosis = 2.45f,
                glcmContrast = entity.glcmContrast,
                glcmEnergy = 0.42f,
                glcmHomogeneity = entity.glcmHomogeneity,
                glcmEntropy = 3.82f,
                calcificationIndex = entity.calcificationIndex,
                hypodenseRatio = 0.12f,
                edgeRoughness = 14.6f
            ),
            imageUri = entity.imageUri,
            samplePresetName = entity.samplePresetName
        )
    }
}
