package com.sahilmaske.peerlearn.model

data class Post(
    val id: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val content: String = "",
    val heading: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val timeAgo: String = "2h"
)