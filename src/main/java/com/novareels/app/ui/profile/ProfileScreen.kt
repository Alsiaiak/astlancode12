@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.novareels.app.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.novareels.app.data.Repo
import com.novareels.app.data.Video
import com.novareels.app.ui.components.BlurBackdrop
import com.novareels.app.ui.components.IconCircle
import com.novareels.app.ui.icons.NIcons
import com.novareels.app.ui.theme.Montserrat
import com.novareels.app.ui.theme.NovaAccent
import com.novareels.app.ui.theme.NovaGlassBorder
import com.novareels.app.ui.theme.NovaSurface
import com.novareels.app.ui.theme.NovaSurfaceHigh
import com.novareels.app.ui.theme.NovaTextDim
import com.novareels.app.ui.theme.avatarBrush
import com.novareels.app.util.Fmt
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onOpenVideo: (String) -> Unit,
    onOpenFollowList: (String) -> Unit = {},
    onOpenDrafts: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val myVideos by Repo.db.videoDao().mine().collectAsStateWithLifecycle(initialValue = emptyList())
    val myLikes by Repo.db.videoDao().myLikes().collectAsStateWithLifecycle(initialValue = 0L)
    var editProfile by remember { mutableStateOf(false) }
    var playVideo by remember { mutableStateOf<String?>(null) }
    var savedOnly by remember { mutableStateOf(false) }
    var watchedOnly by remember { mutableStateOf(false) }
    var likedOnly by remember { mutableStateOf(false) }
    var repostOnly by remember { mutableStateOf(false) }
    var draftsOnly by remember { mutableStateOf(false) }
    val drafts by Repo.db.draftDao().all().collectAsStateWithLifecycle(initialValue = emptyList())
    val watched by Repo.db.videoDao().watched().collectAsStateWithLifecycle(initialValue = emptyList())
    val liked by Repo.db.videoDao().liked().collectAsStateWithLifecycle(initialValue = emptyList())
    var banner by remember { mutableStateOf(Repo.bannerUri) }
    var avatar by remember { mutableStateOf(Repo.avatarUri) }
    var confirmDelete by remember { mutableStateOf<Video?>(null) }

    val bannerPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val f = java.io.File(ctx.filesDir, "banner.jpg")
            ctx.contentResolver.openInputStream(it)?.use { input ->
                f.outputStream().use { input.copyTo(it) }
            }
            Repo.bannerUri = f.absolutePath
            banner = f.absolutePath
        }
    }
    val avatarPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val f = java.io.File(ctx.filesDir, "avatar.jpg")
            ctx.contentResolver.openInputStream(it)?.use { input ->
                f.outputStream().use { input.copyTo(it) }
            }
            Repo.avatarUri = f.absolutePath
            avatar = f.absolutePath
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        Box(Modifier.fillMaxWidth().height(150.dp)) {
            if (banner.isNotBlank()) {
                BlurBackdrop(model = banner, scrim = 0.18f, radius = 22.dp)
                AsyncImage(
                    model = banner,
                    contentDescription = "Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize().background(NovaSurfaceHigh))
                Icon(
                    NIcons.Image, null,
                    Modifier.align(Alignment.Center).size(30.dp),
                    tint = Color.White.copy(alpha = 0.18f)
                )
            }
            Row(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconCircle(
                    NIcons.Image, "Banner",
                    onClick = {
                        bannerPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    size = 34.dp, iconSize = 16.dp
                )
                IconCircle(
                    NIcons.Settings, "Ajustes", onClick = onOpenSettings,
                    size = 34.dp, iconSize = 17.dp
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 18.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                    .clickable {
                        avatarPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (avatar.isNotBlank()) {
                    AsyncImage(
                        model = avatar,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(avatarBrush(Repo.myHue())),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            Repo.myName.removePrefix("@").take(1).uppercase(),
                            color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 25.sp
                        )
                    }
                }
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(NovaAccent)
                        .border(2.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(NIcons.Plus, null, Modifier.size(12.dp), tint = Color.White)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    Repo.myName, color = Color.White,
                    fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, fontFamily = Montserrat
                )
                Text(
                    Repo.myBio, color = NovaTextDim, fontSize = 12.sp, maxLines = 2
                )
            }
            IconCircle(
                NIcons.More, "Editar perfil", onClick = { editProfile = true },
                size = 38.dp, iconSize = 18.dp
            )
        }

        Spacer(Modifier.height(14.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Stat("Reels", myVideos.size.toLong(), Modifier.weight(1f))
            Stat(
                "Seguidores", 1240L, Modifier.weight(1f).clickable {
                    onOpenFollowList("followers")
                }
            )
            Stat(
                "Siguiendo", 318L, Modifier.weight(1f).clickable {
                    onOpenFollowList("following")
                }
            )
            Stat("Me gusta", myLikes, Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GridChip("Mis reels", !savedOnly && !watchedOnly && !likedOnly && !repostOnly && !draftsOnly) {
                savedOnly = false; watchedOnly = false; likedOnly = false
                repostOnly = false; draftsOnly = false
            }
            GridChip("Borradores", draftsOnly) {
                draftsOnly = true; savedOnly = false; watchedOnly = false
                likedOnly = false; repostOnly = false
            }
            GridChip("Reposts", repostOnly) {
                repostOnly = true; savedOnly = false; watchedOnly = false
                likedOnly = false; draftsOnly = false
            }
            GridChip("Guardados", savedOnly) {
                savedOnly = true; watchedOnly = false; likedOnly = false
                repostOnly = false; draftsOnly = false
            }
            GridChip("Vistos", watchedOnly) {
                watchedOnly = true; savedOnly = false; likedOnly = false
                repostOnly = false; draftsOnly = false
            }
            GridChip("Me gusta", likedOnly) {
                likedOnly = true; savedOnly = false; watchedOnly = false
                repostOnly = false; draftsOnly = false
            }
            Spacer(Modifier.weight(1f))
            IconCircle(
                NIcons.Share, "Compartir perfil",
                onClick = {
                    Repo.toastFlow.value = "Perfil de ${Repo.myName} · ${myVideos.size} reels"
                },
                size = 34.dp, iconSize = 16.dp
            )
        }

        Spacer(Modifier.height(10.dp))
        val shown = when {
            savedOnly -> myVideos.filter { it.savedByMe }
            watchedOnly -> watched
            likedOnly -> liked
            repostOnly -> myVideos.filter { it.repostedFrom.isNotEmpty() }
            else -> myVideos
        }
        if (draftsOnly) {
            DraftsGrid(drafts = drafts, onOpen = onOpenDrafts)
        } else if (shown.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(NIcons.Grid, null, Modifier.size(34.dp), tint = NovaTextDim)
                Spacer(Modifier.height(8.dp))
                Text(
                    when {
                        likedOnly -> "Aún no te gusta ningún reel"
                        watchedOnly -> "Aún no has visto reels"
                        savedOnly -> "Aún no guardas reels"
                        repostOnly -> "Aún no has reposteado nada"
                        else -> "Publica tu primer reel"
                    },
                    color = NovaTextDim, fontSize = 12.sp
                )
            }
        }
        if (!draftsOnly) LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 110.dp)
        ) {
            items(shown, key = { it.id }) { v ->
                Box(
                    Modifier
                        .padding(2.dp)
                        .aspectRatio(0.62f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NovaSurface)
                        .combinedClickable(
                            onClick = { playVideo = v.url },
                            onLongClick = { confirmDelete = v }
                        )
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(ctx)
                            .data(v.url)
                            .decoderFactory(VideoFrameDecoder.Factory())
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        error = androidx.compose.ui.graphics.painter.ColorPainter(NovaSurfaceHigh),
                        modifier = Modifier.fillMaxSize()
                    )
                    Row(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(NIcons.Play, null, Modifier.size(12.dp), tint = Color.White)
                        Spacer(Modifier.width(3.dp))
                        Text(
                            Fmt.count(v.views), color = Color.White,
                            fontSize = 10.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (!v.uploaded) {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(NIcons.Waveform, "Subiendo", Modifier.size(12.dp), tint = NovaAccent)
                        }
                    }
                    if (v.repostedFrom.isNotEmpty()) {
                        Icon(
                            NIcons.Repost, "Repost",
                            Modifier
                                .align(Alignment.TopStart)
                                .padding(6.dp)
                                .size(15.dp),
                            tint = NovaAccent
                        )
                    }
                }
            }
        }
    }

    if (editProfile) {
        var name by remember { mutableStateOf(Repo.myName) }
        var bio by remember { mutableStateOf(Repo.myBio) }
        NovaDialog(title = "Editar perfil") {
            Text("Nombre de usuario", color = NovaTextDim, fontSize = 11.sp)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                colors = fieldColors()
            )
            Spacer(Modifier.height(10.dp))
            Text("Biografía", color = NovaTextDim, fontSize = 11.sp)
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it.take(90) },
                colors = fieldColors()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { editProfile = false }) {
                    Text("Cancelar", color = NovaTextDim)
                }
                TextButton(onClick = {
                    Repo.setMyName(name)
                    Repo.myBio = bio
                    editProfile = false
                    Repo.toastFlow.value = "Perfil actualizado"
                }) { Text("Guardar", color = NovaAccent, fontWeight = FontWeight.Bold) }
            }
        }
    }

    confirmDelete?.let { v ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            containerColor = NovaSurfaceHigh,
            title = { Text("Eliminar reel", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Se quitará de tu perfil y del feed. No se puede deshacer.",
                    color = NovaTextDim, fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    Repo.scope.launch { Repo.deleteVideo(v) }
                    confirmDelete = null
                }) { Text("Eliminar", color = Color(0xFFFF5C7A), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) {
                    Text("Cancelar", color = NovaTextDim)
                }
            }
        )
    }

    playVideo?.let { url ->
        PlayerDialog(url) { playVideo = null }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NovaAccent,
    unfocusedBorderColor = NovaGlassBorder,
    cursorColor = NovaAccent,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)

@Composable
private fun NovaDialog(title: String, content: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = NovaSurfaceHigh,
        title = {
            Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        },
        text = { Column { content() } },
        confirmButton = {}
    )
}

@Composable
private fun GridChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) NovaAccent else NovaSurface)
            .border(1.dp, NovaGlassBorder, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Stat(label: String, value: Long, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NovaSurface)
            .border(1.dp, NovaGlassBorder, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            Fmt.count(value), color = Color.White,
            fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, fontFamily = Montserrat
        )
        Text(label, color = NovaTextDim, fontSize = 10.sp)
    }
}

@Composable
private fun PlayerDialog(url: String, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val player = remember(url) {
        ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(androidx.media3.common.MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }
    androidx.compose.ui.window.Dialog(
        onDismissRequest = {
            player.release()
            onDismiss()
        }
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.62f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black)
                .border(1.dp, NovaGlassBorder, RoundedCornerShape(20.dp))
        ) {
            AndroidView(
                factory = { c ->
                    PlayerView(c).apply {
                        useController = true
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        setPlayer(player)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.DraftsGrid(
    drafts: List<com.novareels.app.data.Draft>,
    onOpen: () -> Unit
) {
    if (drafts.isEmpty()) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(NIcons.Edit, null, Modifier.size(34.dp), tint = NovaTextDim)
            Spacer(Modifier.height(8.dp))
            Text("Sin borradores", color = NovaTextDim, fontSize = 12.sp)
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth().weight(1f),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 110.dp)
    ) {
        items(drafts, key = { it.id }) { d ->
            Box(
                Modifier
                    .padding(2.dp)
                    .aspectRatio(0.62f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NovaSurface)
                    .clickable(onClick = onOpen)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(NIcons.Edit, null, Modifier.size(18.dp), tint = NovaAccent)
                    Column {
                        Text(
                            d.caption.ifBlank { "Sin descripción" },
                            color = Color.White, fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold, maxLines = 2
                        )
                        Text(
                            "Borrador", color = NovaTextDim, fontSize = 9.sp
                        )
                    }
                }
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { Repo.deleteDraft(d.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(NIcons.Close, "Borrar", Modifier.size(12.dp), tint = Color.White)
                }
            }
        }
    }
}
