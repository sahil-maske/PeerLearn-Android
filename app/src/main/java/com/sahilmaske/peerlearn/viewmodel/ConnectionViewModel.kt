package com.sahilmaske.peerlearn.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    // NEW: id of the current connections/{id} doc for the pair being watched by
    // listenConnectionStatus, plus who originally sent the request. UI needs both
    // to cancel a pending request or break/block an accepted one without an
    // extra Firestore read.
    private val _activeConnectionId = MutableStateFlow<String?>(null)
    val activeConnectionId: StateFlow<String?> = _activeConnectionId.asStateFlow()

    private val _activeRequestedBy = MutableStateFlow<String?>(null)
    val activeRequestedBy: StateFlow<String?> = _activeRequestedBy.asStateFlow()

    private val _incomingRequests = MutableStateFlow<List<Connection>>(emptyList())
    val incomingRequests: StateFlow<List<Connection>> = _incomingRequests.asStateFlow()

    private val _swapRequests = MutableStateFlow<List<SwapRequest>>(emptyList())
    val swapRequests: StateFlow<List<SwapRequest>> = _swapRequests.asStateFlow()

    // NEW: uid -> display name, used to show real sender name on NotificationScreen
    // Assumes users/{uid} document has a "name" field. Change getString("name")
    // below if your field is called something else (e.g. "fullName", "username").
    private val _senderNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val senderNames: StateFlow<Map<String, String>> = _senderNames.asStateFlow()

    // NEW: live connection count, calculated from the actual "connections" collection
    // instead of a manually incremented field — so old/pre-existing accepted
    // connections are counted correctly too, and it self-corrects if a connection
    // is ever removed.
    private val _connectionCount = MutableStateFlow(0)
    val connectionCount: StateFlow<Int> = _connectionCount.asStateFlow()
    private var countAsUserA = 0
    private var countAsUserB = 0

    // NEW: block relationship for whichever pair listenBlockStatus is currently
    // watching.
    //   "none"          -> neither side has blocked the other
    //   "blockedByMe"   -> current user blocked the target
    //   "blockedByThem" -> target blocked the current user
    //   "mutual"        -> both directions blocked (defensive case)
    private val _blockStatus = MutableStateFlow("none")
    val blockStatus: StateFlow<String> = _blockStatus.asStateFlow()

    private val _blockedUsers = MutableStateFlow<List<User>>(emptyList())
    val blockedUsers: StateFlow<List<User>> = _blockedUsers.asStateFlow()

    private var connectionListener: ListenerRegistration? = null
    private var incomingListener: ListenerRegistration? = null
    private var swapListener: ListenerRegistration? = null
    private var connectionCountListenerA: ListenerRegistration? = null
    private var connectionCountListenerB: ListenerRegistration? = null

    // NEW: two separate listeners because Firestore can't OR across two
    // different document paths in one query — one watches "did I block them",
    // the other watches "did they block me".
    private var blockedByMeListener: ListenerRegistration? = null
    private var blockedByThemListener: ListenerRegistration? = null
    private var blockedUsersListListener: ListenerRegistration? = null
    private var iBlockedThem = false
    private var theyBlockedMe = false

    // ---------- HELPER ----------

    // Same deterministic pattern jo tune ChatViewModel me use kiya hai
    private fun buildConnectionId(userA: String, userB: String): String {
        return listOf(userA, userB).sorted().joinToString("_")
    }

    // NEW: blockedUsers doc id. Unlike connections, a block is DIRECTIONAL
    // (A blocking B is not the same relationship as B blocking A), so we can't
    // sort the pair the way buildConnectionId does — direction has to be baked
    // into the id itself.
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

    // Accept/deny an incoming request.
    // FIX: pehle sirf status update hota tha, koi success/failure listener nahi tha,
    // isliye accept hone ke baad conversation/icebreaker message trigger hi nahi hota tha.
    // requestedBy: jisne originally connection request bheji thi — icebreaker message
    // usi user ki taraf se bheja jayega (senderId = requestedBy), taaki chat me ye
    // uski side se (right-aligned uske liye) dikhe, "system" ki taraf se nahi.
    fun respondToConnection(connectionId: String, accept: Boolean, userA: String, userB: String, requestedBy: String, matchedSkill: String = "") {
        val newStatus = if (accept) "accepted" else "rejected"

        db.collection("connections")
            .document(connectionId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Log.d("ConnectionVM", "Connection $connectionId updated to $newStatus")
                // FIX: sirf accept hone par hi conversation + icebreaker message banao
                if (accept) {
                    createConversationWithIcebreaker(userA, userB, requestedBy, matchedSkill)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ConnectionVM", "Failed to update connection status: $connectionId", e)
            }
    }

    // NEW: connection accept hone ke turant baad call hota hai.
    // - deterministic chatId banata hai (sorted uid pair — ChatViewModel wala hi pattern)
    // - agar conversation doc pehli baar ban raha hai, to naya document banata hai
    // - senderId (requestedBy) ki taraf se icebreaker message bhejta hai
    private fun createConversationWithIcebreaker(userA: String, userB: String, senderId: String, skill: String) {
        val chatId = listOf(userA, userB).sorted().joinToString("_")
        val convoRef = db.collection("conversations").document(chatId)

        convoRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    Log.d("ConnectionVM", "Conversation already exists: $chatId, skipping icebreaker")
                    return@addOnSuccessListener
                }

                val icebreaker = if (skill.isNotBlank()) {
                    "Hey, I saw your profile — I see you know $skill. Can you help me grow in this skill?"
                } else {
                    "Hey, I saw your profile and would love to connect and learn together!"
                }

                convoRef.set(
                    hashMapOf(
                        "participants" to listOf(userA, userB),
                        "lastMessage" to icebreaker,
                        "timestamp" to System.currentTimeMillis(),
                        "unreadCounts" to mapOf(
                            (if (senderId == userA) userB else userA) to 1L,
                            senderId to 0L
                        )
                    )
                ).addOnSuccessListener {
                    convoRef.collection("messages").add(
                        hashMapOf(
                            "senderId" to senderId,
                            "text" to icebreaker,
                            "timestamp" to System.currentTimeMillis()
                        )
                    ).addOnFailureListener { e ->
                        Log.e("ConnectionVM", "Failed to send icebreaker message: $chatId", e)
                    }
                }.addOnFailureListener { e ->
                    Log.e("ConnectionVM", "Failed to create conversation: $chatId", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ConnectionVM", "Failed to check conversation existence: $chatId", e)
            }
    }

    // NEW: Cancel a request YOU sent while it's still pending. Same underlying
    // delete as breakConnection — kept as a separate, clearly-named function so
    // the UI intent stays obvious at the call site.
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

    // NEW: Break an existing ACCEPTED connection. Deliberately does NOT touch
    // the conversations/messages doc — chat history stays intact, this only
    // removes the connection so "Connect" can be sent again later and the
    // profile button flips back to the swap-to-connect state.
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

    // Kept so any existing call sites don't break — prefer cancelConnectionRequest
    // or breakConnection going forward since they make the intent explicit.
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
                // Proceed regardless of whether a connection doc existed to delete —
                // blocking should still succeed even if there was never a connection.
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

    // NEW: Reverse of blockUser. Does not restore any connection that existed
    // before the block — the other user would need to send a fresh request.
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

    // NEW: Real-time — watches BOTH directions of the block relationship
    // between currentUserId and targetUserId. Call this alongside
    // listenConnectionStatus whenever ProfileScreen opens someone else's profile.
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

    // NEW: listen to the list of users blocked by currentUserId.
    // Fetches full User objects for each block relationship so the UI
    // can show names/avatars in the Blocked Users list.
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

                // Fetch details for each blocked UID
                // In a production app with many blocks, you might want to batch this
                // or use a different strategy, but for a standard blocked list this is fine.
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

    // NEW: call this for whichever uid's profile is being shown (own or someone else's).
    // A connection can have this user as either userA or userB, so we need two
    // listeners whose counts get summed together live.
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

    // Real-time listener: use this on ProfileScreen to show Connect/Pending/Message button
    // UPDATED: also populates activeConnectionId + activeRequestedBy so the UI can
    // cancel/break/block without an extra query.
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

    // Real-time listener: use this on NotificationScreen to show incoming pending requests
    fun listenIncomingRequests(currentUserId: String) {
        incomingListener?.remove()

        incomingListener = db.collection("connections")
            .whereEqualTo("status", "pending")
            .whereEqualTo("userB", currentUserId) // requests jo currentUser ko mile hain
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Connection::class.java)
                }
                _incomingRequests.value = list
            }
    }

    // NEW: fetch display names for a list of sender uids, skips ones already cached.
    // Call this whenever incomingRequests updates (see NotificationScreen).
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

    // Real-time listener: use this on RequestsScreen (Swaps tab)
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