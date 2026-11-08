# Noor-e-Hidayat

An Android Islamic companion app built with Kotlin and Jetpack Compose.

## Current app features

- Quran reader with Arabic original text and verse-level English, Urdu and Hindi translations (four reading languages including Arabic).
- Optional full-Quran offline cache: open **More → Download Quran for offline reading** while connected to the internet. Wait for all 114 Surahs to finish; then cached Surahs can be opened offline.
- Quran tafseer screen using the published editions returned by AlQuranCloud.
- Qibla direction based on the device's location; the app does not silently substitute a fixed city when location is unavailable.
- Prayer times from AlAdhan using the selected calculation method and Asr school, with a clearly labelled approximate offline fallback.
- Optional prayer-time notifications and a configurable daily reminder (9:00 AM default). Android battery restrictions can delay notifications.
- Theme selection, appearance modes, custom colors and eye-comfort tint.
- Zakat calculation using gold and silver prices entered by the user for their local market.
- Hijri date shown as an estimate, not as an official local moon-sighting result.
- Haramain live channel embeds for the Saudi Quran TV and Saudi Sunnah TV YouTube channels.
- Offline Arabic Sahih Muslim corpus (7,360 non-empty narrations); other Hadith collections and licensed translations remain separately scoped.

## Data sources and accuracy notes

- Quran verse text and verse translations: [AlQuranCloud API](https://alquran.cloud/api).
- Prayer-time calculations: [AlAdhan API](https://aladhan.com/prayer-times-api).
- Makkah live channel: [Saudi Quran TV](https://www.youtube.com/channel/UCos52azQNBgW63_9uDJoPDA/live).
- Madinah live channel: [Saudi Sunnah TV](https://www.youtube.com/channel/UCROKYPep-UuODNwyipe6JMw/live).

The Hadith library now bundles 7,360 non-empty Arabic narrations from Sahih Muslim for offline reading. The upstream export contains 7,563 numbered rows, including 203 empty placeholders which are omitted without renumbering. English, Urdu and Hindi translations for Sahih Muslim are not bundled because redistribution permission is not established; the app displays an unavailable state instead of machine-translating. Sahih al-Bukhari and the broader Quran/tafsir corpus are not yet complete offline datasets. The app does not currently bundle a preloaded Quran dataset; offline Quran reading becomes available after the user completes the in-app download. A Surah is accepted into the cache only when Arabic, English, Urdu and Hindi ayah counts are all complete. Word-by-word glosses are intentionally not populated until a complete, verified and properly licensed dataset is integrated. Unreferenced Asbab al-Nuzul summaries are not displayed as established reports.

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

## YouTube backend

The repository now includes a separate Node.js service in `backend/` for YouTube lecture search, Haramain channel metadata, and source export.

- `GET /api/youtube?q=...` returns parsed public YouTube search metadata with a 30-minute in-memory cache. This is an unofficial HTML adapter, not the official YouTube Data API; YouTube may block requests or change its page structure. The Android app falls back to its built-in YouTube search WebView when the backend is not configured or the parser cannot return results.
- `GET /api/live-streams` returns the Saudi Quran TV and Saudi Sunnah TV channel live pages. The channels control stream availability; this endpoint does not claim that a live broadcast is always active.
- `GET /api/export-codebase` and `GET /api/download-zip` export an allowlisted set of project source/configuration files. Secrets, signing keys, environment files and build outputs are excluded.

Run the backend instructions in [backend/README.md](backend/README.md). To connect an Android build, pass the deployed HTTPS backend URL as the Gradle property `NOOR_BACKEND_URL`, for example:

```bash
gradle -PNOOR_BACKEND_URL=https://YOUR-BACKEND-HOST assembleDebug
```

Without this property, the app keeps its existing no-key YouTube WebView search and bundled channel fallback.
