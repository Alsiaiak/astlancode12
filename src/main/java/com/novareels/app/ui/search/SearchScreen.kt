package com.novareels.app.ui.search

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.novareels.app.data.Video
import com.novareels.app.ui.icons.NIcons
import com.novareels.app.ui.theme.Montserrat
import com.novareels.app.ui.theme.NovaAccent
import com.novareels.app.ui.theme.NovaGlass
import com.novareels.app.ui.theme.NovaGlassBorder
import com.novareels.app.ui.theme.NovaSurface
import com.novareels.app.ui.theme.NovaTextDim
import com.novareels.app.util.Fmt

private val sugerencias = listOf(
    "viajes", "edición", "amanecer", "música", "ciudad", "atardecer", "POV", "4K"
)

@Composable
fun SearchScreen(
    videos: List<Video>,
    onOpen: (String) -> Unit,
    onOpenUser: (String) -> Unit = {}
) {
    val ctx = LocalContext.current
    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()

    val results = remember(videos, q) {
        if (q.isEmpty()) videos.filter { !it.hidden }
        else videos.filter { v ->
            !v.hidden && (
                v.caption.lowercase().contains(q) ||
                    v.author.lowercase().contains(q) ||
                    v.music.lowercase().contains(q)
                )
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(top = 10.dp)
    ) {
        Text(
            "Buscar",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 26.sp,
            fontFamily = Montserrat,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(NovaSurface)
                .border(1.dp, NovaGlassBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(NIcons.Search, null, Modifier.size(19.dp), tint = NovaTextDim)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text("Busca reels, cuentas o sonidos", color = NovaTextDim, fontSize = 14.sp)
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it.take(40) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(NovaAccent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    NIcons.Close, "Limpiar",
                    Modifier
                        .size(18.dp)
                        .clickable { query = "" },
                    tint = NovaTextDim
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sugerencias.take(4).forEach { s ->
                Chip(s) { query = s }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (q.isEmpty()) "Tendencias" else "Resultados",
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${results.size} reels", color = NovaTextDim, fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(10.dp))
        if (results.isEmpty()) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(NIcons.Search, null, Modifier.size(40.dp), tint = NovaTextDim)
                Spacer(Modifier.height(10.dp))
                Text("Sin resultados para \"$query\"", color = Color.White, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text("Prueba con otra palabra", color = NovaTextDim, fontSize = 12.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 14.dp, end = 14.dp, bottom = 110.dp
                )
            ) {
                items(results, key = { it.id }) { v ->
                    Box(
                        Modifier
                            .padding(3.dp)
                            .aspectRatio(0.62f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NovaSurface)
                            .clickable { onOpen(v.id) }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(ctx)
                                .data(v.url)
                                .decoderFactory(VideoFrameDecoder.Factory())
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            error = androidx.compose.ui.graphics.painter.ColorPainter(NovaSurface),
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(NIcons.Play, null, Modifier.size(11.dp), tint = Color.White)
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    Fmt.count(v.views), color = Color.White,
                                    fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(NovaGlass)
            .border(1.dp, NovaGlassBorder, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text("#$text", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
    }
}
