package com.sahilmaske.peerlearn.ui.settings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.data.model.User
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.ConnectionViewModel

@Composable
fun BlockUsersScreen(
    connectionViewModel: ConnectionViewModel = viewModel(),
    onBack: () -> Unit
) {
    val isPreview = LocalInspectionMode.current
    val blockedUsersState = if (isPreview) {
        remember {
            mutableStateOf(listOf(
                User(uid = "1", name = "John Doe", college = "PeerLearn Institute"),
                User(uid = "2", name = "Jane Smith", college = "Tech University")
            ))
        }
    } else {
        connectionViewModel.blockedUsers.collectAsState()
    }
    val blockedUsers by blockedUsersState

    var unblockingUid by remember { mutableStateOf<String?>(null) }

    val myUid = remember(isPreview) {
        if (isPreview) "preview_uid" else {
            try {
                FirebaseAuth.getInstance().currentUser?.uid
            } catch (_: Exception) {
                null
            }
        }
    }

    LaunchedEffect(myUid) {
        if (!isPreview && myUid != null) {
            connectionViewModel.listenBlockedUsers(myUid)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Back",
                    tint = AppColors.Primary
                )
            }
            Text(
                text = "Blocked Users",
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = AppColors.TextPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.padding(8.dp)
        ) {
        Text(
            text = "Blocked users can't send you connections request or messages.",
            fontSize = 14.sp,
            color = AppColors.TextSecondary,

        )
            Spacer(modifier = Modifier.height(8.dp))
            if (blockedUsers.isEmpty()) {
                // ---- Empty state ----
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AppColors.PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = AppColors.Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("No blocked users", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Users you block will appear here",
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary
                    )
                }
            } else {
                // ---- List ----
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(blockedUsers, key = { it.uid }) { blockedUser ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar with initials fallback, same pattern as ProfileScreen.
                            if (blockedUser.avatarUrl.isNullOrEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.SecondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initials = blockedUser.name
                                        .split(" ")
                                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                        .take(2)
                                        .joinToString("")
                                    Text(initials, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                }
                            } else {
                                AsyncImage(
                                    model = blockedUser.avatarUrl,
                                    contentDescription = blockedUser.name,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    blockedUser.name.ifBlank { "Unknown User" },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.TextPrimary
                                )
                                if (blockedUser.college.isNotBlank()) {
                                    Text(
                                        blockedUser.college,
                                        fontSize = 13.sp,
                                        color = AppColors.TextSecondary
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    if (isPreview) return@OutlinedButton
                                    val myUid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null } ?: return@OutlinedButton
                                    unblockingUid = blockedUser.uid
                                    connectionViewModel.unblockUser(
                                        currentUserId = myUid,
                                        targetUserId = blockedUser.uid,
                                        onSuccess = { unblockingUid = null },
                                        onFailure = { unblockingUid = null }
                                    )
                                },
                                enabled = unblockingUid != blockedUser.uid,
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Primary)
                            ) {
                                Text(
                                    if (unblockingUid == blockedUser.uid) "Unblocking..." else "Unblock",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp),
                            color = AppColors.Divider,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BlockUsersScreenPreview() {

        BlockUsersScreen(onBack = {})
}
