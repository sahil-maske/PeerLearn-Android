# Implementation Plan: Fix GMS SecurityException

Fixing `java.lang.SecurityException: Unknown calling package name 'com.google.android.gms'` by standardizing SDK versions and cleaning up build configurations.

## User Review Required

> [!IMPORTANT]
> The project currently uses `compileSdk` and `targetSdk` version **37**. This is likely the cause of the `SecurityException` as it exceeds current stable Android releases (Android 15 is SDK 35). I will downgrade these to **35**.

> [!WARNING]
> The `google-services.json` file is missing `oauth_client` entries. If authentication still fails after these changes, you must register your SHA-1 fingerprint in the Firebase Console and update the `google-services.json` file.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/PeerLearn2/gradle/libs.versions.toml)
- Downgrade `agp` from `9.3.0` to `8.8.0` (Stable).
- Ensure `google-services` plugin is at a stable version (e.g., `4.4.2`).
- Downgrade `play-services-base` to `18.5.0` to ensure compatibility with GMS on most devices.

#### [MODIFY] [build.gradle.kts](file:///D:/PeerLearn2/app/build.gradle.kts)
- Set `compileSdk` to `35`.
- Set `targetSdk` to `35`.
- Remove duplicate `io.coil-kt:coil-compose` entries.
- Ensure `com.google.gms.google-services` plugin is applied correctly.

#### [MODIFY] [build.gradle.kts](file:///D:/PeerLearn2/build.gradle.kts) (Root)
- Update `google-services` plugin version to `4.4.2`.

### Android Manifest

#### [MODIFY] [AndroidManifest.xml](file:///D:/PeerLearn2/app/src/main/AndroidManifest.xml)
- Review and keep the `<queries>` block to ensure GMS visibility.

## Verification Plan

### Automated Tests
- `gradlew assembleDebug` to verify the build completes successfully with downgraded versions.

### Manual Verification
- Launch the app and check if the `SecurityException` persists during `FirebaseAuth.getInstance()` calls (e.g., on the Login screen).
