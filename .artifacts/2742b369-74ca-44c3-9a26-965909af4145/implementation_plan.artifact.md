# Implementation Plan: Fix Unresolved reference 'HelpDetailScreen'

The goal is to resolve the compilation error in `MainActivity.kt` by providing the missing `HelpDetailScreen` implementation and its corresponding ViewModel.

## User Review Required

> [!IMPORTANT]
> I will create a new file `HelpDetailScreen.kt` in `com.sahilmaske.peerlearn.ui.home`. This screen will handle "Need Help" posts, allowing users to offer help (via comments) and the post owner to mark those offers as helpful.

> [!IMPORTANT]
> I will rename `Helpdetailviewmodel.java` to `HelpDetailViewModel.kt` because it already contains Kotlin code and should match the standard naming conventions and file extensions for Kotlin files.

## Proposed Changes

### ViewModel & Models

#### [RENAME] `Helpdetailviewmodel.java` to `HelpDetailViewModel.kt`
- Location: `D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/viewmodel/HelpDetailViewModel.kt`
- Purpose: Ensure the file extension matches the Kotlin content.

### UI Components

#### [NEW] [HelpDetailScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/HelpDetailScreen.kt)
- Location: `D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/HelpDetailScreen.kt`
- Purpose: Implement the `HelpDetailScreen` composable function. This screen will display the post details and a list of help offers (comments), with functionality for the post owner to mark offers as helpful.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that the unresolved reference is fixed and the project compiles.

### Manual Verification
- Verify that `HelpDetailScreen` is correctly navigated to when "Offer Help" is clicked on the home screen.
- Verify that comments can be added and marked as helpful by the post owner.
