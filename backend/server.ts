import express, { type Request, type Response } from "express";
import archiver from "archiver";
import path from "node:path";
import fs from "node:fs";
import { promises as fsPromises } from "node:fs";
import { fileURLToPath } from "node:url";

const filename = fileURLToPath(import.meta.url);
const backendDir = path.dirname(filename);
const projectRoot = path.resolve(backendDir, "..");
const app = express();
const PORT = Number(process.env.PORT) || 3000;

app.disable("x-powered-by");
app.use(express.json({ limit: "64kb" }));

type YouTubeVideo = {
  id: string;
  title: string;
  channel: string;
  thumbnail: string;
  publishedTime?: string;
  duration?: string;
  views?: string;
};

type CacheEntry = { timestamp: number; data: unknown };
const cache = new Map<string, CacheEntry>();
const CACHE_TTL_MS = 30 * 60 * 1000;
const MAX_CACHE_ENTRIES = 200;

function getText(value: any): string {
  if (!value) return "";
  if (typeof value.simpleText === "string") return value.simpleText;
  if (Array.isArray(value.runs)) {
    return value.runs.map((run: any) => run?.text ?? "").join("");
  }
  return "";
}

function extractInitialData(html: string): unknown | null {
  const markers = ["var ytInitialData =", "ytInitialData ="];
  for (const marker of markers) {
    const markerIndex = html.indexOf(marker);
    if (markerIndex < 0) continue;
    const start = html.indexOf("{", markerIndex + marker.length);
    if (start < 0) continue;

    let depth = 0;
    let inString = false;
    let escaped = false;

    for (let i = start; i < html.length; i++) {
      const char = html[i];
      if (inString) {
        if (escaped) escaped = false;
        else if (char.charCodeAt(0) === 92) escaped = true;
        else if (char === '"') inString = false;
        continue;
      }
      if (char === '"') inString = true;
      else if (char === "{") depth++;
      else if (char === "}") {
        depth--;
        if (depth === 0) {
          try {
            return JSON.parse(html.slice(start, i + 1));
          } catch {
            return null;
          }
        }
      }
    }
  }
  return null;
}

function collectVideoRenderers(node: any, output: any[] = []): any[] {
  if (!node || typeof node !== "object") return output;
  if (Array.isArray(node)) {
    for (const child of node) collectVideoRenderers(child, output);
    return output;
  }
  if (node.videoRenderer?.videoId) output.push(node.videoRenderer);
  for (const [key, child] of Object.entries(node)) {
    if (key !== "videoRenderer") collectVideoRenderers(child, output);
  }
  return output;
}

function mapVideo(renderer: any): YouTubeVideo | null {
  const id = String(renderer.videoId ?? "");
  if (!/^[A-Za-z0-9_-]{11}$/.test(id)) return null;
  const thumbnails = renderer.thumbnail?.thumbnails ?? [];
  const rawThumbnail = thumbnails.length ? thumbnails[thumbnails.length - 1]?.url ?? "" : "";
  const thumbnail = rawThumbnail.startsWith("//") ? "https:" + rawThumbnail : rawThumbnail;

  return {
    id,
    title: getText(renderer.title) || "Untitled YouTube video",
    channel: getText(renderer.ownerText) ||
      getText(renderer.shortBylineText) ||
      getText(renderer.longBylineText) ||
      "YouTube channel",
    thumbnail,
    duration: getText(renderer.lengthText),
    publishedTime: getText(renderer.publishedTimeText),
    views: getText(renderer.viewCountText),
  };
}

function setCache(key: string, data: unknown): void {
  if (cache.size >= MAX_CACHE_ENTRIES) {
    const oldestKey = cache.keys().next().value;
    if (oldestKey) cache.delete(oldestKey);
  }
  cache.set(key, { timestamp: Date.now(), data });
}

app.get("/api/health", (_req: Request, res: Response) => {
  res.json({ ok: true, service: "noor-e-hidayat-backend" });
});

app.get("/api/youtube", async (req: Request, res: Response) => {
  const query = String(req.query.q ?? "").trim();
  if (query.length < 2 || query.length > 160) {
    return res.status(400).json({
      error: "Query parameter q must contain between 2 and 160 characters.",
    });
  }

  const cacheKey = query.toLocaleLowerCase();
  const cached = cache.get(cacheKey);
  if (cached && Date.now() - cached.timestamp < CACHE_TTL_MS) {
    return res.json(cached.data);
  }
  if (cached) cache.delete(cacheKey);

  try {
    const searchUrl = "https://www.youtube.com/results?search_query=" + encodeURIComponent(query);
    const response = await fetch(searchUrl, {
      headers: {
        "User-Agent": "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile Safari/537.36",
        "Accept-Language": "en-US,en;q=0.9,ur;q=0.8",
        Accept: "text/html,application/xhtml+xml",
      },
      signal: AbortSignal.timeout(15000),
    });

    if (!response.ok) {
      return res.status(502).json({
        error: "YouTube search returned HTTP " + response.status + ". Try the official in-app search instead.",
      });
    }

    const html = await response.text();
    const initialData = extractInitialData(html);
    if (!initialData) {
      return res.status(502).json({
        error: "YouTube search markup could not be parsed. The official in-app search remains available.",
      });
    }

    const renderers = collectVideoRenderers(initialData);
    const unique = new Map<string, YouTubeVideo>();
    for (const renderer of renderers) {
      const video = mapVideo(renderer);
      if (video && !unique.has(video.id)) unique.set(video.id, video);
      if (unique.size >= 20) break;
    }

    const payload = {
      query,
      count: unique.size,
      videos: Array.from(unique.values()),
      source: "YouTube search page",
      cached: false,
    };
    setCache(cacheKey, payload);
    return res.json(payload);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unknown search error";
    console.error("YouTube search failed:", message);
    return res.status(502).json({
      error: "YouTube search is temporarily unavailable. Use the official in-app search.",
    });
  }
});

app.get("/api/live-streams", (_req: Request, res: Response) => {
  res.json({
    makkah: [{
      id: "saudi-quran-tv",
      title: "Makkah · Saudi Quran TV",
      titleUrdu: "مکہ مکرمہ · سعودی قرآن چینل",
      location: "Masjid al-Haram, Makkah",
      channelId: "UCos52azQNBgW63_9uDJoPDA",
      youtubeId: "channel:UCos52azQNBgW63_9uDJoPDA",
      channelUrl: "https://www.youtube.com/channel/UCos52azQNBgW63_9uDJoPDA/live",
      description: "Saudi Quran TV channel live page. Stream availability is controlled by the broadcaster."
    }],
    madinah: [{
      id: "saudi-sunnah-tv",
      title: "Madinah · Saudi Sunnah TV",
      titleUrdu: "مدینہ منورہ · سعودی سنہ چینل",
      location: "Al-Masjid an-Nabawi, Madinah",
      channelId: "UCROKYPep-UuODNwyipe6JMw",
      youtubeId: "channel:UCROKYPep-UuODNwyipe6JMw",
      channelUrl: "https://www.youtube.com/channel/UCROKYPep-UuODNwyipe6JMw/live",
      description: "Saudi Sunnah TV channel live page. Stream availability is controlled by the broadcaster."
    }]
  });
});

async function walkFiles(directory: string): Promise<string[]> {
  if (!fs.existsSync(directory)) return [];
  const entries = await fsPromises.readdir(directory, { withFileTypes: true });
  const found: string[] = [];
  for (const entry of entries) {
    if (entry.name.startsWith(".") || entry.name === "build" || entry.name === "dist") continue;
    const absolute = path.join(directory, entry.name);
    if (entry.isDirectory()) found.push(...await walkFiles(absolute));
    else if (entry.isFile() && (
      entry.name.endsWith(".kt") || entry.name.endsWith(".xml") || entry.name.endsWith(".md")
    )) found.push(path.relative(projectRoot, absolute).split(path.sep).join("/"));
  }
  return found;
}

async function getExportFiles(): Promise<string[]> {
  const fixed = [
    "README.md", "settings.gradle.kts", "build.gradle.kts", "gradle.properties",
    "gradle/libs.versions.toml", "app/build.gradle.kts", "app/src/main/AndroidManifest.xml",
    "backend/package.json", "backend/tsconfig.json", "backend/server.ts", "backend/README.md"
  ];
  const roots = [
    "app/src/main/java/com/noorehidayat/app",
    "app/src/test/java/com/noorehidayat/app",
    "app/src/main/res/values",
    "docs"
  ];
  const discovered: string[] = [];
  for (const root of roots) discovered.push(...await walkFiles(path.join(projectRoot, root)));
  return [...new Set([...fixed, ...discovered])]
    .filter(relative => fs.existsSync(path.join(projectRoot, relative)))
    .sort();
}

app.get("/api/export-codebase", async (req: Request, res: Response) => {
  try {
    const files = await getExportFiles();
    const chunks: string[] = [
      "NOOR-E-HIDAYAT — ANDROID SOURCE EXPORT\n",
      "Exported at: " + new Date().toISOString() + "\n",
      "Included source files: " + files.length + "\n\n"
    ];
    let totalLines = 0;

    for (const relative of files) {
      const content = await fsPromises.readFile(path.join(projectRoot, relative), "utf8");
      const lines = content.split("\n").length;
      totalLines += lines;
      chunks.push("\n/* ===== FILE: " + relative + " | " + lines + " lines ===== */\n", content, "\n");
    }

    const bundleText = chunks.join("");
    const download = ["1", "true"].includes(String(req.query.download ?? "").toLowerCase());
    if (download) {
      res.setHeader("Content-Type", "text/plain; charset=utf-8");
      res.setHeader("Content-Disposition", 'attachment; filename="noor-e-hidayat-source.txt"');
      return res.send(bundleText);
    }

    return res.json({
      success: true,
      filesCount: files.length,
      totalLines,
      totalBytes: Buffer.byteLength(bundleText, "utf8"),
      files,
      bundleText
    });
  } catch (error) {
    console.error("Source export failed:", error);
    return res.status(500).json({ error: "Failed to export project source." });
  }
});

app.get("/api/download-zip", async (_req: Request, res: Response) => {
  try {
    const files = await getExportFiles();
    res.status(200);
    res.setHeader("Content-Type", "application/zip");
    res.setHeader("Content-Disposition", 'attachment; filename="noor-e-hidayat-source.zip"');

    const archive = archiver("zip", { zlib: { level: 6 } });
    archive.on("warning", error => {
      if ((error as NodeJS.ErrnoException).code !== "ENOENT") console.error("ZIP warning:", error);
    });
    archive.on("error", error => {
      console.error("ZIP generation failed:", error);
      if (!res.headersSent) res.status(500);
      res.end();
    });
    archive.pipe(res);
    for (const relative of files) {
      archive.file(path.join(projectRoot, relative), { name: relative });
    }
    await archive.finalize();
  } catch (error) {
    console.error("ZIP generation failed:", error);
    if (!res.headersSent) res.status(500).json({ error: "Failed to generate source ZIP." });
  }
});

app.listen(PORT, "0.0.0.0", () => {
  console.log("Noor-e-Hidayat backend listening on port " + PORT);
});
