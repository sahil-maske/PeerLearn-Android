# Implementation Plan - Fix Unresolved 'IconBg' in ChangePassScreen.kt

The build is failing because `IconBg` is used in `ChangePassScreen.kt` but not defined within the file. The compiler is reporting ambiguity because other files in the same package (`AccountScreen.kt` and `VerifyEmailScreen.kt`) define `private val IconBg`, causing a conflict when the name is unresolved locally.

## Proposed Changes

### [app] Component

#### [MODIFY] [ChangePassScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/Settings/ChangePassScreen.kt)
- Define `private val IconBg = Color(0xFFEEEDFE)` at the top level of the file to match the styling used in other settings screens.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that the file now compiles without the "None of the following candidates is applicable" error.

### Manual Verification
- None required as this is a build fix for a missing variable.
