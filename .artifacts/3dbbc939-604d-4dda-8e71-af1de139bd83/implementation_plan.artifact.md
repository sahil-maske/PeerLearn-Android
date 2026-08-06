# Implementation Plan: Fix All Project Errors

This plan aims to resolve all current compilation and runtime errors in the project, including build configuration issues, missing files, and incorrect type mappings.

## User Review Required

> [!IMPORTANT]
> **Build Configuration:** I will downgrade AGP, SDK, and Google Services plugin versions to stable releases to ensure compatibility and fix the `SecurityException`.
> - AGP: `9.3.0` -> `8.7.3`
> - SDK: `37` -> `35`
> - Google Services: `4.5.0` -> `4.4.2`

> [!WARNING]
> **HelpDetailScreen:** The `HelpDetailScreen` was referenced in `MainActivity.kt` but its definition was missing. I will create a placeholder implementation in `com.sahilmaske.peerlearn.ui.home` to resolve the unresolved reference.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/PeerLearn2/gradle/libs.versions.toml)
- Downgrade `agp` to `8.7.3`.

#### [MODIFY] [build.gradle.kts (app)](file:///D:/PeerLearn2/app/build.gradle.kts)
- Downgrade `compileSdk` and `targetSdk` to `35`.
- Remove explicit `play-services-base` dependency to avoid conflicts with Firebase BOM.

#### [MODIFY] [build.gradle.kts (root)](file:///D:/PeerLearn2/build.gradle.kts)
- Downgrade `google-services` plugin to `4.4.2`.

### Manifest & System

#### [MODIFY] [AndroidManifest.xml](file:///D:/PeerLearn2/app/src/main/AndroidManifest.xml)
- Add GMS version metadata: `<meta-data android:name="com.google.android.gms.version" android:value="@integer/google_play_services_version" />`.

### Code & Components

#### [RENAME] `Helpdetailviewmodel.java` to `HelpDetailViewModel.kt`
- Fix the file extension to match the Kotlin code inside.

#### [NEW] [HelpDetailScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/HelpDetailScreen.kt)
- Create a functional placeholder for the help detail view.

#### [MODIFY] [Message.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/model/Message.kt)
- Remove incorrect `java.security.Timestamp` import.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify the build passes.
- Use `analyze_file` on `MainActivity.kt` to ensure `HelpDetailScreen` is resolved.

### Manual Verification
- Deploy the app and navigate through Home, Alerts, and Profile screens to ensure no runtime crashes occur.
