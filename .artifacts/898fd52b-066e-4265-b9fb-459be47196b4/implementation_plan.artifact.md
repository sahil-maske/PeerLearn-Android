# Implementation Plan - Fix Unresolved Reference 'iconBg' and Syntax Error in Account.kt

The build is failing because `iconBg` is used in `Account.kt` but not defined. Additionally, the `AccountScreen` function is missing a closing brace, and there is an unused/incorrect import.

## Proposed Changes

### [app]

#### [MODIFY] [Account.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/Settings/Account.kt)
- Define `iconBg` as `AppColors.PrimaryContainer` within `AccountScreen`.
- Add a missing closing brace `}` to properly close the `AccountScreen` function.
- Remove the unused import `android.R.attr.rowHeight`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that the compilation errors are resolved.
