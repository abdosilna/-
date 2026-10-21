package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkPurpleColorScheme = darkColorScheme(
  primary = PurplePrimary,
  onPrimary = TextPrimary,
  primaryContainer = PurpleDark,
  onPrimaryContainer = PurplePrimaryLight,
  secondary = PurplePrimaryLight,
  onSecondary = DarkBg,
  secondaryContainer = DarkSurfaceCard,
  onSecondaryContainer = TextPrimary,
  tertiary = AccentGold,
  onTertiary = DarkBg,
  tertiaryContainer = DarkSurfaceCard,
  onTertiaryContainer = AccentGoldLight,
  background = DarkBg,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = TextSecondary,
  outline = DarkBorder,
  outlineVariant = DarkBorder,
  error = AdminRed,
  onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to stunning dark purple identity
  content: @Composable () -> Unit
) {
  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    MaterialTheme(
      colorScheme = DarkPurpleColorScheme,
      typography = Typography,
      content = content
    )
  }
}
