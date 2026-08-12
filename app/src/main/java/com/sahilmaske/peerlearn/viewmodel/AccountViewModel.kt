package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
}

sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    object Sent : VerificationState()
    data class Error(val message: String) : VerificationState()
}