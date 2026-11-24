# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LDKeyboard is a Thai/English Android Input Method Editor (IME) with predictive text, spell correction (Levenshtein Distance), phonetic search (Soundex + G2P), and a built-in English-Thai dictionary. App IDs: **pro** = `th.or.nectec.ldkeyboard` (set in the `pro` flavor block, because Play reported `th.or.nectec.twskeyboard` as already taken), **free** = `th.or.nectec.twskeyboard`. The `namespace`, Java packages, and the SharedPreferences file name stay `th.or.nectec.twskeyboard` — never hardcode the app ID in code; use `getPackageName()`.

The app ships in two product flavors: **free** (v1.6, no license gate) and **pro** (v1.7.1, online license activation required on first launch).

## Build Commands

Use the Android Gradle Wrapper from the project root. Because of the product flavors, most tasks are flavor-specific:

```bash
# Build debug APKs
./gradlew assembleFreeDebug     # free flavor (license gate disabled)
./gradlew assembleProDebug      # pro flavor (license gate enabled)
./gradlew assembleDebug         # both flavors

# Build release APKs
./gradlew assembleFreeRelease
./gradlew assembleProRelease

# Play Store bundle (signed if keystore.properties exists at the project root)
./gradlew bundleProRelease

# Install on a connected device
./gradlew installFreeDebug
./gradlew installProDebug

# Lint / clean
./gradlew lint
./gradlew clean
```

Build config: AGP 8.13 / Gradle 8.13 (needs JDK 17+; Android Studio's bundled JBR works — set `JAVA_HOME` to it), compileSdk 36, targetSdk 36, minSdk 21, Java 11. Dependencies are pure AndroidX (no support libs, no Jetifier needed). Prebuilt native `.so` files live in `app/src/main/jniLibs/` (no NDK build from source); `abiFilters` ships only `armeabi-v7a` and `arm64-v8a` because the x86_64 prebuilts are not 16 KB page aligned — so the app cannot run on x86 emulators, test on a real ARM device.

Release signing reads `keystore.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`) from the project root; it and `*.jks` are gitignored. The upload keystore is kept outside the repo.

There are no unit or instrumentation tests in this project.

## Architecture

### Entry Points

- `LicenseActivity` — **Launcher activity.** Gate in front of everything else (see Licensing below).
- `KeyboardSettingActivity` — Main settings UI (themes, font size, beep/vibrate). NOT the keyboard itself.
- `SoftKeyboard` (extends `InputMethodService`) — The actual keyboard service (~2,200 lines). This is the core of the app.
- `ImePreferences` — System IME settings preference fragment (called from Android Settings).
- `ManualActivity`, `G2PActivity`, `DownloadDataActivity`, `ExtractActivity` — help screen and G2P data setup screens.

### Licensing (product flavors)

`flavorDimensions "license"` defines two flavors, each with its own `BuildConfig.LICENSE_ENABLED` field:

| Flavor | versionName | versionCode | `LICENSE_ENABLED` |
|--------|-------------|-------------|-------------------|
| free   | 1.6         | 160         | `false`           |
| pro    | 1.7.1       | 171         | `true`            |

There are no per-flavor source sets — only `app/src/main`. Flavor behavior is branched at runtime on `BuildConfig.LICENSE_ENABLED`.

`LicenseActivity` flow on launch:
1. `LICENSE_ENABLED == false` (free) → straight to `KeyboardSettingActivity`.
2. A key is already stored in SharedPreferences under `LicenseManager.PREF_LICENSE_KEY` → skip the gate.
3. Otherwise show the activation form: input auto-formats to `XXXXX-XXXXX-XXXXX-XXXXX-XXXXX` (validated by `LicenseManager.isValidFormat`), then an `AsyncTask` POSTs `{"keyNum":"<key without dashes>"}` to `LicenseManager.URL_ACTIVATED`. A `"msg":"OK"` in the JSON response stores the key and continues; anything else clears the stored key and shows an error. Server `msg` constants live in `LicenseManager` (`MSG_OK`, `MSG_CHANGE_SERIAL`).

The keyboard itself is also gated: in the pro flavor, `SoftKeyboard.isLicenseLocked()` replaces the keys with `res/layout/license_lock.xml` (buttons: open `LicenseActivity`, show the IME picker) until a key is stored. Text fields inside this app are exempt so the key can be typed with LD Keyboard itself.

`LicenseManager.BASE_URL` currently points at the UAT host (`https://ld-api.uat.rtt.in.th`) — change it there when switching environments. UAT is the production backend — do not change it for Play releases. License network logs are only emitted when `BuildConfig.DEBUG`.

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

Grapheme-to-Phoneme conversion uses a native library (`g2pLib`). The JNI wrapper is `g2pJni.java` (`com.example.bablueza.g2p` package); `GraphemeToPhoneme.java` wraps it for use in `SoftKeyboard`. G2P data files (`initTTS.in`) are loaded from assets at runtime via `DownloadDataActivity` / `ExtractActivity`.

On first use (first LD-button press) `GraphemeToPhoneme.downloadVoiceData()` sees no data in `files/G2P-data/` and opens `DownloadDataActivity` (copy `g2p-data.zip` from assets → `ExtractActivity` unzips). `getG2P()` runs once per candidate word, so a static guard (`claimDataSetup()` / `releaseDataSetup()` in `DownloadDataActivity.onDestroy`) lets only one setup screen open at a time.

**The G2P logic works correctly — do not change it** (data paths, copy/unzip, `initG2P`, phoneme lookup). Only the surrounding launch/UI code may be touched, and after any change the LD-button suggestions must stay identical (e.g. typing "สนาร" → สนุกสนาน, สรงสนาน, เส้นขนาน, แสงขนาน).

### Databases

`spell_sys.db` and `lexitron_v3.db` are pre-packaged in `app/src/main/assets/databases/` and opened via `SQLiteAssetHelper` (`DictionaryDatabaseHelper`, `LexitronDatabaseHelper`). There are no ContentProviders.

### Settings Persistence

All user preferences are stored in a single `SharedPreferences` file keyed by `"th.or.nectec.twskeyboard"`:
- `bgTheme` — drawable resource ID for keyboard background
- `keyTextSize` — SeekBar progress value (10–100)
- `beepStatus` — vibration feedback toggle
- `start` — first-launch flag
- `font` — TTF font filename
- `license_key` — activated license key (pro flavor only)

### Analytics

None. Google Analytics was removed (GA v3 is shut down); `SoftKeyboard.onTracker()` is a no-op kept to avoid touching its many call sites. The app requests only INTERNET, ACCESS_NETWORK_STATE and VIBRATE.

## Conventions

- UI strings shown to users are Thai; keep new user-facing text in Thai to match.
- The codebase still uses `AsyncTask`, raw `HttpURLConnection`, and `findViewById` casts. Match the surrounding style rather than introducing new async/HTTP stacks.
- Version text on screen comes from `BuildConfig.VERSION_NAME` (`LicenseActivity`, `KeyboardSettingActivity`). Never hardcode a version string in layouts.

## Play Store Release Rules

- **Only the pro flavor goes to Google Play.** A separate coordinator uploads; we prepare a handoff folder (AAB, test APK, graphics, Thai README).
- **versionCode must always increase.** Release history: 170 (1.7) is uploaded already; 171 (1.7.1) is prepared but not uploaded. Changes may still go into 171 until it is uploaded; after that the next release must be ≥172. Bump both `versionCode` and `versionName` in the `pro` block of `app/build.gradle` and the table above.
- **Always sign with the existing upload key** (`keystore.properties` → `.jks` outside the repo). Never generate a new key, and never commit `keystore.properties` or `*.jks`. Check the AAB certificate SHA-256 is `8D:CA:57:CC:1E:30:EA:1D:08:BF:E6:E9:52:E5:7A:35:87:1E:C9:45:54:AA:0D:60:D5:08:B8:B5:7A:26:12:B5`.
- **Do not change `LicenseManager.BASE_URL`** (the UAT host is production).
- Lint vital reports `ResourceCycle` in `keyboard_popup_layout.xml`. This is a known false positive (`popupLayout` is only inflated on long-press; verified on device). Any *other* lint vital error must be fixed.

Build the release (JDK = Android Studio JBR):

```bash
./gradlew clean bundleProRelease makeApkFromBundleForProRelease packageProReleaseUniversalApk
# AAB:            app/build/outputs/bundle/proRelease/app-pro-release.aab
# Split APK set:  app/build/intermediates/apks_from_bundle/proRelease/makeApkFromBundleForProRelease/bundle.apks
# Universal APK:  app/build/outputs/apk_from_bundle/proRelease/app-pro-release-universal.apk  (test only, never upload)
```

Test before every handoff, on a real ARM device, using the APKs generated from the AAB (not `installProDebug`):

1. Static: `jarsigner -verify` + certificate SHA-256 above; `aapt2 dump badging` shows the new versionCode, targetSdk 36, not debuggable, only the 3 permissions; `zipalign -c -P 16 4` passes on the arm64 split.
2. Install the splits Play would deliver: unzip `bundle.apks`, then `adb install-multiple` base-master + ABI + density + th + en (on Android 12+ use `base-master_3` and `base-arm64_v8a_2`). Use `-r` to simulate an update.
3. On device: license screen (wrong key → Thai error; valid key → settings), key survives force-stop and update install, Thai/English typing and suggestions, LD button (first press opens G2P setup once only), long-press `.,?!` popup, keyboard locked without a key, no entries in `adb logcat -b crash`.
4. Uninstalling or clearing app data deletes the stored license key, and re-activating may hit `CHANGE_SERIAL`. Ask before doing that on a device that has a key.

Then copy the AAB (renamed `LDKeyboard-pro-v<versionName>-<versionCode>.aab`) and the universal APK to the handoff folder, move the previous version's files into an old-versions subfolder (don't delete them), and update the README there: file names, SHA-256, release notes, and the test-results table.
