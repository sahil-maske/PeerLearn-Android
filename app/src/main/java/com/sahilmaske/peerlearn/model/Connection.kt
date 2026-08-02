package com.sahilmaske.peerlearn.model

import com.google.firebase.Timestamp

data class Connection(
    val connectionId: String = "",
    val userA: String = "",
    val userB: String = "",
    val status: String = "pending", // pending, accepted, rejected
    val requestedBy: String = "",
    val createdAt: Timestamp? = null // FIX: was Long, but Firestore stores FieldValue.serverTimestamp() as a Timestamp — mismatch caused a crash on toObject()
)

data class SwapRequest(
    val swapId: String = "",
    val fromUser: String = "",
    val toUser: String = "",
    val offeredSkill: String = "",
    val wantedSkill: String = "",
    val status: String = "pending",
    val createdAt: Timestamp? = null // FIX: same reason as above
)