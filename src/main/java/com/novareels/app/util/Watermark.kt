package com.novareels.app.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.view.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

object Watermark {

    private const val EGL_RECORDABLE = 0x3142

    suspend fun burn(
        src: File,
        dest: File,
        text: String,
        filter: Int = 0,
        startMs: Int = 0,
        endMs: Int = 0
    ): Boolean =
        withContext(Dispatchers.Default) {
            runCatching {
                transcode(src, dest, text, filter, startMs, endMs)
            }.onFailure { android.util.Log.d("NovaWm", "fallo: ${it.message}") }
                .getOrDefault(false)
        }

    private fun transcode(
        src: File,
        dest: File,
        text: String,
        filter: Int,
        startMs: Int,
        endMs: Int
    ): Boolean {
        val ex = MediaExtractor()
        ex.setDataSource(src.absolutePath)
        var vTrack = -1
        var vFmt: MediaFormat? = null
        var aTrack = -1
        var aFmt: MediaFormat? = null
        for (i in 0 until ex.trackCount) {
            val f = ex.getTrackFormat(i)
            val mime = f.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("video/") && vTrack < 0) { vTrack = i; vFmt = f }
            else if (mime.startsWith("audio/") && aTrack < 0) { aTrack = i; aFmt = f }
        }
        if (vFmt == null) return false
        val rotation = if (vFmt!!.containsKey("rotation-degrees")) vFmt.getInteger("rotation-degrees") else 0
        val swap = rotation == 90 || rotation == 270
        val inW = vFmt.getInteger(MediaFormat.KEY_WIDTH)
        val inH = vFmt.getInteger(MediaFormat.KEY_HEIGHT)
        val outW = if (swap) inH else inW
        val outH = if (swap) inW else inH
        val mime = vFmt.getString(MediaFormat.KEY_MIME)!!

        val out = OutputSurface(inW, inH, filter)
        val encoder = MediaCodec.createEncoderByType("video/avc")
        val ef = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, outW, outH).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo_ColorSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, (outW * outH * 4).coerceIn(2_000_000, 8_000_000))
            setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3)
            if (swap) setInteger("rotation-degrees", rotation)
        }
        encoder.configure(ef, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val encSurface = encoder.createInputSurface()
        val inp = InputSurface(encSurface, out.core.context)
        inp.makeCurrent()
        val wmTex = makeWatermarkTexture(text, outW, outH)
        encoder.start()

        ex.selectTrack(vTrack)
        if (startMs > 0) ex.seekTo(startMs * 1000L, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        val decoder = MediaCodec.createDecoderByType(mime)
        decoder.configure(vFmt, out.surface, null, 0)
        decoder.start()

        val muxer = MediaMuxer(dest.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var audioIdx = -1
        var aEx: MediaExtractor? = null
        if (aTrack >= 0 && aFmt != null) {
            aEx = MediaExtractor()
            aEx.setDataSource(src.absolutePath)
            aEx.selectTrack(aTrack)
            if (startMs > 0) aEx.seekTo(startMs * 1000L, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            audioIdx = muxer.addTrack(aFmt)
        }
        var videoIdx = -1
        var muxStarted = false
        var aEos = aEx == null
        val aBuf = ByteBuffer.allocateDirect(1 shl 16)

        val info = MediaCodec.BufferInfo()
        var eosIn = false
        var eosOut = false
        var loops = 0L
        var frames = 0L
        try {
            while (!eosOut) {
                loops++
                if (loops % 400 == 0L) android.util.Log.d("NovaWm", "loop=$loops eosIn=$eosIn frames=$frames")
                if (!eosIn) {
                    val inIdx = decoder.dequeueInputBuffer(0)
                    if (inIdx >= 0) {
                        val ib = decoder.getInputBuffer(inIdx)!!
                        val size = ex.readSampleData(ib, 0)
                        if (size < 0 || (endMs > 0 && ex.sampleTime > endMs * 1000L)) {
                            decoder.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            eosIn = true
                        } else {
                            decoder.queueInputBuffer(inIdx, 0, size, ex.sampleTime, 0)
                            ex.advance()
                        }
                    }
                }
                val decIdx = decoder.dequeueOutputBuffer(info, 0)
                if (decIdx >= 0) {
                    val render = info.size > 0
                    if (render) frames++
                    decoder.releaseOutputBuffer(decIdx, render)
                    if (render) {
                        out.awaitNewImage()
                        out.bindMatrix()
                        inp.makeCurrent()
                        out.draw()
                        drawWatermark(wmTex, outW, outH)
                        inp.setTime(info.presentationTimeUs * 1000L)
                        inp.swap()
                    }
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        encoder.signalEndOfInputStream()
                    }
                }
                val encIdx = encoder.dequeueOutputBuffer(info, 0)
                if (encIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    videoIdx = muxer.addTrack(encoder.outputFormat)
                    if (!muxStarted && videoIdx >= 0 && (audioIdx >= 0 || aEx == null)) {
                        muxer.start(); muxStarted = true
                    }
                } else if (encIdx >= 0) {
                    val ob = encoder.getOutputBuffer(encIdx)!!
                    if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) info.size = 0
                    if (info.size > 0 && muxStarted) {
                        ob.position(info.offset); ob.limit(info.offset + info.size)
                        muxer.writeSampleData(videoIdx, ob, info)
                    }
                    encoder.releaseOutputBuffer(encIdx, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) eosOut = true
                }
                if (muxStarted && !aEos && aEx != null) {
                    var n = 0
                    while (n < 4 && !aEos) {
                        val size = aEx.readSampleData(aBuf, 0)
                        if (size < 0 || (endMs > 0 && aEx.sampleTime > endMs * 1000L)) { aEos = true; break }
                        info.set(0, size, aEx.sampleTime, 0)
                        muxer.writeSampleData(audioIdx, aBuf, info)
                        aEos = !aEx.advance()
                        n++
                    }
                } else if (aEx != null && muxStarted && aEos) {
                    // audio terminado, nada que hacer
                }
            }
            muxer.stop()
        } finally {
            runCatching { muxer.release() }
            runCatching { decoder.stop(); decoder.release() }
            runCatching { encoder.stop(); encoder.release() }
            runCatching { out.release() }
            runCatching { inp.release() }
            runCatching { ex.release() }
            runCatching { aEx?.release() }
        }
        return dest.length() > 4096
    }

    private val MediaCodecInfo_ColorSurface: Int get() = android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface

    private class OutputSurface(width: Int, height: Int, filter: Int = 0) {
        val core = EglCore(null)
        private var texId = 0
        private lateinit var texture: SurfaceTexture
        private lateinit var _surface: Surface
        val surface: Surface get() = _surface
        private val texMatrix = FloatArray(16)
        private val oesProg: Int
        private val lock = Any()
        private var frameReady = false
        private var filterMode = 0

        init {
            core.makeCurrentPbuffer()
            val ids = IntArray(1)
            GLES20.glGenTextures(1, ids, 0)
            texId = ids[0]
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        texture = SurfaceTexture(texId)
        texture.setDefaultBufferSize(width, height)
        texture.setOnFrameAvailableListener { synchronized(lock) { frameReady = true; (lock as java.lang.Object).notifyAll() } }
            _surface = Surface(texture)
            oesProg = buildProgram(OES_VS, OES_FS)
            filterMode = filter
        }

        fun bindMatrix(): FloatArray = texMatrix

        fun awaitNewImage() {
            core.makeCurrentPbuffer()
            synchronized(lock) {
                if (!frameReady) {
                    (lock as java.lang.Object).wait(2500)
                }
                frameReady = false
            }
            texture.updateTexImage()
            texture.getTransformMatrix(texMatrix)
        }

        fun draw() {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId)
            GLES20.glUseProgram(oesProg)
            GLES20.glUniform1i(GLES20.glGetUniformLocation(oesProg, "uFilter"), filterMode)
            drawWithProgram(oesProg, texMatrix)
        }

        fun release() {
            runCatching { texture.release() }
            runCatching { _surface.release() }
            runCatching { core.release() }
        }
    }

    private class InputSurface(surface: Surface, shared: EGLContext) {
        private val core = EglCore(shared)
        private val eglSurface: EGLSurface = core.createWindow(surface)

        fun makeCurrent() = core.makeCurrentWindow(eglSurface)
        fun setTime(ns: Long) { android.opengl.EGLExt.eglPresentationTimeANDROID(core.display, eglSurface, ns) }
        fun swap() { EGL14.eglSwapBuffers(core.display, eglSurface) }
        fun release() { core.release() }
    }

    private class EglCore(shared: EGLContext?) {
        val display: EGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY).also { d ->
            val v = IntArray(2)
            EGL14.eglInitialize(d, v, 0, v, 1)
        }
        private val config: EGLConfig
        val context: EGLContext

        init {
            val attrs = intArrayOf(
                EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8, EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT or EGL14.EGL_WINDOW_BIT,
                EGL_RECORDABLE, 1, EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val num = IntArray(1)
            EGL14.eglChooseConfig(display, attrs, 0, configs, 0, 1, num, 0)
            config = configs[0]!!
            context = EGL14.eglCreateContext(
                display, config, shared ?: EGL14.EGL_NO_CONTEXT,
                intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE), 0
            )
        }

        fun makeCurrentPbuffer() {
            val surf = EGL14.eglCreatePbufferSurface(
                display, config,
                intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE), 0
            )
            EGL14.eglMakeCurrent(display, surf, surf, context)
        }

        fun createWindow(s: Surface): EGLSurface =
            EGL14.eglCreateWindowSurface(display, config, s, intArrayOf(EGL14.EGL_NONE), 0)

        fun makeCurrentWindow(surf: EGLSurface) {
            EGL14.eglMakeCurrent(display, surf, surf, context)
        }

        fun release() {
            EGL14.eglMakeCurrent(
                display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT
            )
            runCatching { EGL14.eglDestroyContext(display, context) }
            runCatching { EGL14.eglTerminate(display) }
        }
    }

    private const val OES_VS = """
        attribute vec2 aPos;
        attribute vec2 aTex;
        uniform mat4 uM;
        varying vec2 vTex;
        void main() {
            gl_Position = vec4(aPos, 0.0, 1.0);
            vTex = (uM * vec4(aTex, 0.0, 1.0)).xy;
        }
    """

    private const val OES_FS = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTex;
        uniform samplerExternalOES uT;
        uniform int uFilter;
        void main() {
            vec4 c = texture2D(uT, vTex);
            float l = dot(c.rgb, vec3(0.299, 0.587, 0.114));
            if (uFilter == 1) {
                c.rgb = clamp(c.rgb * vec3(1.12, 1.02, 0.86) + vec3(0.03, 0.01, 0.0), 0.0, 1.0);
            } else if (uFilter == 2) {
                c.rgb = clamp(c.rgb * vec3(0.86, 1.0, 1.16) + vec3(0.0, 0.01, 0.04), 0.0, 1.0);
            } else if (uFilter == 3) {
                c.rgb = vec3(l);
            }
            gl_FragColor = c;
        }
    """

    private const val WM_VS = """
        attribute vec2 aPos;
        uniform vec2 uScale;
        uniform vec2 uOffset;
        varying vec2 vTex;
        void main() {
            vTex = vec2(aPos.x * 0.5 + 0.5, 0.5 - aPos.y * 0.5);
            gl_Position = vec4(aPos * uScale + uOffset, 0.0, 1.0);
        }
    """

    private const val WM_FS = """
        precision mediump float;
        varying vec2 vTex;
        uniform sampler2D uT;
        uniform float uAlpha;
        void main() {
            gl_FragColor = texture2D(uT, vTex) * uAlpha;
        }
    """

    private fun buildProgram(vs: String, fs: String): Int {
        val v = compile(GLES20.GL_VERTEX_SHADER, vs)
        val f = compile(GLES20.GL_FRAGMENT_SHADER, fs)
        return GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, v)
            GLES20.glAttachShader(it, f)
            GLES20.glLinkProgram(it)
        }
    }

    private fun compile(type: Int, src: String): Int =
        GLES20.glCreateShader(type).also {
            GLES20.glShaderSource(it, src)
            GLES20.glCompileShader(it)
        }

    private val quad: FloatBuffer = ByteBuffer.allocateDirect(8 * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f))
            position(0)
        }

    private fun drawWithProgram(prog: Int, matrix: FloatArray) {
        GLES20.glUseProgram(prog)
        val posLoc = GLES20.glGetAttribLocation(prog, "aPos")
        quad.position(0)
        GLES20.glVertexAttribPointer(posLoc, 2, GLES20.GL_FLOAT, false, 0, quad)
        GLES20.glEnableVertexAttribArray(posLoc)
        val texLoc = GLES20.glGetAttribLocation(prog, "aTex")
        if (texLoc >= 0) {
            val texs = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
                .apply { put(floatArrayOf(0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f)); position(0) }
            GLES20.glVertexAttribPointer(texLoc, 2, GLES20.GL_FLOAT, false, 0, texs)
            GLES20.glEnableVertexAttribArray(texLoc)
        }
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prog, "uM"), 1, false, matrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private var wmProgram: Int = -1

    private fun makeWatermarkTexture(text: String, videoW: Int, videoH: Int): Int {
        if (wmProgram < 0) wmProgram = buildProgram(WM_VS, WM_FS)
        val bmpW = (videoW * 0.55f).toInt().coerceAtLeast(320)
        val bmpH = (bmpW * 0.14f).toInt().coerceAtMost(140)
        val bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = bmpH * 0.42f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            setShadowLayer(6f, 2f, 2f, 0xAA000000.toInt())
        }
        val y = bmpH * 0.52f
        c.drawText(text, bmpH * 0.18f, y, paint)
        val ids = IntArray(1)
        GLES20.glGenTextures(1, ids, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
        bmp.recycle()
        return ids[0]
    }

    private fun drawWatermark(texId: Int, videoW: Int, videoH: Int) {
        GLES20.glUseProgram(wmProgram)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        val w = 0.62f
        val h = w * (videoW.toFloat() / videoH.toFloat()) * 0.14f * 1.8f
        val ox = -1f + 0.03f * 2f + w
        val oy = -1f + 0.04f * 2f + h
        GLES20.glUniform2f(GLES20.glGetUniformLocation(wmProgram, "uScale"), w, h)
        GLES20.glUniform2f(GLES20.glGetUniformLocation(wmProgram, "uOffset"), ox, oy)
        GLES20.glUniform1f(GLES20.glGetUniformLocation(wmProgram, "uAlpha"), 0.88f)
        val posLoc = GLES20.glGetAttribLocation(wmProgram, "aPos")
        quad.position(0)
        GLES20.glVertexAttribPointer(posLoc, 2, GLES20.GL_FLOAT, false, 0, quad)
        GLES20.glEnableVertexAttribArray(posLoc)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(wmProgram, "uT"), 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisable(GLES20.GL_BLEND)
    }
}
