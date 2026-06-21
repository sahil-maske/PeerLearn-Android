package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null
    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _uiState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _uiState.value = AuthState.Success
            }
            .addOnFailureListener {
                _uiState.value = AuthState.Error(it.message ?: "Login failed")
            }
    }
    fun registerWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _uiState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _uiState.value = AuthState.Success
            }
            .addOnFailureListener {
                _uiState.value = AuthState.Error(it.message ?: "Registration failed")
            }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}