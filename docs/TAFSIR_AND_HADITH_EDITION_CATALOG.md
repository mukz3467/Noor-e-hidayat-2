# Scholar edition catalogue and data-import gate

This catalogue records source discovery only. It does **not** approve any text for redistribution, certify a scholar's views, or prove that an edition is complete. No religious text may be generated or silently translated to fill a gap.

## User target

- Quran: all 114 surahs and 6,236 ayahs.
- At each ayah: five scholar-attributed translation/tafsir editions, selectable by language.
- App languages: Arabic, Urdu, English, Hindi.
- Hadith: complete Sahih al-Bukhari and Sahih Muslim, with book/chapter/narration identifiers.
- Quran and Hadith corpora must work offline after installation and must not depend on paid API keys.

## Quran.com catalogue snapshot

The public Quran.com tafsir catalogue currently lists 23 tafsir editions across languages: Arabic 9, English 3, Urdu 4, Bengali 4, and one each in Kurdish, Russian and Swahili. The displayed catalogue does not list a Hindi tafsir edition. See https://quran.com/ur/tafsirs.

### Five Arabic-language scholar/work candidates

These are five entries present in the Arabic catalogue, not a claim that all five have cleared redistribution rights:

| Work | Scholar / attribution | Catalogue language |
|---|---|---|
| Tafsir al-Qur'an al-'Azim | Ibn Kathir | Arabic |
| Jami' al-Bayan | al-Tabari | Arabic |
| Al-Jami' li-Ahkam al-Qur'an | al-Qurtubi | Arabic |
| Ma'alim at-Tanzil / Tafsir al-Baghawi | al-Baghawi | Arabic |
| Taysir al-Karim ar-Rahman / al-Sa'di | al-Sa'di | Arabic |

### Other language editions explicitly listed by Quran.com

- English: Tafsir Ibn Kathir; Ma'arif al-Qur'an (Mufti Muhammad Shafi); Tazkirul Quran (Maulana Wahiduddin Khan).
- Urdu: Bayan-ul-Quran (Dr. Israr Ahmad); Tazkirul Quran (Maulana Wahiduddin Khan); Fi Zilal al-Quran (Sayyid Qutb); Tafsir Ibn Kathir.
- Hindi: no tafsir edition is listed in this catalogue snapshot.

Important: the catalogue is not a complete survey of every publisher or library. It is only a reliable starting index. Hindi editions and additional editions must be researched separately, and no missing language may be filled with machine translation.

## Quran text

Tanzil's Uthmani Quran text is a candidate for the Arabic base text. Its published terms permit verbatim copying/distribution with attribution and a link to Tanzil, and explicitly prohibit changing the text. The exact version and checksum must be pinned before import. Source: https://tanzil.net/docs/Text_License.

## Hadith dataset candidate — not yet approved for bundling

https://github.com/Jaguar16/open-hadith-data documents structured data for Sahih al-Bukhari and Sahih Muslim, among other collections. Its README currently reports 7,252 Bukhari narrations / 97 books and 3,087 Muslim narrations / 56 books. These are dataset-reported counts, **not independently verified completeness counts**.

The repository states:
- Structured data: CC0 1.0.
- English translations: sourced from Sunnah.com; redistribution rights must be checked against Sunnah.com's terms.
- The extraction is automated and the maintainers do not guarantee completeness or correctness; they direct users to authenticated printed editions and qualified scholars.

Therefore this dataset may be used for schema/source investigation only until:
1. Each Arabic and translated text's edition provenance and redistribution rights are cleared.
2. The full book/chapter/narration structure is checked against a named printed edition.
3. Missing, duplicate, malformed, or edition-numbering differences are resolved.
4. A reviewer signs off on the imported corpus.

## Required release manifest for every edition

Each edition must record:
- stable edition ID and exact work title;
- original scholar/author;
- translator (if different from the author);
- language;
- publisher/digitizer and exact source URL;
- edition/version and locator convention;
- license name and explicit evidence permitting offline redistribution in this app;
- SHA-256 checksum of the imported file;
- review status and reviewer/date.

The app's `canShipOffline` / `isDisplayable` gates must remain false until all required metadata and release review are present.

## Current implementation status

- Source-aware Kotlin records have been added for scholar works, Hadith translation editions and Quran corpus coverage.
- No complete tafsir, translation, Bukhari or Muslim dataset has yet been imported into the app.
- The current Hadith screen still contains a small selected list; it is not the complete two collections.
- The current Quran service still fetches editions from AlQuranCloud at runtime and caches them only after download; this is not a prebundled offline Quran corpus.
- The existing Tafseer screen fetches the AlQuranCloud tafsir catalogue and verse text at runtime; it is not yet a local multi-scholar, four-language tafsir library.

Do not describe Noor-e-Hidayat as containing the complete requested library or as fully offline until the datasets, licensing, integrity checks, airplane-mode tests and Android build all pass.
