package com.noorehidayat.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoorApp()
        }
    }
}

// Backward-compatibility helper for any existing tests
internal fun localized(lang: String, english: String, urdu: String): String =
    if (lang == "اردو") urdu else english

internal fun resolveBackPage(page: String, mediaParentPage: String, videoParentPage: String): String = when (page) {
    "Reader", "Recitations", "Live", "Lectures", "Learning", "Duas", "Quiz", "Qibla", "Tafseer", "Prayer Times", "Favourites",
    "Tasbeeh", "Hisn al-Muslim", "Hadith", "Hijri Calendar", "Zakat", "Khatam Planner", "Social Card", "Ramadan Companion", "Theme Settings" -> "Home"
    else -> "Home"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoorApp() {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    val themeManager = remember { ThemeManager(ctx) }
    val isSystemDark = isSystemInDarkTheme()
    val activeColors = themeManager.resolveActiveColors(isSystemDark)

    var appLanguage by remember {
        val saved = prefs.getString("noor_app_lang", "ur") ?: "ur"
        mutableStateOf(AppLanguage.values().find { it.code == saved } ?: AppLanguage.URDU)
    }

    var currentPage by remember { mutableStateOf("Home") }
    var selectedSurah by remember { mutableStateOf<Surah?>(null) }
    var selectedLiveStream by remember { mutableStateOf<LiveStream?>(null) }
    var selectedLecture by remember { mutableStateOf<LectureVideo?>(null) }
    var activeQari by remember { mutableStateOf(QarisAudioService.qaris.first()) }

    var isTheaterMode by remember { mutableStateOf(false) }
    var arabicFontSize by remember { mutableFloatStateOf(prefs.getFloat("arabic_font_size", 28f)) }
    var isAudioBarVisible by remember { mutableStateOf(false) }

    // Media Player state
    val exoPlayer = remember { ExoPlayer.Builder(ctx).build() }
    var isPlayingAudio by remember { mutableStateOf(false) }
    var audioProgress by remember { mutableFloatStateOf(0f) }
    var currentCdnIndex by remember { mutableIntStateOf(0) }

    // Favourites
    var favourites by remember {
        val favStr = prefs.getString("fav_surahs", "") ?: ""
        mutableStateOf(favStr.split(",").mapNotNull { it.toIntOrNull() }.toSet())
    }

    fun toggleFavourite(num: Int) {
        favourites = if (favourites.contains(num)) favourites - num else favourites + num
        prefs.edit().putString("fav_surahs", favourites.joinToString(",")).apply()
    }

    fun playSurahAudio(surah: Surah, qari: Qari = activeQari) {
        activeQari = qari
        selectedSurah = surah
        isAudioBarVisible = true
        currentCdnIndex = 0

        val urls = QarisAudioService.getAudioUrls(qari, surah.number)
        if (urls.isNotEmpty()) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(MediaItem.fromUri(urls[0]))
            exoPlayer.prepare()
            exoPlayer.play()
            isPlayingAudio = true
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isPlayingAudio = isPlaying
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                // Failover to next CDN
                val surah = selectedSurah ?: return
                val urls = QarisAudioService.getAudioUrls(activeQari, surah.number)
                if (currentCdnIndex + 1 < urls.size) {
                    currentCdnIndex++
                    exoPlayer.setMediaItem(MediaItem.fromUri(urls[currentCdnIndex]))
                    exoPlayer.prepare()
                    exoPlayer.play()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Audio progress polling
    LaunchedEffect(isPlayingAudio) {
        while (isPlayingAudio) {
            val dur = exoPlayer.duration
            val pos = exoPlayer.currentPosition
            if (dur > 0) {
                audioProgress = (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
            }
            delay(500L)
        }
    }

    BackHandler(enabled = currentPage != "Home") {
        if (isTheaterMode) {
            isTheaterMode = false
        } else {
            currentPage = "Home"
        }
    }

    Scaffold(
        topBar = {
            if (!isTheaterMode) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "نورِ ہدایت",
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Noor-e-Hidayat",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }
                    },
                    actions = {
                        // Language switcher button
                        var langMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { langMenuExpanded = true }) {
                                Icon(Icons.Default.Translate, null, tint = Gold, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(appLanguage.label, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = langMenuExpanded,
                                onDismissRequest = { langMenuExpanded = false },
                                modifier = Modifier.background(Panel)
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang.label, color = if (appLanguage == lang) Gold else Cream) },
                                        onClick = {
                                            appLanguage = lang
                                            prefs.edit().putString("noor_app_lang", lang.code).apply()
                                            langMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Emerald,
                        titleContentColor = Cream
                    )
                )
            }
        },
        bottomBar = {
            if (!isTheaterMode) {
                Column {
                    // Global Floating Audio Player Bar
                    if (isAudioBarVisible && selectedSurah != null) {
                        Surface(
                            color = Panel,
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "${selectedSurah!!.english} (${selectedSurah!!.name})",
                                            color = Cream,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(activeQari.name, color = Gold, fontSize = 11.sp)
                                    }
                                    // Rewind 10s
                                    IconButton(onClick = { exoPlayer.seekTo((exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)) }) {
                                        Icon(Icons.Default.Replay10, null, tint = Gold)
                                    }
                                    // Play / Pause
                                    IconButton(onClick = {
                                        if (isPlayingAudio) exoPlayer.pause() else exoPlayer.play()
                                    }) {
                                        Icon(if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Gold)
                                    }
                                    // Forward 10s
                                    IconButton(onClick = { exoPlayer.seekTo((exoPlayer.currentPosition + 10000L).coerceAtMost(exoPlayer.duration)) }) {
                                        Icon(Icons.Default.Forward10, null, tint = Gold)
                                    }
                                    // Close Audio
                                    IconButton(onClick = {
                                        exoPlayer.stop()
                                        isAudioBarVisible = false
                                    }) {
                                        Icon(Icons.Default.Close, null, tint = Muted)
                                    }
                                }
                                Slider(
                                    value = audioProgress,
                                    onValueChange = {
                                        audioProgress = it
                                        val dur = exoPlayer.duration
                                        if (dur > 0) exoPlayer.seekTo((it * dur).toLong())
                                    },
                                    colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold, inactiveTrackColor = Border),
                                    modifier = Modifier.height(14.dp)
                                )
                            }
                        }
                    }

                    // Navigation Bar
                    NavigationBar(containerColor = Emerald, contentColor = Cream) {
                        NavigationBarItem(
                            selected = currentPage == "Home",
                            onClick = { currentPage = "Home" },
                            icon = { Icon(Icons.Default.Home, null) },
                            label = { Text(I18n.t("home", appLanguage), fontSize = 10.sp) },
                            colors = navColors()
                        )
                        NavigationBarItem(
                            selected = currentPage == "Quran",
                            onClick = { currentPage = "Quran" },
                            icon = { Icon(Icons.Default.Book, null) },
                            label = { Text(I18n.t("quran", appLanguage), fontSize = 10.sp) },
                            colors = navColors()
                        )
                        NavigationBarItem(
                            selected = currentPage == "Prayer Times",
                            onClick = { currentPage = "Prayer Times" },
                            icon = { Icon(Icons.Default.Schedule, null) },
                            label = { Text(I18n.t("prayer_times", appLanguage), fontSize = 10.sp) },
                            colors = navColors()
                        )
                        NavigationBarItem(
                            selected = currentPage == "Qibla",
                            onClick = { currentPage = "Qibla" },
                            icon = { Icon(Icons.Default.Explore, null) },
                            label = { Text(I18n.t("qibla", appLanguage), fontSize = 10.sp) },
                            colors = navColors()
                        )
                        NavigationBarItem(
                            selected = currentPage == "More",
                            onClick = { currentPage = "More" },
                            icon = { Icon(Icons.Default.MoreHoriz, null) },
                            label = { Text(I18n.t("more", appLanguage), fontSize = 10.sp) },
                            colors = navColors()
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .background(Emerald)
                .padding(paddingValues)
        ) {
            when (currentPage) {
                "Home" -> HomeScreen(
                    appLanguage = appLanguage,
                    onNavigate = { page -> currentPage = page },
                    onOpenSurah = { surah ->
                        selectedSurah = surah
                        currentPage = "Reader"
                    }
                )
                "Quran" -> QuranCatalogueScreen(
                    appLanguage = appLanguage,
                    onOpenSurah = { surah ->
                        selectedSurah = surah
                        currentPage = "Reader"
                    },
                    onPlayAudio = { surah -> playSurahAudio(surah) }
                )
                "Reader" -> QuranReaderScreen(
                    surah = selectedSurah ?: QuranDataService.allSurahs.first(),
                    appLanguage = appLanguage,
                    arabicFontSize = arabicFontSize,
                    onFontSizeChange = {
                        arabicFontSize = it
                        prefs.edit().putFloat("arabic_font_size", it).apply()
                    },
                    isFav = favourites.contains(selectedSurah?.number ?: 1),
                    onToggleFav = { toggleFavourite(selectedSurah?.number ?: 1) },
                    onPlayAudio = { surah -> playSurahAudio(surah) },
                    activeQari = activeQari,
                    onSelectQari = { qari -> activeQari = qari }
                )
                "Prayer Times" -> PrayerTimesScreen(appLanguage = appLanguage)
                "Qibla" -> QiblaScreen(appLanguage = appLanguage)
                "Live" -> LiveHarameynScreen(
                    appLanguage = appLanguage,
                    isTheaterMode = isTheaterMode,
                    onToggleTheater = { isTheaterMode = !isTheaterMode },
                    selectedStream = selectedLiveStream ?: IslamicContentData.liveStreams.first(),
                    onSelectStream = { selectedLiveStream = it }
                )
                "Lectures" -> IslamicLecturesScreen(
                    appLanguage = appLanguage,
                    isTheaterMode = isTheaterMode,
                    onToggleTheater = { isTheaterMode = !isTheaterMode },
                    selectedLecture = selectedLecture ?: IslamicContentData.curatedLectures.first(),
                    onSelectLecture = { selectedLecture = it }
                )
                "Duas" -> DailyDuasScreen(appLanguage = appLanguage)
                "Quiz" -> IslamicQuizScreen(appLanguage = appLanguage)
                "Tasbeeh" -> TasbeehScreen(appLanguage = appLanguage)
                "Hisn al-Muslim" -> HisnAlMuslimScreen(appLanguage = appLanguage)
                "Hadith" -> HadithLibraryScreen(appLanguage = appLanguage, onOpenCardGenerator = { _, _, _ -> currentPage = "Social Card" })
                "Hijri Calendar" -> HijriCalendarScreen(appLanguage = appLanguage)
                "Zakat" -> ZakatCalculatorScreen(appLanguage = appLanguage)
                "Khatam Planner" -> KhatamPlannerScreen(currentLanguage = appLanguage, onNavigateBack = { currentPage = "Home" })
                "Social Card" -> SocialCardGeneratorScreen(currentLanguage = appLanguage, onNavigateBack = { currentPage = "Home" })
                "Ramadan Companion" -> RamadanCompanionScreen(currentLanguage = appLanguage, onNavigateBack = { currentPage = "Home" })
                "More" -> MoreHubScreen(
                    appLanguage = appLanguage,
                    onNavigate = { page -> currentPage = page }
                )
            }
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Gold,
    selectedTextColor = Gold,
    indicatorColor = Panel,
    unselectedIconColor = Muted,
    unselectedTextColor = Muted
)

// --- Home Screen ---
@Composable
private fun HomeScreen(
    appLanguage: AppLanguage,
    onNavigate: (String) -> Unit,
    onOpenSurah: (Surah) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Holy Kaaba Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Panel),
                border = BorderStroke(1.5.dp, Gold.copy(alpha = 0.7f))
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(185.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.img_kaaba_banner),
                        contentDescription = "The Holy Kaaba",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x40000000),
                                        Color(0xB3000000),
                                        Color(0xFF000000)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "﷽",
                                color = Gold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                I18n.t("app_title", appLanguage).uppercase(),
                                color = Gold,
                                fontSize = 12.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (appLanguage == AppLanguage.URDU) "قرآن پاک، نماز اور اسلامی رہنمائی" else "Your Complete Islamic Companion",
                            color = Cream,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (appLanguage == AppLanguage.URDU) "مستند قرآنی آیات، خوبصورت تلاوتیں، قبلہ نما اور حرمین شریفین لائیو نشریات۔"
                            else "Authentic Quranic calligraphy, audio recitations, live Harameyn & prayer times.",
                            color = Muted,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Ramadan Countdown Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("Ramadan Companion") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Deep),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Panel, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌙", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (appLanguage == AppLanguage.URDU) "رمضان المبارک اسپیشل موڈ" else "Ramadan Companion Mode",
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            if (appLanguage == AppLanguage.URDU) "سحر و افطار لائیو کاؤنٹ ڈاؤن اور نمازِ تراویح گائیڈ" else "Live Sehar/Iftar Timings, Tarawih Tasbeeh & Fasting Tracker",
                            color = Muted,
                            fontSize = 11.sp
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = Gold)
                }
            }
        }

        // Daily Hadith Card with 1-Click Social Card Creator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Panel),
                border = BorderStroke(1.dp, Border)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 16.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (appLanguage == AppLanguage.URDU) "حدیثِ نبوی ﷺ" else "HADITH OF THE DAY",
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        TextButton(
                            onClick = { onNavigate("Social Card") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Share, null, tint = Gold, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(I18n.t("share", appLanguage), color = Gold, fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "\"تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ\"",
                        color = Cream,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (appLanguage == AppLanguage.URDU)
                            "اپنے مسلمان بھائی کے سامنے تمہارا مسکرانا بھی تمہارے لیے صدقہ ہے۔ (جامع ترمذی)"
                        else
                            "Your smiling in the face of your brother is charity for you. (Jami` at-Tirmidhi)",
                        color = Muted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Quick Feature Grid (All 10 World-Class Features)
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeFeatureTile(
                    title = I18n.t("quran", appLanguage),
                    sub = "114 Surahs & Hifz",
                    icon = "📖",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Quran") }
                )
                HomeFeatureTile(
                    title = I18n.t("tasbeeh", appLanguage),
                    sub = "3D Rotating Beads",
                    icon = "📿",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Tasbeeh") }
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeFeatureTile(
                    title = I18n.t("hisn_al_muslim", appLanguage),
                    sub = "150+ Authentic Duas",
                    icon = "🤲",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Hisn al-Muslim") }
                )
                HomeFeatureTile(
                    title = I18n.t("hadith_library", appLanguage),
                    sub = "Bukhari & Muslim",
                    icon = "📚",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Hadith") }
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeFeatureTile(
                    title = I18n.t("khatam_planner", appLanguage),
                    sub = "30 Juz Tracker & Streak",
                    icon = "🎯",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Khatam Planner") }
                )
                HomeFeatureTile(
                    title = I18n.t("card_generator", appLanguage),
                    sub = "4 Luxury Themes",
                    icon = "🖼️",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Social Card") }
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeFeatureTile(
                    title = I18n.t("prayer_times", appLanguage),
                    sub = "30s Adhan Alarm",
                    icon = "🕌",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Prayer Times") }
                )
                HomeFeatureTile(
                    title = I18n.t("qibla", appLanguage),
                    sub = "Kaaba Compass",
                    icon = "🧭",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Qibla") }
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeFeatureTile(
                    title = I18n.t("hijri_calendar", appLanguage),
                    sub = "Moon Phases & Sunnah",
                    icon = "🌙",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Hijri Calendar") }
                )
                HomeFeatureTile(
                    title = I18n.t("zakat_calculator", appLanguage),
                    sub = "Nisab in 7 Currencies",
                    icon = "💰",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Zakat") }
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeFeatureTile(
                    title = I18n.t("live_harameyn", appLanguage),
                    sub = "Makkah & Madinah Live",
                    icon = "🕋",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Live") }
                )
                HomeFeatureTile(
                    title = I18n.t("lectures", appLanguage),
                    sub = "Topics & Scholars",
                    icon = "🎙️",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Lectures") }
                )
            }
        }

        item {
            // Featured Surahs Row
            Text(
                "FEATURED SURAHS",
                color = Gold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Spacer(Modifier.height(8.dp))
            val featured = listOf(1, 18, 36, 55, 67, 112)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(QuranDataService.allSurahs.filter { it.number in featured }) { surah ->
                    Card(
                        modifier = Modifier
                            .width(150.dp)
                            .clickable { onOpenSurah(surah) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Panel),
                        border = BorderStroke(1.dp, Border)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(surah.name, color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(surah.english, color = Cream, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${surah.ayahs} Ayahs", color = Muted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeFeatureTile(title: String, sub: String, icon: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(icon, fontSize = 26.sp)
            Spacer(Modifier.height(8.dp))
            Text(title, color = Cream, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(sub, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

// --- Quran Catalogue Screen ---
@Composable
private fun QuranCatalogueScreen(
    appLanguage: AppLanguage,
    onOpenSurah: (Surah) -> Unit,
    onPlayAudio: (Surah) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery) {
        QuranDataService.allSurahs.filter {
            searchQuery.isBlank() ||
            it.english.contains(searchQuery, ignoreCase = true) ||
            it.name.contains(searchQuery) ||
            it.number.toString() == searchQuery.trim()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text(I18n.t("quran", appLanguage).uppercase(), color = Gold, fontSize = 11.sp, letterSpacing = 2.sp)
        Text(
            if (appLanguage == AppLanguage.URDU) "قرآن مجید کی سورتیں" else "The Holy Quran",
            color = Cream,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(I18n.t("search_surah", appLanguage), color = Muted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Gold) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = Border,
                focusedTextColor = Cream,
                unfocusedTextColor = Cream,
                focusedContainerColor = Panel,
                unfocusedContainerColor = Panel
            )
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp)
        ) {
            items(filtered) { surah ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSurah(surah) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    border = BorderStroke(1.dp, Border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Deep, CircleShape)
                                .border(1.dp, Gold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(surah.number.toString(), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(surah.english, color = Cream, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${surah.meaning} · ${surah.ayahs} Ayahs", color = Muted, fontSize = 11.sp)
                        }
                        Text(surah.name, color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { onPlayAudio(surah) }) {
                            Icon(Icons.Default.PlayCircle, null, tint = Gold)
                        }
                    }
                }
            }
        }
    }
}

// --- Quran Reader Screen ---
@Composable
private fun QuranReaderScreen(
    surah: Surah,
    appLanguage: AppLanguage,
    arabicFontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    isFav: Boolean,
    onToggleFav: () -> Unit,
    onPlayAudio: (Surah) -> Unit,
    activeQari: Qari,
    onSelectQari: (Qari) -> Unit
) {
    val ctx = LocalContext.current
    var ayahs by remember(surah.number) { mutableStateOf<List<Ayah>>(emptyList()) }
    var loading by remember(surah.number) { mutableStateOf(true) }
    var error by remember(surah.number) { mutableStateOf("") }
    var showControls by remember { mutableStateOf(false) }

    LaunchedEffect(surah.number) {
        loading = true
        error = ""
        try {
            ayahs = QuranDataService.getSurahBatch(ctx, surah.number)
        } catch (e: Exception) {
            error = "Could not load Surah text. Check internet connection."
        } finally {
            loading = false
        }
    }

    var showHifzMode by remember { mutableStateOf(false) }
    var hifzRepeatTarget by remember { mutableIntStateOf(3) } // 1, 3, 5, 10, -1 for infinite
    var hifzPlaybackSpeed by remember { mutableFloatStateOf(1.0f) } // 0.75, 1.0, 1.25, 1.5
    var isHifzActive by remember { mutableStateOf(false) }
    var hifzCurrentLoop by remember { mutableIntStateOf(1) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("SURAH ${surah.number} · ${surah.type.uppercase()}", color = Gold, fontSize = 10.sp, letterSpacing = 1.5.sp)
                Text("${surah.english} (${surah.name})", color = Cream, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { onToggleFav() }) {
                Icon(if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = Gold)
            }
            IconButton(onClick = { showHifzMode = !showHifzMode }) {
                Icon(Icons.Default.Repeat, "Hifz Mode", tint = if (showHifzMode || isHifzActive) EmeraldGlow else Gold)
            }
            IconButton(onClick = { showControls = !showControls }) {
                Icon(Icons.Default.FormatSize, null, tint = Gold)
            }
            Button(
                onClick = { onPlayAudio(surah) },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Emerald),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(4.dp))
                Text("Listen", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Hifz Memorization & Verse Loop Panel (Feature 6)
        AnimatedVisibility(visible = showHifzMode) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Deep),
                border = BorderStroke(1.dp, if (isHifzActive) EmeraldGlow else Gold.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                if (appLanguage == AppLanguage.URDU) "حفظ و تکرار موڈ (آڈیو ریپیٹ لوپ)" else "Hifz Memorization & Loop Mode",
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                if (isHifzActive) "Loop Active: Repetition $hifzCurrentLoop of ${if (hifzRepeatTarget < 0) "∞" else "$hifzRepeatTarget"} (Speed: ${hifzPlaybackSpeed}x)"
                                else "Repeat verse audio for effortless memorization",
                                color = if (isHifzActive) EmeraldGlow else Muted,
                                fontSize = 11.sp
                            )
                        }
                        if (isHifzActive) {
                            Badge(containerColor = EmeraldGlow) {
                                Text("ACTIVE", color = Emerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Repetition target chips
                    Text("Repetition Target:", color = Cream, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1 to "1x", 3 to "3x", 5 to "5x", 10 to "10x", -1 to "∞ Loop").forEach { (count, label) ->
                            val isSelected = hifzRepeatTarget == count
                            OutlinedButton(
                                onClick = { hifzRepeatTarget = count },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) Gold else Panel,
                                    contentColor = if (isSelected) Emerald else Cream
                                ),
                                border = BorderStroke(1.dp, if (isSelected) Gold else Border),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Audio speed chips
                    Text("Recitation Speed:", color = Cream, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.75f to "0.75x", 1.0f to "1.0x (Normal)", 1.25f to "1.25x", 1.5f to "1.5x").forEach { (speed, label) ->
                            val isSelected = hifzPlaybackSpeed == speed
                            OutlinedButton(
                                onClick = { hifzPlaybackSpeed = speed },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) Gold else Panel,
                                    contentColor = if (isSelected) Emerald else Cream
                                ),
                                border = BorderStroke(1.dp, if (isSelected) Gold else Border),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isHifzActive = !isHifzActive
                            if (isHifzActive) {
                                hifzCurrentLoop = 1
                                onPlayAudio(surah)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHifzActive) EmeraldGlow else Gold,
                            contentColor = Emerald
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(if (isHifzActive) Icons.Default.Stop else Icons.Default.PlayCircle, null)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (isHifzActive) "Stop Hifz Mode" else "Start Hifz Memorization Loop",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Expandable Font Size Slider & Qari Picker
        AnimatedVisibility(visible = showControls) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Panel),
                border = BorderStroke(1.dp, Border)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(I18n.t("font_size", appLanguage), color = Cream, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${arabicFontSize.toInt()} sp", color = Gold, fontSize = 12.sp)
                    }
                    Slider(
                        value = arabicFontSize,
                        onValueChange = onFontSizeChange,
                        valueRange = 22f..48f,
                        colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold, inactiveTrackColor = Border)
                    )

                    Spacer(Modifier.height(8.dp))
                    Text(I18n.t("select_qari", appLanguage), color = Cream, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                        items(QarisAudioService.qaris) { qari ->
                            FilterChip(
                                selected = activeQari.id == qari.id,
                                onClick = { onSelectQari(qari) },
                                label = { Text(qari.name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Gold,
                                    selectedLabelColor = Emerald,
                                    containerColor = Deep,
                                    labelColor = Cream
                                )
                            )
                        }
                    }
                }
            }
        }

        if (loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 12.dp), color = Gold)
        }

        if (error.isNotBlank()) {
            Text(error, color = Gold, fontSize = 12.sp, modifier = Modifier.padding(vertical = 10.dp))
        }

        // Ayahs List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(ayahs) { ayah ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    border = BorderStroke(1.dp, Border)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        // Top Bar of Ayah Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AYAH ${ayah.number}", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            // Copy button
                            IconButton(onClick = {
                                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Ayah ${ayah.number}", "${ayah.arabic}\n${ayah.urduTranslation}\n${ayah.englishTranslation}")
                                cm.setPrimaryClip(clip)
                                Toast.makeText(ctx, I18n.t("copy_ayah", appLanguage), Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, null, tint = Muted, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Arabic Calligraphy with golden verse circle end marker
                        Text(
                            text = "${ayah.arabic} ۝${ayah.number}",
                            color = Cream,
                            fontSize = arabicFontSize.sp,
                            lineHeight = (arabicFontSize * 1.8f).sp,
                            textAlign = TextAlign.End,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )

                        Divider(color = Border, modifier = Modifier.padding(vertical = 6.dp))

                        // Synchronized Translation based on AppLanguage
                        val translationText = when (appLanguage) {
                            AppLanguage.URDU -> ayah.urduTranslation
                            AppLanguage.ARABIC -> ayah.arabicTafseer
                            AppLanguage.HINDI -> ayah.hindiTranslation
                            AppLanguage.ENGLISH -> ayah.englishTranslation
                        }

                        Text(
                            text = translationText,
                            color = Cream,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            textAlign = if (appLanguage.isRtl) TextAlign.End else TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Secondary English Translation if primary is Urdu/Arabic
                        if (appLanguage != AppLanguage.ENGLISH && ayah.englishTranslation.isNotBlank()) {
                            Text(
                                text = ayah.englishTranslation,
                                color = Muted,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                            )
                        }

                        // Word-by-Word Breakdown Cards
                        if (ayah.words.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                I18n.t("word_by_word", appLanguage),
                                color = Gold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                items(ayah.words) { word ->
                                    Box(
                                        modifier = Modifier
                                            .background(Deep, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(word.arabic, color = Gold, fontSize = 14.sp)
                                            Text(
                                                if (appLanguage == AppLanguage.URDU) word.urdu else word.english,
                                                color = Muted,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Shan-e-Nuzul (Historical Context) Card
                        if (ayah.revelationContext.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Deep),
                                border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                            ) {
                                Column(Modifier.padding(10.dp)) {
                                    Text(
                                        I18n.t("historical_context", appLanguage),
                                        color = Gold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        ayah.revelationContext,
                                        color = Cream,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildEmbeddedVideoHtml(videoId: String): String {
    return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <meta name="referrer" content="strict-origin-when-cross-origin">
        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; background: #000000; overflow: hidden; }
            html, body { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
            .player-wrap { position: relative; width: 100%; height: 100%; }
            iframe { width: 100%; height: 100%; border: 0; display: block; }
        </style>
    </head>
    <body>
        <div class="player-wrap">
            <iframe 
                id="ytplayer"
                src="https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&playsinline=1&controls=1&modestbranding=1&rel=0&iv_load_policy=3&fs=0&enablejsapi=1&origin=https://noorehidayat.app"
                referrerpolicy="strict-origin-when-cross-origin"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen>
            </iframe>
        </div>
    </body>
    </html>
    """.trimIndent()
}

@Composable
private fun InAppVideoPlayer(
    videoId: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = true
                    allowContentAccess = true
                    userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                }
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        // Strictly block all external links and navigation so user stays inside the app!
                        return true
                    }
                }
                tag = videoId
                loadDataWithBaseURL(
                    "https://noorehidayat.app",
                    buildEmbeddedVideoHtml(videoId),
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { webView ->
            if (webView.tag != videoId) {
                webView.tag = videoId
                webView.loadDataWithBaseURL(
                    "https://noorehidayat.app",
                    buildEmbeddedVideoHtml(videoId),
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        modifier = modifier
    )

}

// --- Harameyn 24/7 Live Screen ---
@Composable
private fun LiveHarameynScreen(
    appLanguage: AppLanguage,
    isTheaterMode: Boolean,
    onToggleTheater: () -> Unit,
    selectedStream: LiveStream,
    onSelectStream: (LiveStream) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Emerald)
    ) {
        // Player Section (Embedded In-App Player with zero external links)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTheaterMode) 380.dp else 220.dp)
                .background(Color.Black)
        ) {
            InAppVideoPlayer(
                videoId = selectedStream.youtubeId,
                modifier = Modifier.fillMaxSize()
            )

            // Theater Mode Toggle
            IconButton(
                onClick = onToggleTheater,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    if (isTheaterMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = "Theater Mode",
                    tint = Gold
                )
            }
        }


        if (!isTheaterMode) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    selectedStream.title,
                    color = Cream,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    selectedStream.description,
                    color = Muted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Text(
                    "ALL 24/7 LIVE STREAMS",
                    color = Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp)
                ) {
                    items(IslamicContentData.liveStreams) { stream ->
                        val isSelected = stream.id == selectedStream.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectStream(stream) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Deep else Panel),
                            border = BorderStroke(1.dp, if (isSelected) Gold else Border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LiveTv, null, tint = if (isSelected) Gold else Muted)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(stream.title, color = Cream, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(stream.location, color = Muted, fontSize = 11.sp)
                                }
                                if (isSelected) {
                                    Text("PLAYING", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Islamic Lectures Screen ---
@Composable
private fun IslamicLecturesScreen(
    appLanguage: AppLanguage,
    isTheaterMode: Boolean,
    onToggleTheater: () -> Unit,
    selectedLecture: LectureVideo,
    onSelectLecture: (LectureVideo) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All Topics") }
    var selectedScholar by remember { mutableStateOf("All Scholars") }

    val filtered = remember(selectedCategory, selectedScholar) {
        IslamicContentData.curatedLectures.filter {
            (selectedCategory == "All Topics" || it.category == selectedCategory) &&
            (selectedScholar == "All Scholars" || it.speaker == selectedScholar)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Emerald)
    ) {
        // Video Player
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTheaterMode) 380.dp else 220.dp)
                .background(Color.Black)
        ) {
            InAppVideoPlayer(
                videoId = selectedLecture.youtubeId,
                modifier = Modifier.fillMaxSize()
            )



            IconButton(
                onClick = onToggleTheater,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(if (isTheaterMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, null, tint = Gold)
            }
        }

        if (!isTheaterMode) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(selectedLecture.title, color = Cream, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${selectedLecture.speaker} · ${selectedLecture.category} · ${selectedLecture.duration}", color = Gold, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))

                // Topic Categories
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(IslamicContentData.lectureCategories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold,
                                selectedLabelColor = Emerald,
                                containerColor = Panel,
                                labelColor = Cream
                            )
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Scholars Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(IslamicContentData.scholars) { scholar ->
                        FilterChip(
                            selected = selectedScholar == scholar,
                            onClick = { selectedScholar = scholar },
                            label = { Text(scholar, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold,
                                selectedLabelColor = Emerald,
                                containerColor = Panel,
                                labelColor = Cream
                            )
                        )
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filtered) { lecture ->
                        val isCurrent = lecture.id == selectedLecture.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectLecture(lecture) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isCurrent) Deep else Panel),
                            border = BorderStroke(1.dp, if (isCurrent) Gold else Border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayCircle, null, tint = Gold, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(lecture.title, color = Cream, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${lecture.speaker} · ${lecture.duration}", color = Muted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Daily Duas Screen ---
@Composable
private fun DailyDuasScreen(appLanguage: AppLanguage) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text(I18n.t("duas", appLanguage).uppercase(), color = Gold, fontSize = 11.sp, letterSpacing = 2.sp)
        Text(
            if (appLanguage == AppLanguage.URDU) "مسنون روزمرہ دعائیں اور اذکار" else "Daily Prayers & Supplications",
            color = Cream,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(IslamicContentData.dailyDuas) { dua ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    border = BorderStroke(1.dp, Border)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            if (appLanguage == AppLanguage.URDU) dua.titleUrdu else dua.title,
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            dua.arabic,
                            color = Cream,
                            fontSize = 22.sp,
                            lineHeight = 36.sp,
                            textAlign = TextAlign.End,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(dua.transliteration, color = Muted, fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (appLanguage == AppLanguage.URDU) dua.urdu else dua.english,
                            color = Cream,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("Benefit: ${dua.benefit}", color = Gold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// --- Islamic Quiz Screen (20 Questions) ---
@Composable
private fun IslamicQuizScreen(appLanguage: AppLanguage) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    val questions = IslamicContentData.quiz20Questions

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text(I18n.t("quiz", appLanguage).uppercase(), color = Gold, fontSize = 11.sp, letterSpacing = 2.sp)
        Text(
            if (appLanguage == AppLanguage.URDU) "اسلامی معلومات کا کوئز" else "20-Question Islamic Quiz",
            color = Cream,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        if (currentIndex >= questions.size) {
            // Completion Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Panel),
                border = BorderStroke(1.dp, Gold)
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 48.sp)
                    Text("Quiz Completed!", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Your Score: $score / ${questions.size}", color = Cream, fontSize = 18.sp, modifier = Modifier.padding(vertical = 12.dp))
                    Button(
                        onClick = {
                            currentIndex = 0
                            selectedAnswer = -1
                            score = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Emerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Play Again", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            val q = questions[currentIndex]
            Text("Question ${currentIndex + 1} of ${questions.size} · Score: $score", color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Panel),
                border = BorderStroke(1.dp, Border)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(q.prompt, color = Cream, fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
                    Spacer(Modifier.height(16.dp))

                    q.options.forEachIndexed { idx, option ->
                        val isChosen = selectedAnswer == idx
                        val isCorrect = idx == q.correctIndex
                        val btnColor = when {
                            selectedAnswer >= 0 && isCorrect -> Color(0xFF10B981)
                            selectedAnswer >= 0 && isChosen -> Color(0xFFEF4444)
                            else -> Deep
                        }

                        Button(
                            onClick = {
                                if (selectedAnswer < 0) {
                                    selectedAnswer = idx
                                    if (idx == q.correctIndex) score++
                                }
                            },
                            enabled = selectedAnswer < 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = btnColor,
                                contentColor = Cream,
                                disabledContainerColor = btnColor,
                                disabledContentColor = Cream
                            )
                        ) {
                            Text(option, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                        }
                    }

                    if (selectedAnswer >= 0) {
                        Spacer(Modifier.height(8.dp))
                        Text(q.explanation, color = Gold, fontSize = 12.sp, lineHeight = 18.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                currentIndex++
                                selectedAnswer = -1
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Emerald),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (currentIndex == questions.lastIndex) "Finish Quiz" else "Next Question", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- More Hub Screen ---
@Composable
private fun MoreHubScreen(
    appLanguage: AppLanguage,
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.height(6.dp))
            Text(I18n.t("more", appLanguage).uppercase(), color = Gold, fontSize = 11.sp, letterSpacing = 2.sp)
            Text(
                if (appLanguage == AppLanguage.URDU) "تمام خصوصیات اور سروسز" else "More Features & Services",
                color = Cream,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )
        }

        val items = listOf(
            Triple(I18n.t("tasbeeh", appLanguage), "3D Rotating Beads, Haptics & Masnoon Azkar", "Tasbeeh"),
            Triple(I18n.t("hisn_al_muslim", appLanguage), "150+ Authentic Supplications & Virtues", "Hisn al-Muslim"),
            Triple(I18n.t("hadith_library", appLanguage), "Bukhari, Muslim, 40 Hadith & Riyad as-Salihin", "Hadith"),
            Triple(I18n.t("khatam_planner", appLanguage), "30-Day Goal, Prayer Targets & 30 Juz Tracker", "Khatam Planner"),
            Triple(I18n.t("ramadan_mode", appLanguage), "Sehar/Iftar Countdown, Daily Duas & Tarawih", "Ramadan Companion"),
            Triple(I18n.t("card_generator", appLanguage), "1-Click Luxury Ayah & Hadith Social Cards", "Social Card"),
            Triple(I18n.t("hijri_calendar", appLanguage), "Estimated Hijri date, moon phases & sacred dates", "Hijri Calendar"),
            Triple(I18n.t("zakat_calculator", appLanguage), "Zakat calculator with user-entered current metal rates", "Zakat"),
            Triple(I18n.t("live_harameyn", appLanguage), "24/7 Makkah & Madinah In-App Live Streams", "Live"),
            Triple(I18n.t("lectures", appLanguage), "Trending Topics & Renowned Scholars", "Lectures"),
            Triple(I18n.t("duas", appLanguage), "Morning, Evening & Daily Masnoon Duas", "Duas"),
            Triple(I18n.t("quiz", appLanguage), "20-Question Authentic Islamic Knowledge Challenge", "Quiz"),
            Triple(I18n.t("qibla", appLanguage), "Sensor Compass & Direction to Kaaba", "Qibla"),
            Triple(I18n.t("prayer_times", appLanguage), "Astronomical Timings & 30s Adhan Alarm", "Prayer Times")
        )

        items(items) { (title, subtitle, route) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(route) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Panel),
                border = BorderStroke(1.dp, Border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(title, color = Cream, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(subtitle, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = Gold)
                }
            }
        }
    }
}
