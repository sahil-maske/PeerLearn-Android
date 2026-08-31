package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PrivacyUiState(
    val hideOnlineStatus: Boolean = false,
    val showPhoneNumber: Boolean = false,
    val profileVisibility: String = "Everyone",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PrivacyViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // NEW: listener ka reference rakha hai taaki ViewModel destroy hote hi hata sakein
    private var privacyListener: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    init {
        loadPrivacySettings()
    }

    private fun loadPrivacySettings() {
        val uid = auth.currentUser?.uid ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        // NEW: addSnapshotListener ka return value save kiya
        privacyListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    _uiState.value = _uiState.value.copy(
                        hideOnlineStatus = snapshot.getBoolean("hideOnlineStatus") ?: false,
                        showPhoneNumber = snapshot.getBoolean("showPhoneNumber") ?: false,
                        profileVisibility = snapshot.getString("profileVisibility") ?: "Everyone",
                        isLoading = false
                    )
                }
            }
    }

    fun updateHideOnlineStatus(hide: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("hideOnlineStatus", hide)
    }

    fun updateShowPhoneNumber(show: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("showPhoneNumber", show)
    }

    fun updateProfileVisibility(visibility: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("profileVisibility", visibility)
    }

    // NEW: ViewModel destroy hone pe (screen band hone pe) listener remove karo
    // Warna listener background mein chalta rehta, memory leak aur unnecessary
    // Firestore reads ka reason banta.
    override fun onCleared() {
        super.onCleared()
        privacyListener?.remove()
    }
}