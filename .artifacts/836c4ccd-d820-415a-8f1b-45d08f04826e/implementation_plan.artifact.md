# Fix Firebase Messaging Dependency Resolution

The project is failing to build because `com.google.firebase:firebase-messaging-ktx` is declared without a version and is not correctly managed by a Firebase Bill of Materials (BOM) in the `build.gradle.kts` file. Additionally, the `-ktx` artifacts for Firebase are deprecated as their functionality has been merged into the main artifacts.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/PeerLearn2/gradle/libs.versions.toml)
- Add `firebase-messaging` to the `[libraries]` section, managed by the Firebase BOM.

#### [MODIFY] [build.gradle.kts](file:///D:/PeerLearn2/app/build.gradle.kts)
- Remove the hardcoded `implementation("com.google.firebase:firebase-messaging-ktx")`.
- Remove the redundant hardcoded `implementation(platform("com.google.firebase:firebase-bom:33.7.0"))`.
- Add `implementation(libs.firebase.messaging)` to the dependencies.
- Ensure the Firebase BOM from the version catalog is applied before other Firebase dependencies.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify that the project builds successfully and dependencies are resolved.
