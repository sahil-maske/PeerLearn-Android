package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

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
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    _uiState.value = AuthState.Error("User ID not found after registration")
                    return@addOnSuccessListener
                }

                val userMap = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("users").document(uid)
                    .set(userMap)
                    .addOnSuccessListener {
                        _uiState.value = AuthState.Success
                    }
                    .addOnFailureListener { e ->
                        _uiState.value = AuthState.Error(e.message ?: "Failed to save user data")
                    }
            }
            .addOnFailureListener {
                _uiState.value = AuthState.Error(it.message ?: "Registration failed")
            }
    }

    fun resetState() {
        _uiState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}