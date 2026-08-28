package com.sahilmaske.peerlearn.ui.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.model.User
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel
import com.sahilmaske.peerlearn.ui.home.ImagePickerDialog

// Purple icon badge colors — same language as SettingsScreen
private val IconBg = Color(0xFFEEEDFE)
private val IconTint = Color(0xFF534AB7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onVerifyEmailClick: () -> Unit,
    onLinkAccountClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val isPreview = LocalInspectionMode.current
    val isEmailVerified = remember(isPreview) {
        if (isPreview) true else FirebaseAuth.getInstance().currentUser?.isEmailVerified ?: false
    }
    val userProfile by viewModel.userProfile.collectAsState()
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }


    val sheetState = rememberModalBottomSheetState()
    var showPhoneSheet by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf(userProfile?.phoneNumber ?: "") }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val horizontalPadding: Dp = when {
        screenWidthDp < 600 -> 16.dp
        screenWidthDp < 840 -> 28.dp
        else -> 40.dp
    }

    val currentUserEmail = remember {
        if (isPreview) "sample@email.com" else FirebaseAuth.getInstance().currentUser?.email
    }

    val avatarSize = 110.dp

    LaunchedEffect(Unit) {
        if (!isPreview) {
            viewModel.loadProfile(null) // apna hi profile load hoga
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
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
                text = "Account",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = AppColors.TextPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Avatar section
        Column(
            modifier = Modifier.padding(21.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                if (userProfile?.avatarUrl.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(AppColors.SecondaryContainer)
                            .border(2.dp, AppColors.Primary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = userProfile?.name
                            ?.split(" ")
                            ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                            ?.take(2)
                            ?.joinToString("") ?: "?"
                        Text(
                            initials,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }
                } else {
                    AsyncImage(
                        model = userProfile?.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .border(2.dp, AppColors.Primary.copy(alpha = 0.3f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Edit badge
//                Box(
//                    modifier = Modifier
//                        .size(32.dp)
//                        .offset(x = 4.dp, y = 4.dp)
//                        .clip(CircleShape)
//                        .background(AppColors.TextWhite)
//                        .padding(2.dp)
//                        .clip(CircleShape)
//                        .background(AppColors.Primary)
//                        .clickable { showImagePickerDialog = true },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        Icons.Default.Edit,
//                        contentDescription = "Edit Profile Image",
//                        tint = AppColors.TextWhite,
//                        modifier = Modifier.size(16.dp)
//                    )
//                }
            }

            Spacer(modifier = Modifier.height(21.dp))

            Text(
                text = userProfile?.name ?: "Your Name",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))


        // Account details card
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
                    text = currentUserEmail ?: "No email found",
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


        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .padding(horizontal = horizontalPadding)
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.Surface)
        ) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 52.dp),
                color = AppColors.Divider,
                thickness = 0.9.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVerifyEmailClick() }
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
                        Icons.Default.PrivacyTip,
                        contentDescription = "Verify Email",
                        tint = IconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = if (isEmailVerified) User.STATUS_VERIFIED else
                        User.STATUS_NOT_VERIFIED,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isEmailVerified)
                        Color(0xFF2E7D32)
                    else Color(0xFFC62828),
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

        }

    } /// new column ends here
        Spacer(modifier = Modifier.height(24.dp))
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
                    .clickable{onChangePasswordClick()}
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically

            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IconBg),
                    contentAlignment = Alignment.Center

                )
                {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Privacy",
                        tint = IconTint,
                        modifier = Modifier.size(24.dp)

                    )
                }

                Text(
                    text = "PASSWORD",
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
                thickness = 0.9.dp
            )



            Column(
                modifier = Modifier
                    .fillMaxWidth()
//                    .padding(horizontal = horizontalPadding)
                    .clickable { showPhoneSheet = true }
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.Surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp) ,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(IconBg),
                        contentAlignment = Alignment.Center

                    )
                    {
                        Icon(
                            Icons.Default.PhoneIphone,
                            contentDescription = "Privacy",
                            tint = IconTint,
                            modifier = Modifier.size(24.dp)

                        )
                    }

                    Text(
                        text = userProfile?.phoneNumber?.takeIf { it.isNotBlank() } ?: "PHONE NUMBER",
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

                    if (showPhoneDialog) {
                        var phoneInput by remember { mutableStateOf(userProfile?.phoneNumber ?: "") }

                        AlertDialog(
                            onDismissRequest = { showPhoneDialog = false },
                            title = {
                                Text("Phone Number", fontWeight = FontWeight.SemiBold)
                            },
                            text = {
                                OutlinedTextField(
                                    value = phoneInput,
                                    onValueChange = { phoneInput = it },
                                    label = { Text("Enter phone number") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.updatePhoneNumber(phoneInput)
                                    showPhoneDialog = false
                                }) {
                                    Text("Save", color = AppColors.Primary, fontWeight = FontWeight.Medium)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showPhoneDialog = false }) {
                                    Text("Cancel", color = AppColors.TextSecondary)
                                }
                            }
                        )
                    }


                }
            }
            if (showPhoneSheet){
                ModalBottomSheet(
                    onDismissRequest = { showPhoneSheet = false },
                    sheetState = sheetState,
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .navigationBarsPadding()
                    ) {
                        Text("Phone Number", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = AppColors.TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Enter phone number") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.updatePhoneNumber(phoneInput)
                                showPhoneSheet = false
                            },
                            modifier  = Modifier.fillMaxWidth()
                        ) {
                            Text("Save")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

        }
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.Surface))
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable{onLinkAccountClick()},
                verticalAlignment = Alignment.CenterVertically

            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IconBg),
                    contentAlignment = Alignment.Center
                ){
                    Icon(
                        Icons.Default.Link,
                        contentDescription = "Privacy",
                        tint = IconTint,
                        modifier = Modifier.size(24.dp)
                            .rotate(-140f)

                    )
                }
                Text(
                    text = "LINKED ACCOUNTS",
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


        }
    }

    ImagePickerDialog(
        showDialog = showImagePickerDialog,
        onDismissRequest = { showImagePickerDialog = false },
        onCameraClick = { showImagePickerDialog = false },
        onGalleryClick = { showImagePickerDialog = false }
    )
}

@Preview(showBackground = true)
@Composable
fun AccountScreenPreview() {
    AccountScreen(onBack = {}, onVerifyEmailClick = {}, onChangePasswordClick = {}, onLinkAccountClick = {})
}