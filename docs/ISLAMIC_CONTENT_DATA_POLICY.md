# Islamic content data policy and offline implementation plan

## Non-negotiable accuracy rules

1. Never generate, paraphrase, or machine-translate Quran text, tafsir, asbab al-nuzul, or Hadith and label it as an authenticated source translation.
2. Keep Quran Arabic text separate from translations and tafsir.
3. An asbab al-nuzul entry must include the cited classical source and the report's hadith/athar grading where available. If no specific, reliably sourced report is established for an ayah, show: "No specific verified occasion of revelation is recorded in this dataset." Do not infer a cause from the surah's general theme.
4. Every Hadith record must preserve collection, book/chapter, hadith identifier, Arabic text, translator/source attribution, and any grading metadata. Do not treat numbering systems from different editions as interchangeable.
5. Show an explicit unavailable state for a language/edition that has no verified licensed translation. Never silently substitute another language.
6. Keep content data and app interface translations as separate layers.

## User requirements

- Languages: Urdu, English, Arabic, Hindi.
- No API keys, paid APIs, or runtime network dependency for Quran, tafsir, asbab al-nuzul, or Hadith content.
- Offline access after installation; content should ship as bundled assets or a versioned local database.
- Preserve the existing app navigation and emerald/gold visual theme.

## Current repository audit (branch: fix/islamic-data-accuracy)

- QuranDataService currently requests Quran editions from api.alquran.cloud at runtime and only caches a surah after the request succeeds.
- Quran parsing currently requires Arabic, English and Urdu; Hindi is optional. The Arabic edition ar.muyassar is stored in a field called arabicTafseer, but this is not a detailed per-ayah tafsir corpus.
- revelationContext is currently empty. The source code explicitly says unsourced asbab al-nuzul summaries were removed.
- HadithLibraryScreen currently contains seven hard-coded selected entries across several collections. It is not a complete Sahih al-Bukhari or Sahih Muslim library.
- Therefore the current app must not be described as fully offline or as containing complete tafsir/asbab al-nuzul or complete Bukhari/Muslim.

## Candidate data sources and restrictions to verify before bundling

### Quran text and translations
- Tanzil Quran text is a candidate for a bundled Quran text asset. Its published terms require attribution and prohibit modifying the Quran text. Verify the exact edition, checksum, attribution requirements, and current license before packaging.
- Translation editions require their own per-edition license review. Availability in a public API or repository does not automatically grant redistribution rights.
- Do not claim four-language coverage until each bundled translation has a verified source, exact edition identifier, attribution, and redistribution permission.

### Tafsir and occasions of revelation
- Tafsir must be treated as a separate corpus from translation. Record author/work, language, edition/translator, volume/page or ayah locator, source URL, license, and editorial status.
- Asbab al-nuzul reports must be represented as reports with citations and grading, not as a mandatory text field for every ayah.
- No corpus is approved by this document. A candidate source is not an authenticity or licensing approval.

### Sahih al-Bukhari and Sahih Muslim
- The public GitHub project Jaguar16/open-hadith-data is a candidate for structured data discovery, not an approved final content source. Its own documentation warns that the extraction may be incomplete or incorrect. The dataset license for structured data does not automatically clear third-party English translations contained in the export.
- Before bundling any translation, verify its copyright/redistribution terms. Preserve collection/book/chapter/numbering and source attribution. Cross-check completeness against a trusted edition.
- Do not infer that an entire collection is present from the presence of some records or a book count in a README.

## Proposed local data model

Use stable, explicit identifiers rather than relying on display strings:

- QuranAyah: surahNumber, ayahNumber, arabicUthmani, translationsByLanguage, sourceEditionIds.
- TafsirEntry: surahNumber, ayahNumber, language, workTitle, author, translator, text, volume, page, sourceReference, licenseId, reviewStatus.
- AsbabReport: surahNumber, ayahNumber, reportText, language, primarySource, locator, grading, gradingAuthority, sourceEditionId, reviewStatus.
- HadithRecord: collectionId, bookId, chapterId, hadithId, narrator, arabicText, translationsByLanguage, translator, grading, sourceEditionId.
- ContentEdition: editionId, publisherOrAuthor, language, license, attribution, sourceUrl, version, checksum, verifiedAt.

All records should be searchable offline. Database import must validate expected record counts, required fields, unique identifiers, language tags, and source/license metadata before an edition is marked available.

## Delivery gates

1. Approve and document source + redistribution license for each dataset.
2. Import complete Quran text and licensed translations; verify 114 surahs and 6,236 ayahs.
3. Import detailed tafsir editions independently; show only languages that actually exist in the approved corpus.
4. Import asbab reports with source and grading; do not fabricate coverage for every ayah.
5. Import and cross-check every book/chapter/narration for Bukhari and Muslim; document edition-specific numbering.
6. Package the approved data into local assets/Room database; test airplane mode on a clean install.
7. Run automated integrity tests and Android build before calling the release complete.

## Current status

This file is an implementation and verification contract, not evidence that the datasets have already been bundled. The content corpus remains incomplete until the delivery gates above pass.
