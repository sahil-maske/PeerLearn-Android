package com.sahilmaske.peerlearn.data.model

data class SkillMatchModel(
    val matchedUserId: String,
    val matchedUserName: String,
    val matchScore: Float,
    val matchReason: String
)
