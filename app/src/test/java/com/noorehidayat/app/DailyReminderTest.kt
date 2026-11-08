package com.noorehidayat.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyReminderTest {
    @Test
    fun reminderMessageIsStableForTheSameDayAndLanguage() {
        assertEquals(
            dailyReminderMessage(120, "ur"),
            dailyReminderMessage(120, "ur")
        )
    }

    @Test
    fun reminderMessageSupportsUrduArabicHindiAndEnglish() {
        val languages = listOf("ur", "ar", "hi", "en")
        languages.forEach { language ->
            assertTrue(dailyReminderMessage(12, language).isNotBlank())
        }
        assertNotEquals(
            dailyReminderMessage(12, "ur"),
            dailyReminderMessage(12, "en")
        )
    }

    @Test
    fun unknownLanguageFallsBackToEnglish() {
        assertEquals(
            dailyReminderMessage(44, "en"),
            dailyReminderMessage(44, "unknown")
        )
    }
}
