# Implementation Plan - Add Navigation to Verify Email Screen

The user reported that clicking "VERIFY EMAIL" in the `AccountScreen` does not navigate to the `VerifyEmailScreen`. Upon investigation, I found that the `VerifyEmailScreen` is implemented but not registered in the navigation graph in `MainActivity.kt`. Currently, the `onVerifyEmailClick` callback in `MainActivity.kt` only triggers a Firebase email verification request instead of navigating to the dedicated screen.

## User Review Required

> [!IMPORTANT]
> I will register the route as `"verify_email"`. If you prefer a different route name, please let me know.

## Proposed Changes

### Navigation Setup

#### [MODIFY] [MainActivity.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/MainActivity.kt)
- Import `com.sahilmaske.peerlearn.ui.Settings.VerifyEmailScreen`.
- Add a new `composable` route for `"verify_email"`.
- Update the `onVerifyEmailClick` lambda in the `"account"` route to navigate to `"verify_email"`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to ensure the project compiles with the new navigation route and imports.

### Manual Verification
- The user should verify that clicking the "VERIFY EMAIL" row in the Account screen now opens the Verify Email screen.
