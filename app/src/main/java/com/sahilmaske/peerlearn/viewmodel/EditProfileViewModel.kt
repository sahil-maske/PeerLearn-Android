package com.sahilmaske.peerlearn.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sahilmaske.peerlearn.data.model.User
import com.sahilmaske.peerlearn.util.ImageUploadUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditProfileViewModel : ViewModel() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val _uiState = MutableStateFlow<EditProfileState>(EditProfileState.Idle)
    val uiState: StateFlow<EditProfileState> = _uiState

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = EditProfileState.Loading
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val user = doc.toObject(User::class.java)
                    _userProfile.value = user
                    _uiState.value = EditProfileState.SuccessLoad
                } else {
                    _uiState.value = EditProfileState.Error("Profile not found")
                }
            } catch (e: Exception) {
                _uiState.value = EditProfileState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun uploadPhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isUploading.value = true
            val url = ImageUploadUtils.uploadToCloudinary(context, uri)
            if (url.isNotEmpty()) {
                _userProfile.value = _userProfile.value?.copy(avatarUrl = url)
            }
            _isUploading.value = false
        }
    }

    fun saveProfile(
        name: String,
        tagline: String,
        location: String,
        about: String,
        knownSkills: List<String>,
        learningSkills: List<String>
    ) {
        if (name.isBlank()) {
            _uiState.value = EditProfileState.Error("Name cannot be empty")
            return
        }

        val uid = auth.currentUser?.uid ?: return
        val currentProfile = _userProfile.value ?: return

        val updatedUser = currentProfile.copy(
            name = name,
            tagline = tagline,
            location = location,
            about = about,
            knownSkills = knownSkills,
            learningSkills = learningSkills
        )

        viewModelScope.launch {
            _uiState.value = EditProfileState.Saving
            try {
                db.collection("users").document(uid).set(updatedUser).await()
                _uiState.value = EditProfileState.SuccessSave
            } catch (e: Exception) {
                _uiState.value = EditProfileState.Error(e.message ?: "Failed to save profile")
            }
        }
    }

    fun resetState() {
        _uiState.value = EditProfileState.Idle
    }
}

sealed class EditProfileState {
    object Idle : EditProfileState()
    object Loading : EditProfileState()
    object SuccessLoad : EditProfileState()
    object Saving : EditProfileState()
    object SuccessSave : EditProfileState()
    data class Error(val message: String) : EditProfileState()
}
