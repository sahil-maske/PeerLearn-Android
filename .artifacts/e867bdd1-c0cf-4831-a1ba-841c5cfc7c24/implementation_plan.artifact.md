# Fix Compilation Error in VerifyEmailScreen.kt

The project fails to compile because the `androidx.compose.material3.Button` composable is being called with a non-existent parameter `text`. Additionally, the button's content lambda contains redundant and incorrectly sized UI logic.

## Proposed Changes

### UI Components

#### [MODIFY] [VerifyEmailScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/Settings/VerifyEmailScreen.kt)

- Remove the `text` parameter from the `Button` call.
- Move the `Text` composable into the `Button`'s content lambda.
- Replace the redundant/oversized icon animation logic inside the button with a simple state-aware text or progress indicator.
- Fix the button content to appropriately handle the `isSending` state.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify the fix.

### Manual Verification
- Deploy the app and navigate to the Verify Email screen to ensure the button looks and behaves correctly.
