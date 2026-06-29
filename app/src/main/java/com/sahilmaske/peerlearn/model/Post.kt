package com.sahilmaske.peerlearn.model

data class Post(
    val id: String = "",
    val heading: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val uploadTime: Long = 0L
)