package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.sahilmaske.peerlearn.model.Comment
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

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

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

    // ---- Comments for whichever post is currently open in the bottom sheet ----
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    init {
        try {
            loadPosts()
            loadSuggestedPeers()
        } catch (e: Exception) {
            // This is expected in Compose Preview environments where Firebase is not initialized
            android.util.Log.w("FeedViewModel", "Firebase initialization skipped in preview/test: ${e.message}")
        }
    }

    // Real-time listener — jaise hi Firestore "posts" collection mein naya document
    // add hota hai (PostScreen se post karne ke baad), ye list turant update ho jaati hai.
    fun loadPosts() {
        val user = auth.currentUser
        android.util.Log.d("FeedDebug", "loadPosts: UID=${user?.uid}, email=${user?.email}, isAnonymous=${user?.isAnonymous}")

        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FeedDebug", "loadPosts listener error: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    android.util.Log.d("FeedDebug", "loadPosts listener: fetched ${snapshot.size()} posts")
                    _posts.value = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Post::class.java)?.copy(id = doc.id)
                    }
                }
            }
    }

    // ---- LIKE toggle ----
    // arrayUnion/arrayRemove + increment() atomic operations hain — race-condition safe,
    // isliye do log ek saath like kar rahe hon to bhi count sahi rahega.
    fun toggleLike(postId: String, currentUserId: String, isCurrentlyLiked: Boolean) {
        if (postId.isEmpty() || currentUserId.isEmpty()) return
        val postRef = db.collection("posts").document(postId)
        if (isCurrentlyLiked) {
            postRef.update(
                mapOf(
                    "likedBy" to FieldValue.arrayRemove(currentUserId),
                    "likeCount" to FieldValue.increment(-1)
                )
            )
        } else {
            postRef.update(
                mapOf(
                    "likedBy" to FieldValue.arrayUnion(currentUserId),
                    "likeCount" to FieldValue.increment(1)
                )
            )
        }
    }

    // ---- COMMENTS: real-time listener for a specific post's comments subcollection ----
    fun loadComments(postId: String) {
        if (postId.isEmpty()) return
        db.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FeedDebug", "loadComments error: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _comments.value = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Comment::class.java)?.copy(id = doc.id)
                    }
                }
            }
    }

    // ---- Add a new comment + bump the post's commentCount ----
    fun addComment(postId: String, authorId: String, authorName: String, authorAvatarUrl: String, text: String) {
        if (postId.isEmpty() || text.isBlank()) return
        viewModelScope.launch {
            try {
                val comment = hashMapOf(
                    "authorId" to authorId,
                    "authorName" to authorName,
                    "authorAvatarUrl" to authorAvatarUrl,
                    "text" to text,
                    "timestamp" to System.currentTimeMillis(),
                    "isMarkedHelpful" to false
                )
                db.collection("posts").document(postId).collection("comments").add(comment).await()
                db.collection("posts").document(postId)
                    .update("commentCount", FieldValue.increment(1))
            } catch (e: Exception) {
                android.util.Log.e("FeedDebug", "addComment error: ${e.message}", e)
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
            val college = doc.getString("college") ?: ""
            val knownSkills = doc.get("knownSkills") as? List<String> ?: emptyList()
            val learningSkills = doc.get("learningSkills") as? List<String> ?: emptyList()

            val match = calculateMatchPercentage(mySkills, knownSkills)

            PeerSuggestion(
                id = uid,
                uid = uid,
                name = name,
                avatarUrl = avatarUrl,
                institution = college,
                knowSkill = knownSkills.joinToString(", "),
                learnSkill = learningSkills.joinToString(", "),
                matchPercentage = match
            )
        }.sortedByDescending { it.matchPercentage }
    }
}