package com.noorehidayat.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OfflineQuranDownloadScreen(
    appLanguage: AppLanguage,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isUrdu = appLanguage == AppLanguage.URDU
    var completed by remember { mutableIntStateOf(QuranDataService.cachedSurahCount(context)) }
    var downloading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var finished by remember { mutableStateOf(completed == 114) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isUrdu) "قرآن آف لائن محفوظ کریں" else "Offline Quran",
                        color = Cream,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Emerald)
            )
        },
        containerColor = Emerald
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Panel),
                border = BorderStroke(1.dp, Border)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        if (isUrdu) "مکمل قرآن ڈاؤن لوڈ" else "Download all 114 Surahs",
                        color = Gold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (isUrdu)
                            "یہ سہولت تمام سورتوں کا عربی متن اور مکمل اردو و انگریزی آیت بہ آیت ترجمہ فون میں محفوظ کرتی ہے۔ ڈاؤن لوڈ مکمل ہونے کے بعد یہ مواد انٹرنیٹ کے بغیر دستیاب ہوگا۔"
                        else
                            "This saves the Arabic text and complete Urdu and English verse translations for all 114 Surahs on this device. Once the download finishes, the cached content can be read without an internet connection.",
                        color = Cream,
                        fontSize = 13.sp,
                        lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (isUrdu) "محفوظ شدہ سورتیں: $completed / 114" else "Surahs cached: $completed / 114",
                        color = Cream,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    LinearProgressIndicator(
                        progress = completed / 114f,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        color = Gold,
                        trackColor = Border
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            downloading = true
                            finished = false
                            error = ""
                            completed = 0
                            scope.launch {
                                try {
                                    QuranDataService.downloadAllSurahs(context) { count ->
                                        completed = count
                                    }
                                    finished = true
                                } catch (e: Exception) {
                                    error = e.message ?: "Download failed. Check your internet connection and retry."
                                } finally {
                                    downloading = false
                                }
                            }
                        },
                        enabled = !downloading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Emerald),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (downloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Emerald
                            )
                            Spacer(Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            when {
                                downloading && isUrdu -> "ڈاؤن لوڈ جاری ہے…"
                                downloading -> "Downloading…"
                                finished && isUrdu -> "تمام 114 سورتیں محفوظ ہیں"
                                finished -> "All 114 Surahs are cached"
                                isUrdu -> "مکمل قرآن ڈاؤن لوڈ کریں"
                                else -> "Download complete Quran"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (error.isNotBlank()) {
                Text(error, color = Gold, fontSize = 12.sp, lineHeight = 18.sp)
            }

            Text(
                if (isUrdu)
                    "ماخذ: AlQuran Cloud API۔ لفظ بہ لفظ معانی اس ڈاؤن لوڈ میں شامل نہیں؛ انہیں شامل کرنے کے لیے الگ مستند اور لائسنس یافتہ ڈیٹا درکار ہے۔"
                else
                    "Source: AlQuran Cloud API. Word-by-word glosses are not included in this download; they require a separate verified and properly licensed dataset.",
                color = Muted,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }
    }
}
