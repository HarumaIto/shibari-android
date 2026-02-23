package com.betsudotai.shibari.presentation.ui.theme

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

// ★ ダークモードの配色設定
private val DarkColorScheme = darkColorScheme(
    primary = TacticalRedDark,
    secondary = AchievementGold,
    tertiary = SuccessNeonGreen,

    background = SlateBackgroundDark,
    surface = SlateSurfaceDark,
    surfaceVariant = SlateSurfaceVariantDark,

    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFF1F5F9), // 真っ白ではなく少し落ち着いた文字色
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF94A3B8) // サブテキスト用
)

// ★ ライトモードの配色設定
private val LightColorScheme = lightColorScheme(
    primary = TacticalRedLight,
    secondary = AchievementGold,
    tertiary = SuccessNeonGreen,

    background = SlateBackgroundLight,
    surface = SlateSurfaceLight,
    surfaceVariant = SlateSurfaceVariantLight,

    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569) // サブテキスト用
)

@Composable
fun ShibariTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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