package com.novareels.app.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.novareels.app.ui.icons.NIcons
import com.novareels.app.ui.theme.Montserrat
import com.novareels.app.ui.theme.NovaAccent
import com.novareels.app.ui.theme.NovaGlassBorder
import com.novareels.app.ui.theme.NovaSurface
import com.novareels.app.ui.theme.NovaSurfaceHigh
import com.novareels.app.ui.theme.NovaTextDim
import java.io.File

data class EditResult(val filter: Int, val startMs: Int, val endMs: Int)

private val filtros = listOf("Original", "Cálido", "Frío", "Mono")

@Composable
fun EditorScreen(
    file: File,
    initialFilter: Int,
    onCancel: () -> Unit,
    onApply: (EditResult) -> Unit
) {
    val ctx = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(MediaItem.fromUri(file.toURI().toString()))
            prepare()
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            playWhenReady = true
        }
    }
    var filter by remember { mutableStateOf(initialFilter) }
    var start by remember { mutableStateOf(0f) }
    var end by remember { mutableStateOf(1f) }
    var durationMs by remember { mutableStateOf(1L) }
    LaunchedEffect(player) {
        while (durationMs <= 1L) {
            val d = player.duration
            if (d > 0 && d != androidx.media3.common.C.TIME_UNSET) durationMs = d
            else kotlinx.coroutines.delay(200)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Cancelar", color = NovaTextDim, fontSize = 13.sp,
                modifier = Modifier.clickable {
                    player.release()
                    onCancel()
                }
            )
            Spacer(Modifier.weight(1f))
            Text(
                "Editar reel", color = Color.White, fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold, fontFamily = Montserrat
            )
            Spacer(Modifier.weight(1f))
            Text(
                "Listo", color = NovaAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    player.release()
                    onApply(
                        EditResult(
                            filter = filter,
                            startMs = (start * durationMs).toInt(),
                            endMs = (end * durationMs).toInt()
                        )
                    )
                }
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(18.dp))
                .background(NovaSurfaceHigh)
                .border(1.dp, NovaGlassBorder, RoundedCornerShape(18.dp))
        ) {
            AndroidView(
                factory = { c ->
                    PlayerView(c).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setPlayer(player)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(
                    filtros[filter], color = Color.White,
                    fontSize = 10.5.sp, fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Filtro", color = Color.White, fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            filtros.forEachIndexed { i, name ->
                val sel = filter == i
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { filter = i }
                ) {
                    Box(
                        Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when (i) {
                                    0 -> NovaSurfaceHigh
                                    3 -> Color(0xFF6B6B72)
                                    1 -> Color(0xFFC98A4B)
                                    else -> Color(0xFF4B7BC9)
                                }
                            )
                            .border(
                                if (sel) 2.dp else 1.dp,
                                if (sel) NovaAccent else NovaGlassBorder,
                                RoundedCornerShape(14.dp)
                            )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        name, color = if (sel) Color.White else NovaTextDim,
                        fontSize = 10.sp,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            "Recorte", color = Color.White, fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Inicio ${(start * durationMs / 1000).toInt()}s · Fin ${(end * durationMs / 1000).toInt()}s",
                color = NovaTextDim, fontSize = 11.sp
            )
            Slider(
                value = start,
                onValueChange = { if (it < end - 0.05f) start = it },
                colors = SliderDefaults.colors(
                    thumbColor = NovaAccent, activeTrackColor = NovaAccent,
                    inactiveTrackColor = NovaSurfaceHigh
                )
            )
            Slider(
                value = end,
                onValueChange = { if (it > start + 0.05f) end = it },
                colors = SliderDefaults.colors(
                    thumbColor = NovaAccent, activeTrackColor = NovaAccent,
                    inactiveTrackColor = NovaSurfaceHigh
                )
            )
        }

        Spacer(Modifier.weight(1f))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(NovaSurface)
                    .border(1.dp, NovaGlassBorder, RoundedCornerShape(23.dp))
                    .clickable {
                        filter = 0; start = 0f; end = 1f
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Restablecer", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                Modifier
                    .weight(1.3f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(NovaAccent)
                    .clickable {
                        player.release()
                        onApply(
                            EditResult(
                                filter = filter,
                                startMs = (start * durationMs).toInt(),
                                endMs = (end * durationMs).toInt()
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Aplicar edición", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun AudioPickerSheet(
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    val saved = remember {
        com.novareels.app.data.Repo.savedSounds.split("|").filter { it.isNotBlank() }
    }
    val trending = listOf(
        "Sonido original — @luna.crea",
        "Neon Drive — @kike.beats",
        "Midnight — @sofi.films",
        "Aurora — @valen.motion",
        "Pulse 128 — @mario.edits"
    )
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Text(
            "Usar audio", color = Color.White, fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(12.dp))
        if (saved.isNotEmpty()) {
            Text("Guardados", color = NovaTextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            saved.take(4).forEach { s ->
                AudioRow(s, onPick)
            }
            Spacer(Modifier.height(10.dp))
        }
        Text("Tendencias", color = NovaTextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        trending.forEach { s -> AudioRow(s, onPick) }
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(NovaSurface)
                .border(1.dp, NovaGlassBorder, RoundedCornerShape(14.dp))
                .clickable { onPick("Grabar audio propio") }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(NIcons.Mic, null, Modifier.size(18.dp), tint = NovaAccent)
            Spacer(Modifier.width(10.dp))
            Text(
                "Grabar audio propio", color = Color.White,
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun AudioRow(name: String, onPick: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onPick(name) }
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(NovaSurfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(NIcons.Music, null, Modifier.size(16.dp), tint = NovaAccent)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            name, color = Color.White, fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium, maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Icon(NIcons.Play, null, Modifier.size(15.dp), tint = NovaTextDim)
    }
}
