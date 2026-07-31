# Implementation Plan - Add Composables to Notifications Screen

The user wants to add a composable to the `NotificationScreen`. Based on the current implementation, adding a Preview and an Empty State placeholder would be highly beneficial for development and user experience.

## Proposed Changes

### [Notifications Component]

#### [MODIFY] [Notifications.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/notify/Notifications.kt)
- Add an `EmptyNotificationPlaceholder` composable to handle cases with no notifications.
- Integrate the `EmptyNotificationPlaceholder` into `NotificationScreen`.
- Add a `@Preview` for `NotificationScreen` with mock data.
- Add a `@Preview` for `NotificationRow` to visualize different notification types.

## Verification Plan

### Manual Verification
- Render the `NotificationScreenPreview` using the `render_compose_preview` tool.
- Render the `NotificationRowPreview` to verify icon and button styles.
