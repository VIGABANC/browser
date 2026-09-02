# Aegis Browser — Release Quality, Performance & CI Architecture Design

## 1. Overview
Build pipeline hardening, Proguard/R8 optimization, backup rule security, performance baselines, and test coverage standards.

## 2. Build & Manifest Security
- **Backup Rules**: Explicitly exclude encrypted vault databases, keystores, and auth preferences in `backup_rules.xml` and `data_extraction_rules.xml`.
- **R8 / Proguard Rules**: Retain Room schemas, Moshi adapters, and WebKit interfaces while stripping debugging symbols and obfuscating release classes.
- **App Check & Debug Tokens**: Ensure debug providers are isolated strictly from production builds.

## 3. Test Matrix
- Unit & Robolectric test coverage across:
  - `AegisCryptoManagerTest` (KeyStore GCM roundtrips, nonce uniqueness, tampering rejection, legacy migration).
  - `PrivacyDataEraserTest` (Full database, cache, cookie, and webstorage purge).
  - `UrlNormalizerTest` (Search queries, schemes, IP addresses, localhost).
  - `DirectDownloadEngineTest` (OkHttp transfers, range resumes, atomic part files, cancel cleanup).
  - `HistoryRepositoryTest` (Deduplication, navigation commits, incognito isolation).
  - `PageSecurityStateTest` (Scheme and certificate evaluation).
