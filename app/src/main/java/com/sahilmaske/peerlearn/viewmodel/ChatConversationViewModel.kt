package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.sahilmaske.peerlearn.data.model.Message
import com.sahilmaske.peerlearn.data.model.User
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

class ChatConversationViewModel(private val chatId: String) : ViewModel(

) {

    private val db = Firebase.firestore
    val currentUid: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _peerInfo = MutableStateFlow(PeerInfo())
    val peerInfo: StateFlow<PeerInfo> = _peerInfo

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _otherUserPresence = MutableStateFlow<User?>(null)
    val otherUserPresence: StateFlow<User?> = _otherUserPresence

    private var presenceListener: ListenerRegistration? = null

    init {
        loadConversationAndPeer()
        listenToMessages()
        markAsRead() // NEW: screen khulte hi apna unread count 0 kar do
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

                listenToPeerPresence(otherUid)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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

        // NEW: peer ka unread count +1 badhao, apna 0 pe rakho (kyunki hum khud dekh rahe hain)
        convoRef.set(
            mapOf(
                "participants" to listOf(currentUid, peerUid),
                "lastMessage" to text,
                "timestamp" to System.currentTimeMillis(),
                "unreadCounts" to mapOf(
                    peerUid to FieldValue.increment(1),
                    currentUid to 0L
                )
            ),
            SetOptions.merge()
        )
    }

    // NEW: is chat ko khola matlab maine padh liya — apna unread count 0 kar do
    private fun markAsRead() {
        if (currentUid.isBlank()) return
        db.collection("conversations").document(chatId)
            .update("unreadCounts.$currentUid", 0L)
            .addOnFailureListener {
                // document abhi tak bana hi nahi (pehla message kabhi bheja hi nahi) — ignore kar sakte hain
            }
    }

    override fun onCleared() {
        super.onCleared()
        presenceListener?.remove()
    }
}