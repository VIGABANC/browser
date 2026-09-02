# Aegis Browser — UI / UX Architecture Design

## 1. Overview
Remediation of UI hierarchies to prioritize native browser ergonomics, accessibility compliance, clean Material 3 design tokens, and user-facing terminology.

## 2. Navigation Architecture
- Unified Screen / Overlay Model:
  - Top Level: `BrowserMainScreen` (Web view + Omnibox + Bottom Chrome).
  - Modal Navigation: Full-page routes for `Downloads`, `HistoryBookmarks`, `Settings`, `ShieldsDashboard`, `AiAssistant`, `PasswordVault`, `ReaderMode`.
  - Elimination of dual drifting implementations between modal sheets and full-page destinations.

## 3. Ergonomics & Accessibility
- **Compact Chrome**: Omnibox with security indicator, back, forward, tab switcher chip, and overflow menu.
- **Touch Targets**: Minimum 48dp on all interactive icons and buttons.
- **Copy Polish**: Replaced internal developer strings ("Media Sniffer API", "Gemini Privacy Toggle", "Simulated FFmpeg") with clear user-facing language.
- **Dynamic Type**: Full support for large font scales up to 2.0x without clipping.
- **RTL Support**: Full bi-directional layout alignment with mirrored navigation icons (`Icons.AutoMirrored`).
