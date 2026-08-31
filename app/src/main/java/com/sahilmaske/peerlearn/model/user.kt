package com.sahilmaske.peerlearn.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid : String = "",
    val name : String = "",
    val college : String = "",
    val role : String = "",
    val knownSkills : List<String> = emptyList(),
    val learningSkills : List<String> = emptyList(),
    val about : String = "",
    val avatarUrl : String = "",
    val location : String = "",
    val connection : Int = 0,
    val postCount : Int = 0,
    val helpCount: Int = 0,
    val phoneNumber : String = "",
    val email : String = "",
    @get:PropertyName("isEmailVerified")
    val isEmailVerified : Boolean = false,
    val linkedAccounts: Map<String, String> = emptyMap(),
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,        // ya Timestamp, jo bhi convention already use ho raha hai tumhare model mein
    val hideOnlineStatus: Boolean = false,
    val showPhoneNumber: Boolean = false,
    val profileVisibility: String = "Everyone"
//    val instagramURL : String ="",
//    val linkedInURL : String ="",
//    val gitHubURL : String ="",
//    val gitHubURL : String ="",
) {
    companion object {
        const val STATUS_VERIFIED = "VERIFIED"
        const val STATUS_NOT_VERIFIED = "EMAIL NOT VERIFIED"
    }
}
