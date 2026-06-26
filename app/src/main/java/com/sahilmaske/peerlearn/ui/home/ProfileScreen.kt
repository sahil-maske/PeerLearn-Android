package com.sahilmaske.peerlearn.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.viewmodel.ProfileState
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel
import java.nio.file.WatchEvent

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        viewModel.fetchUserProfile(uid)
    }

    val userProfile by viewModel.userProfile.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState is ProfileState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // profile UI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.ArrowBackIos, contentDescription = "Back")
                }
                Text(
                    text = "Profile",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8E8E93)),
                contentAlignment = Alignment.Center
            ) {
                val initials = userProfile?.name
                    ?.split(" ")
                    ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    ?.take(2)
                    ?.joinToString("") ?: "?"
                Text(
                    text = initials,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userProfile?.name ?: "Your Name",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${userProfile?.knownSkills?.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "I know this Skill"} • ${userProfile?.location ?: "User Location"}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.W900,
                color = Color(0xFF8E8E93)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = userProfile?.learningSkills?.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "I want to learn this Skill",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8E8E93)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (userProfile?.postCount ?: 0).toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Post", fontSize = 12.sp, color = Color(0xFF8A8A93))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (userProfile?.helpCount ?: 0).toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Helps", fontSize = 12.sp, color = Color(0xFF8A8A93))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (userProfile?.connection ?: 0).toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Connections", fontSize = 12.sp, color = Color(0xFF8A8A93))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.5.dp))

            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF7C5CFC)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7C5CFC))
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Edit Profile", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.5.dp))

           Row(
               modifier = Modifier
                   .fillMaxWidth()
                   .padding(horizontal = 16.dp),
               horizontalArrangement = Arrangement.SpaceBetween

           ) {
               // Learning Card
               Card(
                   modifier = Modifier.weight(1f),
                   colors = CardDefaults.cardColors(containerColor = Color(0xFFF1ECFF))
               ) {
                   Column(modifier = Modifier.padding(16.dp)) {
                       Text("Learning", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                       Spacer(modifier = Modifier.height(8.dp))

                       val learningSkills = userProfile?.learningSkills ?: emptyList()
                       if (learningSkills.isEmpty()) {
                           Text("No skills added", fontSize = 11.sp, color = Color(0xFF8A8A93))
                       } else {
                           learningSkills.chunked(2).forEach { rowSkills ->
                               Row(
                                   horizontalArrangement = Arrangement.spacedBy(6.dp),
                                   modifier = Modifier.padding(bottom = 6.dp)
                               ) {
                                   rowSkills.forEach { skill ->
                                       Box(
                                           modifier = Modifier
                                               .clip(RoundedCornerShape(8.dp))
                                               .background(Color(0xFFE3D9FF))
                                               .padding(horizontal = 10.dp, vertical = 5.dp)
                                       ) {
                                           Text(
                                               text = skill,
                                               fontSize = 11.sp,
                                               fontWeight = FontWeight.Medium,
                                               color = Color(0xFF3C3489)
                                           )
                                       }
                                   }
                               }
                           }
                       }
                   }
               }

               Spacer(modifier = Modifier.width(12.dp))

// Known Card
               Card(
                   modifier = Modifier.weight(1f),
                   colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F8EC))
               ) {
                   Column(modifier = Modifier.padding(16.dp)) {
                       Text("Known", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                       Spacer(modifier = Modifier.height(8.dp))

                       val knownSkills = userProfile?.knownSkills ?: emptyList()
                       if (knownSkills.isEmpty()) {
                           Text("No skills added", fontSize = 11.sp, color = Color(0xFF8A8A93))
                       } else {
                           knownSkills.chunked(2).forEach { rowSkills ->
                               Row(
                                   horizontalArrangement = Arrangement.spacedBy(6.dp),
                                   modifier = Modifier.padding(bottom = 6.dp)
                               ) {
                                   rowSkills.forEach { skill ->
                                       Box(
                                           modifier = Modifier
                                               .clip(RoundedCornerShape(8.dp))
                                               .background(Color(0xFFCFF3D8))
                                               .padding(horizontal = 10.dp, vertical = 5.dp)
                                       ) {
                                           Text(
                                               text = skill,
                                               fontSize = 11.sp,
                                               fontWeight = FontWeight.Medium,
                                               color = Color(0xFF27500A)
                                           )
                                       }
                                   }
                               }
                           }
                       }
                   }
               }
           }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState is ProfileState.Loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header spans both columns
                        item(span = { GridItemSpan(2) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // <-- PASTE your existing top bar, avatar, name, subtitle,
                                //     stats card, edit button, learning/known chips here.
                                //     Everything you already built stays exactly the same.

                                Spacer(modifier = Modifier.height(20.dp))

                                // Post section header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Post", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "See all",
                                            fontSize = 13.sp,
                                            color = Color(0xFF7C5CFC),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Icon(
                                            Icons.Filled.ChevronRight,
                                            contentDescription = null,
                                            tint = Color(0xFF7C5CFC),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        // Post grid items
                        items(samplePosts) { post ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .background(Brush.linearGradient(post.gradientColors)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            post.icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            post.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 2
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Outlined.FavoriteBorder,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = Color(0xFF8A8A93)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("${post.likes}", fontSize = 11.sp, color = Color(0xFF8A8A93))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Icon(
                                                Icons.Outlined.ChatBubbleOutline,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = Color(0xFF8A8A93)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("${post.comments}", fontSize = 11.sp, color = Color(0xFF8A8A93))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}

data class Post(
    val title: String,
    val icon: ImageVector,
    val likes: Int,
    val comments: Int,
    val gradientColors: List<Color>
)

val samplePosts = listOf(
    Post(
        title = "Modern Web Development Trends 2024",
        icon = Icons.Default.Code,
        likes = 124,
        comments = 18,
        gradientColors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
    ),
    Post(
        title = "UI/UX Design Principles for Mobile",
        icon = Icons.Default.Palette,
        likes = 85,
        comments = 12,
        gradientColors = listOf(Color(0xFFFF512F), Color(0xFFDD2476))
    ),
    Post(
        title = "Mastering Kotlin Coroutines",
        icon = Icons.Default.Bolt,
        likes = 210,
        comments = 45,
        gradientColors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
    ),
    Post(
        title = "Firebase Authentication Guide",
        icon = Icons.Default.Lock,
        likes = 156,
        comments = 22,
        gradientColors = listOf(Color(0xFFF2994A), Color(0xFFF2C94C))
    )
)
