package com.sahilmaske.peerlearn.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel

@Composable
fun ProfileInfo(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
){
    val userProfile by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(16.dp)
            .clip(RoundedCornerShape(14.dp))
            .verticalScroll(scrollState)
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .background(color = AppColors.Surface, shape = RoundedCornerShape(16.dp))
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Back",
                    tint = AppColors.Primary
                )
            }
            Text(
                text = "Profile Information",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = AppColors.DarkGreen,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Info Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.Surface)
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColors.PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = AppColors.Primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your stored data",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This is the profile information PeerLearn stores and uses for matching.",
                fontSize = 13.sp,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Data List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.Surface)
        ) {
            ProfileInfoItem(
                icon = Icons.Default.Person,
                label = "Name",
                value = userProfile?.name ?: "Not set"
            )
            ProfileInfoItem(
                icon = Icons.Default.Email,
                label = "Email",
                value = userProfile?.email ?: "Not set"
            )
            ProfileInfoItem(
                icon = Icons.Default.Phone,
                label = "Phone Number",
                value = userProfile?.phoneNumber?.takeIf { it.isNotEmpty() } ?: "Not set"
            )
            ProfileInfoItem(
                icon = Icons.Default.School,
                label = "College",
                value = userProfile?.college ?: "Not set"
            )

            ProfileInfoItem(
                icon = Icons.Default.EmojiObjects,
                label = "Known Skills",
                value = userProfile?.knownSkills?.joinToString(", ")?.takeIf { it.isNotEmpty() } ?: "Not set"
            )
            ProfileInfoItem(
                icon = Icons.Default.TrendingUp,
                label = "Want Skills",
                value = userProfile?.learningSkills?.joinToString(", ")?.takeIf { it.isNotEmpty() } ?: "Not set",
                showDivider = false
            )
            ProfileInfoItem(
                icon = Icons.AutoMirrored.Filled.Notes,
                label = "Bio",
                value = userProfile?.about?.takeIf { it.isNotEmpty() } ?: "Not set",
                showDivider = false
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit = {},
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AppColors.Primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = AppColors.TextPrimary,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextSecondary
            )
        }
    }
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = AppColors.Divider.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSkills(){
    ProfileInfo(
        onBack = {}
    )
}
