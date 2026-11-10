package com.novareels.app.ui.messages

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.novareels.app.ui.theme.NovaSurfaceHigh
import com.novareels.app.ui.theme.NovaTextDim
import com.novareels.app.ui.theme.avatarBrush
import com.novareels.app.util.Haptics

@Composable
fun MessagesScreen(onBack: () -> Unit, onOpenVideo: (String) -> Unit) {
    val all by Repo.db.messageDao().all().collectAsStateWithLifecycle(initialValue = emptyList())
    var openThread by remember { mutableStateOf<String?>(Repo.pendingThread) }
    LaunchedEffect(Unit) { Repo.pendingThread = null }

    val threads = remember(all) {
        all.groupBy { it.threadId }.map { (k, v) ->
            k to v.maxByOrNull { it.createdAt }
        }.sortedByDescending { it.second?.createdAt ?: 0L }
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
            Spacer(Modifier.width(14.dp))
            Text(
                "Mensajes", color = Color.White, fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold, fontFamily = Montserrat
            )
        }

        if (threads.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(NIcons.Chat, null, Modifier.size(46.dp), tint = NovaTextDim)
                    Spacer(Modifier.height(10.dp))
                    Text("Sin conversaciones", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "Escribe a alguien desde su perfil",
                        color = NovaTextDim, fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(threads) { (handle, last) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { openThread = handle }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(avatarBrush(handle.hashCode().toFloat() % 360f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                handle.removePrefix("@").take(1).uppercase(),
                                color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                handle, color = Color.White, fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                last?.text ?: "", color = NovaTextDim, fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        Text(
                            com.novareels.app.util.Fmt.ago(last?.createdAt ?: 0L),
                            color = NovaTextDim, fontSize = 10.5.sp
                        )
                    }
                }
            }
        }
    }

    openThread?.let { thread ->
        ChatScreen(thread = thread, onBack = { openThread = null })
    }
}

@Composable
private fun ChatScreen(thread: String, onBack: () -> Unit) {
    val msgs by Repo.db.messageDao().byThread(thread)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(msgs.size) {
        if (msgs.isNotEmpty()) listState.animateScrollToItem(msgs.lastIndex)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .imePadding()
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
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(avatarBrush(thread.hashCode().toFloat() % 360f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    thread.removePrefix("@").take(1).uppercase(),
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(thread, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(msgs) { m ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (m.mine) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp, topEnd = 16.dp,
                                    bottomStart = if (m.mine) 16.dp else 4.dp,
                                    bottomEnd = if (m.mine) 4.dp else 16.dp
                                )
                            )
                            .background(if (m.mine) NovaAccent else NovaSurfaceHigh)
                            .padding(horizontal = 13.dp, vertical = 9.dp)
                    ) {
                        Text(m.text, color = Color.White, fontSize = 13.5.sp)
                    }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(NovaSurface)
                    .border(1.dp, NovaGlassBorder, RoundedCornerShape(22.dp))
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                if (text.isEmpty()) {
                    Text("Mensaje...", color = NovaTextDim, fontSize = 13.sp)
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    cursorBrush = SolidColor(NovaAccent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (text.isBlank()) NovaSurfaceHigh else NovaAccent)
                    .clickable(enabled = text.isNotBlank()) {
                        Haptics.tap(haptics)
                        Repo.sendMessage(thread, text)
                        text = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(NIcons.Send, "Enviar", Modifier.size(20.dp), tint = Color.White)
            }
        }
    }
}
