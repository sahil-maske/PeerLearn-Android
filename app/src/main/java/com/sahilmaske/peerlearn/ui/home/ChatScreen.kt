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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sahilmaske.peerlearn.model.Conversation
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    val conversations by viewModel.conversations.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .statusBarsPadding()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // ---- Top Bar ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
        Row(
            modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE9E7E0))
            .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = Color(0xFF6B6B6B)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Search conversations...",
                color = Color(0xFF6B6B6B),
                fontSize = 15.sp
            )
        }
        Text(
            "Messages",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF000000)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(conversations){ convo ->
                ConversationItem(convo)
            }
        }
    }
}
@Composable
fun ConversationItem(convo: Conversation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = convo.avatarUrl,
                contentDescription = convo.name,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    convo.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    convo.lastMessage,
                    color = Color(0xFF6B6B6B),
                    fontSize = 13.sp,
                    fontWeight = if (convo.hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    convo.time,
                    fontSize = 12.sp,
                    color = if (convo.hasUnread) Color(0xFF0F6E6E) else Color(0xFF6B6B6B)
                )
                Spacer(Modifier.height(6.dp))
                if (convo.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F6E6E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            convo.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    ChatScreen(navController = navController)
}

