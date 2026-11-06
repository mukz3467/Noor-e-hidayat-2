package com.noorehidayat.app

/**
 * Source-aware content contracts for bundled Quran, tafsir, asbab al-nuzul and Hadith editions.
 *
 * These are data models only: an edition must not be displayed as verified until its
 * source, redistribution license and imported records have passed the content validation gate.
 */
enum class ContentLanguage(val code: String) {
    ARABIC("ar"),
    ENGLISH("en"),
    URDU("ur"),
    HINDI("hi")
}

enum class ContentReviewStatus {
    UNVERIFIED,
    SOURCE_CHECKED,
    LICENSE_CLEARED,
    VERIFIED_FOR_RELEASE
}

data class ContentSourceEdition(
    val id: String,
    val title: String,
    val authorOrPublisher: String,
    val language: ContentLanguage,
    val sourceUrl: String,
    val licenseName: String,
    val requiredAttribution: String,
    val version: String,
    val checksumSha256: String,
    val reviewStatus: ContentReviewStatus = ContentReviewStatus.UNVERIFIED
) {
    fun canShipInOfflineApp(): Boolean =
        id.isNotBlank() &&
            title.isNotBlank() &&
            authorOrPublisher.isNotBlank() &&
            sourceUrl.startsWith("https://") &&
            licenseName.isNotBlank() &&
            requiredAttribution.isNotBlank() &&
            version.isNotBlank() &&
            checksumSha256.matches(Regex("^[a-fA-F0-9]{64}$")) &&
            reviewStatus == ContentReviewStatus.VERIFIED_FOR_RELEASE
}

data class AyahTranslationEdition(
    val editionId: String,
    val language: ContentLanguage,
    val text: String
)

data class AyahTafsirEntry(
    val surahNumber: Int,
    val ayahNumber: Int,
    val editionId: String,
    val language: ContentLanguage,
    val workTitle: String,
    val author: String,
    val translator: String?,
    val text: String,
    val volume: String?,
    val pageOrLocator: String,
    val sourceReference: String,
    val reviewStatus: ContentReviewStatus
) {
    fun isDisplayable(): Boolean =
        surahNumber in 1..114 &&
            ayahNumber > 0 &&
            editionId.isNotBlank() &&
            workTitle.isNotBlank() &&
            author.isNotBlank() &&
            text.isNotBlank() &&
            pageOrLocator.isNotBlank() &&
            sourceReference.isNotBlank() &&
            reviewStatus == ContentReviewStatus.VERIFIED_FOR_RELEASE
}

/**
 * An occasion-of-revelation report is optional and must be tied to a specific source.
 * Absence of a report is not an error and must never be filled with an inferred story.
 */
data class AsbabAlNuzulReport(
    val surahNumber: Int,
    val ayahNumber: Int,
    val reportText: String,
    val language: ContentLanguage,
    val primarySource: String,
    val sourceLocator: String,
    val grading: String?,
    val gradingAuthority: String?,
    val editionId: String,
    val reviewStatus: ContentReviewStatus
) {
    fun isDisplayable(): Boolean =
        surahNumber in 1..114 &&
            ayahNumber > 0 &&
            reportText.isNotBlank() &&
            primarySource.isNotBlank() &&
            sourceLocator.isNotBlank() &&
            editionId.isNotBlank() &&
            reviewStatus == ContentReviewStatus.VERIFIED_FOR_RELEASE
}

data class HadithCollectionEdition(
    val collectionId: String,
    val collectionTitle: String,
    val bookId: String,
    val bookTitle: String,
    val chapterId: String?,
    val chapterTitle: String?,
    val hadithId: String,
    val narrator: String?,
    val arabicText: String,
    val translations: List<AyahTranslationEdition>,
    val sourceEditionId: String,
    val grading: String?,
    val sourceLocator: String,
    val reviewStatus: ContentReviewStatus
) {
    fun isDisplayable(): Boolean =
        collectionId in setOf("sahih_al_bukhari", "sahih_muslim") &&
            collectionTitle.isNotBlank() &&
            bookId.isNotBlank() &&
            bookTitle.isNotBlank() &&
            hadithId.isNotBlank() &&
            arabicText.isNotBlank() &&
            sourceEditionId.isNotBlank() &&
            sourceLocator.isNotBlank() &&
            reviewStatus == ContentReviewStatus.VERIFIED_FOR_RELEASE
}


/**
 * A scholarly work is not a translation edition. Keep author, translator, language,
 * and publication rights separate so the app never attributes a translator's wording
 * to the original mufassir.
 */
data class TafsirScholarWork(
    val workId: String,
    val workTitle: String,
    val scholarName: String,
    val language: ContentLanguage,
    val translatorName: String?,
    val publisherOrDigitizer: String,
    val sourceUrl: String,
    val editionId: String,
    val licenseName: String,
    val redistributionPermissionEvidence: String?,
    val reviewStatus: ContentReviewStatus
) {
    fun canShipOffline(): Boolean =
        workId.isNotBlank() &&
            workTitle.isNotBlank() &&
            scholarName.isNotBlank() &&
            publisherOrDigitizer.isNotBlank() &&
            sourceUrl.startsWith("https://") &&
            editionId.isNotBlank() &&
            licenseName.isNotBlank() &&
            !redistributionPermissionEvidence.isNullOrBlank() &&
            reviewStatus == ContentReviewStatus.VERIFIED_FOR_RELEASE
}

/** Translation metadata is attached to the translated edition, not inferred from its author. */
data class HadithTranslationEdition(
    val editionId: String,
    val language: ContentLanguage,
    val translatorName: String,
    val publisherOrDigitizer: String,
    val sourceUrl: String,
    val licenseName: String,
    val redistributionPermissionEvidence: String?,
    val reviewStatus: ContentReviewStatus
) {
    fun canShipOffline(): Boolean =
        editionId.isNotBlank() &&
            translatorName.isNotBlank() &&
            publisherOrDigitizer.isNotBlank() &&
            sourceUrl.startsWith("https://") &&
            licenseName.isNotBlank() &&
            !redistributionPermissionEvidence.isNullOrBlank() &&
            reviewStatus == ContentReviewStatus.VERIFIED_FOR_RELEASE
}

/**
 * A release manifest makes missing language coverage explicit. A scholar's original
 * Arabic work must not be presented as an Urdu, English, or Hindi translation.
 */
data class QuranCorpusCoverage(
    val totalSurahs: Int,
    val totalAyahs: Int,
    val arabicAyahs: Int,
    val translationsByLanguage: Map<ContentLanguage, Int>,
    val tafsirEntriesByEdition: Map<String, Int>,
    val approvedEditions: Set<String>
) {
    fun hasCompleteArabicQuran(): Boolean =
        totalSurahs == 114 && totalAyahs == 6236 && arabicAyahs == 6236

    fun hasCompleteTranslation(language: ContentLanguage): Boolean =
        translationsByLanguage[language] == totalAyahs
}
