# Plan B: Real Download & Media Engine Implementation Plan

## Files to Modify/Create
1. `app/src/main/java/com/example/data/downloader/DownloadEngine.kt`
2. `app/src/main/java/com/example/data/downloader/DownloadManager.kt`
3. `app/src/main/java/com/example/data/downloader/MediaExtractorEngine.kt`
4. `app/src/main/java/com/example/data/downloader/NetworkTrafficMonitor.kt`
5. `app/src/main/java/com/example/data/local/entity/DownloadEntity.kt`
6. `app/src/main/java/com/example/data/local/dao/DownloadDao.kt`
7. `app/src/main/java/com/example/data/local/AegisDatabase.kt`
8. `app/src/test/java/com/example/DownloadEngineTest.kt`
9. `app/src/test/java/com/example/MediaDiscoveryTest.kt`

## Step-by-Step Implementation
1. **Remove Fake Artifacts**: Delete all sample/mock downloads (`createSampleDownloads`), simulated delays, fake resolutions, and placeholder file writers.
2. **Download Database Persistence**:
   - Add `DownloadEntity` and `DownloadDao` to Room database (`AegisDatabase`).
3. **Authoritative Direct Download Engine**:
   - Create `DownloadEngine` using `OkHttpClient` with streaming IO, `.part` file writing, Range header resume support, speed and ETA computation.
4. **Media Extractor Truthfulness**:
   - Refactor `MediaExtractorEngine` and `NetworkTrafficMonitor` to only report actually observed DOM/network media with "Unknown" for unverified metrics.
5. **Unit Tests**:
   - Test OkHttp range requests, atomic rename, cancellation cleanup, and media extraction truthfulness.
