package com.sahilmaske.peerlearn.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.sahilmaske.peerlearn.data.model.Connection
import com.sahilmaske.peerlearn.data.model.SwapRequest
import com.sahilmaske.peerlearn.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectionViewModel : ViewModel() {

    private val db by lazy { FirebaseFirestore.getInstance() }

    // ---------- STATE ----------

    private val _connectionStatus = MutableStateFlow<String?>(null) // null, pending, accepted, rejected
    val connectionStatus: StateFlow<String?> = _connectionStatus.asStateFlow()

    private val _activeConnectionId = MutableStateFlow<String?>(null)
    val activeConnectionId: StateFlow<String?> = _activeConnectionId.asStateFlow()

    private val _activeRequestedBy = MutableStateFlow<String?>(null)
    val activeRequestedBy: StateFlow<String?> = _activeRequestedBy.asStateFlow()

    private val _incomingRequests = MutableStateFlow<List<Connection>>(emptyList())
    val incomingRequests: StateFlow<List<Connection>> = _incomingRequests.asStateFlow()

    private val _swapRequests = MutableStateFlow<List<SwapRequest>>(emptyList())
    val swapRequests: StateFlow<List<SwapRequest>> = _swapRequests.asStateFlow()

    private val _senderNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val senderNames: StateFlow<Map<String, String>> = _senderNames.asStateFlow()

    private val _connectionCount = MutableStateFlow(0)
    val connectionCount: StateFlow<Int> = _connectionCount.asStateFlow()
    private var countAsUserA = 0
    private var countAsUserB = 0

    private val _blockStatus = MutableStateFlow("none")
    val blockStatus: StateFlow<String> = _blockStatus.asStateFlow()

    private val _blockedUsers = MutableStateFlow<List<User>>(emptyList())
    val blockedUsers: StateFlow<List<User>> = _blockedUsers.asStateFlow()

    private var connectionListener: ListenerRegistration? = null
    private var incomingListener: ListenerRegistration? = null
    private var swapListener: ListenerRegistration? = null
    private var connectionCountListenerA: ListenerRegistration? = null
    private var connectionCountListenerB: ListenerRegistration? = null

    private var blockedByMeListener: ListenerRegistration? = null
    private var blockedByThemListener: ListenerRegistration? = null
    private var blockedUsersListListener: ListenerRegistration? = null
    private var iBlockedThem = false
    private var theyBlockedMe = false

    // ---------- HELPER ----------

    private fun buildConnectionId(userA: String, userB: String): String {
        return listOf(userA, userB).sorted().joinToString("_")
    }

    private fun buildBlockId(blockerId: String, blockedId: String): String {
        return "${blockerId}_blocks_$blockedId"
    }

    // ---------- CONNECTION REQUESTS ----------

    fun sendConnectionRequest(
        currentUserId: String,
        targetUserId: String,
        matchedSkill: String = "",
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val connectionId = buildConnectionId(currentUserId, targetUserId)
        val data = hashMapOf(
            "connectionId" to connectionId,
            "userA" to currentUserId,
            "userB" to targetUserId,
            "status" to "pending",
            "requestedBy" to currentUserId,
            "matchedSkill" to matchedSkill,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("connections")
            .document(connectionId)
            .set(data)
            .addOnSuccessListener {
                Log.d("ConnectionVM", "Request sent successfully: $connectionId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ConnectionVM", "Failed to send request: $connectionId", e)
                onFailure(e)
            }
    }

    // FIX: ab onSuccess callback chatId return karta hai (accept hua to actual chatId,
    // reject hua to null) — isi se NotificationScreen navigate kar payega chat_conversation
    // screen pe seedha accept ke baad. Pehle koi callback hi nahi tha, isliye UI ko pata
    // hi nahi chalta tha ki kab navigate karna hai.
    fun respondToConnection(
        connectionId: String,
        accept: Boolean,
        userA: String,
        userB: String,
        requestedBy: String,
        matchedSkill: String = "",
        onSuccess: (chatId: String?) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val newStatus = if (accept) "accepted" else "rejected"

        db.collection("connections")
            .document(connectionId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Log.d("ConnectionVM", "Connection $connectionId updated to $newStatus")
                if (accept) {
                    createConversationWithIcebreaker(userA, userB, requestedBy, matchedSkill) { chatId ->
                        onSuccess(chatId)
                    }
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ConnectionVM", "Failed to update connection status: $connectionId", e)
                onFailure(e)
            }
    }

    // FIX: ab onComplete(chatId) call karta hai har path pe — chahe conversation
    // pehle se exist karta ho, chahe naya bana ho, chahe icebreaker message fail
    // ho jaye (conversation doc to ban hi gaya, navigate karna safe hai).
    private fun createConversationWithIcebreaker(
        userA: String,
        userB: String,
        senderId: String,
        skill: String,
        onComplete: (chatId: String) -> Unit = {}
    ) {
        val participants = listOf(userA, userB).sorted()
        val chatId = participants.joinToString("_")
        val convoRef = db.collection("conversations").document(chatId)

        convoRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    Log.d("ConnectionVM", "Conversation already exists: $chatId, skipping icebreaker")
                    onComplete(chatId)
                    return@addOnSuccessListener
                }

                val icebreaker = if (skill.isNotBlank()) {
                    "Hey, I saw your profile, I see you know $skill, can you help me grow in this skill?"
                } else {
                    "Hey, I saw your profile and would love to connect and learn together!"
                }

                val otherId = if (senderId == userA) userB else userA

                convoRef.set(
                    mapOf(
                        "participants" to participants,
                        "lastMessage" to icebreaker,
                        "timestamp" to FieldValue.serverTimestamp(),
                        "unreadCounts" to mapOf(
                            otherId to 1L,
                            senderId to 0L
                        )
                    ),
                    SetOptions.merge()
                ).addOnSuccessListener {
                    convoRef.collection("messages").add(
                        hashMapOf(
                            "senderId" to senderId,
                            "text" to icebreaker,
                            "timestamp" to System.currentTimeMillis()
                        )
                    ).addOnSuccessListener {
                        onComplete(chatId)
                    }.addOnFailureListener { e ->
                        Log.e("ConnectionVM", "Failed to send icebreaker message: $chatId", e)
                        // conversation doc ban chuka hai, navigate to kar sakte hain
                        onComplete(chatId)
                    }
                }.addOnFailureListener { e ->
                    Log.e("ConnectionVM", "Failed to create conversation: $chatId", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ConnectionVM", "Failed to check conversation existence: $chatId", e)
            }
    }

    fun cancelConnectionRequest(
        connectionId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        db.collection("connections")
            .document(connectionId)
            .delete()
            .addOnSuccessListener {
                Log.d("ConnectionVM", "Cancelled pending request: $connectionId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ConnectionVM", "Failed to cancel request: $connectionId", e)
                onFailure(e)
            }
    }

    fun breakConnection(
        connectionId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        db.collection("connections")
            .document(connectionId)
            .delete()
            .addOnSuccessListener {
                Log.d("ConnectionVM", "Connection broken: $connectionId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ConnectionVM", "Failed to break connection: $connectionId", e)
                onFailure(e)
            }
    }

    @Deprecated(
        "Use cancelConnectionRequest or breakConnection for clearer intent",
        ReplaceWith("breakConnection(connectionId)")
    )
    fun cancelOrRemoveConnection(connectionId: String, userA: String, userB: String, wasAccepted: Boolean) {
        db.collection("connections")
            .document(connectionId)
            .delete()
    }

    fun blockUser(
        currentUserId: String,
        targetUserId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val connectionId = buildConnectionId(currentUserId, targetUserId)
        val blockId = buildBlockId(currentUserId, targetUserId)

        val blockData = hashMapOf(
            "blockerId" to currentUserId,
            "blockedId" to targetUserId,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("connections").document(connectionId).delete()
            .addOnCompleteListener {
                db.collection("blockedUsers").document(blockId).set(blockData)
                    .addOnSuccessListener {
                        Log.d("ConnectionVM", "Blocked user: $blockId")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("ConnectionVM", "Failed to block user: $blockId", e)
                        onFailure(e)
                    }
            }
    }

    fun unblockUser(
        currentUserId: String,
        targetUserId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val blockId = buildBlockId(currentUserId, targetUserId)
        db.collection("blockedUsers").document(blockId).delete()
            .addOnSuccessListener {
                Log.d("ConnectionVM", "Unblocked user: $blockId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ConnectionVM", "Failed to unblock user: $blockId", e)
                onFailure(e)
            }
    }

    fun listenBlockStatus(currentUserId: String, targetUserId: String) {
        blockedByMeListener?.remove()
        blockedByThemListener?.remove()

        blockedByMeListener = db.collection("blockedUsers")
            .document(buildBlockId(currentUserId, targetUserId))
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                iBlockedThem = snapshot?.exists() == true
                updateBlockStatus()
            }

        blockedByThemListener = db.collection("blockedUsers")
            .document(buildBlockId(targetUserId, currentUserId))
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                theyBlockedMe = snapshot?.exists() == true
                updateBlockStatus()
            }
    }

    fun listenBlockedUsers(currentUserId: String) {
        blockedUsersListListener?.remove()

        blockedUsersListListener = db.collection("blockedUsers")
            .whereEqualTo("blockerId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val blockedUids = snapshot.documents.mapNotNull { it.getString("blockedId") }
                if (blockedUids.isEmpty()) {
                    _blockedUsers.value = emptyList()
                    return@addSnapshotListener
                }

                val users = mutableListOf<User>()
                var fetchedCount = 0

                blockedUids.forEach { uid ->
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { userDoc ->
                            val user = userDoc.toObject(User::class.java)
                            if (user != null) {
                                users.add(user)
                            }
                            fetchedCount++
                            if (fetchedCount == blockedUids.size) {
                                _blockedUsers.value = users
                            }
                        }
                        .addOnFailureListener {
                            fetchedCount++
                            if (fetchedCount == blockedUids.size) {
                                _blockedUsers.value = users
                            }
                        }
                }
            }
    }

    private fun updateBlockStatus() {
        _blockStatus.value = when {
            iBlockedThem && theyBlockedMe -> "mutual"
            iBlockedThem -> "blockedByMe"
            theyBlockedMe -> "blockedByThem"
            else -> "none"
        }
    }

    fun listenConnectionCount(uid: String) {
        connectionCountListenerA?.remove()
        connectionCountListenerB?.remove()

        connectionCountListenerA = db.collection("connections")
            .whereEqualTo("userA", uid)
            .whereEqualTo("status", "accepted")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                countAsUserA = snapshot.size()
                _connectionCount.value = countAsUserA + countAsUserB
            }

        connectionCountListenerB = db.collection("connections")
            .whereEqualTo("userB", uid)
            .whereEqualTo("status", "accepted")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                countAsUserB = snapshot.size()
                _connectionCount.value = countAsUserA + countAsUserB
            }
    }

    fun listenConnectionStatus(currentUserId: String, targetUserId: String) {
        connectionListener?.remove()
        val connectionId = buildConnectionId(currentUserId, targetUserId)

        connectionListener = db.collection("connections")
            .document(connectionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    _connectionStatus.value = null
                    _activeConnectionId.value = null
                    _activeRequestedBy.value = null
                    return@addSnapshotListener
                }
                _connectionStatus.value = snapshot.getString("status")
                _activeConnectionId.value = snapshot.id
                _activeRequestedBy.value = snapshot.getString("requestedBy")
            }
    }

    fun listenIncomingRequests(currentUserId: String) {
        incomingListener?.remove()

        incomingListener = db.collection("connections")
            .whereEqualTo("status", "pending")
            .whereEqualTo("userB", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Connection::class.java)
                }
                _incomingRequests.value = list
            }
    }

    fun fetchSenderNames(uids: List<String>) {
        val missing = uids.filter { it.isNotBlank() && it !in _senderNames.value.keys }
        if (missing.isEmpty()) return

        missing.forEach { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name") ?: "Unknown User"
                    _senderNames.value = _senderNames.value + (uid to name)
                }
                .addOnFailureListener {
                    _senderNames.value = _senderNames.value + (uid to "Unknown User")
                }
        }
    }

    // ---------- SWAP REQUESTS ----------

    fun sendSwapRequest(
        fromUser: String,
        toUser: String,
        offeredSkill: String,
        wantedSkill: String
    ) {
        val swapId = db.collection("swapRequests").document().id
        val data = hashMapOf(
            "swapId" to swapId,
            "fromUser" to fromUser,
            "toUser" to toUser,
            "offeredSkill" to offeredSkill,
            "wantedSkill" to wantedSkill,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("swapRequests")
            .document(swapId)
            .set(data)
    }

    fun respondToSwapRequest(swapId: String, accept: Boolean) {
        val newStatus = if (accept) "accepted" else "rejected"
        db.collection("swapRequests")
            .document(swapId)
            .update("status", newStatus)
    }

    fun listenSwapRequests(currentUserId: String) {
        swapListener?.remove()

        swapListener = db.collection("swapRequests")
            .whereEqualTo("toUser", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(SwapRequest::class.java)
                }
                _swapRequests.value = list
            }
    }

    // ---------- CLEANUP ----------

    override fun onCleared() {
        super.onCleared()
        connectionListener?.remove()
        incomingListener?.remove()
        swapListener?.remove()
        connectionCountListenerA?.remove()
        connectionCountListenerB?.remove()
        blockedByMeListener?.remove()
        blockedByThemListener?.remove()
        blockedUsersListListener?.remove()
    }
}