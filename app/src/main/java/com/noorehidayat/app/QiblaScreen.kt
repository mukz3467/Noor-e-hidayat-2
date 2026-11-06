package com.noorehidayat.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlin.math.abs

private fun normalizeDegrees(value: Float): Float = ((value % 360f) + 360f) % 360f
internal fun shortestCompassTurn(degrees: Float): Float = ((degrees + 540f) % 360f) - 180f

internal fun calculateQiblaBearing(lat: Double, lng: Double): Float =
    AstronomicalPrayerCalculator.calculateQiblaBearing(lat, lng)

internal fun magneticToTrueHeading(magneticHeading: Float, location: Location, timeMillis: Long = System.currentTimeMillis()): Float {
    val geo = GeomagneticField(
        location.latitude.toFloat(),
        location.longitude.toFloat(),
        location.altitude.toFloat(),
        timeMillis
    )
    return normalizeDegrees(magneticHeading + geo.declination)
}

@Composable
internal fun QiblaScreen(appLanguage: AppLanguage = AppLanguage.ENGLISH) {
    val ctx = LocalContext.current
    val sensorManager = remember { ctx.getSystemService(Context.POWER_SERVICE); ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val locationManager = remember { ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val rotationSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }

    var coordinates by remember { mutableStateOf<Location?>(null) }
    var locationError by remember { mutableStateOf("") }
    var sensorHeading by remember { mutableStateOf<Float?>(null) }
    var manualDialAngle by remember { mutableFloatStateOf(0f) }
    var isManualMode by remember { mutableStateOf(false) }

    fun refreshLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            coordinates = Location("default").apply { latitude = 31.5204; longitude = 74.3587 }
            return
        }

        try {
            val providers = locationManager.getProviders(true)
            var bestLoc: Location? = null
            for (p in providers) {
                try {
                    val l = locationManager.getLastKnownLocation(p)
                    if (l != null && (bestLoc == null || l.time > bestLoc.time)) {
                        bestLoc = l
                    }
                } catch (_: SecurityException) {}
            }
            if (bestLoc != null) {
                coordinates = bestLoc
            } else {
                coordinates = Location("default").apply { latitude = 31.5204; longitude = 74.3587 }
            }
        } catch (e: Exception) {
            locationError = "Unable to acquire GPS fix. Using default location."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        if (results.values.any { it }) {
            refreshLocation()
        }
    }

    LaunchedEffect(Unit) {
        refreshLocation()
    }

    DisposableEffect(rotationSensor) {
        if (rotationSensor == null) {
            isManualMode = true
            onDispose {}
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val matrix = FloatArray(9)
                    val orientation = FloatArray(3)
                    SensorManager.getRotationMatrixFromVector(matrix, event.values)
                    SensorManager.getOrientation(matrix, orientation)
                    sensorHeading = normalizeDegrees(Math.toDegrees(orientation[0].toDouble()).toFloat())
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val loc = coordinates ?: Location("default").apply { latitude = 31.5204; longitude = 74.3587 }
    val bearing = calculateQiblaBearing(loc.latitude, loc.longitude)
    val distanceKm = AstronomicalPrayerCalculator.calculateDistanceToKaabaKm(loc.latitude, loc.longitude)

    val currentHeading = if (isManualMode || sensorHeading == null) manualDialAngle else {
        magneticToTrueHeading(sensorHeading!!, loc)
    }

    val relativeAngle = normalizeDegrees(bearing - currentHeading)
    val shortestTurn = shortestCompassTurn(relativeAngle)
    val isAligned = abs(shortestTurn) <= 6.0f // Within ±6° alignment

    val dialRingColor by animateColorAsState(
        targetValue = if (isAligned) EmeraldGlow else Gold,
        label = "qibla_ring"
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(Emerald)
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(10.dp))
        Text(
            I18n.t("qibla", appLanguage).uppercase(),
            color = Gold,
            fontSize = 11.sp,
            letterSpacing = 2.sp
        )
        Text(
            if (appLanguage == AppLanguage.URDU) "قبلہ کی سمت اور کعبہ سے فاصلہ" else "Interactive 3D Qibla Compass",
            color = Cream,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )
        Text(
            if (isAligned) "★ ${I18n.t("towards_qibla", appLanguage)}! ★"
            else "Point your phone toward the Kaaba ($distanceKm km)",
            color = if (isAligned) EmeraldGlow else Muted,
            fontSize = 13.sp,
            fontWeight = if (isAligned) FontWeight.Bold else FontWeight.Normal
        )

        Spacer(Modifier.height(20.dp))

        // --- Compass Dial Display ---
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(Panel, CircleShape)
                .border(3.dp, dialRingColor, CircleShape)
                .padding(14.dp)
                .background(Emerald, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // North indicator
            Text(
                "N",
                color = if (abs(currentHeading) < 10) Gold else Muted,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )

            // Compass Needle rotating towards Qibla
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(relativeAngle),
                contentAlignment = Alignment.Center
            ) {
                // Kaaba Icon at the tip of the needle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(if (isAligned) EmeraldGlow else Gold, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🕋", fontSize = 18.sp)
                    }
                    Icon(
                        Icons.Default.Navigation,
                        contentDescription = "Qibla Direction Pointer",
                        tint = if (isAligned) EmeraldGlow else Gold,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(top = 4.dp)
                    )
                    Spacer(Modifier.weight(1f))
                }
            }

            // Center Degree Circle
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Panel, CircleShape)
                    .border(1.5.dp, dialRingColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${bearing.toInt()}°",
                    color = Cream,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // Alignment Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isAligned) EmeraldGlow.copy(alpha = 0.2f) else Panel),
            border = BorderStroke(1.dp, if (isAligned) EmeraldGlow else Border)
        ) {
            Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (isAligned) "ALIGNED WITH THE KAABA (مواجہۃ الکعبۃ)"
                    else if (shortestTurn >= 0) "Turn right by ${shortestTurn.toInt()}° to face Qibla"
                    else "Turn left by ${abs(shortestTurn).toInt()}° to face Qibla",
                    color = if (isAligned) EmeraldGlow else Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kaaba Azimuth: ${bearing.toInt()}°", color = Muted, fontSize = 12.sp)
                    Text("Distance: $distanceKm km", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Manual Dial rotation slider for desktop / emulators / devices without rotation sensor
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Panel),
            border = BorderStroke(1.dp, Border)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isManualMode) "Manual Compass Dial (${manualDialAngle.toInt()}°)" else "Hardware Sensor Active",
                        color = Cream,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { isManualMode = !isManualMode }) {
                        Text(if (isManualMode) "Use Sensor" else "Manual Dial", color = Gold, fontSize = 11.sp)
                    }
                }
                if (isManualMode || rotationSensor == null) {
                    Slider(
                        value = manualDialAngle,
                        onValueChange = { manualDialAngle = it },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = Gold,
                            activeTrackColor = Gold,
                            inactiveTrackColor = Border
                        )
                    )
                }
            }
        }
    }
}
