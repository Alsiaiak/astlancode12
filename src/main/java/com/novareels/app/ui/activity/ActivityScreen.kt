package com.novareels.app.ui.activity

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novareels.app.data.Comment
import com.novareels.app.data.Repo
import com.novareels.app.ui.icons.NIcons
import com.novareels.app.ui.theme.Montserrat
import com.novareels.app.ui.theme.NovaAccent
import com.novareels.app.ui.theme.NovaGlassBorder
import com.novareels.app.ui.theme.NovaSurface
import com.novareels.app.ui.theme.NovaTextDim
import com.novareels.app.ui.theme.avatarBrush
import com.novareels.app.util.Fmt

@Composable
fun ActivityScreen(onOpen: (String) -> Unit, onBack: () -> Unit = {}) {
    val items by Repo.db.commentDao().activity(Repo.myName)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(top = 10.dp)
    ) {
        Text(
            "Actividad",
            color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp,
            fontFamily = Montserrat, modifier = Modifier.padding(horizontal = 18.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Interacciones en tus reels",
            color = NovaTextDim, fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
        Spacer(Modifier.height(14.dp))
        if (items.isEmpty()) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(NIcons.Bell, null, Modifier.size(42.dp), tint = NovaTextDim)
                Spacer(Modifier.height(12.dp))
                Text("Aún no hay actividad", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Cuando comenten tus reels lo verás aquí",
                    color = NovaTextDim, fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 14.dp, end = 14.dp, bottom = 110.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { c ->
                    ActivityRow(c) { onOpen(c.videoId) }
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(c: Comment, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NovaSurface)
            .border(1.dp, NovaGlassBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(avatarBrush(c.authorHue)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                c.author.removePrefix("@").take(1).uppercase(),
                color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    c.author, color = Color.White,
                    fontWeight = FontWeight.Bold, fontSize = 13.sp
                )
                Spacer(Modifier.width(6.dp))
                Text(Fmt.ago(c.createdAt), color = NovaTextDim, fontSize = 10.5.sp)
            }
            Spacer(Modifier.height(2.dp))
            Text(
                when (c.kind) {
                    Comment.STICKER -> "Reaccionó con un sticker"
                    Comment.FOTO -> "Envió una foto en tu reel"
                    Comment.AUDIO -> "Te dejó una nota de voz"
                    else -> c.text
                },
                color = Color.White.copy(alpha = 0.85f), fontSize = 12.5.sp, maxLines = 2
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(NIcons.Comment, null, Modifier.size(18.dp), tint = NovaAccent)
    }
}
