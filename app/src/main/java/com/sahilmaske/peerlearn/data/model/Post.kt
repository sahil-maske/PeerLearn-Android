package com.sahilmaske.peerlearn.data.model



data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val heading: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val intent: String = "teach",
    val skill: String = "",
    val postType: String = "text",

    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val commentCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val timeAgo: String = ""
)