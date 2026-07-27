package com.sahilmaske.peerlearn.model


data class Comment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)