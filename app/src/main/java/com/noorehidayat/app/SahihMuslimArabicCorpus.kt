package com.noorehidayat.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Offline Arabic source corpus for Sahih Muslim.
 *
 * Source: i-muslim bulk export of the classical Arabic edition, sourced from
 * fawazahmed0/hadith-api. The checked-in corpus preserves upstream source numbers,
 * Arabic numbers and book numbers. Empty upstream placeholders are intentionally
 * omitted and are never renumbered or represented as narrations.
 *
 * The export contains 7,563 numbered source rows, of which 7,360 have Arabic text
 * and 203 are empty placeholders. No English, Urdu or Hindi text is fabricated.
 */
internal object SahihMuslimArabicCorpus {
    private const val ASSET_DIRECTORY = "islamic-library/sahih-muslim-ar"
    private const val EXPECTED_NARRATIONS = 7_360

    fun load(context: Context): List<HadithItem> {
        val partNames = context.assets.list(ASSET_DIRECTORY)
            .orEmpty()
            .filter { it.startsWith("part-") && it.endsWith(".json") }
            .sorted()
        require(partNames.size == 15) {
            "Sahih Muslim Arabic corpus is incomplete: expected 15 data parts, found ${partNames.size}."
        }

        val records = ArrayList<HadithItem>(EXPECTED_NARRATIONS)
        val seenNumbers = HashSet<Int>(EXPECTED_NARRATIONS)
        partNames.forEach { partName ->
            val json = context.assets.open("$ASSET_DIRECTORY/$partName")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val rows = JSONArray(json)
            for (index in 0 until rows.length()) {
                val row: JSONObject = rows.getJSONObject(index)
                val number = row.getInt("number")
                val arabic = row.getString("text").trim()
                require(number > 0 && arabic.isNotEmpty()) {
                    "Invalid Sahih Muslim source row in $partName at index $index."
                }
                require(seenNumbers.add(number)) {
                    "Duplicate Sahih Muslim source number $number."
                }
                val bookNumber = row.optInt("bookNumber", 0)
                val arabicNumber = row.opt("arabicNumber")
                    ?.toString()
                    ?.takeIf { it.isNotBlank() && it != "null" }
                records += HadithItem(
                    id = number.toString(),
                    book = "Sahih Muslim",
                    chapter = if (bookNumber > 0) "Book number $bookNumber" else "Book not specified in source",
                    hadithNumber = "Source no. ${arabicNumber ?: number}",
                    narrator = "",
                    arabic = arabic,
                    english = "",
                    urdu = "",
                    topic = "Sahih Muslim · Arabic original",
                    referenceUrl = "",
                    hindi = ""
                )
            }
        }
        require(records.size == EXPECTED_NARRATIONS) {
            "Sahih Muslim Arabic corpus has ${records.size} narrations; expected $EXPECTED_NARRATIONS."
        }
        return records
    }
}
