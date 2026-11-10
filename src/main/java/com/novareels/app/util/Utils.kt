package com.novareels.app.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import java.io.File
import java.util.Locale

object Fmt {
    fun count(n: Long): String = when {
        n >= 1_000_000 -> String.format(Locale("es"), "%.1f M", n / 1_000_000f).replace(".0 ", " ")
        n >= 1000 -> String.format(Locale("es"), "%.1f mil", n / 1000f).replace(".0 ", " ")
        else -> n.toString()
    }

    fun ago(t: Long): String {
        val s = (System.currentTimeMillis() - t) / 1000
        return when {
            s < 60 -> "ahora"
            s < 3600 -> "${s / 60} min"
            s < 86400 -> "${s / 3600} h"
            else -> "${s / 86400} d"
        }
    }

    fun secs(ms: Long): String {
        val s = ms / 1000
        return String.format(Locale.US, "%d:%02d", s / 60, s % 60)
    }
}

object Haptics {
    fun tap(h: HapticFeedback) = h.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    fun strong(h: HapticFeedback) = h.performHapticFeedback(HapticFeedbackType.LongPress)
}

object AudioBox {
    private var player: MediaPlayer? = null
    var playingId: String? = null
        private set

    fun toggle(id: String, pathOrUrl: String, onEnd: () -> Unit): Boolean {
        stop()
        return try {
            val p = MediaPlayer()
            if (pathOrUrl.startsWith("http")) p.setDataSource(pathOrUrl)
            else p.setDataSource(pathOrUrl)
            p.setOnCompletionListener {
                playingId = null
                onEnd()
                it.release()
                player = null
            }
            p.prepare()
            p.start()
            player = p
            playingId = id
            true
        } catch (e: Exception) {
            playingId = null
            false
        }
    }

    fun stop() {
        runCatching {
            player?.stop()
            player?.release()
        }
        player = null
        playingId = null
    }
}

object Files {
    fun commentsDir(ctx: Context): File = File(ctx.filesDir, "comments").apply { mkdirs() }
    fun videosDir(ctx: Context): File = File(ctx.filesDir, "videos").apply { mkdirs() }

    fun copyFromUri(ctx: Context, uri: Uri, dir: File, ext: String): File? = runCatching {
        val f = File(dir, "nv_${System.currentTimeMillis()}.$ext")
        ctx.contentResolver.openInputStream(uri)?.use { input ->
            f.outputStream().use { input.copyTo(it) }
        }
        f
    }.getOrNull()
}
