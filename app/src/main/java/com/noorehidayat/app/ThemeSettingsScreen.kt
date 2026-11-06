package com.noorehidayat.app

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    currentLanguage: AppLanguage,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = LocalThemeManager.current
    val activeColors = LocalAppTheme.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            I18n.t("theme_settings", currentLanguage),
                            color = activeColors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            when (currentLanguage) {
                                AppLanguage.URDU -> "12 پریمیم تھیمز، لائیو پیش منظر اور کسٹم اسٹوڈیو"
                                AppLanguage.ARABIC -> "١٢ سمة راقية، معاينة حية واستوديو مخصص"
                                AppLanguage.HINDI -> "12 प्रीमियम थीम, लाइव पूर्वावलोकन और कस्टम स्टूडियो"
                                AppLanguage.ENGLISH -> "12 Premium Themes, Live Previews & Color Studio"
                            },
                            color = activeColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = activeColors.textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset All", tint = activeColors.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = activeColors.background)
            )
        },
        containerColor = activeColors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row: Gallery, Custom Studio, Smart Appearance
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = activeColors.surfaceVariant,
                contentColor = activeColors.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = activeColors.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.URDU -> "تھیم گیلری"
                                AppLanguage.ARABIC -> "معرض السمات"
                                AppLanguage.HINDI -> "थीम गैलरी"
                                AppLanguage.ENGLISH -> "Theme Gallery"
                            },
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (selectedTabIndex == 0) activeColors.primary else activeColors.textSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.URDU -> "کلر اسٹوڈیو"
                                AppLanguage.ARABIC -> "استوديو الألوان"
                                AppLanguage.HINDI -> "कलर स्टूडियो"
                                AppLanguage.ENGLISH -> "Color Studio"
                            },
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (selectedTabIndex == 1) activeColors.primary else activeColors.textSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.URDU -> "ظاہری ترتیبات"
                                AppLanguage.ARABIC -> "المظهر والجدول"
                                AppLanguage.HINDI -> "दिखावट और समय"
                                AppLanguage.ENGLISH -> "Appearance"
                            },
                            fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (selectedTabIndex == 2) activeColors.primary else activeColors.textSecondary
                        )
                    }
                )
            }

            when (selectedTabIndex) {
                0 -> ThemeGalleryTab(currentLanguage, themeManager, activeColors)
                1 -> CustomColorStudioTab(currentLanguage, themeManager, activeColors)
                2 -> AppearanceSettingsTab(currentLanguage, themeManager, activeColors)
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    if (currentLanguage == AppLanguage.URDU) "ترتیبات ری سیٹ کریں؟" else "Reset All Appearance Settings?",
                    color = activeColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (currentLanguage == AppLanguage.URDU)
                        "کیا آپ تمام تھیمز، شیڈول اور ظاہری ترتیبات کو اصل حالت پر بحال کرنا چاہتے ہیں؟"
                    else
                        "This will restore the default Pure Black AMOLED theme, cancel any schedules, and reset custom colors.",
                    color = activeColors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        themeManager.resetAllSettings()
                        showResetDialog = false
                        Toast.makeText(context, "Appearance settings reset to default", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = activeColors.primary)
                ) {
                    Text("Reset", color = activeColors.background, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = activeColors.textSecondary)
                }
            },
            containerColor = activeColors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// -------------------------------------------------------------
// TAB 1: THEME GALLERY WITH MINIATURE UI PREVIEWS
// -------------------------------------------------------------
@Composable
private fun ThemeGalleryTab(
    currentLanguage: AppLanguage,
    themeManager: ThemeManager,
    activeColors: AppThemeColors
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = activeColors.surface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, activeColors.border)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Palette, null, tint = activeColors.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (currentLanguage == AppLanguage.URDU) "لائیو تھیم سوئچر" else "Instant Live Theme Switching",
                            color = activeColors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            if (currentLanguage == AppLanguage.URDU) "کسی بھی تھیم کو منتخب کریں، پوری ایپ فوراً اپ ڈیٹ ہو جائے گی"
                            else "Tap any theme below to apply it instantly across all screens without restarting.",
                            color = activeColors.textSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }

        items(ThemeCatalog.allThemes) { theme ->
            val isSelected = themeManager.currentThemeId == theme.id

            ThemePreviewCard(
                theme = theme,
                isSelected = isSelected,
                currentLanguage = currentLanguage,
                activeColors = activeColors,
                onSelect = {
                    themeManager.selectTheme(theme.id)
                    Toast.makeText(context, "${theme.name} Applied", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun ThemePreviewCard(
    theme: ThemeDefinition,
    isSelected: Boolean,
    currentLanguage: AppLanguage,
    activeColors: AppThemeColors,
    onSelect: () -> Unit
) {
    val colors = theme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = activeColors.surface),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) activeColors.primary else activeColors.border
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: Name & Selection Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (currentLanguage == AppLanguage.URDU) theme.nameUrdu else theme.name,
                        color = activeColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (!theme.isDark) {
                        Spacer(Modifier.width(6.dp))
                        Badge(containerColor = colors.surfaceVariant) {
                            Text("LIGHT", color = colors.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isSelected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(activeColors.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, null, tint = activeColors.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Active", color = activeColors.primary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Text(
                theme.description,
                color = activeColors.textSecondary,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // --- MINIATURE SAMPLE OF THE APP INTERFACE ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.background)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mini Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(colors.primary, CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text("Noor-e-Hidayat", color = colors.textPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("AR", color = colors.primary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }

                    // Mini Ayah / Content Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(0.5.dp, colors.border)
                    ) {
                        Column(Modifier.padding(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SURAH 1 · AYAH 1", color = colors.primary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                Text("۝1", color = colors.primary, fontSize = 7.sp)
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                color = colors.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "In the name of Allah, the Entirely Merciful",
                                color = colors.textSecondary,
                                fontSize = 7.sp
                            )
                        }
                    }

                    // Mini Bottom Nav
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.width(20.dp).height(4.dp).background(colors.primary, CircleShape))
                        Box(modifier = Modifier.width(14.dp).height(4.dp).background(colors.textSecondary.copy(alpha = 0.4f), CircleShape))
                        Box(modifier = Modifier.width(14.dp).height(4.dp).background(colors.textSecondary.copy(alpha = 0.4f), CircleShape))
                        Box(modifier = Modifier.width(14.dp).height(4.dp).background(colors.textSecondary.copy(alpha = 0.4f), CircleShape))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Color Swatches & Apply Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 5 Swatch circles
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ColorSwatchCircle(colors.background, "BG")
                    ColorSwatchCircle(colors.surface, "Card")
                    ColorSwatchCircle(colors.primary, "Primary")
                    ColorSwatchCircle(colors.accent, "Accent")
                    ColorSwatchCircle(colors.textPrimary, "Text")
                }

                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) activeColors.primary else activeColors.surfaceVariant,
                        contentColor = if (isSelected) activeColors.background else activeColors.textPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (isSelected) "Applied" else "Apply Theme",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatchCircle(color: Color, label: String) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
    )
}

// -------------------------------------------------------------
// TAB 2: CUSTOM COLOR STUDIO
// -------------------------------------------------------------
@Composable
private fun CustomColorStudioTab(
    currentLanguage: AppLanguage,
    themeManager: ThemeManager,
    activeColors: AppThemeColors
) {
    val context = LocalContext.current

    val initial = remember(themeManager.customThemeColors, activeColors) {
        themeManager.customThemeColors ?: activeColors
    }

    var customBg by remember { mutableStateOf(initial.background) }
    var customSurface by remember { mutableStateOf(initial.surface) }
    var customSurfaceVar by remember { mutableStateOf(initial.surfaceVariant) }
    var customPrimary by remember { mutableStateOf(initial.primary) }
    var customText by remember { mutableStateOf(initial.textPrimary) }
    var customTextSec by remember { mutableStateOf(initial.textSecondary) }
    var customBorder by remember { mutableStateOf(initial.border) }
    var customAccent by remember { mutableStateOf(initial.accent) }

    val currentPreview = remember(customBg, customSurface, customSurfaceVar, customPrimary, customText, customTextSec, customBorder, customAccent) {
        AppThemeColors(
            background = customBg,
            surface = customSurface,
            surfaceVariant = customSurfaceVar,
            primary = customPrimary,
            textPrimary = customText,
            textSecondary = customTextSec,
            border = customBorder,
            accent = customAccent,
            navBar = customBg,
            isDark = true
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                if (currentLanguage == AppLanguage.URDU) "اپنا کسٹم تھیم خود تیار کریں:" else "Create Your Own Custom Theme:",
                color = activeColors.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Live Preview of the Custom Theme
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = activeColors.surface),
                border = BorderStroke(1.dp, activeColors.border)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("LIVE STUDIO PREVIEW", color = activeColors.primary, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(currentPreview.background)
                            .border(1.dp, currentPreview.border, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(currentPreview.surfaceVariant, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Noor-e-Hidayat", color = currentPreview.textPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Box(modifier = Modifier.size(10.dp).background(currentPreview.accent, CircleShape))
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = currentPreview.surface),
                                border = BorderStroke(1.dp, currentPreview.border)
                            ) {
                                Column(Modifier.padding(8.dp)) {
                                    Text("CUSTOM PALETTE TEST", color = currentPreview.primary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", color = currentPreview.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                                    Text("Holy Quran Reader & Islamic Companion", color = currentPreview.textSecondary, fontSize = 8.sp)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(currentPreview.surfaceVariant, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 12.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(modifier = Modifier.width(22.dp).height(5.dp).background(currentPreview.primary, CircleShape))
                                Box(modifier = Modifier.width(16.dp).height(5.dp).background(currentPreview.textSecondary.copy(alpha = 0.5f), CircleShape))
                                Box(modifier = Modifier.width(16.dp).height(5.dp).background(currentPreview.textSecondary.copy(alpha = 0.5f), CircleShape))
                            }
                        }
                    }
                }
            }
        }

        // Color Customizers for each slot
        item {
            ColorCustomizerField(
                title = "1. Background Color",
                currentColor = customBg,
                presets = listOf(Color(0xFF000000), Color(0xFF062319), Color(0xFF0A1120), Color(0xFF14101D), Color(0xFF101012), Color(0xFF18130E)),
                onColorChange = { customBg = it }
            )
        }

        item {
            ColorCustomizerField(
                title = "2. Card / Surface Color",
                currentColor = customSurface,
                presets = listOf(Color(0xFF161616), Color(0xFF114230), Color(0xFF192A4C), Color(0xFF2B233D), Color(0xFF25252B), Color(0xFF342A20)),
                onColorChange = { customSurface = it }
            )
        }

        item {
            ColorCustomizerField(
                title = "3. Primary & Heading Color",
                currentColor = customPrimary,
                presets = listOf(Color(0xFFFFD700), Color(0xFFE7C879), Color(0xFF60A5FA), Color(0xFFC084FC), Color(0xFFE2A96D), Color(0xFF38BDF8)),
                onColorChange = { customPrimary = it }
            )
        }

        item {
            ColorCustomizerField(
                title = "4. Main Text & Numbers Color",
                currentColor = customText,
                presets = listOf(Color(0xFFFFDF73), Color(0xFFF4F9F5), Color(0xFFF0F5FF), Color(0xFFF6F2FD), Color(0xFFF4F4F6), Color(0xFFFFFFFF)),
                onColorChange = { customText = it }
            )
        }

        item {
            ColorCustomizerField(
                title = "5. Subtitle & Secondary Text",
                currentColor = customTextSec,
                presets = listOf(Color(0xFFD4B872), Color(0xFFB2D4C3), Color(0xFFA5BBE3), Color(0xFFC9BBDC), Color(0xFFA1A1AA), Color(0xFFD5BA9F)),
                onColorChange = { customTextSec = it }
            )
        }

        item {
            ColorCustomizerField(
                title = "6. Accent / Highlight Color",
                currentColor = customAccent,
                presets = listOf(Color(0xFFFFC107), Color(0xFF10B981), Color(0xFF93C5FD), Color(0xFFE879F9), Color(0xFFF59E0B), Color(0xFF2DD4BF)),
                onColorChange = { customAccent = it }
            )
        }

        // Action Buttons: Save Custom Theme & Reset
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val def = ThemeCatalog.PureBlackAmoled.colors
                        customBg = def.background
                        customSurface = def.surface
                        customSurfaceVar = def.surfaceVariant
                        customPrimary = def.primary
                        customText = def.textPrimary
                        customTextSec = def.textSecondary
                        customBorder = def.border
                        customAccent = def.accent
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = activeColors.textSecondary),
                    border = BorderStroke(1.dp, activeColors.border),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reset Studio", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        themeManager.saveCustomTheme(currentPreview)
                        Toast.makeText(context, "Custom Theme Saved & Applied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = activeColors.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, null, tint = activeColors.background, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Save & Apply", color = activeColors.background, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ColorCustomizerField(
    title: String,
    currentColor: Color,
    presets: List<Color>,
    onColorChange: (Color) -> Unit
) {
    val activeColors = LocalAppTheme.current
    var hexInput by remember(currentColor) {
        val argb = currentColor.toArgb()
        mutableStateOf(String.format("#%06X", (0xFFFFFF and argb)))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = activeColors.surface),
        border = BorderStroke(1.dp, activeColors.border)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = activeColors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(currentColor)
                        .border(1.5.dp, activeColors.primary, CircleShape)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Preset Swatches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                presets.forEach { preset ->
                    val isChosen = preset == currentColor
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(preset)
                            .border(
                                if (isChosen) 2.5.dp else 1.dp,
                                if (isChosen) activeColors.primary else Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                            .clickable {
                                onColorChange(preset)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChosen) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Hex Input Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        hexInput = input
                        val clean = input.removePrefix("#").trim()
                        if (clean.length == 6) {
                            try {
                                val parsed = Color(android.graphics.Color.parseColor("#$clean"))
                                onColorChange(parsed)
                            } catch (_: Exception) {}
                        }
                    },
                    label = { Text("HEX Color Code", color = activeColors.textSecondary, fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = activeColors.textPrimary,
                        unfocusedTextColor = activeColors.textPrimary,
                        focusedBorderColor = activeColors.primary,
                        unfocusedBorderColor = activeColors.border
                    )
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: SMART APPEARANCE SETTINGS & SCHEDULE
// -------------------------------------------------------------
@Composable
private fun AppearanceSettingsTab(
    currentLanguage: AppLanguage,
    themeManager: ThemeManager,
    activeColors: AppThemeColors
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                if (currentLanguage == AppLanguage.URDU) "اسمارٹ ظاہری موڈ:" else "Smart Appearance Modes:",
                color = activeColors.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Appearance Mode Options
        items(AppearanceMode.values()) { mode ->
            val isSelected = themeManager.appearanceMode == mode

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        themeManager.setMode(mode)
                        Toast.makeText(context, "Mode: ${mode.label}", Toast.LENGTH_SHORT).show()
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) activeColors.surfaceVariant else activeColors.surface),
                border = BorderStroke(1.dp, if (isSelected) activeColors.primary else activeColors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { themeManager.setMode(mode) },
                        colors = RadioButtonDefaults.colors(selectedColor = activeColors.primary)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (currentLanguage == AppLanguage.URDU) mode.labelUrdu else mode.label,
                            color = activeColors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            when (mode) {
                                AppearanceMode.SYSTEM -> "Syncs with Android system dark/light theme setting"
                                AppearanceMode.DARK -> "Enforces dark comfort palette across the entire app"
                                AppearanceMode.LIGHT -> "Clean, comfortable light theme for bright environments"
                                AppearanceMode.AUTO_SCHEDULE -> "Automatically switches between Day and Night themes"
                                AppearanceMode.MANUAL -> "Applies your selected theme from the gallery directly"
                            },
                            color = activeColors.textSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }

        // Auto Day / Night Schedule Settings (when AUTO_SCHEDULE is active)
        if (themeManager.appearanceMode == AppearanceMode.AUTO_SCHEDULE) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = activeColors.surface),
                    border = BorderStroke(1.dp, activeColors.primary.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Day & Night Schedule Hours", color = activeColors.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Day Theme Starts", color = activeColors.textPrimary, fontSize = 12.sp)
                                Text("${themeManager.dayStartHour}:00 AM", color = activeColors.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Row {
                                IconButton(onClick = {
                                    val newH = (themeManager.dayStartHour - 1).coerceAtLeast(4)
                                    themeManager.updateSchedule(themeManager.dayThemeId, themeManager.nightThemeId, newH, themeManager.nightStartHour)
                                }) {
                                    Icon(Icons.Default.Remove, null, tint = activeColors.primary)
                                }
                                IconButton(onClick = {
                                    val newH = (themeManager.dayStartHour + 1).coerceAtMost(11)
                                    themeManager.updateSchedule(themeManager.dayThemeId, themeManager.nightThemeId, newH, themeManager.nightStartHour)
                                }) {
                                    Icon(Icons.Default.Add, null, tint = activeColors.primary)
                                }
                            }
                        }

                        Divider(color = activeColors.border, modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Night Theme Starts", color = activeColors.textPrimary, fontSize = 12.sp)
                                Text("${themeManager.nightStartHour}:00 PM", color = activeColors.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Row {
                                IconButton(onClick = {
                                    val newH = (themeManager.nightStartHour - 1).coerceAtLeast(16)
                                    themeManager.updateSchedule(themeManager.dayThemeId, themeManager.nightThemeId, themeManager.dayStartHour, newH)
                                }) {
                                    Icon(Icons.Default.Remove, null, tint = activeColors.primary)
                                }
                                IconButton(onClick = {
                                    val newH = (themeManager.nightStartHour + 1).coerceAtMost(22)
                                    themeManager.updateSchedule(themeManager.dayThemeId, themeManager.nightThemeId, themeManager.dayStartHour, newH)
                                }) {
                                    Icon(Icons.Default.Add, null, tint = activeColors.primary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Eye Comfort & Warmth Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = activeColors.surface),
                border = BorderStroke(1.dp, activeColors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.RemoveRedEye, null, tint = activeColors.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                if (currentLanguage == AppLanguage.URDU) "آرامِ چشم (Eye Comfort Warmth)" else "Eye Comfort & Reading Warmth",
                                color = activeColors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Applies gentle warm tones to eliminate harsh glare during Quran recitation",
                                color = activeColors.textSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Switch(
                        checked = themeManager.eyeComfortEnabled,
                        onCheckedChange = { themeManager.toggleEyeComfort(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = activeColors.primary,
                            checkedTrackColor = activeColors.surfaceVariant
                        )
                    )
                }
            }
        }

        // Independent Arabic Font Size Slider with Live Preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = activeColors.surface),
                border = BorderStroke(1.dp, activeColors.border)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            I18n.t("font_size", currentLanguage),
                            color = activeColors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            "${themeManager.arabicFontSize.toInt()} sp",
                            color = activeColors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Slider(
                        value = themeManager.arabicFontSize,
                        onValueChange = { themeManager.updateArabicFontSize(it) },
                        valueRange = 22f..48f,
                        colors = SliderDefaults.colors(
                            thumbColor = activeColors.primary,
                            activeTrackColor = activeColors.primary,
                            inactiveTrackColor = activeColors.surfaceVariant
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    // Live Arabic text sample
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(activeColors.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝١",
                            color = activeColors.textPrimary,
                            fontSize = themeManager.arabicFontSize.sp,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
