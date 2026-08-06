# Implementation Plan: Fix Unresolved reference 'HelpDetailScreen'

The goal is to resolve the compilation error `Unresolved reference 'HelpDetailScreen'` in `MainActivity.kt` by creating the missing `HelpDetailScreen` component and adding the necessary logic to `FeedViewModel`.

## Proposed Changes

### `app` module

#### [MODIFY] [FeedViewModel.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/viewmodel/FeedViewModel.kt)
- Add a method `toggleHelpful(postId: String, commentId: String, currentIsHelpful: Boolean)` to allow post owners to mark a help offer as helpful.
- Add a method `getPostById(postId: String): Post?` to retrieve a post from the current list.

#### [NEW] [HelpDetailScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/HelpDetailScreen.kt)
- Create the `HelpDetailScreen` composable.
- It will display the details of a "Need Help" post.
- It will show a list of comments, which are considered "Help Offers".
- It will provide a text field to add new help offers.
- If the current user is the author of the post, they will see a button to mark an offer as "Helped".

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify the fix.

### Manual Verification
- Navigate to a "Need Help" post from the home screen.
- Verify the post details are displayed correctly.
- Add a help offer (comment) and verify it appears.
- As the post owner, mark an offer as helpful and verify the checkmark/status updates.
