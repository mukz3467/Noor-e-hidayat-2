package com.noorehidayat.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IslamicSourceModelsTest {
    @Test
    fun quranCoverageRequiresAll114SurahsAnd6236Ayahs() {
        val complete = QuranCorpusCoverage(
            totalSurahs = 114,
            totalAyahs = 6236,
            arabicAyahs = 6236,
            translationsByLanguage = emptyMap(),
            tafsirEntriesByEdition = emptyMap(),
            approvedEditions = emptySet()
        )
        assertTrue(complete.hasCompleteArabicQuran())

        val incomplete = complete.copy(arabicAyahs = 6235)
        assertFalse(incomplete.hasCompleteArabicQuran())
    }

    @Test
    fun translationCoverageIsCheckedPerLanguage() {
        val coverage = QuranCorpusCoverage(
            totalSurahs = 114,
            totalAyahs = 6236,
            arabicAyahs = 6236,
            translationsByLanguage = mapOf(
                ContentLanguage.URDU to 6236,
                ContentLanguage.ENGLISH to 6235
            ),
            tafsirEntriesByEdition = emptyMap(),
            approvedEditions = emptySet()
        )
        assertTrue(coverage.hasCompleteTranslation(ContentLanguage.URDU))
        assertFalse(coverage.hasCompleteTranslation(ContentLanguage.ENGLISH))
        assertFalse(coverage.hasCompleteTranslation(ContentLanguage.HINDI))
    }

    @Test
    fun scholarEditionCannotShipWithoutExplicitRedistributionEvidence() {
        val candidate = TafsirScholarWork(
            workId = "ibn-kathir-en",
            workTitle = "Tafsir Ibn Kathir",
            scholarName = "Ibn Kathir",
            language = ContentLanguage.ENGLISH,
            translatorName = "Unconfirmed translator",
            publisherOrDigitizer = "Unconfirmed publisher",
            sourceUrl = "https://example.org/edition",
            editionId = "edition-1",
            licenseName = "Unknown",
            redistributionPermissionEvidence = null,
            reviewStatus = ContentReviewStatus.LICENSE_CLEARED
        )
        assertFalse(candidate.canShipOffline())
    }

    @Test
    fun scholarEditionShipsOnlyAfterSourceLicenseAndReleaseReview() {
        val approved = TafsirScholarWork(
            workId = "sample-edition",
            workTitle = "Sample Tafsir",
            scholarName = "Sample Scholar",
            language = ContentLanguage.ARABIC,
            translatorName = null,
            publisherOrDigitizer = "Rights holder",
            sourceUrl = "https://example.org/edition",
            editionId = "sample-v1",
            licenseName = "CC BY 4.0",
            redistributionPermissionEvidence = "https://example.org/license",
            reviewStatus = ContentReviewStatus.VERIFIED_FOR_RELEASE
        )
        assertTrue(approved.canShipOffline())
    }
}
