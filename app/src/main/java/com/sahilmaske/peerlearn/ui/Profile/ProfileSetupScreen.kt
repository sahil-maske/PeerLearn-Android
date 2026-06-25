package com.sahilmaske.peerlearn.ui.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.model.User
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel

@Composable
fun ProfileSetupScreen(
    onProfileSaved: () -> Unit = {},
    viewModel: ProfileViewModel = ProfileViewModel(),
) {
    var currentStep by remember { mutableStateOf(1) }

    // Step 1 fields
    var name by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // Step 2 fields
    var bio by remember { mutableStateOf("") }
    var knownSkills by remember { mutableStateOf(listOf<String>()) }
    var learningSkills by remember { mutableStateOf(listOf<String>()) }
    var knownInput by remember { mutableStateOf("") }
    var learningInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center)
        ) {
            if (currentStep == 1) {

                Text(
                    text = "Basic Info",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = Color(0xFF000000)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Step 1 of 2",
                    fontSize = 16.sp,
                    color = Color(0xFF8E8E93)
                )
                Spacer(modifier = Modifier.height(40.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF000000),
                        unfocusedBorderColor = Color(0xFFD1D1D6)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = college,
                    onValueChange = { college = it },
                    label = { Text("College") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF000000),
                        unfocusedBorderColor = Color(0xFFD1D1D6)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (e.g. Nagpur, Maharashtra)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF000000),
                        unfocusedBorderColor = Color(0xFFD1D1D6)
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { currentStep = 2 },
                    enabled = name.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000)
                    )
                ) {
                    Text(
                        text = "Next →",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

            } else {

                Text(
                    text = "Your Skills",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = Color(0xFF000000)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Step 2 of 2",
                    fontSize = 16.sp,
                    color = Color(0xFF8E8E93)
                )
                Spacer(modifier = Modifier.height(40.dp))

                // Known Skills
                Text(
                    text = "Skills I Know",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF000000)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Known skills tags
                if (knownSkills.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(knownSkills) { skill ->
                            AssistChip(
                                onClick = {
                                    knownSkills = knownSkills - skill
                                },
                                label = { Text(skill, fontSize = 13.sp) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFFE1F5EE),
                                    labelColor = Color(0xFF085041)
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = Color(0xFF1D9E75)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Known skill input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = knownInput,
                        onValueChange = { knownInput = it },
                        label = { Text("e.g. Kotlin, Firebase") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1D9E75),
                            unfocusedBorderColor = Color(0xFFD1D1D6)
                        )
                    )
                    Button(
                        onClick = {
                            if (knownInput.isNotBlank()) {
                                knownSkills = knownSkills + knownInput.trim()
                                knownInput = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D9E75)
                        )
                    ) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Learning Skills
                Text(
                    text = "Skills I Want to Learn",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF000000)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Learning skills tags
                if (learningSkills.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(learningSkills) { skill ->
                            AssistChip(
                                onClick = {
                                    learningSkills = learningSkills - skill
                                },
                                label = { Text(skill, fontSize = 13.sp) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFFFAEEDA),
                                    labelColor = Color(0xFF633806)
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = Color(0xFFEF9F27)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Learning skill input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = learningInput,
                        onValueChange = { learningInput = it },
                        label = { Text("e.g. Jetpack Compose") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEF9F27),
                            unfocusedBorderColor = Color(0xFFD1D1D6)
                        )
                    )
                    Button(
                        onClick = {
                            if (learningInput.isNotBlank()) {
                                learningSkills = learningSkills + learningInput.trim()
                                learningInput = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF9F27)
                        )
                    ) {
                        Text("Add", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio (optional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF000000),
                        unfocusedBorderColor = Color(0xFFD1D1D6)
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { currentStep = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF000000)
                        )
                    ) {
                        Text("← Back", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val currentUid = try {
                                FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            } catch (e: Exception) {
                                ""
                            }
                            val user = User(
                                uid = currentUid,
                                name = name,
                                college = college,
                                location = location,
                                bio = bio,
                                knownSkills = knownSkills,
                                learningSkills = learningSkills,
                            )
                            viewModel.saveProfile(user)
                            onProfileSaved()
                        },
                        enabled = knownSkills.isNotEmpty() || learningSkills.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF000000)
                        )
                    ) {
                        Text(
                            text = "Save →",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSetupPreview() {
    ProfileSetupScreen()
}