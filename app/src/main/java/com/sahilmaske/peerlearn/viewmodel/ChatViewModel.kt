package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.sahilmaske.peerlearn.model.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class ChatViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val currentUid: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    // otherUid -> presence listener, taaki duplicate listener na lage
    private val presenceListeners = mutableMapOf<String, ListenerRegistration>()

    init {
        listenToConversations()
    }

    private fun listenToConversations() {
        if (currentUid.isBlank()) return

        db.collection("conversations")
            .whereArrayContains("participants", currentUid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val docs = snapshot.documents

                viewModelScope.launch {
                    val resolvedList = docs.mapNotNull { doc ->
                        try {
                            val participants = doc.get("participants") as? List<*> ?: return@mapNotNull null
                            val otherUid = participants.firstOrNull { it != currentUid } as? String
                                ?: return@mapNotNull null

                            val userDoc = db.collection("users").document(otherUid).get().await()
                            val name = userDoc.getString("name") ?: "Unknown"
                            val avatarUrl = userDoc.getString("avatarUrl") ?: ""
                            val isOnline = userDoc.getBoolean("isOnline") ?: false

                            val lastMessage = doc.getString("lastMessage") ?: ""
                            val timestamp = doc.getLong("timestamp") ?: 0L

                            Conversation(
                                id = doc.id,
                                otherUid = otherUid,
                                name = name,
                                avatarUrl = avatarUrl,
                                lastMessage = lastMessage,
                                time = formatTime(timestamp),
                                isOnline = isOnline
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    _conversations.value = resolvedList

                    // 👇 ab har otherUid pe real-time presence listener attach karo
                    resolvedList.forEach { convo ->
                        attachPresenceListener(convo.otherUid)
                    }
                }
            }
    }

    private fun attachPresenceListener(otherUid: String) {
        if (presenceListeners.containsKey(otherUid)) return // already listening

        val listener = db.collection("users").document(otherUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val isOnline = snapshot.getBoolean("isOnline") ?: false

                // current list mein us user ka isOnline update karo
                _conversations.value = _conversations.value.map { convo ->
                    if (convo.otherUid == otherUid) convo.copy(isOnline = isOnline) else convo
                }
            }

        presenceListeners[otherUid] = listener
    }

    private fun formatTime(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onCleared() {
        super.onCleared()
        presenceListeners.values.forEach { it.remove() }
        presenceListeners.clear()
    }
}