# PassVault

A fully offline, client-side encrypted password manager for Android, built as a security-focused mobile learning project. Every credential is encrypted at rest on the device using a layered key-wrapping scheme (master password → derived key → Keystore-wrapped database passphrase), with no backend, no cloud sync, and no server component of any kind.

**Package:** `com.passvault.app`

## Features

- 🔑 Single master password to unlock the entire vault, verified via PBKDF2 (210,000 iterations) — never stored in plaintext
- 🔒 AES-encrypted local database (SQLCipher) — the raw `.db` file is unreadable without the app's derived key
- 👆 Optional biometric unlock (fingerprint/face), restricted to `BIOMETRIC_STRONG` sensors only
- ⏱️ Configurable auto-lock (immediate / 30s / 1min / 5min) when the app goes to background
- 🐢 Exponential backoff on failed master-password attempts, throttling brute-force without permanent lockout
- 🆘 One-time recovery code, shown once at account creation, to restore access if the master password is forgotten
- 🗂️ Two entry types:
  - **Password**: title, username, password, optional reminder question — password is view-on-demand only, never copyable to clipboard
  - **Secret Question**: stores a visible question plus a salted PBKDF2 hash of the answer. The answer itself is never stored or shown — only verifiable, returning a ✅/❌ result
- ⭐ Categories and favorites, with live filtering on the main list
- 🕵️ Built-in security audit flagging weak and reused passwords
- ✍️ Native Android Autofill Framework integration — fills credentials into other apps without copy/paste, gated behind the same lock/unlock session as the main app
- 💾 Encrypted export/import (AES-GCM + PBKDF2), protected by a separate export password independent of the master password
- ⏳ Loading indicators on every operation that involves cryptographic key derivation, so the UI never appears frozen during PBKDF2 work

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Hilt (dependency injection), Coroutines & Flow |
| Navigation | Jetpack Navigation Compose |
| Local database | Room over SQLCipher (encrypted-at-rest SQLite) |
| Key storage | Android Keystore (hardware-backed key wrapping where supported) |
| Biometrics | `androidx.biometric` (BiometricPrompt) |
| Autocomplete | Android Autofill Framework (native service) |
| Background work | WorkManager |
| Cryptography | PBKDF2 (key derivation), AES-GCM (authenticated encryption) |
| Release hardening | R8/ProGuard (obfuscation + shrinking) |
| Testing | JUnit (unit), Espresso + Compose UI Testing (instrumented) |

**Build config:** AGP 9.2.1 · Kotlin 2.2.10 · Room 2.8.4 · SQLCipher 4.17.0 · Hilt 2.59.2 · Navigation-Compose 2.9.7 · `compileSdk`/`targetSdk` 37 · `minSdk` 26

## Architecture Overview

```
com.passvault.app/
├── data/           Room entities, DAO, type converters, mappers, repository implementation
├── domain/         Domain models (PasswordEntry, EntryType) and repository interfaces
├── security/       All cryptography and session logic:
│                   MasterPasswordManager, KeyDerivation, PassphraseManager,
│                   BiometricKeyManager, BiometricPreferenceManager,
│                   UnlockSessionManager, AutoLockPreferenceManager,
│                   AppLifecycleObserver, SecretAnswerHasher,
│                   ExportManager, ImportManager, AesGcmUtils,
│                   RecoveryCodeManager, RecoveryCodeGenerator
├── ui/             One screen + ViewModel per feature (Compose)
│   └── navigation/    NavGraph, destinations, protected-route redirection
├── autofill/       PassVaultAutofillService, AutofillStructureParser,
│                   AutofillFieldClassifier, AutofillUnlockActivity/Screen/ViewModel
└── di/             Hilt modules
```

Each screen follows a ViewModel + repository pattern, with Room queries exposed as `Flow`s and collected reactively in Compose via `collectAsState()`. Transient state that must survive a system-triggered process death (the SAF file picker for export/import, the Autofill authentication activity) is kept in `SavedStateHandle` rather than Compose's in-memory `remember`, since some OEM Android builds (observed on certain MIUI devices) kill the app process while a system picker is open.

## Security Model

| Mechanism | Purpose |
|---|---|
| PBKDF2, 210,000 iterations | Master password verification is deliberately slow, making brute-force and dictionary attacks impractical even if the stored verifier leaks |
| SQLCipher (AES) at-rest encryption | The database file is unreadable without the derived key — confirmed by pulling the raw `.db` via Device File Explorer and verifying it does not start with the `SQLite format 3` signature |
| Keystore-wrapped database passphrase | The database's own encryption key is never stored in plaintext; it is wrapped by a key held in the Android Keystore |
| `BIOMETRIC_STRONG`-only biometric unlock | Rejects Class 2 ("weak") fingerprint sensors that don't meet Android's hardware security bar for unlocking sensitive data |
| Salted PBKDF2 hash for Secret Question answers | The answer is never stored or displayed — only a hash used for pass/fail verification, the same principle used for OS-level password storage |
| AES-GCM + PBKDF2 for backups | A leaked export file is useless without a second, independent password the attacker is unlikely to also have |
| Exponential backoff | Throttles repeated failed unlock attempts without permanently locking out the legitimate user |
| `allowBackup="false"` | Prevents credential extraction via ADB or OEM cloud backup |
| `FLAG_SECURE` on sensitive screens | Blocks screenshots and the Recents app-switcher thumbnail |
| R8/ProGuard in release builds | Obfuscation and code shrinking, raising the cost of static analysis of the APK |
| No clipboard copy for passwords | The clipboard-copy-with-auto-clear feature was removed after confirming clipboard clearing is unreliable across devices/keyboards; passwords are view-on-demand only |

## Requirements

- Android Studio (compatible with AGP 9.2.1 / Kotlin 2.2.10)
- A JDK compatible with the project's Gradle configuration
- Android SDK with `compileSdk`/`targetSdk` 37 installed
- A physical device or emulator running API 26 (Android 8.0) or higher

## Setup

1. Clone the repository and open it in Android Studio.
2. Let Gradle sync the dependencies.
3. Select a device/emulator and run (`Run ▶`).

To test on a physical device from a clean state (recommended after any database schema change):

```
adb uninstall com.passvault.app
```

then reinstall from Android Studio.

> **Note:** the project currently uses `fallbackToDestructiveMigration` in Room during development. Bumping the database version wipes local data on first launch after the change — expected at this stage, not production-ready without real migrations.

## Running Tests

```
./gradlew testDebugUnitTest             # unit tests (JVM)
./gradlew connectedDebugAndroidTest      # instrumented tests (device/emulator required)
```

Test coverage includes key derivation determinism, unlock session management, failed-attempt backoff, biometric key/preference management (requires an enrolled fingerprint on the emulator: `adb -e emu finger touch 1`), Room persistence and queries, encrypted export/import round-trips, and Autofill field classification.

## Known Limitations

- **No clipboard copy for passwords** — removed by design after the auto-clear mechanism proved unreliable on some devices/keyboards; passwords can only be viewed on-screen.
- **Destructive migrations** — schema version bumps currently wipe local data on first launch; no incremental migration path is implemented yet.
- **Hardware-limited biometrics** — devices with Class 2 ("weak") fingerprint sensors cannot use biometric unlock; this is an intentional security restriction, not a bug.
- **MIUI Autofill quirks** — on some older MIUI builds (observed on Android 10), aggressive background process management can make the Autofill authentication screen launch intermittently.
- **No cross-device sync** — each install is fully independent; the only way to move data between devices is a manual encrypted export/import.

## Development Notes

The project was built incrementally across 9+ blocks: foundational setup, database/key derivation, master password and recovery codes, biometrics, auto-lock and clipboard hardening, general hardening (ProGuard, `allowBackup`, backoff), categories/favorites/audit/export-import/Autofill, testing and multi-device verification, and a final round adding the Secret Question entry type, loading indicators, and fixes for export/import reliability on devices that kill the app process during the system file picker.

## License

This is a learning/portfolio project. No license has been assigned.
