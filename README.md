# Noor-e-Hidayat

An Android Islamic companion app built with Kotlin and Jetpack Compose.

## Current app features

- Quran reader with Arabic text and verse-level Urdu and English translations.
- Optional full-Quran offline cache: open **More → Download Quran for offline reading** while connected to the internet. Wait for all 114 Surahs to finish; then cached Surahs can be opened offline.
- Quran tafseer screen using the published editions returned by AlQuranCloud.
- Qibla direction based on the device's location; the app does not silently substitute a fixed city when location is unavailable.
- Prayer times from AlAdhan using the selected calculation method and Asr school, with a clearly labelled approximate offline fallback.
- Optional prayer-time notifications and a configurable daily reminder (9:00 AM default). Android battery restrictions can delay notifications.
- Theme selection, appearance modes, custom colors and eye-comfort tint.
- Zakat calculation using gold and silver prices entered by the user for their local market.
- Hijri date shown as an estimate, not as an official local moon-sighting result.
- Haramain live channel embeds for the Saudi Quran TV and Saudi Sunnah TV YouTube channels.
- Selected hadith and supplication records with direct links to their cited source pages.

## Data sources and accuracy notes

- Quran verse text and verse translations: [AlQuranCloud API](https://alquran.cloud/api).
- Prayer-time calculations: [AlAdhan API](https://aladhan.com/prayer-times-api).
- Makkah live channel: [Saudi Quran TV](https://www.youtube.com/channel/UCos52azQNBgW63_9uDJoPDA/live).
- Madinah live channel: [Saudi Sunnah TV](https://www.youtube.com/channel/UCROKYPep-UuODNwyipe6JMw/live).

The hadith and dua sections are selected references, not a complete hadith or supplication encyclopedia; each displayed citation should be checked in its linked source. The app does not currently bundle a preloaded Quran dataset. Offline reading becomes available after the user completes the in-app download. Word-by-word glosses are intentionally not populated until a complete, verified and properly licensed dataset is integrated. Unreferenced Asbab al-Nuzul summaries are not displayed as established reports.

Prayer-time fallback calculations are approximate and should be checked against a trusted local mosque when the online calculation service is unavailable. Zakat metal prices are user-entered and are not live market quotes. The Hijri date can differ from local moon-sighting announcements.

## Arabic Quran typography

- Quranic Arabic, Surah names, and Arabic word-by-word labels use the Amiri Naskh typeface, bundled into the APK for offline rendering.
- Amiri is distributed under the SIL Open Font License (OFL). The build downloads the font and its license from the Google Fonts repository; no external font website is opened inside the app.
- Quran verse text itself is not rewritten or replaced by artwork. Only its typeface and presentation are changed.

## Build and test

- JDK 17
- Android Gradle Plugin 9.1.1
- Gradle 9.3.1

Run:

```bash
gradle testDebugUnitTest assembleDebug
```

The GitHub Actions workflow at `.github/workflows/android-ci.yml` runs unit tests and assembles a debug APK for pull requests and pushes to the configured branches.
