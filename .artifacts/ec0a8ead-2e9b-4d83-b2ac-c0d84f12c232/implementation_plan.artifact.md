# Implementation Plan - Fix Navigation to VerifyEmailScreen

The goal is to fix the compilation error in `AccountScreen.kt` and implement the navigation to `VerifyEmailScreen` when the "VERIFY EMAIL" button is clicked.

## Proposed Changes

### [AccountScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/Settings/AccountScreen.kt)

#### [MODIFY] [AccountScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/Settings/AccountScreen.kt)
- Standardize the callback parameter name to `onVerifyEmailClick`.
- Fix the unresolved reference by using the standardized name in the `clickable` modifier.

### [MainActivity.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/MainActivity.kt)

#### [MODIFY] [MainActivity.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/MainActivity.kt)
- Update the `onVerifyEmailClick` lambda in the `account` composable to navigate to the `"verify_email"` route.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to ensure all references are resolved and the project builds.

### Manual Verification
- Render `AccountScreenPreview` to verify it compiles.
- The user can verify the navigation on device/emulator by clicking "VERIFY EMAIL" on the Account screen.
