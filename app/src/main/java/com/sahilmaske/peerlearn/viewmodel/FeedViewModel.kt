package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.sahilmaske.peerlearn.model.PeerSuggestion
import com.sahilmaske.peerlearn.model.Post
import com.sahilmaske.peerlearn.util.calculateMatchPercentage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FeedViewModel(
    private val profileViewModel: ProfileViewModel
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        fun provideFactory(profileViewModel: ProfileViewModel): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FeedViewModel(profileViewModel) as T
                }
            }
    }

    val greetingName: StateFlow<String> = profileViewModel.userProfile
        .map { it?.name ?: "User" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "User")

    private val _suggestions = MutableStateFlow<List<PeerSuggestion>>(emptyList())
    val suggestions: StateFlow<List<PeerSuggestion>> = _suggestions

    private val _allPeers = MutableStateFlow<List<PeerSuggestion>>(emptyList())
    val allPeers: StateFlow<List<PeerSuggestion>> = _allPeers

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    init {
        loadPosts()
        loadSuggestedPeers()
    }

    fun loadPosts() {
        val user = auth.currentUser
        android.util.Log.d("FeedDebug", "loadPosts: UID=${user?.uid}, email=${user?.email}, isAnonymous=${user?.isAnonymous}")

        user?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                android.util.Log.d("FeedDebug", "Token OK, length=${task.result?.token?.length}")
            } else {
                android.util.Log.e("FeedDebug", "Token FAILED: ${task.exception?.message}", task.exception)
            }
        }

        android.util.Log.d("FeedDebug", "loadPosts called")
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    android.util.Log.e("FeedDebug", "loadPosts error: User not authenticated")
                    return@launch
                }

                val snapshot = db.collection("posts").get().await()
                android.util.Log.d("FeedDebug", "loadPosts success: fetched ${snapshot.size()} posts")
                _posts.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                android.util.Log.e("FeedDebug", "loadPosts error: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }

    fun loadSuggestedPeers() {
        android.util.Log.d("FeedDebug", "loadSuggestedPeers called")
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                android.util.Log.d("FeedDebug", "loadSuggestedPeers: currentUser UID = ${currentUser?.uid}")
                
                val currentUserId = currentUser?.uid ?: return@launch
                val mySkills = getMySkills(currentUserId)
                android.util.Log.d("FeedDebug", "loadSuggestedPeers: mySkills = $mySkills")

                val snapshot = db.collection("users")
                    .limit(10)
                    .get()
                    .await()

                android.util.Log.d("FeedDebug", "loadSuggestedPeers success: fetched ${snapshot.size()} users")
                _suggestions.value = mapToPeerList(snapshot, mySkills, currentUserId)
            } catch (e: Exception) {
                android.util.Log.e("FeedDebug", "loadSuggestedPeers error: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }

    fun loadAllPeers() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                val currentUserId = currentUser?.uid ?: return@launch
                val mySkills = getMySkills(currentUserId)

                val snapshot = db.collection("users")
                    .get()
                    .await()

                _allPeers.value = mapToPeerList(snapshot, mySkills, currentUserId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun getMySkills(currentUserId: String): List<String> {
        android.util.Log.d("FeedDebug", "getMySkills called for $currentUserId")
        val myDoc = db.collection("users").document(currentUserId).get().await()
        android.util.Log.d("FeedDebug", "getMySkills: document exists = ${myDoc.exists()}")
        val skills = myDoc.get("knownSkills") as? List<String> ?: emptyList()
        android.util.Log.d("FeedDebug", "getMySkills: returned $skills")
        return skills
    }

    private fun mapToPeerList(
        snapshot: QuerySnapshot,
        mySkills: List<String>,
        currentUserId: String
    ): List<PeerSuggestion> {
        return snapshot.documents.mapNotNull { doc ->
            val uid = doc.id
            if (uid == currentUserId) return@mapNotNull null

            val name = doc.getString("name") ?: "Unknown"
            val avatarUrl = doc.getString("avatarUrl") ?: ""
            val knownSkills = doc.get("knownSkills") as? List<String> ?: emptyList()
            val learningSkills = doc.get("learningSkills") as? List<String> ?: emptyList()

            val match = calculateMatchPercentage(mySkills, knownSkills)

            PeerSuggestion(
                id = uid,
                uid = uid,
                name = name,
                avatarUrl = avatarUrl,
                knowSkill = knownSkills.joinToString(", "),
                learnSkill = learningSkills.joinToString(", "),
                matchPercentage = match
            )
        }.sortedByDescending { it.matchPercentage }
    }
}
