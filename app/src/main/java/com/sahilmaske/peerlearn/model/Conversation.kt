package com.sahilmaske.peerlearn.model

data class Conversation(
    val id: String,
    val otherUid: String = "",
    val name: String,
    val avatarUrl: String,
    val lastMessage: String,
    val time: String,
    val isOnline: Boolean = false,
    val unreadCount: Int = 0,
    val hasUnread: Boolean = false
)