package com.sahilmaske.peerlearn.ui.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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

// Purple icon badge colors — same language as AccountScreen
private val IconBg = Color(0xFFEEEDFE)
private val IconTint = Color(0xFF534AB7)

@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    onProfileVisibilityClick: () -> Unit = {},
    onBlockedUsersClick: () -> Unit = {},
    onDataUsageClick: () -> Unit = {}
) {

    // abhi ke liye local state, baad mein ViewModel se aayega jaise hideOnlineStatus User.kt mein hai
    var hideOnlineStatus by remember { mutableStateOf(false) }
    var showPhoneToConnections by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Top bar
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
                .background(AppColors.Background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Header section — shield icon + title + subtitle
            item {
                Spacer(modifier = Modifier.height(24.dp))

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

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Card 1: Hide Online Status, Profile Visibility, Phone Number, Blocked Users
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.Surface)
                ) {

                    // Row 1: Hide Online Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { hideOnlineStatus = !hideOnlineStatus }
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
                                Icons.Default.VisibilityOff,
                                contentDescription = "Hide Online Status",
                                tint = IconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "HIDE ONLINE STATUS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 20.dp)
                        )
                        Switch(
                            checked = hideOnlineStatus,
                            onCheckedChange = { hideOnlineStatus = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppColors.Surface,
                                checkedTrackColor = AppColors.Primary
                            )
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp),
                        color = AppColors.Divider,
                        thickness = 0.5.dp
                    )

                    // Row 2: Profile Visibility
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProfileVisibilityClick() }
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
                                Icons.Default.Person,
                                contentDescription = "Profile Visibility",
                                tint = IconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "PROFILE VISIBILITY",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 20.dp)
                        )
                        Text(
                            text = "Everyone",
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = AppColors.Primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp),
                        color = AppColors.Divider,
                        thickness = 0.5.dp
                    )

                    // Row 3: Show Phone Number to Connections
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPhoneToConnections = !showPhoneToConnections }
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
                                Icons.Default.Phone,
                                contentDescription = "Show Phone Number",
                                tint = IconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "SHOW PHONE NUMBER TO CONNECTIONS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 20.dp)
                        )
                        Switch(
                            checked = showPhoneToConnections,
                            onCheckedChange = { showPhoneToConnections = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppColors.Surface,
                                checkedTrackColor = AppColors.Primary
                            )
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp),
                        color = AppColors.Divider,
                        thickness = 0.5.dp
                    )

                    // Row 4: Blocked Users
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBlockedUsersClick() }
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
                                Icons.Default.Block,
                                contentDescription = "Blocked Users",
                                tint = IconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "BLOCKED USERS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 20.dp)
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = AppColors.Primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Card 2: Data Usage
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.Surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDataUsageClick() }
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
                                Icons.Default.Info,
                                contentDescription = "Data Usage",
                                tint = IconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "DATA USAGE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 20.dp)
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = AppColors.Primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrivacySettingPreview() {
    PrivacyScreen(onBack = {})
}