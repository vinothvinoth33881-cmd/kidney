package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.MedicalCyan

@Composable
fun AboutScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Screen Header
        Text(
            text = "ACADEMIC PROJECT PROTOTYPE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MedicalCyan,
            letterSpacing = 1.sp
        )
        Text(
            text = "AI-Powered Medical Image Diagnosis System for Kidney Disease",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Machine learning classification using Support Vector Machine (SVM) and Decision Tree algorithms on renal radiological imaging.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        SectionCard(
            title = "1. Abstract",
            content = "Kidney diseases such as nephrolithiasis (stones), renal cysts, and renal cell carcinomas represent significant global health burdens. Early, accurate radiological detection from abdominal CT and MRI scans is vital for preventing renal failure and guiding surgical intervention. This project implements an intelligent, conservative diagnostic pipeline using dual classical machine learning algorithms: Support Vector Machine (SVM) with an RBF kernel and a hierarchical CART Decision Tree. The system extracts multi-order statistical radiomics and Gray-Level Co-occurrence Matrix (GLCM) texture metrics to classify scans into Normal, Stone, Cyst, or Tumor classes with conservative model agreement verification."
        )

        Spacer(modifier = Modifier.height(12.dp))

        SectionCard(
            title = "2. Existing System vs. Proposed System",
            content = "Existing Systems:\n• Manual radiologist screening of hundreds of CT slices leads to diagnostic fatigue and inter-observer variability.\n• Many automated tools operate as uncalibrated black-boxes that force uncertain predictions without consistency checks.\n\nProposed System:\n• Multi-stage image normalization (Gaussian noise reduction, CLAHE contrast enhancement, Sobel ROI segmentation).\n• Explainable radiomics feature vector (GLCM contrast, homogeneity, entropy, calcification index).\n• Conservative Model Agreement Engine: flags disagreements between SVM and Decision Tree for mandatory expert radiologist review, avoiding autonomous diagnostic errors."
        )

        Spacer(modifier = Modifier.height(12.dp))

        SectionCard(
            title = "3. Machine Learning Algorithms",
            content = "Support Vector Machine (SVM):\n• Maps 8-dimensional radiomics feature vectors into Hilbert space using a Radial Basis Function (RBF) kernel K(x, z) = exp(-γ||x - z||²).\n• One-vs-Rest decision hyperplanes with Platt scaling for statistically calibrated confidence scores.\n\nDecision Tree (CART):\n• Recursively partitions feature space using Gini impurity splits.\n• Provides transparent, rule-based clinical justification (e.g., calcification threshold > 0.55% separates stones; fluid attenuation separates cysts)."
        )

        Spacer(modifier = Modifier.height(12.dp))

        SectionCard(
            title = "4. Radiomic Feature Extraction",
            content = "• Intensity Statistics: Mean luminance, standard deviation, skewness (asymmetry), kurtosis (tailedness).\n• Gray-Level Co-occurrence Matrix (GLCM): Contrast (local variations), Homogeneity (smoothness), Energy (uniformity), and Entropy (textural disorder).\n• Density Attenuation Features: Calcification Attenuation Index (Hounsfield Unit approximation for radio-opaque calculi) and Hypodense Fluid attenuation ratio (Bosniak cyst profiling)."
        )

        Spacer(modifier = Modifier.height(12.dp))

        SectionCard(
            title = "5. Key Advantages",
            content = "• Conservative Diagnostic Safety: Never forces a classification if the two algorithms disagree.\n• Zero Heavy External Dependencies: Runs natively with deterministic mathematical accuracy on-device.\n• Complete Privacy: Medical scans and patient reports remain securely in local Room database without external cloud telemetry.\n• Multi-Stage Preprocessing Inspection: Users can visually verify raw, denoised, CLAHE-enhanced, and ROI edge-detected filter stages."
        )

        Spacer(modifier = Modifier.height(12.dp))

        SectionCard(
            title = "6. Limitations & Future Enhancements",
            content = "Limitations:\n• Classical ML features are sensitive to variations in scanner calibration (kVp, slice thickness).\n• Currently tested on 2D axial slices rather than volumetric 3D DICOM series.\n\nFuture Enhancements:\n• Deep Learning CNN & Vision Transformer (ViT) ensemble models for end-to-end feature discovery.\n• Native PACS / DICOM protocol integration for hospital radiology workflow deployment.\n• Multi-center clinical trials with board-certified radiologist feedback loops."
        )

        Spacer(modifier = Modifier.height(16.dp))

        MedicalDisclaimerCard()

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MedicalCyan
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 17.sp
            )
        }
    }
}
