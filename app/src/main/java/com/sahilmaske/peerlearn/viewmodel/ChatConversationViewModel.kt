package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.sahilmaske.peerlearn.model.Message
import com.sahilmaske.peerlearn.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class PeerInfo(
    val uid: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val skillContext: String = ""
)

class ChatConversationViewModel(private val chatId: String) : ViewModel() {

    private val db = Firebase.firestore
    val currentUid: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _peerInfo = MutableStateFlow(PeerInfo())
    val peerInfo: StateFlow<PeerInfo> = _peerInfo

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _otherUserPresence = MutableStateFlow<User?>(null)
    val otherUserPresence: StateFlow<User?> = _otherUserPresence

    // NEW: presence listener ko reference store karna hai taaki onCleared() mein remove kar sake
    private var presenceListener: ListenerRegistration? = null

    init {
        loadConversationAndPeer()
        listenToMessages()
    }

    private fun loadConversationAndPeer() {
        viewModelScope.launch {
            try {
                val convoDoc = db.collection("conversations").document(chatId).get().await()

                val existingParticipants = convoDoc.get("participants") as? List<*>
                val skillContext = convoDoc.getString("skillContext") ?: ""

                val otherUid = if (existingParticipants != null) {
                    existingParticipants.firstOrNull { it != currentUid } as? String
                } else {
                    chatId.split("_").firstOrNull { it != currentUid }
                }

                if (otherUid.isNullOrBlank()) return@launch

                val userDoc = db.collection("users").document(otherUid).get().await()
                _peerInfo.value = PeerInfo(
                    uid = otherUid,
                    name = userDoc.getString("name") ?: "Unknown",
                    avatarUrl = userDoc.getString("avatarUrl") ?: "",
                    skillContext = skillContext
                )

                // NEW: peer ka uid mil gaya, ab uska presence sunna start karo
                listenToPeerPresence(otherUid)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // NEW FUNCTION: other user ke presence document ko real-time sunta hai
    private fun listenToPeerPresence(otherUid: String) {
        presenceListener = db.collection("users").document(otherUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _otherUserPresence.value = snapshot.toObject(User::class.java)
            }
    }

    private fun listenToMessages() {
        db.collection("conversations").document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _messages.value = snapshot.toObjects(Message::class.java)
            }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || currentUid.isBlank()) return

        val peerUid = _peerInfo.value.uid
        if (peerUid.isBlank()) return

        val message = hashMapOf(
            "senderId" to currentUid,
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )
        val convoRef = db.collection("conversations").document(chatId)
        convoRef.collection("messages").add(message)

        convoRef.set(
            mapOf(
                "participants" to listOf(currentUid, peerUid),
                "lastMessage" to text,
                "timestamp" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        )
    }

    // NEW: listener ko clean karo jab ViewModel destroy ho, warna memory leak hoga
    override fun onCleared() {
        super.onCleared()
        presenceListener?.remove()
    }
}