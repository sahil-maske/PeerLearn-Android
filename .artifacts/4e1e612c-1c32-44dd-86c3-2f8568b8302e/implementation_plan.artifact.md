# Fix for SecurityException: Unknown calling package name 'com.google.android.gms'

The application is encountering a `java.lang.SecurityException: Unknown calling package name 'com.google.android.gms'` when interacting with Google services (likely via Firebase). This error, specifically mentioning `com.google.android.gms` as the calling package, usually indicates a deep configuration mismatch, often caused by using experimental or future SDK/plugin versions that are not fully supported by the Google Play Services on the device/emulator.

## User Review Required

> [!IMPORTANT]
> **SDK and Plugin Versions:** The project is currently targeting `compileSdk 37` and `targetSdk 37`, and using Android Gradle Plugin (AGP) `9.3.0`. These versions are beyond current stable releases (Android 15 is API 35). I will downgrade these to stable versions to ensure compatibility with Google Play Services.

> [!WARNING]
> **Firebase/Google Configuration:** The `google-services.json` file is missing `oauth_client` entries for the Android client. This typically happens when the SHA-1 fingerprint of your signing key has not been added to the Firebase Console. While I can fix the code configuration, you may still need to:
> 1. Go to [Firebase Console](https://console.firebase.google.com/).
> 2. Add your SHA-1 fingerprint (obtainable via `./gradlew signingReport`).
> 3. Download the updated `google-services.json` and replace the existing one in `app/`.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/PeerLearn2/gradle/libs.versions.toml)
- Downgrade `agp` to a stable version (e.g., `8.7.3`).
- Downgrade `compileSdk` and `targetSdk` to `35`.
- Downgrade `google-services` plugin version to `4.4.2`.

#### [MODIFY] [build.gradle.kts (app)](file:///D:/PeerLearn2/app/build.gradle.kts)
- Remove explicit dependency on `com.google.android.gms:play-services-base:18.10.0` to allow the Firebase BOM to manage it.
- Remove duplicate `coil-compose` dependency.
- Fix `buildTypes` optimization block (use `isMinifyEnabled` standard).

### Manifest Configuration

#### [MODIFY] [AndroidManifest.xml](file:///D:/PeerLearn2/app/src/main/AndroidManifest.xml)
- Add the `com.google.android.gms.version` meta-data tag to ensure proper service linking.
- Ensure the `queries` section is correctly positioned and not conflicting.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure the project builds with the new versions.
- The `SecurityException` is a runtime error, so manual verification on a device/emulator is required.

### Manual Verification
1. Build and run the app.
2. Observe the logs for the `SecurityException`.
3. If the error persists, verify the SHA-1 registration in Firebase Console as mentioned in the Warning above.
