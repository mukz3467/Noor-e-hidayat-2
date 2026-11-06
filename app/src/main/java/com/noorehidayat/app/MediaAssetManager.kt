package com.noorehidayat.app

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File

/** Wi-Fi-only first-launch starter pack: Al-Fatiha recitation + CC0 Adhan. */
internal object MediaAssetManager {
    private const val PREFS = "noor_prefs"
    private const val ADHAN_FILE = "noor_adhan.ogg"
    private const val FATIHA_FILE = "quran_al_fatiha_alafasy.mp3"
    private const val ADHAN_URL = "https://upload.wikimedia.org/wikipedia/commons/e/e7/Adhan.ogg"
    private const val FATIHA_URL = "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/1.mp3"

    fun ensureStarterDownloads(context: Context) {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: return
        if (!musicDir.exists()) musicDir.mkdirs()
        enqueueIfMissing(context, manager, File(musicDir, ADHAN_FILE), ADHAN_URL, "starter_adhan_download")
        enqueueIfMissing(context, manager, File(musicDir, FATIHA_FILE), FATIHA_URL, "starter_fatiha_download")
    }

    private fun enqueueIfMissing(context: Context, manager: DownloadManager, destination: File, url: String, key: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (destination.exists() && destination.length() > 0L) return
        if (prefs.getLong(key, -1L) >= 0L) return
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(if (key.contains("adhan")) "Adhan audio" else "Quran recitation · Al-Fatiha")
            .setDescription("Downloading starter audio for offline use")
            .setMimeType(if (destination.extension == "ogg") "audio/ogg" else "audio/mpeg")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            .setAllowedOverRoaming(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, destination.name)
        prefs.edit().putLong(key, manager.enqueue(request)).apply()
    }

    fun adhanFile(context: Context): File? = mediaFile(context, ADHAN_FILE)

    fun cachedSurahUri(context: Context, surahNumber: Int): Uri? {
        if (surahNumber != 1) return null
        return mediaFile(context, FATIHA_FILE)?.let { Uri.fromFile(it) }
    }

    private fun mediaFile(context: Context, name: String): File? {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: return null
        return File(dir, name).takeIf { it.exists() && it.isFile && it.length() > 0L }
    }

    fun isAdhanDownloadId(context: Context, id: Long): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong("starter_adhan_download", -1L) == id
}
