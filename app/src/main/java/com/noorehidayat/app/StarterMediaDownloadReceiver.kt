package com.noorehidayat.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StarterMediaDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (id < 0L || !MediaAssetManager.isAdhanDownloadId(context, id)) return
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.query(DownloadManager.Query().setFilterById(id))?.use { cursor ->
            if (cursor.moveToFirst()) {
                val column = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (column >= 0 && cursor.getInt(column) == DownloadManager.STATUS_SUCCESSFUL) {
                    PrayerReminderScheduler.createChannel(context)
                }
            }
        }
    }
}
