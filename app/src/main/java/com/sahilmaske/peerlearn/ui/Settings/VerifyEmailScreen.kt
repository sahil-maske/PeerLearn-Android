package com.sahilmaske.peerlearn.ui.Settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.ui.theme.AppColors

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

private val IconBg = Color(0xFFEEEDFE)
private val IconTint = Color(0xFF534AB7)
private val VerifiedGreen = Color(0xFF0F6E6E)

@Composable
fun VerifyEmailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val email = currentUser?.email ?: "No email found"

    var isVerified by remember { mutableStateOf(currentUser?.isEmailVerified ?: false) }
    var isSending by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    fun sendVerificationEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            statusMessage = "No user logged in"
            isError = true
            return
        }
        isSending = true
        statusMessage = null
        user.sendEmailVerification()
            .addOnSuccessListener {
                isSending = false
                isError = false
                statusMessage = "Verification link sent! Check your inbox."
            }
            .addOnFailureListener { e ->
                isSending = false
                isError = true
                statusMessage = e.message ?: "Failed to send verification email"
            }
    }

    fun refreshVerificationStatus() {
        FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener {
            isVerified = FirebaseAuth.getInstance().currentUser?.isEmailVerified ?: false
        }
    }

    // Re-check status when user comes back to the app (after clicking link in email)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshVerificationStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    VerifyEmailContent(
        onBack = onBack,
        email = email,
        isVerified = isVerified,
        isSending = isSending,
        statusMessage = statusMessage,
        isError = isError,
        onSendClick = { sendVerificationEmail() },
        onRefreshClick = { refreshVerificationStatus() },
        modifier = modifier
    )
}

@Composable
fun VerifyEmailContent(
    onBack: () -> Unit,
    email: String,
    isVerified: Boolean,
    isSending: Boolean,
    statusMessage: String?,
    isError: Boolean,
    onSendClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(21.dp),
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
                text = "Verify Email",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = AppColors.TextPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(110.dp))

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) innerColumn@{

            // Icon circle — animated swap between email icon and checkmark
            Box(contentAlignment = Alignment.Center) {

                // Email icon — shown when NOT verified
                this@innerColumn.AnimatedVisibility(
                    visible = !isVerified,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(IconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = IconTint,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Checkmark icon — shown when verified (bouncy pop-in)
                this@innerColumn.AnimatedVisibility(
                    visible = isVerified,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(VerifiedGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VerifiedGreen,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isVerified) "Email Verified" else "Verify your email",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isVerified)
                    "Your email address is confirmed."
                else
                    "We'll send a verification link to:",
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (!isVerified) {
                Button(
                    onClick = onSendClick,
                    enabled = !isSending,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(48.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AppColors.TextWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Send verification link",
                            color = AppColors.TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                TextButton(onClick = onRefreshClick) {
                    Text(
                        "I already verified — refresh status",
                        color = AppColors.Primary,
                        fontSize = 13.sp
                    )
                }
            }

            statusMessage?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    color = if (isError) Color(0xFFB00020) else VerifiedGreen,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VerifyEmailScreenPreview() {
    VerifyEmailContent(
        onBack = {},
        email = "user@example.com",
        isVerified = false,
        isSending = false,
        statusMessage = "Verification link sent! Check your inbox.",
        isError = false,
        onSendClick = {},
        onRefreshClick = {}
    )
}