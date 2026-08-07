package com.sahilmaske.peerlearn.ui.home

import android.net.Uri
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VerifiedUser
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
import com.sahilmaske.peerlearn.viewmodel.ConnectionViewModel
import com.sahilmaske.peerlearn.viewmodel.ProfileState
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uid: String? = null,
    viewModel: ProfileViewModel = viewModel(),
    connectionViewModel: ConnectionViewModel = viewModel(),
    onNavigateToChat: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    // ---- State & Data ----
    val userProfileFromVM by viewModel.userProfile.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val isPreview = LocalInspectionMode.current

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
        userProfileFromVM
    }

    // Fetch current user profile on first launch
    LaunchedEffect(uid) {
        if (isPreview) return@LaunchedEffect
        val targetUid = uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        viewModel.fetchUserProfile(targetUid)
        viewModel.listenPostCount(targetUid) // NEW: real-time count from actual posts, not a stale counter field
    }


    val currentUserUid = remember {
        mutableStateOf(if (isPreview) "preview_uid" else FirebaseAuth.getInstance().currentUser?.uid)
    }
    // Observe auth state changes
    DisposableEffect(Unit) {
        if (!isPreview) {
            val auth = FirebaseAuth.getInstance()
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                currentUserUid.value = firebaseAuth.currentUser?.uid
            }
            auth.addAuthStateListener(listener)
            onDispose { auth.removeAuthStateListener(listener) }
        } else {
            onDispose {}
        }
    }
    val isOwnProfile = uid == null || uid == currentUserUid.value

    // ---- Connection state (only relevant for other users' profiles) ----
    val connectionStatus by connectionViewModel.connectionStatus.collectAsState()

    // NEW: live post count from the real posts collection (see ProfileViewModel.listenPostCount)
    val postCount by viewModel.postCount.collectAsState()

    // NEW: live connection count from the real connections collection
    val connectionCount by connectionViewModel.connectionCount.collectAsState()

    // NEW: local flag for instant "Request Sent" feedback right after swipe,
    // before the Firestore listener catches up and connectionStatus becomes "pending"
    var justSentRequest by remember { mutableStateOf(false) }

    LaunchedEffect(uid, currentUserUid.value) {
        if (isPreview) return@LaunchedEffect
        val targetUid = uid ?: currentUserUid.value ?: return@LaunchedEffect
        connectionViewModel.listenConnectionCount(targetUid) // NEW: live count for whichever profile is shown

        if (isOwnProfile) return@LaunchedEffect
        val myUid = currentUserUid.value ?: return@LaunchedEffect
        connectionViewModel.listenConnectionStatus(myUid, targetUid)
    }

    // Once the real listener confirms "pending" (or beyond), drop the temporary flag
    // so the permanent "Request Pending" / "Message" state takes over cleanly.
    LaunchedEffect(connectionStatus) {
        if (connectionStatus == "pending" || connectionStatus == "accepted") {
            justSentRequest = false
        }
    }

    // ---- Responsive sizing (compatible across all mobile widths) ----
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    // Avatar scales between 96dp (small phones ~320dp width) and 130dp (large phones ~430dp+ width)
    val avatarSize = remember(screenWidthDp) {
        max(96.dp.value, min(130.dp.value, screenWidthDp.value * 0.30f)).dp
    }
    // Horizontal padding scales between 12dp and 20dp based on screen width
    val horizontalPadding = remember(screenWidthDp) {
        max(12.dp.value, min(20.dp.value, screenWidthDp.value * 0.045f)).dp
    }

    // Dialog/sheet states
    var selectedSkillType by remember { mutableStateOf<String?>(null) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            coroutineScope.launch {
                val imageUrl = uploadToCloudinary(context, it)
                if (imageUrl.isNotBlank()) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .set(mapOf("avatarUrl" to imageUrl), SetOptions.merge())
                        .addOnSuccessListener {
                            viewModel.fetchUserProfile(uid)
                        }
                }
            }
        }
    }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let {
                coroutineScope.launch {
                    val imageUrl = uploadToCloudinary(context, it)
                    if (imageUrl.isNotBlank()) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .set(mapOf("avatarUrl" to imageUrl), SetOptions.merge())
                            .addOnSuccessListener {
                                viewModel.fetchUserProfile(uid)
                            }
                    }
                }
            }
        }
    }

    // Dummy posts - replace with Firestore data later
    val dummyPosts = listOf(
        Post(id = "1", heading = "Modern Web Development Trends 2024", description = "In this post I walk you through modern web development.", likeCount = 124, commentCount = 18, imageUrl = "purple"),
        Post(id = "2", heading = "UI/UX Design Principles for Mobile", description = "Learn core principles of mobile UI/UX design.", likeCount = 85, commentCount = 12, imageUrl = "red"),
        Post(id = "3", heading = "Flutter vs Kotlin", description = "A detailed comparison between Flutter and Kotlin.", likeCount = 60, commentCount = 9, imageUrl = "green"),
        Post(id = "4", heading = "Clean Architecture in Android", description = "How to structure your Android app using Clean Architecture.", likeCount = 45, commentCount = 7, imageUrl = "orange"),
    )

    // ---- Skills Dialog ----
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
                    val skills = if (selectedSkillType == "learning")
                        userProfile?.learningSkills else userProfile?.knownSkills
                    if (skills.isNullOrEmpty()) {
                        Text("No skills added yet", color = AppColors.TextSecondary)
                    } else {
                        skills.forEach {
                            Text("• $it", fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp), color = AppColors.TextPrimary)
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

    // ---- Image Picker Dialog ----
    ImagePickerDialog(
        showDialog = showImagePickerDialog,
        onDismissRequest = { showImagePickerDialog = false },
        onCameraClick = {
            showImagePickerDialog = false
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                java.io.File.createTempFile("avatar_", ".jpg", context.cacheDir)
            )
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        },
        onGalleryClick = {
            showImagePickerDialog = false
            galleryLauncher.launch("image/*")
        }
    )

    // ---- Post Detail Bottom Sheet ----
    if (selectedPost != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedPost = null },
            sheetState = sheetState,
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

                listOf("Save post", "Hide post", "Report post").forEach { action ->
                    TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                        Text(action, color = AppColors.TextPrimary, fontSize = 16.sp)
                    }
                }
                TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text("Delete post", color = AppColors.Error, fontSize = 16.sp)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // ---- Main Screen ----
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Loading state
        if (uiState is ProfileState.Loading) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            }
            return@LazyColumn
        }

        // ---- Top Bar ----
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
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = AppColors.Icon
                    )
                }
            }
        }

        // ---- Avatar with Edit Icon ----
        item {
            Spacer(Modifier.height(10.dp))
            Box(contentAlignment = Alignment.BottomEnd) {

                if (userProfile?.avatarUrl.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(avatarSize) // FIX: was 140.dp, now matches image size + scales per screen
                            .clip(CircleShape)
                            .background(AppColors.SecondaryContainer)
                            .border(2.dp, AppColors.Primary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = userProfile?.name
                            ?.split(" ")
                            ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                            ?.take(2)
                            ?.joinToString("") ?: "?"
                        Text(initials, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    }
                } else {
                    AsyncImage(
                        model = userProfile?.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(avatarSize) // FIX: was 110.dp, now matches placeholder size + scales per screen
                            .clip(CircleShape)
                            .border(2.dp, AppColors.Primary.copy(alpha = 0.3f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Edit badge — white ring + single dark green background, no double bg
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

        // ---- Name + College + Location + Bio ----
        item {
            Text(
                text = userProfile?.name ?: "Your Name",
                fontSize = 24.sp,
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
                fontSize = 17.sp,
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

        // ---- Stats Card (Posts, Helps, Connections) ----
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

        // ---- Edit Profile (own profile) OR Connect/Pending/Message (other user's profile) ----
        item {
            if (isOwnProfile) {
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
            } else {
                when {
                    connectionStatus == "accepted" -> {
                        Button(
                            onClick = {
                                val myUid = currentUserUid.value ?: return@Button
                                val targetUid = uid ?: return@Button
                                val chatId = listOf(myUid, targetUid).sorted().joinToString("_")
                                onNavigateToChat(chatId)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                        ) {
                            Text("Message", fontWeight = FontWeight.Medium, color = AppColors.TextWhite)
                        }
                    }
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
                    // NEW: instant confirmation right after swipe, while listener hasn't
                    // caught up to "pending" yet
                    justSentRequest -> {
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
                        // null or "rejected" -> allow sending a fresh connection request
                        SlideToSwapButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding),
                            onConfirmed = {
                                val myUid = currentUserUid.value ?: return@SlideToSwapButton
                                val targetUid = uid ?: return@SlideToSwapButton
                                connectionViewModel.sendConnectionRequest(
                                    currentUserId = myUid,
                                    targetUserId = targetUid,
                                    onSuccess = {
                                        // Only show "Request Sent" once Firestore actually confirms the write
                                        justSentRequest = true
                                    },
                                    onFailure = { e ->
                                        android.widget.Toast.makeText(
                                            context,
                                            "Couldn't send request: ${e.localizedMessage ?: "check your connection"}",
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

        // ---- Can Teach Section ----
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
                        Text("Can Teach", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    }
                    Spacer(Modifier.height(10.dp))

                    val teachSkills = userProfile?.knownSkills
                    if (teachSkills.isNullOrEmpty()) {
                        Text("No skills added yet", fontSize = 13.sp, color = AppColors.TextSecondary)
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            teachSkills.forEach { skill ->
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

        // ---- Wants to Learn Section ----
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

                    val learnSkills = userProfile?.learningSkills
                    if (learnSkills.isNullOrEmpty()) {
                        Text("No skills added yet", fontSize = 13.sp, color = AppColors.TextSecondary)
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            learnSkills.forEach { skill ->
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
            Spacer(Modifier.height(20.dp))
        }

        // ---- Recent Recognition ----
        item {
            Text(
                "Recent Recognition",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            )
            Spacer(Modifier.height(10.dp))

            // Top Mentor badge card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFF085041),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Top Mentor (March)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                        Text("Helped 12 peers with Design Systems.", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            // Two stat cards side by side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF085041),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = Color(0xFF835400),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ---- Posts Header ----
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

        // ---- Posts Grid ----
        items(dummyPosts.chunked(2)) { rowPosts ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowPosts.forEach { post ->
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
                if (rowPosts.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// ---- Image Picker Dialog ----
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

// Separate function for uploading to Cloudinary
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun uploadToCloudinary(context: android.content.Context, uri: Uri): String {
    val cloudName = "db7wneko6"
    val uploadPreset = "peerlearn_avatar"

    val stream = context.contentResolver.openInputStream(uri) ?: return ""
    val originalBitmap = android.graphics.BitmapFactory.decodeStream(stream)
    stream.close()

    val size = minOf(originalBitmap.width, originalBitmap.height)
    val x = (originalBitmap.width - size) / 2
    val y = (originalBitmap.height - size) / 2
    val cropped = android.graphics.Bitmap.createBitmap(originalBitmap, x, y, size, size)

    val resized = android.graphics.Bitmap.createScaledBitmap(cropped, 400, 400, true)
    val baos = java.io.ByteArrayOutputStream()
    resized.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, baos)
    val bytes = baos.toByteArray()

    return withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            val boundary = "Boundary-${System.currentTimeMillis()}"
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            val output = connection.outputStream
            output.write("--$boundary\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\n$uploadPreset\r\n".toByteArray())
            output.write("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"avatar.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n".toByteArray())
            output.write(bytes)
            output.write("\r\n--$boundary--\r\n".toByteArray())
            output.flush()

            val response = connection.inputStream.bufferedReader().readText()
            org.json.JSONObject(response).getString("secure_url")
                .replace("/upload/", "/upload/w_400,h_400,c_thumb,g_face/")
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}