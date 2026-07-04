package com.sahilmaske.peerlearn.ui.home.HomeScreenComponents



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sahilmaske.peerlearn.model.PeerSuggestion
import com.sahilmaske.peerlearn.ui.theme.AppColors.Icon

@Composable
fun PeerSuggestionCard(
    peer: PeerSuggestion
) {
    Column(
        modifier = Modifier.width(95.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFEDEDED)),
            contentAlignment = Alignment.Center
        ) {
            if (peer.avatarUrl.isNotBlank()) {
                AsyncImage(
                    model = peer.avatarUrl,
                    contentDescription = peer.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }else {
                Text(
                    text = peer.name.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000)
                )
            }

        }
//        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text =peer.name,
            fontSize =14.sp,
            fontWeight = FontWeight.Medium,
            color = androidx.compose.ui.graphics.Color(0xFF000000),

        )
//        Spacer(modifier = Modifier.height(0.dp))
        Text(
            text = "${peer.matchPercentage}% match",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6C63FF)
        )
        OutlinedButton(
            onClick = { /* connect logic */ },
            modifier = Modifier.fillMaxWidth().height(30.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Color(0xFF6C63FF)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6C63FF))

        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }

    }

}

@Preview(showBackground = true)
@Composable
fun PeerSuggestionCardPreview() {
    PeerSuggestionCard(
        peer = PeerSuggestion(
            id = "1",
            name = "John Doe",
            avatarUrl = "",
            skill = "UI/UX • Figma"
        )
    )
}