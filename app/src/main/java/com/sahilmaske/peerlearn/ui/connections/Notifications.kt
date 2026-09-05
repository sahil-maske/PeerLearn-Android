package com.sahilmaske.peerlearn.ui.connections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.data.model.Connection
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.ConnectionViewModel

// Screen width buckets — phone / foldable / tablet.
// Just one number decides everything, easy to reason about.
private const val MEDIUM_BREAKPOINT = 600   // dp
private const val EXPANDED_BREAKPOINT = 840 // dp

@Composable
fun NotificationScreen(
    connectionViewModel: ConnectionViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToChat: (String) -> Unit = {}
) {
    val myUid = try {
        FirebaseAuth.getInstance().currentUser?.uid
    } catch (e: Exception) {
        null
    } ?: return

    val incomingRequests by connectionViewModel.incomingRequests.collectAsState()
    val senderNames by connectionViewModel.senderNames.collectAsState()

    LaunchedEffect(myUid) {
        connectionViewModel.listenIncomingRequests(myUid)
    }

    LaunchedEffect(incomingRequests) {
        if (incomingRequests.isNotEmpty()) {
            connectionViewModel.fetchSenderNames(incomingRequests.map { it.userA })
        }
    }

    NotificationContent(
        incomingRequests = incomingRequests,
        senderNames = senderNames,
        onAccept = { request ->
            connectionViewModel.respondToConnection(
                connectionId = request.connectionId,
                accept = true,
                userA = request.userA,
                userB = request.userB,
                requestedBy = request.requestedBy,
                matchedSkill = request.matchedSkill
            )
            onNavigateToChat(request.connectionId)
        },
        onDeny = { request ->
            connectionViewModel.respondToConnection(
                connectionId = request.connectionId,
                accept = false,
                userA = request.userA,
                userB = request.userB,
                requestedBy = request.requestedBy,
                matchedSkill = request.matchedSkill
            )
        }
    )
}

@Composable
fun NotificationContent(
    incomingRequests: List<Connection>,
    senderNames: Map<String, String>,
    onAccept: (Connection) -> Unit,
    onDeny: (Connection) -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidth >= MEDIUM_BREAKPOINT
    val useGrid = screenWidth >= EXPANDED_BREAKPOINT

    // Content never stretches full-width on large screens; stays centered
    // and capped so it's readable instead of edge-to-edge.
    val maxContentWidth: Dp = if (isTablet) 800.dp else Dp.Unspecified
    val sidePadding: Dp = if (isTablet) 32.dp else 16.dp

    Scaffold(
        topBar = {
            NotificationTopBar(isTablet = isTablet)
        },
        containerColor = AppColors.Background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(modifier = Modifier.widthIn(max = maxContentWidth).fillMaxSize()) {
                if (incomingRequests.isEmpty()) {
                    EmptyNotificationsState()
                } else if (useGrid) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(sidePadding),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(incomingRequests, key = { it.connectionId }) { request ->
                            ConnectionRequestCard(
                                request = request,
                                senderName = senderNames[request.userA],
                                onAccept = { onAccept(request) },
                                onDeny = { onDeny(request) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(sidePadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(incomingRequests, key = { it.connectionId }) { request ->
                            ConnectionRequestCard(
                                request = request,
                                senderName = senderNames[request.userA],
                                onAccept = { onAccept(request) },
                                onDeny = { onDeny(request) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---- Top bar: same content, scales cleanly for phone vs tablet ----
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTopBar(isTablet: Boolean) {
    Column {
        TopAppBar(
            title = {
                Column {
                    Text(
                        "Notifications",
                        style = if (isTablet) MaterialTheme.typography.headlineSmall
                        else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        "Stay updated with your learning community.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppColors.Background
            )
        )
        HorizontalDivider(color = AppColors.Divider, thickness = 0.7.dp)
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    val mockRequests = listOf(
        Connection(connectionId = "1", userA = "user1", userB = "user2", status = "pending"),
        Connection(connectionId = "2", userA = "user3", userB = "user2", status = "pending")
    )
    NotificationContent(
        incomingRequests = mockRequests,
        senderNames = mapOf("user1" to "Aditi Sharma"),
        onAccept = {},
        onDeny = {}
    )
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
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "You're all caught up! New connection\nrequests will show up here.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// ---- Connection Request Card ----
@Composable
fun ConnectionRequestCard(
    request: Connection,
    senderName: String?,
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
                        Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = Color(0xFFB5651D),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "CONNECTION REQUEST",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextSecondary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        senderName?.let { "$it wants to connect" } ?: "Loading request…",
                        style = MaterialTheme.typography.titleSmall,
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
                    border = BorderStroke(1.dp, AppColors.Error)
                ) {
                    Text("Deny", fontWeight = FontWeight.Medium, color = AppColors.Error)
                }
            }
        }
    }
}