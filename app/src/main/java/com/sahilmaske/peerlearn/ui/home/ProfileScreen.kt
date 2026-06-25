package com.sahilmaske.peerlearn.ui.home


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
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
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = userProfile?.name ?: "Loading...")
        Text(text = userProfile?.college ?: "Loading...")
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
