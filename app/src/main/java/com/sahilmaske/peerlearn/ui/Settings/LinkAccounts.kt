package com.sahilmaske.peerlearn.ui.Settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sahilmaske.peerlearn.ui.theme.AppColors

// Purple icon badge colors — same as AccountScreen
private val IconBg = Color(0xFFEEEDFE)
private val IconTint = Color(0xFF534AB7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkAccounts(onBack: () -> Unit) {

    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val db = remember { if (isPreview) null else FirebaseFirestore.getInstance() }
    val uid = remember { if (isPreview) "preview_uid" else FirebaseAuth.getInstance().currentUser?.uid }

    // ---------- Instagram state ----------
    var instagramLink by remember { mutableStateOf<String?>(null) }
    var showInstagramSheet by remember { mutableStateOf(false) }
    var instagramInput by remember { mutableStateOf("") }
    var instagramError by remember { mutableStateOf(false) }
    val instagramSheetState = rememberModalBottomSheetState()

    // ---------- LinkedIn state ----------
    var linkedInLink by remember { mutableStateOf<String?>(null) }
    var showLinkedInSheet by remember { mutableStateOf(false) }
    var linkedInInput by remember { mutableStateOf("") }
    var linkedInError by remember { mutableStateOf(false) }
    val linkedInSheetState = rememberModalBottomSheetState()

    // ---------- GitHub state ----------
    var githubLink by remember { mutableStateOf<String?>(null) }
    var showGithubSheet by remember { mutableStateOf(false) }
    var githubInput by remember { mutableStateOf("") }
    var githubError by remember { mutableStateOf(false) }
    val githubSheetState = rememberModalBottomSheetState()

    // ---------- Twitter/X state ----------
    var twitterLink by remember { mutableStateOf<String?>(null) }
    var showTwitterSheet by remember { mutableStateOf(false) }
    var twitterInput by remember { mutableStateOf("") }
    var twitterError by remember { mutableStateOf(false) }
    val twitterSheetState = rememberModalBottomSheetState()

    // NEW: screen khulte hi Firestore se linkedAccounts map load karo
    LaunchedEffect(uid) {
        if (uid == null || db == null) return@LaunchedEffect
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val map = doc.get("linkedAccounts") as? Map<*, *> ?: return@addOnSuccessListener
                instagramLink = map["instagram"] as? String
                linkedInLink = map["linkedin"] as? String
                githubLink = map["github"] as? String
                twitterLink = map["twitter"] as? String
            }
    }

    // NEW: ek link save karne ka common function — Firestore map field mein merge karta hai
    fun saveLink(platform: String, link: String) {
        if (uid == null || db == null) return
        db.collection("users").document(uid)
            .set(mapOf("linkedAccounts" to mapOf(platform to link)), SetOptions.merge())
    }

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
                    .clickable {
                        if (instagramLink != null) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(instagramLink)))
                        } else {
                            showInstagramSheet = true
                        }
                    }
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
                        Icons.Default.CameraAlt,
                        contentDescription = "Instagram",
                        tint = IconTint,
                        modifier = Modifier.size(20.dp)
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
                    text = if (instagramLink != null) "Connected" else "Connect",
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
                    .clickable {
                        if (linkedInLink != null) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(linkedInLink)))
                        } else {
                            showLinkedInSheet = true
                        }
                    }
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
                    text = if (linkedInLink != null) "Connected" else "Connect",
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
                    .clickable {
                        if (githubLink != null) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(githubLink)))
                        } else {
                            showGithubSheet = true
                        }
                    }
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
                    text = if (githubLink != null) "Connected" else "Connect",
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
                    .clickable {
                        if (twitterLink != null) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(twitterLink)))
                        } else {
                            showTwitterSheet = true
                        }
                    }
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
                    text = if (twitterLink != null) "Connected" else "Connect",
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
            // Last row ke baad divider nahi
        }
    }

    // ---------- Instagram bottom sheet ----------
    if (showInstagramSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInstagramSheet = false },
            sheetState = instagramSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text("Connect Instagram", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = AppColors.TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = instagramInput,
                    onValueChange = { instagramInput = it; instagramError = false },
                    placeholder = { Text("https://instagram.com/yourusername") },
                    singleLine = true,
                    isError = instagramError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (instagramError) {
                    Text(
                        text = "Please enter a valid Instagram profile link",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val trimmed = instagramInput.trim()
                        val isValid = trimmed.startsWith("https://instagram.com/") ||
                                trimmed.startsWith("https://www.instagram.com/")
                        if (isValid) {
                            instagramLink = trimmed
                            saveLink("instagram", trimmed) // NEW: Firestore mein save
                            showInstagramSheet = false
                        } else {
                            instagramError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // ---------- LinkedIn bottom sheet ----------
    if (showLinkedInSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLinkedInSheet = false },
            sheetState = linkedInSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text("Connect LinkedIn", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = AppColors.TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = linkedInInput,
                    onValueChange = { linkedInInput = it; linkedInError = false },
                    placeholder = { Text("https://linkedin.com/in/yourprofile") },
                    singleLine = true,
                    isError = linkedInError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (linkedInError) {
                    Text(
                        text = "Please enter a valid LinkedIn profile link",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val trimmed = linkedInInput.trim()
                        val isValid = trimmed.startsWith("https://linkedin.com/") ||
                                trimmed.startsWith("https://www.linkedin.com/")
                        if (isValid) {
                            linkedInLink = trimmed
                            saveLink("linkedin", trimmed) // NEW: Firestore mein save
                            showLinkedInSheet = false
                        } else {
                            linkedInError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // ---------- GitHub bottom sheet ----------
    if (showGithubSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGithubSheet = false },
            sheetState = githubSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text("Connect GitHub", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = AppColors.TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = githubInput,
                    onValueChange = { githubInput = it; githubError = false },
                    placeholder = { Text("https://github.com/yourusername") },
                    singleLine = true,
                    isError = githubError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (githubError) {
                    Text(
                        text = "Please enter a valid GitHub profile link",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val trimmed = githubInput.trim()
                        val isValid = trimmed.startsWith("https://github.com/") ||
                                trimmed.startsWith("https://www.github.com/")
                        if (isValid) {
                            githubLink = trimmed
                            saveLink("github", trimmed) // NEW: Firestore mein save
                            showGithubSheet = false
                        } else {
                            githubError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // ---------- Twitter/X bottom sheet ----------
    if (showTwitterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTwitterSheet = false },
            sheetState = twitterSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text("Connect Twitter/X", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = AppColors.TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = twitterInput,
                    onValueChange = { twitterInput = it; twitterError = false },
                    placeholder = { Text("https://x.com/yourusername") },
                    singleLine = true,
                    isError = twitterError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (twitterError) {
                    Text(
                        text = "Please enter a valid Twitter/X profile link",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val trimmed = twitterInput.trim()
                        val isValid = trimmed.startsWith("https://twitter.com/") ||
                                trimmed.startsWith("https://x.com/") ||
                                trimmed.startsWith("https://www.x.com/")
                        if (isValid) {
                            twitterLink = trimmed
                            saveLink("twitter", trimmed) // NEW: Firestore mein save
                            showTwitterSheet = false
                        } else {
                            twitterError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LinkAccountsPreview() {
    LinkAccounts(onBack = {})
}