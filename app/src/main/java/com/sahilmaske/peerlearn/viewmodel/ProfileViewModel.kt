package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sahilmaske.peerlearn.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

class ProfileViewModel : ViewModel() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val _userProfile = MutableStateFlow<User?>(null)

    val userProfile: StateFlow<User?> = _userProfile
    fun fetchUserProfile(uid : String) {
        _uiState.value = ProfileState.Loading
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                _userProfile.value = user
                _uiState.value = ProfileState.Success
            }
            .addOnFailureListener {
                _uiState.value = ProfileState.Error(it.message ?: "Failed to fetch user profile")
            }
    }

    private val _uiState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val uiState: StateFlow<ProfileState> = _uiState

    fun saveProfile(user: User) {
        _uiState.value = ProfileState.Loading
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                _uiState.value = ProfileState.Success
            }
            .addOnFailureListener {
                _uiState.value = ProfileState.Error(it.message ?: "Failed to save profile")
            }
    }

    fun loadProfile(uid: String?) {
        val targetUid = uid ?: auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                _uiState.value = ProfileState.Loading
                val doc = db.collection("users").document(targetUid).get().await()
                _userProfile.value = doc.toObject(User::class.java)
                _uiState.value = ProfileState.Success
            } catch (e: Exception) {
                _uiState.value = ProfileState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

}

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}