package com.sahilmaske.peerlearn.data.model

import java.security.Timestamp

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
