package com.noorehidayat.app

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date
import kotlin.math.*

object AstronomicalPrayerCalculator {

    const val KAABA_LATITUDE = 21.4225
    const val KAABA_LONGITUDE = 39.8262

    data class PrayerTimetable(
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String,
        val dateReadable: String
    )

    /**
     * Calculates astronomical prayer times using solar positioning algorithm.
     * @param lat Latitude in degrees
     * @param lng Longitude in degrees
     * @param isHanafi True for Hanafi Asr (shadow factor = 2), False for Shafi'i/Standard (shadow factor = 1)
     */
    fun calculateTimes(lat: Double, lng: Double, isHanafi: Boolean = true): PrayerTimetable {
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val d = dayOfYear.toDouble()

        // 1. Sun's Declination (in radians)
        val decDeg = -23.44 * cos(Math.toRadians(360.0 / 365.0 * (d + 10.0)))
        val dec = Math.toRadians(decDeg)
        val phi = Math.toRadians(lat)

        // 2. Equation of Time (minutes)
        val eqTime = 15.0 * sin(Math.toRadians(720.0 / 365.0 * (d - 81.0)))

        // 3. Solar Noon (hours)
        val timezoneOffsetHours = (cal.timeZone.rawOffset + cal.timeZone.dstSavings).toDouble() / (1000.0 * 60.0 * 60.0)
        val solarNoon = 12.0 + timezoneOffsetHours - (lng / 15.0) - (eqTime / 60.0)

        // Hour angle helper
        fun hourAngle(altitudeDeg: Double): Double {
            val a = Math.toRadians(altitudeDeg)
            val cosOmega = (sin(a) - sin(phi) * sin(dec)) / (cos(phi) * cos(dec))
            val clamped = cosOmega.coerceIn(-1.0, 1.0)
            return Math.toDegrees(acos(clamped)) / 15.0 // Convert to hours
        }

        // Fajr (altitude = -18°)
        val fajrHA = hourAngle(-18.0)
        val fajrTime = solarNoon - fajrHA

        // Sunrise (altitude = -0.833°)
        val sunriseHA = hourAngle(-0.833)
        val sunriseTime = solarNoon - sunriseHA

        // Asr (shadow ratio)
        val shadowFactor = if (isHanafi) 2.0 else 1.0
        val asrAltRad = atan(1.0 / (shadowFactor + tan(abs(phi - dec))))
        val asrAltDeg = Math.toDegrees(asrAltRad)
        val asrHA = hourAngle(asrAltDeg)
        val asrTime = solarNoon + asrHA

        // Maghrib (altitude = -0.833°)
        val maghribTime = solarNoon + sunriseHA

        // Isha (altitude = -18°)
        val ishaTime = solarNoon + fajrHA

        fun formatHours(h: Double): String {
            val normalized = ((h % 24.0) + 24.0) % 24.0
            val hours = normalized.toInt()
            val mins = ((normalized - hours) * 60.0).roundToInt().coerceIn(0, 59)
            return String.format("%02d:%02d", hours, mins)
        }

        val dateStr = String.format("%02d/%02d/%04d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))

        return PrayerTimetable(
            fajr = formatHours(fajrTime),
            sunrise = formatHours(sunriseTime),
            dhuhr = formatHours(solarNoon),
            asr = formatHours(asrTime),
            maghrib = formatHours(maghribTime),
            isha = formatHours(ishaTime),
            dateReadable = dateStr
        )
    }

    /**
     * Great-Circle Forward Azimuth formula to calculate the exact Qibla direction.
     */
    fun calculateQiblaBearing(lat: Double, lng: Double): Float {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLambda = Math.toRadians(KAABA_LONGITUDE - lng)

        val y = sin(deltaLambda)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)
        val bearing = Math.toDegrees(atan2(y, x))
        return ((bearing % 360.0 + 360.0) % 360.0).toFloat()
    }

    /**
     * Haversine Distance in Kilometers from device location to the Holy Kaaba.
     */
    fun calculateDistanceToKaabaKm(lat: Double, lng: Double): Int {
        val r = 6371.0 // Earth radius in km
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(KAABA_LATITUDE)
        val deltaPhi = Math.toRadians(KAABA_LATITUDE - lat)
        val deltaLambda = Math.toRadians(KAABA_LONGITUDE - lng)

        val a = sin(deltaPhi / 2.0).pow(2.0) + cos(phi1) * cos(phi2) * sin(deltaLambda / 2.0).pow(2.0)
        val c = 2.0 * asin(sqrt(a).coerceIn(0.0, 1.0))
        return (r * c).roundToInt()
    }

    // --- 30-Second Adhan Alarm Engine ---
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var countdownRunnable: Runnable? = null

    private val _isAdhanPlaying = MutableStateFlow(false)
    val isAdhanPlaying = _isAdhanPlaying.asStateFlow()

    private val _adhanCountdownSeconds = MutableStateFlow(0)
    val adhanCountdownSeconds = _adhanCountdownSeconds.asStateFlow()

    fun trigger30SecondAdhan(context: Context) {
        stopAdhan()
        try {
            // High fidelity CDN Adhan with fallback to default notification/alarm tone
            val adhanUrl = "https://cdn.islamic.network/audio/adhan/ar.makkah.mp3"
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                try {
                    setDataSource(adhanUrl)
                    prepareAsync()
                    setOnPreparedListener {
                        start()
                    }
                    setOnErrorListener { _, _, _ ->
                        fallbackToRingtone(context)
                        true
                    }
                } catch (e: Exception) {
                    fallbackToRingtone(context)
                }
            }

            _isAdhanPlaying.value = true
            _adhanCountdownSeconds.value = 30

            countdownRunnable = object : Runnable {
                override fun run() {
                    val current = _adhanCountdownSeconds.value
                    if (current > 1) {
                        _adhanCountdownSeconds.value = current - 1
                        handler.postDelayed(this, 1000L)
                    } else {
                        stopAdhan()
                    }
                }
            }
            handler.postDelayed(countdownRunnable!!, 1000L)

        } catch (e: Exception) {
            fallbackToRingtone(context)
        }
    }

    private fun fallbackToRingtone(context: Context) {
        try {
            val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mediaPlayer = MediaPlayer.create(context, alert)?.apply {
                isLooping = true
                start()
            }
        } catch (_: Exception) {}
    }

    fun stopAdhan() {
        countdownRunnable?.let { handler.removeCallbacks(it) }
        countdownRunnable = null
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        _isAdhanPlaying.value = false
        _adhanCountdownSeconds.value = 0
    }
}
