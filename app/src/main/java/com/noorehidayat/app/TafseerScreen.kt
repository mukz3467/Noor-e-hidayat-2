package com.noorehidayat.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private data class Edition(val id: String, val name: String, val lang: String)
private data class TafsirAyah(val number: Int, val text: String)

private suspend fun tafsirRequest(path: String): JSONObject = withContext(Dispatchers.IO) {
    val connection = URL("https://api.alquran.cloud/v1/$path").openConnection() as HttpURLConnection
    connection.connectTimeout = 15000
    connection.readTimeout = 25000
    try {
        if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
        JSONObject(connection.inputStream.bufferedReader().use { it.readText() }).also {
            require(it.optInt("code") == 200)
        }
    } finally {
        connection.disconnect()
    }
}

@Composable
internal fun TafseerScreen(
    surah: Surah?,
    appLanguage: AppLanguage = AppLanguage.ENGLISH
) {
    val context = LocalContext.current
    val isUrdu = appLanguage == AppLanguage.URDU
    val background = LocalAppTheme.current.background
    val surface = LocalAppTheme.current.surface
    val primary = LocalAppTheme.current.primary
    val textPrimary = LocalAppTheme.current.textPrimary
    val textSecondary = LocalAppTheme.current.textSecondary

    var activeSurah by remember(surah?.number) { mutableStateOf(surah) }
    var editions by remember(activeSurah?.number) { mutableStateOf<List<Edition>>(emptyList()) }
    var selectedEdition by remember(activeSurah?.number) { mutableStateOf<Edition?>(null) }
    var ayahs by remember(activeSurah?.number) { mutableStateOf<List<TafsirAyah>>(emptyList()) }
    var catalogLoading by remember { mutableStateOf(false) }
    var tafsirLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var surahMenuExpanded by remember { mutableStateOf(false) }
    var editionMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(activeSurah?.number) {
        val current = activeSurah ?: return@LaunchedEffect
        catalogLoading = true
        error = ""
        editions = emptyList()
        selectedEdition = null
        ayahs = emptyList()
        try {
            val localEditions = withContext(Dispatchers.IO) {
                IslamicContentDatabase.get(context).getTafsirEditions()
            }
            if (localEditions.isNotEmpty()) {
                val localOptions = localEditions.map { local ->
                    Edition(
                        id = local.id,
                        name = local.scholar + " · " + local.title,
                        lang = local.language
                    )
                }
                editions = localOptions
                selectedEdition = localOptions.firstOrNull()
                return@LaunchedEffect
            }

            val response = tafsirRequest("edition/type/tafsir").getJSONArray("data")
            val available = buildList {
                for (index in 0 until response.length()) {
                    val item = response.getJSONObject(index)
                    val id = item.optString("identifier")
                    val language = item.optString("language")
                    if (
                        id.isNotBlank() &&
                        item.optString("format") == "text" &&
                        language in setOf("ur", "en", "ar")
                    ) {
                        add(
                            Edition(
                                id = id,
                                name = item.optString("englishName").ifBlank { item.optString("name") },
                                lang = language
                            )
                        )
                    }
                }
            }.sortedBy { when (it.lang) { "ur" -> 0; "en" -> 1; else -> 2 } }
            editions = available
            selectedEdition = available.firstOrNull()
            if (available.isEmpty()) {
                error = if (isUrdu) "اس وقت اردو، انگریزی یا عربی تفسیر دستیاب نہیں۔"
                else "No Urdu, English or Arabic Tafseer edition is currently listed."
            }
        } catch (_: Exception) {
            error = if (isUrdu) "تفاسیر کی فہرست نہیں کھل سکی۔ انٹرنیٹ چیک کریں۔"
            else "Tafseer catalogue unavailable. Check your internet connection."
        } finally {
            catalogLoading = false
        }
    }

    LaunchedEffect(activeSurah?.number, selectedEdition?.id) {
        val currentSurah = activeSurah ?: return@LaunchedEffect
        val edition = selectedEdition ?: return@LaunchedEffect
        tafsirLoading = true
        error = ""
        ayahs = emptyList()
        try {
            val localRows = withContext(Dispatchers.IO) {
                IslamicContentDatabase.get(context).getTafsirForSurah(currentSurah.number, edition.id)
            }
            if (localRows.isNotEmpty()) {
                ayahs = localRows.map { row -> TafsirAyah(row.ayahNumber, row.text) }
                return@LaunchedEffect
            }

            val response = tafsirRequest("surah/${currentSurah.number}/${edition.id}")
                .getJSONObject("data")
                .getJSONArray("ayahs")
            ayahs = buildList {
                for (index in 0 until response.length()) {
                    val item = response.getJSONObject(index)
                    add(TafsirAyah(item.optInt("numberInSurah"), item.optString("text")))
                }
            }
            if (ayahs.isEmpty()) {
                error = if (isUrdu) "اس سورت کے لیے تفسیر کا متن موصول نہیں ہوا۔"
                else "No Tafseer text was returned for this Surah."
            }
        } catch (_: Exception) {
            error = if (isUrdu) "یہ تفسیر لوڈ نہیں ہو سکی۔ دوسری تفسیر منتخب کریں یا دوبارہ کوشش کریں۔"
            else "Could not load this Tafseer edition. Choose another edition or retry."
        } finally {
            tafsirLoading = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text(
            if (isUrdu) "قرآن کی تفسیر" else "QURAN EXPLANATION",
            color = primary,
            fontSize = 10.sp,
            letterSpacing = 2.sp
        )
        Text(
            if (isUrdu) "تفسیر القرآن" else "Tafseer of the Quran",
            color = textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (isUrdu) "مستند مطبوعہ تفسیری ایڈیشن منتخب کریں" else "Choose a published tafseer edition",
            color = textSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 3.dp, bottom = 12.dp)
        )

        Box {
            OutlinedButton(
                onClick = { surahMenuExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = primary),
                border = BorderStroke(1.dp, textSecondary.copy(alpha = 0.35f))
            ) {
                Text(
                    activeSurah?.let { "${it.number}. ${it.english}" }
                        ?: if (isUrdu) "سورۃ منتخب کریں" else "Select Surah",
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = surahMenuExpanded,
                onDismissRequest = { surahMenuExpanded = false },
                modifier = Modifier.heightIn(max = 350.dp).background(surface)
            ) {
                QuranDataService.allSurahs.forEach { item ->
                    DropdownMenuItem(
                        text = { Text("${item.number}. ${item.english}", color = textPrimary) },
                        onClick = {
                            activeSurah = item
                            surahMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Box {
            OutlinedButton(
                onClick = { editionMenuExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = primary),
                border = BorderStroke(1.dp, textSecondary.copy(alpha = 0.35f))
            ) {
                Text(
                    selectedEdition?.let { "${it.name} · ${it.lang.uppercase()}" }
                        ?: if (isUrdu) "تفسیر منتخب کریں" else "Select Tafseer edition",
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = editionMenuExpanded,
                onDismissRequest = { editionMenuExpanded = false },
                modifier = Modifier.heightIn(max = 350.dp).background(surface)
            ) {
                editions.forEach { edition ->
                    DropdownMenuItem(
                        text = { Text("${edition.name} · ${edition.lang.uppercase()}", color = textPrimary) },
                        onClick = {
                            selectedEdition = edition
                            editionMenuExpanded = false
                        }
                    )
                }
            }
        }

        if (catalogLoading || tafsirLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                color = primary
            )
        }
        if (error.isNotBlank()) {
            Text(
                error,
                color = primary,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 12.dp)
        ) {
            items(ayahs) { ayah ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = surface),
                    border = BorderStroke(1.dp, primary.copy(alpha = 0.2f))
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            if (isUrdu) "آیت ${ayah.number}" else "AYAH ${ayah.number}",
                            color = primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            ayah.text,
                            color = textPrimary,
                            fontSize = 14.sp,
                            lineHeight = 23.sp,
                            textAlign = if (selectedEdition?.lang == "ur" || selectedEdition?.lang == "ar") TextAlign.End else TextAlign.Start,
                            modifier = Modifier.fillMaxWidth().padding(top = 7.dp)
                        )
                    }
                }
            }
        }

        Text(
            if (isUrdu)
                "تفسیر منتخب مطبوعہ ایڈیشن سے حاصل کی جاتی ہے۔ شرعی احکام کے لیے مستند اہلِ علم سے رجوع کریں۔"
            else
                "Tafseer text is from the selected published edition. Consult qualified scholarship for religious rulings.",
            color = textSecondary,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
