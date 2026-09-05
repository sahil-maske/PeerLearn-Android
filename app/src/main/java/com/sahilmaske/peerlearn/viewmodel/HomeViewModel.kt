package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import com.sahilmaske.peerlearn.data.model.Post
import com.sahilmaske.peerlearn.data.model.PeerSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val _greetingName = MutableStateFlow("Sahil")
    val greetingName: StateFlow<String> = _greetingName

    private val _suggestions = MutableStateFlow<List<PeerSuggestion>>(emptyList())
    val suggestions: StateFlow<List<PeerSuggestion>> = _suggestions

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    init {
        loadDummyData()
    }

    private fun loadDummyData() {
        _suggestions.value = listOf(
            PeerSuggestion(id = "1", name = "Riya Sharma", knowSkill = "UI/UX • Figma"),
            PeerSuggestion(id = "2", name = "Aman Verma", knowSkill = "Kotlin • Backend"),
            PeerSuggestion(id = "3", name = "Neha Patil", knowSkill = "Python • ML")
        )
        _posts.value = listOf(
            Post(id = "p1", authorName = "Riya Sharma", description = "Just finished my first Compose animation! 🎉", likeCount = 12, commentCount = 3, timeAgo = "1h"),
            Post(id = "p2", authorName = "Aman Verma", description = "Anyone up for a DSA study group this week?", likeCount = 8, commentCount = 5, timeAgo = "3h"),
            Post(id = "p3", authorName = "Neha Patil", description = "Sharing my ML project notes soon, stay tuned!", likeCount = 20, commentCount = 7, timeAgo = "5h")
        )
    }
}