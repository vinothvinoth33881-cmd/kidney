package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MedicalCyan,
    onPrimary = Color(0xFF031024),
    primaryContainer = Color(0xFF0E3A5A),
    onPrimaryContainer = Color(0xFFBCE9FF),
    secondary = MedicalBlueSecondary,
    onSecondary = Color(0xFF041929),
    secondaryContainer = Color(0xFF163E63),
    onSecondaryContainer = Color(0xFFCAE6FF),
    tertiary = MedicalIndigo,
    onTertiary = Color.White,
    background = MedicalDarkBg,
    onBackground = Color(0xFFF1F5F9),
    surface = MedicalDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = MedicalDarkCard,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = MedicalDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF4F46E5),
    onTertiary = Color.White,
    background = MedicalLightBg,
    onBackground = MedicalLightText,
    surface = MedicalLightSurface,
    onSurface = MedicalLightText,
    surfaceVariant = MedicalLightCard,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun KidneyAITheme(
    darkTheme: Boolean = true, // default to futuristic medical dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
