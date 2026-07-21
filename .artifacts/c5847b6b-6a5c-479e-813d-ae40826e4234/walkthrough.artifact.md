# Walkthrough - Fix Errors in PeerSuggestionCard.kt

I have fixed the compilation errors in `PeerSuggestionCard.kt` by updating the `PeerSuggestion` model and correcting field references in the UI and ViewModel.

## Changes

### [app]

#### [PeerSuggestion.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/model/PeerSuggestion.kt)
- Added `institution` field to match the requirements of `PeerSuggestionCard`.

#### [PeerSuggestionCard.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/HomeScreenComponents/PeerSuggestionCard.kt)
- Fixed typo: changed `peer.collage` to `peer.institution`.
- Updated to use the existing `learnSkill` field instead of the undefined `wantSkill`.

#### [FeedViewModel.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/viewmodel/FeedViewModel.kt)
- Updated mapping logic to fetch `college` from Firestore and assign it to `institution` in `PeerSuggestion`.

## Verification Results

### Automated Tests
- Analyzed `PeerSuggestionCard.kt` and confirmed that all compilation errors are resolved. Only a package naming warning remains.
