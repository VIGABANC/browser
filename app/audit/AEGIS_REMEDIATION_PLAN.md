# Aegis Browser — Remediation Plan

## Phase 0 — Baseline Reproducibility
- [x] Verify project compilation and unit test baseline.
- [x] Create repository inventory and architecture maps.

## Phase 1 — P0 Security & Privacy Fixes
- [ ] Fix WebView Mixed Content Mode in `BrowserWebView.kt` (Change `MIXED_CONTENT_ALWAYS_ALLOW` to `MIXED_CONTENT_NEVER_ALLOW`).

## Phase 2 — P1 Security & Downloader Hardening
- [ ] Implement `JSONObject.quote()` safe credential encoding in `BrowserWebView.kt`.
- [ ] Add URL scheme validation and `--` argument boundaries in `YtdlpWrapper.kt`.

## Phase 3 — P2 Architecture & Thread Safety
- [ ] Replace `GlobalScope` with an application-managed `CoroutineScope` in `AegisApplication.kt`.
- [ ] Safeguard WebView pre-warming to only run on Main dispatcher or when UI is ready.

## Phase 4 — P3 Cleanup & Dead Code Removal
- [x] Delete obsolete duplicate `data/db/` package directory.

## Phase 5 — Verification & Final Reporting
- [ ] Run `compile_applet`.
- [ ] Run `gradle :app:testDebugUnitTest`.
- [ ] Generate `audit/AEGIS_FINAL_REPORT.md`.
