package com.sahilmaske.peerlearn.ui.home.HomeScreenComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.sahilmaske.peerlearn.model.PeerSuggestion

private val TealPrimary = Color(0xFF0F6E6E)
private val TealChipBg = Color(0xFFB9E8E4)
private val TealChipText = Color(0xFF0F6E6E)
private val PeachChipBg = Color(0xFFF8D9AE)
private val PeachChipText = Color(0xFF8A5A1E)
private val DashedBorderColor = Color(0xFF6C7BFF)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PeerSuggestionCard(
    peer: PeerSuggestion,
    onPeerClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(2.dp)
            .width(180.dp)
            .background(Color(0xFFFFFFFF), shape = RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // ---- Avatar + name + institution ----
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable {
                        onPeerClick(peer.uid)   // ← poora card click karega, sirf avatar nahi
                    }
                    .background(Color(0xFFF0F0F5)),
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
                } else {
                    Text(
                        text = peer.name.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = peer.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // NOTE: PeerSuggestion needs an `institution` field for this —
                // add it to the model + Firestore doc. Falls back to nothing if blank.
                if (peer.institution.isNotBlank()) {
                    Text(
                        text = peer.institution,
                        fontSize = 12.sp,
                        color = Color(0xFF6B6B6B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ---- Can Teach ----
        if (peer.knowSkill.isNotBlank()) {
            Text(
                text = "Can Teach",
                fontSize = 12.sp,
                color = Color(0xFF6B6B6B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                peer.knowSkill.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { skill ->
                    SkillChip(text = skill, background = TealChipBg, textColor = TealChipText)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ---- Wants to Learn ----
        if (peer.learnSkill.isNotBlank()) {
            Text(
                text = "Wants to Learn",
                fontSize = 12.sp,
                color = Color(0xFF6B6B6B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                peer.learnSkill.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { skill ->
                    SkillChip(text = skill, background = PeachChipBg, textColor = PeachChipText)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // ---- Connect button (teal fill + dashed outline) ----
        DashedConnectButton(
            onClick = {
                val db = FirebaseFirestore.getInstance()
                db.collection("testCollection")
                    .document("testDoc")
                    .set(mapOf("message" to "Hello Firebase"))
            }
        )
    }
}

@Composable
private fun SkillChip(text: String, background: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(background, shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun DashedConnectButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(TealPrimary, shape = RoundedCornerShape(50))
            .dashedBorder(color = DashedBorderColor, strokeWidth = 1.5.dp, cornerRadius = 50.dp)
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Connect", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// Dashed border drawn manually since Compose has no built-in dashed BorderStroke
private fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp
): Modifier = this.drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    )
}

// Simple no-ripple clickable so the dashed border isn't obscured by a ripple overlay
@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.then(
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PeerSuggestionCardPreview() {
    PeerSuggestionCard(
        peer = PeerSuggestion(
            id = "1",
            name = "Jordan Smith",
            avatarUrl = "",
            institution = "Stanford University",
            knowSkill = "Python, UI Design",
            learnSkill = "Guitar",
            matchPercentage = 92
        ),
        onPeerClick = {}
    )
}