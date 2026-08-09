package com.sahilmaske.peerlearn.ui.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahilmaske.peerlearn.ui.theme.AppColors

// ---------- Screen size categories (same pattern as ProfileScreen/ChatConversationScreen) ----------
enum class SettingsScreenSize { COMPACT, MEDIUM, EXPANDED }

@Composable
private fun rememberSettingsScreenSize(): SettingsScreenSize {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp < 600 -> SettingsScreenSize.COMPACT
        widthDp < 840 -> SettingsScreenSize.MEDIUM
        else -> SettingsScreenSize.EXPANDED
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    // NEW: every row now has its own callback instead of sharing one onClick.
    // Each defaults to {} so the screen still compiles/builds fine even before
    // you wire the actual logic behind a given row — wire them one at a time.
    onAccountClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onAppearanceClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {}
) {
    val screenSize = rememberSettingsScreenSize()

    // Content max-width so the settings card doesn't stretch edge-to-edge on tablets
    val contentMaxWidth: Dp = when (screenSize) {
        SettingsScreenSize.COMPACT -> Dp.Unspecified
        SettingsScreenSize.MEDIUM -> 560.dp
        SettingsScreenSize.EXPANDED -> 680.dp
    }
    val horizontalPadding = when (screenSize) {
        SettingsScreenSize.COMPACT -> 16.dp
        SettingsScreenSize.MEDIUM -> 28.dp
        SettingsScreenSize.EXPANDED -> 40.dp
    }
    val rowHeight = when (screenSize) {
        SettingsScreenSize.COMPACT -> 56.dp
        SettingsScreenSize.MEDIUM -> 60.dp
        SettingsScreenSize.EXPANDED -> 64.dp
    }
    val titleFontSize = when (screenSize) {
        SettingsScreenSize.COMPACT -> 18.sp
        SettingsScreenSize.MEDIUM -> 20.sp
        SettingsScreenSize.EXPANDED -> 22.sp
    }

    // Root Box centers the content column on wide screens; on phones it just fills the width.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (contentMaxWidth != Dp.Unspecified) Modifier.widthIn(max = contentMaxWidth)
                    else Modifier.fillMaxWidth()
                )
                .padding(16.dp)
        ) {

            // ---- Top Bar ----
            // FIX: previously used a hardcoded Spacer(95.dp) to fake-center the title,
            // which breaks on any screen width other than the one it was eyeballed for.
            // A Box with the back button pinned to the start and the title centered
            // via Modifier.align stays correctly centered on every device size.
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Grouped settings card ----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.Surface)
            ) {
                SettingsRow(
                    icon = Icons.Default.AccountCircle,
                    label = "ACCOUNT",
                    height = rowHeight,
                    onClick = onAccountClick
                )
                SettingsDivider()

                SettingsRow(
                    icon = Icons.Default.Notifications,
                    label = "NOTIFICATIONS",
                    height = rowHeight,
                    onClick = onNotificationsClick
                )
                SettingsDivider()

                SettingsRow(
                    icon = Icons.Default.PrivacyTip,
                    label = "PRIVACY",
                    height = rowHeight,
                    onClick = onPrivacyClick
                )
                SettingsDivider()

                SettingsRow(
                    icon = Icons.Default.DarkMode,
                    label = "APP APPEARANCE",
                    height = rowHeight,
                    onClick = onAppearanceClick
                )
                SettingsDivider()

                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    label = "SUPPORT",
                    height = rowHeight,
                    onClick = onSupportClick
                )
                SettingsDivider()

                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    label = "LOGOUT",
                    height = rowHeight,
                    tint = Color(0xFFE53935),
                    onClick = onLogoutClick
                )
                SettingsDivider()

                SettingsRow(
                    icon = Icons.Default.DeleteForever,
                    label = "DELETE ACCOUNT",
                    height = rowHeight,
                    tint = Color(0xFFE53935),
                    onClick = onDeleteAccountClick
                )
            }
        }
    }
}

// ---- Reusable row (avoids repeating the same Row/Icon/Text block 7 times) ----
@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    height: Dp,
    onClick: () -> Unit,
    tint: Color = AppColors.TextSecondary
) {
    // NEW: explicit ripple + interaction source so every row gives clear tap feedback
    // (press ripple), instead of relying on the default indication only.
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AppColors.Primary),
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = tint,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp),
        color = AppColors.Divider,
        thickness = 0.5.dp
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SettingsScreenPreviewPhone() {
    SettingsScreen(onBack = {})
}

@Preview(showBackground = true, widthDp = 700)
@Composable
fun SettingsScreenPreviewMedium() {
    SettingsScreen(onBack = {})
}

@Preview(showBackground = true, widthDp = 1000)
@Composable
fun SettingsScreenPreviewExpanded() {
    SettingsScreen(onBack = {})
}