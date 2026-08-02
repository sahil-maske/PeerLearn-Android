package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.sahilmaske.peerlearn.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _uiState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val uiState: StateFlow<ProfileState> = _uiState

    // NEW: real post count, calculated live from the "posts" collection instead of
    // a manually incremented field on the user doc. This way old posts (created
    // before any counter existed) are counted correctly too, and it self-corrects
    // if a post is ever deleted.
    private val _postCount = MutableStateFlow(0)
    val postCount: StateFlow<Int> = _postCount

    private var postCountListener: ListenerRegistration? = null

    fun fetchUserProfile(uid: String) {
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


    fun listenUserProfile(uid: String) {
        db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _userProfile.value = snapshot.toObject(User::class.java)
            }
    }

    // NEW: call this alongside fetchUserProfile/listenUserProfile/loadProfile,
    // for whichever uid's profile is being shown (own or someone else's).
    fun listenPostCount(uid: String) {
        postCountListener?.remove()
        postCountListener = db.collection("posts")
            .whereEqualTo("authorId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _postCount.value = snapshot.size()
            }
    }


    fun loadProfile(uid: String?) {
        val targetUid = uid ?: auth.currentUser?.uid
        android.util.Log.d("ProfileDebug", "loadProfile called. targetUid: $targetUid")

        if (targetUid == null) {
            android.util.Log.e("ProfileDebug", "targetUid is null, returning")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = ProfileState.Loading
                val doc = db.collection("users").document(targetUid).get().await()
                android.util.Log.d("ProfileDebug", "loadProfile success. doc exists: ${doc.exists()}")

                if (doc.exists()) {
                    val user = doc.toObject(User::class.java)
                    android.util.Log.d("ProfileDebug", "User object: $user")
                    _userProfile.value = user
                } else {
                    android.util.Log.e("ProfileDebug", "No document found for UID: $targetUid")
                }

                _uiState.value = ProfileState.Success
            } catch (e: Exception) {
                android.util.Log.e("ProfileDebug", "loadProfile error: ${e.message}", e)
                _uiState.value = ProfileState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        postCountListener?.remove()
    }
}

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}