package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AccountViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _email = MutableStateFlow(auth.currentUser?.email ?: "")
    val email: StateFlow<String> = _email

    private val _isVerified = MutableStateFlow(auth.currentUser?.isEmailVerified ?: false)
    val isVerified: StateFlow<Boolean> = _isVerified

    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState: StateFlow<VerificationState> = _verificationState

    private val _passwordResetState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val passwordResetState: StateFlow<VerificationState> = _passwordResetState

    private val _deletionState = MutableStateFlow<DeletionState>(DeletionState.Idle)
    val deletionState: StateFlow<DeletionState> = _deletionState

    fun sendVerificationEmail() {
        _verificationState.value = VerificationState.Loading
        auth.currentUser?.sendEmailVerification()
            ?.addOnSuccessListener {
                _verificationState.value = VerificationState.Sent
            }
            ?.addOnFailureListener { e ->
                _verificationState.value = VerificationState.Error(e.message ?: "Failed to send")
            }
    }

    fun sendPasswordResetEmail() {
        val email = auth.currentUser?.email ?: return
        _passwordResetState.value = VerificationState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _passwordResetState.value = VerificationState.Sent
            }
            .addOnFailureListener { e ->
                _passwordResetState.value = VerificationState.Error(e.message ?: "Failed to send")
            }
    }

    // Call this to refresh status after user clicks link and comes back to app
    fun checkVerificationStatus() {
        auth.currentUser?.reload()?.addOnSuccessListener {
            _isVerified.value = auth.currentUser?.isEmailVerified ?: false
        }
    }

    fun deleteAccount() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        
        viewModelScope.launch {
            _deletionState.value = DeletionState.Loading
            try {
                // 1. Delete from Firestore
                FirebaseFirestore.getInstance().collection("users").document(uid).delete().await()
                
                // 2. Delete from Auth
                user.delete().await()
                
                _deletionState.value = DeletionState.Success
            } catch (e: Exception) {
                if (e.message?.contains("RECENT_LOGIN") == true || e.toString().contains("recent-login")) {
                    _deletionState.value = DeletionState.RequiresRecentLogin
                } else {
                    _deletionState.value = DeletionState.Error(e.message ?: "Failed to delete account")
                }
            }
        }
    }

    fun resetDeletionState() {
        _deletionState.value = DeletionState.Idle
    }
}

sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    object Sent : VerificationState()
    data class Error(val message: String) : VerificationState()
}

sealed class DeletionState {
    object Idle : DeletionState()
    object Loading : DeletionState()
    object Success : DeletionState()
    object RequiresRecentLogin : DeletionState()
    data class Error(val message: String) : DeletionState()
}
