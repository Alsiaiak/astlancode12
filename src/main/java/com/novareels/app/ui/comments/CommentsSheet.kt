package com.novareels.app.ui.comments

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.novareels.app.data.Comment
import com.novareels.app.data.Repo
import com.novareels.app.player.PlayerHost
import com.novareels.app.ui.icons.NIcons
import com.novareels.app.ui.theme.NovaCyan
import com.novareels.app.ui.theme.NovaGlass
import com.novareels.app.ui.theme.NovaGlassBorder
import com.novareels.app.ui.theme.NovaPink
import com.novareels.app.ui.theme.NovaRed
import com.novareels.app.ui.theme.Montserrat
import com.novareels.app.ui.theme.NovaAccent
import com.novareels.app.ui.theme.NovaTextDim
import com.novareels.app.ui.theme.avatarBrush
import com.novareels.app.ui.components.BlurBackdrop
import com.novareels.app.ui.theme.novaGradient
import com.novareels.app.util.AudioBox
import com.novareels.app.util.Fmt
import com.novareels.app.util.Files
import com.novareels.app.util.Haptics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CommentsSheet(videoId: String, onDismiss: () -> Unit, backdrop: String? = null) {
    val ctx = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val comments by remember(videoId) { Repo.db.commentDao().byVideo(videoId) }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var text by remember { mutableStateOf("") }
    var stickerPicker by remember { mutableStateOf(false) }
    var recording by remember { mutableStateOf(false) }
    var recSeconds by remember { mutableStateOf(0) }
    var recFile by remember { mutableStateOf<java.io.File?>(null) }
    var playingId by remember { mutableStateOf<String?>(null) }
    var newestFirst by remember { mutableStateOf(true) }
    var replyTo by remember { mutableStateOf<Comment?>(null) }

    val roots = remember(comments, newestFirst) {
        val list = comments.filter { it.parentId == null }
        if (newestFirst) list.sortedByDescending { it.createdAt } else list.sortedBy { it.createdAt }
    }
    val repliesOf = remember(comments) { comments.filter { it.parentId != null }.groupBy { it.parentId } }

    DisposableEffect(Unit) {
        PlayerHost.pause()
        onDispose {
            AudioBox.stop()
            PlayerHost.resume()
            stopRecording(recFile, false)
        }
    }

    LaunchedEffect(recording) {
        var s = 0
        while (recording) {
            delay(1000); s++; recSeconds = s
        }
        recSeconds = 0
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            Files.copyFromUri(ctx, it, Files.commentsDir(ctx), "jpg")?.let { f ->
                Repo.scope.launch { Repo.addComment(ctx, videoId, text, Comment.FOTO, null, f, replyTo?.id) }
                text = ""
                replyTo = null
            }
        }
    }

    val micPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) startRecording(ctx) { f, ok -> recFile = f; recording = ok } }

    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.86f)
            .background(Color.Black)
    ) {
        BlurBackdrop(model = backdrop, scrim = 0.82f, radius = 44.dp)
        Column(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
        ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(18.dp))
            Text(
                "Comentarios", color = Color.White,
                fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, fontFamily = Montserrat
            )
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(NovaPink.copy(alpha = 0.25f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("${comments.size}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
            Icon(
                NIcons.Sort, "Ordenar",
                Modifier
                    .padding(end = 14.dp)
                    .clickable {
                        newestFirst = !newestFirst
                        Haptics.tap(haptics)
                    },
                tint = if (newestFirst) NovaAccent else Color.White
            )
            Icon(
                NIcons.Close, "Cerrar",
                Modifier
                    .padding(end = 18.dp)
                    .clickable(onClick = onDismiss),
                tint = Color.White
            )
        }
        Box(
            Modifier
                .padding(horizontal = 18.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(NovaGlassBorder)
        )

        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = false
        ) {
            items(roots, key = { it.id }) { c ->
                Column {
                    CommentRow(
                        comment = c,
                        onPlayAudio = {
                            Haptics.tap(haptics)
                            if (playingId != null && playingId != c.id) AudioBox.stop()
                            val source = c.mediaUrl ?: c.mediaPath
                            if (source != null) {
                                val ok = AudioBox.toggle(c.id, source) { playingId = null }
                                playingId = if (ok) c.id else null
                            }
                        },
                        onReply = { replyTo = c },
                        onDelete = { Repo.scope.launch { Repo.deleteComment(c) } }
                    )
                    repliesOf[c.id]?.sortedBy { it.createdAt }?.forEach { r ->
                        Row(Modifier.padding(start = 34.dp)) {
                            CommentRow(
                                comment = r,
                                compact = true,
                                onPlayAudio = {
                                    Haptics.tap(haptics)
                                    if (playingId != null && playingId != r.id) AudioBox.stop()
                                    val source = r.mediaUrl ?: r.mediaPath
                                    if (source != null) {
                                        val ok = AudioBox.toggle(r.id, source) { playingId = null }
                                        playingId = if (ok) r.id else null
                                    }
                                },
                                onReply = { replyTo = c },
                                onDelete = { Repo.scope.launch { Repo.deleteComment(r) } }
                            )
                        }
                    }
                }
            }
            if (roots.isEmpty()) {
                item {
                    Text(
                        "Sé el primero en comentar",
                        color = NovaTextDim, fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 24.dp)
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        AnimatedContent(
            targetState = stickerPicker,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "sp"
        ) { open ->
            if (open) {
                LazyRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(Sticker.entries) { s ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                Haptics.tap(haptics)
                                Repo.scope.launch {
                                    Repo.addComment(ctx, videoId, text, Comment.STICKER, s.name, null, replyTo?.id)
                                }
                                text = ""
                                replyTo = null
                                stickerPicker = false
                            }
                        ) {
                            StickerView(s, 58.dp)
                            Spacer(Modifier.height(2.dp))
                            Text(s.label, fontSize = 10.sp, color = NovaTextDim)
                        }
                    }
                }
            }
        }

        AnimatedContent(
            targetState = recording,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "rec"
        ) { rec ->
            if (rec) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PulsingDot()
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Grabando… ${recSeconds}s",
                        color = NovaRed, fontWeight = FontWeight.Bold, fontSize = 14.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        NIcons.Trash, "Cancelar",
                        Modifier
                            .clickable {
                                stopRecording(recFile, false)
                                recording = false
                            },
                        tint = NovaTextDim
                    )
                    Spacer(Modifier.width(18.dp))
                    SendButton {
                        val f = stopRecording(recFile, true)
                        recording = false
                        if (f != null) {
                            Repo.scope.launch {
                                Repo.addComment(ctx, videoId, "", Comment.AUDIO, null, f, replyTo?.id)
                            }
                            replyTo = null
                        }
                    }
                }
            } else {
                Column(Modifier.fillMaxWidth()) {
                replyTo?.let { r ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Respondiendo a ${r.author}",
                            color = NovaAccent, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            NIcons.Close, "Cancelar respuesta",
                            Modifier
                                .size(15.dp)
                                .clickable { replyTo = null },
                            tint = NovaTextDim
                        )
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Añade un comentario…", color = NovaTextDim, fontSize = 13.5.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NovaAccent,
                            unfocusedBorderColor = NovaGlassBorder,
                            focusedContainerColor = NovaGlass,
                            unfocusedContainerColor = NovaGlass,
                            cursorColor = NovaAccent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        NIcons.Star, "Stickers",
                        Modifier
                            .padding(8.dp)
                            .clickable { stickerPicker = !stickerPicker },
                        tint = if (stickerPicker) NovaCyan else Color.White
                    )
                    Icon(
                        NIcons.Image, "Foto",
                        Modifier
                            .padding(8.dp)
                            .clickable {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        tint = Color.White
                    )
                    Icon(
                        NIcons.Mic, "Audio",
                        Modifier
                            .padding(8.dp)
                            .clickable {
                                if (ctx.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                                    android.content.pm.PackageManager.PERMISSION_GRANTED
                                ) startRecording(ctx) { f, ok -> recFile = f; recording = ok }
                                else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                            },
                        tint = Color.White
                    )
                    SendButton(enabled = text.isNotBlank()) {
                        Haptics.tap(haptics)
                        val t = text
                        val parent = replyTo?.id
                        text = ""
                        replyTo = null
                        Repo.scope.launch {
                            Repo.addComment(ctx, videoId, t, Comment.TEXTO, null, null, parent)
                        }
                    }
                }
                }
            }
        }
    }
}
}

@Composable
private fun SendButton(enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (enabled) novaGradient else SolidColor(Color.White.copy(alpha = 0.15f)))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(NIcons.Send, "Enviar", Modifier.size(18.dp), tint = Color.White)
    }
}

@Composable
private fun PulsingDot() {
    val t = rememberInfiniteTransitionScroll()
    Canvas(Modifier.size(12.dp)) {
        drawCircle(NovaRed, size.minDimension / 2f * t, center)
    }
}

@Composable
private fun rememberInfiniteTransitionScroll(): Float {
    val t = androidx.compose.animation.core.rememberInfiniteTransition(label = "dot")
    val v by t.animateFloat(
        0.55f, 1f,
        androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(500),
            androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "v"
    )
    return v
}

@Composable
fun CommentRow(
    comment: Comment,
    onPlayAudio: () -> Unit,
    compact: Boolean = false,
    onReply: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val haptics = LocalHapticFeedback.current
    val ctx = LocalContext.current
    Row(
        Modifier
            .fillMaxWidth()
            .pointerInput(comment.id) {
                detectTapGestures(
                    onDoubleTap = {
                        Haptics.strong(haptics)
                        Repo.scope.launch { Repo.likeComment(comment, forceLike = true) }
                    }
                )
            }
            .padding(
                start = if (compact) 8.dp else 18.dp,
                end = 18.dp,
                top = 6.dp,
                bottom = 6.dp
            )
    ) {
        Box(
            Modifier
                .size(if (compact) 28.dp else 34.dp)
                .clip(CircleShape)
                .background(avatarBrush(comment.authorHue), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                comment.author.removePrefix("@").take(1).uppercase(),
                color = Color.White, fontWeight = FontWeight.ExtraBold,
                fontSize = if (compact) 11.sp else 13.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "${comment.author} · ${Fmt.ago(comment.createdAt)}",
                color = NovaTextDim, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            when (comment.kind) {
                Comment.TEXTO -> Text(
                    comment.text, color = Color.White,
                    fontSize = (13.5f * com.novareels.app.ui.theme.NovaTextScale).sp
                )
                Comment.STICKER -> Row(verticalAlignment = Alignment.CenterVertically) {
                    StickerView(Sticker.byId(comment.sticker), 54.dp)
                    if (comment.text.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(comment.text, color = Color.White, fontSize = 13.5.sp)
                    }
                }
                Comment.FOTO -> {
                    val src = comment.mediaUrl ?: comment.mediaPath
                    if (src != null) {
                        AsyncImage(
                            model = src,
                            contentDescription = "Foto",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .widthIn(max = 220.dp)
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, NovaGlassBorder, RoundedCornerShape(16.dp))
                        )
                    }
                }
                Comment.AUDIO -> AudioBubble(comment, playing = AudioBox.playingId == comment.id, onPlay = onPlayAudio)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Responder",
                    color = NovaTextDim, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            Haptics.tap(haptics)
                            onReply()
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
                if (comment.author == Repo.myName) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Eliminar",
                        color = NovaRed.copy(alpha = 0.9f), fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                Haptics.tap(haptics)
                                onDelete()
                            }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val pop = remember { Animatable(1f) }
            LaunchedEffect(comment.likedByMe) {
                if (comment.likedByMe) {
                    pop.snapTo(1.5f)
                    pop.animateTo(
                        1f,
                        androidx.compose.animation.core.spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
                        )
                    )
                }
            }
            Icon(
                if (comment.likedByMe) NIcons.HeartFill else NIcons.HeartOutline, "Me gusta",
                Modifier
                    .scale(pop.value)
                    .padding(6.dp)
                    .clickable {
                        Haptics.tap(haptics)
                        Repo.scope.launch { Repo.likeComment(comment) }
                    },
                tint = if (comment.likedByMe) NovaRed else NovaTextDim
            )
            Text(
                Fmt.count(comment.likes),
                color = NovaTextDim, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AudioBubble(comment: Comment, playing: Boolean, onPlay: () -> Unit) {
    val phase = rememberInfiniteTransitionScroll()
    Row(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(NovaGlass)
            .border(1.dp, NovaGlassBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onPlay)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (playing) NIcons.Pause else NIcons.Play, "Reproducir",
            Modifier.size(22.dp), tint = NovaCyan
        )
        Spacer(Modifier.width(10.dp))
        Canvas(Modifier.width(90.dp).height(20.dp)) {
            val n = 14
            val bw = size.width / (n * 2f)
            for (i in 0 until n) {
                val base = 0.25f + 0.6f * kotlin.math.abs(
                    kotlin.math.sin(i * 1.7f + (if (playing) phase * 6f else 0f))
                )
                val h = size.height * base.coerceIn(0.15f, 1f)
                drawRoundRect(
                    Brush.verticalGradient(listOf(NovaPink, NovaCyan)),
                    topLeft = Offset(i * bw * 2f + bw * 0.5f, (size.height - h) / 2),
                    size = androidx.compose.ui.geometry.Size(bw, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(bw)
                )
            }
        }
    }
}

private fun startRecording(ctx: Context, onState: (java.io.File?, Boolean) -> Unit) {
    val file = java.io.File(Files.commentsDir(ctx), "aud_${System.currentTimeMillis()}.m4a")
    runCatching {
        val rec: MediaRecorder = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(ctx)
        else @Suppress("DEPRECATION") MediaRecorder()
        rec.setAudioSource(MediaRecorder.AudioSource.MIC)
        rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        rec.setAudioEncodingBitRate(96000)
        rec.setAudioSamplingRate(44100)
        rec.setOutputFile(file.absolutePath)
        rec.prepare()
        rec.start()
        recorders.add(rec)
        onState(file, true)
    }.onFailure { onState(null, false) }
}

private fun stopRecording(file: java.io.File?, keep: Boolean): java.io.File? {
    runCatching {
        recorders.forEach { it.stop(); it.release() }
    }
    recorders.clear()
    if (!keep) file?.delete()
    return if (keep && file != null && file.exists()) file else null
}

private val recorders = java.util.ArrayList<MediaRecorder>()

