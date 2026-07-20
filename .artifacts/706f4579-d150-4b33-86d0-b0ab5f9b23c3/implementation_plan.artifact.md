# Implementation Plan - Fix NaviScreen Render Issues

The `NaviScreen` Compose Preview is failing because it tries to instantiate `FeedViewModel` and `ProfileViewModel` using default parameters. `FeedViewModel` initializes `FirebaseFirestore.getInstance()` in its constructor, which is not supported in the Android Studio Preview environment. Additionally, `NaviScreen` uses `FirebaseAuth` in a `LaunchedEffect`.

## Proposed Changes

### [Component Name] UI Layer

#### [MODIFY] [NaviScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/NaviScreen.kt)

- **Extract Stateless Composable**: Create `NaviScreenContent` which will contain the UI structure (Bottom Nav, Animated Content) but accept its state and screen content as parameters.
- **Refactor `NaviScreen`**: Update the stateful `NaviScreen` to use `NaviScreenContent`, passing the actual ViewModels and navigation logic.
- **Update Preview**: Update `HomeScreenPreview` to use `NaviScreenContent` with mock data and dummy screen content, bypassing the ViewModel initialization and Firebase calls.

## Verification Plan

### Manual Verification
- Verify that the `HomeScreenPreview` in `NaviScreen.kt` renders correctly in Android Studio.
- Verify that the app still works as expected when running on a device (to ensure the refactoring didn't break the actual app logic).
