# Aegis Browser — Remediation & Implementation Plan (Phase 26)

## Priority Matrix

### Tier 0 — Reproducible Build & Architecture Baseline
- [x] Establish reproducible compilation (`compile_applet`) and local JVM unit testing (`gradle :app:testDebugUnitTest`).
- [x] Classify real browser engine as **C — Android WebView browser** and generate `audit/00-repository-inventory.md` and `audit/01-architecture-gap.md`.

### Tier 1 — Security & Privacy Hardening (P0 / P1)
- [x] **AEGIS-SEC-001 (P0)**: Lock `mixedContentMode` to `MIXED_CONTENT_NEVER_ALLOW` in `BrowserWebView.kt`.
- [x] **AEGIS-SEC-002 (P1)**: Implement `JSONObject.quote()` encoding for AutoFill JavaScript injection.
- [x] **AEGIS-SEC-003 (P1)**: Add URL scheme validation and `--` parameter terminators in `YtdlpWrapper.kt`.
- [x] **AEGIS-SEC-004 (P1)**: Enforce file:// protocol block in `WebViewClient.shouldOverrideUrlLoading`.

### Tier 2 — Lifecycle & Background Threading (P2)
- [x] **AEGIS-ARCH-001 (P2)**: Replace `GlobalScope` with application-managed `SupervisorJob() + Dispatchers.IO` scope in `AegisApplication.kt`.
- [x] **AEGIS-ARCH-002 (P2)**: Remove non-UI thread `WebView` instantiation during startup.

### Tier 3 — Accessibility & Theming (P3)
- [x] **AEGIS-THEME-001 (P3)**: Update light theme primary and secondary color tokens for WCAG AA 4.5:1 contrast compliance.
- [x] **AEGIS-A11Y-001 (P3)**: Fix `AccessibilityUtils.kt` to decouple TalkBack enablement from reduced motion.
- [x] **AEGIS-UI-001 (P3)**: Migrate deprecated icons to `Icons.AutoMirrored` across menus and modals.
- [x] **AEGIS-CLEAN-001 (P3)**: Purge legacy unused `data/db/` duplicate package.

### Tier 4 — Verification & Delivery
- [x] Execute `compile_applet` build validation.
- [x] Execute `gradle :app:testDebugUnitTest` test suite.
- [x] Finalize audit reports and synchronization.
