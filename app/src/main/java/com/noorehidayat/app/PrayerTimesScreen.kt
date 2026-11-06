package com.noorehidayat.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
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

    val isAdhanActive by AstronomicalPrayerCalculator.isAdhanPlaying.collectAsState()
    val adhanCountdown by AstronomicalPrayerCalculator.adhanCountdownSeconds.collectAsState()

    fun locate() {
        if (!permitted(ctx)) return
        try {
            lm.getProviders(true).forEach { p ->
                try {
                    val x = lm.getLastKnownLocation(p)
                    if (x != null && (loc == null || x.time > (loc?.time ?: 0L))) loc = x
                } catch (_: SecurityException) {}
            }
            if (loc == null) {
                // Use default coordinates (e.g. Islamabad/Lahore 31.5204, 74.3587 or Makkah 21.4225, 39.8262)
                loc = Location("default").apply {
                    latitude = 31.5204
                    longitude = 74.3587
                }
            }
        } catch (_: Exception) {
            error = "Unable to read device location."
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        allowed = r.values.any { it }
        if (allowed) locate() else {
            loc = Location("default").apply { latitude = 31.5204; longitude = 74.3587 }
        }
    }

    LaunchedEffect(Unit) {
        allowed = permitted(ctx)
        locate()
    }

    LaunchedEffect(loc?.latitude, loc?.longitude, method, school) {
        val l = loc ?: Location("default").apply { latitude = 31.5204; longitude = 74.3587 }
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
            dateText = astro.dateReadable
        } finally {
            loading = false
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
                    launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
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
