package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
  primary = CyberMagenta,
  onPrimary = Color.White,
  primaryContainer = CyberViolet,
  onPrimaryContainer = Color.White,
  secondary = CyberCyan,
  onSecondary = Color.Black,
  tertiary = CyberGold,
  onTertiary = Color.Black,
  background = DarkBackground,
  onBackground = LightText,
  surface = DarkSurface,
  onSurface = LightText,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DimText
)

@Composable
fun ColorRush3DTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = CyberColorScheme,
    typography = Typography,
    content = content
  )
}

