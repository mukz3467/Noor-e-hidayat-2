package com.noorehidayat.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.util.Calendar

/**
 * Noor-e-Hidayat Premium Dynamic Theme System
 * Centralized theme tokens, catalog of 12 distinct aesthetic themes,
 * live preview engine, custom studio and smart appearance scheduling.
 */

data class AppThemeColors(
    val background: Color,       // Primary scaffold background
    val surfaceVariant: Color,   // Secondary surface (inner cards, chips, input fields)
    val surface: Color,          // Elevated surface (cards, dialogs, bottom sheets)
    val primary: Color,          // Brand accent, primary buttons, major titles
    val textPrimary: Color,      // High emphasis text, Quranic ayahs, numbers
    val textSecondary: Color,    // Medium emphasis text, subtitles, hints
    val border: Color,           // Subtle container outlines and dividers
    val accent: Color,           // Special highlights, streaks, active states
    val navBar: Color,           // Navigation bar & TopBar container color
    val isDark: Boolean = true
)

data class ThemeDefinition(
    val id: String,
    val name: String,
    val nameUrdu: String,
    val description: String,
    val colors: AppThemeColors,
    val isDark: Boolean = true
)

enum class AppearanceMode(val label: String, val labelUrdu: String) {
    SYSTEM("System Default", "سسٹم کے مطابق"),
    DARK("Always Dark", "ہمیشہ ڈارک"),
    LIGHT("Always Light", "ہمیشہ لائٹ"),
    AUTO_SCHEDULE("Auto Day / Night", "خودکار دن / رات شیڈول"),
    MANUAL("Manual Theme", "دستی انتخاب")
}

object ThemeCatalog {
    // 1. Islamic Emerald – Deep Islamic green, warm white, and soft sage
    val IslamicEmerald = ThemeDefinition(
        id = "islamic_emerald",
        name = "Islamic Emerald",
        nameUrdu = "اسلامک زمرد",
        description = "Deep Islamic green with warm ivory text and sage accents",
        colors = AppThemeColors(
            background = Color(0xFF062319),
            surfaceVariant = Color(0xFF0D3325),
            surface = Color(0xFF114230),
            primary = Color(0xFFE7C879),
            textPrimary = Color(0xFFF4F9F5),
            textSecondary = Color(0xFFB2D4C3),
            border = Color(0xFF235842),
            accent = Color(0xFF10B981),
            navBar = Color(0xFF062319),
            isDark = true
        )
    )

    // 2. Sage Serenity – Muted sage green, cream, and charcoal
    val SageSerenity = ThemeDefinition(
        id = "sage_serenity",
        name = "Sage Serenity",
        nameUrdu = "سیج سکون",
        description = "Calm muted sage green with cream text and soothing tones",
        colors = AppThemeColors(
            background = Color(0xFF141E18),
            surfaceVariant = Color(0xFF1F2C24),
            surface = Color(0xFF283A30),
            primary = Color(0xFF88C5A3),
            textPrimary = Color(0xFFEBF4EE),
            textSecondary = Color(0xFFA5C5B3),
            border = Color(0xFF385042),
            accent = Color(0xFF6EE7B7),
            navBar = Color(0xFF141E18),
            isDark = true
        )
    )

    // 3. Midnight Forest – Dark forest green with warm ivory text
    val MidnightForest = ThemeDefinition(
        id = "midnight_forest",
        name = "Midnight Forest",
        nameUrdu = "شبینہ جنگل",
        description = "Darkest pine forest backdrop with warm ivory text & golden accents",
        colors = AppThemeColors(
            background = Color(0xFF0B1611),
            surfaceVariant = Color(0xFF12241C),
            surface = Color(0xFF1B3327),
            primary = Color(0xFFD4AF37),
            textPrimary = Color(0xFFF4EFE6),
            textSecondary = Color(0xFFB4CDBF),
            border = Color(0xFF2A4D3B),
            accent = Color(0xFF4ADE80),
            navBar = Color(0xFF0B1611),
            isDark = true
        )
    )

    // 4. Ocean Breeze – Soft teal, light blue, and white
    val OceanBreeze = ThemeDefinition(
        id = "ocean_breeze",
        name = "Ocean Breeze",
        nameUrdu = "نسیمِ بحر",
        description = "Deep marine sapphire with soft ocean teal and crisp text",
        colors = AppThemeColors(
            background = Color(0xFF08181E),
            surfaceVariant = Color(0xFF0F2630),
            surface = Color(0xFF163644),
            primary = Color(0xFF38BDF8),
            textPrimary = Color(0xFFEDF9FC),
            textSecondary = Color(0xFF94D2E2),
            border = Color(0xFF265365),
            accent = Color(0xFF2DD4BF),
            navBar = Color(0xFF08181E),
            isDark = true
        )
    )

    // 5. Royal Navy – Deep navy, muted blue, and soft white
    val RoyalNavy = ThemeDefinition(
        id = "royal_navy",
        name = "Royal Navy",
        nameUrdu = "شاہی بحریہ",
        description = "Midnight royal navy blue with celestial accents",
        colors = AppThemeColors(
            background = Color(0xFF0A1120),
            surfaceVariant = Color(0xFF111D35),
            surface = Color(0xFF192A4C),
            primary = Color(0xFF60A5FA),
            textPrimary = Color(0xFFF0F5FF),
            textSecondary = Color(0xFFA5BBE3),
            border = Color(0xFF2B4372),
            accent = Color(0xFF93C5FD),
            navBar = Color(0xFF0A1120),
            isDark = true
        )
    )

    // 6. Desert Sand – Warm beige, sand, and brown
    val DesertSand = ThemeDefinition(
        id = "desert_sand",
        name = "Desert Sand",
        nameUrdu = "ریگِ صحرا",
        description = "Warm desert sand dusk with terracotta and amber highlights",
        colors = AppThemeColors(
            background = Color(0xFF18130E),
            surfaceVariant = Color(0xFF251E16),
            surface = Color(0xFF342A20),
            primary = Color(0xFFE2A96D),
            textPrimary = Color(0xFFF9F1E7),
            textSecondary = Color(0xFFD5BA9F),
            border = Color(0xFF524132),
            accent = Color(0xFFF59E0B),
            navBar = Color(0xFF18130E),
            isDark = true
        )
    )

    // 7. Lavender Calm – Soft lavender, muted purple, and off-white
    val LavenderCalm = ThemeDefinition(
        id = "lavender_calm",
        name = "Lavender Calm",
        nameUrdu = "سکونِ سوسن",
        description = "Soothing deep violet twilight with soft lavender serenity",
        colors = AppThemeColors(
            background = Color(0xFF14101D),
            surfaceVariant = Color(0xFF1E182B),
            surface = Color(0xFF2B233D),
            primary = Color(0xFFC084FC),
            textPrimary = Color(0xFFF6F2FD),
            textSecondary = Color(0xFFC9BBDC),
            border = Color(0xFF4B3E68),
            accent = Color(0xFFE879F9),
            navBar = Color(0xFF14101D),
            isDark = true
        )
    )

    // 8. Rose Serenity – Dusty rose, soft pink, and warm white
    val RoseSerenity = ThemeDefinition(
        id = "rose_serenity",
        name = "Rose Serenity",
        nameUrdu = "گلابِ راحت",
        description = "Muted dusty rose and deep plum with warm rosy glow",
        colors = AppThemeColors(
            background = Color(0xFF1A0F14),
            surfaceVariant = Color(0xFF281720),
            surface = Color(0xFF37202C),
            primary = Color(0xFFF472B6),
            textPrimary = Color(0xFFFDF2F7),
            textSecondary = Color(0xFFDDAFCA),
            border = Color(0xFF573347),
            accent = Color(0xFFFB7185),
            navBar = Color(0xFF1A0F14),
            isDark = true
        )
    )

    // 9. Minimal White – Clean white, subtle grey, and charcoal (Light Mode)
    val MinimalWhite = ThemeDefinition(
        id = "minimal_white",
        name = "Minimal White",
        nameUrdu = "سفید سادگی",
        description = "Ultra-clean eye-comfort light theme with high contrast charcoal text",
        colors = AppThemeColors(
            background = Color(0xFFF8F9FA),
            surfaceVariant = Color(0xFFEDF0F2),
            surface = Color(0xFFFFFFFF),
            primary = Color(0xFF0F766E),
            textPrimary = Color(0xFF18181B),
            textSecondary = Color(0xFF52525B),
            border = Color(0xFFE2E8F0),
            accent = Color(0xFFD97706),
            navBar = Color(0xFFFFFFFF),
            isDark = false
        ),
        isDark = false
    )

    // 10. Golden Heritage – Muted gold, deep green, and cream
    val GoldenHeritage = ThemeDefinition(
        id = "golden_heritage",
        name = "Golden Heritage",
        nameUrdu = "سنہری ورثہ",
        description = "Royal Ottoman bronze and gold with antique heritage aesthetics",
        colors = AppThemeColors(
            background = Color(0xFF110F09),
            surfaceVariant = Color(0xFF1D1910),
            surface = Color(0xFF2A2417),
            primary = Color(0xFFF59E0B),
            textPrimary = Color(0xFFFEF9E7),
            textSecondary = Color(0xFFDAC899),
            border = Color(0xFF524528),
            accent = Color(0xFFFBBF24),
            navBar = Color(0xFF110F09),
            isDark = true
        )
    )

    // 11. Charcoal Night – Dark charcoal with soft grey and ivory
    val CharcoalNight = ThemeDefinition(
        id = "charcoal_night",
        name = "Charcoal Night",
        nameUrdu = "کوئلہ رات",
        description = "Modern slate charcoal with warm platinum and ivory typography",
        colors = AppThemeColors(
            background = Color(0xFF101012),
            surfaceVariant = Color(0xFF1A1A1E),
            surface = Color(0xFF25252B),
            primary = Color(0xFFE5B842),
            textPrimary = Color(0xFFF4F4F6),
            textSecondary = Color(0xFFA1A1AA),
            border = Color(0xFF3F3F46),
            accent = Color(0xFF38BDF8),
            navBar = Color(0xFF101012),
            isDark = true
        )
    )

    // 12. Pure Black AMOLED – True black background with 24K Kaaba Gold
    val PureBlackAmoled = ThemeDefinition(
        id = "pure_black",
        name = "Pure Black AMOLED",
        nameUrdu = "خالص سیاہ کعبہ",
        description = "Holy Kaaba Kiswah true black with 24K radiant gold calligraphy",
        colors = AppThemeColors(
            background = Color(0xFF000000),
            surfaceVariant = Color(0xFF0D0D0D),
            surface = Color(0xFF161616),
            primary = Color(0xFFFFD700),
            textPrimary = Color(0xFFFFDF73),
            textSecondary = Color(0xFFD4B872),
            border = Color(0xFF6B5824),
            accent = Color(0xFFFFC107),
            navBar = Color(0xFF000000),
            isDark = true
        )
    )

    val allThemes: List<ThemeDefinition> = listOf(
        PureBlackAmoled,
        IslamicEmerald,
        SageSerenity,
        MidnightForest,
        OceanBreeze,
        RoyalNavy,
        DesertSand,
        LavenderCalm,
        RoseSerenity,
        MinimalWhite,
        GoldenHeritage,
        CharcoalNight
    )

    fun getThemeById(id: String): ThemeDefinition {
        return allThemes.find { it.id == id } ?: PureBlackAmoled
    }
}

/**
 * Controller holding dynamic theme state, scheduling & persistence.
 */
class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("noor_theme_prefs", Context.MODE_PRIVATE)

    var currentThemeId by mutableStateOf(prefs.getString("selected_theme_id", "pure_black") ?: "pure_black")
        private set

    var appearanceMode by mutableStateOf(
        AppearanceMode.valueOf(prefs.getString("appearance_mode", AppearanceMode.MANUAL.name) ?: AppearanceMode.MANUAL.name)
    )
        private set

    var dayThemeId by mutableStateOf(prefs.getString("day_theme_id", "minimal_white") ?: "minimal_white")
        private set

    var nightThemeId by mutableStateOf(prefs.getString("night_theme_id", "pure_black") ?: "pure_black")
        private set

    var dayStartHour by mutableIntStateOf(prefs.getInt("day_start_hour", 6))
        private set

    var nightStartHour by mutableIntStateOf(prefs.getInt("night_start_hour", 18))
        private set

    var eyeComfortEnabled by mutableStateOf(prefs.getBoolean("eye_comfort_enabled", false))
        private set

    var arabicFontSize by mutableFloatStateOf(prefs.getFloat("arabic_font_size_custom", 28f))
        private set

    var customThemeColors by mutableStateOf<AppThemeColors?>(loadCustomTheme())
        private set

    fun selectTheme(themeId: String) {
        currentThemeId = themeId
        appearanceMode = AppearanceMode.MANUAL
        prefs.edit()
            .putString("selected_theme_id", themeId)
            .putString("appearance_mode", AppearanceMode.MANUAL.name)
            .apply()
    }

    fun setMode(mode: AppearanceMode) {
        appearanceMode = mode
        prefs.edit().putString("appearance_mode", mode.name).apply()
    }

    fun updateSchedule(dayTheme: String, nightTheme: String, dayHour: Int, nightHour: Int) {
        dayThemeId = dayTheme
        nightThemeId = nightTheme
        dayStartHour = dayHour
        nightStartHour = nightHour
        prefs.edit()
            .putString("day_theme_id", dayTheme)
            .putString("night_theme_id", nightTheme)
            .putInt("day_start_hour", dayHour)
            .putInt("night_start_hour", nightHour)
            .apply()
    }

    fun toggleEyeComfort(enabled: Boolean) {
        eyeComfortEnabled = enabled
        prefs.edit().putBoolean("eye_comfort_enabled", enabled).apply()
    }

    fun updateArabicFontSize(size: Float) {
        arabicFontSize = size
        prefs.edit().putFloat("arabic_font_size_custom", size).apply()
    }

    fun saveCustomTheme(colors: AppThemeColors) {
        customThemeColors = colors
        prefs.edit()
            .putInt("custom_bg", colors.background.toArgb())
            .putInt("custom_surface_var", colors.surfaceVariant.toArgb())
            .putInt("custom_surface", colors.surface.toArgb())
            .putInt("custom_primary", colors.primary.toArgb())
            .putInt("custom_text_pri", colors.textPrimary.toArgb())
            .putInt("custom_text_sec", colors.textSecondary.toArgb())
            .putInt("custom_border", colors.border.toArgb())
            .putInt("custom_accent", colors.accent.toArgb())
            .putInt("custom_nav", colors.navBar.toArgb())
            .putBoolean("custom_is_dark", colors.isDark)
            .putBoolean("has_custom_theme", true)
            .apply()
        selectTheme("custom")
    }

    fun resetAllSettings() {
        prefs.edit().clear().apply()
        currentThemeId = "pure_black"
        appearanceMode = AppearanceMode.MANUAL
        dayThemeId = "minimal_white"
        nightThemeId = "pure_black"
        dayStartHour = 6
        nightStartHour = 18
        eyeComfortEnabled = false
        arabicFontSize = 28f
        customThemeColors = null
    }

    private fun loadCustomTheme(): AppThemeColors? {
        if (!prefs.getBoolean("has_custom_theme", false)) return null
        return try {
            AppThemeColors(
                background = Color(prefs.getInt("custom_bg", 0xFF000000.toInt())),
                surfaceVariant = Color(prefs.getInt("custom_surface_var", 0xFF0D0D0D.toInt())),
                surface = Color(prefs.getInt("custom_surface", 0xFF161616.toInt())),
                primary = Color(prefs.getInt("custom_primary", 0xFFFFD700.toInt())),
                textPrimary = Color(prefs.getInt("custom_text_pri", 0xFFFFDF73.toInt())),
                textSecondary = Color(prefs.getInt("custom_text_sec", 0xFFD4B872.toInt())),
                border = Color(prefs.getInt("custom_border", 0xFF6B5824.toInt())),
                accent = Color(prefs.getInt("custom_accent", 0xFFFFC107.toInt())),
                navBar = Color(prefs.getInt("custom_nav", 0xFF000000.toInt())),
                isDark = prefs.getBoolean("custom_is_dark", true)
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Resolves the active AppThemeColors considering appearance mode,
     * time of day schedule, eye comfort tint and custom themes.
     */
    fun resolveActiveColors(isSystemInDark: Boolean): AppThemeColors {
        val baseColors: AppThemeColors = when (appearanceMode) {
            AppearanceMode.DARK -> {
                if (currentThemeId == "custom" && customThemeColors != null) customThemeColors!!
                else {
                    val def = ThemeCatalog.getThemeById(currentThemeId)
                    if (def.isDark) def.colors else ThemeCatalog.PureBlackAmoled.colors
                }
            }
            AppearanceMode.LIGHT -> {
                if (currentThemeId == "custom" && customThemeColors != null) customThemeColors!!
                else ThemeCatalog.MinimalWhite.colors
            }
            AppearanceMode.SYSTEM -> {
                if (isSystemInDark) {
                    val def = ThemeCatalog.getThemeById(currentThemeId)
                    if (def.isDark) def.colors else ThemeCatalog.PureBlackAmoled.colors
                } else {
                    ThemeCatalog.MinimalWhite.colors
                }
            }
            AppearanceMode.AUTO_SCHEDULE -> {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val isDayTime = if (dayStartHour < nightStartHour) {
                    hour in dayStartHour until nightStartHour
                } else {
                    hour >= dayStartHour || hour < nightStartHour
                }
                val themeIdToUse = if (isDayTime) dayThemeId else nightThemeId
                ThemeCatalog.getThemeById(themeIdToUse).colors
            }
            AppearanceMode.MANUAL -> {
                if (currentThemeId == "custom" && customThemeColors != null) {
                    customThemeColors!!
                } else {
                    ThemeCatalog.getThemeById(currentThemeId).colors
                }
            }
        }

        // Apply gentle Eye Comfort reading warmth if toggled on
        return if (eyeComfortEnabled) {
            baseColors.copy(
                background = if (baseColors.isDark) Color(0xFF14120D) else Color(0xFFF9F7EE),
                surface = if (baseColors.isDark) Color(0xFF1E1A13) else Color(0xFFFFFDF5),
                surfaceVariant = if (baseColors.isDark) Color(0xFF262118) else Color(0xFFF0EDE0),
                textPrimary = if (baseColors.isDark) Color(0xFFFFECC4) else Color(0xFF2A2318)
            )
        } else {
            baseColors
        }
    }
}

val LocalAppTheme = staticCompositionLocalOf {
    ThemeCatalog.PureBlackAmoled.colors
}

val LocalThemeManager = staticCompositionLocalOf<ThemeManager> {
    error("ThemeManager not provided")
}
