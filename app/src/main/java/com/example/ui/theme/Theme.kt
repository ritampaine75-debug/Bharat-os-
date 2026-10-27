package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val BharatLightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECE0),
    onPrimaryContainer = Color(0xFF4E1600),
    secondary = AshokaNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE6F5),
    onSecondaryContainer = Color(0xFF041426),
    tertiary = DeccanEmerald,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD2EBD9),
    onTertiaryContainer = Color(0xFF052115),
    background = WarmCreamBackground,
    onBackground = TextCharcoalPrimary,
    surface = WarmCreamSurface,
    onSurface = TextCharcoalPrimary,
    surfaceVariant = WarmCreamSurfaceVariant,
    onSurfaceVariant = TextCharcoalSecondary,
    outline = WarmCreamOutline
)

private val BharatDarkColorScheme = darkColorScheme(
    primary = SaffronLight,
    onPrimary = Color(0xFF3E1200),
    primaryContainer = SaffronDark,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFF90B4E8),
    onSecondary = Color(0xFF07192F),
    secondaryContainer = AshokaNavy,
    onSecondaryContainer = Color(0xFFDCE6F5),
    tertiary = Color(0xFF75C79E),
    onTertiary = Color(0xFF062B1D),
    tertiaryContainer = DeccanEmerald,
    onTertiaryContainer = Color(0xFFD2EBD9),
    background = CharcoalBackground,
    onBackground = TextLightPrimary,
    surface = CharcoalSurface,
    onSurface = TextLightPrimary,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = TextLightSecondary,
    outline = CharcoalOutline
)

@Composable
fun BharatOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> BharatDarkColorScheme
        else -> BharatLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
