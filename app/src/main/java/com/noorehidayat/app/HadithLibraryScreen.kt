package com.noorehidayat.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun HadithLibraryScreen(
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    onOpenCardGenerator: ((String, String, String) -> Unit)? = null
) {
    val ctx = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedBook by remember { mutableStateOf("All") }

    // Show only collections actually present in the verified local dataset.
    val books = remember(hadiths) {
        listOf("All") + hadiths.map { it.book }.distinct().sorted()
    }

    // No hand-written sample narrations: only source-backed records are displayed.
    val sampleHadiths = emptyList<HadithItem>()

    var hadiths by remember { mutableStateOf(sampleHadiths.filterNot { it.book == "Sahih Muslim" }) }

    LaunchedEffect(appLanguage) {
        val languageRecords = withContext(Dispatchers.IO) {
            runCatching {
                val database = IslamicContentDatabase.get(ctx)
                listOf("en", "ur", "hi").associateWith { code ->
                    database.getVerifiedHadiths(languageCode = code)
                }
            }.getOrDefault(emptyMap())
        }
        val bundledMuslimArabic = withContext(Dispatchers.IO) {
            runCatching { SahihMuslimArabicCorpus.load(ctx) }.getOrDefault(emptyList())
        }

        // Join translations by the stable source hadith ID. A missing edition stays
        // empty; the UI explicitly reports that it is unavailable rather than translating it.
        val recordsByLanguage = languageRecords.mapValues { (_, rows) ->
            rows.associateBy { "${it.collectionId}:${it.id}" }
        }
        val baseRecords = languageRecords.values.flatten()
            .distinctBy { "${it.collectionId}:${it.id}" }
        val databaseItems = baseRecords.map { record ->
            val collectionTitle = if (record.collectionId == "sahih_al_bukhari") {
                "Sahih al-Bukhari"
            } else {
                "Sahih Muslim"
            }
            HadithItem(
                id = record.id,
                book = collectionTitle,
                chapter = record.chapterTitle ?: record.bookTitle,
                hadithNumber = record.id,
                narrator = record.narrator.orEmpty(),
                arabic = record.arabicText,
                english = recordsByLanguage["en"]?.get("${record.collectionId}:${record.id}")?.translation.orEmpty(),
                urdu = recordsByLanguage["ur"]?.get("${record.collectionId}:${record.id}")?.translation.orEmpty(),
                topic = record.bookTitle,
                referenceUrl = "",
                hindi = recordsByLanguage["hi"]?.get("${record.collectionId}:${record.id}")?.translation.orEmpty()
            )
        }
        val databaseMuslimNumbers = baseRecords
            .filter { it.collectionId == "sahih_al_muslim" }
            .map { it.id }
            .toSet()
        hadiths = databaseItems + bundledMuslimArabic.filterNot { it.id in databaseMuslimNumbers }
    }
    val filtered = remember(searchQuery, selectedBook, hadiths) {
        hadiths.filter {
            (selectedBook == "All" || it.book == selectedBook) &&
            (searchQuery.isBlank() || it.english.contains(searchQuery, true) || it.urdu.contains(searchQuery) || it.hindi.contains(searchQuery) || it.arabic.contains(searchQuery) || it.topic.contains(searchQuery, true) || it.narrator.contains(searchQuery, true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Emerald)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text(
            I18n.t("hadith_library", appLanguage).uppercase(),
            color = Gold,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (appLanguage == AppLanguage.URDU) "منتخب احادیث مع حوالہ جات" else "Selected Hadith References",
            color = Cream,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by topic, keyword, or narrator...", color = Muted, fontSize = 13.sp) },
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

        Spacer(Modifier.height(8.dp))

        // Book Selector
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(books) { book ->
                FilterChip(
                    selected = selectedBook == book,
                    onClick = { selectedBook = book },
                    label = { Text(book, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Gold,
                        selectedLabelColor = Emerald,
                        containerColor = Panel,
                        labelColor = Cream
                    )
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Hadith List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filtered) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    border = BorderStroke(1.dp, Border)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.book, color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${item.chapter} · ${item.hadithNumber}", color = Muted, fontSize = 11.sp)
                            }
                            Row {
                                IconButton(onClick = {
                                    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipText = listOf(item.arabic, item.english, item.urdu, item.hindi, "[${item.book} - ${item.hadithNumber}]").filter { it.isNotBlank() }.joinToString("\n\n")
                                    val clip = ClipData.newPlainText(item.book, clipText)
                                    cm.setPrimaryClip(clip)
                                    Toast.makeText(ctx, "Hadith copied to clipboard", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.ContentCopy, null, tint = Muted)
                                }
                                if (onOpenCardGenerator != null) {
                                    IconButton(onClick = {
                                        onOpenCardGenerator(item.arabic, item.urdu, "${item.book} - ${item.hadithNumber}")
                                    }) {
                                        Icon(Icons.Default.Share, null, tint = Gold)
                                    }
                                }
                            }
                        }

                        // Arabic original text
                        Text("Arabic · Original", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            item.arabic,
                            color = Cream,
                            fontSize = 22.sp,
                            lineHeight = 38.sp,
                            textAlign = TextAlign.End,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )

                        if (item.narrator.isNotBlank()) {
                            Text("Narrated by: ${item.narrator}", color = Gold, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            Spacer(Modifier.height(6.dp))
                        }

                        listOf(
                            Triple("English", item.english, false),
                            Triple("اردو", item.urdu, true),
                            Triple("हिन्दी", item.hindi, false)
                        ).forEach { (languageLabel, translationText, rtl) ->
                            Spacer(Modifier.height(5.dp))
                            Text(languageLabel, color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                translationText.ifBlank {
                                    when (languageLabel) {
                                        "اردو" -> "اس روایت کا مستند اردو ترجمہ اس وقت تصدیق شدہ ڈیٹا میں دستیاب نہیں۔"
                                        "हिन्दी" -> "इस रिवायत का सत्यापित हिंदी अनुवाद अभी उपलब्ध नहीं है।"
                                        else -> "A verified English translation is not available for this narration yet."
                                    }
                                },
                                color = if (translationText.isBlank()) Muted else Cream,
                                fontSize = 13.sp,
                                lineHeight = 21.sp,
                                textAlign = if (rtl) TextAlign.End else TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Deep, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Topic: ${item.topic}", color = Muted, fontSize = 10.sp)
                        }
                        if (item.referenceUrl.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    try {
                                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.referenceUrl)))
                                    } catch (_: Exception) {
                                        Toast.makeText(ctx, "Unable to open the source reference.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text(
                                    if (appLanguage == AppLanguage.URDU) "اصل حوالہ دیکھیں" else "Open source reference",
                                    color = Gold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
