package com.sahilmaske.peerlearn.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.EditProfileState
import com.sahilmaske.peerlearn.viewmodel.EditProfileViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()

    var name by remember { mutableStateOf("") }
    var tagline by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var knownSkills by remember { mutableStateOf(listOf<String>()) }
    var learningSkills by remember { mutableStateOf(listOf<String>()) }

    var newKnownSkill by remember { mutableStateOf("") }
    var newLearningSkill by remember { mutableStateOf("") }

    var showImagePicker by remember { mutableStateOf(false) }
    var pendingCameraImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = it.name
            tagline = it.tagline
            location = it.location
            about = it.about
            knownSkills = it.knownSkills
            learningSkills = it.learningSkills
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is EditProfileState.SuccessSave) {
            Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show()
            onBack()
        } else if (uiState is EditProfileState.Error) {
            Toast.makeText(context, (uiState as EditProfileState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadPhoto(context, it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingCameraImageUri?.let { viewModel.uploadPhoto(context, it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is EditProfileState.Saving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = AppColors.Primary)
                    } else {
                        IconButton(
                            onClick = {
                                viewModel.saveProfile(name, tagline, location, about, knownSkills, learningSkills)
                            },
                            enabled = !isUploading
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = AppColors.Primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Surface)
            )
        },
        containerColor = AppColors.Background
    ) { padding ->
        if (uiState is EditProfileState.Loading && userProfile == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(AppColors.SecondaryContainer)
                                .clickable { showImagePicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (userProfile?.avatarUrl.isNullOrEmpty()) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(40.dp), tint = AppColors.TextSecondary)
                            } else {
                                AsyncImage(
                                    model = userProfile?.avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (isUploading) {
                                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                        // Edit overlay badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .clip(CircleShape)
                                .background(AppColors.Primary)
                                .clickable { showImagePicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }

                item {
                    EditTextField(value = name, onValueChange = { name = it }, label = "Display Name", required = true)
                    EditTextField(value = tagline, onValueChange = { if (it.length <= 80) tagline = it }, label = "Tagline (Bio)", placeholder = "e.g. Android Developer")
                    EditTextField(value = location, onValueChange = { location = it }, label = "Location", placeholder = "e.g. Pune, India")
                    EditTextField(value = about, onValueChange = { about = it }, label = "About / Description", singleLine = false, minLines = 3)

                    Spacer(Modifier.height(16.dp))
                }

                item {
                    SkillInputSection(
                        title = "Can Teach (Known Skills)",
                        skills = knownSkills,
                        onAddSkill = { if (it.isNotBlank()) knownSkills = knownSkills + it.trim() },
                        onRemoveSkill = { skill -> knownSkills = knownSkills.filter { it != skill } }
                    )
                    Spacer(Modifier.height(16.dp))
                    SkillInputSection(
                        title = "Wants to Learn",
                        skills = learningSkills,
                        onAddSkill = { if (it.isNotBlank()) learningSkills = learningSkills + it.trim() },
                        onRemoveSkill = { skill -> learningSkills = learningSkills.filter { it != skill } }
                    )
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }

    if (showImagePicker) {
        ImagePickerDialog(
            showDialog = showImagePicker,
            onDismissRequest = { showImagePicker = false },
            onCameraClick = {
                showImagePicker = false
                val tempFile = File.createTempFile("avatar_", ".jpg", context.cacheDir)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
                pendingCameraImageUri = uri
                cameraLauncher.launch(uri)
            },
            onGalleryClick = {
                showImagePicker = false
                galleryLauncher.launch("image/*")
            }
        )
    }
}

@Composable
fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    required: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = if (required) "$label *" else label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Divider
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillInputSection(
    title: String,
    skills: List<String>,
    onAddSkill: (String) -> Unit,
    onRemoveSkill: (String) -> Unit
) {
    var newSkill by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
        Spacer(Modifier.height(8.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            skills.forEach { skill ->
                InputChip(
                    selected = false,
                    onClick = { },
                    label = { Text(skill, fontSize = 12.sp) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.size(14.dp).clickable { onRemoveSkill(skill) }
                        )
                    },
                    shape = RoundedCornerShape(50),
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = AppColors.PrimaryContainer,
                        labelColor = AppColors.Primary
                    ),
                    border = null
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newSkill,
                onValueChange = { newSkill = it },
                placeholder = { Text("Add a skill...", fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.Divider
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { 
                    onAddSkill(newSkill)
                    newSkill = ""
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = AppColors.Primary, contentColor = Color.White),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}
