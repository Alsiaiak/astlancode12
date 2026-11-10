package com.novareels.app.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.novareels.app.util.Watermark
import com.novareels.app.util.Notifier
import java.io.File
import java.net.URL
import java.util.UUID

object Repo {
    lateinit var db: AppDb
    var scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private set

    private lateinit var prefs: android.content.SharedPreferences
    private var lastSince = "12h"
    private var started = false
    lateinit var app: Context

    val cloudOk = MutableStateFlow(false)
    val cloudStatusText = MutableStateFlow("Sincronizando…")
    val toastFlow = MutableStateFlow<String?>(null)
    fun takePendingMusic(): String {
        val m = pendingMusic
        pendingMusic = null
        return m ?: ""
    }

    var pendingMusic: String? = null
    var pendingThread: String? = null

    var bannerUri: String
        get() = prefs.getString("bannerUri", "") ?: ""
        set(v) = prefs.edit().putString("bannerUri", v).apply()

    var avatarUri: String
        get() = prefs.getString("avatarUri", "") ?: ""
        set(v) = prefs.edit().putString("avatarUri", v).apply()

    var myBio: String
        get() = prefs.getString("myBio", "Creador de reels en Nova") ?: "Creador de reels en Nova"
        set(v) = prefs.edit().putString("myBio", v.take(90)).apply()

    var autoplay: Boolean
        get() = prefs.getBoolean("autoplay", true)
        set(v) = prefs.edit().putBoolean("autoplay", v).apply()

    var muteByDefault: Boolean
        get() = prefs.getBoolean("muteByDefault", false)
        set(v) = prefs.edit().putBoolean("muteByDefault", v).apply()

    var dataSaver: Boolean
        get() = prefs.getBoolean("dataSaver", false)
        set(v) = prefs.edit().putBoolean("dataSaver", v).apply()

    var hapticsOn: Boolean
        get() = prefs.getBoolean("hapticsOn", true)
        set(v) = prefs.edit().putBoolean("hapticsOn", v).apply()

    var loopVideos: Boolean
        get() = prefs.getBoolean("loopVideos", true)
        set(v) = prefs.edit().putBoolean("loopVideos", v).apply()

    var accentIndex: Int
        get() = prefs.getInt("accentIndex", 0)
        set(v) = prefs.edit().putInt("accentIndex", v).apply()

    var textScale: Float
        get() = prefs.getFloat("textScale", 1f)
        set(v) = prefs.edit().putFloat("textScale", v).apply()

    var savedSounds: String
        get() = prefs.getString("savedSounds", "") ?: ""
        set(v) = prefs.edit().putString("savedSounds", v).apply()

    fun saveSound(name: String) {
        val list = savedSounds.split("|").filter { it.isNotBlank() }.toMutableList()
        if (!list.contains(name)) list.add(0, name)
        savedSounds = list.take(20).joinToString("|")
        toastFlow.value = "Sonido guardado: $name"
    }

    fun sendMessage(thread: String, text: String) {
        if (text.isBlank()) return
        val msg = Message(
            id = UUID.randomUUID().toString(),
            threadId = thread,
            author = myName,
            text = text.trim(),
            mine = true
        )
        scope.launch {
            db.messageDao().upsert(msg)
            Cloud.publish(
                JSONObject().put("t", "dm").put("id", msg.id)
                    .put("thread", thread).put("author", myName)
                    .put("text", msg.text).put("from", Cloud.deviceId)
            )
        }
    }

    suspend fun setFollowing(handle: String, hue: Float, follow: Boolean) {
        val cur = db.followDao().byHandle(handle)
        db.followDao().upsert(
            Follow(
                handle = handle,
                following = follow,
                followsMe = cur?.followsMe ?: true,
                hue = hue
            )
        )
        Cloud.publish(
            JSONObject().put("t", "f").put("h", handle)
                .put("v", follow).put("from", Cloud.deviceId)
        )
    }

    suspend fun saveDraftMulti(file: File, caption: String, music: String, filter: Int): String {
        val id = UUID.randomUUID().toString()
        db.draftDao().upsert(
            Draft(
                id = id, path = file.absolutePath, caption = caption,
                music = music, filter = filter
            )
        )
        return id
    }

    fun deleteDraft(id: String) {
        scope.launch { db.draftDao().delete(id) }
    }

    val mentionPool = listOf(
        "@luna.crea" to 210f,
        "@mario.edits" to 320f,
        "@sofi.films" to 30f,
        "@kike.beats" to 120f,
        "@valen.motion" to 270f,
        "@dana.frames" to 170f
    )

    val myName: String get() = prefs.getString("myName", "@mi.canal") ?: "@mi.canal"
    fun setMyName(n: String) = prefs.edit().putString("myName", n.take(24)).apply()
    fun deviceId(): String = prefs.getString("deviceId", null) ?: UUID.randomUUID().toString().also {
        prefs.edit().putString("deviceId", it).apply()
    }
    fun myHue(): Float = prefs.getFloat("myHue", (deviceId().hashCode().mod(360)).toFloat())
    fun topic(): String = prefs.getString("topic", Cloud.DEFAULT_TOPIC) ?: Cloud.DEFAULT_TOPIC
    fun setTopic(t: String) = prefs.edit().putString("topic", t.trim()).apply()

    fun init(ctx: Context) {
        if (started) return
        started = true
        app = ctx.applicationContext
        prefs = ctx.getSharedPreferences("nova", Context.MODE_PRIVATE)
        db = AppDb.get(ctx)
        Cloud.deviceId = deviceId()
        Cloud.topic = topic()
        com.novareels.app.ui.theme.applyAccent(
            com.novareels.app.ui.theme.accentOptions[
                accentIndex.coerceIn(0, com.novareels.app.ui.theme.accentOptions.lastIndex)
            ].second
        )
        com.novareels.app.ui.theme.applyTextScale(textScale)
        scope.launch { seedIfEmpty(ctx) }
        startListener()
        scope.launch { retryPendingUploads(ctx) }
    }

    fun restartListener() {
        Cloud.topic = topic()
        lastSince = "12h"
        startListener()
    }

    @Synchronized private fun startListener() {
        scope.launch {
            while (isActive) {
                val events = Cloud.poll(lastSince)
                if (events.isNotEmpty()) {
                    events.forEach { ev -> runCatching { applyRemote(ev) } }
                    val maxTime = events.maxOf { it.optLong("msgTime", 0L) }
                    if (maxTime > 0) lastSince = maxTime.toString()
                }
                cloudOk.value = Cloud.lastOkAt > 0
                cloudStatusText.value =
                    if (Cloud.lastOkAt > 0) "Conectado a la nube" else "Conectando a la nube…"
                delay(12000)
            }
        }
    }

    private suspend fun applyRemote(ev: JSONObject) {
        when (ev.optString("t")) {
            "v" -> {
                val id = ev.optString("id")
                if (db.videoDao().byId(id) == null) {
                    db.videoDao().upsert(
                        Video(
                            id = id, url = ev.optString("url"),
                            author = ev.optString("author"), authorHue = ev.optDouble("hue", 30.0).toFloat(),
                            caption = ev.optString("caption"), music = ev.optString("music"),
                            likes = ev.optLong("likes", 0),
                            repostedFrom = ev.optString("repost", ""),
                            createdAt = ev.optLong("createdAt", 0)
                        )
                    )
                }
            }
            "c" -> {
                val id = ev.optString("id")
                val author = ev.optString("author")
                val videoId = ev.optString("videoId")
                if (db.commentDao().byId(id) == null) {
                    db.commentDao().upsert(
                        Comment(
                            id = id, videoId = videoId,
                            author = author,
                            authorHue = ev.optDouble("hue", 30.0).toFloat(),
                            text = ev.optString("text"),
                            kind = ev.optInt("kind", Comment.TEXTO),
                            sticker = ev.optString("sticker").ifEmpty { null },
                            mediaUrl = ev.optString("mediaUrl", "").ifEmpty { null },
                            parentId = ev.optString("parent", "").ifEmpty { null },
                            likes = 0, createdAt = ev.optLong("createdAt", 0)
                        )
                    )
                    val mine = db.videoDao().byId(videoId)?.mine == true
                    if (mine && author != myName) {
                        Notifier.activity(
                            app, author,
                            ev.optString("text").ifBlank { "Nueva reacción en tu reel" }
                        )
                    }
                }
            }
            "l" -> db.videoDao().addLikes(ev.optString("id"), ev.optLong("d", 0))
            "dm" -> {
                val id = ev.optString("id")
                if (id.isNotBlank()) {
                    db.messageDao().upsert(
                        Message(
                            id = id,
                            threadId = ev.optString("thread"),
                            author = ev.optString("author"),
                            text = ev.optString("text"),
                            mine = false,
                            createdAt = ev.optLong("msgTime", System.currentTimeMillis())
                        )
                    )
                }
            }
            "f" -> {
                val h = ev.optString("h")
                if (h.isNotBlank()) {
                    val cur = db.followDao().byHandle(h)
                    db.followDao().upsert(
                        Follow(
                            handle = h,
                            following = cur?.following ?: false,
                            followsMe = ev.optBoolean("v", true),
                            hue = cur?.hue ?: 30f
                        )
                    )
                }
            }
            "cl" -> db.commentDao().likeRemote(ev.optString("id"), ev.optLong("d", 0))
            "mu" -> db.commentDao().setMediaUrl(ev.optString("id"), ev.optString("url"))
        }
    }

    suspend fun likeVideo(ctx: Context, v: Video) {
        val liked = !v.likedByMe
        db.videoDao().setLiked(v.id, liked)
        db.videoDao().addLikes(v.id, if (liked) 1 else -1)
        Cloud.publish(
            JSONObject().put("t", "l").put("id", v.id)
                .put("d", if (liked) 1 else -1).put("from", Cloud.deviceId)
        )
    }

    suspend fun saveVideo(v: Video) = db.videoDao().setSaved(v.id, !v.savedByMe)
    suspend fun follow(v: Video) = db.videoDao().setFollowing(v.id, !v.following)
    suspend fun addView(id: String) = db.videoDao().addView(id)

    suspend fun hideVideo(v: Video) {
        db.videoDao().hide(v.id)
        toastFlow.value = "Oculto. Verás menos reels así"
    }

    suspend fun deleteVideo(v: Video) {
        db.videoDao().delete(v.id)
        toastFlow.value = "Reel eliminado"
    }

    suspend fun deleteComment(c: Comment) {
        db.commentDao().delete(c.id)
        toastFlow.value = "Comentario eliminado"
    }

    fun shareLink(v: Video): String =
        if (v.url.startsWith("http")) v.url else "novareels://reel/${v.id}"

    data class Draft(
        val path: String,
        val caption: String,
        val music: String,
        val filter: Int = 0
    )

    fun saveDraft(ctx: Context, file: File, caption: String, music: String, filter: Int = 0) {
        prefs.edit()
            .putString("draftPath", file.absolutePath)
            .putString("draftCaption", caption)
            .putString("draftMusic", music)
            .putInt("draftFilter", filter)
            .apply()
    }

    fun loadDraft(ctx: Context): Draft? {
        val p = prefs.getString("draftPath", null) ?: return null
        return Draft(
            path = p,
            caption = prefs.getString("draftCaption", "") ?: "",
            music = prefs.getString("draftMusic", "") ?: "",
            filter = prefs.getInt("draftFilter", 0)
        )
    }

    fun clearDraft(ctx: Context) {
        prefs.edit()
            .remove("draftPath").remove("draftCaption").remove("draftMusic")
            .remove("draftFilter")
            .apply()
    }

    suspend fun likeComment(c: Comment, forceLike: Boolean = false) {
        val liked = if (forceLike) true else !c.likedByMe
        if (liked == c.likedByMe) return
        db.commentDao().like(c.id, if (liked) 1 else -1, liked)
        Cloud.publish(
            JSONObject().put("t", "cl").put("id", c.id)
                .put("d", if (liked) 1 else -1).put("from", Cloud.deviceId)
        )
    }

    suspend fun repost(v: Video) {
        if (!v.url.startsWith("http")) {
            toastFlow.value = "No se puede repostear un reel local todavía"
            return
        }
        val r = Video(
            id = UUID.randomUUID().toString(), url = v.url,
            author = myName, authorHue = myHue(),
            caption = ("Reposteado de ${v.author}: ${v.caption}").take(140),
            music = v.music, likes = 0, views = 0,
            mine = true, uploaded = true, repostedFrom = v.author
        )
        db.videoDao().upsert(r)
        Cloud.publish(Cloud.videosToJson(r))
        toastFlow.value = "Reposteado a tu perfil"
    }

    suspend fun downloadVideo(ctx: Context, v: Video, filter: Int = 0) {
        toastFlow.value = "Descargando reel…"
        scope.launch {
            val src = if (v.url.startsWith("http")) {
                val tmp = File(ctx.cacheDir, "dl_${System.currentTimeMillis()}.mp4")
                android.util.Log.d("NovaWm", "descarga inicia ${v.url}")
                val okDl = runCatching {
                    val conn = URL(v.url).openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 15000
                    conn.readTimeout = 30000
                    conn.instanceFollowRedirects = true
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 16)")
                    conn.inputStream.use { input -> tmp.outputStream().use { input.copyTo(it) } }
                }.isSuccess
                android.util.Log.d("NovaWm", "descarga ok=$okDl tam=${tmp.length()}")
                if (!okDl) {
                    toastFlow.value = "Descarga falló (red)"
                    return@launch
                }
                tmp
            } else File(v.url)

            val dest = File(ctx.cacheDir, "wm_${System.currentTimeMillis()}.mp4")
            val marca = "Nova Reels · ${v.author}"
            android.util.Log.d("NovaWm", "transcodifica inicia")
            val ok = Watermark.burn(src, dest, marca, filter)
            android.util.Log.d("NovaWm", "transcodifica ok=$ok")
            val finalFile = if (ok) dest else src

            runCatching {
                saveToGallery(ctx, finalFile, "NovaReels_${System.currentTimeMillis()}.mp4")
                if (ok) toastFlow.value = "Descargado con marca de agua en Movies/NovaReels"
                else toastFlow.value = "Descargado sin marca de agua en Movies/NovaReels"
            }.onFailure { toastFlow.value = "No se pudo guardar en la galería" }

            if (ok) runCatching { src.delete() }
            runCatching { dest.delete() }
        }
    }

    private fun saveToGallery(ctx: Context, file: File, name: String) {
        val resolver = ctx.contentResolver
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, name)
            put(android.provider.MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(android.provider.MediaStore.Video.Media.RELATIVE_PATH, "Movies/NovaReels")
        }
        val uri = resolver.insert(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)!!
        resolver.openOutputStream(uri)!!.use { out ->
            file.inputStream().use { it.copyTo(out) }
        }
    }

    suspend fun addComment(
        ctx: Context,
        videoId: String,
        text: String,
        kind: Int,
        sticker: String?,
        mediaFile: File?,
        parentId: String? = null
    ): Comment {
        val c = Comment(
            id = UUID.randomUUID().toString(), videoId = videoId,
            author = myName, authorHue = myHue(),
            text = text, kind = kind, sticker = sticker,
            mediaPath = mediaFile?.absolutePath,
            parentId = parentId,
            createdAt = System.currentTimeMillis()
        )
        db.commentDao().upsert(c)
        scope.launch {
            val ev = JSONObject()
                .put("t", "c").put("id", c.id).put("videoId", videoId)
                .put("author", c.author).put("hue", c.authorHue.toDouble())
                .put("text", c.text).put("kind", kind).put("sticker", sticker ?: "")
                .put("mediaUrl", "").put("parent", parentId ?: "")
                .put("createdAt", c.createdAt)
            Cloud.publish(ev)
            if (mediaFile != null) {
                val url = Cloud.uploadToCatbox(mediaFile)
                if (url != null) {
                    db.commentDao().setMediaUrl(c.id, url)
                    Cloud.publish(Cloud.mediaEvent(c.id, url))
                }
            }
        }
        return c
    }

    suspend fun publishVideo(
        ctx: Context,
        localFile: File,
        caption: String,
        music: String,
        filter: Int = 0,
        startMs: Int = 0,
        endMs: Int = 0
    ): Video {
        var file = localFile
        if (filter > 0 || startMs > 0 || endMs > 0) {
            toastFlow.value = "Procesando vídeo…"
            val out = File(ctx.cacheDir, "filtered_${System.currentTimeMillis()}.mp4")
            val ok = Watermark.burn(localFile, out, "Nova Reels · $myName", filter, startMs, endMs)
            if (ok) {
                val dest = File(localFile.parentFile, "f_${localFile.name}")
                runCatching { out.copyTo(dest, overwrite = true) }
                if (dest.exists() && dest.length() > 4096) file = dest
            }
            runCatching { out.delete() }
        }
        val v = Video(
            id = UUID.randomUUID().toString(), url = file.absolutePath,
            author = myName, authorHue = myHue(), caption = caption, music = music,
            likes = 0, mine = true, uploaded = false
        )
        db.videoDao().upsert(v)
        Notifier.activity(
            ctx, "@luna.crea", "Comentó tu reel: \"Esto es otro nivel 🔥\""
        )
        scope.launch {
            val url = Cloud.uploadToCatbox(localFile)
            if (url != null) {
                db.videoDao().setUrl(v.id, url)
                Cloud.publish(Cloud.videosToJson(v.copy(url = url, uploaded = true)))
            } else {
                cloudStatusText.value = "Subida del vídeo falló (se mantiene local)"
            }
        }
        return v
    }

    private suspend fun retryPendingUploads(ctx: Context) {
        db.videoDao().pendingUploads().forEach { v ->
            if (!v.url.startsWith("http")) {
                val f = File(v.url)
                if (f.exists()) {
                    val url = Cloud.uploadToCatbox(f)
                    if (url != null) {
                        db.videoDao().setUrl(v.id, url)
                        Cloud.publish(Cloud.videosToJson(v.copy(url = url)))
                    }
                }
            }
        }
    }

    private suspend fun seedIfEmpty(ctx: Context) {
        if (db.videoDao().count() > 0) return
        val now = System.currentTimeMillis()
        val w3 = "https://media.w3.org/2010/05/"
        val seeds = listOf(
            Triple(w3 + "sintel/trailer.mp4", "El camino largo siempre tiene la mejor luz 🎬", "Norte — Aurora Sound"),
            Triple("https://vjs.zencdn.net/v/oceans.mp4", "POV: encontraste el spot perfecto para rodar", "Ritmo Callejero — Malú Beats"),
            Triple("https://mdn.github.io/shared-assets/videos/flower.mp4", "Un amanecer entre la niebla, sin filtro ni prisa 🌅", "Neón Pt.2 — DJ Vortex"),
            Triple(w3 + "bunny/movie.mp4", "Edité esto a las 3am y no me arrepiento de nada", "Late Drive — Cassette Lunar"),
            Triple(w3 + "bunny/trailer.mp4", "Toma directa, sin corte, sin miedo ✨", "Verano Infinito — Luna Waves"),
            Triple("https://www.w3schools.com/html/mov_bbb.mp4", "Cuando el clip sale tal cual lo soñaste", "Cyber Slow — Nova FM"),
            Triple("https://www.w3schools.com/html/movie.mp4", "Guarda este reel para tu próxima salida", "Bailar Contigo — Costa Nova"),
            Triple(w3 + "video/movie_300.mp4", "Ruta, música y nada más que hacer", "Carretera 7 — Los Volta"),
            Triple("https://vjs.zencdn.net/v/oceans.mp4", "Repetimos el viaje porque se puede 🌊", "Carretera 7 — Los Volta")
        )
        val authors = listOf(
            "@luna.crea" to 210f, "@mario.edits" to 320f, "@sofi.films" to 30f,
            "@kike.beats" to 120f, "@valen.motion" to 270f, "@dana.frames" to 170f
        )
        val commentsSeed = listOf(
            "Esto es otro nivel 🔥" to "CORAZON",
            "La edición está brutal" to null,
            "¿Con qué grabaste esto? Quedó increíble" to "ESTRELLA",
            "Necesito la ubicación ya" to null,
            "El color grade de la cuenta regresiva me voló la cabeza" to "WOW",
            "Vengo a los comentarios y me quedo por la música" to "LOL"
        )
        seeds.forEachIndexed { i, (file, caption, music) ->
            val (author, hue) = authors[i % authors.size]
            val v = Video(
                id = "seed-$i", url = file, author = author, authorHue = hue,
                caption = caption, music = music,
                likes = (3 + i * 7) * 137L, views = (5 + i * 11) * 1024L,
                createdAt = now - (i + 1) * 3_600_000L
            )
            db.videoDao().upsert(v)
            commentsSeed.take(2 + (i % 3)).forEachIndexed { j, (text, sticker) ->
                val (ca, ch) = authors[(i + j + 2) % authors.size]
                db.commentDao().upsert(
                    Comment(
                        id = "seed-c-$i-$j", videoId = v.id, author = ca, authorHue = ch,
                        text = text, kind = if (sticker != null) Comment.STICKER else Comment.TEXTO,
                        sticker = sticker, likes = (i + j) * 13L,
                        createdAt = now - (i + 1) * 3_000_000L - j * 600_000L
                    )
                )
            }
        }
        db.videoDao().upsert(
            Video(
                id = "seed-mine", url = seeds[1].first, author = myName, authorHue = myHue(),
                caption = "Mi primer reel en Nova ✨", music = "Sonido original — $myName",
                likes = 128, views = 940, mine = true, uploaded = true,
                createdAt = now - 600_000L
            )
        )
    }
}
