package com.sahilmaske.peerlearn.ui.Settings


import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Poori app ke liye ek hi jagah user ka online/offline status track karta hai.
 *
 * - onStart() -> jab app foreground mein aaye (koi bhi screen open ho) -> isOnline = true
 * - onStop()  -> jab app background mein jaye / band ho -> isOnline = false + lastSeen = abhi ka time
 *
 * Ye ProcessLifecycleOwner ke saath register hota hai (MainActivity ya Application class mein),
 * isliye har individual screen mein alag se presence code likhne ki zaroorat nahi.
 */
class PresenceManager : DefaultLifecycleObserver {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onStart(owner: LifecycleOwner) {
        setOnlineStatus(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        setOnlineStatus(false)
    }

    private fun setOnlineStatus(isOnline: Boolean) {
        val uid = auth.currentUser?.uid ?: return

        val updates = if (isOnline) {
            mapOf("isOnline" to true)
        } else {
            mapOf(
                "isOnline" to false,
                "lastSeen" to System.currentTimeMillis()
            )
        }

        db.collection("users").document(uid)
            .update(updates)
            .addOnFailureListener { e ->
                Log.e("PresenceManager", "Failed to update presence", e)
            }
    }
}