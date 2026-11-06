package com.noorehidayat.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Local, source-aware storage for the Quran and Hadith library.
 *
 * The schema intentionally stores edition and rights metadata beside the text.
 * An empty database is not a claim that the corpus has been imported. Only records
 * whose edition is VERIFIED_FOR_RELEASE are returned to reader-facing queries.
 */
internal class IslamicContentDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    data class TafsirEdition(
        val id: String,
        val title: String,
        val scholar: String,
        val translator: String?,
        val language: String
    )

    data class TafsirAyahText(val ayahNumber: Int, val text: String)

    data class HadithRecord(
        val id: String,
        val collectionId: String,
        val bookTitle: String,
        val chapterTitle: String?,
        val narrator: String?,
        val arabicText: String,
        val sourceLocator: String,
        val translation: String?
    )

    data class Coverage(
        val arabicAyahs: Int,
        val translationAyahsByLanguage: Map<String, Int>,
        val tafsirEntriesByEdition: Map<String, Int>,
        val bukhariNarrations: Int,
        val muslimNarrations: Int
    ) {
        val hasCompleteArabicQuran: Boolean get() = arabicAyahs == EXPECTED_AYAHS
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE content_editions (
                edition_id TEXT PRIMARY KEY NOT NULL,
                content_type TEXT NOT NULL,
                work_title TEXT NOT NULL,
                scholar_name TEXT NOT NULL,
                translator_name TEXT,
                language_code TEXT NOT NULL,
                publisher TEXT NOT NULL,
                source_url TEXT NOT NULL,
                license_name TEXT NOT NULL,
                license_evidence_url TEXT NOT NULL,
                edition_version TEXT NOT NULL,
                sha256 TEXT NOT NULL,
                review_status TEXT NOT NULL
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE quran_ayahs (
                surah_number INTEGER NOT NULL,
                ayah_number INTEGER NOT NULL,
                verse_key TEXT NOT NULL UNIQUE,
                arabic_text TEXT NOT NULL,
                PRIMARY KEY(surah_number, ayah_number)
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE quran_translations (
                edition_id TEXT NOT NULL,
                surah_number INTEGER NOT NULL,
                ayah_number INTEGER NOT NULL,
                text TEXT NOT NULL,
                PRIMARY KEY(edition_id, surah_number, ayah_number),
                FOREIGN KEY(edition_id) REFERENCES content_editions(edition_id)
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE tafsir_entries (
                edition_id TEXT NOT NULL,
                surah_number INTEGER NOT NULL,
                ayah_number INTEGER NOT NULL,
                text TEXT NOT NULL,
                volume TEXT,
                page_locator TEXT NOT NULL,
                source_reference TEXT NOT NULL,
                PRIMARY KEY(edition_id, surah_number, ayah_number),
                FOREIGN KEY(edition_id) REFERENCES content_editions(edition_id)
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE asbab_reports (
                report_id TEXT PRIMARY KEY NOT NULL,
                surah_number INTEGER NOT NULL,
                ayah_number INTEGER NOT NULL,
                language_code TEXT NOT NULL,
                report_text TEXT NOT NULL,
                primary_source TEXT NOT NULL,
                source_locator TEXT NOT NULL,
                grading TEXT,
                grading_authority TEXT,
                edition_id TEXT NOT NULL,
                review_status TEXT NOT NULL
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE hadith_collections (
                collection_id TEXT PRIMARY KEY NOT NULL,
                collection_title TEXT NOT NULL,
                source_edition_id TEXT NOT NULL
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE hadith_books (
                book_id TEXT PRIMARY KEY NOT NULL,
                collection_id TEXT NOT NULL,
                book_number TEXT NOT NULL,
                book_title TEXT NOT NULL
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE hadith_chapters (
                chapter_id TEXT PRIMARY KEY NOT NULL,
                book_id TEXT NOT NULL,
                chapter_number TEXT,
                chapter_title TEXT NOT NULL
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE hadith_records (
                hadith_id TEXT PRIMARY KEY NOT NULL,
                collection_id TEXT NOT NULL,
                book_id TEXT NOT NULL,
                chapter_id TEXT,
                narrator TEXT,
                arabic_text TEXT NOT NULL,
                source_locator TEXT NOT NULL,
                grading TEXT,
                review_status TEXT NOT NULL
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE hadith_translations (
                hadith_id TEXT NOT NULL,
                edition_id TEXT NOT NULL,
                text TEXT NOT NULL,
                PRIMARY KEY(hadith_id, edition_id)
            )""".trimIndent()
        )
        db.execSQL("CREATE INDEX idx_tafsir_surah_ayah ON tafsir_entries(surah_number, ayah_number)")
        db.execSQL("CREATE INDEX idx_hadith_collection_book ON hadith_records(collection_id, book_id)")
        db.execSQL("CREATE INDEX idx_translation_language ON content_editions(language_code, content_type)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Never silently discard religious content during an upgrade.
        // Future schema changes must use explicit, forward-only migrations.
        if (oldVersion != newVersion) {
            throw IllegalStateException("Unsupported Islamic content database migration: $oldVersion -> $newVersion")
        }
    }

    fun getTafsirEditions(languageCode: String? = null): List<TafsirEdition> {
        val sql = buildString {
            append(
                """SELECT edition_id, work_title, scholar_name, translator_name, language_code
                   FROM content_editions
                   WHERE content_type = 'tafsir' AND review_status = 'VERIFIED_FOR_RELEASE'"""
            )
            if (!languageCode.isNullOrBlank()) append(" AND language_code = ?")
            append(" ORDER BY scholar_name, work_title")
        }
        val args = if (languageCode.isNullOrBlank()) null else arrayOf(languageCode)
        return readableDatabase.rawQuery(sql, args).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        TafsirEdition(
                            id = cursor.getString(0),
                            title = cursor.getString(1),
                            scholar = cursor.getString(2),
                            translator = cursor.getString(3),
                            language = cursor.getString(4)
                        )
                    )
                }
            }
        }
    }

    fun getTafsirForSurah(surahNumber: Int, editionId: String): List<TafsirAyahText> =
        readableDatabase.rawQuery(
            """SELECT t.ayah_number, t.text
               FROM tafsir_entries t
               JOIN content_editions e ON e.edition_id = t.edition_id
               WHERE t.surah_number = ? AND t.edition_id = ?
                 AND e.review_status = 'VERIFIED_FOR_RELEASE'
               ORDER BY t.ayah_number""",
            arrayOf(surahNumber.toString(), editionId)
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(TafsirAyahText(cursor.getInt(0), cursor.getString(1)))
            }
        }

    fun getVerifiedHadiths(collectionId: String? = null, languageCode: String? = null): List<HadithRecord> {
        val where = StringBuilder("h.review_status = 'VERIFIED_FOR_RELEASE'")
        val args = mutableListOf<String>()
        if (!collectionId.isNullOrBlank()) {
            where.append(" AND h.collection_id = ?")
            args += collectionId
        }
        if (!languageCode.isNullOrBlank()) {
            where.append(" AND te.language_code = ?")
            args += languageCode
        }
        val sql =
            """SELECT h.hadith_id, h.collection_id, b.book_title, c.chapter_title,
                      h.narrator, h.arabic_text, h.source_locator, ht.text
               FROM hadith_records h
               JOIN hadith_books b ON b.book_id = h.book_id
               LEFT JOIN hadith_chapters c ON c.chapter_id = h.chapter_id
               LEFT JOIN hadith_translations ht ON ht.hadith_id = h.hadith_id
               LEFT JOIN content_editions te ON te.edition_id = ht.edition_id
               WHERE $where
               ORDER BY h.collection_id, b.book_number, h.hadith_id"""
        return readableDatabase.rawQuery(sql, args.toTypedArray().takeIf { it.isNotEmpty() }).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        HadithRecord(
                            id = cursor.getString(0),
                            collectionId = cursor.getString(1),
                            bookTitle = cursor.getString(2),
                            chapterTitle = cursor.getString(3),
                            narrator = cursor.getString(4),
                            arabicText = cursor.getString(5),
                            sourceLocator = cursor.getString(6),
                            translation = cursor.getString(7)
                        )
                    )
                }
            }
        }
    }

    fun getCoverage(): Coverage {
        val db = readableDatabase
        val arabicCount = scalarCount(db, "SELECT COUNT(*) FROM quran_ayahs")
        val translations = mutableMapOf<String, Int>()
        db.rawQuery(
            """SELECT e.language_code, COUNT(*)
               FROM quran_translations t JOIN content_editions e ON e.edition_id = t.edition_id
               WHERE e.review_status = 'VERIFIED_FOR_RELEASE'
               GROUP BY e.language_code""",
            null
        ).use { c -> while (c.moveToNext()) translations[c.getString(0)] = c.getInt(1) }

        val tafsir = mutableMapOf<String, Int>()
        db.rawQuery(
            """SELECT edition_id, COUNT(*) FROM tafsir_entries
               GROUP BY edition_id""",
            null
        ).use { c -> while (c.moveToNext()) tafsir[c.getString(0)] = c.getInt(1) }

        return Coverage(
            arabicAyahs = arabicCount,
            translationAyahsByLanguage = translations,
            tafsirEntriesByEdition = tafsir,
            bukhariNarrations = scalarCount(
                db,
                "SELECT COUNT(*) FROM hadith_records WHERE collection_id = 'sahih_al_bukhari' AND review_status = 'VERIFIED_FOR_RELEASE'"
            ),
            muslimNarrations = scalarCount(
                db,
                "SELECT COUNT(*) FROM hadith_records WHERE collection_id = 'sahih_muslim' AND review_status = 'VERIFIED_FOR_RELEASE'"
            )
        )
    }

    private fun scalarCount(db: SQLiteDatabase, sql: String): Int =
        db.rawQuery(sql, null).use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }

    companion object {
        private const val DATABASE_NAME = "noor_islamic_library.db"
        private const val DATABASE_VERSION = 1
        private const val EXPECTED_AYAHS = 6236

        @Volatile private var instance: IslamicContentDatabase? = null

        fun get(context: Context): IslamicContentDatabase =
            instance ?: synchronized(this) {
                instance ?: IslamicContentDatabase(context).also { instance = it }
            }
    }
}
