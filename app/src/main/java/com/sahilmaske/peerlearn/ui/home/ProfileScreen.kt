package com.sahilmaske.peerlearn.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
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
import com.sahilmaske.peerlearn.model.Post
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.ProfileState
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    // ---- State & Data ----
    val userProfile by viewModel.userProfile.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Fetch current user profile on first launch
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        viewModel.fetchUserProfile(uid)
    }

    // Dialog/sheet states
    var selectedSkillType by remember { mutableStateOf<String?>(null) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadToCloudinary(context, it, viewModel) }
    }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let { uploadToCloudinary(context, it, viewModel) }
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
    // Shows when user taps Learning or Known card
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
    // Shows Camera / Gallery options when user taps camera icon
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
    // Shows when user long presses a post card
    if (selectedPost != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedPost = null },
            sheetState = sheetState,
            containerColor = AppColors.Surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Post info
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

                // Post actions
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
        modifier = Modifier.fillMaxSize()
            .background(AppColors.Background)
            .padding(12.dp),
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(AppColors.Surface.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {}) {
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

        // ---- Avatar with Camera Icon ----
        item {
            Spacer(Modifier.height(10.dp))
            Box(contentAlignment = Alignment.BottomEnd) {

                // Avatar - shows initials if no image, else Coil loads image
                if (userProfile?.avatarUrl.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier.size(110.dp).clip(CircleShape).background(AppColors.SecondaryContainer),
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
                        modifier = Modifier.size(110.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Camera icon - bottom right corner like WhatsApp
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary)
                        .clickable { showImagePickerDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = AppColors.TextWhite, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ---- Name + Skills Text ----
        item {
            Text(
                userProfile?.name ?: "Your Name",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${userProfile?.knownSkills?.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "I know this Skill"} • ${userProfile?.location ?: "User Location"}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = AppColors.TextSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                userProfile?.learningSkills?.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "I want to learn this Skill",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = AppColors.TextSecondary
            )
            Spacer(Modifier.height(16.dp))
        }

        // ---- Stats Card (Posts, Helps, Connections) ----
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text((userProfile?.postCount ?: 0).toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        Text("Post", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                    Text("|", fontSize = 28.sp, color = AppColors.Divider)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text((userProfile?.helpCount ?: 0).toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        Text("Helps", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                    Text("|", fontSize = 28.sp, color = AppColors.Divider)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text((userProfile?.connection ?: 0).toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        Text("Connections", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // ---- Edit Profile Button ----
        item {
            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AppColors.Primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Primary)
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = AppColors.Primary)
                Spacer(Modifier.width(8.dp))
                Text("Edit Profile", fontWeight = FontWeight.Medium, color = AppColors.Primary)
            }
            Spacer(Modifier.height(16.dp))
        }

        // ---- Skills Cards (Learning + Known) ----
        // Tap on card to see all skills in a dialog
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Learning skills card
                Card(
                    onClick = { selectedSkillType = "learning" },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AppColors.Divider)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Learning", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        val skills = userProfile?.learningSkills
                        if (skills.isNullOrEmpty()) {
                            Text("No skills added", fontSize = 12.sp, color = AppColors.TextSecondary)
                        } else {
                            skills.forEach { Text("• $it", fontSize = 12.sp, color = AppColors.TextSecondary) }
                        }
                    }
                }

                // Known skills card
                Card(
                    onClick = { selectedSkillType = "known" },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AppColors.Divider)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Known", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        val skills = userProfile?.knownSkills
                        if (skills.isNullOrEmpty()) {
                            Text("No skills added", fontSize = 12.sp, color = AppColors.TextSecondary)
                        } else {
                            skills.forEach { Text("• $it", fontSize = 12.sp, color = AppColors.TextSecondary) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ---- Posts Header ----
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Posts", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = AppColors.TextPrimary)
                Text("See all", color = AppColors.Primary, fontSize = 14.sp)
            }
            Spacer(Modifier.height(8.dp))
        }

        // ---- Posts Grid ----
        // 2 posts per row, long press to see detail in bottom sheet
        items(dummyPosts.chunked(2)) { rowPosts ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
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
                        shape = RoundedCornerShape(16.dp),
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
                // Fill empty space if only 1 post in row
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
fun uploadToCloudinary(context: android.content.Context, uri: Uri, viewModel: ProfileViewModel) {
    val cloudName = "db7wneko6" // Your Cloudinary Cloud Name
    val uploadPreset = "peerlearn_avatar" // SAHI

    val stream = context.contentResolver.openInputStream(uri) ?: return
    val originalBitmap = android.graphics.BitmapFactory.decodeStream(stream)
    stream.close()

// Square crop - center se
    val size = minOf(originalBitmap.width, originalBitmap.height)
    val x = (originalBitmap.width - size) / 2
    val y = (originalBitmap.height - size) / 2
    val cropped = android.graphics.Bitmap.createBitmap(originalBitmap, x, y, size, size)

// Resize to 400x400
    val resized = android.graphics.Bitmap.createScaledBitmap(cropped, 400, 400, true)
    val baos = java.io.ByteArrayOutputStream()
    resized.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, baos)
    val bytes = baos.toByteArray()


    Thread {
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
            val imageUrl = org.json.JSONObject(response).getString("secure_url")
                .replace("/upload/", "/upload/w_400,h_400,c_thumb,g_face/")

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@Thread
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(mapOf("avatarUrl" to imageUrl), SetOptions.merge())
                .addOnSuccessListener {
                    viewModel.fetchUserProfile(uid)
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}