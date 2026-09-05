package com.sahilmaske.peerlearn.ui.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.AccountViewModel
import com.sahilmaske.peerlearn.viewmodel.DeletionState

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAccountClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSupportClick: () -> Unit,
    onToSClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onDeleteSuccess: () -> Unit,
    viewModel: AccountViewModel = viewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val deletionState by viewModel.deletionState.collectAsState()

    LaunchedEffect(deletionState) {
        if (deletionState is DeletionState.Success) {
            onDeleteSuccess()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("This will permanently delete your account and all associated data. This action cannot be undone.")
                    if (deletionState is DeletionState.RequiresRecentLogin) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "For security reasons, you must have logged in recently to perform this action. Please log out and log back in, then try again.",
                            color = AppColors.Error,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (deletionState is DeletionState.Error) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            (deletionState as DeletionState.Error).message,
                            color = AppColors.Error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteAccount() },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Error)
                ) {
                    if (deletionState is DeletionState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = AppColors.Error)
                    } else {
                        Text("Delete Permanently")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    viewModel.resetDeletionState()
                }) {
                    Text("Cancel", color = AppColors.TextSecondary)
                }
            }
        )
    }

    // ---- DEVICE COMPATIBILITY: screen width check ----
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val horizontalPadding: Dp = when {
        screenWidthDp < 600 -> 16.dp
        screenWidthDp < 840 -> 28.dp
        else -> 40.dp
    }
    val rowHeight: Dp = when {
        screenWidthDp < 600 -> 56.dp
        screenWidthDp < 840 -> 60.dp
        else -> 64.dp
    }
    val titleFontSize = when {
        screenWidthDp < 600 -> 18.sp
        screenWidthDp < 840 -> 20.sp
        else -> 22.sp
    }
    val contentMaxWidth: Dp = when {
        screenWidthDp < 600 -> Dp.Unspecified
        screenWidthDp < 840 -> 560.dp
        else -> 680.dp
    }

    val iconBg = AppColors.PrimaryContainer

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (contentMaxWidth != Dp.Unspecified) Modifier.widthIn(max = contentMaxWidth)
                    else Modifier.fillMaxWidth()
                )
                .padding(12.dp)
        ) {
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
            Spacer(modifier = Modifier.height(48.dp))

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
                        .height(rowHeight)
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
                            tint = AppColors.Primary,
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
                        .clickable { onPrivacyClick() }
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
                        .clickable { onSupportClick() }
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
                        .clickable { onToSClick() }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = "Terms",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "TERMS OF SERVICE",
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
                        .clickable { onPrivacyPolicyClick() }
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
                            contentDescription = "Privacy Policy",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "PRIVACY POLICY",
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
                        .clickable { showDeleteDialog = true }
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
        onPrivacyClick = {},
        onLogoutClick = {},
        onSupportClick = {},
        onToSClick = {},
        onPrivacyPolicyClick = {},
        onDeleteSuccess = {}
    )
}
