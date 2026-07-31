package com.sahilmaske.peerlearn.model

data class Connection(
    val connectionId: String = "",
    val userA: String = "",
    val userB: String = "",
    val status: String = "pending", // pending, accepted, rejected
    val requestedBy: String = "",
    val createdAt: Long = 0L
)

data class SwapRequest(
    val swapId: String = "",
    val fromUser: String = "",
    val toUser: String = "",
    val offeredSkill: String = "",
    val wantedSkill: String = "",
    val status: String = "pending",
    val createdAt: Long = 0L
)