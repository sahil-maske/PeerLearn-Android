# Implementation Plan - Final Fix for SubcomposeLayout Intrinsic Measurement Crash

## Goal
Fix the `java.lang.IllegalStateException` occurring in `PostScreen.kt` by removing the unsupported `IntrinsicSize.Min` from `BoxWithConstraints` and implementing a robust sizing strategy for the toggle indicator.

## Problem
The `BoxWithConstraints` component is configured with `Modifier.height(IntrinsicSize.Min)`. As an implementation of `SubcomposeLayout`, it does not support intrinsic measurements. This results in a crash because the layout system cannot calculate the intrinsic height of a component that defers its composition until measurement.

## Proposed Changes

### [MODIFY] [PostScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/PostScreen.kt)

1. **Remove `Modifier.height(IntrinsicSize.Min)`** from `BoxWithConstraints`.
2. **Refactor the sliding indicator** to use `Modifier.matchParentSize()` on a wrapper `Box`. This allows the indicator to match the height of the toggle (defined by the `Row` text) without forcing the `BoxWithConstraints` into an unsupported intrinsic measurement pass.
3. **Ensure the indicator width** is correctly maintained at `itemWidth` (half of `maxWidth`).

```kotlin
// Proposed indicator implementation:
Box(modifier = Modifier.matchParentSize()) {
    Box(
        modifier = Modifier
            .offset(x = indicatorOffset)
            .width(itemWidth)
            .fillMaxHeight() // Now works because parent is matchParentSize
            .clip(RoundedCornerShape(50))
            .background(AppColors.DarkGreen)
    )
}
```

## Verification Plan

### Automated Tests
- Run `gradle_build :app:assembleDebug` to verify compilation.

### Manual Verification
- Deploy the app.
- Open the "Create Post" screen.
- Verify the "Intent Toggle" (Teach/Learn) works correctly without crashing.
- Ensure the teal indicator correctly fills the height of the toggle and slides smoothly.
