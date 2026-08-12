package com.sahilmaske.peerlearn.ui.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.AccountViewModel
import com.sahilmaske.peerlearn.viewmodel.VerificationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = viewModel()
) {
    val passwordResetState by viewModel.passwordResetState.collectAsState()
    val email by viewModel.email.collectAsState()

    ChangePasswordContent(
        email = email,
        passwordResetState = passwordResetState,
        onBack = onBack,
        onSendResetLink = { viewModel.sendPasswordResetEmail() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordContent(
    email: String,
    passwordResetState: VerificationState,
    onBack: () -> Unit,
    onSendResetLink: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Change Password",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            tint = AppColors.Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier.size(88.dp)
                .clip(CircleShape)
                    .background(Color(0xFFEEEDFE)),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Lock Icon",
                    tint = AppColors.Primary,
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Secure Your Account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "We will send a password reset link to your registered email address:\n$email",
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                color = AppColors.TextSecondary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (passwordResetState) {
                is VerificationState.Sent -> {
                    Text(
                        text = "Reset email sent successfully! Please check your inbox.",
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
                is VerificationState.Error -> {
                    Text(
                        text = passwordResetState.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onSendResetLink,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                enabled = passwordResetState !is VerificationState.Loading
            ) {
                if (passwordResetState is VerificationState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Send Reset Link", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePassScreenPreview() {
    ChangePasswordContent(
        email = "user@example.com",
        passwordResetState = VerificationState.Idle,
        onBack = {},
        onSendResetLink = {}
    )
}