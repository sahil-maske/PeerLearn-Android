package com.sahilmaske.peerlearn.ui.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahilmaske.peerlearn.ui.theme.AppColors

// Purple icon badge colors — same as AccountScreen
private val IconBg = Color(0xFFEEEDFE)
private val IconTint = Color(0xFF534AB7)

@Composable
fun LinkAccounts(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(28.dp),
        horizontalAlignment = Alignment.Start
    ) {

        // ---------- Top bar ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Back",
                    tint = AppColors.Primary
                )
            }
            Text(
                text = "Linked Accounts",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = AppColors.TextPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Connect your social profiles",
            fontSize = 14.sp,
            color = AppColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ---------- Card jisme sab rows hai ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
        ) {

            // ---------- Row 1: Instagram ----------
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
                    // TODO: yaha Instagram ka icon daal
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = IconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Instagram",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextPrimary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                )

                Text(
                    text = "@user_handle",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = IconTint
                )

                Spacer(modifier = Modifier.width(8.dp))

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

            // ---------- Row 2: LinkedIn ----------
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
                        Icons.Default.Work,
                        contentDescription = "LinkedIn",
                        tint = IconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "LinkedIn",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextPrimary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                )

                Text(
                    text = "Connect",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = IconTint
                )

                Spacer(modifier = Modifier.width(8.dp))

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

            // ---------- Row 3: GitHub ----------
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
                        Icons.Default.Code,
                        contentDescription = "GitHub",
                        tint = IconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "GitHub",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextPrimary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                )

                Text(
                    text = "@dev_name",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = IconTint
                )

                Spacer(modifier = Modifier.width(8.dp))

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

            // ---------- Row 4: Twitter/X ----------
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
                        Icons.Default.AlternateEmail,
                        contentDescription = "Twitter/X",
                        tint = IconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Twitter/X",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextPrimary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                )

                Text(
                    text = "Connect",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = IconTint
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(14.dp)
                )
            }
            // Last row ke baad divider nahi (jaisa AccountScreen me tha)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LinkAccountsPreview() {
    LinkAccounts(onBack = {})
}