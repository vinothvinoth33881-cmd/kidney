package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_reports")
data class ScanReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientRef: String,
    val scanName: String,
    val scanType: String,
    val timestamp: Long,
    val svmClass: String,
    val svmConfidence: Float,
    val dtClass: String,
    val dtConfidence: Float,
    val agreementStatus: String,
    val finalCategory: String?,
    val meanIntensity: Float,
    val glcmContrast: Float,
    val glcmHomogeneity: Float,
    val calcificationIndex: Float,
    val imageUri: String? = null,
    val samplePresetName: String? = null
)
