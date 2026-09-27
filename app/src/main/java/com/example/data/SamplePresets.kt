package com.example.data

import com.example.R

object SamplePresets {

    val presets = listOf(
        SampleScanPreset(
            id = "normal_preset",
            title = "Case 1: Normal Bilateral Kidney CT",
            description = "Axial abdominal CT showing symmetric renal parenchyma with intact corticomedullary margin and no nephrolithiasis.",
            expectedClass = KidneyDiseaseClass.NORMAL,
            drawableResId = R.drawable.sample_scan_normal,
            patientId = "PAT-2026-N104"
        ),
        SampleScanPreset(
            id = "stone_preset",
            title = "Case 2: Nephrolithiasis (Kidney Stone)",
            description = "High-attenuation radio-opaque calculus lodged in upper renal calyx with localized acoustic impedance interface.",
            expectedClass = KidneyDiseaseClass.KIDNEY_STONE,
            drawableResId = R.drawable.sample_scan_stone,
            patientId = "PAT-2026-S308"
        ),
        SampleScanPreset(
            id = "cyst_preset",
            title = "Case 3: Benign Renal Cyst",
            description = "Well-circumscribed homogeneous fluid-density lesion (-5 to 15 HU) adhering to Bosniak Category I criteria.",
            expectedClass = KidneyDiseaseClass.KIDNEY_CYST,
            drawableResId = R.drawable.sample_scan_cyst,
            patientId = "PAT-2026-C512"
        ),
        SampleScanPreset(
            id = "tumor_preset",
            title = "Case 4: Renal Cell Parenchymal Mass",
            description = "Heterogeneous enhancing soft-tissue parenchymal distortion with irregular border architecture and high GLCM contrast.",
            expectedClass = KidneyDiseaseClass.KIDNEY_TUMOR,
            drawableResId = R.drawable.sample_scan_tumor,
            patientId = "PAT-2026-T881"
        )
    )
}
