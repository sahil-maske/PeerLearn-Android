# Walkthrough - Fixing ChatScreen Preview Render Issue

I have fixed the render issue in `ChatScreenPreview` where Firebase was not initialized.

## Changes

### [ChatScreen.kt](file:///D:/PeerLearn2/app/src/main/java/com/sahilmaske/peerlearn/ui/home/ChatScreen.kt)

- Refactored `ChatScreen` into a stateful version (taking `ChatViewModel`) and a stateless version `ChatScreenContent` (taking `List<Conversation>`).
- Updated `ChatScreenPreview` to use `ChatScreenContent` with dummy data instead of `ChatScreen`, which avoids the instantiation of `ChatViewModel` and its Firebase dependencies during preview rendering.

```diff
-@Composable
-fun ChatScreen(
-    navController: NavController,
-    viewModel: ChatViewModel = viewModel()
-) {
-    val conversations by viewModel.conversations.collectAsState()
-
-    Column(
-        modifier = Modifier
-            .fillMaxSize()
-            .background(AppColors.Background)
-            .statusBarsPadding()
-            .padding(12.dp),
-        verticalArrangement = Arrangement.spacedBy(12.dp),
-        horizontalAlignment = Alignment.Start
-    ) {
-        // ... UI code ...
-    }
-}

+@Composable
+fun ChatScreen(
+    navController: NavController,
+    viewModel: ChatViewModel = viewModel()
+) {
+    val conversations by viewModel.conversations.collectAsState()
+    ChatScreenContent(
+        navController = navController,
+        conversations = conversations
+    )
+}
+
+@Composable
+fun ChatScreenContent(
+    navController: NavController,
+    conversations: List<Conversation>
+) {
+    Column(
+        modifier = Modifier
+            .fillMaxSize()
+            .background(AppColors.Background)
+            .statusBarsPadding()
+            .padding(12.dp),
+        verticalArrangement = Arrangement.spacedBy(12.dp),
+        horizontalAlignment = Alignment.Start
+    ) {
+        // ... UI code ...
+    }
+}
```

## Verification Results

### Preview Rendering
The `ChatScreenPreview` now renders correctly in Android Studio without throwing `IllegalStateException: Default FirebaseApp is not initialized`.

![ChatScreenPreview](file:///D:/PeerLearn2/.artifacts/7c6136fa-dda5-4b78-bada-61fcbf4a5fc2/ChatScreenPreview.png)
