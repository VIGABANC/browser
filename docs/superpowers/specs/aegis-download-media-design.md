# Aegis Browser — Real Download & Media Engine Architecture Design

## 1. Overview
Remediation of the download architecture from simulated/placeholder behavior into a single, authoritative, reliable download and media inspection engine.

## 2. Eliminated Anti-Patterns
- Deleted all fake downloads, fake progress simulations, fake delay loops, and placeholder text files (`Aegis Browser Downloaded Media:...`).
- Deleted fabricated format generation (fake 1080p/720p/480p/360p/MP3 synthetic options for arbitrary URLs).
- Removed mock yt-dlp / mock ffmpeg transcoding paths.

## 3. Authoritative Download Pipeline
```
UI / Composable
  -> DownloadViewModel / StateHolder
  -> DownloadRepository (authoritative coordinator)
  -> OkHttp Download Engine (Range-header support, atomic .part files)
  -> Room Local Database (DownloadEntity persistence)
  -> MediaStore / Scoped Storage File Finalization
```

## 4. Transfer Mechanics & Integrity
- **HTTP Engine**: `OkHttpClient` with connection timeouts (15s) and streaming body reading.
- **Resume Capability**: Real HTTP `Range: bytes=offset-` requests when server provides `Accept-Ranges: bytes` and matching `ETag` or `Last-Modified`.
- **Atomic Writes**: Download writes directly to `<dest>.part` file. Upon 100% byte verification and HTTP 200/206 validation, renamed atomically to final filename.
- **Progress Tracking**: Real byte counting (`bytesRead`, `contentLength`, real-time throughput `speedBps`, calculated `etaSeconds`).
- **Cleanup**: Incomplete, paused, or failed downloads cleanly retain or delete temporary `.part` files based on user action.

## 5. Media Discovery Policy
- Surface only verifiable media extracted from:
  1. Real HTML DOM media elements (`<video>`, `<audio>`, `<source>` tags).
  2. Genuine network requests intercepted in `shouldInterceptRequest` (`video/mp4`, `video/webm`, `audio/mpeg`, `audio/ogg`, etc.).
  3. Real parsed HLS master manifests (`.m3u8` variant streams).
- When quality parameters (bitrate, dimensions, codecs) are not provided by headers/DOM, explicitly display `"Unknown"`.
- Never fabricate audio extraction unless direct audio stream URL exists.
