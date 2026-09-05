package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class SupportUiState(
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null
)

class SupportViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    fun reportProblem(description: String) {
        val userId = auth.currentUser?.uid ?: return
        if (description.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Description cannot be empty")
            return
        }

        _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

        val report = hashMapOf(
            "userId" to userId,
            "description" to description,
            "timestamp" to Date(),
            "status" to "Pending",
            "type" to "Problem"
        )

        viewModelScope.launch {
            db.collection("reports")
                .add(report)
                .addOnSuccessListener {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitSuccess = true)
                }
                .addOnFailureListener { e ->
                    _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = e.message)
                }
        }
    }

    fun reportUser(reportedUserId: String, description: String) {
        val userId = auth.currentUser?.uid ?: return
        if (reportedUserId.isBlank() || description.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "All fields are required")
            return
        }

        _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

        val report = hashMapOf(
            "userId" to userId,
            "reportedUserId" to reportedUserId,
            "description" to description,
            "timestamp" to Date(),
            "status" to "Pending",
            "type" to "User"
        )

        viewModelScope.launch {
            db.collection("reports")
                .add(report)
                .addOnSuccessListener {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitSuccess = true)
                }
                .addOnFailureListener { e ->
                    _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = e.message)
                }
        }
    }

    fun resetState() {
        _uiState.value = SupportUiState()
    }
}
