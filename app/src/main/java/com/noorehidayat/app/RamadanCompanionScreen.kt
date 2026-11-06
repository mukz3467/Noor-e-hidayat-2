package com.noorehidayat.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamadanCompanionScreen(
    currentLanguage: AppLanguage,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ramadan_prefs", Context.MODE_PRIVATE) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Fasting & Timings, 1: Authentic Duas, 2: Tarawih Guide
    var completedFasts by remember {
        val saved = prefs.getStringSet("completed_fasts", emptySet()) ?: emptySet()
        mutableStateOf(saved.mapNotNull { it.toIntOrNull() }.toSet())
    }

    // Live Countdown Timer state
    var countdownText by remember { mutableStateOf("04h 28m 12s") }
    var isNextIftar by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            val second = cal.get(Calendar.SECOND)

            // Approximate Sehar 04:45, Iftar 18:30 for countdown demonstration
            val currentSecondsOfDay = hour * 3600 + minute * 60 + second
            val seharSeconds = 4 * 3600 + 45 * 60
            val iftarSeconds = 18 * 3600 + 30 * 60

            val (targetSeconds, forIftar) = if (currentSecondsOfDay < seharSeconds) {
                Pair(seharSeconds - currentSecondsOfDay, false)
            } else if (currentSecondsOfDay < iftarSeconds) {
                Pair(iftarSeconds - currentSecondsOfDay, true)
            } else {
                Pair((24 * 3600 - currentSecondsOfDay) + seharSeconds, false)
            }

            isNextIftar = forIftar
            val h = targetSeconds / 3600
            val m = (targetSeconds % 3600) / 60
            val s = targetSeconds % 60
            countdownText = String.format("%02dh %02dm %02ds", h, m, s)
            delay(1000L)
        }
    }

    fun saveFasts(set: Set<Int>) {
        completedFasts = set
        prefs.edit().putStringSet("completed_fasts", set.map { it.toString() }.toSet()).apply()
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "$label copied!", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            I18n.t("ramadan_mode", currentLanguage),
                            color = Cream,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            when (currentLanguage) {
                                AppLanguage.URDU -> "سحر و افطار، تراویح، اور مسنون دعائیں"
                                AppLanguage.ARABIC -> "دليل الصيام والإفطار والتراويح والأدعية"
                                AppLanguage.HINDI -> "सहरी, इफ़्तार, तरावीह और दुआएं"
                                AppLanguage.ENGLISH -> "Sehar, Iftar, Tarawih & Sacred Duas"
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Emerald)
            )
        },
        containerColor = Emerald
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Deep,
                contentColor = Gold,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(it[selectedTab]),
                        color = Gold
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            if (currentLanguage == AppLanguage.URDU) "سحر و افطار" else "Fasting & Times",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (selectedTab == 0) Gold else Muted
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            if (currentLanguage == AppLanguage.URDU) "رمضان کی دعائیں" else "Sacred Duas",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (selectedTab == 1) Gold else Muted
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            if (currentLanguage == AppLanguage.URDU) "نمازِ تراویح" else "Tarawih Guide",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (selectedTab == 2) Gold else Muted
                        )
                    }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                when (selectedTab) {
                    0 -> {
                        // Section 1: Live Sehar & Iftar Countdown Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF241C0C), Color(0xFF0A0A0A))
                                            )
                                        )
                                        .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            if (isNextIftar) "⏳ COUNTDOWN TO IFTAR" else "⏳ COUNTDOWN TO SEHAR ENDS",
                                            color = Gold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            countdownText,
                                            color = Cream,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 32.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Sehar (Imsak)", color = Muted, fontSize = 11.sp)
                                                Text("04:45 AM", color = Gold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            }
                                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Border))
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Iftar (Maghrib)", color = Muted, fontSize = 11.sp)
                                                Text("06:30 PM", color = Gold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 30 Days Fasting Tracker
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Panel),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Ramadan Fasting Tracker (30 Days)",
                                            color = Cream,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "${completedFasts.size} / 30 Fasts",
                                            color = Gold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // 5 rows of 6 fasts
                                    for (row in 0..4) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            for (col in 1..6) {
                                                val fastNum = row * 6 + col
                                                val isChecked = completedFasts.contains(fastNum)
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isChecked) EmeraldGlow else Deep)
                                                        .border(
                                                            1.dp,
                                                            if (isChecked) EmeraldGlow else Border,
                                                            CircleShape
                                                        )
                                                        .clickable {
                                                            val updated = if (isChecked) completedFasts - fastNum else completedFasts + fastNum
                                                            saveFasts(updated)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isChecked) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = Emerald, modifier = Modifier.size(20.dp))
                                                    } else {
                                                        Text("$fastNum", color = Cream, fontSize = 12.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Section 2: Authentic Ramadan Duas
                        item {
                            RamadanDuaCard(
                                title = "Sehri Niyyah (سحر کی نیت)",
                                arabic = "وَبِصَوْمِ غَدٍ نَّوَيْتُ مِنْ شَهْرِ رَمَضَانَ",
                                transliteration = "Wa bisawmi ghadin nawaytu min shahri ramadan",
                                english = "I intend to keep the fast tomorrow for the month of Ramadan.",
                                urdu = "اور میں نے ماہِ رمضان کے کل کے روزے کی نیت کی۔",
                                reference = "Intention of Fasting (Abu Dawood)",
                                onCopy = { copyToClipboard("Sehri Dua", it) }
                            )
                        }

                        item {
                            RamadanDuaCard(
                                title = "Sunnah Iftar Dua (افطار کی مسنون دعا)",
                                arabic = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
                                transliteration = "Dhahaba az-zama'u wabtallatil 'urooqu wa thabatal ajru in sha Allah",
                                english = "The thirst has gone, the veins are moist, and the reward is confirmed, if Allah wills.",
                                urdu = "پیاس چلی گئی، رگیں تر ہو گئیں اور ان شاء اللہ ثواب پکا ہو گیا۔",
                                reference = "Sunan Abi Dawud 2357 (Sahih)",
                                onCopy = { copyToClipboard("Iftar Dua", it) }
                            )
                        }

                        item {
                            RamadanDuaCard(
                                title = "First Ashra Dua - Mercy (پہلا عشرہ: رحمت)",
                                arabic = "رَّبِّ اغْفِرْ وَارْحَمْ وَأَنتَ خَيْرُ الرَّاحِمِينَ",
                                transliteration = "Rabbighfir warham wa anta khayrur-rahimeen",
                                english = "My Lord, forgive and have mercy, for You are the best of the merciful.",
                                urdu = "اے میرے رب! بخش دے اور رحم فرما، اور تو سب سے بہترین رحم فرمانے والا ہے۔",
                                reference = "Surah Al-Mu'minun 23:118",
                                onCopy = { copyToClipboard("1st Ashra Dua", it) }
                            )
                        }

                        item {
                            RamadanDuaCard(
                                title = "Second Ashra Dua - Forgiveness (دوسرا عشرہ: مغفرت)",
                                arabic = "أَسْتَغْفِرُ اللَّهَ رَبِّي مِنْ كُلِّ ذَنْبٍ وَأَتُوبُ إِلَيْهِ",
                                transliteration = "Astaghfirullaha rabbi min kulli dhambinw-wa atoobu ilayh",
                                english = "I ask forgiveness from Allah, my Lord, for every sin and I turn to Him in repentance.",
                                urdu = "میں اللہ سے اپنے تمام گناہوں کی معافی مانگتا ہوں جو میرا رب ہے اور اسی کی طرف رجوع کرتا ہوں۔",
                                reference = "Seeking Forgiveness (Tawbah)",
                                onCopy = { copyToClipboard("2nd Ashra Dua", it) }
                            )
                        }

                        item {
                            RamadanDuaCard(
                                title = "Third Ashra & Laylatul Qadr (تیسرا عشرہ و شبِ قدر)",
                                arabic = "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
                                transliteration = "Allahumma innaka 'afuwwun tuhibbul 'afwa fa'fu 'anni",
                                english = "O Allah, You are Most Forgiving, and You love forgiveness; so forgive me.",
                                urdu = "اے اللہ! تو بہت معاف فرمانے والا ہے، معافی کو پسند فرماتا ہے، پس مجھے معاف فرما دے۔",
                                reference = "Jami` at-Tirmidhi 3513 (Sahih)",
                                onCopy = { copyToClipboard("Laylatul Qadr Dua", it) }
                            )
                        }
                    }

                    2 -> {
                        // Section 3: Tarawih Guide & Authentic Tasbeeh
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Panel),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Authentic Tarawih Tasbeeh (تسبیحِ تراویح)",
                                        color = Gold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "Recited during the rest period (Tarwiha) after every 4 Rakats",
                                        color = Muted,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Deep, RoundedCornerShape(10.dp))
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            "سُبْحَانَ ذِي الْمُلْكِ وَالْمَلَكُوتِ، سُبْحَانَ ذِي الْعِزَّةِ وَالْعَظَمَةِ وَالْهَيْبَةِ وَالْقُدْرَةِ وَالْكِبْرِيَاءِ وَالْجَبَرُوتِ، سُبْحَانَ الْمَلِكِ الْحَيِّ الَّذِي لَا يَنَامُ وَلَا يَمُوتُ، سُبُّوحٌ قُدُّوسٌ رَبُّنَا وَرَبُّ الْمَلَائِكَةِ وَالرُّوحِ، لَا إِلَهَ إِلَّا اللَّهُ نَسْتَغْفِرُ اللَّهَ، نَسْأَلُكَ الْجَنَّةَ وَنَعُوذُ بِكَ مِنَ النَّارِ",
                                            color = Cream,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 28.sp,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        "Urdu Translation:",
                                        color = Gold,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        "پاک ہے وہ ذات جو زمین و آسمان کی بادشاہت والی ہے۔ پاک ہے وہ ذات جو عزت، عظمت، ہیبت، قدرت، بڑائی اور غلبے والی ہے۔ پاک ہے وہ بادشاہ جو ہمیشہ زندہ رہنے والا ہے، جسے نہ نیند آتی ہے اور نہ موت۔ وہ نہایت پاک اور بے عیب ہے، ہمارا اور فرشتوں اور روح القدس (حضرت جبرائیل علیہ السلام) کا پروردگار ہے۔ اللہ کے سوا کوئی معبود نہیں، ہم اللہ سے مغفرت چاہتے ہیں، ہم تجھ سے جنت مانگتے ہیں اور جہنم کی آگ سے تیری پناہ چاہتے ہیں۔",
                                        color = Cream.copy(alpha = 0.9f),
                                        fontSize = 12.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            copyToClipboard(
                                                "Tarawih Tasbeeh",
                                                "سُبْحَانَ ذِي الْمُلْكِ وَالْمَلَكُوتِ، سُبْحَانَ ذِي الْعِزَّةِ وَالْعَظَمَةِ وَالْهَيْبَةِ وَالْقُدْرَةِ وَالْكِبْرِيَاءِ وَالْجَبَرُوتِ..."
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Emerald, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Copy Tasbeeh", color = Emerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Tarawih Rakats (8 and 20 Explanation)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Deep),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Tarawih Guidelines (8 or 20 Rakats)", color = Cream, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "• Both 8 Rakats (as practiced extensively with long qiyam) and 20 Rakats (as established under Caliph Umar ibn al-Khattab and recited at Masjid al-Haram) are authentic Sunnah traditions respected across all major schools of thought.\n• Tarawih is prayed in pairs of 2 rakats each.\n• Take a brief restful pause after every 4 rakats for zikr, tasbeeh, or reflection.\n• Follow with 3 Rakats of Witr prayer.",
                                        color = Muted,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun RamadanDuaCard(
    title: String,
    arabic: String,
    transliteration: String,
    english: String,
    urdu: String,
    reference: String,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                IconButton(onClick = { onCopy("$arabic\n\n$urdu\n\n$english\n($reference)") }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Gold, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Deep, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    arabic,
                    color = Cream,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(transliteration, color = Gold.copy(alpha = 0.8f), fontSize = 11.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(urdu, color = Cream, fontSize = 12.5.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(english, color = Muted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("— $reference", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
