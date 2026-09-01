package com.sahilmaske.peerlearn.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import androidx.compose.ui.platform.LocalInspectionMode
import com.sahilmaske.peerlearn.model.User
import com.sahilmaske.peerlearn.model.Post
import com.sahilmaske.peerlearn.ui.components.SlideToSwapButton
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.ConnectionActionMode
import com.sahilmaske.peerlearn.viewmodel.ConnectionActionsSheet
import com.sahilmaske.peerlearn.viewmodel.ConnectionViewModel
import com.sahilmaske.peerlearn.viewmodel.ProfileState
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.max
import kotlin.math.min

// Screen size buckets, based on Material3's standard width breakpoints.
// COMPACT = phones, MEDIUM = small tablets/foldables, EXPANDED = large tablets.
enum class ProfileScreenSize { COMPACT, MEDIUM, EXPANDED }

@Composable
private fun rememberProfileScreenSize(): ProfileScreenSize {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    return when {
        screenWidthDp < 600 -> ProfileScreenSize.COMPACT
        screenWidthDp < 840 -> ProfileScreenSize.MEDIUM
        else -> ProfileScreenSize.EXPANDED
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uid: String? = null,
    viewModel: ProfileViewModel = viewModel(),
    connectionViewModel: ConnectionViewModel = viewModel(),
    onNavigateToChat: (String) -> Unit = {},
    onBack: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    // ---------------- Profile data ----------------
    val profileFromFirestore by viewModel.userProfile.collectAsState()
    val profileLoadState by viewModel.uiState.collectAsState()

    val isPreview = LocalInspectionMode.current

    // In Android Studio's @Preview, skip Firestore entirely and show fake data instead.
    val userProfile = if (isPreview) {
        User(
            uid = "preview_uid",
            name = "Sahil Maske",
            college = "Modern College",
            role = "Android Developer",
            knownSkills = listOf("Kotlin", "Compose", "Firebase"),
            learningSkills = listOf("Rust", "Three.js"),
            about = "Passionate about building beautiful and performant Android apps.",
            location = "Pune, India",
            connection = 124,
            postCount = 12,
            helpCount = 45
        )
    } else {
        profileFromFirestore
    }

    // Load the profile (and start listening for its live post count) the first time
    // this screen appears, or whenever a different uid is passed in.
    LaunchedEffect(uid) {
        if (isPreview) return@LaunchedEffect
        val profileUidToLoad = uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        viewModel.fetchUserProfile(profileUidToLoad)
        viewModel.listenPostCount(profileUidToLoad) // real-time count from actual posts, not a stale counter field
    }

    val loggedInUid = remember {
        mutableStateOf(if (isPreview) "preview_uid" else FirebaseAuth.getInstance().currentUser?.uid)
    }
    // Keep loggedInUid in sync if the user signs in/out while this screen is open.
    DisposableEffect(Unit) {
        if (!isPreview) {
            val auth = FirebaseAuth.getInstance()
            val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                loggedInUid.value = firebaseAuth.currentUser?.uid
            }
            auth.addAuthStateListener(authListener)
            onDispose { auth.removeAuthStateListener(authListener) }
        } else {
            onDispose {}
        }
    }
    val isViewingOwnProfile = uid == null || uid == loggedInUid.value

    // ---------------- Connection state (only matters when viewing someone else's profile) ----------------
    val connectionStatus by connectionViewModel.connectionStatus.collectAsState()

    // NEW: id of the live connections/{id} doc + who sent it, so cancel/break/block
    // can act on it directly. NEW: block relationship in either direction.
    val activeConnectionId by connectionViewModel.activeConnectionId.collectAsState()
    val activeRequestedBy by connectionViewModel.activeRequestedBy.collectAsState()
    val blockStatus by connectionViewModel.blockStatus.collectAsState()

    // NEW: controls the cancel/break/block bottom sheet.
    var showConnectionActionsSheet by remember { mutableStateOf(false) }

    val canViewFullProfile = isViewingOwnProfile ||
            userProfile?.profileVisibility != "Connections Only" ||
            connectionStatus == "accepted"

    // Live post count from the real posts collection (see ProfileViewModel.listenPostCount).
    val postCount by viewModel.postCount.collectAsState()

    // Live connection count from the real connections collection.
    val connectionCount by connectionViewModel.connectionCount.collectAsState()

    // Shows "Request Sent" immediately after the swipe action succeeds, before the
    // Firestore listener below has a chance to update connectionStatus to "pending".
    var requestJustSent by remember { mutableStateOf(false) }

    LaunchedEffect(uid, loggedInUid.value) {
        if (isPreview) return@LaunchedEffect
        val profileUidToWatch = uid ?: loggedInUid.value ?: return@LaunchedEffect
        connectionViewModel.listenConnectionCount(profileUidToWatch) // live count for whichever profile is shown

        if (isViewingOwnProfile) return@LaunchedEffect
        val myUid = loggedInUid.value ?: return@LaunchedEffect
        connectionViewModel.listenConnectionStatus(myUid, profileUidToWatch)
        connectionViewModel.listenBlockStatus(myUid, profileUidToWatch) // NEW: watch both block directions too
    }

    // Once the real Firestore listener confirms "pending" (or beyond), drop the
    // temporary flag so the permanent "Request Pending" / "Message" state takes over.
    LaunchedEffect(connectionStatus) {
        if (connectionStatus == "pending" || connectionStatus == "accepted") {
            requestJustSent = false
        }
    }

    // ---------------- Responsive sizing (COMPACT / MEDIUM / EXPANDED) ----------------
    val screenSize = rememberProfileScreenSize()
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    // Avatar scales within each breakpoint's own range instead of one global formula,
    // so tablets don't just get a slightly-bigger phone avatar — they get a size
    // appropriate to their own category.
    val avatarSize = remember(screenWidthDp, screenSize) {
        when (screenSize) {
            ProfileScreenSize.COMPACT -> max(96.dp.value, min(130.dp.value, screenWidthDp.value * 0.30f)).dp
            ProfileScreenSize.MEDIUM -> 150.dp
            ProfileScreenSize.EXPANDED -> 170.dp
        }
    }
    // Horizontal padding scales per breakpoint.
    val horizontalPadding = remember(screenWidthDp, screenSize) {
        when (screenSize) {
            ProfileScreenSize.COMPACT -> max(12.dp.value, min(20.dp.value, screenWidthDp.value * 0.045f)).dp
            ProfileScreenSize.MEDIUM -> 28.dp
            ProfileScreenSize.EXPANDED -> 36.dp
        }
    }
    // Content max-width: on tablets we don't want cards stretching edge-to-edge,
    // so we cap the width and center the whole column.
    val contentMaxWidth: Dp = when (screenSize) {
        ProfileScreenSize.COMPACT -> Dp.Unspecified
        ProfileScreenSize.MEDIUM -> 620.dp
        ProfileScreenSize.EXPANDED -> 760.dp
    }
    // Posts grid column count per breakpoint.
    val postGridColumns = when (screenSize) {
        ProfileScreenSize.COMPACT -> 2
        ProfileScreenSize.MEDIUM -> 3
        ProfileScreenSize.EXPANDED -> 4
    }
    // Name/college font sizes scale up slightly on larger screens for visual balance.
    val nameFontSize = when (screenSize) {
        ProfileScreenSize.COMPACT -> 24.sp
        ProfileScreenSize.MEDIUM -> 26.sp
        ProfileScreenSize.EXPANDED -> 28.sp
    }
    val collegeFontSize = when (screenSize) {
        ProfileScreenSize.COMPACT -> 17.sp
        ProfileScreenSize.MEDIUM -> 18.sp
        ProfileScreenSize.EXPANDED -> 19.sp
    }

    // ---------------- Dialog / bottom-sheet state ----------------
    var selectedSkillType by remember { mutableStateOf<String?>(null) }   // "learning" or "known", or null when no dialog is open
    var selectedPost by remember { mutableStateOf<Post?>(null) }         // post currently open in the detail sheet
    var showImagePickerDialog by remember { mutableStateOf(false) }
    val postDetailSheetState = rememberModalBottomSheetState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Pick an existing photo from the gallery, upload it, then save the resulting URL.
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { pickedImageUri ->
        pickedImageUri?.let { imageUri ->
            coroutineScope.launch {
                val uploadedImageUrl = uploadToCloudinary(context, imageUri)
                if (uploadedImageUrl.isNotBlank()) {
                    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUid)
                        .set(mapOf("avatarUrl" to uploadedImageUrl), SetOptions.merge())
                        .addOnSuccessListener {
                            viewModel.fetchUserProfile(currentUid)
                        }
                }
            }
        }
    }

    // Holds the temp file URI while the camera app is capturing a new photo.
    var pendingCameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Take a new photo with the camera, upload it, then save the resulting URL.
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { photoWasSaved ->
        if (photoWasSaved) {
            pendingCameraImageUri?.let { imageUri ->
                coroutineScope.launch {
                    val uploadedImageUrl = uploadToCloudinary(context, imageUri)
                    if (uploadedImageUrl.isNotBlank()) {
                        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(currentUid)
                            .set(mapOf("avatarUrl" to uploadedImageUrl), SetOptions.merge())
                            .addOnSuccessListener {
                                viewModel.fetchUserProfile(currentUid)
                            }
                    }
                }
            }
        }
    }

    // Placeholder posts — swap for real Firestore data later.
    val placeholderPosts = listOf(
        Post(id = "1", heading = "Modern Web Development Trends 2024", description = "In this post I walk you through modern web development.", likeCount = 124, commentCount = 18, imageUrl = "purple"),
        Post(id = "2", heading = "UI/UX Design Principles for Mobile", description = "Learn core principles of mobile UI/UX design.", likeCount = 85, commentCount = 12, imageUrl = "red"),
        Post(id = "3", heading = "Flutter vs Kotlin", description = "A detailed comparison between Flutter and Kotlin.", likeCount = 60, commentCount = 9, imageUrl = "green"),
        Post(id = "4", heading = "Clean Architecture in Android", description = "How to structure your Android app using Clean Architecture.", likeCount = 45, commentCount = 7, imageUrl = "orange"),
    )

    // ---------------- Skills dialog (shown when a "Known"/"Learning" chip area is tapped) ----------------
    if (selectedSkillType != null) {
        AlertDialog(
            onDismissRequest = { selectedSkillType = null },
            title = {
                Text(
                    text = if (selectedSkillType == "learning") "Learning Skills" else "Known Skills",
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary
                )
            },
            text = {
                Column {
                    val skillsToShow = if (selectedSkillType == "learning")
                        userProfile?.learningSkills else userProfile?.knownSkills
                    if (skillsToShow.isNullOrEmpty()) {
                        Text("No skills added yet", color = AppColors.TextSecondary)
                    } else {
                        skillsToShow.forEach { skill ->
                            Text("• $skill", fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp), color = AppColors.TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSkillType = null }) {
                    Text("Close", color = AppColors.Primary)
                }
            }
        )
    }

    // ---------------- Camera vs Gallery picker dialog ----------------
    ImagePickerDialog(
        showDialog = showImagePickerDialog,
        onDismissRequest = { showImagePickerDialog = false },
        onCameraClick = {
            showImagePickerDialog = false
            val tempPhotoUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                java.io.File.createTempFile("avatar_", ".jpg", context.cacheDir)
            )
            pendingCameraImageUri = tempPhotoUri
            cameraLauncher.launch(tempPhotoUri)
        },
        onGalleryClick = {
            showImagePickerDialog = false
            galleryLauncher.launch("image/*")
        }
    )

    // ---------------- Post detail bottom sheet ----------------
    if (selectedPost != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedPost = null },
            sheetState = postDetailSheetState,
            containerColor = AppColors.Surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Text(selectedPost!!.heading, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text(
                    selectedPost!!.description.ifEmpty { "No description added." },
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("❤️ ${selectedPost!!.likeCount}", color = AppColors.TextPrimary)
                    Text("💬 ${selectedPost!!.commentCount}", color = AppColors.TextPrimary)
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = AppColors.Divider)
                Spacer(Modifier.height(8.dp))

                listOf("Save post", "Hide post", "Report post").forEach { actionLabel ->
                    TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                        Text(actionLabel, color = AppColors.TextPrimary, fontSize = 16.sp)
                    }
                }
                TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text("Delete post", color = AppColors.Error, fontSize = 16.sp)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // ---------------- Cancel / Break / Block bottom sheet ----------------
    // NEW: single sheet reused for all three destructive connection actions,
    // plus unblock. Which options it shows is driven by `mode`, computed from
    // the same connectionStatus/blockStatus/activeRequestedBy already on screen.
    ConnectionActionsSheet(
        show = showConnectionActionsSheet,
        mode = when {
            blockStatus == "blockedByMe" -> ConnectionActionMode.UNBLOCK
            connectionStatus == "accepted" -> ConnectionActionMode.ACCEPTED
            connectionStatus == "pending" && activeRequestedBy == loggedInUid.value -> ConnectionActionMode.PENDING_SENT
            else -> ConnectionActionMode.NONE
        },
        onDismiss = { showConnectionActionsSheet = false },
        onBreakConnection = {
            val connectionId = activeConnectionId ?: return@ConnectionActionsSheet
            connectionViewModel.breakConnection(connectionId)
            showConnectionActionsSheet = false
        },
        onCancelRequest = {
            val connectionId = activeConnectionId ?: return@ConnectionActionsSheet
            connectionViewModel.cancelConnectionRequest(connectionId)
            showConnectionActionsSheet = false
        },
        onBlockUser = {
            val myUid = loggedInUid.value ?: return@ConnectionActionsSheet
            val otherUid = uid ?: return@ConnectionActionsSheet
            connectionViewModel.blockUser(myUid, otherUid)
            showConnectionActionsSheet = false
        },
        onUnblockUser = {
            val myUid = loggedInUid.value ?: return@ConnectionActionsSheet
            val otherUid = uid ?: return@ConnectionActionsSheet
            connectionViewModel.unblockUser(myUid, otherUid)
            showConnectionActionsSheet = false
        }
    )

    // ---------------- Main screen content ----------------
    // Root Box centers the content column on wide screens; on phones it just fills the width.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .then(
                    if (contentMaxWidth != Dp.Unspecified) Modifier.widthIn(max = contentMaxWidth)
                    else Modifier.fillMaxWidth()
                )
                .fillMaxWidth()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Loading state — bail out before drawing the rest of the profile.
            if (profileLoadState is ProfileState.Loading) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.Primary)
                    }
                }
                return@LazyColumn
            }

            // ---- Top bar: back button, title, settings ----
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 12.dp)
                        .background(AppColors.Surface.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            tint = AppColors.Icon
                        )
                    }
                    Text("Profile", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)

                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = AppColors.Icon
                        )
                    }
                }
            }

            // ---- Avatar with edit badge ----
            item {
                Spacer(Modifier.height(10.dp))
                Box(contentAlignment = Alignment.BottomEnd) {

                    if (userProfile?.avatarUrl.isNullOrEmpty()) {
                        // No avatar uploaded yet — fall back to initials on a colored circle.
                        Box(
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .background(AppColors.SecondaryContainer)
                                .border(2.dp, AppColors.Primary.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val nameInitials = userProfile?.name
                                ?.split(" ")
                                ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                ?.take(2)
                                ?.joinToString("") ?: "?"
                            Text(nameInitials, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        }
                    } else {
                        AsyncImage(
                            model = userProfile?.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .border(2.dp, AppColors.Primary.copy(alpha = 0.3f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Edit badge — white ring + single dark green background, no double bg.
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .offset(x = 4.dp, y = 4.dp)
                            .clip(CircleShape)
                            .background(AppColors.TextWhite)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F6E6E))
                            .clickable { showImagePickerDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = AppColors.TextWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ---- Name, college, location, bio ----
            item {
                Text(
                    text = userProfile?.name ?: "Your Name",
                    fontSize = nameFontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = userProfile?.college ?: "College Name",
                    fontSize = collegeFontSize,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                )
                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = userProfile?.location?.takeIf { it.isNotBlank() } ?: "Location not added",
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = userProfile?.about?.takeIf { it.isNotBlank() }
                        ?: "Computer Science major passionate about bridging the gap between design and code.",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding * 1.5f) // slightly wider inset for readable line length
                )

                Spacer(Modifier.height(8.dp))
            }

            // ---- Stats card: Posts, Helps, Connections ----
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(postCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                            Text("Post", fontSize = 12.sp, color = AppColors.TextSecondary)
                        }
                        VerticalDivider(modifier = Modifier.height(32.dp), color = AppColors.Divider)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text((userProfile?.helpCount ?: 0).toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                            Text("Helps", fontSize = 12.sp, color = AppColors.TextSecondary)
                        }
                        VerticalDivider(modifier = Modifier.height(32.dp), color = AppColors.Divider)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(connectionCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                            Text("Connections", fontSize = 12.sp, color = AppColors.TextSecondary)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ---- Primary action: Edit Profile (own profile) OR Connect/Pending/Message (other user) ----
            item {
                if (isViewingOwnProfile) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AppColors.Primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Primary)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = AppColors.Primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Profile", fontWeight = FontWeight.Medium, color = AppColors.Primary)
                    }
                } else if (blockStatus == "blockedByThem") {
                    // NEW: they blocked me — no action is possible from here, so just
                    // show a neutral, disabled state instead of a Connect/Message button.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColors.SecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Unavailable", color = AppColors.TextSecondary, fontWeight = FontWeight.Medium)
                    }
                } else {
                    when {
                        // NEW: I've blocked them — tapping opens the sheet to unblock.
                        blockStatus == "blockedByMe" -> {
                            OutlinedButton(
                                onClick = { showConnectionActionsSheet = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, AppColors.Error),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error)
                            ) {
                                Text("Blocked \u2014 Tap to Unblock", fontWeight = FontWeight.Medium)
                            }
                        }
                        connectionStatus == "accepted" -> {
                            // NEW: Message button now shares the row with a "\u22ee" menu button
                            // that opens Break Connection / Block User.
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val myUid = loggedInUid.value ?: return@Button
                                        val otherUid = uid ?: return@Button
                                        // Sorting both UIDs keeps the chat id identical no matter who opens the chat first.
                                        val chatId = listOf(myUid, otherUid).sorted().joinToString("_")
                                        onNavigateToChat(chatId)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                                ) {
                                    Text("Message", fontWeight = FontWeight.Medium, color = AppColors.TextWhite)
                                }
                                OutlinedButton(
                                    onClick = { showConnectionActionsSheet = true },
                                    modifier = Modifier
                                        .height(48.dp)
                                        .width(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, AppColors.Divider),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Icon)
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Connection options")
                                }
                            }
                        }
                        // NEW: I'm the one who sent this pending request — tapping opens
                        // the sheet to cancel it (or block instead).
                        connectionStatus == "pending" && activeRequestedBy == loggedInUid.value -> {
                            OutlinedButton(
                                onClick = { showConnectionActionsSheet = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, AppColors.Divider),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextPrimary)
                            ) {
                                Text("Request Pending \u00b7 Tap to Cancel", fontWeight = FontWeight.Medium)
                            }
                        }
                        // They sent the request to me — nothing to cancel from this screen
                        // (accept/reject already lives on NotificationScreen).
                        connectionStatus == "pending" -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Request Pending", fontWeight = FontWeight.Medium)
                            }
                        }
                        // Instant confirmation shown right after swipe, while the listener
                        // hasn't caught up to "pending" yet.
                        requestJustSent -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0F6E6E),
                                    disabledContainerColor = Color(0xFF0F6E6E)
                                )
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AppColors.TextWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Request Sent", fontWeight = FontWeight.Medium, color = AppColors.TextWhite)
                            }
                        }
                        else -> {
                            // null or "rejected" -> allow sending a fresh connection request.
                            SlideToSwapButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding),
                                onConfirmed = {
                                    val myUid = loggedInUid.value ?: return@SlideToSwapButton
                                    val otherUid = uid ?: return@SlideToSwapButton
                                    connectionViewModel.sendConnectionRequest(
                                        currentUserId = myUid,
                                        targetUserId = otherUid,
                                        onSuccess = {
                                            // Only show "Request Sent" once Firestore actually confirms the write.
                                            requestJustSent = true
                                        },
                                        onFailure = { error ->
                                            android.widget.Toast.makeText(
                                                context,
                                                "Couldn't send request: ${error.localizedMessage ?: "check your connection"}",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ---- "Can Teach" section ----
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF0F6E6E),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Can Teach",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                        }
                        Spacer(Modifier.height(10.dp))

                        val knownSkills = userProfile?.knownSkills
                        if (knownSkills.isNullOrEmpty()) {
                            Text("No skills added yet", fontSize = 13.sp, color = AppColors.TextSecondary)
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                knownSkills.forEach { skill ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(AppColors.PrimaryContainer)
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(skill, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F6E6E))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ---- "Wants to Learn" section ----
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFFFDDB5),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Wants to Learn", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        }
                        Spacer(Modifier.height(10.dp))

                        val learningSkills = userProfile?.learningSkills
                        if (learningSkills.isNullOrEmpty()) {
                            Text("No skills added yet", fontSize = 13.sp, color = AppColors.TextSecondary)
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                learningSkills.forEach { skill ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFF5D9A8))
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(skill, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF8A5A00))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // ---- Recent Recognition ----
            item {
                if(canViewFullProfile) {
                    // Top Mentor badge card.
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                tint = Color(0xFF085041),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Linked Accounts",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )

                            }
                        }
                        val instagramLink = userProfile?.linkedAccounts?.get("instagram")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (instagramLink != null){
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(instagramLink)
                                            )
                                        )
                                    }
                                    else {
                                        Toast.makeText(context, "Instagram is not linked", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Instagram",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = "Instagram",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextPrimary,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = AppColors.Divider,
                            thickness = 0.5.dp
                        )
                        val linkedLink = userProfile?.linkedAccounts?.get("linkedIn")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if(linkedLink !=null){
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(linkedLink)
                                            )
                                        )
                                    }
                                    else {
                                        Toast.makeText(context, "LinkedIn is Not Linked", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Work,
                                    contentDescription = "LinkedIn",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = "LinkedIn",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextPrimary,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = AppColors.Divider,
                            thickness = 0.5.dp
                        )
                        val githubLink = userProfile?.linkedAccounts?.get("github")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (githubLink !=null){
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(githubLink)
                                            )
                                        )
                                    }else
                                    {
                                        Toast.makeText(context, "Github is Not linked", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = "Github",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = "GitHub",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextPrimary,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = AppColors.Divider,
                            thickness = 0.5.dp
                        )
                        val twitterLink = userProfile?.linkedAccounts?.get("twitter")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (twitterLink !=null){
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(twitterLink)
                                            )
                                        )
                                    }
                                    else {
                                        Toast.makeText(context, "Twitter Is Not Linked", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AlternateEmail,
                                    contentDescription = "X",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Twitter/X",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextPrimary,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    //  HARE IS THE END OF LINK ACCOUNTS
                }
            }

            // ---- Posts header ----
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Posts", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = AppColors.TextPrimary)
                    Text("See all", color = AppColors.Primary, fontSize = 14.sp)
                }
                Spacer(Modifier.height(8.dp))
            }

            // ---- Posts grid (2 cols COMPACT, 3 cols MEDIUM, 4 cols EXPANDED) ----
            items(placeholderPosts.chunked(postGridColumns)) { rowOfPosts ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowOfPosts.forEach { post ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(onLongPress = { selectedPost = post })
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            when (post.imageUrl) {
                                                "purple" -> listOf(AppColors.Primary, AppColors.PrimaryContainer)
                                                "red" -> listOf(AppColors.Tertiary, AppColors.TertiaryContainer)
                                                "green" -> listOf(AppColors.Secondary, AppColors.SecondaryContainer)
                                                "orange" -> listOf(AppColors.TertiaryContainer, AppColors.SecondaryContainer)
                                                else -> listOf(AppColors.Primary, AppColors.PrimaryContainer)
                                            }
                                        )
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = post.heading,
                                    color = AppColors.TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.align(Alignment.BottomStart)
                                )
                            }
                        }
                    }
                    // Fill remaining slots in the last row so cards don't stretch to fill the gap.
                    if (rowOfPosts.size < postGridColumns) {
                        repeat(postGridColumns - rowOfPosts.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

    }
}

// Dialog that lets the user choose between taking a new photo or picking one from the gallery.
@Composable
fun ImagePickerDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            containerColor = AppColors.Surface,
            title = { Text("Choose Profile Picture", fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary) },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Take Photo", color = AppColors.TextPrimary) },
                        leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = AppColors.Icon) },
                        modifier = Modifier.clickable { onCameraClick() },
                        colors = ListItemDefaults.colors(containerColor = AppColors.Surface)
                    )
                    ListItem(
                        headlineContent = { Text("Choose from Gallery", color = AppColors.TextPrimary) },
                        leadingContent = { Icon(Icons.Default.Image, contentDescription = null, tint = AppColors.Icon) },
                        modifier = Modifier.clickable { onGalleryClick() },
                        colors = ListItemDefaults.colors(containerColor = AppColors.Surface)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel", color = AppColors.Primary)
                }
            }
        )
    }
}

// Crops the picked image to a square, resizes it, and uploads it to Cloudinary.
// Returns the resulting (face-cropped) URL, or an empty string if anything failed.
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun uploadToCloudinary(context: android.content.Context, imageUri: Uri): String {
    val cloudinaryCloudName = "db7wneko6"
    val cloudinaryUploadPreset = "peerlearn_avatar"

    val inputStream = context.contentResolver.openInputStream(imageUri) ?: return ""
    val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
    inputStream.close()

    // Crop to a centered square before resizing, so the avatar isn't stretched.
    val squareSide = minOf(originalBitmap.width, originalBitmap.height)
    val cropStartX = (originalBitmap.width - squareSide) / 2
    val cropStartY = (originalBitmap.height - squareSide) / 2
    val squareBitmap = android.graphics.Bitmap.createBitmap(originalBitmap, cropStartX, cropStartY, squareSide, squareSide)

    val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(squareBitmap, 400, 400, true)
    val jpegOutputStream = java.io.ByteArrayOutputStream()
    resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, jpegOutputStream)
    val jpegBytes = jpegOutputStream.toByteArray()

    return withContext(Dispatchers.IO) {
        try {
            val uploadUrl = java.net.URL("https://api.cloudinary.com/v1_1/$cloudinaryCloudName/image/upload")
            val multipartBoundary = "Boundary-${System.currentTimeMillis()}"
            val connection = uploadUrl.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$multipartBoundary")

            val requestBody = connection.outputStream
            requestBody.write("--$multipartBoundary\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\n$cloudinaryUploadPreset\r\n".toByteArray())
            requestBody.write("--$multipartBoundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"avatar.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n".toByteArray())
            requestBody.write(jpegBytes)
            requestBody.write("\r\n--$multipartBoundary--\r\n".toByteArray())
            requestBody.flush()

            val responseBody = connection.inputStream.bufferedReader().readText()
            // Insert Cloudinary's face-aware square-crop transform into the returned URL.
            org.json.JSONObject(responseBody).getString("secure_url")
                .replace("/upload/", "/upload/w_400,h_400,c_thumb,g_face/")
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ProfileScreenPreviewPhone() {
    ProfileScreen()
}

@Preview(showBackground = true, widthDp = 700)
@Composable
fun ProfileScreenPreviewMedium() {
    ProfileScreen()
}

@Preview(showBackground = true, widthDp = 1000)
@Composable
fun ProfileScreenPreviewExpanded() {
    ProfileScreen()
}