# Release Notes

## Version 1.0.0 — September 22, 2026

This major release completely transforms **Quick Type** into a full-fidelity implementation of Palm/HP webOS's legendary **Just Type** feature on Android. Built for Android 15 & 16 (Target SDK 36), it combines webOS's friction-free universal search and contextual actions with modern Android architecture.

---

### Universal Real-Time Multi-Category Search
- **No CLI Command Prefixes**: Eliminated the need to type `call ` or `text `. Just start typing anything into the search field, and Quick Type simultaneously evaluates and categorizes results in real-time.
- **Categorized Sections with webOS Dividers**:
  - 🧮 **Calculation**: Instant math evaluation with tap-to-copy.
  - 📱 **Applications**: Substring, word prefix, and acronym search (`"yt"` $\rightarrow$ **Y**ou**T**ube).
  - 👥 **Contacts**: Matched contacts with dedicated one-tap **Call** (📞) and **SMS** (💬) action buttons.
  - ⚡ **Quick Actions**: Contextual actions taking whatever text you've typed.
  - 🌐 **Search Everywhere**: Configurable web and service search providers.
- **Empty State "Frequently Used Apps"**: When the search box is empty, Quick Type automatically displays your most frequently launched apps for instant one-tap launching.

---

### Contextual Quick Actions (webOS Signature Feature)
- **New Note / Memo**: Dispatches standard `ACTION_SEND` (`text/plain`) with your typed text prefilled, opening Google Keep, Notes, or your preferred note-taking app.
- **New Calendar Event**: Dispatches `ACTION_INSERT` on `Events.CONTENT_URI` with the event title prefilled.
- **Natural Language Timers & Alarms**:
  - Typing `timer 5m`, `timer 10 min`, `timer 45s`, or `timer 2h` automatically configures the Android Clock app.
  - Typing `alarm 7am`, `alarm 7:30pm`, or `alarm 06:45` configures alarms directly.
- **New Email Draft**: Dispatches `ACTION_SENDTO` (`mailto:`) with your query as the prefilled subject line.
- **Search Play Store**: Opens direct store lookups via `market://search?q=`.
- **Direct Phone Dialing**: Automatically detects typed phone numbers and offers direct dialing via `ACTION_DIAL`.

---

### Inline Calculator & Unit Utilities
- **Safe Recursive-Descent Parser**: Computes arithmetic expressions (`+`, `-`, `*` / `x` / `×`, `/` / `÷`, `%`, `^`, parentheses, and decimals) without third-party bloat or external API calls.
- **Human-Friendly Syntax**: Supports natural queries like `(120 + 30) / 5`, `15% of 200`, and `2^8`.
- **Tap-to-Copy**: Tapping the calculation card immediately copies the computed answer to the system clipboard.

---

### Android System Integration & Accessibility
- **Default Digital Assistant**: Registered `android.intent.action.ASSIST` in `AndroidManifest.xml`. You can now select Quick Type as your device's default digital assistant in Android Settings (summoned via long-press on home or bottom-corner swipe gestures).
- **Quick Settings Tile**: Added `QuickTypeTileService` so Quick Type can be pinned to your Android notification shade for instant access anywhere in the OS.
- **Hardware Keyboard Forwarding**: Detects keystrokes on physical keyboards (e.g. Unihertz Titan, F(x)tec, or Bluetooth keyboards) and routes them straight to the search box without needing to tap the screen first.

---

### Android 15/16 & Edge-to-Edge System Insets
- **Dynamic Inset Handling**: Implemented `ViewCompat.setOnApplyWindowInsetsListener` with `WindowInsetsCompat.Type.systemBars()` on both the search view and Settings. Content cleanly rests below the status bar clock, battery indicators, camera cutouts, and above the bottom gesture navigation pill.
- **Light/Dark Status Bar Controls**: Integrated `WindowInsetsControllerCompat` to ensure status bar indicators remain crisp and legible against dark themes.

---

### Redesigned Settings & Provider Customization
- **Theme.JustType.Settings**: Modernized the Settings screen with proper action bar back navigation (`← Quick Type Settings`) and legible typography.
- **Category Toggles**: Easily toggle Applications, Contacts, Quick Actions, Calculator, and Web Search.
- **Web Provider Selection**: Enable or disable Google, Maps, YouTube, Wikipedia, DuckDuckGo, Reddit, GitHub, and Amazon.
- **Reset Launch History**: One-tap action to reset app frequency statistics.
- **webOS Tribute**: Palm Pre heritage documentation and the classic `20090606` Easter egg.

---

### Architecture & Reproducible Builds
- **Target SDK 36 (Android 16)**: Upgraded `targetSdk` and `compileSdk` to 36, resolving Google Play target API requirements.
- **Async Caching on Dispatchers.IO**: Background coroutines load applications and contacts with zero UI thread freezing.
- **ConcatAdapter & ListAdapter**: Unified multi-section scrolling with `DiffUtil` for 120Hz smooth rendering.
- **F-Droid & IzzyOnDroid Reproducible Builds**: Configured `dependenciesInfo.includeInApk = false` to eliminate non-reproducible binary metadata blobs.
