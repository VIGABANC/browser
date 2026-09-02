# Aegis Browser — Browser Core & Architecture Design

## 1. Overview
Decomposition of monolithic state management into cohesive, single-responsibility domains and unified state holders.

## 2. Domain Decomposition
- **TabsStateHolder**: Single authoritative state for active tab, tab list, tab reordering, and session restore.
- **BrowserNavigationController**: Omnibox text input, URL normalization (handling search queries vs valid HTTP/HTTPS schemes), back/forward navigation state.
- **HistoryCoordinator**: Deduplicated single-event visit recording on main-frame commit (bypassing redundant on-page-finished/on-input writes, ignoring incognito).
- **SecurityStateHolder**: Structured `PageSecurityState` classification (`SECURE_HTTPS`, `INSECURE_HTTP`, `INTERNAL_PAGE`, `CERTIFICATE_ERROR`, `MIXED_CONTENT`, `UNKNOWN`) based on verifiable WebView connection metadata.
- **IncognitoManager**: Strict ephemeral session boundaries, isolated cache directories, zero history logging, and immediate memory purge on closure.

## 3. Concurrency & Lifecycle
- Tab-specific network monitors, script injections, and evaluators bound to tab lifecycle scopes (`SupervisorJob()`).
- Cancellation of dangling coroutines and timers upon tab destruction.
