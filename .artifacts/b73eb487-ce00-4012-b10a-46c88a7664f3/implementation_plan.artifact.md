# Restore specific navigation logic in ChatConversationScreen

The user wants to restore a specific navigation call `navController.navigate("chat") { popUpTo(0) }` in the back button of `ChatConversationScreen`. To do this without causing "Unresolved reference" errors, we need to pass the `NavController` to the relevant composables and ensure the correct imports are present.

## User Review Required

> [!WARNING]
> The route `"chat"` is not currently defined in the main `NavHost` in `MainActivity.kt`. Navigating to it will cause a crash at runtime unless the route is added or corrected. I will implement the code as requested, but you may need to ensure the `"chat"` route exists.

> [!IMPORTANT]
> `popUpTo(0)` will clear the entire back stack. This is usually intended for navigating back to a root destination, but ensure this is the desired behavior for your app's navigation flow.

## Proposed Changes

### [peerlearn](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn)

#### [MODIFY] [ChatConversationScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/ChatConversationScreen.kt)
- Add `import androidx.navigation.NavController`
- Update `ChatConversationScreen` signature to accept `navController: NavController`.
- Update `ChatConversationContent` signature to accept `navController: NavController`.
- Update `ChatTopBar` signature to accept `navController: NavController`.
- Pass `navController` through these components.
- Restore the `IconButton` `onClick` logic to `{ navController.navigate("chat") { popUpTo(0) } }`.
- Update the Previews to pass a mock/remembered `NavController`.

#### [MODIFY] [MainActivity.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/MainActivity.kt)
- Update the call to `ChatConversationScreen` to pass the `navController`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to ensure the project builds without "Unresolved reference" errors.

### Manual Verification
- Deploy the app and test the back button in the Chat Conversation screen.
- Verify that it navigates to the expected destination (or check Logcat for navigation errors if the "chat" route is missing).
