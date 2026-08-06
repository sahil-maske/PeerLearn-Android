# Implementation Plan: Fix Unresolved reference 'HelpDetailScreen'

The goal is to resolve the compilation error `Unresolved reference 'HelpDetailScreen'` in `MainActivity.kt` by creating the missing `HelpDetailScreen` composable and ensuring its ViewModel is correctly named.

## User Review Required

> [!IMPORTANT]
> I will create a new file `HelpDetailScreen.kt` in `com.sahilmaske.peerlearn.ui.home`. This screen will display a "Need Help" post and its comments (help offers). It will allow users to post new help offers and allow the post owner to mark specific offers as helpful.

> [!IMPORTANT]
> I will rename `Helpdetailviewmodel.java` to `HelpDetailViewModel.kt`. The file currently contains Kotlin code but has a `.java` extension, which is inconsistent and can lead to confusion.

## Proposed Changes

### ViewModel & Models

#### [RENAME] `Helpdetailviewmodel.java` to `HelpDetailViewModel.kt`
- Location: `D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/viewmodel/HelpDetailViewModel.kt`
- Purpose: Correct the file extension for the Kotlin code.

### UI Components

#### [NEW] [HelpDetailScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/HelpDetailScreen.kt)
- Location: `D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/HelpDetailScreen.kt`
- Purpose: Implement the `HelpDetailScreen` composable.
- Key Features:
    - Display the "Need Help" post content.
    - List comments as "Help Offers".
    - Allow post owners to mark comments as "Helpful" (toggling the `isMarkedHelpful` flag).
    - Text field to add new help offers.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that the unresolved reference is fixed and the project compiles.

### Manual Verification
- Verify navigation from the Home screen's "Offer Help" button to the `HelpDetailScreen`.
- Verify that comments are displayed and the "Mark as Helpful" toggle works for the post owner.
