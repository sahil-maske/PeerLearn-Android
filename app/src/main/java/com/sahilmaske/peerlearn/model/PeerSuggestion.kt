package com.sahilmaske.peerlearn.model

data class PeerSuggestion(
    val id: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val knowSkill: String = "",
    val learnSkill: String = "",
    val matchPercentage: Int = 0
)