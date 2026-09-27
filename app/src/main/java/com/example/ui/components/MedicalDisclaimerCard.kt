package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DiagnosisStone

@Composable
fun MedicalDisclaimerCard(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DiagnosisStone.copy(alpha = 0.08f))
            .border(1.dp, DiagnosisStone.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(if (compact) 10.dp else 14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = "Medical Disclaimer",
                tint = DiagnosisStone,
                modifier = Modifier
                    .size(if (compact) 20.dp else 24.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "ACADEMIC RESEARCH PROTOTYPE — MEDICAL DISCLAIMER",
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DiagnosisStone,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "This application is an AI-assisted academic prototype designed for machine learning research demonstration. It is NOT a medical device, does NOT provide clinically validated diagnoses, and must never replace the clinical judgment of a licensed radiologist or nephrologist.",
                    fontSize = if (compact) 11.sp else 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
