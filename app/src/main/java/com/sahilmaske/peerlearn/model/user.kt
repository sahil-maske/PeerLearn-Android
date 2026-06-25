package com.sahilmaske.peerlearn.model



data class User(
    val uid : String = "",
    val name : String = "",
    val college : String = "",
    val role : String = "",
    val skillsHave : List<String> = emptyList(),
    val skillsWant : List<String> = emptyList(),
    val bio : String = "",
    val avatarUrl : String = "",
    val location : String = "",
    val connection : Int = 0,
    val postCount : Int = 0,
    val helpCount: Int = 0,


)