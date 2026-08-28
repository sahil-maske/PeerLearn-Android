package com.sahilmaske.peerlearn.ui.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Step 4 me yaha token Firestore me save karenge
        Log.d("FCMService", "New token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // TODO: yaha notification dikhane ka code aayega (Step 5)
        Log.d("FCMService", "Message received: ${message.notification?.title}")
    }
}