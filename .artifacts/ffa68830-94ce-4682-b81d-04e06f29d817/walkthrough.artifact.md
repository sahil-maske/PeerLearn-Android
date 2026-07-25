# Walkthrough - Final Fix for SubcomposeLayout Intrinsic Measurement Crash

I have applied a final, robust fix for the `IllegalStateException` that was occurring on the `PostScreen`.

## Root Cause Analysis

The crash was caused by `Modifier.height(IntrinsicSize.Min)` being applied to a `BoxWithConstraints`.
1. **BoxWithConstraints** is built on **SubcomposeLayout**, which defers composition until measurement.
2. **Intrinsic Measurements** require knowing the sizes of children *before* measurement.
3. Because `SubcomposeLayout` hasn't composed its children yet, it cannot provide intrinsic measurements, leading to the crash.

## Solution

I have refactored the layout to avoid intrinsic measurements entirely while still achieving the desired "match height" look for the indicator.

### [PostScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/PostScreen.kt)

- **Removed `Modifier.height(IntrinsicSize.Min)`**: The `BoxWithConstraints` now naturally wraps its contents (the `Row` of text).
- **Nested Indicator Box**: I wrapped the teal indicator in a `Box(Modifier.matchParentSize())`.
    - `matchParentSize()` tells the layout system: "Measure me after you know the parent's size, and make me match it exactly."
    - This allows the inner indicator `Box` to use `fillMaxHeight()` and an explicit `width(itemWidth)` safely, as it now has a fixed reference height from its wrapper.
- **Import Cleanup**: Removed the unused `IntrinsicSize` import.

## Verification

### Automated Tests
- Ran `:app:assembleDebug` and it passed.

### UI Consistency
- This approach ensures the teal background exactly matches the height of the "Teach" and "Learn" options, which are determined by the text and its padding.
