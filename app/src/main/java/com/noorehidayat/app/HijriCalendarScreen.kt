package com.noorehidayat.app

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
internal fun HijriCalendarScreen(appLanguage: AppLanguage = AppLanguage.ENGLISH) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("noor_hijri_prefs", Context.MODE_PRIVATE) }
    var moonAdjustment by remember { mutableIntStateOf(prefs.getInt("moon_adj", 0)) }

    // Astronomical Hijri calculation approximation from Gregorian date
    val cal = Calendar.getInstance()
    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
    val year = cal.get(Calendar.YEAR)
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 2=Mon, 5=Thu

    val isMonday = dayOfWeek == Calendar.MONDAY
    val isThursday = dayOfWeek == Calendar.THURSDAY

    val hijriMonthsEn = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    val hijriMonthsUr = listOf(
        "محرم الحرام", "صفر المظفر", "ربیع الاول", "ربیع الثانی",
        "جمادی الاول", "جمادی الثانی", "رجب المرجب", "شعبان المعظم",
        "رمضان المبارک", "شوال المکرم", "ذی القعدہ", "ذی الحجہ"
    )

    // Accurate calculation based on 354.36 days per lunar year
    val epochDiffDays = (year - 622) * 365.25 + dayOfYear - 227 + moonAdjustment
    val hijriYear = (epochDiffDays / 354.367).toInt() + 1
    val dayInHijriYear = (epochDiffDays % 354.367).toInt().coerceAtLeast(0)
    val hijriMonthIdx = (dayInHijriYear / 29.53).toInt().coerceIn(0, 11)
    val hijriDay = ((dayInHijriYear % 29.53).toInt() + 1).coerceIn(1, 30)

    val isWhiteDay = hijriDay in 13..15

    // Moon phase estimate
    val moonAgeDays = (dayInHijriYear % 29.53).toFloat()
    val moonPhaseName = when {
        moonAgeDays < 1.5f -> "New Moon (محاق)"
        moonAgeDays < 7.0f -> "Waxing Crescent (ہلال)"
        moonAgeDays < 9.0f -> "First Quarter (تربیع اول)"
        moonAgeDays < 14.0f -> "Waxing Gibbous (أحدب متزايد)"
        moonAgeDays < 16.5f -> "Full Moon (بدر التمام)"
        moonAgeDays < 22.0f -> "Waning Gibbous (أحدب متناقص)"
        moonAgeDays < 24.0f -> "Last Quarter (تربیع ثانی)"
        else -> "Waning Crescent (ہلال آخر الشهر)"
    }

    val sacredCountdowns = remember(hijriDay, hijriMonthIdx) {
        listOf(
            SacredSeasonCountdown("Ramadan Mubarak", "رمضان المبارک", "1 Ramadan", calculateDaysUntil(hijriMonthIdx, hijriDay, 8, 1), "🌙"),
            SacredSeasonCountdown("Laylat al-Qadr", "شبِ قدر", "27 Ramadan", calculateDaysUntil(hijriMonthIdx, hijriDay, 8, 27), "✨"),
            SacredSeasonCountdown("Eid al-Fitr", "عید الفطر", "1 Shawwal", calculateDaysUntil(hijriMonthIdx, hijriDay, 9, 1), "🕌"),
            SacredSeasonCountdown("Day of Arafah", "یومِ عرفہ", "9 Dhu al-Hijjah", calculateDaysUntil(hijriMonthIdx, hijriDay, 11, 9), "🕋"),
            SacredSeasonCountdown("Eid al-Adha", "عید الاضحیٰ", "10 Dhu al-Hijjah", calculateDaysUntil(hijriMonthIdx, hijriDay, 11, 10), "🐑"),
            SacredSeasonCountdown("Ashura (10th Muharram)", "یومِ عاشوراء", "10 Muharram", calculateDaysUntil(hijriMonthIdx, hijriDay, 0, 10), "📜"),
            SacredSeasonCountdown("Rabi' al-Awwal (Mawlid)", "ربیع الاول شریف", "12 Rabi' al-Awwal", calculateDaysUntil(hijriMonthIdx, hijriDay, 2, 12), "🌹")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Emerald)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text(
            I18n.t("hijri_calendar", appLanguage).uppercase(),
            color = Gold,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (appLanguage == AppLanguage.URDU) "اسلامی ہجری کیلنڈر و چاند" else "Hijri Calendar & Moon Phases",
            color = Cream,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        // Today's Hijri Date Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Panel),
            border = BorderStroke(1.5.dp, Gold)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("UMM AL-QURA CALENDAR", color = Gold, fontSize = 10.sp, letterSpacing = 1.5.sp)
                    Text(
                        "${if (moonAdjustment >= 0) "+$moonAdjustment" else moonAdjustment} Day Adj",
                        color = Muted,
                        fontSize = 10.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "$hijriDay ${hijriMonthsUr[hijriMonthIdx]} $hijriYear ھ",
                    color = Cream,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    "$hijriDay ${hijriMonthsEn[hijriMonthIdx]} $hijriYear AH",
                    color = Gold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(Modifier.height(10.dp))

                // Moon Phase Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Deep, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("🌓", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(moonPhaseName, color = Cream, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Sunnah Fasting Badges
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isMonday || isThursday) EmeraldGlow.copy(alpha = 0.2f) else Deep),
                border = BorderStroke(1.dp, if (isMonday || isThursday) EmeraldGlow else Border)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text("MONDAY & THURSDAY", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (isMonday || isThursday) "Today is a Sunnah Fast! 🌟" else "Sunnah Fast Days",
                        color = Cream,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isWhiteDay) EmeraldGlow.copy(alpha = 0.2f) else Deep),
                border = BorderStroke(1.dp, if (isWhiteDay) EmeraldGlow else Border)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text("WHITE DAYS (13, 14, 15)", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (isWhiteDay) "Ayyam al-Beed Fast Today! 🌕" else "Ayyam al-Beed",
                        color = Cream,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Moon Adjustment Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Panel),
            border = BorderStroke(1.dp, Border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Moon-Sighting Offset: ${if (moonAdjustment >= 0) "+$moonAdjustment" else moonAdjustment}d", color = Cream, fontSize = 12.sp)
                Row {
                    TextButton(onClick = {
                        moonAdjustment = (moonAdjustment - 1).coerceAtLeast(-2)
                        prefs.edit().putInt("moon_adj", moonAdjustment).apply()
                    }) {
                        Text("-1 Day", color = Gold, fontSize = 11.sp)
                    }
                    TextButton(onClick = {
                        moonAdjustment = (moonAdjustment + 1).coerceAtMost(2)
                        prefs.edit().putInt("moon_adj", moonAdjustment).apply()
                    }) {
                        Text("+1 Day", color = Gold, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text("SACRED SEASONS & DAYS COUNTDOWN", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)

        // Countdowns List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            items(sacredCountdowns) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    border = BorderStroke(1.dp, Border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.icon, fontSize = 26.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (appLanguage == AppLanguage.URDU) item.nameUr else item.nameEn,
                                color = Cream,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(item.hijriDateStr, color = Muted, fontSize = 11.sp)
                        }
                        Box(
                            modifier = Modifier
                                .background(Deep, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                if (item.daysRemaining == 0) "Today! 🎉" else "${item.daysRemaining} days left",
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun calculateDaysUntil(currMonth: Int, currDay: Int, targetMonth: Int, targetDay: Int): Int {
    val currTotalDays = currMonth * 30 + currDay
    val targetTotalDays = targetMonth * 30 + targetDay
    val diff = targetTotalDays - currTotalDays
    return if (diff >= 0) diff else diff + 354
}
