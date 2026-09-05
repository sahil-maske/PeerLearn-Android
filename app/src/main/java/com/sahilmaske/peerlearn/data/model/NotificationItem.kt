package com.sahilmaske.peerlearn.data.model

enum class NotificationType {
    MILESTONE, DISCUSSION, CONNECTION_REQUEST, SYSTEM
}

data class NotificationItem(
    val id: String = "",
    val type: NotificationType = NotificationType.SYSTEM,
    val title: String = "",       // e.g. "Milestone Reached", "Skill Swap Request"
    val content: String = "",     // main message text
    val timeAgo: String = "",
    val isUnread: Boolean = false,
    val relatedConnectionId: String? = null, // only for CONNECTION_REQUEST
    val fromUserId: String? = null
)