package com.noorehidayat.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object QuranDataService {

    private val memCache = mutableMapOf<Int, List<Ayah>>()

    val allSurahs = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "The Opening", 7, "Meccan"),
        Surah(2, "البقرة", "Al-Baqarah", "The Cow", 286, "Medinan"),
        Surah(3, "آل عمران", "Ali 'Imran", "Family of Imran", 200, "Medinan"),
        Surah(4, "النساء", "An-Nisa", "The Women", 176, "Medinan"),
        Surah(5, "المائدة", "Al-Ma'idah", "The Table Spread", 120, "Medinan"),
        Surah(6, "الأنعام", "Al-An'am", "The Cattle", 165, "Meccan"),
        Surah(7, "الأعراف", "Al-A'raf", "The Heights", 206, "Meccan"),
        Surah(8, "الأنفال", "Al-Anfal", "The Spoils of War", 75, "Medinan"),
        Surah(9, "التوبة", "At-Tawbah", "The Repentance", 129, "Medinan"),
        Surah(10, "يونس", "Yunus", "Jonah", 109, "Meccan"),
        Surah(11, "هود", "Hud", "Hud", 123, "Meccan"),
        Surah(12, "يوسف", "Yusuf", "Joseph", 111, "Meccan"),
        Surah(13, "الرعد", "Ar-Ra'd", "The Thunder", 43, "Medinan"),
        Surah(14, "إبراهيم", "Ibrahim", "Abraham", 52, "Meccan"),
        Surah(15, "الحجر", "Al-Hijr", "The Rocky Tract", 99, "Meccan"),
        Surah(16, "النحل", "An-Nahl", "The Bee", 128, "Meccan"),
        Surah(17, "الإسراء", "Al-Isra", "The Night Journey", 111, "Meccan"),
        Surah(18, "الكهف", "Al-Kahf", "The Cave", 110, "Meccan"),
        Surah(19, "مريم", "Maryam", "Mary", 98, "Meccan"),
        Surah(20, "طه", "Ta-Ha", "Ta-Ha", 135, "Meccan"),
        Surah(21, "الأنبياء", "Al-Anbiya", "The Prophets", 112, "Meccan"),
        Surah(22, "الحج", "Al-Hajj", "The Pilgrimage", 78, "Medinan"),
        Surah(23, "المؤمنون", "Al-Mu'minun", "The Believers", 118, "Meccan"),
        Surah(24, "النور", "An-Nur", "The Light", 64, "Medinan"),
        Surah(25, "الفرقان", "Al-Furqan", "The Criterion", 77, "Meccan"),
        Surah(26, "الشعراء", "Ash-Shu'ara", "The Poets", 227, "Meccan"),
        Surah(27, "النمل", "An-Naml", "The Ant", 93, "Meccan"),
        Surah(28, "القصص", "Al-Qasas", "The Stories", 88, "Meccan"),
        Surah(29, "العنكبوت", "Al-'Ankabut", "The Spider", 69, "Meccan"),
        Surah(30, "الروم", "Ar-Rum", "The Romans", 60, "Meccan"),
        Surah(31, "لقمان", "Luqman", "Luqman", 34, "Meccan"),
        Surah(32, "السجدة", "As-Sajdah", "The Prostration", 30, "Meccan"),
        Surah(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", 73, "Medinan"),
        Surah(34, "سبأ", "Saba", "Sheba", 54, "Meccan"),
        Surah(35, "فاطر", "Fatir", "Originator", 45, "Meccan"),
        Surah(36, "يس", "Ya-Sin", "Ya-Sin", 83, "Meccan"),
        Surah(37, "الصافات", "As-Saffat", "Those Who Set The Ranks", 182, "Meccan"),
        Surah(38, "ص", "Sad", "The Letter Sad", 88, "Meccan"),
        Surah(39, "الزمر", "Az-Zumar", "The Troops", 75, "Meccan"),
        Surah(40, "غافر", "Ghafir", "The Forgiver", 85, "Meccan"),
        Surah(41, "فصلت", "Fussilat", "Explained in Detail", 54, "Meccan"),
        Surah(42, "الشورى", "Ash-Shura", "The Consultation", 53, "Meccan"),
        Surah(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", 89, "Meccan"),
        Surah(44, "الدخان", "Ad-Dukhan", "The Smoke", 59, "Meccan"),
        Surah(45, "الجاثية", "Al-Jathiyah", "The Crouching", 37, "Meccan"),
        Surah(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", 35, "Meccan"),
        Surah(47, "محمد", "Muhammad", "Muhammad", 38, "Medinan"),
        Surah(48, "الفتح", "Al-Fath", "The Victory", 29, "Medinan"),
        Surah(49, "الحجرات", "Al-Hujurat", "The Rooms", 18, "Medinan"),
        Surah(50, "ق", "Qaf", "The Letter Qaf", 45, "Meccan"),
        Surah(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", 60, "Meccan"),
        Surah(52, "الطور", "At-Tur", "The Mount", 49, "Meccan"),
        Surah(53, "النجم", "An-Najm", "The Star", 62, "Meccan"),
        Surah(54, "القمر", "Al-Qamar", "The Moon", 55, "Meccan"),
        Surah(55, "الرحمن", "Ar-Rahman", "The Beneficent", 78, "Medinan"),
        Surah(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", 96, "Meccan"),
        Surah(57, "الحديد", "Al-Hadid", "The Iron", 29, "Medinan"),
        Surah(58, "المجادلة", "Al-Mujadila", "The Pleading Woman", 22, "Medinan"),
        Surah(59, "الحشر", "Al-Hashr", "The Exile", 24, "Medinan"),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "She That Is To Be Examined", 13, "Medinan"),
        Surah(61, "الصف", "As-Saff", "The Ranks", 14, "Medinan"),
        Surah(62, "الجمعة", "Al-Jumu'ah", "The Congregation, Friday", 11, "Medinan"),
        Surah(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", 11, "Medinan"),
        Surah(64, "التغابن", "At-Taghabun", "The Mutual Disillusion", 18, "Medinan"),
        Surah(65, "الطلاق", "At-Talaq", "The Divorce", 12, "Medinan"),
        Surah(66, "التحريم", "At-Tahrim", "The Prohibition", 12, "Medinan"),
        Surah(67, "الملك", "Al-Mulk", "The Sovereignty", 30, "Meccan"),
        Surah(68, "القلم", "Al-Qalam", "The Pen", 52, "Meccan"),
        Surah(69, "الحاقة", "Al-Haqqah", "The Reality", 52, "Meccan"),
        Surah(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", 44, "Meccan"),
        Surah(71, "نوح", "Nuh", "Noah", 28, "Meccan"),
        Surah(72, "الجن", "Al-Jinn", "The Jinn", 28, "Meccan"),
        Surah(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", 20, "Meccan"),
        Surah(74, "المدثر", "Al-Muddaththir", "The Cloaked One", 56, "Meccan"),
        Surah(75, "القيامة", "Al-Qiyamah", "The Resurrection", 40, "Meccan"),
        Surah(76, "الإنسان", "Al-Insan", "The Human", 31, "Medinan"),
        Surah(77, "المرسلات", "Al-Mursalat", "The Emissaries", 50, "Meccan"),
        Surah(78, "النبأ", "An-Naba", "The Tidings", 40, "Meccan"),
        Surah(79, "النازعات", "An-Nazi'at", "Those Who Drag Forth", 46, "Meccan"),
        Surah(80, "عبس", "Abasa", "He Frowned", 42, "Meccan"),
        Surah(81, "التكوير", "At-Takwir", "The Overthrowing", 29, "Meccan"),
        Surah(82, "الانفطار", "Al-Infitar", "The Cleaving", 19, "Meccan"),
        Surah(83, "المطففين", "Al-Mutaffifin", "The Defrauding", 36, "Meccan"),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "The Splitting Open", 25, "Meccan"),
        Surah(85, "البروج", "Al-Buruj", "The Mansions of the Stars", 22, "Meccan"),
        Surah(86, "الطارق", "At-Tariq", "The Morning Star", 17, "Meccan"),
        Surah(87, "الأعلى", "Al-A'la", "The Most High", 19, "Meccan"),
        Surah(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", 26, "Meccan"),
        Surah(89, "الفجر", "Al-Fajr", "The Dawn", 30, "Meccan"),
        Surah(90, "البلد", "Al-Balad", "The City", 20, "Meccan"),
        Surah(91, "الشمس", "Ash-Shams", "The Sun", 15, "Meccan"),
        Surah(92, "الليل", "Al-Layl", "The Night", 21, "Meccan"),
        Surah(93, "الضحى", "Ad-Duha", "The Morning Brightness", 11, "Meccan"),
        Surah(94, "الشرح", "Ash-Sharh", "The Relief", 8, "Meccan"),
        Surah(95, "التين", "At-Tin", "The Fig", 8, "Meccan"),
        Surah(96, "العلق", "Al-'Alaq", "The Clot", 19, "Meccan"),
        Surah(97, "القدر", "Al-Qadr", "The Night of Decree", 5, "Meccan"),
        Surah(98, "البينة", "Al-Bayyinah", "The Clear Proof", 8, "Medinan"),
        Surah(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", 8, "Medinan"),
        Surah(100, "العاديات", "Al-'Adiyat", "The Courser", 11, "Meccan"),
        Surah(101, "القارعة", "Al-Qari'ah", "The Calamity", 11, "Meccan"),
        Surah(102, "التكاثر", "At-Takathur", "The Rivalry in World Increase", 8, "Meccan"),
        Surah(103, "العصر", "Al-'Asr", "The Declining Day", 3, "Meccan"),
        Surah(104, "الهمزة", "Al-Humazah", "The Traducer", 9, "Meccan"),
        Surah(105, "الفيل", "Al-Fil", "The Elephant", 5, "Meccan"),
        Surah(106, "قريش", "Quraysh", "Quraysh", 4, "Meccan"),
        Surah(107, "الماعون", "Al-Ma'un", "The Small Kindnesses", 7, "Meccan"),
        Surah(108, "الکوثر", "Al-Kawthar", "The Abundance", 3, "Meccan"),
        Surah(109, "الكافرون", "Al-Kafirun", "The Disbelievers", 6, "Meccan"),
        Surah(110, "النصر", "An-Nasr", "The Divine Support", 3, "Medinan"),
        Surah(111, "المسد", "Al-Masad", "The Palm Fiber", 5, "Meccan"),
        Surah(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", 4, "Meccan"),
        Surah(113, "الفلق", "Al-Falaq", "The Daybreak", 5, "Meccan"),
        Surah(114, "الناس", "An-Nas", "Mankind", 6, "Meccan")
    )

    private val shanENuzulMap = mapOf(
        1 to "Revealed in Makkah early in the Prophetic mission. Known as Umm al-Kitab (Mother of the Book) and al-Sab' al-Mathani (The Seven Oft-Repeated Verses). It is the opening dialogue between servant and Creator recited in every unit of Salah.",
        2 to "Revealed in Madinah over several years. Addresses legal frameworks, faith principles, the history of Adam (AS), Bani Israel, and ends with the legendary Ayat al-Kursi (2:255).",
        36 to "Revealed in Makkah to reassure the Prophet ﷺ and validate the Divine message when Quraysh chieftains rejected resurrection. Considered the Heart of the Quran.",
        55 to "Known as 'The Bride of the Quran' ('Aroos al-Quran). Highlights divine favors and repeatedly challenges mankind and jinn: 'Which of the favors of your Lord will you deny?'",
        67 to "Revealed in Makkah. Recited before sleep to provide protection in the grave, focusing on Allah's absolute sovereignty and dominion over the cosmos.",
        108 to "Revealed when al-'As ibn Wa'il mocked the Prophet ﷺ after the death of his son al-Qasim, calling him 'Abtar' (cut off). Allah responded by granting him al-Kawthar (Infinite Abundance).",
        112 to "Revealed when the polytheists of Makkah asked the Prophet ﷺ: 'Describe your Lord's lineage to us.' Allah revealed the eternal declaration of Tawhid (Absolute Oneness).",
        113 to "Revealed alongside Surah An-Nas (al-Mu'awwidhatayn) when a spell was attempted by Labid ibn al-A'sam. Jibril (AS) descended with these two Surahs for spiritual protection.",
        114 to "Revealed for seeking refuge from the whisperings of Satan and evil instigators among mankind and jinn."
    )

    suspend fun getSurahBatch(context: Context, surahNumber: Int): List<Ayah> = withContext(Dispatchers.IO) {
        memCache[surahNumber]?.let { return@withContext it }

        val prefs = context.getSharedPreferences("noor_quran_cache", Context.MODE_PRIVATE)
        val cacheKey = "noor_ayahs_v3_$surahNumber"
        val cached = prefs.getString(cacheKey, null)
        if (!cached.isNullOrBlank()) {
            try {
                val list = parseBatchResponse(cached, surahNumber)
                if (list.isNotEmpty()) {
                    memCache[surahNumber] = list
                    return@withContext list
                }
            } catch (_: Exception) {}
        }

        // Single batch call with 5 editions
        val urlStr = "https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,en.sahih,ur.jalandhry,ar.muyassar,hi.hindi"
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 25000
        try {
            if (conn.responseCode in 200..299) {
                val raw = conn.inputStream.bufferedReader().use { it.readText() }
                val list = parseBatchResponse(raw, surahNumber)
                if (list.isNotEmpty()) {
                    prefs.edit().putString(cacheKey, raw).apply()
                    memCache[surahNumber] = list
                    return@withContext list
                }
            }
        } catch (_: Exception) {
        } finally {
            conn.disconnect()
        }

        // Fallback: If network fails, provide curated initial Ayahs so app never crashes
        val fallback = getFallbackAyahs(surahNumber)
        memCache[surahNumber] = fallback
        return@withContext fallback
    }

    private fun parseBatchResponse(raw: String, surahNumber: Int): List<Ayah> {
        val root = JSONObject(raw)
        val dataArray = root.getJSONArray("data")

        var uthmaniAyahs: List<String> = emptyList()
        var englishAyahs: List<String> = emptyList()
        var urduAyahs: List<String> = emptyList()
        var arabicTafseerAyahs: List<String> = emptyList()
        var hindiAyahs: List<String> = emptyList()

        for (i in 0 until dataArray.length()) {
            val editionObj = dataArray.getJSONObject(i)
            val identifier = editionObj.getJSONObject("edition").optString("identifier")
            val ayahs = editionObj.getJSONArray("ayahs")
            val texts = mutableListOf<String>()
            for (j in 0 until ayahs.length()) {
                texts.add(ayahs.getJSONObject(j).optString("text"))
            }

            when (identifier) {
                "quran-uthmani" -> uthmaniAyahs = texts
                "en.sahih" -> englishAyahs = texts
                "ur.jalandhry" -> urduAyahs = texts
                "ar.muyassar" -> arabicTafseerAyahs = texts
                "hi.hindi" -> hindiAyahs = texts
            }
        }

        val count = uthmaniAyahs.size
        val result = mutableListOf<Ayah>()
        val contextNote = shanENuzulMap[surahNumber] ?: ""

        for (idx in 0 until count) {
            val num = idx + 1
            val arabicText = uthmaniAyahs.getOrNull(idx) ?: ""
            val words = generateWords(arabicText)

            result.add(
                Ayah(
                    number = num,
                    arabic = arabicText,
                    englishTranslation = englishAyahs.getOrNull(idx) ?: "",
                    urduTranslation = urduAyahs.getOrNull(idx) ?: "",
                    arabicTafseer = arabicTafseerAyahs.getOrNull(idx) ?: "",
                    hindiTranslation = hindiAyahs.getOrNull(idx) ?: "",
                    words = words,
                    revelationContext = if (idx == 0) contextNote else ""
                )
            )
        }
        return result
    }

    private fun generateWords(arabic: String): List<WordMeaning> {
        val tokens = arabic.split(" ").filter { it.isNotBlank() }
        return tokens.take(4).map { token ->
            WordMeaning(
                arabic = token,
                transliteration = token,
                english = "Word breakdown",
                urdu = "لفظی مفہوم"
            )
        }
    }

    private fun getFallbackAyahs(surahNumber: Int): List<Ayah> {
        if (surahNumber == 1) {
            return listOf(
                Ayah(1, "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "شروع اللہ کا نام لے کر جو بڑا مہربان نہایت رحم والا ہے", "بدأت قراءتي باسم الله مستعينا به", "अल्लाह के नाम से जो रहमान और रहीम है।", listOf(WordMeaning("بِسْمِ", "Bismi", "In name", "نام سے"), WordMeaning("ٱللَّهِ", "Allahi", "of Allah", "اللہ کے")), shanENuzulMap[1] ?: ""),
                Ayah(2, "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds -", "سب طرح کی تعریف خدا ہی کو (سزاوار) ہے جو تمام مخلوقات کا پروردگار ہے", "الثناء الكامل والشكر الخالص لله وحده", "सब प्रशंसा अल्लाह ही के लिए है जो सारे संसार का रब है।", listOf(WordMeaning("ٱلْحَمْدُ", "Al-Hamdu", "All praise", "سب تعریف"), WordMeaning("لِلَّهِ", "Lillahi", "to Allah", "اللہ کے لیے")), ""),
                Ayah(3, "ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", "بڑا مہربان نہایت رحم والا", "الرحمن الذي وسعت رحمته كل شيء", "बड़ा मेहरबान, निहायत रहम वाला।", listOf(WordMeaning("ٱلرَّحْمَـٰنِ", "Ar-Rahmani", "The Beneficent", "نہایت مہربان")), ""),
                Ayah(4, "مَـٰلِكِ يَوْمِ ٱلدِّينِ", "Sovereign of the Day of Recompense.", "انصاف کے دن کا حاکم", "المتصرف وحده في يوم الحساب والجزاء", "बदले के दिन का मालिक।", listOf(WordMeaning("مَـٰلِكِ", "Maliki", "Master", "مالک")), ""),
                Ayah(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", "(اے پروردگار) ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں", "نخصك وحدك بالعبادة ونستعين بك وحدك", "हम तेरी ही इबादत करते हैं और तुझ ही से मदद चाहते हैं।", listOf(WordMeaning("إِيَّاكَ", "Iyyaka", "You alone", "تجھ ہی کو")), ""),
                Ayah(6, "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ", "Guide us to the straight path -", "ہم کو سیدھے رستے چلا", "وفقنا وأرشدنا إلى الطريق القويم", "हमें सीधे रास्ते पर चला।", listOf(WordMeaning("ٱهْدِنَا", "Ihdina", "Guide us", "ہدایت دے")), ""),
                Ayah(7, "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", "ان لوگوں کے رستے جن پر تو اپنا فضل وکرم کرتا رہا نہ ان کے جن پر غصہ ہوتا رہا اور نہ گمراہوں کے", "طريق الذين تفضلت عليهم بالهداية", "उनका रास्ता जिन पर तूने इनाम किया, न उनका जिन पर क्रोध हुआ और न बहके हुओं का।", listOf(WordMeaning("صِرَٰطَ", "Sirata", "The path", "راستہ")), "")
            )
        }
        return listOf(
            Ayah(1, "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "اللہ کے نام سے جو نہایت مہربان اور رحم فرمانے والا ہے", "بسم الله الرحمن الرحيم", "अल्लाह के नाम से जो रहमान और रहीम है।", emptyList(), shanENuzulMap[surahNumber] ?: "")
        )
    }
}
