# Implementation Plan: Fix GMS SecurityException and Firestore Deserialization Crash

The application is facing two critical issues:
1.  **SecurityException:** `Unknown calling package name 'com.google.android.gms'`. This is caused by a mismatch in GMS configuration and experimental SDK versions.
2.  **RuntimeException:** `Failed to convert a value of type com.google.firebase.Timestamp to long`. This is caused by Firestore data being stored as a `Timestamp` but the Kotlin model expecting a `Long`.

## User Review Required

> [!IMPORTANT]
> **Experimental SDK Versions:** The project uses `agp = "9.3.0"`, `compileSdk = 37`, and `targetSdk = 37`. I will downgrade these to stable versions (AGP 8.7.3, SDK 35) to ensure compatibility.

> [!WARNING]
> **SHA-1 Fingerprint:** The `DEVELOPER_ERROR` in GMS logs suggests that the SHA-1 fingerprint of your signing key might not be registered in the Firebase Console. You should:
> 1. Run `./gradlew signingReport` to get your SHA-1.
> 2. Add it to your project in [Firebase Console](https://console.firebase.google.com/).
> 3. Download the updated `google-services.json` and replace the one in `app/`.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/PeerLearn2/gradle/libs.versions.toml)
- Downgrade `agp` from `9.3.0` to `8.7.3`.
- Downgrade `google-services` plugin to `4.4.2`.

#### [MODIFY] [build.gradle.kts (app)](file:///D:/PeerLearn2/app/build.gradle.kts)
- Downgrade `compileSdk` and `targetSdk` to `35`.
- Remove explicit `play-services-base` dependency to avoid version conflicts.

### Manifest Configuration

#### [MODIFY] [AndroidManifest.xml](file:///D:/PeerLearn2/app/src/main/AndroidManifest.xml)
- Add GMS version metadata: `<meta-data android:name="com.google.android.gms.version" android:value="@integer/google_play_services_version" />`.

### Firestore & Data Models

#### [MODIFY] [ConnectionViewModel.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/viewmodel/ConnectionViewModel.kt)
- Standardize all timestamp writes to use `System.currentTimeMillis()` (consistent with `Post` and `User` models).
- Update real-time listeners (`listenIncomingRequests` and `listenSwapRequests`) to safely handle both `Long` and `Timestamp` values from Firestore. This prevents crashes if existing data contains `Timestamp` objects.

#### [MODIFY] [Message.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/model/Message.kt)
- Remove the incorrect `import java.security.Timestamp`.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify the build passes.

### Manual Verification
- Deploy the app and navigate to the "Alerts" tab.
- Verify that the app no longer crashes when fetching connection requests.
- Check Logcat for any remaining `SecurityException` or `GoogleApiManager` errors.
