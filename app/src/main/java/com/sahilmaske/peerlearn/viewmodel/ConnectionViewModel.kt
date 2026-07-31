package com.sahilmaske.peerlearn.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.sahilmaske.peerlearn.model.Connection
import com.sahilmaske.peerlearn.model.SwapRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectionViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // ---------- STATE ----------

    private val _connectionStatus = MutableStateFlow<String?>(null) // null, pending, accepted, rejected
    val connectionStatus: StateFlow<String?> = _connectionStatus.asStateFlow()

    private val _incomingRequests = MutableStateFlow<List<Connection>>(emptyList())
    val incomingRequests: StateFlow<List<Connection>> = _incomingRequests.asStateFlow()

    private val _swapRequests = MutableStateFlow<List<SwapRequest>>(emptyList())
    val swapRequests: StateFlow<List<SwapRequest>> = _swapRequests.asStateFlow()

    private var connectionListener: ListenerRegistration? = null
    private var incomingListener: ListenerRegistration? = null
    private var swapListener: ListenerRegistration? = null

    // ---------- HELPER ----------

    // Same deterministic pattern jo tune ChatViewModel me use kiya hai
    private fun buildConnectionId(userA: String, userB: String): String {
        return listOf(userA, userB).sorted().joinToString("_")
    }

    // ---------- CONNECTION REQUESTS ----------

    fun sendConnectionRequest(currentUserId: String, targetUserId: String) {
        val connectionId = buildConnectionId(currentUserId, targetUserId)
        val data = hashMapOf(
            "connectionId" to connectionId,
            "userA" to currentUserId,
            "userB" to targetUserId,
            "status" to "pending",
            "requestedBy" to currentUserId,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("connections")
            .document(connectionId)
            .set(data)
    }

    fun respondToConnection(connectionId: String, accept: Boolean) {
        val newStatus = if (accept) "accepted" else "rejected"
        db.collection("connections")
            .document(connectionId)
            .update("status", newStatus)
    }

    fun cancelOrRemoveConnection(connectionId: String) {
        db.collection("connections")
            .document(connectionId)
            .delete()
    }

    // Real-time listener: use this on ProfileScreen to show Connect/Pending/Message button
    fun listenConnectionStatus(currentUserId: String, targetUserId: String) {
        connectionListener?.remove()
        val connectionId = buildConnectionId(currentUserId, targetUserId)

        connectionListener = db.collection("connections")
            .document(connectionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    _connectionStatus.value = null
                    return@addSnapshotListener
                }
                _connectionStatus.value = snapshot.getString("status")
            }
    }

    // Real-time listener: use this on RequestsScreen to show incoming pending requests
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

    fun respondToConnection(connectionId: String, accept: Boolean, userA: String, userB: String) {
        val newStatus = if (accept) "accepted" else "rejected"

        db.collection("connections")
            .document(connectionId)
            .update("status", newStatus)
            .addOnSuccessListener {
                if (accept) {
                    // Increment connection count on BOTH users when accepted
                    val usersRef = db.collection("users")
                    usersRef.document(userA).update("connection", FieldValue.increment(1))
                    usersRef.document(userB).update("connection", FieldValue.increment(1))
                }
            }
    }

    fun cancelOrRemoveConnection(connectionId: String, userA: String, userB: String, wasAccepted: Boolean) {
        db.collection("connections")
            .document(connectionId)
            .delete()
            .addOnSuccessListener {
                if (wasAccepted) {
                    val usersRef = db.collection("users")
                    usersRef.document(userA).update("connection", FieldValue.increment(-1))
                    usersRef.document(userB).update("connection", FieldValue.increment(-1))
                }
            }
    }

    // ---------- CLEANUP ----------

    override fun onCleared() {
        super.onCleared()
        connectionListener?.remove()
        incomingListener?.remove()
        swapListener?.remove()
    }
}