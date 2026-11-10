package com.novareels.app.ui.comments

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.novareels.app.ui.theme.Montserrat
import com.novareels.app.ui.theme.NovaAccent
import com.novareels.app.ui.theme.NovaCyan
import com.novareels.app.ui.theme.NovaLime
import com.novareels.app.ui.theme.NovaPink
import com.novareels.app.ui.theme.NovaViolet
import com.novareels.app.ui.theme.novaGradient
import kotlin.math.cos
import kotlin.math.sin

enum class Sticker(val label: String) {
    CORAZON("Corazón"),
    FUEGO("Fuego"),
    ESTRELLA("Estrella"),
    LOL("JAJA"),
    CIENTO("100"),
    WOW("WOW");

    companion object {
        fun byId(id: String?): Sticker = entries.firstOrNull { it.name == id } ?: CORAZON
    }
}

@Composable
fun StickerView(sticker: Sticker, size: Dp, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "st")
    val ph by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "ph"
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val s = this.size.minDimension
            val w = this.size.width
            val h = this.size.height
            when (sticker) {
                Sticker.CORAZON -> {
                    val pulse = 1f + 0.12f * sin(ph * 2f * Math.PI.toFloat())
                    scale(pulse, pulse) {
                        val sw = s * 0.8f
                        val x0 = (w - sw) / 2
                        val y0 = (h - sw * 0.85f) / 2
                        val path = Path().apply {
                            moveTo(x0 + sw / 2, y0 + sw * 0.85f)
                            cubicTo(x0 - sw * 0.28f, y0 + sw * 0.32f, x0 + sw * 0.18f, y0 - sw * 0.12f, x0 + sw / 2, y0 + sw * 0.28f)
                            cubicTo(x0 + sw * 0.82f, y0 - sw * 0.12f, x0 + sw * 1.28f, y0 + sw * 0.32f, x0 + sw / 2, y0 + sw * 0.85f)
                            close()
                        }
                        drawPath(path, SolidColor(NovaAccent))
                    }
                }
                Sticker.FUEGO -> {
                    val flick = 1f + 0.07f * sin(ph * 2f * Math.PI.toFloat() * 1.4f)
                    scale(1f, flick) {
                        val path = Path().apply {
                            moveTo(w / 2, s * 0.04f)
                            cubicTo(w * 0.95f, s * 0.45f, w * 0.82f, s * 0.97f, w / 2, s * 0.97f)
                            cubicTo(w * 0.18f, s * 0.97f, w * 0.05f, s * 0.45f, w / 2, s * 0.04f)
                            close()
                        }
                        drawPath(path, SolidColor(NovaPink))
                        val inner = Path().apply {
                            moveTo(w / 2, s * 0.55f)
                            cubicTo(w * 0.68f, s * 0.72f, w * 0.66f, s * 0.97f, w / 2, s * 0.97f)
                            cubicTo(w * 0.34f, s * 0.97f, w * 0.32f, s * 0.72f, w / 2, s * 0.55f)
                            close()
                        }
                        drawPath(inner, SolidColor(Color(0xFFFFC46B)))
                    }
                }
                Sticker.ESTRELLA -> {
                    rotate(ph * 360f) {
                        val n = 5
                        val r = s * 0.42f
                        val c = Offset(w / 2, h / 2)
                        val path = Path()
                        for (i in 0 until n * 2) {
                            val rad = if (i % 2 == 0) r else r * 0.45f
                            val ang = i * Math.PI / n - Math.PI / 2
                            val pt = Offset(c.x + rad * cos(ang).toFloat(), c.y + rad * sin(ang).toFloat())
                            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                        }
                        path.close()
                        drawPath(path, SolidColor(NovaAccent))
                    }
                }
                Sticker.LOL -> {
                    val bounce = 1f + 0.1f * sin(ph * 2f * Math.PI.toFloat())
                    scale(bounce, bounce) {
                        drawCircle(SolidColor(NovaAccent), s * 0.42f, center)
                        drawCircle(Color.Black.copy(alpha = 0.4f), s * 0.42f, center, style = Stroke(s * 0.05f))
                    }
                }
                Sticker.CIENTO -> {
                    drawLine(
                        NovaCyan, Offset(w * 0.14f, h * 0.72f), Offset(w * 0.3f, h * 0.5f),
                        s * 0.075f, StrokeCap.Round
                    )
                    drawLine(
                        NovaPink, Offset(w * 0.7f, h * 0.5f), Offset(w * 0.86f, h * 0.72f),
                        s * 0.075f, StrokeCap.Round
                    )
                    drawCircle(
                        SolidColor(NovaAccent), s * 0.3f * (1f + 0.12f * sin(ph * 2f * Math.PI.toFloat())),
                        center, style = Stroke(s * 0.06f, cap = StrokeCap.Round)
                    )
                }
                Sticker.WOW -> {
                    rotate(sin(ph * 2f * Math.PI.toFloat()) * 20f) {
                        val path = Path().apply {
                            addOval(androidx.compose.ui.geometry.Rect(w / 2 - s * 0.07f, h * 0.2f, w / 2 + s * 0.07f, h * 0.34f))
                            addOval(androidx.compose.ui.geometry.Rect(w / 2 - s * 0.05f, h * 0.42f, w / 2 + s * 0.05f, h * 0.52f))
                        }
                        drawPath(path, NovaViolet)
                    }
                }
            }
        }
        when (sticker) {
            Sticker.LOL -> PulsingText("JAJA", size)
            Sticker.CIENTO -> PulsingText("100", size)
            Sticker.WOW -> PulsingText("WOW", size)
            else -> {}
        }
    }
}

@Composable
private fun PulsingText(text: String, size: Dp) {
    Box(contentAlignment = Alignment.Center) {
        Text(
            text,
            fontSize = (size.value * 0.26f).sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = Montserrat,
            style = TextStyle(
                brush = SolidColor(NovaAccent),
                letterSpacing = 1.sp
            )
        )
    }
}
