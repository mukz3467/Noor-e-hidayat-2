package com.noorehidayat.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject

/**
 * Installs a reviewed JSON release bundle from assets/islamic-library/release.json.
 *
 * This importer deliberately rejects incomplete or unlicensed bundles before it
 * changes the database. The bundle is a build-time asset; no network/API key is
 * required at runtime. No bundle is shipped by this change until its texts and
 * redistribution permissions have been checked.
 */
internal object IslamicContentImporter {
    data class ImportResult(
        val quranAyahs: Int,
        val translationRecords: Int,
        val tafsirRecords: Int,
        val bukhariNarrations: Int,
        val muslimNarrations: Int
    )

    fun installBundledRelease(context: Context): ImportResult {
        val raw = context.assets.open(RELEASE_ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(raw)
        require(root.optInt("schemaVersion") == 1) { "Unsupported Islamic library release schema." }
        val releaseVersion = root.required("releaseVersion")
        val releasePrefs = context.getSharedPreferences("noor_islamic_release", Context.MODE_PRIVATE)
        if (releasePrefs.getString("installed_version", null) == releaseVersion) {
            val coverage = IslamicContentDatabase.get(context).getCoverage()
            return ImportResult(
                coverage.arabicAyahs,
                coverage.translationAyahsByLanguage.values.sum(),
                coverage.tafsirEntriesByEdition.values.sum(),
                coverage.bukhariNarrations,
                coverage.muslimNarrations
            )
        }

        val editions = root.getJSONArray("editions")
        val editionById = validateEditions(editions)
        val quran = root.getJSONArray("quranAyahs")
        validateQuran(quran, editionById)

        val translationEditions = editionById.values.filter { it.optString("contentType") == "translation" }
        // Arabic is the original Quran text, not a translation. Require one complete,
        // separately attributed edition for each requested translation language.
        require(setOf("en", "ur", "hi").all { language ->
            translationEditions.any { it.optString("languageCode") == language }
        }) {
            "Quran translations must include verified English, Urdu and Hindi editions."
        }

        val translations = root.optJSONArray("translations") ?: JSONArray()
        validateCompleteEditionRecords(translations, editionById, "translation")

        // Tafsir is a scholarly corpus, not a translation set. Classical works may
        // cover only selected ayahs; never invent missing entries or demand an
        // artificial minimum number of scholars/languages.
        val tafsirEditions = editionById.values.filter { it.optString("contentType") == "tafsir" }
        val tafsir = root.optJSONArray("tafsirEntries") ?: JSONArray()
        validateTafsirRecords(tafsir, editionById)
        require(tafsir.isEmpty() || tafsirEditions.isNotEmpty()) {
            "Tafsir entries require at least one separately attributed tafsir edition."
        }

        val collections = root.optJSONArray("hadithCollections") ?: JSONArray()
        val books = root.optJSONArray("hadithBooks") ?: JSONArray()
        val chapters = root.optJSONArray("hadithChapters") ?: JSONArray()
        val hadiths = root.optJSONArray("hadithRecords") ?: JSONArray()
        val hadithTranslations = root.optJSONArray("hadithTranslations") ?: JSONArray()
        validateHadiths(collections, books, chapters, hadiths, hadithTranslations, editionById)

        val db = IslamicContentDatabase.get(context).writableDatabase
        db.beginTransaction()
        val result: ImportResult
        try {
            clearReleaseTables(db)
            insertEditions(db, editions)
            insertQuran(db, quran)
            insertTranslations(db, translations)
            insertTafsir(db, tafsir)
            insertHadithCollections(db, collections)
            insertHadithBooks(db, books)
            insertHadithChapters(db, chapters)
            insertHadithRecords(db, hadiths)
            insertHadithTranslations(db, hadithTranslations)

            val bukhari = hadiths.countObjects { it.optString("collectionId") == "sahih_al_bukhari" }
            val muslim = hadiths.countObjects { it.optString("collectionId") == "sahih_al_muslim" }
            result = ImportResult(quran.length(), translations.length(), tafsir.length(), bukhari, muslim)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        releasePrefs.edit().putString("installed_version", releaseVersion).apply()
        return result
    }

    private fun validateEditions(editions: JSONArray): Map<String, JSONObject> {
        require(editions.length() > 0) { "Release contains no content editions." }
        val result = linkedMapOf<String, JSONObject>()
        for (index in 0 until editions.length()) {
            val edition = editions.getJSONObject(index)
            val id = edition.required("editionId")
            val language = edition.required("languageCode")
            require(language in SUPPORTED_LANGUAGES) { "Unsupported language '$language' in edition '$id'." }
            require(edition.required("contentType") in CONTENT_TYPES) { "Unsupported content type in edition '$id'." }
            require(edition.required("sourceUrl").startsWith("https://")) { "Edition '$id' needs an HTTPS source URL." }
            require(edition.required("licenseName").isNotBlank()) { "Edition '$id' has no license." }
            require(edition.required("licenseEvidenceUrl").startsWith("https://")) { "Edition '$id' needs a license evidence URL." }
            require(edition.required("publisher").isNotBlank()) { "Edition '$id' has no publisher/digitizer." }
            require(edition.required("workTitle").isNotBlank()) { "Edition '$id' has no work title." }
            require(edition.required("scholarName").isNotBlank()) { "Edition '$id' has no scholar/author." }
            require(edition.required("editionVersion").isNotBlank()) { "Edition '$id' has no edition version." }
            require(edition.required("sha256").matches(Regex("^[a-fA-F0-9]{64}$"))) { "Edition '$id' has an invalid SHA-256." }
            require(edition.required("reviewStatus") == VERIFIED) { "Edition '$id' has not passed release review." }
            val sectarianMetadataKeys = listOf(
                "sectLabel",
                "sectarianLabel",
                "sectarianGroup",
                "sectAffiliation",
                "denominationLabel",
                "groupIdentity"
            )
            require(sectarianMetadataKeys.none { key ->
                edition.optString(key).isNotBlank()
            }) {
                "Edition '$id' must not assign sectarian or denomination labels."
            }
            require(result.put(id, edition) == null) { "Duplicate edition ID '$id'." }
        }
        return result
    }

    private fun validateQuran(quran: JSONArray, editions: Map<String, JSONObject>) {
        require(quran.length() == EXPECTED_AYAHS) {
            "Quran release must contain exactly $EXPECTED_AYAHS ayahs; found ${quran.length()}."
        }
        val surahCounts = mutableMapOf<Int, Int>()
        val seen = mutableSetOf<String>()
        val quranEditionIds = mutableSetOf<String>()
        for (index in 0 until quran.length()) {
            val row = quran.getJSONObject(index)
            val surah = row.optInt("surahNumber")
            val ayah = row.optInt("ayahNumber")
            val key = "$surah:$ayah"
            require(surah in 1..114 && ayah > 0) { "Invalid Quran locator '$key'." }
            require(row.required("arabicText").isNotBlank()) { "Arabic text is blank at $key." }
            require(seen.add(key)) { "Duplicate Quran ayah '$key'." }
            val edition = editions[row.required("editionId")]
                ?: error("Quran ayah '$key' refers to an unknown edition.")
            require(edition.optString("contentType") == "quran_text") { "Quran ayah '$key' must use a quran_text edition." }
            quranEditionIds += row.required("editionId")
            surahCounts[surah] = (surahCounts[surah] ?: 0) + 1
        }
        require(quranEditionIds.size == 1) { "The Arabic Quran text must come from one pinned source edition." }
        require(surahCounts.size == 114) { "Quran release must include all 114 Surahs." }
        QuranDataService.allSurahs.forEach { surah ->
            require(surahCounts[surah.number] == surah.ayahs) {
                "Surah ${surah.number} is incomplete in the release."
            }
        }
        require(quran.getJSONObject(0).optInt("surahNumber") == 1 &&
            quran.getJSONObject(0).optInt("ayahNumber") == 1) {
            "Quran ayahs must be ordered from 1:1."
        }
    }

    private fun validateCompleteEditionRecords(
        records: JSONArray,
        editions: Map<String, JSONObject>,
        contentType: String
    ) {
        val counts = mutableMapOf<String, MutableSet<String>>()
        for (index in 0 until records.length()) {
            val row = records.getJSONObject(index)
            val editionId = row.required("editionId")
            val edition = editions[editionId] ?: error("Unknown edition '$editionId'.")
            require(edition.optString("contentType") == contentType) {
                "Edition '$editionId' is not a $contentType edition."
            }
            val surah = row.optInt("surahNumber")
            val ayah = row.optInt("ayahNumber")
            require(surah in 1..114 && ayah > 0) { "Invalid $contentType ayah locator $surah:$ayah." }
            require(row.required("text").isNotBlank()) { "Blank $contentType text at $surah:$ayah." }
            counts.getOrPut(editionId) { mutableSetOf() }.add("$surah:$ayah")
        }
        editions.forEach { (id, edition) ->
            if (edition.optString("contentType") == contentType) {
                require(counts[id]?.size == EXPECTED_AYAHS) {
                    "Edition '$id' must contain all $EXPECTED_AYAHS ayah entries for $contentType."
                }
            }
        }
    }

    private fun validateTafsirRecords(
        records: JSONArray,
        editions: Map<String, JSONObject>
    ) {
        val seen = mutableSetOf<String>()
        for (index in 0 until records.length()) {
            val row = records.getJSONObject(index)
            val editionId = row.required("editionId")
            val edition = editions[editionId] ?: error("Unknown tafsir edition '$editionId'.")
            require(edition.optString("contentType") == "tafsir") {
                "Edition '$editionId' is not a tafsir edition."
            }
            val surah = row.optInt("surahNumber")
            val ayah = row.optInt("ayahNumber")
            require(surah in 1..114 && ayah in 1..QuranDataService.allSurahs[surah - 1].ayahs) {
                "Invalid tafsir ayah locator $surah:$ayah."
            }
            require(row.required("text").isNotBlank()) { "Blank tafsir text at $surah:$ayah." }
            require(row.required("pageLocator").isNotBlank()) { "Tafsir page/locator is missing at $surah:$ayah." }
            require(row.required("sourceReference").isNotBlank()) { "Tafsir source reference is missing at $surah:$ayah." }
            require(seen.add("$editionId:$surah:$ayah")) {
                "Duplicate tafsir entry for edition '$editionId' at $surah:$ayah."
            }
        }
    }

    private fun validateHadiths(
        collections: JSONArray,
        books: JSONArray,
        chapters: JSONArray,
        hadiths: JSONArray,
        translations: JSONArray,
        editions: Map<String, JSONObject>
    ) {
        val collectionRows = (0 until collections.length()).map { collections.getJSONObject(it) }
        val collectionIds = collectionRows.map { it.required("collectionId") }
        require(collectionIds.toSet() == setOf("sahih_al_bukhari", "sahih_al_muslim") &&
            collectionIds.size == 2) {
            "Hadith release must contain exactly one Bukhari and one Muslim collection record."
        }
        val collectionById = collectionRows.associateBy { it.required("collectionId") }
        collectionRows.forEach { collection ->
            val id = collection.required("collectionId")
            val sourceEdition = editions[collection.required("sourceEditionId")]
                ?: error("Hadith collection '$id' references an unknown source edition.")
            require(sourceEdition.optString("contentType") == "hadith_source") {
                "Hadith collection '$id' must reference a hadith_source edition."
            }
            require(collection.required("numberingScheme").isNotBlank()) {
                "Hadith collection '$id' must declare its edition-specific numbering scheme."
            }
            require(collection.optInt("expectedBookCount") > 0 &&
                collection.optInt("expectedNarrationCount") > 0) {
                "Hadith collection '$id' must declare positive expected book and narration counts."
            }
        }

        val bookRows = (0 until books.length()).map { books.getJSONObject(it) }
        val bookById = bookRows.associateBy { it.required("bookId") }
        require(bookById.size == bookRows.size && bookRows.isNotEmpty()) {
            "Hadith book identifiers must be unique and both collections must include their books."
        }
        bookRows.forEach { book ->
            require(book.required("collectionId") in collectionById) {
                "Hadith book references an unknown collection."
            }
            require(book.required("bookNumber").isNotBlank() && book.required("bookTitle").isNotBlank()) {
                "Hadith book number and title are required."
            }
        }

        val chapterRows = (0 until chapters.length()).map { chapters.getJSONObject(it) }
        val chapterById = chapterRows.associateBy { it.required("chapterId") }
        require(chapterById.size == chapterRows.size) { "Hadith chapter identifiers must be unique." }
        chapterRows.forEach { chapter ->
            require(chapter.required("bookId") in bookById) {
                "Hadith chapter references an unknown book."
            }
            require(chapter.required("chapterTitle").isNotBlank()) {
                "Hadith chapter title is missing."
            }
        }

        val hadithRows = (0 until hadiths.length()).map { hadiths.getJSONObject(it) }
        require(hadithRows.isNotEmpty()) { "Hadith release contains no narrations." }
        val hadithIds = mutableSetOf<String>()
        val countsByCollection = mutableMapOf<String, Int>()
        val booksWithNarrations = mutableSetOf<String>()
        hadithRows.forEach { row ->
            val collectionId = row.required("collectionId")
            require(collectionId in collectionById) { "Unknown Hadith collection '$collectionId'." }
            val bookId = row.required("bookId")
            val book = bookById[bookId] ?: error("Hadith references an unknown book '$bookId'.")
            require(book.required("collectionId") == collectionId) {
                "Hadith collection does not match its referenced book."
            }
            val chapterId = row.optString("chapterId")
            if (chapterId.isNotBlank()) {
                val chapter = chapterById[chapterId] ?: error("Hadith references an unknown chapter '$chapterId'.")
                require(chapter.required("bookId") == bookId) {
                    "Hadith chapter does not belong to its referenced book."
                }
            }
            require(row.required("arabicText").isNotBlank()) { "Hadith Arabic text is blank." }
            require(row.required("sourceLocator").isNotBlank()) { "Hadith source locator is missing." }
            require(row.required("reviewStatus") == VERIFIED) { "Hadith record has not passed release review." }
            require(hadithIds.add(row.required("hadithId"))) { "Duplicate Hadith ID." }
            countsByCollection[collectionId] = (countsByCollection[collectionId] ?: 0) + 1
            booksWithNarrations += bookId
        }

        collectionRows.forEach { collection ->
            val id = collection.required("collectionId")
            val expectedBooks = collection.optInt("expectedBookCount")
            val actualBooks = bookRows.count { it.optString("collectionId") == id }
            val expectedNarrations = collection.optInt("expectedNarrationCount")
            val actualNarrations = countsByCollection[id] ?: 0
            require(actualBooks == expectedBooks) {
                "Collection '$id' has $actualBooks books; expected $expectedBooks for its pinned edition."
            }
            require(actualNarrations == expectedNarrations) {
                "Collection '$id' has $actualNarrations narrations; expected $expectedNarrations for its pinned edition."
            }
            require(bookRows.filter { it.optString("collectionId") == id }
                .all { booksWithNarrations.contains(it.required("bookId")) }) {
                "Collection '$id' contains a book with no narrations."
            }
        }

        val translationKeys = mutableSetOf<String>()
        for (index in 0 until translations.length()) {
            val row = translations.getJSONObject(index)
            val hadithId = row.required("hadithId")
            require(hadithId in hadithIds) {
                "Hadith translation references an unknown narration."
            }
            val editionId = row.required("editionId")
            val edition = editions[editionId] ?: error("Unknown Hadith translation edition.")
            require(edition.optString("contentType") == "hadith_translation") {
                "Hadith translation must reference a hadith_translation edition."
            }
            require(row.required("text").isNotBlank()) { "Hadith translation is blank." }
            require(translationKeys.add("$hadithId:$editionId")) {
                "Duplicate Hadith translation for narration '$hadithId' and edition '$editionId'."
            }
        }
    }

    private fun clearReleaseTables(db: SQLiteDatabase) {
        listOf(
            "hadith_translations", "hadith_records", "hadith_chapters", "hadith_books",
            "hadith_collections", "asbab_reports", "tafsir_entries", "quran_translations",
            "quran_ayahs", "content_editions"
        ).forEach { db.delete(it, null, null) }
    }

    private fun insertEditions(db: SQLiteDatabase, rows: JSONArray) {
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            db.insertOrThrow("content_editions", null, ContentValues().apply {
                put("edition_id", row.required("editionId"))
                put("content_type", row.required("contentType"))
                put("work_title", row.required("workTitle"))
                put("scholar_name", row.required("scholarName"))
                put("translator_name", row.optString("translatorName").takeIf { it.isNotBlank() })
                put("language_code", row.required("languageCode"))
                put("publisher", row.required("publisher"))
                put("source_url", row.required("sourceUrl"))
                put("license_name", row.required("licenseName"))
                put("license_evidence_url", row.required("licenseEvidenceUrl"))
                put("edition_version", row.required("editionVersion"))
                put("sha256", row.required("sha256"))
                put("review_status", VERIFIED)
            })
        }
    }

    private fun insertQuran(db: SQLiteDatabase, rows: JSONArray) {
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            db.insertOrThrow("quran_ayahs", null, ContentValues().apply {
                put("surah_number", row.optInt("surahNumber"))
                put("ayah_number", row.optInt("ayahNumber"))
                put("verse_key", row.required("surahNumber").toString() + ":" + row.required("ayahNumber"))
                put("arabic_text", row.required("arabicText"))
                put("edition_id", row.required("editionId"))
            })
        }
    }

    private fun insertTranslations(db: SQLiteDatabase, rows: JSONArray) {
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            db.insertOrThrow("quran_translations", null, ContentValues().apply {
                put("edition_id", row.required("editionId"))
                put("surah_number", row.optInt("surahNumber"))
                put("ayah_number", row.optInt("ayahNumber"))
                put("text", row.required("text"))
            })
        }
    }

    private fun insertTafsir(db: SQLiteDatabase, rows: JSONArray) {
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            db.insertOrThrow("tafsir_entries", null, ContentValues().apply {
                put("edition_id", row.required("editionId"))
                put("surah_number", row.optInt("surahNumber"))
                put("ayah_number", row.optInt("ayahNumber"))
                put("text", row.required("text"))
                put("volume", row.optString("volume").takeIf { it.isNotBlank() })
                put("page_locator", row.required("pageLocator"))
                put("source_reference", row.required("sourceReference"))
            })
        }
    }

    private fun insertHadithCollections(db: SQLiteDatabase, rows: JSONArray) {
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            db.insertOrThrow("hadith_collections", null, ContentValues().apply {
                put("collection_id", row.required("collectionId"))
                put("collection_title", row.required("collectionTitle"))
                put("source_edition_id", row.required("sourceEditionId"))
            })
        }
    }

    private fun insertHadithBooks(db: SQLiteDatabase, rows: JSONArray) {
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            db.insertOrThrow("hadith_books", null, ContentValues().apply {
                put("book_id", row.required("bookId"))
                put("collection_id", row.required("collectionId"))
                put("book_number", row.required("bookNumber"))
                put("book_title", row.required("bookTitle"))
            })
        }
    }

    private fun insertHadithChapters(db: SQLiteDatabase, rows: JSONArray) {
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            db.insertOrThrow("hadith_chapters", null, ContentValues().apply {
                put("chapter_id", row.required("chapterId"))
                put("book_id", row.required("bookId"))
                put("chapter_number", row.optString("chapterNumber").takeIf { it.isNotBlank() })
                put("chapter_title", row.required("chapterTitle"))
            })
        }
    }

    private fun insertHadithRecords(db: SQLiteDatabase, rows: JSONArray) {
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            db.insertOrThrow("hadith_records", null, ContentValues().apply {
                put("hadith_id", row.required("hadithId"))
                put("collection_id", row.required("collectionId"))
                put("book_id", row.required("bookId"))
                put("chapter_id", row.optString("chapterId").takeIf { it.isNotBlank() })
                put("narrator", row.optString("narrator").takeIf { it.isNotBlank() })
                put("arabic_text", row.required("arabicText"))
                put("source_locator", row.required("sourceLocator"))
                put("grading", row.optString("grading").takeIf { it.isNotBlank() })
                put("review_status", VERIFIED)
            })
        }
    }

    private fun insertHadithTranslations(db: SQLiteDatabase, rows: JSONArray) {
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            db.insertOrThrow("hadith_translations", null, ContentValues().apply {
                put("hadith_id", row.required("hadithId"))
                put("edition_id", row.required("editionId"))
                put("text", row.required("text"))
            })
        }
    }

    private fun JSONArray.countObjects(predicate: (JSONObject) -> Boolean): Int {
        var count = 0
        for (index in 0 until length()) if (predicate(getJSONObject(index))) count++
        return count
    }

    private fun JSONObject.required(key: String): String =
        optString(key).takeIf { it.isNotBlank() } ?: error("Required field '$key' is missing.")

    private const val RELEASE_ASSET = "islamic-library/release.json"
    private const val EXPECTED_AYAHS = 6236
    private const val VERIFIED = "VERIFIED_FOR_RELEASE"
    private val SUPPORTED_LANGUAGES = setOf("ar", "ur", "en", "hi")
    private val CONTENT_TYPES = setOf("quran_text", "translation", "tafsir", "hadith_source", "hadith_translation")
}
