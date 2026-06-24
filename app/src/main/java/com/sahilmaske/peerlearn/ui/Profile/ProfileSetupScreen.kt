package com.sahilmaske.peerlearn.ui.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.model.User
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel

@Composable
fun ProfileSetupScreen(
    onProfileSaved: () -> Unit = {},
    viewModel: ProfileViewModel = ProfileViewModel(),
) {
    var currentStep by remember { mutableStateOf(1) }
    var name by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var skillsHave by remember { mutableStateOf("") }
    var skillsWant by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

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
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { currentStep = 2 },
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

                OutlinedTextField(
                    value = skillsHave,
                    onValueChange = { skillsHave = it },
                    label = { Text("Skills I Know (e.g. Kotlin, Firebase)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF000000),
                        unfocusedBorderColor = Color(0xFFD1D1D6)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = skillsWant,
                    onValueChange = { skillsWant = it },
                    label = { Text("Skills I Want to Learn") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF000000),
                        unfocusedBorderColor = Color(0xFFD1D1D6)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

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
                        Text(
                            text = "← Back",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                    Button(
                        onClick = { onProfileSaved()
                            val user = User(
                                uid = currentUid,
                                name = name,
                                college = college,
                                skillsHave = skillsHave,
                                skillsWant = skillsWant,
                                bio = bio
                            )
                            viewModel.saveProfile(user)
                        },
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