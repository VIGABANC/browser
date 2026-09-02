# Aegis Browser — Security & Privacy Architecture Design

## 1. Overview & Threat Model
This document details the architectural remediation of Aegis Browser's cryptographic vault, WebView security perimeter, privacy data eraser, and AI context boundary.

## 2. Vault Cryptography & AndroidKeyStore
### Vulnerabilities Remediated
- Removed hard-coded static byte array encryption key (`VAULT_SECRET_BYTES`).
- Replaced unauthenticated `AES/CBC/PKCS5Padding` with authenticated `AES/GCM/NoPadding`.
- Eliminated Base64 plaintext fallback on encryption/decryption failure.

### Target Architecture
- **Key Storage**: Hardware-backed or TEE `AndroidKeyStore` provider (`AndroidKeyStore`), alias `"AegisVaultKey_v2"`.
- **Cipher**: `AES/GCM/NoPadding` with 256-bit key size and 128-bit authentication tag (`GCMParameterSpec`).
- **Nonce/IV Generation**: Cryptographically secure 12-byte random IV (`SecureRandom`) per encryption operation.
- **Payload Format**: Versioned envelope string `v2:<Base64(IV)>:<Base64(Ciphertext+Tag)>`.
- **Migration**: Controlled legacy decryption using legacy CBC key strictly for existing records; on successful migration, immediately re-encrypt using v2 AES-GCM and persist updated ciphertext. Fail closed if unauthenticated or corrupted.

## 3. WebView Security Perimeter
### Hardening Directives
- **Script Injection**: Removed credential and password operations from global `@JavascriptInterface` bridges.
- **Access Control**: Explicitly set `allowFileAccess = false`, `allowContentAccess = false`, `mixedContentMode = MIXED_CONTENT_NEVER_ALLOW`.
- **Scheme Filtering**: Allow only `http`, `https`, `about:blank`, `about:home`, `javascript:`. Strictly reject unexpected custom schemes, `file://`, and malicious intent payloads.
- **SSL Error Handling**: Fail closed on SSL errors. Never bypass `onReceivedSslError`.
- **Lifecycle Cleanup**: On tab closure or WebView disposal, stop loading, clear history, clear matches, detach interface bridges, and invoke `destroy()`.

## 4. Truthful Privacy Data Eraser
- Orchestration boundary `PrivacyDataEraser` executing coordinated deletion:
  1. Room database entries (browsing history, bookmarks if selected, autofill credentials).
  2. WebKit `CookieManager` (`removeAllCookies`, `flush`).
  3. WebStorage (`WebStorage.getInstance().deleteAllData()`).
  4. WebView cache directory and files (`clearCache(true)`).
  5. In-memory AI chat messages and ephemeral states.
- Returns explicit status `Result<PrivacyEraseSummary>` verifying operation completion.

## 5. Gemini AI Context Consent & Disclosure
- Transparent user toggle: "Include current page context in AI requests".
- Default safe: In Incognito mode, page context is never forwarded without manual per-request user approval.
- Redaction: Regex-based scrubbing of password fields, authorization tokens, credit cards, and sensitive identifiers prior to transmission.
- No fabricated telemetry: Remove all synthetic security/privacy claims.
