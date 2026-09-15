package com.sahilmaske.peerlearn.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sahilmaske.peerlearn.data.model.User
import com.sahilmaske.peerlearn.repository.SkillMatchRepository
import com.sahilmaske.peerlearn.util.ImageUploadUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class EditProfileState {
    object Idle : EditProfileState()
    object Loading : EditProfileState()
    object Saving : EditProfileState()
    object SuccessSave : EditProfileState()
    data class Error(val message: String) : EditProfileState()
}

class EditProfileViewModel(
    private val repository: SkillMatchRepository = SkillMatchRepository()
) : ViewModel() {

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
                    _userProfile.value = doc.toObject(User::class.java)
                }
                _uiState.value = EditProfileState.Idle
            } catch (e: Exception) {
                _uiState.value = EditProfileState.Error(e.message ?: "Failed to load profile")
            }
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
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = EditProfileState.Saving
            try {
                val currentUser = _userProfile.value
                
                // Embedding update logic
                var newKnownEmbedding = currentUser?.knownSkillsEmbedding ?: emptyList()
                var newLearningEmbedding = currentUser?.learningSkillsEmbedding ?: emptyList()

                if (currentUser?.knownSkills != knownSkills && knownSkills.isNotEmpty()) {
                    newKnownEmbedding = repository.getEmbedding(knownSkills.joinToString(", ")) ?: newKnownEmbedding
                }
                if (currentUser?.learningSkills != learningSkills && learningSkills.isNotEmpty()) {
                    newLearningEmbedding = repository.getEmbedding(learningSkills.joinToString(", ")) ?: newLearningEmbedding
                }

                val updates = mutableMapOf<String, Any>(
                    "name" to name,
                    "tagline" to tagline,
                    "location" to location,
                    "about" to about,
                    "knownSkills" to knownSkills,
                    "learningSkills" to learningSkills,
                    "knownSkillsEmbedding" to newKnownEmbedding,
                    "learningSkillsEmbedding" to newLearningEmbedding
                )

                db.collection("users").document(uid).update(updates).await()
                
                // Update local state
                _userProfile.value = currentUser?.copy(
                    name = name,
                    tagline = tagline,
                    location = location,
                    about = about,
                    knownSkills = knownSkills,
                    learningSkills = learningSkills,
                    knownSkillsEmbedding = newKnownEmbedding,
                    learningSkillsEmbedding = newLearningEmbedding
                )
                
                _uiState.value = EditProfileState.SuccessSave
            } catch (e: Exception) {
                _uiState.value = EditProfileState.Error(e.message ?: "Failed to save profile")
            }
        }
    }

    fun uploadPhoto(context: Context, uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val imageUrl = ImageUploadUtils.uploadToCloudinary(context, uri)
                if (imageUrl.isNotEmpty()) {
                    db.collection("users").document(uid)
                        .update("avatarUrl", imageUrl)
                        .await()
                    
                    _userProfile.value = _userProfile.value?.copy(avatarUrl = imageUrl)
                }
            } catch (e: Exception) {
                // Optionally handle error
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun resetState() {
        _uiState.value = EditProfileState.Idle
    }
}
