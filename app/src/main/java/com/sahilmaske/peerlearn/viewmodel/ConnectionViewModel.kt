package com.sahilmaske.peerlearn.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.sahilmaske.peerlearn.model.Connection
import com.sahilmaske.peerlearn.model.SwapRequest
import com.sahilmaske.peerlearn.model.User
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
    fun respondToConnection(connectionId: String, accept: Boolean, userA: String, userB: String, requestedBy: String) {
        val newStatus = if (accept) "accepted" else "rejected"

        db.collection("connections")
            .document(connectionId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Log.d("ConnectionVM", "Connection $connectionId updated to $newStatus")
                // FIX: sirf accept hone par hi conversation + icebreaker message banao
                if (accept) {
                    createConversationWithIcebreaker(userA, userB, requestedBy)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ConnectionVM", "Failed to update connection status: $connectionId", e)
            }
    }

    // NEW: connection accept hone ke turant baad call hota hai.
    // - deterministic chatId banata hai (sorted uid pair — ChatViewModel wala hi pattern)
    // - agar conversation doc pehli baar ban raha hai, to naya document banata hai
    // - dono users ke skill data (knowSkill/learnSkill) fetch karke ek PERSONALIZED
    //   icebreaker banata hai, taaki receiver ko dekhte hi context mil jaye aur wo
    //   reply karne ke liye motivate ho — generic "Say hi" se koi reply nahi karta
    private fun createConversationWithIcebreaker(userA: String, userB: String, senderId: String) {
        val chatId = listOf(userA, userB).sorted().joinToString("_")
        val convoRef = db.collection("conversations").document(chatId)

        convoRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    Log.d("ConnectionVM", "Conversation already exists: $chatId, skipping icebreaker")
                    return@addOnSuccessListener
                }

                // dono users ka profile fetch karo taaki skill-based icebreaker bana sakein
                val userADoc = db.collection("users").document(userA).get()
                val userBDoc = db.collection("users").document(userB).get()

                userADoc.addOnSuccessListener { docA ->
                    userBDoc.addOnSuccessListener { docB ->
                        val icebreaker = buildSkillIcebreaker(docA, docB)

                        convoRef.set(
                            hashMapOf(
                                "participants" to listOf(userA, userB),
                                "lastMessage" to icebreaker,
                                "timestamp" to System.currentTimeMillis()
                            )
                        ).addOnFailureListener { e ->
                            Log.e("ConnectionVM", "Failed to create conversation: $chatId", e)
                        }

                        convoRef.collection("messages").add(
                            hashMapOf(
                                "senderId" to senderId, // FIX: "system" ki jagah requestedBy — message ab request-sender ki taraf se dikhega
                                "text" to icebreaker,
                                "timestamp" to System.currentTimeMillis()
                            )
                        ).addOnFailureListener { e ->
                            Log.e("ConnectionVM", "Failed to send icebreaker message: $chatId", e)
                        }
                    }.addOnFailureListener { e ->
                        Log.e("ConnectionVM", "Failed to fetch userB profile: $userB", e)
                    }
                }.addOnFailureListener { e ->
                    Log.e("ConnectionVM", "Failed to fetch userA profile: $userA", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ConnectionVM", "Failed to check conversation existence: $chatId", e)
            }
    }

    // NEW: dono users ke knowSkill/learnSkill fields se ek PSYCHOLOGICALLY-CHARGED
    // icebreaker banata hai — sirf polite question nahi, balki genuine persuasion
    // principles use karta hai jo reply-rate ko real me push karte hain:
    //
    //   1. CURIOSITY GAP — puri baat nahi batate, ek open loop chhodte hain jise
    //      dimag khud close karna chahta hai (Zeigarnik effect)
    //   2. PERSONAL STAKE — receiver ko seedha dikhta hai "isme mera kya fayda hai",
    //      abstract nahi
    //   3. MICRO-COMMITMENT — bada ask nahi ("chat karo"), chhota easy ask
    //      ("ek line likho") — chhota commitment dena psychologically zyada asaan hai
    //   4. SOCIAL PROOF / NORMALIZATION — implicitly signal karta hai ki "yahi
    //      normal next step hai", jisse hesitation kam hoti hai
    //   5. NAME-DROP + DIRECT ADDRESS — apna naam sunte hi dimag automatically
    //      attention deta hai (cocktail party effect)
    private fun buildSkillIcebreaker(
        docA: com.google.firebase.firestore.DocumentSnapshot,
        docB: com.google.firebase.firestore.DocumentSnapshot
    ): String {
        val nameA = docA.getString("name")?.trim()?.takeIf { it.isNotBlank() } ?: "This user"
        val nameB = docB.getString("name")?.trim()?.takeIf { it.isNotBlank() } ?: "This user"
        val knowA = docA.getString("knowSkill")?.trim().orEmpty()
        val learnA = docA.getString("learnSkill")?.trim().orEmpty()
        val knowB = docB.getString("knowSkill")?.trim().orEmpty()
        val learnB = docB.getString("learnSkill")?.trim().orEmpty()

        return when {
            // CASE 1 — Bidirectional perfect match: dono ek dusre ke liye exactly wahi
            // hain jo doosra dhoondh raha tha. Ye framing "destiny/rare luck" angle use
            // karti hai — jitna rare cheez lagti hai, utna zyada log act karte hain
            // (scarcity bias).
            knowA.isNotBlank() && learnB.isNotBlank() && knowA.equals(learnB, ignoreCase = true) &&
                    knowB.isNotBlank() && learnA.isNotBlank() && knowB.equals(learnA, ignoreCase = true) ->
                "This kind of match is rare — $nameA has exactly what $nameB is looking for, and $nameB has exactly what $nameA is looking for. " +
                        "$nameA, drop one question about $knowB and see what happens 👀"

            // CASE 2 — One-directional match: curiosity gap + micro-commitment.
            // "aur bhi bata sakta hai" wala open loop chhoda hai jo curiosity trigger karta hai.
            knowA.isNotBlank() && learnB.isNotBlank() && knowA.equals(learnB, ignoreCase = true) ->
                "$nameB — $nameA already knows $knowA, which is exactly what you've been trying to learn. " +
                        "Most people just ask one small thing first. What's yours?"

            // CASE 3 — Reverse one-directional match
            knowB.isNotBlank() && learnA.isNotBlank() && knowB.equals(learnA, ignoreCase = true) ->
                "$nameA — $nameB already knows $knowB, which is exactly what you've been trying to learn. " +
                        "Most people just ask one small thing first. What's yours?"

            // CASE 4 — Mutual skill swap possible: personal-stake framing, dono taraf se
            // "tumhara fayda" clear, plus normalization ("swap" already common concept
            // in this app so mentioning it signals it's the expected next move).
            knowA.isNotBlank() && knowB.isNotBlank() && learnA.isNotBlank() && learnB.isNotBlank() ->
                "$nameA can teach $knowA and wants $learnA. $nameB can teach $knowB and wants $learnB. Wait — sounds like a swap worth exploring. " +
                        "Who's going to make the first move?"

            // CASE 5 — Only knowSkill on both sides — curiosity + direct name-address
            knowA.isNotBlank() && knowB.isNotBlank() ->
                "$nameA knows $knowA. $nameB knows $knowB. Neither of you has asked the other anything yet — " +
                        "$nameA, what's one thing about $knowB you'd want to know right now?"

            // CASE 6 — Only one side has data — personal stake framed as opportunity cost
            knowA.isNotBlank() ->
                "$nameA already knows $knowA. $nameB, most people wait too long to just ask — what's stopping you?"
            knowB.isNotBlank() ->
                "$nameB already knows $knowB. $nameA, most people wait too long to just ask — what's stopping you?"

            // CASE 7 — No skill data at all — still keep a curiosity + micro-commitment hook
            else -> "You just matched. The people who message first here usually get the best conversations — what's the one skill you're hoping to walk away with?"
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

    // NEW: Block targetUserId.
    //  1. Deletes any existing connection doc between the two (pending OR
    //     accepted) so the blocked user immediately disappears from the
    //     Connect/Pending/Message state.
    //  2. Writes a directional blockedUsers doc so listenBlockStatus (and,
    //     ideally, a Firestore security rule) can check the relationship.
    // NOTE: this does not enforce the block server-side by itself — pair it
    // with a Firestore rule that checks blockedUsers before allowing a new
    // connections/{id} write, otherwise a blocked user's client could still
    // attempt to send a request.
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