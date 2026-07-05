package com.sahilmaske.peerlearn.ui.home.HomeScreenComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.sahilmaske.peerlearn.model.PeerSuggestion

@Composable
fun PeerRowCard(
    peer: PeerSuggestion,
    navController: NavController
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)   // card ke bahar ka margin
            .clip(RoundedCornerShape(18.dp))                // pehle shape define
            .background(Color(0xFFFFFFFF))                  // fir background
            .clickable {
                navController.navigate("profile/${peer.uid}")
            }
            .padding(14.dp),                                // sabse last — content padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ---- Avatar ----
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFF2F1FA)),
            contentAlignment = Alignment.Center
        ) {
            if (peer.avatarUrl.isNotBlank()) {
                AsyncImage(
                    model = peer.avatarUrl,
                    contentDescription = peer.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = peer.name.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6C63FF)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // ---- Name + Skills ----
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = peer.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Knows • ${peer.knowSkill}",
                fontSize = 12.5.sp,
                color = Color(0xFF8A8A8E),
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Learning • ${peer.learnSkill}",
                fontSize = 12.5.sp,
                color = Color(0xFF8A8A8E),
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // ---- Actions ----
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2F1FA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Message",
                    tint = Color(0xFF6C63FF),
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { /* connect logic */ },
                modifier = Modifier.height(30.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6C63FF)
                )
            ) {
                Text(
                    text = "Connect",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostRowCard() {
    val navController = rememberNavController()
    PeerRowCard(
        peer = PeerSuggestion(
            id = "1",
            name = "John Doe",
            avatarUrl = "",
            knowSkill = "UI/UX • Figma",
            learnSkill = "Firebase • SQL",
            matchPercentage = 92
        ),
        navController = navController
    )
}