package com.sahilmaske.peerlearn.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.model.PeerSuggestion
import com.sahilmaske.peerlearn.model.Post
import com.sahilmaske.peerlearn.model.User
import com.sahilmaske.peerlearn.ui.home.HomeScreenComponents.CommentsBottomSheet
import com.sahilmaske.peerlearn.ui.home.HomeScreenComponents.PeerSuggestionCard
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.util.timeAgo
import com.sahilmaske.peerlearn.viewmodel.FeedViewModel
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel(),
    viewModel: FeedViewModel = viewModel(
        factory = FeedViewModel.provideFactory(profileViewModel)
    ),
    currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
) {
    val suggestions by viewModel.suggestions.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()

    HomeScreenContent(
        userProfile = userProfile,
        suggestions = suggestions,
        posts = posts,
        currentUserId = currentUserId,
        onSeeAllClick = {
            navController.navigate("see_all_peers")
        },
        onPeerClick = { uid ->
            navController.navigate("profile/$uid")
        },
        onNotificationClick = {
            navController.navigate("notifications")
        },
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    userProfile: User?,
    suggestions: List<PeerSuggestion>,
    posts: List<Post>,
    currentUserId: String,
    onSeeAllClick: () -> Unit,
    onPeerClick: (String) -> Unit,
    onNotificationClick: () -> Unit,
    viewModel: FeedViewModel? = null
) {
    // ---- Comments bottom sheet state ----
    var activeCommentsPostId by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val comments by (viewModel?.comments?.collectAsState() ?: remember { mutableStateOf(emptyList()) })

    LaunchedEffect(activeCommentsPostId) {
        val postId = activeCommentsPostId
        if (postId != null) {
            viewModel?.loadComments(postId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ---- Top Bar ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = Color(0xFF0F6E6E),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "PeerLearn",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F6E6E)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = AppColors.Icon,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                if (userProfile?.avatarUrl.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AppColors.SecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = userProfile?.name
                            ?.split(" ")
                            ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                            ?.take(2)
                            ?.joinToString("") ?: "?"
                        Text(initials, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    }
                } else {
                    AsyncImage(
                        model = userProfile.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // ---- Scrollable content ----
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    val firstName = userProfile?.name?.split(" ")?.firstOrNull() ?: "there"
                    Text(
                        text = "Good morning, $firstName",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ready to share a skill today?",
                        fontSize = 15.sp,
                        color = Color(0xFF6B6B6B)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recommended Peers",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF000000)
                    )
                    Text(
                        text = "See all",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F6E6E),
                        modifier = Modifier.clickable { onSeeAllClick() }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(suggestions) { peer ->
                        PeerSuggestionCard(
                            peer = peer,
                            onPeerClick = onPeerClick
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Community Feed",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF000000)
                )
            }

            items(posts) { post ->
                PostCard(
                    post = post,
                    currentUserId = currentUserId,
                    onLikeClick = {
                        val isLiked = currentUserId in post.likedBy
                        viewModel?.toggleLike(post.id, currentUserId, isLiked)
                    },
                    onCommentClick = {
                        activeCommentsPostId = post.id
                    }
                )
            }
        }
    }

    // ---- Comments Bottom Sheet ----
    if (activeCommentsPostId != null) {
        CommentsBottomSheet(
            comments = comments,
            sheetState = sheetState,
            onDismiss = { activeCommentsPostId = null },
            onSendComment = { text ->
                val postId = activeCommentsPostId
                if (postId != null) {
                    viewModel?.addComment(
                        postId = postId,
                        authorId = currentUserId,
                        authorName = userProfile?.name ?: "Anonymous",
                        authorAvatarUrl = userProfile?.avatarUrl ?: "",
                        text = text
                    )
                }
            }
        )
    }
}

// ==================== POST CARD (X/Twitter style) ====================
@Composable
fun PostCard(
    post: Post,
    currentUserId: String = "",
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {}
) {
    val initials = post.authorName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")

    val isLiked = currentUserId.isNotEmpty() && currentUserId in post.likedBy

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // ---- Avatar ----
            if (post.authorAvatarUrl.isNotEmpty()) {
                AsyncImage(
                    model = post.authorAvatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AppColors.SkillKnownBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.SkillKnownText)
                }
            }
            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // ---- Name + time + badge row ----
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(post.authorName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                    Spacer(Modifier.width(6.dp))
                    Text("· ${timeAgo(post.timestamp)}", fontSize = 12.sp, color = AppColors.TextSecondary)

                    if (post.postType == "query") {
                        Spacer(Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFEEEDFE))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Query", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3C3489))
                        }
                    } else if (post.intent.isNotEmpty()) {
                        Spacer(Modifier.weight(1f))
                        val isTeaching = post.intent == "teach"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isTeaching) AppColors.SkillKnownBg else AppColors.SkillLearnBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                if (isTeaching) "Teaching" else "Learning",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isTeaching) AppColors.SkillKnownText else AppColors.SkillLearnText
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                if (post.heading.isNotEmpty()) {
                    Text(
                        text = post.heading,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    text = post.description,
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary,
                    lineHeight = 18.sp
                )

                if (post.postType == "image" && post.imageUrl.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "Post image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                Spacer(Modifier.height(10.dp))

                // ---- Engagement row: comment, like, share ----
                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCommentClick() }
                    ) {
                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comments", tint = AppColors.TextSecondary, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${post.commentCount}", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLikeClick() }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color(0xFFE0245E) else AppColors.TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${post.likeCount}", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                    Icon(Icons.Outlined.Share, contentDescription = "Share", tint = AppColors.TextSecondary, modifier = Modifier.size(15.dp))
                }
            }
        }
    }

    HorizontalDivider(color = AppColors.Divider, thickness = 0.5.dp)
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    val mockSuggestions = listOf(
        PeerSuggestion(id = "1", name = "Sahil Maske", knowSkill = "Kotlin, Java", matchPercentage = 95),
        PeerSuggestion(id = "2", name = "John Doe", knowSkill = "Python, C++", matchPercentage = 80),
        PeerSuggestion(id = "3", name = "Jane Smith", knowSkill = "React, JS", matchPercentage = 70)
    )
    val mockPosts = listOf(
        Post(
            id = "p1",
            authorName = "Sahil Maske",
            heading = "UI/UX Design",
            description = "Can help with Figma basics and prototyping fundamentals.",
            intent = "teach",
            postType = "text",
            likeCount = 12,
            commentCount = 3,
            timestamp = System.currentTimeMillis() - 2 * 60 * 60 * 1000
        ),
        Post(
            id = "p2",
            authorName = "Riya K.",
            heading = "How do I center a div in CSS?",
            description = "Tried flexbox but it's not working in my grid layout.",
            postType = "query",
            likeCount = 2,
            commentCount = 5,
            timestamp = System.currentTimeMillis() - 6 * 60 * 60 * 1000
        )
    )
    HomeScreenContent(
        userProfile = null,
        suggestions = mockSuggestions,
        posts = mockPosts,
        currentUserId = "mock_user_id",
        onSeeAllClick = {},
        onPeerClick = {},
        onNotificationClick = {}
    )
}