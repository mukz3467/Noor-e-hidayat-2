package com.noorehidayat.app

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class BackendYouTubeVideo(
    val id: String,
    val title: String,
    val channel: String,
    val thumbnail: String,
    val duration: String,
    val publishedTime: String,
    val views: String
)

object YouTubeBackend {
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(18, TimeUnit.SECONDS)
            .callTimeout(22, TimeUnit.SECONDS)
            .build()
    }

    private val baseUrl: String
        get() = BuildConfig.NOOR_BACKEND_BASE_URL.trim().trimEnd('/')

    val isConfigured: Boolean
        get() = baseUrl.startsWith("https://") || baseUrl.startsWith("http://10.0.2.2")

    suspend fun searchVideos(query: String): List<BackendYouTubeVideo> =
        withContext(Dispatchers.IO) {
            require(query.trim().length >= 2) { "Enter at least two characters." }
            val url = baseUrl + "/api/youtube?q=" + Uri.encode(query.trim())
            val json = getJson(url)
            val videos = json.optJSONArray("videos") ?: return@withContext emptyList()
            buildList {
                for (index in 0 until videos.length()) {
                    val item = videos.optJSONObject(index) ?: continue
                    val id = item.optString("id")
                    if (!id.matches(Regex("[A-Za-z0-9_-]{11}"))) continue
                    add(
                        BackendYouTubeVideo(
                            id = id,
                            title = item.optString("title", "YouTube video"),
                            channel = item.optString("channel", "YouTube channel"),
                            thumbnail = item.optString("thumbnail"),
                            duration = item.optString("duration"),
                            publishedTime = item.optString("publishedTime"),
                            views = item.optString("views")
                        )
                    )
                }
            }
        }

    suspend fun liveStreams(): List<LiveStream> = withContext(Dispatchers.IO) {
        val json = getJson(baseUrl + "/api/live-streams")
        val result = mutableListOf<LiveStream>()
        appendStreams(json.optJSONArray("makkah"), result, "Makkah")
        appendStreams(json.optJSONArray("madinah"), result, "Madinah")
        result
    }

    private fun appendStreams(
        array: org.json.JSONArray?,
        destination: MutableList<LiveStream>,
        defaultLocation: String
    ) {
        if (array == null) return
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val youtubeId = item.optString("youtubeId")
            if (!youtubeId.startsWith("channel:") &&
                !youtubeId.matches(Regex("[A-Za-z0-9_-]{11}"))
            ) continue

            destination += LiveStream(
                id = item.optString("id", defaultLocation.lowercase()),
                title = item.optString("title", defaultLocation),
                titleUrdu = item.optString("titleUrdu", defaultLocation),
                location = item.optString("location", defaultLocation),
                youtubeId = youtubeId,
                description = item.optString("description", "")
            )
        }
    }

    private fun getJson(url: String): JSONObject {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("User-Agent", "Noor-e-Hidayat-Android")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching {
                    JSONObject(body).optString("error")
                }.getOrDefault("")
                throw IllegalStateException(
                    message.ifBlank { "Backend request failed (HTTP ${response.code})." }
                )
            }
            return JSONObject(body)
        }
    }
}
