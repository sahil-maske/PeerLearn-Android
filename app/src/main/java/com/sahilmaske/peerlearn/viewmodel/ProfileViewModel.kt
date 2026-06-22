package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sahilmaske.peerlearn.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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
}

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}