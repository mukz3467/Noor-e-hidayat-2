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

## Source review update (2026-11-06)

This update records what the published source documentation actually establishes. It is not a scholarly approval of any imported text.

### Quran Arabic

- Tanzil publishes its Uthmani Quran text as CC BY 3.0 and permits verbatim redistribution in apps when the source is credited, a link to Tanzil is provided, and its copyright notice is retained.
- Tanzil's terms explicitly prohibit changing the text. Any import must therefore preserve the source text exactly; normalization, diacritic removal, Basmala rewriting, or other textual transformations must not be applied to the distributed text.
- Candidate: Tanzil Uthmani v1.1. Before release, pin the original downloaded file, compute its SHA-256, independently validate all 114 surahs / 6,236 ayahs and preserve the required notice.

Primary terms: https://tanzil.net/docs/Text_License
Official download page: https://tanzil.net/download/

### Quran translations and Tafsir

- UmmahLibrary's published per-dataset attribution identifies several translation sources, but explicitly warns that copyright varies by translation and that each upstream license must be confirmed before redistribution. It also says Saheeh International was excluded because its license does not permit free redistribution under that project's policy.
- The same attribution lists Tafsir al-Muyassar, al-Tabari, Ibn Kathir and Ma'arif-ul-Qur'an as content plugins while stating that the works remain copyright of their authors/publishers. The existence of a plugin or downloadable/API text is not proof that an offline copy may be redistributed.
- No five-edition, four-language Tafsir corpus is approved for bundling by this review. No five-edition translation bundle is approved either. Urdu/Hindi availability must not be inferred from a catalogue listing.
- QUL resource licensing varies by resource. Every selected resource needs its own license evidence and redistribution permission before inclusion.

Reference attribution register: https://github.com/UmmahLibrary/ummah-library/blob/main/packages/data/ATTRIBUTION.md
QUL documentation: https://qul.tarteel.ai/

### Sahih al-Bukhari and Sahih Muslim

- i-muslim's public download documentation states that its Arabic classical Hadith texts are public-domain text and lists 7,580 Bukhari Arabic entries and 7,360 Muslim Arabic entries. It distinguishes these from third-party non-Arabic translations: most mirrored translations are metadata-only because redistribution permission is unclear; only the rows authored by i-muslim are CC0.
- Those published counts are source-reported counts, not proof that the export covers every book/chapter/narration in a specific printed edition. In particular, the Muslim count must be reconciled with the chosen edition's numbering and repeated/sub-numbered reports before it can be described as complete.
- The Hadith Project reports 7,580 Bukhari narrations in 97 books using Muhammad Fu'ad Abd al-Baqi numbering. Its page documents multiple upstream sources and explains that 187 Bukhari rows were unmatched in one alignment process. This is useful provenance work, but its stated data sources include ODbL 1.0 / DbCL 1.0 material; the exact database/content terms and attribution must be carried through before reuse. Its public documentation does not establish a complete Muslim corpus in the same release.
- Therefore neither a complete Bukhari+Muslim offline release nor four-language Hadith translations are approved yet. Do not copy English text from Sunnah.com mirrors: i-muslim identifies those mirrored translations as proprietary and serves them as metadata only when no open license grant is on file.

References:
- i-muslim source and license model: https://i-muslim.com/id/downloads
- The Hadith Project Bukhari provenance and numbering: https://www.thehadithproject.com/collection/bukhari/
- The Hadith Project data blueprint and licensing: https://www.thehadithproject.com/blueprint/

### Release decision

The current importer/database/UI work remains infrastructure only. No placeholder corpus, machine-translated religious text, scraped third-party translation, or guessed attribution may be used to satisfy a count requirement. The next data import may proceed only for an individually approved, version-pinned source whose exact text, coverage, numbering, license and required attribution have been checked. Where no verified text exists in a language, the app must show that language as unavailable rather than substitute English, Urdu, Hindi or Arabic silently.

## Neutral Islamic editorial standard (user requirement)

Noor-e-Hidayat is an Islamic learning and reference app, not a sect, denomination, movement, madhhab or organisation's platform. It must not assign a sectarian identity to the app, its users, the Prophet's teachings, Quran verses, Hadith collections, narrators, or scholars.

### Display rules

- Present the Quran and the named source editions of Sahih al-Bukhari and Sahih Muslim as source texts with exact citations. Do not add sectarian ownership labels.
- Do not add sectarian filters, sect-based rankings, takfir, polemics, disparaging descriptions, or claims that one present-day group alone represents all Muslims.
- Do not label a narrator, scholar, translation or tafsir as belonging to a sect unless a historically necessary fact is directly relevant, reliably sourced, and explained neutrally. Such labels are not required for the core reader and should normally be omitted.
- Attribute every tafsir, translation, explanation, grading and scholarly view to the actual named author/editor/source. Never imply that one scholar's interpretation is the universal Muslim position.
- Where reliable scholars differ, describe the disagreement with named sources and neutral language. Do not manufacture a consensus or decide the dispute in the app's own voice.
- Separate primary source text from translation, commentary, editorial notes and user-interface copy. Preserve primary texts verbatim.
- Do not add devotional slogans, polemical framing, or sect-specific rulings to source records.
- If a question requires a religious ruling, the app should direct the reader to a qualified scholar rather than issuing an unattributed ruling.

### Data and review gate

Each release reviewer must check not only text accuracy and licensing but also that edition metadata contains no sectarian classification or polemical tags. Any necessary historical context must be cited and worded descriptively, not as endorsement. A neutral tone does not mean suppressing a source's own wording: quote the source faithfully, attribute it, and distinguish it from the app's editorial voice.

This policy does not certify that every user or scholar agrees with every source. It defines how the app presents sourced material without defining or attacking any Muslim community.
