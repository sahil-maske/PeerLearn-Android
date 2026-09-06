package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.sahilmaske.peerlearn.data.model.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class ChatViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount.asStateFlow()

    // otherUid -> presence listener, taaki duplicate listener na lage
    private val presenceListeners = mutableMapOf<String, ListenerRegistration>()
    private var convoListener: ListenerRegistration? = null
    private var updateJob: Job? = null
    private var authListener: FirebaseAuth.AuthStateListener? = null

    init {
        // Listen to auth state changes to ensure we have a valid UID for the query
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (!uid.isNullOrBlank()) {
                listenToConversations(uid)
            } else {
                _conversations.value = emptyList()
                _totalUnreadCount.value = 0
                convoListener?.remove()
            }
        }
        auth.addAuthStateListener(authListener!!)
    }

    private fun listenToConversations(uid: String) {
        convoListener?.remove()
        convoListener = db.collection("conversations")
            .whereArrayContains("participants", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val docs = snapshot.documents

                updateJob?.cancel()
                updateJob = viewModelScope.launch {
                    // Parallel resolve for efficiency (real-time performance)
                    val resolvedList = docs.map { doc ->
                        async {
                            try {
                                val participants = doc.get("participants") as? List<*> ?: return@async null
                                val otherUid = participants.firstOrNull { it != uid } as? String
                                    ?: return@async null

                                val userDoc = db.collection("users").document(otherUid).get().await()
                                val name = userDoc.getString("name") ?: "Unknown"
                                val avatarUrl = userDoc.getString("avatarUrl") ?: ""

                                val hideOnlineStatus = userDoc.getBoolean("hideOnlineStatus") ?: false
                                val rawIsOnline = userDoc.getBoolean("isOnline") ?: false
                                val isOnline = rawIsOnline && !hideOnlineStatus

                                val lastMessage = doc.getString("lastMessage") ?: ""
                                
                                // handle both Long and Timestamp; use current time if pending (null)
                                // to ensure new conversations appear at the top immediately.
                                val timestamp = when (val t = doc.get("timestamp")) {
                                    is Long -> t
                                    is Timestamp -> t.toDate().time
                                    else -> System.currentTimeMillis()
                                }

                                // unreadCounts map se apna number nikalo
                                val unreadMap = doc.get("unreadCounts") as? Map<*, *>
                                val unreadCount = (unreadMap?.get(uid) as? Long)?.toInt() ?: 0

                                Conversation(
                                    id = doc.id,
                                    otherUid = otherUid,
                                    name = name,
                                    avatarUrl = avatarUrl,
                                    lastMessage = lastMessage,
                                    time = formatTime(timestamp),
                                    isOnline = isOnline,
                                    unreadCount = unreadCount,
                                    hasUnread = unreadCount > 0
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }
                    }.mapNotNull { it.await() }

                    _conversations.value = resolvedList
                    _totalUnreadCount.value = resolvedList.sumOf { it.unreadCount }

                    resolvedList.forEach { convo ->
                        attachPresenceListener(convo.otherUid)
                    }
                }
            }
    }

    private fun attachPresenceListener(otherUid: String) {
        if (presenceListeners.containsKey(otherUid)) return

        val listener = db.collection("users").document(otherUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val hideOnlineStatus = snapshot.getBoolean("hideOnlineStatus") ?: false
                val rawIsOnline = snapshot.getBoolean("isOnline") ?: false
                val isOnline = rawIsOnline && !hideOnlineStatus

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
        authListener?.let { auth.removeAuthStateListener(it) }
        convoListener?.remove()
        presenceListeners.values.forEach { it.remove() }
        presenceListeners.clear()
        updateJob?.cancel()
    }
}