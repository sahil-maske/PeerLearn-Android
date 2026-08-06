package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sahilmaske.peerlearn.model.Message
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

    init {
        loadConversationAndPeer()
        listenToMessages()
    }

    private fun loadConversationAndPeer() {
        viewModelScope.launch {
            try {
                val convoDoc = db.collection("conversations").document(chatId).get().await()
                val participants = convoDoc.get("participants") as? List<*> ?: return@launch
                val skillContext = convoDoc.getString("skillContext") ?: ""

                val otherUid = participants.firstOrNull { it != currentUid } as? String ?: return@launch

                val userDoc = db.collection("users").document(otherUid).get().await()
                _peerInfo.value = PeerInfo(
                    uid = otherUid,
                    name = userDoc.getString("name") ?: "Unknown",
                    avatarUrl = userDoc.getString("avatarUrl") ?: "",
                    skillContext = skillContext
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
        val message = hashMapOf(
            "senderId" to currentUid,
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )
        val convoRef = db.collection("conversations").document(chatId)
        convoRef.collection("messages").add(message)
        convoRef.update(
            mapOf(
                "lastMessage" to text,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }
}