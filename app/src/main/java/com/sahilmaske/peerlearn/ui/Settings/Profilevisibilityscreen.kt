package com.sahilmaske.peerlearn.ui.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.ui.theme.PeerLearnTheme
import com.sahilmaske.peerlearn.viewmodel.PrivacyViewModel

@Composable
fun ProfileVisibilityScreen(
    onBack: () -> Unit,
    viewModel: PrivacyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileVisibilityContent(
        profileVisibility = uiState.profileVisibility,
        onBack = onBack,
        onVisibilityChange = { viewModel.updateProfileVisibility(it) }
    )
}

@Composable
fun ProfileVisibilityContent(
    profileVisibility: String,
    onBack: () -> Unit,
    onVisibilityChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(28.dp)
    ) {

        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(
                    AppColors.Surface,
                    RoundedCornerShape(16.dp)
                )
                .padding(vertical = 4.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Back",
                    tint = AppColors.Primary
                )
            }

            Text(
                text = "Profile Visibility",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.DarkGreen
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Options
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.Surface)
        ) {

            // Everyone
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onVisibilityChange("Everyone")
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Everyone",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Anyone on PeerLearn can view your linked accounts.",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                }

                if (profileVisibility == "Everyone") {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = AppColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                color = AppColors.Divider
            )

            // Connections Only
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onVisibilityChange("Connections Only")
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Connections Only",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Only people you're connected with can view your linked accounts.",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                }

                if (profileVisibility == "Connections Only") {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = AppColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileVisibilityPreview() {
    PeerLearnTheme {
        ProfileVisibilityContent(
            profileVisibility = "Everyone",
            onBack = {},
            onVisibilityChange = {}
        )
    }
}

