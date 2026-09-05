package com.sahilmaske.peerlearn.ui.Settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.SupportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBack: () -> Unit,
    onReportProblemClick: () -> Unit,
    onReportUserClick: () -> Unit,
    onToSClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    val context = LocalContext.current
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Support",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = titleFontSize,
                        color = AppColors.TextPrimary
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
                    containerColor = AppColors.Background
                )
            )
        },
        containerColor = AppColors.Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "FAQ / Help Center",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                FAQSection()
            }

            item {
                Text(
                    text = "Contact & Reports",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.Surface)
                ) {
                    SupportRow(
                        icon = Icons.Default.Email,
                        title = "Contact Us",
                        iconBg = AppColors.PrimaryContainer,
                        iconTint = AppColors.Primary,
                        rowHeight = rowHeight,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@peerlearn.com")
                                putExtra(Intent.EXTRA_SUBJECT, "PeerLearn Support Request")
                            }
                            context.startActivity(intent)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = AppColors.Divider)
                    SupportRow(
                        icon = Icons.Default.ReportProblem,
                        title = "Report a Problem",
                        iconBg = AppColors.SecondaryContainer,
                        iconTint = AppColors.Secondary,
                        rowHeight = rowHeight,
                        onClick = onReportProblemClick
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = AppColors.Divider)
                    SupportRow(
                        icon = Icons.Default.PersonOff,
                        title = "Report a User",
                        iconBg = AppColors.TertiaryContainer,
                        iconTint = Color.White,
                        rowHeight = rowHeight,
                        onClick = onReportUserClick
                    )
                }
            }

            item {
                Text(
                    text = "Legal",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.Surface)
                ) {
                    SupportRow(
                        icon = Icons.Default.Description,
                        title = "Terms of Service",
                        iconBg = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF1976D2),
                        rowHeight = rowHeight,
                        onClick = onToSClick
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = AppColors.Divider)
                    SupportRow(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy",
                        iconBg = Color(0xFFE8F5E9),
                        iconTint = Color(0xFF388E3C),
                        rowHeight = rowHeight,
                        onClick = onPrivacyClick
                    )
                }
            }



            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "PeerLearn v1.0.0",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SupportRow(
    icon: ImageVector,
    title: String,
    iconBg: Color,
    iconTint: Color,
    rowHeight: Dp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun FAQSection() {
    val faqs = listOf(
        "How do I create a new post?" to "Tap the '+' button on the home screen to start sharing your knowledge.",
        "How do I link my accounts?" to "Go to Settings > Account > Link Accounts to connect your social profiles.",
        "Is my data secure?" to "Yes, we use industry-standard encryption and Firebase security rules to protect your data.",
        "How can I report a bug?" to "Use the 'Report a Problem' option below to send us the details."
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.Surface)
    ) {
        faqs.forEachIndexed { index, pair ->
            FAQItem(question = pair.first, answer = pair.second)
            if (index < faqs.size - 1) {
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = AppColors.Divider)
            }
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = question,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.rotate(rotation),
                tint = AppColors.TextSecondary
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                text = answer,
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportProblemScreen(
    onBack: () -> Unit,
    viewModel: SupportViewModel = viewModel()
) {
    var description by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            onBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Report a Problem", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, "Back", tint = AppColors.Primary)
                    }
                }
            )
        },
        containerColor = AppColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Describe the issue") },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                shape = RoundedCornerShape(12.dp)
            )
            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = Color.Red, fontSize = 12.sp)
            }
            Button(
                onClick = { viewModel.reportProblem(description) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                enabled = !uiState.isSubmitting
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Report", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportUserScreen(
    onBack: () -> Unit,
    viewModel: SupportViewModel = viewModel()
) {
    var reportedUserId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            onBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Report a User", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, "Back", tint = AppColors.Primary)
                    }
                }
            )
        },
        containerColor = AppColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = reportedUserId,
                onValueChange = { reportedUserId = it },
                label = { Text("User ID or Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Reason for reporting") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(12.dp)
            )
            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = Color.Red, fontSize = 12.sp)
            }
            Button(
                onClick = { viewModel.reportUser(reportedUserId, description) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                enabled = !uiState.isSubmitting
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Report", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
