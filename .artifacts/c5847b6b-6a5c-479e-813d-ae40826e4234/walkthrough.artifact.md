# Walkthrough - Fix Unresolved reference 'UserProfile'

I have fixed the compilation error in `HomeScreen.kt` where the class `UserProfile` was being referenced but not found. The correct class in the project is `User`.

## Changes

### [app]

#### [HomeScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/HomeScreen.kt)

- Updated the import from `com.sahilmaske.peerlearn.model.UserProfile` to `com.sahilmaske.peerlearn.model.User`.
- Updated the `userProfile` parameter type in `HomeScreenContent` to `User?`.

```diff
-import com.sahilmaske.peerlearn.model.UserProfile
+import com.sahilmaske.peerlearn.model.User
...
 @Composable
 fun HomeScreenContent(
-    userProfile: UserProfile?,                    // FIX: new param replaces undefined avatarUrl
+    userProfile: User?,                    // FIX: new param replaces undefined avatarUrl
     suggestions: List<PeerSuggestion>,
```

## Verification Results

### Automated Tests
- Executed `./gradlew :app:compileDebugKotlin` which finished successfully.
