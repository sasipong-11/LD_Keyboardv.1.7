# AGENTS.md

Guidance for coding agents working in this repository. Read this before making changes.

## Project

**LDKeyboard** — a Thai/English Android IME (Input Method Editor) with predictive text, spell
correction (Levenshtein distance), phonetic search (Soundex + G2P) and a built-in English–Thai
dictionary (Lexitron). Application ID: pro = `th.or.nectec.ldkeyboard`, free = `th.or.nectec.twskeyboard` (namespace/Java packages stay `th.or.nectec.twskeyboard`).

Ships in two product flavors: **free** (v1.6, no license gate) and **pro** (v1.7, online license
activation on first launch).

## Build & run

Gradle wrapper from the repo root. On Windows use `gradlew.bat`; `./gradlew` works from Git Bash.
Most tasks are flavor-specific:

```bash
./gradlew assembleFreeDebug      # free flavor (license gate off)
./gradlew assembleProDebug       # pro flavor (license gate on)
./gradlew assembleDebug          # both flavors
./gradlew assembleFreeRelease    # release variants
./gradlew assembleProRelease
./gradlew installFreeDebug       # install on a connected device/emulator
./gradlew lint
./gradlew clean
```

Toolchain: compileSdk 29, buildTools 28.0.3, minSdk 16, targetSdk 29, AGP 3.2.1.

**There are no unit or instrumentation tests.** Do not claim a change is verified by tests. The only
automated checks available are `assemble*` and `lint`; anything behavioral has to be exercised on a
device or emulator (install the IME, enable it under Settings → Language & input, then switch to it).

## Repository layout

```
app/src/main/java/th/or/nectec/twskeyboard/        main app code (single source set)
  editdistance/      Distance, WordDistance — candidate ranking
  ldrule/            LDRule, AddMissingChar, Mapping, Word — edit-distance variants
  soundex/           Soundex, SoundexWord, IgnoreLR, MissSpellingWord, Part
app/src/main/java/com/example/bablueza/g2p/        g2pJni — JNI wrapper for the native G2P lib
app/src/main/java/com/android/inputmethodcommon/   AOSP IME settings helper classes
app/src/main/res/xml/        keyboard layouts + IME metadata (method.xml, ime_preferences.xml)
app/src/main/assets/         HTML manual, g2p-data.zip, prepackaged .db files
app/src/main/assets/databases/   spell_sys.db, lexitron_v3.db (copied to the device on first run)
app/src/main/jniLibs/<abi>/      prebuilt g2pLib .so files (7 ABIs)
```

There are **no per-flavor source sets** — only `app/src/main`. Flavor differences are branched at
runtime on `BuildConfig.LICENSE_ENABLED`.

## Architecture

### Entry points

- `LicenseActivity` — **launcher activity**, the gate in front of everything else.
- `KeyboardSettingActivity` — the settings UI (themes, font size, beep/vibrate). Not the keyboard.
- `SoftKeyboard extends InputMethodService` — the actual keyboard, ~2,200 lines, core of the app.
- `ImePreferences` — preference fragment reached from Android's system IME settings.
- `ManualActivity`, `G2PActivity`, `DownloadDataActivity`, `ExtractActivity` — help and G2P setup.

### Licensing

`flavorDimensions "license"` with a `BuildConfig.LICENSE_ENABLED` field per flavor:

| Flavor | versionName | versionCode | `LICENSE_ENABLED` |
|--------|-------------|-------------|-------------------|
| free   | 1.6         | 160         | `false`           |
| pro    | 1.7         | 170         | `true`            |

`LicenseActivity` on launch:

1. `LICENSE_ENABLED == false` → straight to `KeyboardSettingActivity`.
2. A key already stored under `LicenseManager.PREF_LICENSE_KEY` → skip the gate.
3. Otherwise show the activation form. Input auto-formats to `XXXXX-XXXXX-XXXXX-XXXXX-XXXXX`
   (validated by `LicenseManager.isValidFormat`), then an `AsyncTask` POSTs
   `{"keyNum":"<key without dashes>"}` to `LicenseManager.URL_ACTIVATED`. `"msg":"OK"` stores the key
   and continues; anything else clears the stored key and shows an error. Server `msg` constants
   (`MSG_OK`, `MSG_CHANGE_SERIAL`) live in `LicenseManager`.

`LicenseManager.BASE_URL` currently points at the **UAT** host (`https://ld-api.uat.rtt.in.th`) —
that constant is the single place to change when switching environments. The manifest sets
`usesCleartextTraffic="true"`.

### Keyboard layouts

`res/xml/`: `qwerty_thai.xml` / `qwerty_thai_shift.xml` (Thai), `qwerty.xml` / `qwerty_shift.xml`
(English), `symbols.xml` / `symbols_shift.xml`. `LatinKeyboard` extends the framework `Keyboard`;
`LatinKeyboardView` extends `KeyboardView` for custom rendering.

### Predictive text / spell correction pipeline

`SoftKeyboard` accumulates `mComposing`, then:

1. **Exact match** — `DictionaryDatabaseHelper` over `spell_sys.db`, table `BEST_SPELL_TH`
   (`SENSEGROUP` = word, `G2P` = phonetic form).
2. **Phonetic search** — `SoundexWord` generates Soundex variants and queries by G2P representation;
   `IgnoreLR` handles L/R confusion.
3. **Edit-distance candidates** — `LDRule`: `deleteChar()`, `switchChar()`, `changeChar()`,
   `addChar()`, plus `AddMissingChar` for multi-syllable Thai words.
4. **Ranking** — `Distance` / `WordDistance` sort by edit distance.
5. **Display** — `CandidateView`, up to 6 suggestions per page (4 in portrait on small screens),
   with pagination.

### Dictionary lookup (Lexitron)

`LexitronDatabaseHelper` queries `lexitron_v3.db` for part of speech, Thai translation and English
definition; `DictionaryAdapter` renders them into a `ListView`; `Vocab` is the entry POJO.

### Native G2P

Grapheme-to-phoneme uses the prebuilt native `g2pLib`. `g2pJni` is the JNI wrapper,
`GraphemeToPhoneme` wraps it for `SoftKeyboard`. Data files (`initTTS.in`) are unpacked from assets
at runtime by `DownloadDataActivity` / `ExtractActivity`. **No NDK build from source** —
`jni.srcDirs` is deliberately empty and the `.so` files are checked in.

### Content providers

- `DictionaryProvider`, authority `th.or.nectec.twskeyboard.g2p` → `spell_sys.db`
- `LexitronProvider`, authority `th.or.nectec.twskeyboard.lexitron` → `lexitron_v3.db`

Both databases ship in `app/src/main/assets/databases/` and are copied on first launch via
`CopyDatabase` / `SQLiteAssetHelper`.

### Settings persistence

One `SharedPreferences` file named `"th.or.nectec.twskeyboard"`:

| Key | Meaning |
|-----|---------|
| `bgTheme` | drawable resource ID for the keyboard background |
| `keyTextSize` | SeekBar progress, 10–100 |
| `beepStatus` | vibration/beep feedback toggle |
| `start` | first-launch flag |
| `font` | TTF filename |
| `license_key` | activated license key (pro only) |

### Analytics

`AnalyticsApplication` (the `Application` subclass) initializes Google Analytics 10.2.4 on startup.

## Conventions

- **User-facing strings are Thai.** Keep new user-visible text in Thai to match.
- This is an old codebase (minSdk 16): `AsyncTask`, raw `HttpURLConnection`, `findViewById` casts,
  no data binding, no Kotlin. **Match the surrounding style** rather than introducing new async or
  HTTP stacks, Kotlin files, or modern Jetpack libraries.
- AndroidX is enabled with Jetifier (`android.useAndroidX=true`, `android.enableJetifier=true`) while
  the declared dependencies are still `com.android.support:*:28.0.0`. **New code should import
  `androidx.*`** (see `LicenseActivity` for the pattern); don't convert existing
  `android.support.*` imports wholesale.
- Prefer small, local edits. `SoftKeyboard.java` is large and load-bearing — read the surrounding
  method before touching it, and don't reformat or reorganize it as a side effect.

## Gotchas

- Changing a flavor's `versionCode` / `versionName` affects released artifacts — leave them alone
  unless that is the task.
- `local.properties` is untracked (local SDK path); `gradle.properties` is tracked.
- Asset `.db` files are binary and large; never rewrite them as text. `databases/lexitron_v3_old.db`
  is a leftover and is not in use.
- `libs/apache-httpcomponents-*.jar` are vendored jars, not Maven dependencies.
- Prebuilt `.so` files exist for 7 ABIs; adding an ABI means adding a binary, not a build flag.

## Git

Working branch is `feature/license`; the main branch is `main`. Commit or push only when asked.
