package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
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

                // Doc pehle se exist karta hai (participants field milegi)
                val existingParticipants = convoDoc.get("participants") as? List<*>
                val skillContext = convoDoc.getString("skillContext") ?: ""

                val otherUid = if (existingParticipants != null) {
                    // Case 1: conversation doc already ban chuka hai
                    existingParticipants.firstOrNull { it != currentUid } as? String
                } else {
                    // Case 2: doc abhi tak exist hi nahi karta (naya user, pehla message abhi tak nahi bheja)
                    // chatId khud deterministic hai: "${uid1}_${uid2}" (sorted) -> usi se doosra uid nikal lo
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

        val peerUid = _peerInfo.value.uid
        if (peerUid.isBlank()) return // peer resolve nahi hua abhi tak, message mat bhejo

        val message = hashMapOf(
            "senderId" to currentUid,
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )
        val convoRef = db.collection("conversations").document(chatId)
        convoRef.collection("messages").add(message)

        // FIX: .update() ki jagah .set() + merge — agar doc exist nahi karta to
        // "participants" field ke saath naya bana dega, agar exist karta hai to
        // sirf ye fields update karega (poora doc overwrite nahi hoga)
        convoRef.set(
            mapOf(
                "participants" to listOf(currentUid, peerUid),
                "lastMessage" to text,
                "timestamp" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        )
    }
}