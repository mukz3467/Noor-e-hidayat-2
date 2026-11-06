package com.noorehidayat.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Global Dynamic Theme Color Forwarders
// Every Composable reading these tokens automatically reacts to the active theme!
val Emerald: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.background

val Deep: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.surfaceVariant

val Panel: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.surface

val Gold: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.primary

val Cream: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.textPrimary

val Muted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.textSecondary

val Border: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.border

val EmeraldGlow: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.accent

data class Surah(
    val number: Int,
    val name: String,
    val english: String,
    val meaning: String,
    val ayahs: Int,
    val type: String
)

data class Ayah(
    val number: Int,
    val arabic: String,
    val englishTranslation: String,
    val urduTranslation: String,
    val arabicTafseer: String,
    val hindiTranslation: String,
    val words: List<WordMeaning> = emptyList(),
    val revelationContext: String = "",
    val translationEditions: List<AyahTranslationText> = emptyList(),
    val tafsirEntries: List<AyahTafsirText> = emptyList()
)

data class AyahTafsirText(
    val editionId: String,
    val languageCode: String,
    val scholarName: String,
    val translatorName: String?,
    val workTitle: String,
    val text: String,
    val sourceReference: String
)

data class AyahTranslationText(
    val editionId: String,
    val languageCode: String,
    val scholarName: String,
    val translatorName: String?,
    val workTitle: String,
    val text: String
)

data class WordMeaning(
    val arabic: String,
    val transliteration: String,
    val english: String,
    val urdu: String
)

data class Qari(
    val id: String,
    val name: String,
    val nameUrdu: String,
    val cdns: List<String>
)

data class LiveStream(
    val id: String,
    val title: String,
    val titleUrdu: String,
    val location: String,
    val youtubeId: String,
    val description: String
)

data class LectureVideo(
    val id: String,
    val title: String,
    val speaker: String,
    val category: String,
    val youtubeId: String,
    val duration: String
)

data class IslamicDua(
    val title: String,
    val titleUrdu: String,
    val arabic: String,
    val transliteration: String,
    val english: String,
    val urdu: String,
    val benefit: String
)

data class QuizItem(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

// --- Feature 1: Digital Tasbeeh & Zikr Models ---
data class TasbeehItem(
    val id: String,
    val arabic: String,
    val transliteration: String,
    val englishMeaning: String,
    val urduMeaning: String,
    val target: Int,
    val currentCount: Int = 0
)

// --- Feature 2: Complete Hisn al-Muslim Models ---
data class HisnDuaItem(
    val id: String,
    val category: String,
    val categoryUrdu: String,
    val title: String,
    val titleUrdu: String,
    val arabic: String,
    val transliteration: String,
    val english: String,
    val urdu: String,
    val reference: String,
    val virtue: String,
    val recommendedCount: Int = 1,
    val referenceUrl: String = ""
)

// --- Feature 3: Authentic Hadith Models ---
data class HadithItem(
    val id: String,
    val book: String,
    val chapter: String,
    val hadithNumber: String,
    val narrator: String,
    val arabic: String,
    val english: String,
    val urdu: String,
    val topic: String,
    val referenceUrl: String = "",
    val hindi: String = ""
)

// --- Feature 4: Hijri Calendar & Moon Models ---
data class HijriDate(
    val day: Int,
    val monthNameEn: String,
    val monthNameUr: String,
    val monthNameAr: String,
    val year: Int,
    val moonPhase: String,
    val moonAgePercent: Float
)

data class SacredSeasonCountdown(
    val nameEn: String,
    val nameUr: String,
    val hijriDateStr: String,
    val daysRemaining: Int,
    val icon: String
)

// --- Feature 5: Smart Zakat Models ---
data class CurrencyRate(
    val code: String,
    val symbol: String,
    val goldGramPrice: Double,
    val silverGramPrice: Double
)

// --- Feature 7: Khatam Quran Planner Models ---
data class KhatamPlan(
    val totalDays: Int,
    val dailyTargetAyahs: Int,
    val targetPerPrayer: Int,
    val completedAyahs: Int,
    val currentJuz: Int,
    val streakDays: Int
)

// --- Feature 8: Ayah & Hadith Card Templates ---
enum class CardTheme(val id: String, val label: String, val bgStart: Color, val bgEnd: Color, val textClr: Color, val accentClr: Color) {
    ROYAL_EMERALD("emerald", "Kaaba Kiswah Black & Gold", Color(0xFF000000), Color(0xFF161616), Color(0xFFFFDF73), Color(0xFFFFD700)),
    MIDNIGHT_BLUE("blue", "Midnight Obsidian & Gold", Color(0xFF080808), Color(0xFF1A170F), Color(0xFFFFE8A3), Color(0xFFF3C74D)),
    MINIMAL_DARK("mono", "Sacred Charcoal & 24K Gold", Color(0xFF0D0D0D), Color(0xFF211D14), Color(0xFFFFF2C6), Color(0xFFFFD700)),
    WARM_PARCHMENT("parchment", "Warm Amber Parchment", Color(0xFF1F140A), Color(0xFF38230F), Color(0xFFFEF3C7), Color(0xFFF59E0B))
}

// 4 Synchronized Languages: English, Urdu (اردو), Arabic (العربية), Hindi (हिन्दी)
enum class AppLanguage(val code: String, val label: String, val isRtl: Boolean) {
    ENGLISH("en", "English", false),
    URDU("ur", "اردو", true),
    ARABIC("ar", "العربية", true),
    HINDI("hi", "हिन्दी", false)
}

object I18n {
    fun t(key: String, lang: AppLanguage): String {
        val dict = when (lang) {
            AppLanguage.URDU -> urduDict
            AppLanguage.ARABIC -> arabicDict
            AppLanguage.HINDI -> hindiDict
            AppLanguage.ENGLISH -> englishDict
        }
        return dict[key] ?: englishDict[key] ?: key
    }

    private val englishDict = mapOf(
        "app_title" to "Noor-e-Hidayat",
        "home" to "Home",
        "quran" to "Holy Quran",
        "reader" to "Quran Reader",
        "tafseer" to "Tafseer",
        "recitations" to "Audio Recitations",
        "prayer_times" to "Prayer Times",
        "qibla" to "Qibla Compass",
        "live_harameyn" to "Harameyn Live",
        "lectures" to "Islamic Lectures",
        "learning" to "Quran Learning",
        "duas" to "Daily Duas",
        "quiz" to "Islamic Quiz",
        "more" to "More",
        "settings" to "Settings",
        "tasbeeh" to "Digital Tasbeeh",
        "hisn_al_muslim" to "Hisn al-Muslim",
        "hadith_library" to "Hadith Library",
        "hijri_calendar" to "Hijri Calendar",
        "zakat_calculator" to "Zakat Calculator",
        "khatam_planner" to "Khatam Quran Planner",
        "card_generator" to "Card Generator",
        "ramadan_mode" to "Ramadan Companion",
        "hifz_mode" to "Hifz Memorization Mode",
        "search_surah" to "Search Surah name, number...",
        "font_size" to "Arabic Font Size",
        "copy_ayah" to "Ayah copied to clipboard",
        "word_by_word" to "Word-by-Word Vocabulary",
        "historical_context" to "Historical Context (Shan-e-Nuzul)",
        "play_audio" to "Play Recitation",
        "select_qari" to "Select Qari",
        "test_adhan" to "Test Adhan Alarm (30s Preview)",
        "adhan_playing" to "Adhan Alarm Active",
        "stop_adhan" to "Stop Adhan",
        "towards_qibla" to "Aligned with Kaaba",
        "qibla_bearing" to "Qibla Bearing",
        "distance_to_kaaba" to "Distance to Kaaba",
        "save" to "Save",
        "saved" to "Saved",
        "share" to "Share Card",
        "streak" to "Day Streak",
        "reset" to "Reset",
        "target" to "Target",
        "theme_settings" to "Theme & Colors",
        "custom_theme" to "Custom Color Studio",
        "appearance" to "Appearance Settings",
        "eye_comfort" to "Eye Comfort & Warmth"
    )

    private val urduDict = mapOf(
        "app_title" to "نورِ ہدایت",
        "home" to "صفحہ اول",
        "quran" to "قرآن پاک",
        "reader" to "تلاوتِ قرآن",
        "tafseer" to "تفسیر القرآن",
        "recitations" to "خوبصورت تلاوتیں",
        "prayer_times" to "نماز کے اوقات",
        "qibla" to "قبلہ نما",
        "live_harameyn" to "حرمین شریفین لائیو",
        "lectures" to "اسلامی بیانات",
        "learning" to "قرآن سیکھیں",
        "duas" to "روزمرہ دعائیں",
        "quiz" to "اسلامی کوئز",
        "more" to "مزید",
        "settings" to "ترتیبات",
        "tasbeeh" to "ڈیجیٹل تسبیح کاؤنٹر",
        "hisn_al_muslim" to "حصن المسلم (مسنون دعائیں)",
        "hadith_library" to "مستند کتبِ احادیث",
        "hijri_calendar" to "ہجری کیلنڈر اور چاند",
        "zakat_calculator" to "جامع زکوٰۃ کیلکولیٹر",
        "khatam_planner" to "ختمِ قرآن پلانر و ٹریکر",
        "card_generator" to "آیت و حدیث کارڈ جنریٹر",
        "ramadan_mode" to "رمضان المبارک اسپیشل",
        "hifz_mode" to "حفظ و تکرار موڈ",
        "search_surah" to "سورہ کا نام یا نمبر تلاش کریں...",
        "font_size" to "عربی خط کا سائز",
        "copy_ayah" to "آیت کاپی کر لی گئی ہے",
        "word_by_word" to "لفظ بہ لفظ ترجمہ و معانی",
        "historical_context" to "شانِ نزول اور پس منظر",
        "play_audio" to "تلاوت سنیں",
        "select_qari" to "قاری منتخب کریں",
        "test_adhan" to "اذان الارم ٹیسٹ (30 سیکنڈ پیش منظر)",
        "adhan_playing" to "اذان الارم جاری ہے",
        "stop_adhan" to "اذان بند کریں",
        "towards_qibla" to "قبلہ کے عین سامنے",
        "qibla_bearing" to "قبلہ کا زاویہ",
        "distance_to_kaaba" to "کعبہ شریف سے فاصلہ",
        "save" to "محفوظ کریں",
        "saved" to "محفوظ شدہ",
        "share" to "کارڈ شیئر کریں",
        "streak" to "روزانہ ذکر تسلسل",
        "reset" to "ری سیٹ",
        "target" to "ہدف",
        "theme_settings" to "تھیمز اور رنگ",
        "custom_theme" to "کسٹم کلر اسٹوڈیو",
        "appearance" to "ظاہری ترتیبات",
        "eye_comfort" to "آرامِ چشم (Eye Comfort)"
    )

    private val arabicDict = mapOf(
        "app_title" to "نور الهداية",
        "home" to "الرئيسية",
        "quran" to "القرآن الكريم",
        "reader" to "قارئ القرآن",
        "tafseer" to "التفسير",
        "recitations" to "التلاوات القرآنية",
        "prayer_times" to "مواقيت الصلاة",
        "qibla" to "اتجاه القبلة",
        "live_harameyn" to "الحرمين بث مباشر",
        "lectures" to "محاضرات إسلامية",
        "learning" to "تعليم القرآن",
        "duas" to "أدعية يومية",
        "quiz" to "مسابقة إسلامية",
        "more" to "المزيد",
        "settings" to "الإعدادات",
        "tasbeeh" to "المسبحة الإلكترونية",
        "hisn_al_muslim" to "حصن المسلم",
        "hadith_library" to "موسوعة الأحاديث النبوية",
        "hijri_calendar" to "التقويم الهجري وأطوار القمر",
        "zakat_calculator" to "حاسبة الزكاة الذكية",
        "khatam_planner" to "مخطط ختم القرآن",
        "card_generator" to "صانع بطاقات الآيات والحديث",
        "ramadan_mode" to "دليل رمضان المبارك",
        "hifz_mode" to "نمط الحفظ والتكرار",
        "search_surah" to "ابحث عن السورة...",
        "font_size" to "حجم الخط القرآني",
        "copy_ayah" to "تم نسخ الآية الكريمة",
        "word_by_word" to "معاني الكلمات",
        "historical_context" to "أسباب النزول",
        "play_audio" to "استمع للتلاوة",
        "select_qari" to "اختر القارئ",
        "test_adhan" to "تجربة منبه الأذان (٣٠ ثانية)",
        "adhan_playing" to "صوت الأذان يعمل",
        "stop_adhan" to "إيقاف الأذان",
        "towards_qibla" to "متجه نحو القبلة",
        "qibla_bearing" to "درجة القبلة",
        "distance_to_kaaba" to "المسافة إلى الكعبة",
        "save" to "حفظ",
        "saved" to "تم الحفظ",
        "share" to "مشاركة البطاقة",
        "streak" to "تتابع الأذكار",
        "reset" to "إعادة تعيين",
        "target" to "الهدف",
        "theme_settings" to "السمات والألوان",
        "custom_theme" to "استوديو الألوان المخصص",
        "appearance" to "إعدادات المظهر",
        "eye_comfort" to "راحة العين"
    )

    private val hindiDict = mapOf(
        "app_title" to "नूर-ए-हिदायत",
        "home" to "होम",
        "quran" to "पवित्र कुरआन",
        "reader" to "कुरआन पाठ",
        "tafseer" to "तफ़सीर",
        "recitations" to "तिलावत ऑडियो",
        "prayer_times" to "नमाज़ का समय",
        "qibla" to "किबला दिशा",
        "live_harameyn" to "हरमैन लाइव",
        "lectures" to "इस्लामी बयान",
        "learning" to "कुरआन सीखें",
        "duas" to "दैनिक दुआएं",
        "quiz" to "इस्लामिक क्विज़",
        "more" to "अन्य",
        "settings" to "सेटिंग्स",
        "tasbeeh" to "डिजिटल तस्बीह काउंटर",
        "hisn_al_muslim" to "हिस्नुल मुस्लिम",
        "hadith_library" to "हदीस संग्रह",
        "hijri_calendar" to "हिजरी कैलेंडर और चांद",
        "zakat_calculator" to "ज़कात कैलकुलेटर",
        "khatam_planner" to "खत्म-ए-कुरआन प्लानर",
        "card_generator" to "आयत और हदीस कार्ड",
        "ramadan_mode" to "रमज़ान मुबारक स्पेशल",
        "hifz_mode" to "हिफ़्ज़ और दोहराव मोड",
        "search_surah" to "सूरह खोजें...",
        "font_size" to "अरबी फ़ॉन्ट आकार",
        "copy_ayah" to "आयत कॉपी हो गई",
        "word_by_word" to "शब्द-दर-शब्द अर्थ",
        "historical_context" to "शान-ए-नुज़ूल (पृष्ठभूमि)",
        "play_audio" to "तिलावत सुनें",
        "select_qari" to "क़ारी चुनें",
        "test_adhan" to "अज़ान अलार्म टेस्ट (30 सेकंड)",
        "adhan_playing" to "अज़ान बज रही है",
        "stop_adhan" to "अज़ान बंद करें",
        "towards_qibla" to "किबला की सीध में",
        "qibla_bearing" to "किबلا कोण",
        "distance_to_kaaba" to "काबा से दूरी",
        "save" to "सहेजें",
        "saved" to "सहेजा गया",
        "share" to "शेयर करें",
        "streak" to "दैनिक ज़िक्र स्ट्रीک",
        "reset" to "रीसेट",
        "target" to "लक्ष्य",
        "theme_settings" to "थीम और रंग",
        "custom_theme" to "कस्टम कलर स्टूडियो",
        "appearance" to "दिखावट सेटिंग्स",
        "eye_comfort" to "आंखों का आराम"
    )
}
