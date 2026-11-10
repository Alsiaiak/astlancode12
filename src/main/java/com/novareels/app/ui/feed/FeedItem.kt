package com.novareels.app.ui.feed

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.novareels.app.data.Repo
import com.novareels.app.data.Video
import com.novareels.app.player.PlayerHost
import com.novareels.app.ui.icons.NIcons
import com.novareels.app.ui.theme.NovaAccent
import com.novareels.app.ui.theme.NovaGlass
import com.novareels.app.ui.theme.NovaGlassBorder
import com.novareels.app.ui.theme.NovaRed
import com.novareels.app.ui.theme.avatarBrush
import com.novareels.app.util.Fmt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Offset2(val x: Float, val y: Float)

private val captionShadow = Shadow(Color.Black, Offset(0f, 2f), 8f)

@Composable
fun FeedItem(
    video: Video,
    isActive: Boolean,
    muted: Boolean,
    commentCount: Int,
    onLike: () -> Unit,
    onDoubleLike: () -> Unit,
    onComment: () -> Unit,
    onRepost: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onDownload: () -> Unit,
    onFollow: () -> Unit,
    onMore: () -> Unit,
    onOpenUser: (String) -> Unit = {},
    onUseSound: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val player = remember(video.id) { PlayerHost.get(ctx) }
    var buffering by remember { mutableStateOf(false) }
    var paused by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    var dragFrac by remember { mutableStateOf(0f) }
    var speed by remember { mutableStateOf(1f) }
    val bursts = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isActive, video.id) {
        if (isActive) {
            PlayerHost.play(ctx, video.url)
            player.repeatMode =
                if (Repo.loopVideos) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            while (isActive) {
                buffering = player.isLoading
                if (!dragging) {
                    progress = if (player.duration > 0)
                        player.currentPosition.toFloat() / player.duration else 0f
                }
                delay(250)
            }
        }
    }
    LaunchedEffect(muted) { player.volume = if (muted) 0f else 1f }
    LaunchedEffect(speed) { player.playbackParameters = PlaybackParameters(speed, 1f) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(video.id) {
                detectTapGestures(
                    onDoubleTap = { onDoubleLike() },
                    onTap = {
                        if (player.isPlaying) {
                            player.pause(); paused = true
                        } else {
                            player.play(); paused = false
                        }
                    }
                )
            }
    ) {
        if (isActive) {
            AndroidView(
                factory = { c ->
                    PlayerView(c).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setShutterBackgroundColor(android.graphics.Color.BLACK)
                    }
                },
                update = { pv -> pv.player = player },
                modifier = Modifier.fillMaxSize()
            )
        } else if (video.url.startsWith("http")) {
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(video.url)
                    .decoderFactory(VideoFrameDecoder.Factory())
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isActive && buffering) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp),
                color = Color.White, strokeWidth = 2.5.dp,
                trackColor = Color.White.copy(alpha = 0.15f)
            )
        }
        AnimatedVisibility(
            visible = paused && isActive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.42f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(NIcons.Play, null, Modifier.size(40.dp), tint = Color.White)
            }
        }

        Row(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .pointerInput(video.id) {
                        detectTapGestures(
                            onPress = {
                                speed = 2f
                                tryAwaitRelease()
                                speed = 1f
                            }
                        )
                    }
            )
            Spacer(Modifier.weight(1.4f))
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .pointerInput(video.id) {
                        detectTapGestures(
                            onPress = {
                                speed = 2f
                                tryAwaitRelease()
                                speed = 1f
                            }
                        )
                    }
            )
        }

        AnimatedVisibility(
            visible = speed == 2f,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.dp, NovaGlassBorder, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    "2x ▶▶", color = Color.White,
                    fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 124.dp)
                .fillMaxWidth(0.72f)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onOpenUser(video.author) }
                    ) {
                    Box(
                        Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                            .background(avatarBrush(video.authorHue), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            video.author.removePrefix("@").take(1).uppercase(),
                            color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        video.author, color = Color.White,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        style = TextStyle(shadow = captionShadow)
                    )
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (video.following) Color.White.copy(alpha = 0.16f) else Color.White)
                            .border(
                                1.dp,
                                if (video.following) Color.White.copy(alpha = 0.4f) else Color.White,
                                RoundedCornerShape(50)
                            )
                            .clickable(onClick = onFollow)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        AnimatedContent(
                            targetState = video.following,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "f"
                        ) { following ->
                            Text(
                                if (following) "Siguiendo" else "Seguir",
                                color = if (following) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold, fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                ExpandableCaption(video.caption)
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onUseSound)
                ) {
                    Icon(NIcons.Music, null, Modifier.size(14.dp), tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${video.music} · ${video.author}",
                        color = Color.White, fontSize = 12.5.sp,
                        maxLines = 1, modifier = Modifier.basicMarquee(),
                        style = TextStyle(shadow = captionShadow)
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .border(1.dp, NovaGlassBorder, RoundedCornerShape(50))
                            .clickable {
                                speed = when (speed) {
                                    1f -> 1.5f
                                    1.5f -> 2f
                                    2f -> 0.5f
                                    else -> 1f
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            if (speed == 1f) "1x" else "${speed}x".replace(".0", ""),
                            color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        ActionRail(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 128.dp),
            video = video,
            commentCount = commentCount,
            onLike = onLike,
            onComment = onComment,
            onRepost = onRepost,
            onShare = onShare,
            onSave = onSave,
            onDownload = onDownload,
            onMore = onMore
        )

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 84.dp)
                .fillMaxWidth()
                .height(22.dp)
                .pointerInput(video.id) {
                    detectHorizontalDragGestures(
                        onDragStart = { off ->
                            dragging = true
                            dragFrac = (off.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            player.seekTo((dragFrac * player.duration).toLong())
                            dragging = false
                        },
                        onDragCancel = { dragging = false }
                    ) { change, _ ->
                        dragFrac = (change.position.x / size.width).coerceIn(0f, 1f)
                        change.consume()
                    }
                }
                .pointerInput(video.id) {
                    detectTapGestures { off ->
                        val f = (off.x / size.width).coerceIn(0f, 1f)
                        player.seekTo((f * player.duration).toLong())
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(if (dragging) 4.dp else 2.dp)
                    .background(Color.White.copy(alpha = 0.22f))
            )
            Box(
                Modifier
                    .fillMaxWidth((if (dragging) dragFrac else progress).coerceIn(0f, 1f))
                    .height(if (dragging) 4.dp else 2.dp)
                    .background(Color.White)
            )
        }

        if (dragging) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 118.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.72f))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    "${Fmt.secs((dragFrac * player.duration).toLong())} / ${Fmt.secs(player.duration)}",
                    color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold
                )
            }
        }

        bursts.forEach { id ->
            HeartBurst(key = id, modifier = Modifier.align(Alignment.Center)) { bursts.remove(id) }
        }
    }
}

@Composable
fun ExpandableCaption(caption: String) {
    var expanded by remember { mutableStateOf(false) }
    var overflows by remember { mutableStateOf(false) }
    Text(
        caption,
        color = Color.White, fontSize = (13.5f * com.novareels.app.ui.theme.NovaTextScale).sp,
        lineHeight = (18f * com.novareels.app.ui.theme.NovaTextScale).sp,
        maxLines = if (expanded) 6 else 2,
        style = TextStyle(shadow = captionShadow),
        onTextLayout = { overflows = it.hasVisualOverflow },
        modifier = Modifier.clickable { if (overflows || expanded) expanded = !expanded }
    )
}

@Composable
fun ActionRail(
    modifier: Modifier,
    video: Video,
    commentCount: Int,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onRepost: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onDownload: () -> Unit,
    onMore: () -> Unit
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        RailButton(
            icon = if (video.likedByMe) NIcons.HeartFill else NIcons.HeartOutline,
            label = Fmt.count(video.likes),
            tint = if (video.likedByMe) NovaRed else Color.White,
            pop = video.likedByMe,
            onClick = onLike
        )
        RailButton(
            icon = NIcons.Comment,
            label = Fmt.count(commentCount.toLong()),
            bounceKey = commentCount,
            onClick = onComment
        )
        RailButton(
            icon = if (video.savedByMe) NIcons.BookmarkFill else NIcons.BookmarkOutline,
            label = "Guardar",
            tint = if (video.savedByMe) NovaAccent else Color.White,
            pop = video.savedByMe,
            onClick = onSave
        )
        RailButton(icon = NIcons.Share, label = "Enviar", onClick = onShare)
        RailButton(icon = NIcons.More, label = "Más", onClick = onMore)
        SpinningDisc()
    }
}

@Composable
fun RailButton(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    pop: Boolean = false,
    bounceKey: Int? = null,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pop, bounceKey) {
        scale.snapTo(0.72f)
        scale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
    ) {
        Box(
            Modifier
                .scale(scale.value)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.38f))
                .border(1.dp, NovaGlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(23.dp), tint = tint)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            label, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold,
            style = TextStyle(shadow = captionShadow)
        )
    }
}

@Composable
fun SpinningDisc() {
    val t = rememberInfiniteTransition(label = "disc")
    val ang by t.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "ang"
    )
    Box(
        Modifier
            .graphicsLayer { rotationZ = ang }
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.5.dp, Color.White.copy(alpha = 0.55f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(avatarBrush(210f)),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(4.dp).clip(CircleShape).background(Color.Black))
        }
    }
}

@Composable
fun HeartBurst(key: Long, modifier: Modifier = Modifier, onDone: () -> Unit) {
    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(1f) }
    val drift = remember { Animatable(0f) }
    LaunchedEffect(key) {
        val job1 = launch { alpha.animateTo(0f, tween(560, delayMillis = 420)) }
        val job2 = launch { drift.animateTo(-90f, tween(980, easing = LinearOutSlowInEasing)) }
        scale.animateTo(1.3f, spring(dampingRatio = 0.42f, stiffness = 420f))
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        job1.join(); job2.join()
        onDone()
    }
    Icon(
        NIcons.HeartFill, null,
        modifier
            .offset(y = drift.value.dp)
            .scale(scale.value)
            .alpha(alpha.value)
            .size(120.dp),
        tint = NovaRed
    )
}
