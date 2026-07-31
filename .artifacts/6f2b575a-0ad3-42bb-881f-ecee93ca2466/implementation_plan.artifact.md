# Fix Unresolved reference 'ChatConversationScreen' and Navigation Structure

The project is currently failing to build because `MainActivity.kt` references a non-existent `ChatConversationScreen` component and has an incorrect nested navigation structure. Additionally, `ChatScreen` is being called with an unexpected parameter in `NaviScreen.kt`.

## Proposed Changes

### UI Components

#### [NEW] [ChatConversationScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/ChatConversationScreen.kt)
- Create a new composable `ChatConversationScreen` in the `com.sahilmaske.peerlearn.ui.home` package.
- This component will accept `chatId: String` and `onBack: () -> Unit` as parameters.
- It will serve as a placeholder for the chat conversation view.

#### [MODIFY] [ChatScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/ChatScreen.kt)
- Update `ChatScreen` to accept `navController: NavController` as a parameter.
- This resolves the mismatch with its usage in `NaviScreen.kt`.

### Navigation

#### [MODIFY] [MainActivity.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/MainActivity.kt)
- Correct the `see_all_peers` route to call `SeeAllPeersScreen(navController = navController)`.
- Move the `chat_conversation/{chatId}` route to the top level of the `NavHost` where it belongs.
- Ensure all imports are correctly handled.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that the project builds without unresolved references.

### Manual Verification
- Deploy the app to a device/emulator.
- Navigate to the "Chat" tab to verify `ChatScreen` loads (via `NaviScreen`).
- If navigation to `chat_conversation` is triggered, verify the placeholder screen appears.
