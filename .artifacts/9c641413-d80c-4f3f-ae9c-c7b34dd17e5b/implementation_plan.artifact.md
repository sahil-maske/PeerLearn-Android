# Implementation Plan: Fix Unresolved reference 'HelpDetailScreen'

The goal is to resolve the compilation error `Unresolved reference 'HelpDetailScreen'` in `MainActivity.kt` by providing the missing `HelpDetailScreen` implementation and fixing the incorrect file extension for `HelpDetailViewModel`.

## User Review Required

> [!IMPORTANT]
> I will create a new file `HelpDetailScreen.kt` in `com.sahilmaske.peerlearn.ui.home`. This screen will handle "Need Help" posts, allowing users to offer help (via comments) and the post owner to mark those offers as helpful.

> [!IMPORTANT]
> I will rename `Helpdetailviewmodel.java` to `HelpDetailViewModel.kt` because it contains Kotlin code and should match standard naming conventions.

## Proposed Changes

### ViewModel & Models

#### [RENAME] `Helpdetailviewmodel.java` to `HelpDetailViewModel.kt`
- Location: `D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/viewmodel/HelpDetailViewModel.kt`
- Purpose: Correct the file extension for the Kotlin code.

### UI Components

#### [NEW] [HelpDetailScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/HelpDetailScreen.kt)
- Location: `D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/HelpDetailScreen.kt`
- Purpose: Implement the `HelpDetailScreen` composable function.
- Features:
    - Display the full "Need Help" post details.
    - List comments as "Help Offers".
    - Allow users to post new help offers (comments).
    - If the current user is the post owner, show a "Mark as Helpful" button on each comment.
    - Visually highlight offers that have been marked as helpful.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that the unresolved reference is fixed and the project compiles.

### Manual Verification
- Deploy the app.
- Click "Offer Help" on a "Need Help" post from the Home feed.
- Verify navigation to `HelpDetailScreen`.
- Add a comment and verify it appears.
- Log in as the post owner and verify the ability to mark a comment as helpful.
