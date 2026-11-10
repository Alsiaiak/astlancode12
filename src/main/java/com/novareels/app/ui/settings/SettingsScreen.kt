package com.novareels.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novareels.app.data.Repo
import com.novareels.app.ui.icons.NIcons
import com.novareels.app.ui.theme.Montserrat
import com.novareels.app.ui.theme.NovaAccent
import com.novareels.app.ui.theme.NovaGlassBorder
import com.novareels.app.ui.theme.NovaSurface
import com.novareels.app.ui.theme.NovaTextDim
import com.novareels.app.ui.theme.accentOptions
import com.novareels.app.ui.theme.applyAccent
import com.novareels.app.ui.theme.applyTextScale
import java.io.File

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val cloudOk by Repo.cloudOk.collectAsStateWithLifecycle()
    val status by Repo.cloudStatusText.collectAsStateWithLifecycle()
    var autoplay by remember { mutableStateOf(Repo.autoplay) }
    var muted by remember { mutableStateOf(Repo.muteByDefault) }
    var saver by remember { mutableStateOf(Repo.dataSaver) }
    var haptics by remember { mutableStateOf(Repo.hapticsOn) }
    var loop by remember { mutableStateOf(Repo.loopVideos) }
    var topic by remember { mutableStateOf(Repo.topic()) }
    var cacheSize by remember { mutableStateOf(dirSize(ctx.cacheDir)) }
    var accent by remember { mutableStateOf(Repo.accentIndex) }
    var scale by remember { mutableStateOf(Repo.textScale) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 30.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(NovaSurface)
                    .border(1.dp, NovaGlassBorder, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    NIcons.Chevron, "Volver",
                    Modifier.size(20.dp).rotate(180f),
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Ajustes", color = Color.White,
                fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, fontFamily = Montserrat
            )
        }

        Section("Apariencia")
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(NovaSurface)
                .border(1.dp, NovaGlassBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text("Color de acento", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                accentOptions.forEachIndexed { i, (name, color) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            accent = i
                            Repo.accentIndex = i
                            applyAccent(color)
                            Repo.toastFlow.value = "Acento $name"
                        }
                    ) {
                        Box(
                            Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (accent == i) 3.dp else 1.dp,
                                    if (accent == i) Color.White else NovaGlassBorder,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (accent == i) {
                                Icon(NIcons.Check, null, Modifier.size(18.dp), tint = Color.Black)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            name, color = if (accent == i) Color.White else NovaTextDim,
                            fontSize = 9.5.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Tamaño del texto", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Normal" to 1f, "Grande" to 1.1f, "Enorme" to 1.2f).forEach { (label, value) ->
                    val sel = kotlin.math.abs(scale - value) < 0.01f
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (sel) NovaAccent else Color.White.copy(alpha = 0.08f))
                            .border(1.dp, NovaGlassBorder, RoundedCornerShape(50))
                            .clickable {
                                scale = value
                                Repo.textScale = value
                                applyTextScale(value)
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            label, color = Color.White, fontSize = 11.5.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Section("Reproducción")
        ToggleRow("Reproducción automática", "Los reels se reproducen al abrirlos", autoplay) {
            autoplay = it; Repo.autoplay = it
        }
        ToggleRow("Silenciar al iniciar", "Empieza sin sonido", muted) {
            muted = it; Repo.muteByDefault = it
        }
        ToggleRow("Repetir en bucle", "Vuelve al inicio al terminar", loop) {
            loop = it; Repo.loopVideos = it
        }
        ToggleRow("Ahorro de datos", "No precarga el reel siguiente", saver) {
            saver = it; Repo.dataSaver = it
        }
        ToggleRow("Vibración háptica", "Respuesta al dar like y navegar", haptics) {
            haptics = it; Repo.hapticsOn = it
        }

        Section("Sincronización")
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(NovaSurface)
                .border(1.dp, NovaGlassBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (cloudOk) Color(0xFF3DDC84) else Color(0xFFFF6B6B))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (cloudOk) "Conectado" else "Conectando",
                    color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(status, color = NovaTextDim, fontSize = 10.5.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text("Canal privado", color = NovaTextDim, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(1.dp, NovaGlassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = topic,
                    onValueChange = { topic = it.take(40) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    cursorBrush = SolidColor(NovaAccent),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Aplicar",
                    color = NovaAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        Repo.setTopic(topic)
                        Repo.restartListener()
                        Repo.toastFlow.value = "Canal actualizado"
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Cualquiera con el mismo canal ve tus reels y comentarios en tiempo real.",
                color = NovaTextDim, fontSize = 10.5.sp
            )
        }

        Section("Almacenamiento")
        ActionRow("Borrar caché", "$cacheSize en archivos temporales") {
            ctx.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
            cacheSize = dirSize(ctx.cacheDir)
            Repo.toastFlow.value = "Caché borrada"
        }

        Section("Acerca de")
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(NovaSurface)
                .border(1.dp, NovaGlassBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text(
                "Nova Reels 1.1", color = Color.White,
                fontWeight = FontWeight.Bold, fontSize = 13.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Feed vertical, comentarios con foto, audio y stickers, repost, guardados y descarga con marca de agua. Sincronización gratuita sin cuenta.",
                color = NovaTextDim, fontSize = 11.5.sp, lineHeight = 16.sp
            )
        }
    }
}

private fun dirSize(dir: File): String {
    val bytes = runCatching {
        dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }.getOrDefault(0L)
    return when {
        bytes > 1_000_000 -> "${bytes / 1_000_000} MB"
        bytes > 1_000 -> "${bytes / 1_000} KB"
        else -> "$bytes B"
    }
}

private fun Modifier.unusedRotation(): Modifier = this
@Composable
private fun Section(title: String) {
    Text(
        title.uppercase(),
        color = NovaTextDim, fontSize = 10.5.sp, fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 18.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NovaSurface)
            .border(1.dp, NovaGlassBorder, RoundedCornerShape(16.dp))
            .clickable { onChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = NovaTextDim, fontSize = 10.5.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NovaAccent,
                uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.12f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun ActionRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NovaSurface)
            .border(1.dp, NovaGlassBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = NovaTextDim, fontSize = 10.5.sp)
        }
        Icon(NIcons.Chevron, null, Modifier.size(18.dp), tint = NovaTextDim)
    }
}
