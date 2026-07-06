# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LDKeyboard is a Thai/English Android Input Method Editor (IME) with predictive text, spell correction (Levenshtein Distance), phonetic search (Soundex + G2P), and a built-in English-Thai dictionary. The app ID is `th.or.nectec.twskeyboard`.

## Build Commands

Use Android Gradle Wrapper from the project root:

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug APK on connected device
./gradlew installDebug

# Run lint
./gradlew lint

# Clean build artifacts
./gradlew clean
```

Build config: compileSdk 29, minSdk 16, targetSdk 29. Requires NDK because of the native `g2pLib` library.

There are no unit or instrumentation tests in this project.

## Architecture

### Entry Points

- `KeyboardSettingActivity` — Launcher activity (settings UI for themes, font size, beep/vibrate). NOT the keyboard itself.
- `SoftKeyboard` (extends `InputMethodService`) — The actual keyboard service (~2,200 lines). This is the core of the app.
- `ImePreferences` — System IME settings preference fragment (called from Android Settings).

### Keyboard Layouts

Five keyboard layouts defined in `res/xml/`:
- `qwerty_thai.xml` / `qwerty_thai_shift.xml` — Thai character layout
- `qwerty.xml` / `qwerty_shift.xml` — English QWERTY layout
- `symbols.xml` / `symbols_shift.xml` — Symbol layout

`LatinKeyboard` extends the Android `Keyboard` class; `LatinKeyboardView` extends `KeyboardView` for custom rendering.

### Predictive Text / Spell Correction Pipeline

When the user types, `SoftKeyboard` builds a `mComposing` string and runs this pipeline:

1. **Exact match** — query `DictionaryDatabaseHelper` (`spell_sys.db`, table `BEST_SPELL_TH`, columns: `SENSEGROUP` word, `G2P` phonetic).
2. **Phonetic search** — `SoundexWord` generates Soundex variants, then queries the dictionary via G2P phonetic representations. `IgnoreLR.java` handles L/R phonetic confusion.
3. **Edit-distance candidates** — `LDRule` generates variations:
   - `deleteChar()`, `switchChar()`, `changeChar()`, `addChar()`
   - For multi-syllable Thai words: `AddMissingChar`
4. **Ranking** — `Distance.java` / `WordDistance.java` sort candidates by edit distance.
5. **Display** — `CandidateView` shows up to 6 suggestions per page (4 in portrait on small screens), with pagination.

### Dictionary Lookup (Lexitron)

When the user selects a candidate or taps the dictionary button, `LexitronDatabaseHelper` queries `lexitron_v3.db` to fetch the word's part of speech, Thai translation, and English definition. `DictionaryAdapter` renders the results in a `ListView`. The `Vocab.java` POJO holds one dictionary entry.

### Native G2P Library

Grapheme-to-Phoneme conversion uses a native NDK library (`g2pLib`). The JNI wrapper is `G2PJni.java` (`com.example.bablueza.g2p` package); `GraphemeToPhoneme.java` wraps it for use in `SoftKeyboard`. G2P data files (`initTTS.in`) are loaded from assets at runtime via `DownloadDataActivity` / `ExtractActivity`.

### Content Providers

Two ContentProviders expose the SQLite databases to other app components:
- `DictionaryProvider` (authority: `th.or.nectec.twskeyboard.g2p`) → `spell_sys.db`
- `LexitronProvider` (authority: `th.or.nectec.twskeyboard.lexitron`) → `lexitron_v3.db`

Both databases are pre-packaged in `app/src/main/assets/` and copied on first launch via `CopyDatabase.java` / `SQLiteAssetHelper`.

### Settings Persistence

All user preferences are stored in a single `SharedPreferences` file keyed by `"th.or.nectec.twskeyboard"`:
- `bgTheme` — drawable resource ID for keyboard background
- `keyTextSize` — SeekBar progress value (10–100)
- `beepStatus` — vibration feedback toggle
- `start` — first-launch flag
- `font` — TTF font filename

### Analytics

`AnalyticsApplication` (the `Application` subclass) initializes Google Analytics 10.2.4 on startup.
