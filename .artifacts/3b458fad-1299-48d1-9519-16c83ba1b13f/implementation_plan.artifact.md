# Implementation Plan - Fix NaviScreen Preview Render Issue

The `NaviScreen` Compose Preview is failing because it attempts to instantiate `FeedViewModel` and `ProfileViewModel` using default parameters. These ViewModels access Firebase services (`FirebaseFirestore`, `FirebaseAuth`) in their constructors or initialization blocks, which are not available during Preview rendering.

## User Review Required

> [!IMPORTANT]
> I will refactor `NaviScreen` to separate the ViewModel logic from the UI layout. This will involve creating a `NaviScreenContent` Composable that takes data and sub-screen lambdas, making it easily previewable with mock data.

## Proposed Changes

### UI Components

#### [MODIFY] [NaviScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/NaviScreen.kt)
- Extract the core UI logic of `NaviScreen` into a new Composable called `NaviScreenContent`.
- `NaviScreenContent` will accept parameters for:
    - Current selected item state.
    - User profile data.
    - Composable lambdas for each tab screen (`HomeScreen`, `QAScreen`, etc.).
    - Callback for item selection.
- Update `NaviScreen` to be a lightweight wrapper that manages ViewModels and passes state to `NaviScreenContent`.
- Update `HomeScreenPreview` to use `NaviScreenContent` with mock data and empty lambdas for sub-screens to avoid ViewModel instantiation.

## Verification Plan

### Manual Verification
- Verify that the `HomeScreenPreview` in `NaviScreen.kt` renders correctly in Android Studio without the Firebase initialization error.
- Ensure the app still functions correctly when running on a device/emulator.
