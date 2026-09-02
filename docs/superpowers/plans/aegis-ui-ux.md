# Plan D: UI & UX Remediation Implementation Plan

## Files to Modify/Create
1. `app/src/main/java/com/example/ui/BrowserMainScreen.kt`
2. `app/src/main/java/com/example/ui/components/OmniboxBar.kt`
3. `app/src/main/java/com/example/ui/components/BottomToolbar.kt`
4. `app/src/main/java/com/example/ui/pages/SettingsPage.kt`
5. `app/src/main/java/com/example/ui/pages/DownloadsPage.kt`
6. `app/src/main/res/values/strings.xml`

## Step-by-Step Implementation
1. **Sanitize UI Strings**: Replace developer jargon ("Media Sniffer API", "Simulated FFmpeg", "Gemini Privacy Toggle") with clear user terminology.
2. **Standardize Navigation**: Ensure unified routes between full pages and modal components.
3. **Accessibility**: Add explicit `contentDescription` attributes and enforce 48dp touch targets.
4. **Settings Hierarchy**: Organize settings into Privacy & Security, Downloads, Search, Autofill, AI, Appearance, About.
