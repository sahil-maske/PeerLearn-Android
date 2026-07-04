package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import com.sahilmaske.peerlearn.model.Post
import com.sahilmaske.peerlearn.model.PeerSuggestion
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
            PeerSuggestion("1", "Riya Sharma", "", "UI/UX • Figma"),
            PeerSuggestion("2", "Aman Verma", "", "Kotlin • Backend"),
            PeerSuggestion("3", "Neha Patil", "", "Python • ML")
        )
        _posts.value = listOf(
            Post("p1", "Riya Sharma", "", "Just finished my first Compose animation! 🎉", likeCount = 12, commentCount = 3, timeAgo = "1h"),
            Post("p2", "Aman Verma", "", "Anyone up for a DSA study group this week?", likeCount = 8, commentCount = 5, timeAgo = "3h"),
            Post("p3", "Neha Patil", "", "Sharing my ML project notes soon, stay tuned!", likeCount = 20, commentCount = 7, timeAgo = "5h")
        )
    }
}