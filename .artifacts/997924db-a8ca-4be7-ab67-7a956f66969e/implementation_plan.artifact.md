# Implementation Plan - Fix Unresolved Reference 'iconBg' and Syntax Error in Account.kt

The build is failing because `iconBg` is used in `Account.kt` but not defined. Additionally, the `AccountScreen` composable function is missing a closing brace.

## Proposed Changes

### UI Components

#### [MODIFY] [Account.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/Settings/Account.kt)

- Define `iconBg` as `AppColors.PrimaryContainer` within `AccountScreen`.
- Add the missing closing brace for the `AccountScreen` function.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to ensure the project builds successfully.

### Manual Verification
- Render the `AccountScreenPreview` to verify the UI looks correct with the background color.
