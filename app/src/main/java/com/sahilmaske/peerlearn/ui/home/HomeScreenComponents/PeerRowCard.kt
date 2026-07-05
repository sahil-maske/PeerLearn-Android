package com.sahilmaske.peerlearn.ui.home.HomeScreenComponents

import androidx.compose.foundation.background
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
import coil.compose.AsyncImage
import com.sahilmaske.peerlearn.model.PeerSuggestion

@Composable
fun PeerRowCard (
    peer: PeerSuggestion,
){

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFFFFFFFF),
                shape = RoundedCornerShape(16.dp)),
        verticalAlignment = Alignment.CenterVertically

    ){
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0F0F5)),
            contentAlignment = Alignment.Center
        ) {
            if (peer.avatarUrl.isNotBlank()) {
                AsyncImage(
                    model = peer.avatarUrl,
                    contentDescription = peer.name,
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = peer.name.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6C63FF)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = peer.name,
                fontSize = 21.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF000000),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text ="KnowSkill : ${peer.knowSkill}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "LearnSkill : ${peer.learnSkill}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
        Column(
            modifier = Modifier
                .padding(start = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = "Message",
                tint = Color(0xFF6C63FF),
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { /* connect logic */ },
                modifier = Modifier
                    .height(32.dp),
                shape = RoundedCornerShape(50),

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
fun PostRowCard (){
    PeerRowCard(
        peer = PeerSuggestion(
            id = "1",
            name = "John Doe",
            avatarUrl = "",
            knowSkill = "UI/UX • Figma",
            learnSkill = "Firebase • SQL",
            matchPercentage = 92
        )
    )

}