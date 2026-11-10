# BUILD & INSTALLATION GUIDE — ZAID ASHIQ X ULTRA V. CODER

## 1. Prerequisites

- Android SDK (Compile SDK 36, Min SDK 24)
- Java 11+ / Kotlin 2.2+
- Gradle (configured in project)

## 2. Building the APKs

### Debug APK
```bash
gradle :app:assembleDebug
```
Output location:
`app/build/outputs/apk/debug/app-debug.apk`

### Signed Release APK
```bash
gradle :app:assembleRelease
```
Output location:
`app/build/outputs/apk/release/app-release.apk`

## 3. Running Runtime & Unit Verification

```bash
bash scripts/verify-runtime.sh
```
Or directly via Gradle:
```bash
gradle :app:testDebugUnitTest
```

## 4. Embedded Runtime Compatibility Note

Android 10+ (API 29–36) enforces strict SELinux W^X (`app_data_file` execute prohibition) and Google Play prohibits downloading external executable native code at runtime. ZAID ASHIQ X ULTRA V. CODER addresses this transparently via its **Hybrid In-APK JS Sandbox + Local Node IPC Bridge (`127.0.0.1:19480`)**, bundling all engine JS modules in `app/src/main/assets/node/` and `app/src/main/assets/plugins/` while executing cryptographic framing and WebSocket transport natively on-device.
