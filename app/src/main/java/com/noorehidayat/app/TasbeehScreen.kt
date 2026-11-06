package com.noorehidayat.app

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun TasbeehScreen(appLanguage: AppLanguage = AppLanguage.ENGLISH) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("noor_tasbeeh_prefs", Context.MODE_PRIVATE) }
    val vibrator = remember { ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    val presetAzkar = remember {
        listOf(
            TasbeehItem("1", "سُبْحَانَ اللَّهِ", "SubhanAllah", "Glory be to Allah", "اللہ پاک ہے", 33),
            TasbeehItem("2", "الْحَمْدُ لِلَّهِ", "Alhamdulillah", "Praise be to Allah", "تمام تعریفیں اللہ کے لیے ہیں", 33),
            TasbeehItem("3", "اللَّهُ أَكْبَرُ", "Allahu Akbar", "Allah is the Greatest", "اللہ سب سے بڑا ہے", 34),
            TasbeehItem("4", "أَسْتَغْفِرُ اللَّهَ", "Astaghfirullah", "I seek Allah's forgiveness", "میں اللہ سے بخشش مانگتا ہوں", 100),
            TasbeehItem("5", "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", "Durood Sharif", "Blessings upon the Prophet ﷺ", "اے اللہ محمد ﷺ پر رحمت بھیج", 100),
            TasbeehItem("6", "لَا إِلٰهَ إِلَّا اللَّهُ", "Kalima Tayyibah", "None worthy of worship but Allah", "اللہ کے سوا کوئی معبود نہیں", 100),
            TasbeehItem("7", "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "La Hawla", "No power except with Allah", "نیکی کی طاقت اور برائی سے بچنے کی ہمت صرف اللہ سے ہے", 100)
        )
    }

    var selectedZikr by remember { mutableStateOf(presetAzkar.first()) }
    var currentCount by remember { mutableIntStateOf(prefs.getInt("zikr_count_${selectedZikr.id}", 0)) }
    var totalDailyCounts by remember { mutableIntStateOf(prefs.getInt("daily_total", 0)) }
    var streakDays by remember { mutableIntStateOf(prefs.getInt("streak_days", 5)) }
    var isHapticEnabled by remember { mutableStateOf(prefs.getBoolean("haptic_enabled", true)) }

    var showCustomDialog by remember { mutableStateOf(false) }
    var customArabic by remember { mutableStateOf("") }
    var customTranslit by remember { mutableStateOf("") }
    var customTargetStr by remember { mutableStateOf("100") }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    fun vibrate(isMilestone: Boolean = false) {
        if (!isHapticEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (isMilestone) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 200), -1))
                } else {
                    vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                vibrator.vibrate(if (isMilestone) 300L else 30L)
            }
        } catch (_: Exception) {}
    }

    fun handleCount() {
        val next = currentCount + 1
        val isTargetReached = next % selectedZikr.target == 0
        currentCount = next
        totalDailyCounts += 1

        prefs.edit()
            .putInt("zikr_count_${selectedZikr.id}", currentCount)
            .putInt("daily_total", totalDailyCounts)
            .putString("last_zikr_date", todayStr)
            .apply()

        vibrate(isMilestone = isTargetReached)
    }

    fun resetCount() {
        currentCount = 0
        prefs.edit().putInt("zikr_count_${selectedZikr.id}", 0).apply()
        vibrate(false)
    }

    val progress = (currentCount % selectedZikr.target).toFloat() / selectedZikr.target.toFloat()
    val animatedRotation by animateFloatAsState(targetValue = currentCount * 12f, label = "beads")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Emerald)
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    I18n.t("tasbeeh", appLanguage).uppercase(),
                    color = Gold,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (appLanguage == AppLanguage.URDU) "ذکر و تسبیح کاؤنٹر" else "Interactive Digital Tasbeeh",
                    color = Cream,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Haptic toggle & custom zikr button
            Row {
                IconButton(onClick = {
                    isHapticEnabled = !isHapticEnabled
                    prefs.edit().putBoolean("haptic_enabled", isHapticEnabled).apply()
                }) {
                    Icon(
                        if (isHapticEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                        null,
                        tint = if (isHapticEnabled) Gold else Muted
                    )
                }
                IconButton(onClick = { showCustomDialog = true }) {
                    Icon(Icons.Default.AddCircleOutline, null, tint = Gold)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Streak Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Panel),
            border = BorderStroke(1.dp, Border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$streakDays-Day Zikr Streak",
                        color = Gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Text(
                    "Total: $totalDailyCounts",
                    color = Cream,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Preset Azkar Horizontal Selector
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presetAzkar) { item ->
                val isSelected = item.id == selectedZikr.id
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedZikr = item
                        currentCount = prefs.getInt("zikr_count_${item.id}", 0)
                    },
                    label = { Text(item.transliteration, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Gold,
                        selectedLabelColor = Emerald,
                        containerColor = Panel,
                        labelColor = Cream
                    ),
                    border = BorderStroke(1.dp, if (isSelected) Gold else Border)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Selected Zikr Display
        Text(
            selectedZikr.arabic,
            color = Cream,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            if (appLanguage == AppLanguage.URDU) selectedZikr.urduMeaning else selectedZikr.englishMeaning,
            color = Gold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(24.dp))

        // --- 3D Tasbeeh Bead Disc Touch Target ---
        Box(
            modifier = Modifier
                .size(240.dp)
                .shadow(16.dp, CircleShape)
                .background(Panel, CircleShape)
                .border(4.dp, Gold, CircleShape)
                .clickable { handleCount() },
            contentAlignment = Alignment.Center
        ) {
            // Rotating bead beads ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(animatedRotation),
                contentAlignment = Alignment.Center
            ) {
                for (i in 0 until 12) {
                    val angle = (i * 30).toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(angle)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = 12.dp)
                                .background(Gold.copy(alpha = 0.8f), CircleShape)
                                .border(1.dp, Emerald, CircleShape)
                        )
                    }
                }
            }

            // Inner Central Tap Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .size(150.dp)
                    .background(Deep, CircleShape)
                    .border(2.dp, Border, CircleShape)
            ) {
                Text(
                    currentCount.toString(),
                    color = Cream,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Target: ${selectedZikr.target}",
                    color = Gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "TAP TO COUNT",
                    color = Muted,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Control Actions (Reset & Milestone)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = { resetCount() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Muted),
                border = BorderStroke(1.dp, Border),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(I18n.t("reset", appLanguage), fontSize = 12.sp)
            }

            Button(
                onClick = {
                    currentCount += selectedZikr.target - (currentCount % selectedZikr.target)
                    vibrate(true)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Panel, contentColor = Gold),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Complete Target (${selectedZikr.target})", fontSize = 12.sp)
            }
        }
    }

    // Custom Zikr Dialog
    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Add Custom Zikr", color = Gold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customArabic,
                        onValueChange = { customArabic = it },
                        placeholder = { Text("Arabic text (e.g. سُبْحَانَ اللَّهِ)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Cream, unfocusedTextColor = Cream)
                    )
                    OutlinedTextField(
                        value = customTranslit,
                        onValueChange = { customTranslit = it },
                        placeholder = { Text("Transliteration / Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Cream, unfocusedTextColor = Cream)
                    )
                    OutlinedTextField(
                        value = customTargetStr,
                        onValueChange = { customTargetStr = it },
                        placeholder = { Text("Target count (e.g. 100)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Cream, unfocusedTextColor = Cream)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val t = customTargetStr.toIntOrNull() ?: 100
                        if (customArabic.isNotBlank() || customTranslit.isNotBlank()) {
                            val newZikr = TasbeehItem(
                                id = System.currentTimeMillis().toString(),
                                arabic = customArabic.ifBlank { customTranslit },
                                transliteration = customTranslit.ifBlank { customArabic },
                                englishMeaning = "Custom Zikr",
                                urduMeaning = "مخصوص ورد",
                                target = t
                            )
                            selectedZikr = newZikr
                            currentCount = 0
                            showCustomDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Emerald)
                ) {
                    Text("Save Zikr")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) { Text("Cancel", color = Muted) }
            },
            containerColor = Panel
        )
    }
}
