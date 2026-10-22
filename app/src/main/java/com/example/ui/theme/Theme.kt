package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DispatchGoLightScheme = lightColorScheme(
    primary = DeepBlue,
    onPrimary = Color.White,
    primaryContainer = DeepBlueContainer,
    onPrimaryContainer = DeepBlueDark,

    secondary = AccentOrange,
    onSecondary = Color.White,
    secondaryContainer = AccentOrangeContainer,
    onSecondaryContainer = AccentOrangeDark,

    tertiary = StatusInfo,
    onTertiary = Color.White,
    tertiaryContainer = StatusInfoContainer,
    onTertiaryContainer = Color(0xFF0369A1),

    background = LogisticsBg,
    onBackground = SlateTextPrimary,

    surface = LogisticsSurface,
    onSurface = SlateTextPrimary,
    surfaceVariant = LogisticsSurfaceVariant,
    onSurfaceVariant = SlateTextSecondary,

    outline = LogisticsBorder,
    outlineVariant = Color(0xFFCBD5E1),

    error = StatusError,
    onError = Color.White,
    errorContainer = StatusErrorContainer,
    onErrorContainer = Color(0xFF991B1B)
)

private val DispatchGoDarkScheme = darkColorScheme(
    primary = DeepBlueLight,
    onPrimary = Color(0xFF0B1736),
    primaryContainer = DeepBlueDark,
    onPrimaryContainer = Color(0xFFBFDBFE),

    secondary = AccentOrangeLight,
    onSecondary = Color(0xFF431407),
    secondaryContainer = AccentOrangeDark,
    onSecondaryContainer = Color(0xFFFFEDD5),

    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF082F49),

    background = Color(0xFF0B1120),
    onBackground = Color(0xFFF1F5F9),

    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),

    outline = Color(0xFF334155),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

@Composable
fun DispatchGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DispatchGoDarkScheme else DispatchGoLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
