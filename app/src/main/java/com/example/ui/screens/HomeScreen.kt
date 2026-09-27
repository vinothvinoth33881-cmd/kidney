package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AgreementStatus
import com.example.data.KidneyDiseaseClass
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.DiagnosisCyst
import com.example.ui.theme.DiagnosisNormal
import com.example.ui.theme.DiagnosisStone
import com.example.ui.theme.DiagnosisTumor
import com.example.ui.theme.MedicalCyan

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val reports by viewModel.historyReports.collectAsState()
    val scrollState = rememberScrollState()

    val totalScans = reports.size
    val normalCount = reports.count { it.finalCategory == KidneyDiseaseClass.NORMAL }
    val abnormalCount = reports.count { it.finalCategory != null && it.finalCategory != KidneyDiseaseClass.NORMAL }
    val agreementCount = reports.count { it.agreementStatus == AgreementStatus.AGREEMENT }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Hero Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.kidney_hero_banner),
                        contentDescription = "Futuristic Kidney AI Diagnostics Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xCC070D1E),
                                        Color(0xFF070D1E)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xBB0B132B))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MedicalCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ML CLINICAL RESEARCH ENGINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalCyan,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI-Powered Kidney Disease Diagnosis",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Intelligent Medical Image Analysis Using Machine Learning (Support Vector Machine & Decision Tree)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onNavigate(AppScreen.DIAGNOSIS) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MedicalCyan,
                                contentColor = Color(0xFF031024)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Analysis", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onNavigate(AppScreen.MODEL_INFO) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MedicalCyan
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ML Models", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diagnostic Metrics Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Total Scans",
                value = totalScans.toString(),
                subtitle = "Historical archives",
                color = MedicalCyan,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Normal",
                value = normalCount.toString(),
                subtitle = "Homogeneous",
                color = DiagnosisNormal,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Abnormal",
                value = abnormalCount.toString(),
                subtitle = "Pathological",
                color = DiagnosisStone,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Medical Pipeline Flow Diagram
        Text(
            text = "AI DIAGNOSTIC WORKFLOW PIPELINE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MedicalCyan,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                WorkflowStepItem(
                    step = "1",
                    title = "Upload CT/MRI Scan",
                    detail = "DICOM/PNG/JPEG tomography cross-section validation & dimensions check",
                    icon = Icons.Default.CloudUpload
                )
                WorkflowDivider()
                WorkflowStepItem(
                    step = "2",
                    title = "Radiological Preprocessing",
                    detail = "Spatial 128x128 resize, 3x3 Gaussian smoothing, CLAHE contrast stretch",
                    icon = Icons.Default.FilterDrama
                )
                WorkflowDivider()
                WorkflowStepItem(
                    step = "3",
                    title = "Radiomic Feature Extraction",
                    detail = "GLCM contrast/homogeneity/entropy, HU density statistics, Sobel gradients",
                    icon = Icons.Default.Grain
                )
                WorkflowDivider()
                WorkflowStepItem(
                    step = "4",
                    title = "SVM + Decision Tree Classification",
                    detail = "Dual independent classifiers compute class probabilities & decision margins",
                    icon = Icons.Default.Science
                )
                WorkflowDivider()
                WorkflowStepItem(
                    step = "5",
                    title = "Conservative Agreement Logic",
                    detail = "Final diagnosis generated ONLY when SVM & DT agree; flags disagreements",
                    icon = Icons.Default.CheckCircleOutline
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Supported Diagnostic Categories
        Text(
            text = "SUPPORTED PATHOLOGICAL CLASSES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MedicalCyan,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KidneyDiseaseClass.values().forEach { category ->
                DiseaseClassCard(category)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Academic Disclaimer
        MedicalDisclaimerCard()

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WorkflowStepItem(
    step: String,
    title: String,
    detail: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MedicalCyan.copy(alpha = 0.15f))
                .border(1.dp, MedicalCyan.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MedicalCyan
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun WorkflowDivider() {
    Box(
        modifier = Modifier
            .padding(start = 15.dp, top = 2.dp, bottom = 2.dp)
            .width(2.dp)
            .height(16.dp)
            .background(MedicalCyan.copy(alpha = 0.25f))
    )
}

@Composable
private fun DiseaseClassCard(category: KidneyDiseaseClass) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(category.color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = category.color
                )
                Text(
                    text = category.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}
