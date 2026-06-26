package com.sahilmaske.peerlearn.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.viewmodel.ProfileState
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel

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

        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
