# Plan C: Browser Core & Architecture Implementation Plan

## Files to Modify/Create
1. `app/src/main/java/com/example/data/model/PageSecurityState.kt`
2. `app/src/main/java/com/example/data/repository/HistoryRepository.kt`
3. `app/src/main/java/com/example/data/repository/TabsRepository.kt`
4. `app/src/main/java/com/example/viewmodel/BrowserViewModel.kt`
5. `app/src/test/java/com/example/BrowserCoreTest.kt`

## Step-by-Step Implementation
1. **Model PageSecurityState**: Enum defining `SECURE_HTTPS`, `INSECURE_HTTP`, `INTERNAL_PAGE`, `CERTIFICATE_ERROR`, `MIXED_CONTENT`, `UNKNOWN`.
2. **History Coordinator**: Deduplicate history visits; commit single entry per navigation lifecycle, omit incognito browsing.
3. **Tab State Single Source of Truth**: Unify tab lists into single authoritative state flow.
4. **Unit Tests**: Test URL normalization, history deduplication, and tab lifecycle transitions.
