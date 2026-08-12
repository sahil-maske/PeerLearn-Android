# Implementation Plan - Fix ChangePasswordScreen Preview Render Issue

The `ChangePasswordScreen` preview fails because it attempts to instantiate `AccountViewModel`, which depends on `FirebaseAuth.getInstance()`. Firebase is not initialized in the Preview environment, leading to an `IllegalStateException`.

## User Review Required

> [!IMPORTANT]
> The fix involves refactoring `ChangePasswordScreen` into a stateful/stateless pair. The `ChangePasswordScreen` will remain the entry point for the app, but its internal UI logic will be moved to `ChangePasswordContent` to allow for successful rendering in Previews.

## Proposed Changes

### UI Components

#### [MODIFY] [ChangePassScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/Settings/ChangePassScreen.kt)

- Extract the UI logic into `ChangePasswordContent`.
- Update `ChangePasswordScreen` to handle ViewModel interaction.
- Update `ChangePassScreenPreview` to use `ChangePasswordContent` with mock data.

## Verification Plan

### Automated Tests
- I will run `render_compose_preview` for `ChangePassScreenPreview` to ensure it renders without errors.

### Manual Verification
- Verify that the screen still functions correctly in the app by checking its usage in `MainActivity.kt`.
