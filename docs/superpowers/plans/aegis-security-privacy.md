# Plan A: Security & Privacy Implementation Plan

## Files to Modify/Create
1. `app/src/main/java/com/example/data/security/AegisCryptoManager.kt`
2. `app/src/main/java/com/example/data/security/PrivacyDataEraser.kt`
3. `app/src/main/java/com/example/ui/components/BrowserWebView.kt`
4. `app/src/main/java/com/example/data/gemini/GeminiClient.kt`
5. `app/src/main/res/xml/backup_rules.xml`
6. `app/src/main/res/xml/data_extraction_rules.xml`
7. `app/src/test/java/com/example/AegisCryptoManagerTest.kt`
8. `app/src/test/java/com/example/PrivacyDataEraserTest.kt`

## Step-by-Step Implementation
1. **TDD Crypto**: Write `AegisCryptoManagerTest` with tests for:
   - GCM encryption/decryption round trip
   - Random nonce uniqueness
   - Tampered ciphertext rejection
   - Legacy CBC migration to v2 envelope format
   - Fail-closed behavior (no plaintext fallback)
2. **Implement `AegisCryptoManager`**:
   - `AndroidKeyStore` AES-256 key generation (`KeyGenParameterSpec` with `PURPOSE_ENCRYPT or PURPOSE_DECRYPT`, `GCM`, `NoPadding`).
   - `encrypt(plaintext: String): String` returning `"v2:" + Base64(iv) + ":" + Base64(ciphertextWithTag)`.
   - `decrypt(payload: String): String` with automatic legacy migration fallback for `"v1:"` or legacy pair representations.
3. **Implement `PrivacyDataEraser`**:
   - Coordinated wiping of Room tables, Cookies, WebStorage, WebView disk cache, and state flows.
4. **Harden `BrowserWebView`**:
   - Remove sensitive password access from JavaScript bridges.
   - Disable file/content access (`allowFileAccess = false`, `allowContentAccess = false`).
   - Sanitize scheme checks (`http`, `https`, `about:`, `javascript:`).
5. **AI Privacy Sanitizer**:
   - Remove fabricated claims from `GeminiClient`. Add credential/PII scrubbers.
6. **Update Backup Rules**:
   - Exclude sensitive databases and shared preferences from cloud auto-backup.
