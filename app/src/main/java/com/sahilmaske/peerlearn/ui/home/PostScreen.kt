package com.sahilmaske.peerlearn.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

// ==================== BOUNCING DOTS LOADER ====================
@Composable
fun BouncingDotsLoader(
    modifier: Modifier = Modifier,
    dotColor: Color = Color.White,
    dotSize: Dp = 8.dp
) {
    val dotCount = 3
    val infiniteTransition = rememberInfiniteTransition(label = "dotsLoader")

    val scales = List(dotCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 500,
                    delayMillis = index * 150,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dotScale$index"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        scales.forEach { scaleState ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        scaleX = scaleState.value
                        scaleY = scaleState.value
                        alpha = scaleState.value
                    }
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

// ==================== CLOUDINARY IMAGE UPLOAD ====================
suspend fun uploadImageToCloudinary(context: android.content.Context, uri: Uri): String {
    return suspendCancellableCoroutine { continuation ->
        val cloudName = "db7wneko6"
        val uploadPreset = "peerlearn_avatar"

        try {
            val stream = context.contentResolver.openInputStream(uri)
            val bytes = stream?.readBytes()
            stream?.close()

            if (bytes == null) {
                continuation.resumeWith(Result.success(""))
                return@suspendCancellableCoroutine
            }

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
                    output.write("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"post.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n".toByteArray())
                    output.write(bytes)
                    output.write("\r\n--$boundary--\r\n".toByteArray())
                    output.flush()

                    val response = connection.inputStream.bufferedReader().readText()
                    val imageUrl = org.json.JSONObject(response).getString("secure_url")
                    continuation.resumeWith(Result.success(imageUrl))
                } catch (e: Exception) {
                    continuation.resumeWith(Result.success(""))
                }
            }.start()
        } catch (e: Exception) {
            continuation.resumeWith(Result.success(""))
        }
    }
}

@Composable
fun PostScreen(
    onClose: () -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val userProfile by profileViewModel.userProfile.collectAsState()

    var selectedIntent by remember { mutableStateOf("teach") }
    var skill by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isTablet = maxWidth >= 600.dp
        val horizontalPadding = if (isTablet) 32.dp else 16.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = horizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                        .background(
                            color = AppColors.Surface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { onClose() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AppColors.Icon
                        )
                    }

                    Text(
                        text = "Create Post",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = AppColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = {},
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "School",
                            tint = AppColors.Icon
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                        .padding(8.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Share your expertise",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = AppColors.TextPrimary,
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Fill out the details below to find your next learning partner.",
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = AppColors.TextSecondary,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Intent",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AppColors.TextPrimary,
                        textAlign = TextAlign.Start
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.Surface)
                            .padding(4.dp)
                    ) {
                        val itemWidth = maxWidth / 3

                        val indicatorOffset by animateDpAsState(
                            targetValue = when (selectedIntent) {
                                "teach" -> 0.dp
                                "learn" -> itemWidth
                                else -> itemWidth * 2 // "help"
                            },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "indicatorOffset"
                        )

                        Box(modifier = Modifier.matchParentSize()) {
                            Box(
                                modifier = Modifier
                                    .offset(x = indicatorOffset)
                                    .width(itemWidth)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selectedIntent == "help") Color(0xFFD9822B) else AppColors.DarkGreen)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedIntent = "teach" }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Teach",
                                    color = if (selectedIntent == "teach") Color.White else AppColors.TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedIntent = "learn" }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Learn",
                                    color = if (selectedIntent == "learn") Color.White else AppColors.TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedIntent = "help" }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Need Help",
                                    color = if (selectedIntent == "help") Color.White else AppColors.TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "What's the skill?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = AppColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = skill,
                        onValueChange = { skill = it },
                        placeholder = {
                            Text(
                                text = "e.g. Urban Gardening, UI/UX Design",
                                color = AppColors.TextSecondary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = AppColors.Border,
                            focusedBorderColor = AppColors.DarkGreen,
                            unfocusedContainerColor = AppColors.Surface,
                            focusedContainerColor = AppColors.Surface
                        )
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Description/Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AppColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = {
                            Text(
                                text = when (selectedIntent) {
                                    "teach" -> "Describe what you can teach and your experience..."
                                    "learn" -> "Describe what you want to learn and your current level..."
                                    else -> "Describe what you need help with..."
                                },
                                color = AppColors.TextSecondary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = AppColors.Border,
                            focusedBorderColor = AppColors.DarkGreen,
                            unfocusedContainerColor = AppColors.Surface,
                            focusedContainerColor = AppColors.Surface
                        )
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = "Add a Photo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AppColors.TextPrimary
                    )
                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.5f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { imagePickerLauncher.launch("image/*") }
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                                            startY = 100f
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "PREVIEW",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Your post will be visible to peers",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawRoundRect(
                                    color = Color(0xFF1D9E75),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                                    ),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AppColors.SkillKnownBg.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Add photo",
                                        tint = AppColors.DarkGreen,
                                        modifier = Modifier.height(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tap to add a photo",
                                        color = AppColors.DarkGreen,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "or drop it here",
                                        color = AppColors.TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Posts with photos get 3x more responses",
                                        color = AppColors.TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    val isFormValid = skill.isNotBlank() && description.isNotBlank()
                    var isPosting by remember { mutableStateOf(false) }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isPosting = true
                                try {
                                    val uid = userProfile?.uid
                                        ?: FirebaseAuth.getInstance().currentUser?.uid

                                    if (uid != null) {
                                        var uploadedImageUrl = ""
                                        if (selectedImageUri != null) {
                                            uploadedImageUrl = uploadImageToCloudinary(context, selectedImageUri!!)
                                        }

                                        val newPost = hashMapOf(
                                            "authorId" to uid,
                                            "authorName" to (userProfile?.name ?: "Anonymous"),
                                            "authorAvatarUrl" to (userProfile?.avatarUrl ?: ""),
                                            "heading" to skill,
                                            "description" to description,
                                            "intent" to selectedIntent,
                                            "postType" to if (selectedImageUri != null) "image" else "text",
                                            "imageUrl" to uploadedImageUrl,
                                            "likeCount" to 0,
                                            "commentCount" to 0,
                                            "timestamp" to System.currentTimeMillis()
                                        )

                                        FirebaseFirestore.getInstance()
                                            .collection("posts")
                                            .add(newPost)
                                            .await()
                                    }

                                    isPosting = false
                                    skill = ""
                                    description = ""
                                    selectedImageUri = null
                                    selectedIntent = "teach"

                                    snackbarHostState.showSnackbar("Post shared successfully!")
                                    onClose()
                                } catch (e: Exception) {
                                    isPosting = false
                                    snackbarHostState.showSnackbar("Failed to post: ${e.message}")
                                }
                            }
                        },
                        enabled = isFormValid && !isPosting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.DarkGreen,
                            disabledContainerColor = AppColors.DarkGreen.copy(alpha = 0.4f)
                        )
                    ) {
                        if (isPosting) {
                            BouncingDotsLoader(dotColor = Color.White)
                        } else {
                            Text(
                                text = "Post to Community",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.height(18.dp)
                            )
                        }
                    }

                    if (!isFormValid) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Fill in skill and description to continue",
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Preview(showBackground = true, name = "Phone")
@Composable
fun PostScreenPreview() {
    PostScreen()
}

@Preview(showBackground = true, name = "Small Phone", widthDp = 320, heightDp = 640)
@Composable
fun PostScreenPreviewSmall() {
    PostScreen()
}

@Preview(showBackground = true, name = "Tablet", widthDp = 800, heightDp = 1000)
@Composable
fun PostScreenPreviewTablet() {
    PostScreen()
}