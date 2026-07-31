package com.sahilmaske.peerlearn.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.model.Connection
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.ConnectionViewModel

@Composable
fun NotificationScreen(
    connectionViewModel: ConnectionViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {}
) {
    val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val incomingRequests by connectionViewModel.incomingRequests.collectAsState()

    LaunchedEffect(myUid) {
        connectionViewModel.listenIncomingRequests(myUid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // ---- Header ----
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                "Notifications",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Stay updated with your learning community.",
                fontSize = 13.sp,
                color = AppColors.TextSecondary
            )
        }

        HorizontalDivider(color = AppColors.Divider, thickness = 0.7.dp)

        // ---- Body ----
        if (incomingRequests.isEmpty()) {
            EmptyNotificationsState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(incomingRequests, key = { it.connectionId }) { request ->
                    ConnectionRequestCard(
                        request = request,
                        onAccept = {
                            connectionViewModel.respondToConnection(
                                connectionId = request.connectionId,
                                accept = true,
                                userA = request.userA,
                                userB = request.userB
                            )
                        },
                        onDeny = {
                            connectionViewModel.respondToConnection(
                                connectionId = request.connectionId,
                                accept = false,
                                userA = request.userA,
                                userB = request.userB
                            )
                        }
                    )
                }
            }
        }
    }
}

// ---- Empty State ----
@Composable
fun EmptyNotificationsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(AppColors.SecondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.NotificationsNone,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(42.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "No notifications yet",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "You're all caught up! New connection\nrequests will show up here.",
            fontSize = 13.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// ---- Connection Request Card ----
@Composable
fun ConnectionRequestCard(
    request: Connection,
    onAccept: () -> Unit,
    onDeny: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE3C2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.SwapHoriz,
                        contentDescription = null,
                        tint = Color(0xFFB5651D),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "CONNECTION REQUEST",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextSecondary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "New request from user", // TODO: replace with fetched name
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary)
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                ) {
                    Text("Accept", fontWeight = FontWeight.Medium, color = AppColors.TextWhite)
                }
                OutlinedButton(
                    onClick = onDeny,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Error)
                ) {
                    Text("Deny", fontWeight = FontWeight.Medium, color = AppColors.Error)
                }
            }
        }
    }
}