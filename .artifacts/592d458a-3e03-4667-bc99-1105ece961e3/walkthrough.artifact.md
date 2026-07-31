# Walkthrough - Notification Navigation Fix

I have fixed the application crash that occurred when clicking the notification icon on the Home screen.

## Changes

### Navigation

#### [MainActivity.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/MainActivity.kt)
- Registered the `"notifications"` route in the `NavHost`.
- Added the `NotificationScreen` composable to handle navigation to the notifications view.

## Verification Results

### Automated Tests
- N/A (UI navigation check)

### Manual Verification
- The crash was caused by `IllegalArgumentException: Navigation destination that matches request NavDeepLinkRequest{ uri=android-app://androidx.navigation/notifications } cannot be found`.
- By adding `composable("notifications") { NotificationScreen() }` to the `NavHost`, this request now resolves correctly.
