package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgreementStatus
import com.example.data.DiagnosticResult
import com.example.data.ExtractedFeatures
import com.example.data.KidneyDiseaseClass
import com.example.data.ModelPrediction
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.DiagnosisDisagreement
import com.example.ui.theme.DiagnosisNormal
import com.example.ui.theme.DiagnosisStone
import com.example.ui.theme.DiagnosisTumor
import com.example.ui.theme.MedicalCyan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResultScreen(
    viewModel: MainViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val result by viewModel.currentResult.collectAsState()
    val bitmap by viewModel.selectedBitmap.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    if (result == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No diagnostic report selected.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onNavigate(AppScreen.DIAGNOSIS) },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalCyan)
                ) {
                    Text("Start Scan Analysis", color = Color(0xFF031024))
                }
            }
        }
        return
    }

    val res = result!!
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(res.timestamp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DIAGNOSTIC REPORT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MedicalCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "AI Kidney Scan Analysis",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F1A34))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = res.patientRef,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MedicalCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scan Thumbnail & Metadata Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (bitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF060B1A))
                            .border(1.dp, MedicalCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Scan Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = res.scanName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${res.scanType} • $dateStr",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Pipeline duration: ${res.processingDurationMs} ms",
                        fontSize = 10.sp,
                        color = MedicalCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Agreement Status Banner
        AgreementBanner(res.agreementStatus, res.finalCategory)

        Spacer(modifier = Modifier.height(16.dp))

        // Side-by-side Model Results (SVM vs Decision Tree)
        Text(
            text = "DUAL MACHINE LEARNING CLASSIFIERS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MedicalCyan,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModelCard(
                prediction = res.svmPrediction,
                modifier = Modifier.weight(1f)
            )
            ModelCard(
                prediction = res.dtPrediction,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Probability Distribution Visualizer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "MODEL PROBABILITY DISTRIBUTION (CALIBRATED)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MedicalCyan,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                KidneyDiseaseClass.values().forEach { cls ->
                    val svmP = res.svmPrediction.classProbabilities[cls] ?: 0f
                    val dtP = res.dtPrediction.classProbabilities[cls] ?: 0f
                    val avgP = (svmP + dtP) / 2f

                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = cls.displayName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${String.format("%.1f", avgP * 100)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = cls.color
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { avgP.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = cls.color,
                            trackColor = Color(0xFF0F1A34)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Disease Information Card (if agreement reached)
        if (res.finalCategory != null) {
            DiseaseInfoCard(res.finalCategory!!)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Extracted Radiomics Features Breakdown
        FeaturesTableCard(res.extractedFeatures)

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons: Share/Export, New Scan, History
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val shareText = generateReportText(res, dateStr)
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Export Diagnostic Report")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MedicalCyan,
                    contentColor = Color(0xFF031024)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Report", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { onNavigate(AppScreen.DIAGNOSIS) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MedicalCyan
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Scan", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { onNavigate(AppScreen.HISTORY) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderShared,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MedicalCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mandatory Disclaimer
        MedicalDisclaimerCard()

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AgreementBanner(
    status: AgreementStatus,
    finalCategory: KidneyDiseaseClass?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = status.color.copy(alpha = 0.12f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, status.color.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (status.isConsistent) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = status.color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = status.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = status.color
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status.subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (finalCategory != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(finalCategory.color.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PREDICTED CATEGORY:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = finalCategory.displayName.uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = finalCategory.color
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No autonomous diagnosis issued. Due to divergence between Support Vector Machine and Decision Tree models, human radiological review is required.",
                    fontSize = 11.sp,
                    color = DiagnosisDisagreement,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun ModelCard(
    prediction: ModelPrediction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = prediction.modelName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MedicalCyan
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = prediction.predictedClass.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = prediction.predictedClass.color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Model Confidence: ${String.format("%.1f", prediction.confidence * 100)}%",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { prediction.confidence },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = prediction.predictedClass.color,
                trackColor = Color(0xFF0F1A34)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = prediction.decisionPathSummary,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 13.sp,
                maxLines = 3
            )
        }
    }
}

@Composable
private fun DiseaseInfoCard(category: KidneyDiseaseClass) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "CLINICAL RADIOLOGY BRIEFING: ${category.displayName.uppercase()}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = category.color,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = category.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Characteristic CT/MRI Signs:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = category.radiologicSigns,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun FeaturesTableCard(features: ExtractedFeatures) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "EXTRACTED RADIOMICS FEATURE VECTOR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MedicalCyan,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            FeatureRow("Mean Intensity (Luminance)", String.format("%.1f", features.meanIntensity))
            FeatureRow("Intensity StdDev (Variance)", String.format("%.2f", features.stdDev))
            FeatureRow("GLCM Contrast (Texture Roughness)", String.format("%.2f", features.glcmContrast))
            FeatureRow("GLCM Homogeneity (Uniformity)", String.format("%.3f", features.glcmHomogeneity))
            FeatureRow("GLCM Energy (Angular 2nd Moment)", String.format("%.3f", features.glcmEnergy))
            FeatureRow("GLCM Texture Entropy", String.format("%.2f", features.glcmEntropy))
            FeatureRow("Calcification Attenuation Index", "${String.format("%.2f", features.calcificationIndex)}%")
            FeatureRow("Hypodense Fluid Ratio", "${String.format("%.1f", features.hypodenseRatio)}%")
            FeatureRow("Sobel Edge Roughness Magnitude", String.format("%.1f", features.edgeRoughness))
        }
    }
}

@Composable
private fun FeatureRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun generateReportText(result: DiagnosticResult, dateStr: String): String {
    return """
        ========================================================
        AI-POWERED MEDICAL IMAGE DIAGNOSIS SYSTEM (ACADEMIC PROTOTYPE)
        KIDNEY DISEASE CLASSIFICATION REPORT (SVM + DECISION TREE)
        ========================================================
        Patient Reference: ${result.patientRef}
        Scan File Name:    ${result.scanName}
        Modality:          ${result.scanType}
        Date & Time:       $dateStr
        --------------------------------------------------------
        MODEL 1: SUPPORT VECTOR MACHINE (SVM)
        - Classification: ${result.svmPrediction.predictedClass.displayName}
        - Calibrated Confidence: ${String.format("%.1f", result.svmPrediction.confidence * 100)}%
        - Decision Margin: ${result.svmPrediction.decisionPathSummary}
        
        MODEL 2: DECISION TREE (CART)
        - Classification: ${result.dtPrediction.predictedClass.displayName}
        - Calibrated Confidence: ${String.format("%.1f", result.dtPrediction.confidence * 100)}%
        - Splitting Rule Path: ${result.dtPrediction.decisionPathSummary}
        --------------------------------------------------------
        CONSISTENCY EVALUATION: ${result.agreementStatus.title}
        FINAL SYSTEM CATEGORY: ${result.finalCategory?.displayName ?: "DISAGREEMENT - EXPERT REVIEW REQUIRED"}
        --------------------------------------------------------
        RADIOMIC FEATURES:
        - Mean Luminance: ${String.format("%.1f", result.extractedFeatures.meanIntensity)}
        - GLCM Contrast:  ${String.format("%.2f", result.extractedFeatures.glcmContrast)}
        - GLCM Homogeneity: ${String.format("%.3f", result.extractedFeatures.glcmHomogeneity)}
        - Calcification Ratio: ${String.format("%.2f", result.extractedFeatures.calcificationIndex)}%
        --------------------------------------------------------
        MANDATORY MEDICAL DISCLAIMER:
        This output was generated by an AI-assisted academic prototype for
        research demonstration. It is NOT a clinical diagnosis and must never
        replace interpretation by a certified medical radiologist.
        ========================================================
    """.trimIndent()
}
