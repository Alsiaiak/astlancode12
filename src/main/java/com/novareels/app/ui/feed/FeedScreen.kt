@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.novareels.app.ui.feed

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novareels.app.data.Repo
import com.novareels.app.data.Video
import com.novareels.app.ui.components.BlurBackdrop
import com.novareels.app.ui.components.Dot
import com.novareels.app.ui.icons.NIcons
import com.novareels.app.ui.theme.Montserrat
import com.novareels.app.ui.theme.NovaAccent
import com.novareels.app.ui.theme.NovaGlassBorder
import com.novareels.app.ui.theme.NovaLime
import com.novareels.app.ui.theme.NovaSurface
import com.novareels.app.ui.theme.NovaTextDim
import com.novareels.app.util.Haptics
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    videos: List<Video>,
    commentCounts: Map<String, Int>,
    startingVideoId: String?,
    onOpenComments: (String) -> Unit,
    onRecord: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenUser: (String) -> Unit = {},
    onGoCreate: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var tab by remember { mutableStateOf(0) }
    var muted by remember { mutableStateOf(Repo.muteByDefault) }
    var audioOnly by remember { mutableStateOf(false) }
    var moreFor by remember { mutableStateOf<Video?>(null) }
    var shareFor by remember { mutableStateOf<Video?>(null) }
    var soundFor by remember { mutableStateOf<Video?>(null) }

    val filtered = remember(videos, tab) {
        val visible = videos.filter { !it.hidden }
        if (tab == 1) visible.filter { it.following || it.mine } else visible
    }
    val pagerState = rememberPagerState(pageCount = { filtered.size })

    LaunchedEffect(startingVideoId, filtered.size) {
        val idx = startingVideoId?.let { id -> filtered.indexOfFirst { it.id == id } }
        if (idx != null && idx >= 0 && pagerState.currentPage != idx) {
            pagerState.scrollToPage(idx)
        }
    }

    LaunchedEffect(Unit) {
        androidx.compose.runtime.snapshotFlow { pagerState.currentPage }.collectLatest { page ->
            filtered.getOrNull(page)?.let { Repo.addView(it.id) }
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (filtered.isEmpty()) {
            EmptyFeed()
        }
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            val v = filtered[page]
            Box(Modifier.fillMaxSize()) {
            FeedItem(
                video = v,
                isActive = page == pagerState.currentPage,
                muted = muted,
                commentCount = commentCounts[v.id] ?: 0,
                onLike = {
                    Haptics.strong(haptics)
                    Repo.scope.launch { Repo.likeVideo(ctx, v) }
                },
                onDoubleLike = {
                    Haptics.tap(haptics)
                    if (!v.likedByMe) Repo.scope.launch { Repo.likeVideo(ctx, v) }
                },
                onComment = {
                    Haptics.tap(haptics)
                    onOpenComments(v.id)
                },
                onRepost = {
                    Haptics.strong(haptics)
                    Repo.scope.launch { Repo.repost(v) }
                },
                onDownload = {
                    Haptics.tap(haptics)
                    Repo.scope.launch { Repo.downloadVideo(ctx, v) }
                },
                onShare = {
                    Haptics.tap(haptics)
                    shareFor = v
                },
                onSave = {
                    Haptics.tap(haptics)
                    Repo.scope.launch { Repo.saveVideo(v) }
                },
                onFollow = {
                    Haptics.tap(haptics)
                    Repo.scope.launch { Repo.follow(v) }
                },
                onMore = {
                    Haptics.tap(haptics)
                    moreFor = v
                },
                onOpenUser = onOpenUser,
                onUseSound = {
                    Haptics.tap(haptics)
                    soundFor = v
                }
            )
            if (audioOnly) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.93f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            NIcons.Music, null,
                            Modifier.size(64.dp),
                            tint = NovaAccent
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Modo solo audio", color = Color.White,
                            fontWeight = FontWeight.ExtraBold, fontSize = 17.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            v.music, color = NovaTextDim, fontSize = 12.sp
                        )
                    }
                }
            }
            }
        }

        TopBar(
            tab = tab,
            onTab = { tab = it },
            onRecord = onRecord,
            muted = muted,
            onMute = { muted = !muted },
            onSearch = onOpenSearch,
            onActivity = onOpenActivity,
            audioOnly = audioOnly,
            onAudioOnly = { audioOnly = !audioOnly },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )
    }

    shareFor?.let { v ->
        ModalBottomSheet(
            onDismissRequest = { shareFor = null },
            containerColor = NovaSurface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ShareSheet(
                video = v,
                onSend = { friend ->
                    Repo.scope.launch {
                        Repo.addComment(
                            ctx, v.id, "Te comparto este reel 👀", com.novareels.app.data.Comment.TEXTO,
                            null, null, null
                        )
                    }
                    Repo.toastFlow.value = "Enviado a $friend"
                    shareFor = null
                },
                onExternal = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Mira este reel de ${v.author} en Nova Reels: ${Repo.shareLink(v)}"
                        )
                    }
                    ctx.startActivity(Intent.createChooser(intent, "Compartir reel"))
                    shareFor = null
                }
            )
        }
    }

    soundFor?.let { v ->
        ModalBottomSheet(
            onDismissRequest = { soundFor = null },
            containerColor = NovaSurface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
                Text(
                    "Sonido", color = Color.White,
                    fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    v.music + " · " + v.author,
                    color = NovaTextDim, fontSize = 12.5.sp, maxLines = 2
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(SolidColor(NovaAccent))
                        .clickable {
                            Repo.pendingMusic = v.music
                            Repo.toastFlow.value = "Sonido cargado, elige un vídeo"
                            soundFor = null
                            onGoCreate()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Usar este sonido", color = Color.White,
                        fontWeight = FontWeight.ExtraBold, fontSize = 14.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    moreFor?.let { v ->
        ModalBottomSheet(
            onDismissRequest = { moreFor = null },
            containerColor = Color.Transparent,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = null
        ) {
            Box(Modifier.fillMaxWidth().height(470.dp)) {
                BlurBackdrop(model = v.url, scrim = 0.82f, radius = 40.dp)
                Column(Modifier.fillMaxSize().padding(20.dp)) {
                    Text(
                        "Opciones del reel", color = Color.White,
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, fontFamily = Montserrat
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${v.author} · ${v.music}", color = NovaTextDim, fontSize = 12.sp)
                    Spacer(Modifier.height(14.dp))
                    SheetAction(NIcons.Close, "No me interesa") {
                        Repo.scope.launch { Repo.hideVideo(v) }
                        moreFor = null
                    }
                    SheetAction(NIcons.Share, "Copiar enlace") {
                        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Nova Reels", Repo.shareLink(v)))
                        Repo.toastFlow.value = "Enlace copiado"
                        moreFor = null
                    }
                    SheetAction(NIcons.Repost, "Repostear en mi perfil") {
                        Repo.scope.launch { Repo.repost(v) }
                        moreFor = null
                    }
                    SheetAction(NIcons.Download, "Descargar con marca de agua") {
                        Repo.scope.launch { Repo.downloadVideo(ctx, v) }
                        moreFor = null
                    }
                    SheetAction(NIcons.Music, "Guardar sonido") {
                        Repo.saveSound(v.music)
                        moreFor = null
                    }
                    SheetAction(NIcons.At, "Mencionar a alguien") {
                        Repo.toastFlow.value = "Mención añadida: @${v.author.removePrefix("@")}"
                        moreFor = null
                    }
                    SheetAction(NIcons.Person, "Ver perfil de ${v.author}") {
                        moreFor = null
                        onOpenUser(v.author)
                    }
                    SheetAction(NIcons.Star, "Reportar contenido") {
                        Repo.toastFlow.value = "Reporte enviado. Gracias por cuidar la comunidad"
                        moreFor = null
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = Color.White)
        Spacer(Modifier.width(14.dp))
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EmptyFeed() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(NIcons.Grid, null, Modifier.size(44.dp), tint = NovaTextDim)
        Spacer(Modifier.height(12.dp))
        Text("No hay reels aquí todavía", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Sigue a alguien o publica el tuyo", color = NovaTextDim, fontSize = 12.sp)
    }
}

@Composable
private fun TopBar(
    tab: Int,
    onTab: (Int) -> Unit,
    onRecord: () -> Unit,
    muted: Boolean,
    onMute: () -> Unit,
    onSearch: () -> Unit,
    onActivity: () -> Unit,
    audioOnly: Boolean,
    onAudioOnly: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cloudOk by Repo.cloudOk.collectAsStateWithLifecycle()
    Column(
        modifier
            .statusBarsPadding()
            .padding(top = 8.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                NIcons.Camera, "Grabar",
                Modifier
                    .size(25.dp)
                    .clickable(onClick = onRecord),
                tint = Color.White
            )
            Spacer(Modifier.width(10.dp))
            Dot(if (cloudOk) NovaLime else NovaTextDim, 7.dp)
            Spacer(Modifier.weight(1f))
            Row {
                TabItem("Para ti", tab == 0) { onTab(0) }
                Spacer(Modifier.width(18.dp))
                TabItem("Siguiendo", tab == 1) { onTab(1) }
            }
            Spacer(Modifier.weight(1f))
            Icon(
                NIcons.Search, "Buscar",
                Modifier
                    .size(23.dp)
                    .clickable(onClick = onSearch),
                tint = Color.White
            )
            Spacer(Modifier.width(16.dp))
            Icon(
                NIcons.Bell, "Actividad",
                Modifier
                    .size(23.dp)
                    .clickable(onClick = onActivity),
                tint = Color.White
            )
            Spacer(Modifier.width(16.dp))
            Icon(
                if (muted) NIcons.VolumeOff else NIcons.VolumeOn, "Sonido",
                Modifier
                    .size(23.dp)
                    .clickable(onClick = onMute),
                tint = Color.White
            )
            Spacer(Modifier.width(16.dp))
            Icon(
                NIcons.Music, "Solo audio",
                Modifier
                    .size(23.dp)
                    .clickable(onClick = onAudioOnly),
                tint = if (audioOnly) NovaAccent else Color.White
            )
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun TabItem(text: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            fontSize = 16.sp,
            fontFamily = Montserrat,
            modifier = Modifier.clickable(onClick = onClick)
        )
        Spacer(Modifier.height(3.dp))
        val w by animateDpAsState(
            if (selected) 20.dp else 0.dp,
            spring(dampingRatio = 0.7f), label = "u"
        )
        Box(
            Modifier
                .width(w)
                .height(2.5.dp)
                .clip(CircleShape)
                .background(NovaAccent)
        )
    }
}
