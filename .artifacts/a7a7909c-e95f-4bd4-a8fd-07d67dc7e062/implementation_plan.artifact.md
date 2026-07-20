# Implementation Plan - Fix Firebase Initialization Error in Compose Preview

The `NaviScreen` and its sub-screens (like `HomeScreen`) are failing to render in Android Studio Preview because they instantiate ViewModels that depend on Firebase services (Firestore, Auth) which are not initialized during preview.

## Proposed Changes

### [MODIFY] [NaviScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/NaviScreen.kt)

- Refactor `NaviScreen` to separate ViewModel logic from UI layout.
- Introduce `NaviScreenContent` as a stateless composable that takes the current selection and a content lambda.
- Update `HomeScreenPreview` to use `NaviScreenContent` with mock data, avoiding ViewModel instantiation.

### [MODIFY] [ProfileScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/ProfileScreen.kt)

- Although the primary issue is in `NaviScreen`'s preview, `ProfileScreen` also has similar issues. I will ensure its preview is also safe or that it's handled when previewing `NaviScreen`. (Actually, I'll focus on `NaviScreen` first as it's the requested fix).

## Verification Plan

### Manual Verification
- Verify that `HomeScreenPreview` in `NaviScreen.kt` renders correctly in Android Studio without the Firebase initialization error.
- Ensure the app still functions correctly on a device/emulator.
