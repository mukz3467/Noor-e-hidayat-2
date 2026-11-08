# Noor-e-Hidayat backend

Separate Node.js service used by the native Kotlin Android app.

## Requirements
- Node.js 20 or newer
- npm

## Run locally

```bash
cd backend
npm install
npm run dev
```

The service listens on port 3000 by default.

## Endpoints
- `GET /api/health` — health check.
- `GET /api/youtube?q=...` — parses public YouTube search-page metadata and caches successful responses for 30 minutes. This is not the official YouTube Data API. YouTube can block automated requests or change its markup; the app retains the official YouTube search WebView as fallback. No invented fallback video IDs are returned.
- `GET /api/live-streams` — returns the Saudi Quran TV and Saudi Sunnah TV channel live pages. The broadcaster controls whether a live stream is currently available.
- `GET /api/export-codebase` — JSON source bundle for an allowlisted set of Kotlin and project configuration files.
- `GET /api/export-codebase?download=1` — downloads the same source bundle as text.
- `GET /api/download-zip` — creates and downloads a ZIP of the same source files.

The source exporter excludes `.env` files, signing keys, build outputs and arbitrary filesystem paths.

## Connect the Android app

Deploy this folder as a Node.js web service and copy its HTTPS base URL. Build the Android app with:

```bash
NOOR_BACKEND_URL=https://YOUR-BACKEND-HOST gradle assembleDebug
```

Gradle injects this into `BuildConfig.NOOR_BACKEND_BASE_URL`. If the variable is empty, the app continues to use its built-in official YouTube search WebView and bundled official-channel defaults.

Do not commit secrets or private credentials to the repository.
