@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.novareels.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novareels.app.data.Repo
import com.novareels.app.player.PlayerHost
import com.novareels.app.ui.activity.ActivityScreen
import com.novareels.app.ui.comments.CommentsSheet
import com.novareels.app.ui.components.BlurBackdrop
import com.novareels.app.ui.create.CreateScreen
import com.novareels.app.ui.feed.FeedScreen
import com.novareels.app.ui.icons.NIcons
import com.novareels.app.ui.messages.MessagesScreen
import com.novareels.app.ui.profile.FollowListScreen
import com.novareels.app.ui.profile.ProfileScreen
import com.novareels.app.ui.profile.UserProfileScreen
import com.novareels.app.ui.search.SearchScreen
import com.novareels.app.ui.settings.SettingsScreen
import com.novareels.app.ui.theme.NovaAccent
import com.novareels.app.ui.theme.NovaGlassBorder
import com.novareels.app.ui.theme.NovaSurface
import com.novareels.app.ui.theme.NovaTheme
import com.novareels.app.util.Haptics

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Repo.init(this)
        setContent {
            NovaTheme { NovaApp() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayerHost.release()
    }
}

@Composable
fun NovaApp() {
    val ctx = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var tab by remember { mutableStateOf(0) }
    var settings by remember { mutableStateOf(false) }
    var commentsFor by remember { mutableStateOf<String?>(null) }
    var openVideo by remember { mutableStateOf<String?>(null) }
    var profileFor by remember { mutableStateOf<String?>(null) }
    var followList by remember { mutableStateOf<String?>(null) }
    var messagesOpen by remember { mutableStateOf(false) }
    var activityOpen by remember { mutableStateOf(false) }

    val videos by Repo.db.videoDao().all()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val counts by Repo.db.commentDao().counts()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val commentCounts = remember(counts) { counts.associate { it.videoId to it.n } }
    val backdrop = remember(videos) { videos.firstOrNull { !it.hidden }?.url }

    val view = LocalView.current
    androidx.compose.runtime.LaunchedEffect(tab == 0) { view.keepScreenOn = tab == 0 }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        Repo.toastFlow.collect { msg ->
            msg?.let {
                Toast.makeText(ctx, it, Toast.LENGTH_LONG).show()
                Repo.toastFlow.value = null
            }
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, tab) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> PlayerHost.pause()
                Lifecycle.Event.ON_RESUME -> if (tab == 0) PlayerHost.resume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        when {
            settings -> SettingsScreen(onBack = { settings = false })
            followList != null -> FollowListScreen(
                kind = followList!!,
                onBack = { followList = null },
                onOpenUser = { h ->
                    followList = null
                    profileFor = h
                }
            )
            profileFor != null -> UserProfileScreen(
                handle = profileFor!!,
                onBack = { profileFor = null },
                onOpenVideo = { id ->
                    profileFor = null
                    openVideo = id
                    tab = 0
                },
                onMessage = { h ->
                    profileFor = null
                    messagesOpen = true
                    Repo.pendingThread = h
                },
                onOpenFollowList = { kind ->
                    followList = kind
                }
            )
            messagesOpen -> MessagesScreen(
                onBack = { messagesOpen = false },
                onOpenVideo = { id ->
                    messagesOpen = false
                    openVideo = id
                    tab = 0
                }
            )
            activityOpen -> ActivityScreen(
                onOpen = { id ->
                    activityOpen = false
                    openVideo = id
                    tab = 0
                },
                onBack = { activityOpen = false }
            )
            tab == 0 -> FeedScreen(
                videos = videos,
                commentCounts = commentCounts,
                startingVideoId = openVideo,
                onOpenComments = { commentsFor = it },
                onRecord = { tab = 2 },
                onOpenSearch = { tab = 1 },
                onOpenActivity = { activityOpen = true },
                onOpenUser = { profileFor = it },
                onGoCreate = { tab = 2 }
            )
            tab == 1 -> SearchScreen(
                videos = videos,
                onOpen = { id ->
                    openVideo = id
                    tab = 0
                },
                onOpenUser = { profileFor = it }
            )
            tab == 2 -> CreateScreen(onDone = { msg ->
                tab = 0
                msg?.let {
                    Haptics.strong(haptics)
                }
            })
            tab == 3 -> MessagesScreen(
                onBack = { tab = 0 },
                onOpenVideo = { id ->
                    openVideo = id
                    tab = 0
                }
            )
            else -> ProfileScreen(
                onOpenSettings = { settings = true },
                onOpenVideo = { id ->
                    openVideo = id
                    tab = 0
                },
                onOpenFollowList = { followList = it },
                onOpenDrafts = { tab = 2 }
            )
        }

        if (tab != 2 && !settings && followList == null && profileFor == null &&
            !messagesOpen && !activityOpen
        ) {
            BottomBar(
                tab = tab,
                backdrop = backdrop,
                onSelect = { tab = it },
                onCreate = { tab = 2 },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        commentsFor?.let { vid ->
            ModalBottomSheet(
                onDismissRequest = { commentsFor = null },
                containerColor = Color.Transparent,
                sheetState = androidx.compose.material3.rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                ),
                dragHandle = null
            ) {
                CommentsSheet(
                    videoId = vid,
                    onDismiss = { commentsFor = null },
                    backdrop = videos.firstOrNull { it.id == vid }?.url
                )
            }
        }
    }
}

@Composable
private fun BottomBar(
    tab: Int,
    backdrop: String?,
    onSelect: (Int) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .background(NovaSurface)
            .border(1.dp, NovaGlassBorder)
    ) {
        Row(
            Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BarItem(NIcons.Home, "Reels", tab == 0) { onSelect(0) }
            BarItem(NIcons.Search, "Buscar", tab == 1) { onSelect(1) }
            CreateButton(onCreate)
            BarItem(NIcons.Chat, "Mensajes", tab == 3) { onSelect(3) }
            BarItem(NIcons.Person, "Perfil", tab == 4) { onSelect(4) }
        }
    }
}

@Composable
private fun BarItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        if (selected) 1.1f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "s"
    )
    val haptics = LocalHapticFeedback.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                Haptics.tap(haptics)
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            icon, label,
            Modifier
                .scale(scale)
                .size(22.dp),
            tint = if (selected) Color.White else Color.White.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.45f),
            fontSize = 9.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun CreateButton(onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(NovaAccent)
            .clickable {
                Haptics.strong(haptics)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(NIcons.Plus, "Crear", Modifier.size(22.dp), tint = Color.White)
    }
}
