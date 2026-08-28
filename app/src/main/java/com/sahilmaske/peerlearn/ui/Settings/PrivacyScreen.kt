package com.sahilmaske.peerlearn.ui.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.ui.theme.AppColors

// Purple icon badge colors — same as AccountScreen
private val IconBg = Color(0xFFEEEDFE)
private val IconTint = Color(0xFF534AB7)

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val isPreview = LocalInspectionMode.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val horizontalPadding: Dp = when {
        screenWidthDp < 600 -> 16.dp
        screenWidthDp < 840 -> 28.dp
        else -> 40.dp
    }

    val currentUserEmail = remember {
        if (isPreview) "sample@email.com" else FirebaseAuth.getInstance().currentUser?.email
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
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
                text = "Privacy",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = AppColors.DarkGreen,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.Background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // privacy content here
            item {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = "Privacy",
                        tint = AppColors.Primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your data, your control",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Manage what others can see and how your information is used across PeerLearn.",
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.Surface)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(IconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "user e-mail",
                                tint = IconTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "HIDE ONLINE STATUS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Verified",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(16.dp)

                        )


                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp),
                        color = AppColors.Divider,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrivacySettingPreview() {
    PrivacyScreen(onBack = {})
}