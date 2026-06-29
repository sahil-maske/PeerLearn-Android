package com.sahilmaske.peerlearn.ui.theme

import android.app.Activity
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
import com.sahilmaske.peerlearn.ui.theme.AppColors

private val CustomColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.TextWhite,
    primaryContainer = AppColors.PrimaryContainer,
    onPrimaryContainer = AppColors.TextPrimary,
    secondary = AppColors.Secondary,
    onSecondary = AppColors.TextWhite,
    secondaryContainer = AppColors.SecondaryContainer,
    onSecondaryContainer = AppColors.TextPrimary,
    tertiary = AppColors.Tertiary,
    onTertiary = AppColors.TextWhite,
    tertiaryContainer = AppColors.TertiaryContainer,
    onTertiaryContainer = AppColors.TextPrimary,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.Surface,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.Divider,
    error = AppColors.Error,
    onError = AppColors.TextWhite
)

private val DarkColorScheme = CustomColorScheme
private val LightColorScheme = CustomColorScheme
@Composable
fun PeerLearnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // false kar do
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}