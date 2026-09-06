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
    private val auth = FirebaseAuth.getInstance()
    private val currentUid: String get() = auth.currentUser?.uid ?: ""

    private val _peerInfo = MutableStateFlow(PeerInfo())
    val peerInfo: StateFlow<PeerInfo> = _peerInfo

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _otherUserPresence = MutableStateFlow<User?>(null)
    val otherUserPresence: StateFlow<User?> = _otherUserPresence

    private var messagesListener: ListenerRegistration? = null
    private var presenceListener: ListenerRegistration? = null

    init {
        loadConversationAndPeer()
        listenToMessages()
        markAsRead() // NEW: screen khulte hi apna unread count 0 kar do
    }

    private fun loadConversationAndPeer() {
        val uid = currentUid
        viewModelScope.launch {
            try {
                val convoDoc = db.collection("conversations").document(chatId).get().await()

                val existingParticipants = convoDoc.get("participants") as? List<*>
                val skillContext = convoDoc.getString("skillContext") ?: ""

                val otherUid = if (existingParticipants != null) {
                    existingParticipants.firstOrNull { it != uid } as? String
                } else {
                    chatId.split("_").firstOrNull { it != uid }
                }

                if (otherUid.isNullOrBlank() || otherUid == uid) {
                    // Fallback parse logic if uid was empty during parse
                    val resolvedOther = chatId.split("_").firstOrNull { it != uid }
                    if (resolvedOther.isNullOrBlank()) return@launch
                    
                    val userDoc = db.collection("users").document(resolvedOther).get().await()
                    _peerInfo.value = PeerInfo(
                        uid = resolvedOther,
                        name = userDoc.getString("name") ?: "Unknown",
                        avatarUrl = userDoc.getString("avatarUrl") ?: "",
                        skillContext = skillContext
                    )
                    listenToPeerPresence(resolvedOther)
                    return@launch
                }

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
        messagesListener?.remove()
        messagesListener = db.collection("conversations").document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _messages.value = snapshot.toObjects(Message::class.java)
            }
    }

    fun sendMessage(text: String) {
        val uid = currentUid
        if (text.isBlank() || uid.isBlank()) return

        val peerUid = _peerInfo.value.uid
        if (peerUid.isBlank() || peerUid == uid) return

        val timestampValue = System.currentTimeMillis()
        
        val message = hashMapOf(
            "senderId" to uid,
            "text" to text,
            "timestamp" to timestampValue
        )
        val convoRef = db.collection("conversations").document(chatId)
        convoRef.collection("messages").add(message)

        // NEW: use FieldValue.serverTimestamp() for the main doc to ensure perfect 
        // global sorting for both users. unread counts are also updated in the same call.
        convoRef.set(
            mapOf(
                "participants" to listOf(uid, peerUid).sorted(), // Ensure deterministic order
                "lastMessage" to text,
                "timestamp" to FieldValue.serverTimestamp(),
                "unreadCounts" to mapOf(
                    peerUid to FieldValue.increment(1),
                    uid to 0L
                )
            ),
            SetOptions.merge()
        )
    }

    // NEW: is chat ko khola matlab maine padh liya — apna unread count 0 kar do
    private fun markAsRead() {
        val uid = currentUid
        if (uid.isBlank()) return
        db.collection("conversations").document(chatId)
            .update("unreadCounts.$uid", 0L)
            .addOnFailureListener {
                // document abhi tak bana hi nahi (pehla message kabhi bheja hi nahi) — ignore kar sakte hain
            }
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
        presenceListener?.remove()
    }
}