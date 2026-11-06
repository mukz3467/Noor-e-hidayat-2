package com.noorehidayat.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testI18nTranslations() {
        assertEquals("نورِ ہدایت", I18n.t("app_title", AppLanguage.URDU))
        assertEquals("Noor-e-Hidayat", I18n.t("app_title", AppLanguage.ENGLISH))
        assertEquals("نور الهداية", I18n.t("app_title", AppLanguage.ARABIC))
        assertEquals("नूर-ए-हिदायत", I18n.t("app_title", AppLanguage.HINDI))
    }

    @Test
    fun testZakatNisabCalculation() {
        val goldNisabGrams = 87.48
        val silverNisabGrams = 612.36
        val goldPricePerGram = 23500.0
        val silverPricePerGram = 310.0

        val goldNisabTotal = goldNisabGrams * goldPricePerGram
        val silverNisabTotal = silverNisabGrams * silverPricePerGram

        assertTrue(goldNisabTotal > 2000000.0)
        assertTrue(silverNisabTotal > 180000.0)

        val totalAssets = 500000.0
        val liabilities = 100000.0
        val netWealth = totalAssets - liabilities
        val zakatPayable = if (netWealth >= silverNisabTotal) netWealth * 0.025 else 0.0

        assertEquals(10000.0, zakatPayable, 0.01)
    }

    @Test
    fun testKhatamTargetCalculation() {
        val totalAyahs = 6236
        val goalDays30 = 30
        val daily30 = (totalAyahs + goalDays30 - 1) / goalDays30
        val perPrayer = (daily30 + 4) / 5

        assertEquals(208, daily30)
        assertEquals(42, perPrayer)
    }
}
