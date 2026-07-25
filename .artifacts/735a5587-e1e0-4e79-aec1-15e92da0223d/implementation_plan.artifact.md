# Implementation Plan - Fix Intrinsic Measurements Error in PostScreen

The application is crashing with a `java.lang.IllegalStateException` because `BoxWithConstraints` (which uses `SubcomposeLayout`) is being asked for intrinsic measurements via `Modifier.height(IntrinsicSize.Min)`. This is not supported in Jetpack Compose.

## User Review Required

> [!IMPORTANT]
> The proposed fix removes the unsupported `IntrinsicSize.Min` and uses `Modifier.matchParentSize()` for the animated indicator. This is the standard way to make a child match the parent's size in a `Box` without affecting the parent's size calculation.

## Proposed Changes

### UI Components

#### [MODIFY] [PostScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/PostScreen.kt)
- Remove `Modifier.height(IntrinsicSize.Min)` from the `BoxWithConstraints` component.
- Change `Modifier.fillMaxHeight()` to `Modifier.matchParentSize()` for the sliding indicator `Box` inside `BoxWithConstraints`.

## Verification Plan

### Automated Tests
- Build the project to ensure no syntax errors are introduced.
- Run `gradle_build("app:assembleDebug")` to verify compilation.

### Manual Verification
- Launch the application and navigate to the Post screen.
- Verify that the "Intent" toggle (I want to teach / I want to learn) displays correctly and the animation still works.
- Confirm that the crash no longer occurs.
