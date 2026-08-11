package com.sahilmaske.peerlearn.ui.Settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahilmaske.peerlearn.ui.theme.AppColors

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAccountClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // ---- DEVICE COMPATIBILITY: screen width check ----
    // screenWidthDp batata hai device ki width dp me. Chhote phone ~360dp hote hain,
    // tablets 600dp+ hote hain. Isi number se decide karte hain "chhota hai ya bada".
    val screenWidthDp = LocalConfiguration.current.screenWidthDp

    // Simple 3-way check: COMPACT (phone) / MEDIUM (small tablet) / EXPANDED (large tablet)
    // Values (16, 28, 40 dp) manually chuni hain — jitna bada screen utna zyada padding
    // dena taaki content edge se chipka na lage.
    val horizontalPadding: Dp = when {
        screenWidthDp < 600 -> 16.dp
        screenWidthDp < 840 -> 28.dp
        else -> 40.dp
    }

    // Row height bhi thoda badha dete hain bade screens pe — tablet pe chhota row
    // ajeeb/squished lagta hai, thoda comfortable touch target chahiye.
    val rowHeight: Dp = when {
        screenWidthDp < 600 -> 56.dp
        screenWidthDp < 840 -> 60.dp
        else -> 64.dp
    }

    // Title font size bhi thoda scale karte hain
    val titleFontSize = when {
        screenWidthDp < 600 -> 18.sp
        screenWidthDp < 840 -> 20.sp
        else -> 22.sp
    }

    // Content max width: tablet/large screen pe agar content full-width stretch
    // hoga to cards bahut lambi/ajeeb dikhengi. Isliye max width set karke
    // content ko center me rakhte hain (Dp.Unspecified = phone pe koi limit nahi).
    val contentMaxWidth: Dp = when {
        screenWidthDp < 600 -> Dp.Unspecified
        screenWidthDp < 840 -> 560.dp
        else -> 680.dp
    }

    // Icon background color for the new iOS-style rows
    val iconBg = AppColors.PrimaryContainer

    // Box root banaya taaki bade screens pe Column ko horizontally center kar sakein.
    // contentAlignment = TopCenter matlab: content upar se start hoga, aur horizontally center hoga.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    // agar contentMaxWidth set hai (tablet), to width usi tak limit karo
                    // warna (phone pe) poori width le lo
                    if (contentMaxWidth != Dp.Unspecified) Modifier.widthIn(max = contentMaxWidth)
                    else Modifier.fillMaxWidth()
                )
                .padding(16.dp)
        ) {
            // ---- Top Bar ----

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        tint = AppColors.Primary
                    )
                }
                Text(
                    text = "Settings",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = titleFontSize,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
                Spacer(modifier = Modifier.width(28.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)  // FIX: fixed 16.dp -> responsive variable
                    .clip(RoundedCornerShape(14.dp))  // iOS grouped-table-view corner radius (usually 10-14dp)
                    .background(AppColors.Surface)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)  // FIX: fixed 56.dp -> responsive variable
                        .clickable { onAccountClick() }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Account",
                            tint = AppColors.Primary, // Changed to Primary for visibility
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "ACCOUNT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextSecondary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp),
                    color = AppColors.Divider,
                    thickness = 0.5.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                        .clickable { /* TODO: Notifications */ }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "NOTIFICATIONS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextSecondary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp),
                    color = AppColors.Divider,
                    thickness = 0.5.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                        .clickable { /* TODO: Privacy */ }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PrivacyTip,
                            contentDescription = "Privacy",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "PRIVACY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextSecondary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp),
                    color = AppColors.Divider,
                    thickness = 0.5.dp
                )


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                        .clickable { /* TODO: App Appearance */ }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DarkMode,
                            contentDescription = "Dark Mode",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "APP APPEARANCE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextSecondary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp),
                    color = AppColors.Divider,
                    thickness = 0.5.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                        .clickable { /* TODO: Support */ }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Support",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "SUPPORT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextSecondary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp),
                    color = AppColors.Divider,
                    thickness = 0.5.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                        .clickable { onLogoutClick() }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "LOGOUT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE53935),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp),
                    color = AppColors.Divider,
                    thickness = 0.5.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                        .clickable { /* TODO: Delete Account */ }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = "Delete Account",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "DELETE ACCOUNT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE53935),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))

                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SettingsScreenPreviewPhone() {
    SettingsScreen(
        onBack = {},
        onAccountClick = {},
        onLogoutClick = {}
    )
}

@Preview(showBackground = true, widthDp = 1000)
@Composable
fun SettingsScreenPreviewTablet() {
    SettingsScreen(
        onBack = {},
        onAccountClick = {},
        onLogoutClick = {}
    )
}