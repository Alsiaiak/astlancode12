@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.novareels.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontVariation
import com.novareels.app.R

val NovaAccent: Color get() = accentState.value
val NovaViolet: Color get() = accentState.value
val NovaPink = Color(0xFFFF5C7A)
val NovaCyan: Color get() = accentState.value
val NovaLime = Color(0xFF3DDC84)
val NovaRed = Color(0xFFFF3B5C)

private val accentState = androidx.compose.runtime.mutableStateOf(Color(0xFF5E7CFF))
private val textScaleState = androidx.compose.runtime.mutableStateOf(1f)

val NovaTextScale: Float get() = textScaleState.value

fun applyAccent(color: Color) {
    accentState.value = color
}

fun applyTextScale(scale: Float) {
    textScaleState.value = scale.coerceIn(0.9f, 1.2f)
}

val accentOptions = listOf(
    "Azul" to Color(0xFF5E7CFF),
    "Violeta" to Color(0xFF9B6BFF),
    "Cian" to Color(0xFF2FD4C4),
    "Ámbar" to Color(0xFFFFB020)
)

val NovaGlass = Color(0x14FFFFFF)
val NovaGlassBorder = Color(0x24FFFFFF)
val NovaStroke = Color(0x1FFFFFFF)
val NovaTextDim = Color(0x99FFFFFF)
val NovaTextFaint = Color(0x66FFFFFF)

val NovaSurface = Color(0xFF0A0A0C)
val NovaSurfaceHigh = Color(0xFF131317)
val NovaSurfaceMax = Color(0xFF1B1B21)

val novaGradient: Brush = SolidColor(NovaAccent)
val novaGradient2: Brush = SolidColor(NovaAccent)
val novaGradient3: Brush = SolidColor(NovaAccent)

fun avatarBrush(hue: Float): Brush = SolidColor(Color.hsl(hue, 0.42f, 0.44f))

val Montserrat = FontFamily(
    Font(
        R.font.montserrat, FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.montserrat, FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.montserrat, FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.montserrat, FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        R.font.montserrat, FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800))
    ),
)

private val novaColors: androidx.compose.material3.ColorScheme
    @androidx.compose.runtime.Composable
    get() = darkColorScheme(
        primary = NovaAccent,
        onPrimary = Color.White,
        secondary = NovaAccent,
        background = Color.Black,
        onBackground = Color.White,
        surface = Color.Black,
        onSurface = Color.White,
        surfaceContainer = NovaSurface,
        surfaceContainerHigh = NovaSurfaceHigh,
        outline = NovaStroke,
        error = NovaRed
    )

@Composable
fun NovaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = novaColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
