# Fix Firebase Messaging Dependency Issue

The project is failing to build because `com.google.firebase:firebase-messaging-ktx` is declared without a version and is not correctly managed by the Firebase BOM in `app/build.gradle.kts`. Additionally, there are redundant and hardcoded Firebase BOM declarations.

## Proposed Changes

### [Gradle Configuration]

#### [MODIFY] [libs.versions.toml](file:///D:/PeerLearn2/gradle/libs.versions.toml)
- Add `firebase-messaging` to the `[libraries]` section so it can be managed via the version catalog and the Firebase BOM.

#### [MODIFY] [app/build.gradle.kts](file:///D:/PeerLearn2/app/build.gradle.kts)
- Remove the hardcoded `implementation("com.google.firebase:firebase-messaging-ktx")`.
- Remove redundant and hardcoded `implementation(platform("com.google.firebase:firebase-bom:33.7.0"))`.
- Add `implementation(libs.firebase.messaging)` to the dependencies.
- Clean up duplicate `implementation(platform(libs.firebase.bom))` declarations.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify the project builds successfully.
- Run `Gradle Sync` to ensure the IDE resolves all dependencies.

### Manual Verification
- Verify that `FCMService.kt` still compiles without errors after switching from `-ktx` to the main dependency.
