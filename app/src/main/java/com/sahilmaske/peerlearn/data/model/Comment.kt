package com.sahilmaske.peerlearn.data.model


data class Comment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isMarkedHelpful: Boolean = false // NEW: set true when the post owner marks this comment as having helped them
)