# Implementation Plan - Fix Unresolved Reference 'iconBg' and Syntax Error in Account.kt

The build is failing due to an unresolved reference `iconBg` and a structural syntax error in `Account.kt` where the `AccountScreen` function is closed prematurely.

## Proposed Changes

### [app]

#### [MODIFY] [Account.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/Settings/Account.kt)
- Remove the misplaced closing brace `}` at the beginning of the `AccountScreen` function.
- Define `val iconBg = AppColors.PrimaryContainer` inside `AccountScreen`.
- Ensure all Composable blocks are correctly nested and closed.
- Add the missing closing brace at the end of the `AccountScreen` function.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify the fix.

### Manual Verification
- None required as this is a build fix.
