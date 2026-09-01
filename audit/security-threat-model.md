# Aegis Browser — Security Threat Model

## Assets
1. **User Browsing History, Bookmarks, and Vault Credentials** (Stored locally in Room database and AES-256 encrypted fields).
2. **Local File System & Sandbox** (Protected from untrusted web pages).
3. **Downloader Output & Media Files** (Protected from arbitrary path traversal and command injection).
4. **Network Privacy & Anti-Tracking** (Enforced by AdBlock filters, HTTPS encryption, and Safe Mode whitelist).

## Threat Matrix

| Asset | Threat | Existing Mitigation | Identified Gap | Severity | Fix |
|---|---|---|---|---|---|
| **WebView Subresources** | Mixed content downgrade (HTTP on HTTPS) | `MIXED_CONTENT_ALWAYS_ALLOW` in WebView settings | Insecure subresources permitted on secure sites | **P0** | Change to `MIXED_CONTENT_NEVER_ALLOW` |
| **Credential AutoFill** | JS injection / XSS through unescaped quotes | `.replace("'", "\\'")` naive string escape | Special chars / backslashes can break out of script | **P1** | Use `JSONObject.quote()` for proper JSON escaping |
| **Downloader CLI** | Argument injection via malicious URLs | `ProcessBuilder` with raw strings | Flag arguments in URL could alter subprocess behavior | **P1** | Add argument boundary `--` and validate URL protocol scheme |
| **Application Lifecycle** | `GlobalScope` unmanaged coroutines & thread safety | Background launch in `Application.onCreate` | Coroutine leaks & non-UI WebView instantiation | **P2** | Use `CoroutineScope(SupervisorJob() + Dispatchers.IO)` |
| **Local Database** | Stale / duplicate database package | Legacy `com.example.data.db` directory | Dead code ambiguity | **P3** | Removed duplicate `data.db` folder |
