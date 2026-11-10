package com.novareels.app.ui.profile

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.novareels.app.data.Repo
import com.novareels.app.ui.icons.NIcons
import com.novareels.app.ui.theme.Montserrat
import com.novareels.app.ui.theme.NovaAccent
import com.novareels.app.ui.theme.NovaGlassBorder
import com.novareels.app.ui.theme.NovaSurface
import com.novareels.app.ui.theme.NovaSurfaceHigh
import com.novareels.app.ui.theme.NovaTextDim
import com.novareels.app.ui.theme.avatarBrush
import com.novareels.app.util.Fmt
import com.novareels.app.util.Haptics
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen(
    handle: String,
    onBack: () -> Unit,
    onOpenVideo: (String) -> Unit,
    onMessage: (String) -> Unit,
    onOpenFollowList: (String) -> Unit = {}
) {
    val ctx = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val videos by Repo.db.videoDao().all().collectAsStateWithLifecycle(initialValue = emptyList())
    val follows by Repo.db.followDao().all().collectAsStateWithLifecycle(initialValue = emptyList())
    val mine = remember(videos) { videos.filter { it.author == handle && !it.hidden } }
    val hue = remember(handle, videos) {
        videos.firstOrNull { it.author == handle }?.authorHue ?: (handle.hashCode() % 360).toFloat()
    }
    val following = follows.firstOrNull { it.handle == handle }?.following == true
    var followers by remember(handle) { mutableStateOf(1200 + (handle.hashCode() % 900)) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                NIcons.Chevron, "Atrás",
                Modifier
                    .size(24.dp)
                    .clickable(onClick = onBack),
                tint = Color.White
            )
            Spacer(Modifier.width(10.dp))
            Text(
                handle, color = Color.White, fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold, fontFamily = Montserrat
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                Column {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(avatarBrush(hue))
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offsetUp(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            Modifier
                                .size(78.dp)
                                .clip(CircleShape)
                                .background(NovaSurfaceHigh)
                                .border(3.dp, Color.Black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                handle.removePrefix("@").take(1).uppercase(),
                                color = Color.White, fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (following) NovaSurface else NovaAccent)
                                .border(1.dp, NovaGlassBorder, RoundedCornerShape(50))
                                .clickable {
                                    Haptics.tap(haptics)
                                    scope.launch {
                                        Repo.setFollowing(handle, hue, !following)
                                        followers += if (following) -1 else 1
                                    }
                                }
                                .padding(horizontal = 20.dp, vertical = 9.dp)
                        ) {
                            Text(
                                if (following) "Siguiendo" else "Seguir",
                                color = Color.White, fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(NovaSurface)
                                .border(1.dp, NovaGlassBorder, RoundedCornerShape(50))
                                .clickable { onMessage(handle) }
                                .padding(horizontal = 16.dp, vertical = 9.dp)
                        ) {
                            Text(
                                "Mensaje", color = Color.White, fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            handle, color = Color.White, fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Creador de reels · $handle",
                            color = NovaTextDim, fontSize = 12.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                            Stat("${mine.size}", "Reels")
                            Stat(
                                Fmt.count(followers.toLong()), "Seguidores",
                                Modifier.clickable { onOpenFollowList("followers") }
                            )
                            Stat(
                                Fmt.count((followers / 3).toLong()), "Siguiendo",
                                Modifier.clickable { onOpenFollowList("following") }
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(NovaSurface)
                                    .border(1.dp, NovaGlassBorder, RoundedCornerShape(50))
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text("Reels", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(NovaSurface)
                                    .border(1.dp, NovaGlassBorder, RoundedCornerShape(50))
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text("Reposts", color = NovaTextDim, fontSize = 11.5.sp)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
            items(mine) { v ->
                Box(
                    Modifier
                        .aspectRatio(0.66f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NovaSurface)
                        .clickable { onOpenVideo(v.id) }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(ctx)
                            .data(java.io.File(v.url))
                            .decoderFactory(VideoFrameDecoder.Factory())
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        error = androidx.compose.ui.graphics.painter.ColorPainter(NovaSurfaceHigh),
                        modifier = Modifier.fillMaxSize()
                    )
                    Row(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(NIcons.Play, null, Modifier.size(11.dp), tint = Color.White)
                        Spacer(Modifier.width(3.dp))
                        Text(
                            Fmt.count(v.views), color = Color.White, fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = NovaTextDim, fontSize = 10.5.sp)
    }
}

private fun Modifier.offsetUp(): Modifier = this.then(
    Modifier.padding(top = 0.dp)
)

@Composable
fun FollowListScreen(
    kind: String,
    onBack: () -> Unit,
    onOpenUser: (String) -> Unit
) {
    val following by Repo.db.followDao().following().collectAsStateWithLifecycle(initialValue = emptyList())
    val followers by Repo.db.followDao().followers().collectAsStateWithLifecycle(initialValue = emptyList())
    val list = if (kind == "followers") followers else following

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                NIcons.Chevron, "Atrás",
                Modifier
                    .size(24.dp)
                    .clickable(onClick = onBack),
                tint = Color.White
            )
            Spacer(Modifier.width(12.dp))
            Text(
                if (kind == "followers") "Seguidores" else "Siguiendo",
                color = Color.White, fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold, fontFamily = Montserrat
            )
        }
        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (kind == "followers") "Aún no tienes seguidores"
                    else "Aún no sigues a nadie",
                    color = NovaTextDim, fontSize = 12.5.sp
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(list) { f ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenUser(f.handle) }
                            .padding(horizontal = 16.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(avatarBrush(f.hue)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                f.handle.removePrefix("@").take(1).uppercase(),
                                color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                f.handle, color = Color.White, fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (f.followsMe) "Te sigue" else "No te sigue",
                                color = NovaTextDim, fontSize = 11.sp
                            )
                        }
                        Icon(NIcons.Chevron, null, Modifier.size(18.dp), tint = NovaTextDim)
                    }
                }
            }
        }
    }
}
