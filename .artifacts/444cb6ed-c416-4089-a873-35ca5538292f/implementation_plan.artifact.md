# Fix Back Arrow Position and Chat Header Layout

The user reported that the back arrow in `ChatConversationScreen` is not in its correct place. This is due to the `Row` container having `Modifier.fillMaxSize()` and `verticalAlignment = Alignment.CenterVertically`, which causes the entire header to be centered in the middle of the screen.

## User Review Required

> [!IMPORTANT]
> I will be restructuring the `ChatConversationScreen` to use a `Scaffold` layout. This will move the header to the top and provide a dedicated area for chat messages.

## Proposed Changes

### UI Components

#### [MODIFY] [ChatConversationScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/ChatConversationScreen.kt)
- Wrap the screen in a `Scaffold`.
- Create a custom `TopAppBar` using the existing `Row` logic but constrained to the top.
- Fix the `Modifier.fillMaxSize()` issue.
- Add a placeholder for messages to make it a functional screen.
- Ensure the back arrow is correctly tinted and positioned.

## Verification Plan

### Automated Tests
- Run `gradlew :app:compileDebugKotlin` to ensure no regressions.

### Manual Verification
- Render the `ChatConversationScreenPreview` to verify the header is at the top and the back arrow is correctly placed.
