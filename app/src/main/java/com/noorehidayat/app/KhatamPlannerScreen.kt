package com.noorehidayat.app

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val JUZ_NAMES = listOf(
    "1. Alif Lam Meem (الم)",
    "2. Sayaqool (سيقول)",
    "3. Tilka'r-Rusul (تلك الرسل)",
    "4. Lan Tanaaloo (لن تنالوا)",
    "5. Wal-Muhsanat (والمحصنات)",
    "6. La Yuhibbullah (لا يحب الله)",
    "7. Wa Iza Sami'oo (وإذا سمعوا)",
    "8. Wa Law Annana (ولو أننا)",
    "9. Qalal-Mala'u (قال الملأ)",
    "10. Wa'lamoo (واعلموا)",
    "11. Ya'taziroon (يعتذرون)",
    "12. Wa Mamin Daabbah (وما من دابة)",
    "13. Wa Ma Oobari'oo (وما أبرئ)",
    "14. Ruba'ma (ربما)",
    "15. Subhanallazi (سبحان الذي)",
    "16. Qal Alam (قال ألم)",
    "17. Iqtaraba (اقترب)",
    "18. Qad Aflaha (قد أفلح)",
    "19. Wa Qalallazina (وقال الذين)",
    "20. A'man Khalaqa (أمن خلق)",
    "21. Utlu Ma Oohiya (اتل ما أوحي)",
    "22. Wa Man Yaqnut (ومن يقنت)",
    "23. Wa Maliya (وما لي)",
    "24. Faman Azlamu (فمن أظلم)",
    "25. Ilayhi Yuraddu (إليه يرد)",
    "26. Ha'a Meem (حم)",
    "27. Qala Fama Khatbukum (قال فما خطبكم)",
    "28. Qad Sami'allah (قد سمع الله)",
    "29. Tabarakallazi (تبارك الذي)",
    "30. 'Amma Yatasa'aloon (عم يتساءلون)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhatamPlannerScreen(
    currentLanguage: AppLanguage,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("khatam_planner_prefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var goalDays by remember { mutableStateOf(prefs.getInt("goal_days", 30)) }
    var completedJuzSet by remember {
        val saved = prefs.getStringSet("completed_juz", emptySet()) ?: emptySet()
        mutableStateOf(saved.mapNotNull { it.toIntOrNull() }.toSet())
    }
    var streakDays by remember { mutableStateOf(prefs.getInt("streak_days", 5)) }
    var userReflection by remember { mutableStateOf(prefs.getString("reflection_note", "") ?: "") }
    var showNoteDialog by remember { mutableStateOf(false) }

    fun saveJuzState(newSet: Set<Int>) {
        completedJuzSet = newSet
        prefs.edit().putStringSet("completed_juz", newSet.map { it.toString() }.toSet()).apply()
    }

    val totalAyahs = 6236
    val dailyAyahs = remember(goalDays) { (totalAyahs + goalDays - 1) / goalDays }
    val perPrayerAyahs = remember(dailyAyahs) { (dailyAyahs + 4) / 5 }
    val progressFraction = remember(completedJuzSet) { completedJuzSet.size / 30f }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            I18n.t("khatam_planner", currentLanguage),
                            color = Cream,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            when (currentLanguage) {
                                AppLanguage.URDU -> "ختمِ قرآن کا باقاعدہ ہدف اور روزانہ ٹریکر"
                                AppLanguage.ARABIC -> "خطة ختم القرآن الكريم ومتابعة الأجزاء"
                                AppLanguage.HINDI -> "कुरआन खत्म करने की योजना और ट्रैकर"
                                AppLanguage.ENGLISH -> "Targeted Quran Completion & Streak Tracker"
                            },
                            color = Gold,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Cream)
                    }
                },
                actions = {
                    IconButton(onClick = { showNoteDialog = true }) {
                        Icon(Icons.Default.EditNote, contentDescription = "Notes", tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Emerald)
            )
        },
        containerColor = Emerald
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Progress Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${completedJuzSet.size} / 30 Juz Completed",
                                    color = Cream,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    "Target: $goalDays Days (${dailyAyahs} Ayahs/Day)",
                                    color = Muted,
                                    fontSize = 12.sp
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Deep, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("🔥", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "$streakDays ${I18n.t("streak", currentLanguage)}",
                                    color = Gold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = Gold,
                            trackColor = Deep,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${(progressFraction * 100).toInt()}% Done",
                                color = EmeraldGlow,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Text(
                                "${30 - completedJuzSet.size} Juz Remaining",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Target Plan Selector (30, 60, 90, 365 Days)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Deep),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.URDU -> "ہدفِ تکمیل کا انتخاب کریں:"
                                AppLanguage.ARABIC -> "اختر مدة الختمة:"
                                AppLanguage.HINDI -> "खत्म करने का लक्ष्य चुनें:"
                                AppLanguage.ENGLISH -> "Select Completion Plan:"
                            },
                            color = Gold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(30, 60, 90, 365).forEach { days ->
                                val selected = goalDays == days
                                OutlinedButton(
                                    onClick = {
                                        goalDays = days
                                        prefs.edit().putInt("goal_days", days).apply()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) Gold else Color.Transparent,
                                        contentColor = if (selected) Emerald else Cream
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (selected) Gold else Border
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        if (days == 365) "1 Yr" else "${days}d",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Per Prayer Targets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Panel, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                "Fajr" to perPrayerAyahs,
                                "Dhuhr" to perPrayerAyahs,
                                "Asr" to perPrayerAyahs,
                                "Maghrib" to perPrayerAyahs,
                                "Isha" to perPrayerAyahs
                            ).forEach { (prayer, ayahs) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(prayer, color = Muted, fontSize = 11.sp)
                                    Text(
                                        "$ayahs",
                                        color = Gold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text("Ayahs", color = Cream.copy(alpha = 0.7f), fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Reflection / Note Card Preview
            if (userReflection.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showNoteDialog = true },
                        colors = CardDefaults.cardColors(containerColor = Panel),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.StickyNote2, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("My Quran Notes & Reflections", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(userReflection, color = Cream, fontSize = 12.sp, maxLines = 3)
                            }
                        }
                    }
                }
            }

            // 30 Juz Checklist Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.URDU -> "30 پاروں کی مکمل فہرست"
                            AppLanguage.ARABIC -> "قائمة الأجزاء الثلاثين"
                            AppLanguage.HINDI -> "30 पारे की सूची"
                            AppLanguage.ENGLISH -> "30 Juz / Para Checklist"
                        },
                        color = Cream,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    TextButton(
                        onClick = {
                            if (completedJuzSet.size == 30) saveJuzState(emptySet())
                            else saveJuzState((1..30).toSet())
                        }
                    ) {
                        Text(
                            if (completedJuzSet.size == 30) "Reset All" else "Mark All",
                            color = Gold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 30 Juz Items
            itemsIndexed(JUZ_NAMES) { index, name ->
                val juzNumber = index + 1
                val isDone = completedJuzSet.contains(juzNumber)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val updated = if (isDone) completedJuzSet - juzNumber else completedJuzSet + juzNumber
                            saveJuzState(updated)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) Deep.copy(alpha = 0.9f) else Panel
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDone) EmeraldGlow.copy(alpha = 0.5f) else Border
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        if (isDone) EmeraldGlow else Deep,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Emerald, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("$juzNumber", color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    name,
                                    color = if (isDone) Muted else Cream,
                                    fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    if (isDone) "Completed ✓" else "Not read yet",
                                    color = if (isDone) EmeraldGlow else Muted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Checkbox(
                            checked = isDone,
                            onCheckedChange = {
                                val updated = if (isDone) completedJuzSet - juzNumber else completedJuzSet + juzNumber
                                saveJuzState(updated)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = EmeraldGlow,
                                checkmarkColor = Emerald,
                                uncheckedColor = Border
                            )
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Notes Dialog
    if (showNoteDialog) {
        var tempNote by remember { mutableStateOf(userReflection) }
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = {
                Text("Quran Notes & Duas", color = Cream, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = tempNote,
                    onValueChange = { tempNote = it },
                    placeholder = { Text("Write personal reflections, ayahs to revisit, or intentions...", color = Muted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Cream,
                        unfocusedTextColor = Cream,
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Border
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        userReflection = tempNote
                        prefs.edit().putString("reflection_note", tempNote).apply()
                        showNoteDialog = false
                    }
                ) {
                    Text(I18n.t("save", currentLanguage), color = Gold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Cancel", color = Muted)
                }
            },
            containerColor = Deep,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
