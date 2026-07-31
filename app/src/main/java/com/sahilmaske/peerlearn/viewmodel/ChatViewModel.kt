package com.sahilmaske.peerlearn.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahilmaske.peerlearn.model.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel : ViewModel() {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        // TODO: replace with Firestore fetch
        _conversations.value = listOf(
            Conversation("1", "Marcus Chen", "", "Can we meet at the library...", "14:32", 2, true),
            Conversation("2", "Elara Vance", "", "That Python trick worked per...", "Yesterday"),
            Conversation("3", "UI/UX Design Study Group", "", "David: I've uploaded the...", "Wed")
        )
    }
}