# Fix Unresolved reference 'ChatConversationScreen'

The project is currently failing to build because `MainActivity.kt` references `ChatConversationScreen`, which does not exist in the codebase. Additionally, the navigation graph in `MainActivity.kt` contains nested `composable` calls that are syntactically incorrect.

## User Review Required

> [!IMPORTANT]
> I will be creating a new `ChatConversationScreen` component as a placeholder to resolve the build error. You will need to implement the actual chat logic later.

## Proposed Changes

### Core Navigation & UI

#### [MODIFY] [MainActivity.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/MainActivity.kt)
- Fix the `see_all_peers` route to correctly call `SeeAllPeersScreen`.
- Move the `chat_conversation` route to the top level of the `NavHost`.
- Ensure proper imports for the new screen.

#### [MODIFY] [ChatScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/ChatScreen.kt)
- Update `ChatScreen` to accept `NavController` as a parameter, as it is already being passed in `NaviScreen.kt`.

#### [NEW] [ChatConversationScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/ChatConversationScreen.kt)
- Create a new composable `ChatConversationScreen` that takes `chatId` and `onBack` callback to resolve the unresolved reference.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that the unresolved reference error is gone and the project compiles successfully.

### Manual Verification
- Deploy the app to a device/emulator and verify that navigating to "See All Peers" works and doesn't crash.
