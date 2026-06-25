package com.sahilmaske.peerlearn.ui.home


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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Airplay
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
           val uid = FirebaseAuth.getInstance().currentUser?.uid?: return@LaunchedEffect
            viewModel.fetchUserProfile(uid)
        }
    val userProfile by viewModel.userProfile.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState is ProfileState.Loading) {
            CircularProgressIndicator()
        } else {
            // profile UI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                IconButton(
                    onClick = {}
                ) {
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
                modifier = Modifier.size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8E8E93)),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = userProfile?.name?:"Your Name",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center

                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = userProfile?.college ?: "Your College",
                    fontSize = 14.sp,
                    color = Color(0xFF8E8E93)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
