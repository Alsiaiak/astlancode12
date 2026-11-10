package com.novareels.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object Cloud {
    const val DEFAULT_TOPIC = "novareels-prod-a3f7c2d9e5"
    private const val NTFY = "https://ntfy.sh"
    private const val CATBOX = "https://catbox.moe/user/api.php"
    private const val UA = "NovaReels/1.0"

    var topic: String = DEFAULT_TOPIC
    var deviceId: String = ""

    enum class Status { OFFLINE, OK }
    @Volatile var lastOkAt: Long = 0

    private fun conn(url: String, method: String = "GET"): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 20000
            setRequestProperty("User-Agent", UA)
        }

    suspend fun publish(event: JSONObject): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val c = conn("$NTFY/${topic}", "POST")
            c.doOutput = true
            c.setRequestProperty("Title", "nova")
            c.setRequestProperty("Content-Type", "text/plain")
            c.outputStream.use { it.write(event.toString().toByteArray()) }
            (c.responseCode == 200).also {
                android.util.Log.d("NovaCloud", "publish ${event.optString("t")} -> $it")
                if (it) lastOkAt = System.currentTimeMillis()
            }
        }.onFailure { android.util.Log.d("NovaCloud", "publish ERROR ${it.message}") }
            .getOrDefault(false)
    }

    suspend fun poll(since: String): List<JSONObject> = withContext(Dispatchers.IO) {
        runCatching {
            val c = conn("$NTFY/${topic}/json?poll=1&since=$since")
            if (c.responseCode != 200) {
                android.util.Log.d("NovaCloud", "poll code ${c.responseCode}")
                return@runCatching emptyList()
            }
            android.util.Log.d("NovaCloud", "poll OK since=$since")
            val body = c.inputStream.bufferedReader().readText()
            lastOkAt = System.currentTimeMillis()
            body.lineSequence().filter { it.isNotBlank() }.mapNotNull { line ->
                val obj = runCatching { JSONObject(line) }.getOrNull() ?: return@mapNotNull null
                if (obj.optString("event") != "message") return@mapNotNull null
                runCatching { JSONObject(obj.optString("message")) }.getOrNull()
            }.filter { it.optString("from", "") != deviceId }.toList()
        }.onFailure { android.util.Log.d("NovaCloud", "poll ERROR ${it.message}") }
            .getOrDefault(emptyList())
    }

    suspend fun uploadToCatbox(file: File): String? = withContext(Dispatchers.IO) {
        runCatching {
            val boundary = "nova" + UUID.randomUUID().toString().replace("-", "")
            val c = conn(CATBOX, "POST")
            c.doOutput = true
            c.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
            c.setRequestProperty("User-Agent", UA)
            DataOutputStream(c.outputStream).use { out ->
                out.writeBytes("--$boundary\r\n")
                out.writeBytes("Content-Disposition: form-data; name=\"reqtype\"\r\n\r\n")
                out.writeBytes("fileupload\r\n")
                out.writeBytes("--$boundary\r\n")
                out.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"${file.name}\"\r\n")
                out.writeBytes("Content-Type: application/octet-stream\r\n\r\n")
                file.inputStream().use { it.copyTo(out, 8192) }
                out.writeBytes("\r\n--$boundary--\r\n")
            }
            if (c.responseCode == 200) {
                val res = c.inputStream.bufferedReader().readText().trim()
                if (res.startsWith("https://")) res else null
            } else null
        }.getOrNull()
    }

    fun mediaEvent(commentId: String, url: String): JSONObject =
        JSONObject().put("t", "mu").put("id", commentId).put("url", url)

    fun videosToJson(v: Video): JSONObject = JSONObject()
        .put("t", "v").put("id", v.id).put("url", v.url)
        .put("author", v.author).put("hue", v.authorHue.toDouble())
        .put("caption", v.caption).put("music", v.music)
        .put("likes", v.likes).put("repost", v.repostedFrom)
        .put("createdAt", v.createdAt)
}
