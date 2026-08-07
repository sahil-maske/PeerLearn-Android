package com.sahilmaske.peerlearn.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.model.Comment
import com.sahilmaske.peerlearn.model.Post
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.util.timeAgo
import com.sahilmaske.peerlearn.viewmodel.HelpDetailViewModel
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpDetailScreen(
    postId: String,
    onBack: () -> Unit,
    viewModel: HelpDetailViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val post by viewModel.post.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()
    val currentUserId = remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid ?: "") }
    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUserId.value = firebaseAuth.currentUser?.uid ?: ""
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
        viewModel.listenComments(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help Request Detail", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Surface)
            )
        },
        bottomBar = {
            CommentInputField { text ->
                val user = userProfile
                user?.let {
                    viewModel.addComment(
                        postId = postId,
                        authorId = currentUserId.value,
                        authorName = it.name,
                        authorAvatarUrl = it.avatarUrl,
                        text = text
                    )
                }
            }
        },
        containerColor = AppColors.Background
    )
{ padding ->
        if (post == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    PostDetailHeader(post!!)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Help Offers (${post!!.commentCount})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (comments.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No help offers yet. Be the first to help!", color = AppColors.TextSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(comments) { comment ->
                        HelpOfferRow(
                            comment = comment,
                            isPostOwner = post!!.authorId == currentUserId.value,
                            onMarkHelpful = {
                                viewModel.toggleMarkAsHelpful(
                                    postId = postId,
                                    commentId = comment.id,
                                    helperId = comment.authorId,
                                    currentlyMarked = comment.isMarkedHelpful
                                )
                            }
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun PostDetailHeader(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.authorAvatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(timeAgo(post.timestamp), color = AppColors.TextSecondary, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            if (post.heading.isNotEmpty()) {
                Text(post.heading, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AppColors.TextPrimary)
                Spacer(Modifier.height(4.dp))
            }
            Text(post.description, fontSize = 15.sp, color = AppColors.TextPrimary)
            
            if (post.imageUrl.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            if (post.skill.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = AppColors.SkillLearnBg,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.SkillLearnBorder)
                ) {
                    Text(
                        text = "Seeking help with: ${post.skill}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = AppColors.SkillLearnText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun HelpOfferRow(
    comment: Comment,
    isPostOwner: Boolean,
    onMarkHelpful: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        border = if (comment.isMarkedHelpful) androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.DarkGreen) else null
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = comment.authorAvatarUrl,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(comment.authorName, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text(timeAgo(comment.timestamp), color = AppColors.TextSecondary, fontSize = 11.sp)
                }
                if (comment.isMarkedHelpful) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Helpful",
                        tint = AppColors.DarkGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(comment.text, fontSize = 14.sp, color = AppColors.TextPrimary)
            
            if (isPostOwner && !comment.isMarkedHelpful) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onMarkHelpful,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.SecondaryContainer, contentColor = AppColors.Secondary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Mark as Helpful", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CommentInputField(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier.fillMaxWidth().imePadding(),
        color = AppColors.Surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Offer help or comment...", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.Border
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSend(text)
                        text = ""
                    }
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = AppColors.Primary)
            }
        }
    }
}
