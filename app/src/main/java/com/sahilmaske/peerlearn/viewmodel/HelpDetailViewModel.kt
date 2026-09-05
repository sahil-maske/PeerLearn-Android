package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.sahilmaske.peerlearn.model.Comment
import com.sahilmaske.peerlearn.model.Post
import com.sahilmaske.peerlearn.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HelpDetailViewModel : ViewModel() {

    private val db by lazy { FirebaseFirestore.getInstance() }

    private val _post = MutableStateFlow<Post?>(null)
    val post: StateFlow<Post?> = _post

    // Reuses the same "posts/{postId}/comments" subcollection as the regular
    // like/comment system — a help offer IS just a comment, with an extra
    // isMarkedHelpful flag on top.
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    // NEW: Map of userId -> User object for real-time profile info in the offers list
    private val _commentAuthors = MutableStateFlow<Map<String, User>>(emptyMap())
    val commentAuthors: StateFlow<Map<String, User>> = _commentAuthors

    private var commentsListener: ListenerRegistration? = null
    private var postListener: ListenerRegistration? = null

    fun listenPost(postId: String) {
        if (postId.isEmpty()) return
        postListener?.remove()
        postListener = db.collection("posts").document(postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _post.value = snapshot.toObject(Post::class.java)?.copy(id = snapshot.id)
            }
    }

    fun loadPost(postId: String) {
        if (postId.isEmpty()) return
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { doc ->
                _post.value = doc.toObject(Post::class.java)?.copy(id = doc.id)
            }
    }

    fun listenComments(postId: String) {
        if (postId.isEmpty()) return
        commentsListener?.remove()
        commentsListener = db.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val newComments = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                }
                _comments.value = newComments
                fetchAuthorProfiles(newComments)
            }
    }

    private fun fetchAuthorProfiles(comments: List<Comment>) {
        val uniqueAuthorIds = comments.map { it.authorId }.distinct()
        val currentAuthors = _commentAuthors.value.toMutableMap()
        
        uniqueAuthorIds.forEach { authorId ->
            if (!currentAuthors.containsKey(authorId)) {
                db.collection("users").document(authorId).get()
                    .addOnSuccessListener { doc ->
                        doc.toObject(User::class.java)?.let { user ->
                            _commentAuthors.value = _commentAuthors.value + (authorId to user)
                        }
                    }
            }
        }
    }

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
                android.util.Log.e("HelpDetailVM", "addComment error: ${e.message}", e)
            }
        }
    }

    // Toggle a comment's "helped" status. Only the post owner should be able to call
    // this (enforced in the UI). Increments/decrements the commenter's helpCount on
    // their user doc so the profile badge stays in sync — un-marking by mistake is
    // reversible, it just decrements back down.
    fun toggleMarkAsHelpful(postId: String, commentId: String, helperId: String, currentlyMarked: Boolean) {
        if (postId.isEmpty() || commentId.isEmpty() || helperId.isEmpty()) return
        val newValue = !currentlyMarked

        db.collection("posts").document(postId).collection("comments").document(commentId)
            .update("isMarkedHelpful", newValue)
            .addOnSuccessListener {
                db.collection("users").document(helperId)
                    .update("helpCount", FieldValue.increment(if (newValue) 1L else -1L))
            }
    }

    override fun onCleared() {
        super.onCleared()
        commentsListener?.remove()
        postListener?.remove()
    }
}