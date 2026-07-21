# Implementation Plan - Fix Errors in PeerSuggestionCard.kt

The `PeerSuggestionCard.kt` file has several compilation errors due to missing fields in the `PeerSuggestion` model and typos in the UI code.

## Proposed Changes

### [app]

#### [MODIFY] [PeerSuggestion.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/model/PeerSuggestion.kt)
- Add `institution: String = ""` to the `PeerSuggestion` data class.
- Add `wantSkill: String = ""` to the `PeerSuggestion` data class (or use `learnSkill`). To minimize changes in `PeerSuggestionCard.kt` and keep it consistent with its intent, I will add `institution` and `wantSkill`.

#### [MODIFY] [PeerSuggestionCard.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/HomeScreenComponents/PeerSuggestionCard.kt)
- Fix typo: change `peer.collage` to `peer.institution` on line 105.
- (Optional but recommended) Change package name to lowercase `homescreencomponents` to resolve the warning, but I'll focus on errors first. Actually, the user asked to "remove the all error", so I'll stick to errors.

#### [MODIFY] [FeedViewModel.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/viewmodel/FeedViewModel.kt)
- Fetch `college` from Firestore and map it to `institution` in `PeerSuggestion`.
- Map `learningSkills` to `wantSkill` in `PeerSuggestion`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that all errors are resolved.
