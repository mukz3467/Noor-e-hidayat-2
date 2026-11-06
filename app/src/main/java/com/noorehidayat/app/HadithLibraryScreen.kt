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

@Composable
internal fun HadithLibraryScreen(
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    onOpenCardGenerator: ((String, String, String) -> Unit)? = null
) {
    val ctx = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedBook by remember { mutableStateOf("All") }

    val books = remember {
        listOf("All", "Sahih al-Bukhari", "Sahih Muslim", "Jami` at-Tirmidhi", "40 Hadith Nawawi", "Riyad as-Salihin")
    }

    val sampleHadiths = remember {
        listOf(
            HadithItem(
                id = "1",
                book = "Sahih al-Bukhari",
                chapter = "Book of Faith",
                hadithNumber = "Hadith 1",
                narrator = "Umar ibn al-Khattab (RA)",
                arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
                english = "Actions are judged by intentions, and every person will get what they intended.",
                urdu = "اعمال کا دارومدار نیتوں پر ہے، اور ہر انسان کو وہی ملے گا جس کی اس نے نیت کی۔",
                topic = "Intention & Sincerity",
                referenceUrl = "https://sunnah.com/bukhari:1"
            ),
            HadithItem(
                id = "2",
                book = "Sahih al-Bukhari",
                chapter = "Good Manners",
                hadithNumber = "Hadith 13",
                narrator = "Anas ibn Malik (RA)",
                arabic = "لَا يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لِأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
                english = "None of you truly believes until he loves for his brother what he loves for himself.",
                urdu = "تم میں سے کوئی شخص اس وقت تک مومن نہیں ہو سکتا جب تک وہ اپنے بھائی کے لیے وہی پسند نہ کرے جو اپنے لیے پسند کرتا ہے۔",
                topic = "Brotherhood & Love",
                referenceUrl = "https://sunnah.com/bukhari:13"
            ),
            HadithItem(
                id = "3",
                book = "Sahih Muslim",
                chapter = "Virtues & Etiquette",
                hadithNumber = "Hadith 2564c",
                narrator = "Abu Hurairah (RA)",
                arabic = "إِنَّ اللَّهَ لَا يَنْظُرُ إِلَى صُوَرِكُمْ وَأَمْوَالِكُمْ، وَلَكِنْ يَنْظُرُ إِلَى قُلُوبِكُمْ وَأَعْمَالِكُمْ",
                english = "Verily, Allah does not look at your outward appearances or your wealth, but He looks at your hearts and your deeds.",
                urdu = "بے شک اللہ تعالی تمہاری صورتوں اور تمہارے مالوں کو نہیں دیکھتا، بلکہ وہ تمہارے دلوں اور تمہارے اعمال کو دیکھتا ہے۔",
                topic = "Purity of Heart",
                referenceUrl = "https://sunnah.com/muslim:2564c"
            ),
            HadithItem(
                id = "4",
                book = "40 Hadith Nawawi",
                chapter = "Foundations of Islam",
                hadithNumber = "Hadith 2",
                narrator = "Umar ibn al-Khattab (RA)",
                arabic = "الإِحْسَانُ أَنْ تَعْبُدَ اللَّهَ كَأَنَّكَ تَرَاهُ، فَإِنْ لَمْ تَكُنْ تَرَاهُ فَإِنَّهُ يَرَاكَ",
                english = "Ihsan (spiritual excellence) is to worship Allah as though you see Him; and if you do not see Him, indeed He sees you.",
                urdu = "احسان یہ ہے کہ تم اللہ کی عبادت اس طرح کرو گویا تم اسے دیکھ رہے ہو، اور اگر تم اسے نہ دیکھ سکو تو یقین جانو کہ وہ تمہیں دیکھ رہا ہے۔",
                topic = "Ihsan & Excellence",
                referenceUrl = "https://sunnah.com/nawawi40:2"
            ),
            HadithItem(
                id = "5",
                book = "Sahih al-Bukhari",
                chapter = "Virtue of Quran",
                hadithNumber = "Hadith 5027",
                narrator = "Uthman ibn Affan (RA)",
                arabic = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
                english = "The best among you are those who learn the Quran and teach it to others.",
                urdu = "تم میں سے سب سے بہتر وہ شخص ہے جس نے قرآن سیکھا اور دوسروں کو سکھایا۔",
                topic = "Quran Learning",
                referenceUrl = "https://sunnah.com/bukhari:5027"
            ),
            HadithItem(
                id = "6",
                book = "Jami` at-Tirmidhi",
                chapter = "Righteousness & Good Deeds",
                hadithNumber = "Hadith 1956 · Hasan",
                narrator = "Abu Dharr (RA)",
                arabic = "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ",
                english = "Smiling in the face of your brother is an act of charity for you.",
                urdu = "اپنے بھائی کے سامنے مسکرانا بھی تمہارے لیے صدقہ ہے۔",
                topic = "Kindness & Charity",
                referenceUrl = "https://sunnah.com/tirmidhi:1956"
            ),
            HadithItem(
                id = "7",
                book = "Riyad as-Salihin",
                chapter = "Patience",
                hadithNumber = "Hadith 27",
                narrator = "Suhaib (RA)",
                arabic = "عَجَبًا لِأَمْرِ الْمُؤْمِنِ، إِنَّ أَمْرَهُ كُلَّهُ خَيْرٌ، وَلَيْسَ ذَاكَ لِأَحَدٍ إِلَّا لِلْمُؤْمِنِ",
                english = "How wonderful is the affair of the believer, for all his affairs are good! If good happens, he gives thanks, and if hardship strikes, he shows patience...",
                urdu = "مومن کا معاملہ بھی خوب ہے! اس کے ہر معاملے میں خیر ہے؛ اگر اسے خوشی ملے تو وہ شکر کرتا ہے، اور اگر کوئی تکلیف پہنچے تو صبر کرتا ہے...",
                topic = "Patience & Gratitude",
                referenceUrl = "https://sunnah.com/riyadussalihin/introduction/27"
            )
        )
    }

    var hadiths by remember { mutableStateOf(sampleHadiths) }

    LaunchedEffect(appLanguage) {
        val localRecords = withContext(Dispatchers.IO) {
            runCatching {
                IslamicContentDatabase.get(ctx).getVerifiedHadiths(languageCode = appLanguage.code)
            }.getOrDefault(emptyList())
        }
        if (localRecords.isNotEmpty()) {
            hadiths = localRecords.map { record ->
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
                    english = if (appLanguage == AppLanguage.URDU) "" else record.translation.orEmpty(),
                    urdu = if (appLanguage == AppLanguage.URDU) record.translation.orEmpty() else "",
                    topic = record.bookTitle,
                    referenceUrl = ""
                )
            }
        }
    }

    val filtered = remember(searchQuery, selectedBook) {
        hadiths.filter {
            (selectedBook == "All" || it.book == selectedBook) &&
            (searchQuery.isBlank() || it.english.contains(searchQuery, true) || it.urdu.contains(searchQuery) || it.arabic.contains(searchQuery) || it.topic.contains(searchQuery, true) || it.narrator.contains(searchQuery, true))
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
                                    val clip = ClipData.newPlainText(item.book, "${item.arabic}\n${item.urdu}\n${item.english}\n[${item.book} - ${item.hadithNumber}]")
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

                        // Arabic Hadith
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

                        Text("Narrated by: ${item.narrator}", color = Gold, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        Spacer(Modifier.height(6.dp))

                        Text(
                            if (appLanguage == AppLanguage.URDU) item.urdu else item.english,
                            color = Cream,
                            fontSize = 13.sp,
                            lineHeight = 21.sp
                        )

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
