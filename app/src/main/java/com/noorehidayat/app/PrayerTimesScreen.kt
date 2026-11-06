package com.noorehidayat.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PrayerRow(val name: String, val time: String, val arabicName: String, val urduName: String)

private val prayerMethods = listOf(
    1 to "University of Islamic Sciences, Karachi",
    2 to "ISNA (North America)",
    3 to "Muslim World League (MWL)",
    4 to "Umm Al-Qura, Makkah",
    5 to "Egyptian General Authority",
    13 to "Diyanet, Turkey"
)

private fun permitted(c: Context) =
    ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
    ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private suspend fun fetchOnlinePrayerData(lat: Double, lon: Double, method: Int, school: Int): Pair<List<PrayerRow>, String> = withContext(Dispatchers.IO) {
    val datePath = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
    val urlStr = "https://api.aladhan.com/v1/timings/$datePath?latitude=$lat&longitude=$lon&method=$method&school=$school"
    val conn = URL(urlStr).openConnection() as HttpURLConnection
    conn.connectTimeout = 12000
    conn.readTimeout = 15000
    try {
        if (conn.responseCode in 200..299) {
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            if (json.optInt("code") == 200) {
                val d = json.getJSONObject("data")
                val t = d.getJSONObject("timings")
                val rows = listOf(
                    PrayerRow("Fajr", t.optString("Fajr").substringBefore(" ").take(5), "الفجر", "فجر"),
                    PrayerRow("Sunrise", t.optString("Sunrise").substringBefore(" ").take(5), "الشروق", "طلوع آفتاب"),
                    PrayerRow("Dhuhr", t.optString("Dhuhr").substringBefore(" ").take(5), "الظهر", "ظہر"),
                    PrayerRow("Asr", t.optString("Asr").substringBefore(" ").take(5), "العصر", "عصر"),
                    PrayerRow("Maghrib", t.optString("Maghrib").substringBefore(" ").take(5), "المغرب", "مغرب"),
                    PrayerRow("Isha", t.optString("Isha").substringBefore(" ").take(5), "العشاء", "عشاء")
                )
                return@withContext rows to d.getJSONObject("date").optString("readable")
            }
        }
    } catch (_: Exception) {}
    finally {
        conn.disconnect()
    }

    // Mathematical Astronomical fallback
    val astro = AstronomicalPrayerCalculator.calculateTimes(lat, lon, isHanafi = (school == 1))
    val rows = listOf(
        PrayerRow("Fajr", astro.fajr, "الفجر", "فجر"),
        PrayerRow("Sunrise", astro.sunrise, "الشروق", "طلوع آفتاب"),
        PrayerRow("Dhuhr", astro.dhuhr, "الظهر", "ظہر"),
        PrayerRow("Asr", astro.asr, "العصر", "عصر"),
        PrayerRow("Maghrib", astro.maghrib, "المغرب", "مغرب"),
        PrayerRow("Isha", astro.isha, "العشاء", "عشاء")
    )
    rows to (astro.dateReadable + " (Astronomical Solar Model)")
}

@Composable
internal fun PrayerTimesScreen(appLanguage: AppLanguage = AppLanguage.ENGLISH) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("noor_prefs", 0) }
    val lm = remember { ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    var loc by remember { mutableStateOf<Location?>(null) }
    var rows by remember { mutableStateOf<List<PrayerRow>>(emptyList()) }
    var dateText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var allowed by remember { mutableStateOf(permitted(ctx)) }
    var method by remember { mutableIntStateOf(prefs.getInt("prayer_method", 1)) }
    var school by remember { mutableIntStateOf(prefs.getInt("prayer_school", 1)) } // 1: Hanafi, 0: Shafi'i
    var methodsExpanded by remember { mutableStateOf(false) }
    var prayerRemindersEnabled by remember {
        mutableStateOf(prefs.getBoolean("prayer_reminders_enabled", false))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && loc != null && rows.isNotEmpty()) {
            prefs.edit()
                .putBoolean("prayer_reminders_enabled", true)
                .putString("prayer_latitude", loc!!.latitude.toString())
                .putString("prayer_longitude", loc!!.longitude.toString())
                .putInt("prayer_method", method)
                .putInt("prayer_school", school)
                .apply()
            prayerRemindersEnabled = true
            error = ""
        } else {
            error = if (granted) {
                "Wait until local prayer times are loaded before enabling reminders."
            } else {
                "Notification permission is required for prayer reminders."
            }
        }
    }

    fun setPrayerReminders(enabled: Boolean) {
        if (!enabled) {
            prayerRemindersEnabled = false
            prefs.edit().putBoolean("prayer_reminders_enabled", false).apply()
            PrayerReminderScheduler.cancelAll(ctx)
            return
        }
        val currentLocation = loc
        if (currentLocation == null || rows.isEmpty()) {
            error = "Wait until local prayer times are loaded before enabling reminders."
            return
        }
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            prefs.edit()
                .putBoolean("prayer_reminders_enabled", true)
                .putString("prayer_latitude", currentLocation.latitude.toString())
                .putString("prayer_longitude", currentLocation.longitude.toString())
                .putInt("prayer_method", method)
                .putInt("prayer_school", school)
                .apply()
            prayerRemindersEnabled = true
            error = ""
        }
    }

    val isAdhanActive by AstronomicalPrayerCalculator.isAdhanPlaying.collectAsState()
    val adhanCountdown by AstronomicalPrayerCalculator.adhanCountdownSeconds.collectAsState()

    fun locate() {
        if (!permitted(ctx)) return
        try {
            val providers = lm.getProviders(true)
            val now = System.currentTimeMillis()
            var bestLocation: Location? = null
            providers.forEach { provider ->
                try {
                    val candidate = lm.getLastKnownLocation(provider)
                    val ageMs = candidate?.let { now - it.time } ?: Long.MAX_VALUE
                    if (candidate != null && ageMs in 0..21_600_000L &&
                        (bestLocation == null || candidate.time > bestLocation!!.time)) {
                        bestLocation = candidate
                    }
                } catch (_: SecurityException) {}
            }
            if (bestLocation != null) {
                loc = bestLocation
                error = ""
                return
            }
            val provider = when {
                LocationManager.NETWORK_PROVIDER in providers -> LocationManager.NETWORK_PROVIDER
                LocationManager.GPS_PROVIDER in providers -> LocationManager.GPS_PROVIDER
                else -> providers.firstOrNull()
            }
            if (provider == null) {
                error = "No location provider is enabled. Turn on device location and retry."
                return
            }
            error = "Waiting for a fresh device location…"
            lm.requestSingleUpdate(provider, object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    loc = location
                    error = ""
                }
                @Deprecated("Deprecated in Android")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
                override fun onProviderEnabled(provider: String) = Unit
                override fun onProviderDisabled(provider: String) {
                    error = "Location provider was disabled. Turn on location and retry."
                }
            }, Looper.getMainLooper())
        } catch (_: SecurityException) {
            error = "Location permission is required for local prayer times."
        } catch (_: Exception) {
            error = "Unable to acquire a fresh device location. Please retry."
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        allowed = r.values.any { it }
        if (allowed) locate() else {
            loc = null
            rows = emptyList()
            error = "Location permission is required for local prayer times."
        }
    }

    LaunchedEffect(Unit) {
        allowed = permitted(ctx)
        locate()
    }

    LaunchedEffect(loc?.latitude, loc?.longitude, method, school) {
        val l = loc
        if (l == null) {
            rows = emptyList()
            dateText = ""
            loading = false
            if (error.isBlank()) error = "Enable location and grant permission to calculate prayer times."
            return@LaunchedEffect
        }
        loading = true
        error = ""
        try {
            val r = fetchOnlinePrayerData(l.latitude, l.longitude, method, school)
            rows = r.first
            dateText = r.second
        } catch (_: Exception) {
            val astro = AstronomicalPrayerCalculator.calculateTimes(l.latitude, l.longitude, school == 1)
            rows = listOf(
                PrayerRow("Fajr", astro.fajr, "الفجر", "فجر"),
                PrayerRow("Sunrise", astro.sunrise, "الشروق", "طلوع آفتاب"),
                PrayerRow("Dhuhr", astro.dhuhr, "الظهر", "ظہر"),
                PrayerRow("Asr", astro.asr, "العصر", "عصر"),
                PrayerRow("Maghrib", astro.maghrib, "المغرب", "مغرب"),
                PrayerRow("Isha", astro.isha, "العشاء", "عشاء")
            )
            dateText = astro.dateReadable + " · Approximate offline solar calculation; method-specific angles are not applied."
            error = "Online calculation unavailable. Offline times are estimates; verify with your local mosque."
        } finally {
            loading = false
        }
    }

    LaunchedEffect(rows, loc?.latitude, loc?.longitude, method, school, prayerRemindersEnabled) {
        val currentLocation = loc
        if (prayerRemindersEnabled && currentLocation != null && rows.isNotEmpty()) {
            prefs.edit()
                .putString("prayer_latitude", currentLocation.latitude.toString())
                .putString("prayer_longitude", currentLocation.longitude.toString())
                .putInt("prayer_method", method)
                .putInt("prayer_school", school)
                .apply()
            PrayerReminderScheduler.scheduleDay(ctx, rows.associate { it.name to it.time })
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            AstronomicalPrayerCalculator.stopAdhan()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Emerald)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text(
            I18n.t("prayer_times", appLanguage).uppercase(),
            color = Gold,
            fontSize = 11.sp,
            letterSpacing = 2.sp
        )
        Text(
            if (appLanguage == AppLanguage.URDU) "اوقاتِ نماز و اذان" else "Timetable & Adhan Alarm",
            color = Cream,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )
        Text(
            dateText.ifBlank { "Astronomical Solar Calculator" },
            color = Muted,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(12.dp))

        // --- Active Adhan Alarm Banner ---
        AnimatedVisibility(visible = isAdhanActive) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldGlow.copy(alpha = 0.2f)),
                border = BorderStroke(1.5.dp, EmeraldGlow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.NotificationsActive, null, tint = EmeraldGlow, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            I18n.t("adhan_playing", appLanguage),
                            color = Cream,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "$adhanCountdown seconds remaining",
                            color = EmeraldGlow,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = { AstronomicalPrayerCalculator.stopAdhan() },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Emerald),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(I18n.t("stop_adhan", appLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Adhan Test Button ---
        Button(
            onClick = {
                if (isAdhanActive) {
                    AstronomicalPrayerCalculator.stopAdhan()
                } else {
                    AstronomicalPrayerCalculator.trigger30SecondAdhan(ctx)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAdhanActive) Color(0xFFEF4444) else Panel,
                contentColor = if (isAdhanActive) Color.White else Gold
            ),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Gold)
        ) {
            Icon(if (isAdhanActive) Icons.Default.VolumeOff else Icons.Default.VolumeUp, null)
            Spacer(Modifier.width(8.dp))
            Text(
                if (isAdhanActive) I18n.t("stop_adhan", appLanguage) else I18n.t("test_adhan", appLanguage),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(10.dp))

        // Calculation Method and School selector
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Asr School (Hanafi / Shafi'i)
            FilterChip(
                selected = school == 1,
                onClick = {
                    school = if (school == 1) 0 else 1
                    prefs.edit().putInt("prayer_school", school).apply()
                },
                label = { Text(if (school == 1) "Hanafi (Asr ×2)" else "Shafi'i (Asr ×1)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Gold,
                    selectedLabelColor = Emerald,
                    containerColor = Panel,
                    labelColor = Cream
                ),
                border = BorderStroke(1.dp, Border)
            )

            // Location refresh
            OutlinedButton(
                onClick = {
                    if (permitted(ctx)) locate()
                    else launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                border = BorderStroke(1.dp, Border),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (allowed) "GPS Active" else "Use GPS", fontSize = 12.sp)
            }
        }

        if (loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 10.dp), color = Gold)
        }

        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { methodsExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                border = BorderStroke(1.dp, Border),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Calculation method: ${prayerMethods.firstOrNull { it.first == method }?.second ?: "Select method"}",
                    fontSize = 12.sp
                )
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = methodsExpanded,
                onDismissRequest = { methodsExpanded = false }
            ) {
                prayerMethods.forEach { (methodId, methodName) ->
                    DropdownMenuItem(
                        text = { Text(methodName) },
                        onClick = {
                            method = methodId
                            prefs.edit().putInt("prayer_method", methodId).apply()
                            methodsExpanded = false
                        }
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Panel),
            border = BorderStroke(1.dp, Border)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (appLanguage == AppLanguage.URDU) "نماز کے اوقات کی یاد دہانی" else "Prayer-time reminders",
                        color = Cream,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        if (appLanguage == AppLanguage.URDU) "ہر نماز کے وقت نوٹیفکیشن" else "Notify at Fajr, Dhuhr, Asr, Maghrib and Isha",
                        color = Muted,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = prayerRemindersEnabled,
                    onCheckedChange = { setPrayerReminders(it) }
                )
            }
        }
        Text(
            if (appLanguage == AppLanguage.URDU)
                "نوٹ: Android کی بیٹری سیونگ پابندیاں نوٹیفکیشن میں تاخیر کر سکتی ہیں۔"
            else
                "Note: Android battery restrictions may delay notifications; exact-alarm access is not requested.",
            color = Muted,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )
        if (error.isNotBlank()) {
            Text(error, color = Gold, fontSize = 11.sp, lineHeight = 16.sp)
        }

        // Prayer Timetable List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp)
        ) {
            items(rows) { row ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    border = BorderStroke(1.dp, Border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                when (appLanguage) {
                                    AppLanguage.URDU -> row.urduName
                                    AppLanguage.ARABIC -> row.arabicName
                                    else -> row.name
                                },
                                color = Cream,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(row.arabicName, color = Muted, fontSize = 11.sp)
                        }
                        Text(
                            row.time,
                            color = Gold,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
