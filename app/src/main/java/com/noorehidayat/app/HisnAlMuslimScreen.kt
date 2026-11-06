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
internal fun HisnAlMuslimScreen(appLanguage: AppLanguage = AppLanguage.ENGLISH) {
    val ctx = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember {
        listOf("All", "Morning & Evening", "Sleep & Wake", "Mosque & Home", "Distress & Ruqyah", "Forgiveness", "Travel & Nature")
    }

    val duas = remember {
        listOf(
            HisnDuaItem(
                id = "1",
                category = "Morning & Evening",
                categoryUrdu = "صبح و شام کے اذکار",
                title = "Morning Protection (Sayyidul Istighfar)",
                titleUrdu = "سید الاستغفار",
                arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
                transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu...",
                english = "O Allah, You are my Lord; there is no deity except You. You created me and I am Your servant, and I uphold Your covenant and promise as much as I am able...",
                urdu = "اے اللہ تو میرا رب ہے، تیرے سوا کوئی معبود نہیں، تو نے مجھے پیدا کیا اور میں تیرا بندہ ہوں اور میں اپنی طاقت کے مطابق تیرے عہد اور وعدے پر قائم ہوں...",
                reference = "Sahih al-Bukhari (6306)",
                referenceUrl = "https://sunnah.com/bukhari:6306",
                virtue = "Whoever recites this with sincere conviction and passes away that day enters Paradise.",
                recommendedCount = 1
            ),
            HisnDuaItem(
                id = "2",
                category = "Morning & Evening",
                categoryUrdu = "صبح و شام کے اذکار",
                title = "Protection Against All Sudden Harm",
                titleUrdu = "ہر ناگہانی آفت و نقصان سے حفاظت",
                arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                transliteration = "Bismillahil-lathee la yadurru ma'asmihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Samee'ul-'Aleem",
                english = "In the name of Allah, with whose name nothing on earth or in heaven can cause harm, and He is the All-Hearing, the All-Knowing.",
                urdu = "اللہ کے نام سے جس کے نام کی برکت سے زمین اور آسمان کی کوئی چیز نقصان نہیں پہنچا سکتی اور وہی خوب سننے اور جاننے والا ہے۔",
                reference = "Sunan Abi Dawud (5088), Jami` at-Tirmidhi (3388)",
                referenceUrl = "https://sunnah.com/abudawud:5088",
                virtue = "Recited 3 times in morning and evening to be protected from all unexpected harm.",
                recommendedCount = 3
            ),
            HisnDuaItem(
                id = "3",
                category = "Sleep & Wake",
                categoryUrdu = "سوتے اور جاگتے وقت",
                title = "Upon Waking Up",
                titleUrdu = "صبح بیدار ہونے پر",
                arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
                transliteration = "Alhamdu lillahil-lathee ahyana ba'da ma amatana wa ilayhin-nushoor",
                english = "All praise is for Allah who gave us life after having taken it from us, and unto Him is the resurrection.",
                urdu = "تمام تعریفیں اللہ کے لیے ہیں جس نے ہمیں مارنے کے بعد زندہ کیا اور اسی کی طرف اٹھنا ہے۔",
                reference = "Sahih al-Bukhari (6312)",
                referenceUrl = "https://sunnah.com/bukhari:6312",
                virtue = "Revives connection with Allah right at the start of the day.",
                recommendedCount = 1
            ),
            HisnDuaItem(
                id = "4",
                category = "Sleep & Wake",
                categoryUrdu = "سوتے اور جاگتے وقت",
                title = "Before Falling Asleep",
                titleUrdu = "سونے سے پہلے کی دعا",
                arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
                transliteration = "Bismika Allahumma amootu wa ahya",
                english = "In Your name, O Allah, I die and I live.",
                urdu = "اے اللہ! تیرے ہی نام کے ساتھ میں مرتا ہوں اور جیتا ہوں۔",
                reference = "Sahih al-Bukhari (6324)",
                referenceUrl = "https://sunnah.com/bukhari:6324",
                virtue = "Entrusts the soul directly to Allah's care during the night.",
                recommendedCount = 1
            ),
            HisnDuaItem(
                id = "5",
                category = "Mosque & Home",
                categoryUrdu = "مسجد اور گھر کے اذکار",
                title = "Entering the Mosque",
                titleUrdu = "مسجد میں داخل ہونے کی دعا",
                arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
                transliteration = "Allahummaf-tah lee abwaba rahmatik",
                english = "O Allah, open for me the gates of Your mercy.",
                urdu = "اے اللہ! میرے لیے اپنی رحمت کے دروازے کھول دے۔",
                reference = "Sahih Muslim (713)",
                referenceUrl = "https://sunnah.com/muslim:713",
                virtue = "Enters the house of Allah under His active mercy and angels' prayer.",
                recommendedCount = 1
            ),
            HisnDuaItem(
                id = "6",
                category = "Mosque & Home",
                categoryUrdu = "مسجد اور گھر کے اذکار",
                title = "Leaving the Home",
                titleUrdu = "گھر سے نکلتے وقت کی دعا",
                arabic = "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                transliteration = "Bismillahi, tawakkaltu 'alallahi, wa la hawla wa la quwwata illa billah",
                english = "In the name of Allah, I trust in Allah; there is no power or strength except with Allah.",
                urdu = "اللہ کے نام کے ساتھ، میں نے اللہ پر بھروسہ کیا، نیکی اور برائی سے بچنے کی کوئی طاقت نہیں مگر اللہ ہی سے۔",
                reference = "Abu Dawud (5095), at-Tirmidhi (3426)",
                referenceUrl = "https://sunnah.com/abudawud:5095",
                virtue = "It is said to him: 'You are guided, defended and protected', and Shaytan turns away from him.",
                recommendedCount = 1
            ),
            HisnDuaItem(
                id = "7",
                category = "Distress & Ruqyah",
                categoryUrdu = "پریشانی اور دم کے اذکار",
                title = "Relief from Anxiety and Sorrow",
                titleUrdu = "غم، قرض اور پریشانی سے نجات",
                arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ، وَغَلَبَةِ الرِّجَالِ",
                transliteration = "Allahumma innee a'oodhu bika minal-hammi wal-hazani, wal-'ajzi wal-kasali...",
                english = "O Allah, I seek refuge in You from anxiety and grief, helplessness and laziness, stinginess and cowardice, the burden of debt and oppression by men.",
                urdu = "اے اللہ! میں غم اور پریشانی، عاجزی اور سستی، بخل اور بزدلی، قرض کے بوجھ اور لوگوں کے غلبے سے تیری پناہ مانگتا ہوں۔",
                reference = "Sahih al-Bukhari (6369)",
                referenceUrl = "https://sunnah.com/bukhari:6369",
                virtue = "Prophet ﷺ regularly taught this to companions facing unbearable grief or heavy debt.",
                recommendedCount = 1
            ),
            HisnDuaItem(
                id = "8",
                category = "Distress & Ruqyah",
                categoryUrdu = "پریشانی اور دم کے اذکار",
                title = "Dua of Prophet Yunus (AS) in Distress",
                titleUrdu = "آیت کریمہ (حضرت یونس علیہ السلام کی دعا)",
                arabic = "لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
                transliteration = "La ilaha illa Anta subhanaka innee kuntu minaz-zalimeen",
                english = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
                urdu = "تیرے سوا کوئی معبود نہیں، تو پاک ہے، بے شک میں ہی قصوروار تھا۔",
                reference = "Surah Al-Anbiya (21:87), Jami` at-Tirmidhi (3505)",
                referenceUrl = "https://sunnah.com/tirmidhi:3505",
                virtue = "No Muslim supplicates with this for any distress except that Allah relieves him.",
                recommendedCount = 1
            ),
            HisnDuaItem(
                id = "9",
                category = "Travel & Nature",
                categoryUrdu = "سفر اور فطرت کے اذکار",
                title = "Dua for Traveling",
                titleUrdu = "سفر کی دعا",
                arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ",
                transliteration = "Subhanal-lathee sakhkhara lana hadha wa ma kunna lahu muqrineen, wa inna ila Rabbina lamunqaliboon",
                english = "Glory to Him who has subjected this to us, for we could never have accomplished this by ourselves, and to our Lord we shall return.",
                urdu = "پاک ہے وہ ذات جس نے اس (سواری) کو ہمارے قابو میں کر دیا، اور ہم اسے قابو میں لانے والے نہ تھے، اور ہم اپنے رب ہی کی طرف لوٹنے والے ہیں۔",
                reference = "Surah Az-Zukhruf (43:13-14), Sahih Muslim (1342)",
                referenceUrl = "https://sunnah.com/muslim:1342",
                virtue = "Provides spiritual safety and blessings throughout the journey.",
                recommendedCount = 1
            )
        )
    }

    val filtered = remember(searchQuery, selectedCategory) {
        duas.filter {
            (selectedCategory == "All" || it.category == selectedCategory) &&
            (searchQuery.isBlank() || it.title.contains(searchQuery, true) || it.urdu.contains(searchQuery) || it.arabic.contains(searchQuery) || it.english.contains(searchQuery, true))
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
            I18n.t("hisn_al_muslim", appLanguage).uppercase(),
            color = Gold,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (appLanguage == AppLanguage.URDU) "حصن المسلم (مستند مسنون دعائیں)" else "Fortress of the Muslim (Hisn al-Muslim)",
            color = Cream,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by topic, symptom, or situation...", color = Muted, fontSize = 13.sp) },
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

        // Category Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories) { cat ->
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

        Spacer(Modifier.height(10.dp))

        // Duas List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filtered) { dua ->
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
                                Text(
                                    if (appLanguage == AppLanguage.URDU) dua.titleUrdu else dua.title,
                                    color = Gold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${dua.category} · Repeat ${dua.recommendedCount}x",
                                    color = Muted,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(onClick = {
                                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(dua.title, "${dua.arabic}\n${dua.transliteration}\n${dua.urdu}\n${dua.reference}")
                                cm.setPrimaryClip(clip)
                                Toast.makeText(ctx, "Dua copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, null, tint = Muted)
                            }
                        }

                        // Arabic Calligraphy
                        Text(
                            dua.arabic,
                            color = Cream,
                            fontSize = 24.sp,
                            lineHeight = 40.sp,
                            textAlign = TextAlign.End,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )

                        Text(dua.transliteration, color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                        Spacer(Modifier.height(6.dp))

                        // Translation
                        Text(
                            if (appLanguage == AppLanguage.URDU) dua.urdu else dua.english,
                            color = Cream,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )

                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(dua.reference, color = Gold, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text("Benefit: ${dua.virtue}", color = Muted, fontSize = 10.sp, modifier = Modifier.weight(1f, fill = false), textAlign = TextAlign.End)
                        }
                        if (dua.referenceUrl.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    try {
                                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(dua.referenceUrl)))
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
