# Islamic library dataset source register

This file records source availability and release decisions for the Noor-e-Hidayat offline library. A source being downloadable does not by itself mean that its text has been imported, checked for completeness, or approved for redistribution.

## Required language codes

- Arabic: `ar` — original Arabic text.
- Urdu: `ur`
- English: `en`
- Hindi: `hi`

Missing translations must remain explicitly unavailable. Do not machine-translate religious texts or silently substitute another language.

## Quran text and translations

### Quranpedia versioned dumps

- Catalog: https://api.quranpedia.net/dumps?lang=en
- API documentation and usage policy: https://api.quranpedia.net/
- Current catalog version observed during source review: `2026-11-05`.
- The catalog lists a Hafs Arabic mushaf dump (`mushafs-1.json.gz`, approximately 394 KB).
- The translation catalog lists Urdu translation file `1966.json` (approximately 6.7 MB), Hindi translation file `1986.json` (approximately 3.9 MB), and several English translation files, including Pickthall (`13604.json`) and Yusuf Ali (`13605.json`).
- Quranpedia states that each file carries its own license metadata. Its policy distinguishes use inside an app from republishing the dataset as a downloadable database. Check each selected translation file's embedded license and attribution before release.
- Do not treat the catalog listing alone as a redistribution grant.

## Tafsir and Asbab al-Nuzul

Quranpedia's official dump catalog lists 149 tafsir book files and two Asbab al-Nuzul books. Examples include:

- Tafsir al-Muyassar: `tafsir-book-2012.json.gz`.
- Tafsir al-Jalalayn: `tafsir-book-272.json.gz`.
- Asbab al-Nuzul by al-Wahidi: `asbab-book-2919.json.gz`.
- Al-Muharrar fi Asbab al-Nuzul: `asbab-book-460.json.gz`.

The selected book's metadata must be read from the official books index and its embedded license must be checked. Tafsir coverage is naturally edition-specific and may be partial; validate real ayah locators rather than requiring every work to contain exactly 6,236 entries. An Asbab report must retain its original source and locator; absence of a report is not a defect.

## Sahih al-Bukhari and Sahih Muslim

### i-muslim open downloads

- Source and license counts: https://i-muslim.com/id/downloads
- Developer documentation: https://i-muslim.com/ur/developers
- The source currently lists 7,580 Arabic Bukhari records and 7,360 Arabic Muslim records as full text from public-domain classical editions.
- The source explicitly distinguishes CC0-authored translations from upstream mirrored translations whose text is withheld when redistribution permission is not on file.
- Its published counts do not prove that every printed edition's numbering or chapter structure matches the app's selected reference edition. Preserve the source locator and numbering scheme, and reconcile counts against the selected edition before marking a collection complete.
- Current listed English/Urdu/Hindi translation coverage for Bukhari and Muslim is not sufficient to claim complete four-language translations. Keep unavailable language states visible rather than copying text from a proprietary mirror or generating translations.

### Alternative research dataset (not approved for release)

- https://huggingface.co/datasets/meeAtif/hadith_datasets
- This dataset lists Arabic and English for Bukhari and Muslim and an MIT dataset license. That dataset-level license alone does not establish that third-party translator copyrights are cleared. Do not bundle its English text until translator-specific redistribution rights are documented.

## Release gate

A corpus may be marked `VERIFIED_FOR_RELEASE` only after all of the following are recorded:

1. Exact work, author, translator, publisher/digitizer, language, and edition.
2. Original source URL and source edition numbering.
3. Embedded license text and evidence that redistribution in an offline APK is allowed.
4. SHA-256 of the exact downloaded file.
5. Completeness checks against the declared edition counts and locators.
6. No machine-generated translation, inferred Asbab report, or sectarian metadata.

## Sahih Muslim Arabic corpus attached (2026-11-06)

The repository now includes an offline Arabic Sahih Muslim corpus under `app/src/main/assets/islamic-library/sahih-muslim-ar/`:

- Source endpoint: https://i-muslim.com/api/v1/translations/hadith/muslim/ar
- Source metadata identifies the work as the classical Arabic edition, attributes it to the public-domain Arabic source, and identifies the upstream source as `fawazahmed0:ara-muslim`.
- License evidence and source policy: https://i-muslim.com/id/downloads
- The source response contained 7,563 numbered rows. Exactly 7,360 rows had non-empty Arabic text; 203 rows were empty placeholders. The app bundles the 7,360 non-empty narrations, preserves each upstream number, Arabic number and book number, and does not renumber the text.
- The data is split into 15 JSON asset parts plus a manifest to keep repository commits manageable.
- `SahihMuslimArabicCorpus.kt` validates all 15 parts, rejects duplicate source numbers and blank narrations, and requires exactly 7,360 records before displaying them offline.
- `HadithLibraryScreen.kt` now appends the bundled Arabic corpus to the Hadith library. When Urdu, English or Hindi is selected, it explicitly shows that a translation is unavailable rather than generating or substituting one.

Important limits: this is the Arabic original only. It does not establish licensed Urdu, English or Hindi translations, and the generic book-number labels are not scholarly book-title metadata. The full cross-language release manifest for Quran, tafsir and Bukhari remains a separate unfinished deliverable.