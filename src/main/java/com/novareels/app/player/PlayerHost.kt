package com.novareels.app.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

object PlayerHost {
    @Volatile private var player: ExoPlayer? = null

    fun get(ctx: Context): ExoPlayer {
        player?.let { return it }
        synchronized(this) {
            player?.let { return it }
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(15000, 30000, 800, 1500)
                .build()
            val httpFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Linux; Android 16; NovaReels/1.0) AppleWebKit/537.36")
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(12000)
                .setReadTimeoutMs(20000)
            return ExoPlayer.Builder(ctx.applicationContext)
                .setLoadControl(loadControl)
                .setMediaSourceFactory(DefaultMediaSourceFactory(httpFactory))
                .build().also {
                    it.setPlaybackParameters(PlaybackParameters(1f, 1f))
                    player = it
                }
        }
    }

    fun play(ctx: Context, url: String, pv: PlayerView? = null) {
        val p = get(ctx)
        if (p.currentMediaItem?.localConfiguration?.uri.toString() != url) {
            p.setMediaItem(MediaItem.fromUri(url))
            p.prepare()
            p.playWhenReady = true
        } else {
            p.playWhenReady = true
        }
        pv?.player = p
    }

    fun pause() {
        player?.playWhenReady = false
    }

    fun resume() {
        player?.playWhenReady = true
    }

    fun release() {
        player?.release()
        player = null
    }
}
