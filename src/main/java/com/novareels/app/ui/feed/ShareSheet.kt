package com.novareels.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novareels.app.data.Video
import com.novareels.app.ui.icons.NIcons
import com.novareels.app.ui.theme.NovaAccent
import com.novareels.app.ui.theme.NovaGlassBorder
import com.novareels.app.ui.theme.NovaSurface
import com.novareels.app.ui.theme.NovaTextDim
import com.novareels.app.ui.theme.avatarBrush

private val amigos = listOf(
    "@luna.crea" to 210f,
    "@mario.edits" to 320f,
    "@sofi.films" to 30f,
    "@kike.beats" to 120f,
    "@valen.motion" to 270f,
    "@dana.frames" to 170f
)

@Composable
fun ShareSheet(video: Video, onSend: (String) -> Unit, onExternal: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
        Text(
            "Enviar a", color = Color.White,
            fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(amigos) { (name, hue) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSend(name) }
                ) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(avatarBrush(hue)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            name.removePrefix("@").take(1).uppercase(),
                            color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        name.removePrefix("@").take(9),
                        color = NovaTextDim, fontSize = 10.5.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NovaSurface)
                .border(1.dp, NovaGlassBorder, RoundedCornerShape(16.dp))
                .clickable(onClick = onExternal)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(NIcons.Link, null, Modifier.size(19.dp), tint = NovaAccent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Compartir enlace", color = Color.White,
                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp
                )
                Text(
                    "WhatsApp, Telegram, correo…", color = NovaTextDim, fontSize = 11.sp
                )
            }
            Icon(NIcons.Chevron, null, Modifier.size(18.dp), tint = NovaTextDim)
        }
        Spacer(Modifier.height(8.dp))
    }
}
