package com.noorehidayat.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CuratedCardContent(
    val title: String,
    val arabic: String,
    val english: String,
    val urdu: String,
    val reference: String
)

private val CURATED_CARDS = listOf(
    CuratedCardContent(
        title = "Ayat-ul-Kursi (آية الكرسي)",
        arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ",
        english = "Allah! There is no deity except Him, the Ever-Living, the Sustainer of all existence.",
        urdu = "اللہ کے سوا کوئی معبود نہیں، وہ ہمیشہ زندہ اور سب کو سنبھالنے والا ہے۔",
        reference = "Surah Al-Baqarah 2:255"
    ),
    CuratedCardContent(
        title = "Ease After Hardship (اليسر مع العسر)",
        arabic = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا • إِنَّ مَعَ الْعُسْرِ يُسْرًا",
        english = "For indeed, with hardship [will be] ease. Indeed, with hardship [will be] ease.",
        urdu = "پس بے شک تنگی کے ساتھ آسانی ہے۔ بے شک تنگی کے ساتھ آسانی ہے۔",
        reference = "Surah Ash-Sharh 94:5-6"
    ),
    CuratedCardContent(
        title = "Mercy of the Lord (رحمة ربك)",
        arabic = "وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ",
        english = "And your Lord is going to give you, and you will be satisfied.",
        urdu = "اور عنقریب تمہارا رب تمہیں اتنا دے گا کہ تم خوش ہو جاؤ گے۔",
        reference = "Surah Ad-Duha 93:5"
    ),
    CuratedCardContent(
        title = "Remembering Allah (ألا بذكر الله)",
        arabic = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
        english = "Unquestionably, by the remembrance of Allah hearts are assured.",
        urdu = "خبردار! اللہ کے ذکر ہی سے دلوں کو اطمینان حاصل ہوتا ہے۔",
        reference = "Surah Ar-Ra'd 13:28"
    ),
    CuratedCardContent(
        title = "Prophetic Smile Hadith (تبسمك صدقة)",
        arabic = "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ",
        english = "Your smiling in the face of your brother is charity for you.",
        urdu = "اپنے مسلمان بھائی کے سامنے تمہارا مسکرانا بھی تمہارے لیے صدقہ ہے۔",
        reference = "Jami` at-Tirmidhi 1956"
    ),
    CuratedCardContent(
        title = "Best of Mankind (خير الناس)",
        arabic = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
        english = "The best among you are those who learn the Quran and teach it.",
        urdu = "تم میں سے بہترین وہ ہے جو قرآن سیکھے اور سکھائے۔",
        reference = "Sahih al-Bukhari 5027"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialCardGeneratorScreen(
    currentLanguage: AppLanguage,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedTheme by remember { mutableStateOf(CardTheme.ROYAL_EMERALD) }
    var selectedPresetIndex by remember { mutableStateOf(0) }
    var customArabic by remember { mutableStateOf("") }
    var customTranslation by remember { mutableStateOf("") }
    var customReference by remember { mutableStateOf("") }
    var isCustomMode by remember { mutableStateOf(false) }

    var arabicFontSize by remember { mutableFloatStateOf(20f) }
    var showEnglish by remember { mutableStateOf(true) }
    var showUrdu by remember { mutableStateOf(true) }

    val activeArabic = if (isCustomMode && customArabic.isNotBlank()) customArabic else CURATED_CARDS[selectedPresetIndex].arabic
    val activeEnglish = if (isCustomMode && customTranslation.isNotBlank()) customTranslation else CURATED_CARDS[selectedPresetIndex].english
    val activeUrdu = if (isCustomMode && customTranslation.isNotBlank()) customTranslation else CURATED_CARDS[selectedPresetIndex].urdu
    val activeRef = if (isCustomMode && customReference.isNotBlank()) customReference else CURATED_CARDS[selectedPresetIndex].reference

    fun copyCardText() {
        val shareText = buildString {
            appendLine("﷽")
            appendLine()
            appendLine(activeArabic)
            appendLine()
            if (showUrdu) {
                appendLine(activeUrdu)
                appendLine()
            }
            if (showEnglish) {
                appendLine(activeEnglish)
                appendLine()
            }
            appendLine("— $activeRef")
            appendLine()
            appendLine("✨ Shared via Noor-e-Hidayat (نورِ ہدایت)")
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Islamic Card", shareText))
        Toast.makeText(context, "Card text copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun shareCardText() {
        val shareText = buildString {
            appendLine("﷽")
            appendLine()
            appendLine(activeArabic)
            appendLine()
            if (showUrdu) {
                appendLine(activeUrdu)
                appendLine()
            }
            if (showEnglish) {
                appendLine(activeEnglish)
                appendLine()
            }
            appendLine("— $activeRef")
            appendLine()
            appendLine("✨ Shared via Noor-e-Hidayat (نورِ ہدایت)")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Islamic Wisdom - $activeRef")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Islamic Card via"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            I18n.t("card_generator", currentLanguage),
                            color = Cream,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            when (currentLanguage) {
                                AppLanguage.URDU -> "خوبصورت سوشل میڈیا کارڈ ڈیزائنر"
                                AppLanguage.ARABIC -> "صانع بطاقات الآيات والأحاديث النبوية"
                                AppLanguage.HINDI -> "सोशल कार्ड जनरेटर"
                                AppLanguage.ENGLISH -> "1-Click Social Ayah & Hadith Card Creator"
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
                    IconButton(onClick = { shareCardText() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Gold)
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Selector Chips
            Text(
                "1. Choose Luxury Card Theme:",
                color = Gold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardTheme.values().forEach { theme ->
                    val isSelected = selectedTheme == theme
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(listOf(theme.bgStart, theme.bgEnd))
                            )
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Gold else Border,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedTheme = theme },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            when (theme) {
                                CardTheme.ROYAL_EMERALD -> "Emerald"
                                CardTheme.MIDNIGHT_BLUE -> "Midnight"
                                CardTheme.MINIMAL_DARK -> "Slate"
                                CardTheme.WARM_PARCHMENT -> "Amber"
                            },
                            color = theme.textClr,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Visual Card Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(listOf(selectedTheme.bgStart, selectedTheme.bgEnd))
                        )
                        .border(
                            2.dp,
                            selectedTheme.accentClr.copy(alpha = 0.4f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Card Header
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "﷽",
                                color = selectedTheme.accentClr,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Divider(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .width(60.dp),
                                color = selectedTheme.accentClr.copy(alpha = 0.5f),
                                thickness = 1.dp
                            )
                        }

                        // Arabic Content
                        Text(
                            activeArabic,
                            color = selectedTheme.textClr,
                            fontSize = arabicFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = (arabicFontSize * 1.5f).sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )

                        // Translations
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (showUrdu) {
                                Text(
                                    activeUrdu,
                                    color = selectedTheme.textClr.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            if (showEnglish) {
                                Text(
                                    activeEnglish,
                                    color = selectedTheme.textClr.copy(alpha = 0.75f),
                                    fontSize = 11.5.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }

                        // Footer Reference & App branding
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "— $activeRef",
                                color = selectedTheme.accentClr,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Noor-e-Hidayat • نورِ ہدایت",
                                color = selectedTheme.textClr.copy(alpha = 0.4f),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            // Quick Action Buttons (Copy & Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { copyCardText() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Deep),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Text", color = Cream, fontSize = 13.sp)
                }

                Button(
                    onClick = { shareCardText() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Emerald, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Card", color = Emerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Adjust Font Size Slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Deep),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Arabic Font Size", color = Cream, fontSize = 12.sp)
                        Text("${arabicFontSize.toInt()} sp", color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(
                        value = arabicFontSize,
                        onValueChange = { arabicFontSize = it },
                        valueRange = 16f..28f,
                        colors = SliderDefaults.colors(
                            thumbColor = Gold,
                            activeTrackColor = Gold,
                            inactiveTrackColor = Panel
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = showUrdu,
                                onCheckedChange = { showUrdu = it },
                                colors = CheckboxDefaults.colors(checkedColor = Gold, checkmarkColor = Emerald)
                            )
                            Text("Urdu", color = Cream, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = showEnglish,
                                onCheckedChange = { showEnglish = it },
                                colors = CheckboxDefaults.colors(checkedColor = Gold, checkmarkColor = Emerald)
                            )
                            Text("English", color = Cream, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Select Preset or Write Custom
            Text(
                "2. Choose Preset Ayah or Hadith:",
                color = Gold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            CURATED_CARDS.forEachIndexed { index, item ->
                val isSelected = !isCustomMode && selectedPresetIndex == index
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            isCustomMode = false
                            selectedPresetIndex = index
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Deep else Panel
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Gold else Border
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                isCustomMode = false
                                selectedPresetIndex = index
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Gold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(item.title, color = Cream, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(item.reference, color = Muted, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Custom Text Input Option
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isCustomMode = true },
                colors = CardDefaults.cardColors(
                    containerColor = if (isCustomMode) Deep else Panel
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCustomMode) Gold else Border
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isCustomMode,
                            onClick = { isCustomMode = true },
                            colors = RadioButtonDefaults.colors(selectedColor = Gold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Custom Text / Dua / Ayah", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (isCustomMode) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customArabic,
                            onValueChange = { customArabic = it },
                            label = { Text("Arabic Text", color = Muted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Cream,
                                unfocusedTextColor = Cream,
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Border
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customTranslation,
                            onValueChange = { customTranslation = it },
                            label = { Text("Translation (Urdu/English)", color = Muted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Cream,
                                unfocusedTextColor = Cream,
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Border
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customReference,
                            onValueChange = { customReference = it },
                            label = { Text("Reference / Citation", color = Muted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Cream,
                                unfocusedTextColor = Cream,
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Border
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
