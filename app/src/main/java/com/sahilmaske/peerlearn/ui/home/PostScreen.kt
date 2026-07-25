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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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


            var selectedIntent by remember { mutableStateOf("teach") }


            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.Surface)
                    .padding(4.dp)
            ) {

                val itemWidth = maxWidth / 2


                val indicatorOffset by animateDpAsState(
                    targetValue = if (selectedIntent == "teach") 0.dp else itemWidth,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "indicatorOffset"
                )

               
                Box(modifier = Modifier.matchParentSize()) {
                    Box(
                        modifier = Modifier
                            .offset(x = indicatorOffset)
                            .width(itemWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.Primary)
                    )
                }

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
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "What's the skill?",
                fontWeight = FontWeight.Bold,
                fontSize = 15.5.sp,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            var skill by remember { mutableStateOf("") }

            OutlinedTextField(
                value = skill,
                onValueChange = { skill = it },
                placeholder = {
                    Text(
                        text = "e.g. Urban Gardening, UI/UX Design",
                        color = AppColors.TextSecondary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppColors.Border,
                    focusedBorderColor = AppColors.DarkGreen,
                    unfocusedContainerColor = AppColors.Surface,
                    focusedContainerColor = AppColors.Surface
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Description/Details",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            var description by remember { mutableStateOf("") }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = {
                    Text(
                        text = if (selectedIntent == "teach")
                            "Describe what you can teach and your experience..."
                        else
                            "Describe what you want to learn and your current level...",
                        color = AppColors.TextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppColors.Border,
                    focusedBorderColor = AppColors.DarkGreen,
                    unfocusedContainerColor = AppColors.Surface,
                    focusedContainerColor = AppColors.Surface
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Add Tags",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            var tags by remember { mutableStateOf(listOf("Beginner Friendly", "In-person")) }
            var newTag by remember { mutableStateOf("") }
            var showTagInput by remember { mutableStateOf(false) }

// "+ Add Tag" chip
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.Surface)
                    .clickable { showTagInput = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Tag",
                    tint = AppColors.TextPrimary,
                    modifier = Modifier.height(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add Tag",
                    fontSize = 13.sp,
                    color = AppColors.TextPrimary
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostScreenPreview() {
    PostScreen()
}