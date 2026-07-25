package com.sahilmaske.peerlearn.ui.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahilmaske.peerlearn.ui.theme.AppColors

@Composable
fun PostScreen() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ==================== TOP BAR ====================
        // Close icon (left) + Title (center) + School icon (right)
        // Box + contentAlignment(Center) se Text automatically beech mein aa jaata hai,
        // aur icons ko align(CenterStart)/align(CenterEnd) se force kiya hai apni jagah.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = AppColors.Surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = AppColors.Icon
                )
            }

            Text(
                text = "Create Post",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = {},
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "School",
                    tint = AppColors.Icon
                )
            }
        }

        // ==================== BODY ====================
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Share your expertise",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fill out the details below to find your next learning partner.",
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Intent",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))

            // ==================== INTENT TOGGLE ====================
            // selectedIntent state track karta hai ki abhi "teach" selected hai ya "learn".
            // Jab bhi ye value change hoti hai, Compose apne aap re-render karta hai
            // aur indicatorOffset animate hoke naye position pe slide ho jaata hai.
            var selectedIntent by remember { mutableStateOf("teach") }

            // BoxWithConstraints use kiya hai kyunki isse "maxWidth" mil jaati hai —
            // yani container ki actual available width, jisse hum itemWidth (aadha) nikaal sakein.
            //
            // IMPORTANT: .height(IntrinsicSize.Min) zaroori hai warna neeche wale
            // .fillMaxHeight() ko koi reference height nahi milegi (0 ho sakti hai).
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.Surface)
                    .padding(4.dp)
            ) {
                // Container ki aadhi width — yahi ek single toggle option ki width hai
                val itemWidth = maxWidth / 2

                // Indicator ka horizontal position animate hota hai:
                // "teach" selected -> offset 0 (left), "learn" selected -> offset itemWidth (right)
                // spring() bouncy/natural feel deta hai, tween() se zyada smooth lagta hai
                val indicatorOffset by animateDpAsState(
                    targetValue = if (selectedIntent == "teach") 0.dp else itemWidth,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "indicatorOffset"
                )

                // ---- LAYER 1 (peeche): Sliding teal indicator ----
                // matchParentSize() wrapper Box use kiya hai jisse indicator
                // background correctly fill ho sake bina Intrinsic measurement crash ke.
                Box(modifier = Modifier.matchParentSize()) {
                    Box(
                        modifier = Modifier
                            .offset(x = indicatorOffset)
                            .width(itemWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.DarkGreen)
                    )
                }

                // ---- LAYER 2 (upar): Clickable text labels ----
                // Ye transparent hai, sirf click-detection aur text dikhane ke liye hai.
                // Indicator peeche slide hota rehta hai, ye upar static rehta hai.
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedIntent = "teach" }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "I want to teach",
                            color = if (selectedIntent == "teach") Color.White else AppColors.TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedIntent = "learn" }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "I want to learn",
                            color = if (selectedIntent == "learn") Color.White else AppColors.TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostScreenPreview() {
    PostScreen()
}