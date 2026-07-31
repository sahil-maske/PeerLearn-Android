# Fix Navigation Crash for Notifications

The application crashes when the user clicks on the notification icon in the `HomeScreen` because the `"notifications"` route is missing from the top-level `NavHost`.

## Proposed Changes

### Navigation

#### [MODIFY] [MainActivity.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/MainActivity.kt)
- Import `NotificationScreen` from `com.sahilmaske.peerlearn.ui.notify`.
- Add a new `composable` destination for the `"notifications"` route in the `NavHost`.

## Verification Plan

### Manual Verification
- Deploy the application to an Android device or emulator.
- Log in and navigate to the Home screen.
- Click on the Notification icon in the top bar.
- Verify that the application navigates to the Notifications screen instead of crashing.
