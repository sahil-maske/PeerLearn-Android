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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.sahilmaske.peerlearn.model.PeerSuggestion
import com.sahilmaske.peerlearn.model.Post
import com.sahilmaske.peerlearn.model.User
import com.sahilmaske.peerlearn.ui.home.HomeScreenComponents.PeerSuggestionCard
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.FeedViewModel
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel

private val TealPrimary = Color(0xFF0F6E6E)

@Composable
fun HomeScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel(),
    viewModel: FeedViewModel = viewModel(
        factory = FeedViewModel.provideFactory(profileViewModel)
    )
) {
    val suggestions by viewModel.suggestions.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()

    HomeScreenContent(
        userProfile = userProfile,
        suggestions = suggestions,
        posts = posts,
        onSeeAllClick = {
            navController.navigate("see_all_peers")
        },
        onHomeClick = { /* already on home */ },
        onSearchClick = { navController.navigate("search") },
        onAddClick = { navController.navigate("create_post") },
        onProfileClick = { navController.navigate("profile") }
    )
}

@Composable
fun HomeScreenContent(
    userProfile: User?,
    suggestions: List<PeerSuggestion>,
    posts: List<Post>,
    onSeeAllClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---- Top Bar ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "PeerLearn",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* navigate to notifications */ }) {
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
                            model = userProfile?.avatarUrl,
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
                // ---- Greeting ----
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recommended Peers",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            text = "See all",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TealPrimary,
                            modifier = Modifier.clickable { onSeeAllClick() }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.padding(start = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(suggestions) { peer ->
                            PeerSuggestionCard(peer = peer)
                        }
                    }
                }
                item {
                    Text(
                        text = "Community Feed",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                items(posts) { post ->
                    PostCard(
                        post = post,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }

        // ---- Bottom Navigation Bar ----
        BottomNavBar(
            onHomeClick = onHomeClick,
            onSearchClick = onSearchClick,
            onAddClick = onAddClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
fun BottomNavBar(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAddClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home (active/filled teal circle)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(TealPrimary)
                .clickable { onHomeClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = "Home",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        IconButton(onClick = onSearchClick) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = "Search",
                tint = AppColors.Icon,
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(onClick = onAddClick) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint = AppColors.Icon,
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(onClick = onProfileClick) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = "Profile",
                tint = AppColors.Icon,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    val mockSuggestions = listOf(
        PeerSuggestion(id = "1", name = "Sahil Maske", knowSkill = "Kotlin, Java", matchPercentage = 95),
        PeerSuggestion(id = "2", name = "John Doe", knowSkill = "Python, C++", matchPercentage = 80),
        PeerSuggestion(id = "3", name = "Jane Smith", knowSkill = "React, JS", matchPercentage = 70)
    )
    HomeScreenContent(
        userProfile = null,
        suggestions = mockSuggestions,
        posts = emptyList(),
        onSeeAllClick = {}
    )
}

@Composable
fun PostCard(post: Post, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar with initials, orange like the reference
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5A623)),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = post.authorName
                        .split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        .take(2)
                        .joinToString("")
                    Text(initials, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = post.authorName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = post.timeAgo, // e.g. "2 hours ago" — add this field to Post if missing
                        fontSize = 12.sp,
                        color = Color(0xFF8A8A8A)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = post.heading,
                fontWeight = FontWeight.SemiBold,
                color = TealPrimary,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = post.description, fontSize = 14.sp, color = Color(0xFF3A3A3A))
        }
    }
}