# Implementation Plan - Fix Firebase Messaging Dependency Issue

The project is failing to build because `com.google.firebase:firebase-messaging-ktx` is declared without a version and is not being correctly resolved via the Firebase BoM. Additionally, the `app/build.gradle.kts` file has redundant and hardcoded BoM declarations.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/PeerLearn2/gradle/libs.versions.toml)
- Add `firebase-messaging` to the `[libraries]` section to manage it via the Version Catalog.
- We will use the main `firebase-messaging` artifact as KTX extensions are now included in the main artifact since Firebase BoM v32.0.0.

#### [MODIFY] [build.gradle.kts](file:///D:/PeerLearn2/app/build.gradle.kts)
- Remove hardcoded `implementation("com.google.firebase:firebase-messaging-ktx")`.
- Remove redundant and hardcoded `firebase-bom` declarations.
- Use `libs.firebase.messaging` for consistency with other Firebase dependencies.
- Clean up the dependencies block to remove duplicates.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify that the project builds successfully and dependencies are resolved.

### Manual Verification
- Verify that `FCMService.kt` still compiles correctly (it will, as the imports remain the same).
