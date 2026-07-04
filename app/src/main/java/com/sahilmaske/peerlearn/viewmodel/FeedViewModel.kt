package com.sahilmaske.peerlearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sahilmaske.peerlearn.model.PeerSuggestion
import com.sahilmaske.peerlearn.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FeedViewModel(
    private val profileViewModel: ProfileViewModel // ya shared repository
) : ViewModel() {

    companion object {
        fun provideFactory(profileViewModel: ProfileViewModel): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(profileViewModel) as T
            }
        }
    }

    val greetingName: StateFlow<String> = profileViewModel.userProfile
        .map { it?.name ?: "User" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "User")

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
