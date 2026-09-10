package com.sahilmaske.peerlearn.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sahilmaske.peerlearn.data.model.SkillMatchModel
import com.sahilmaske.peerlearn.data.model.User
import kotlinx.coroutines.tasks.await

class SkillMatchRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Step 1: Saare users Firestore se fetch kar (current user ko chhodke)
    suspend fun getRuleBasedMatches(currentUser: User): List<SkillMatchModel> {
        val snapshot = firestore.collection("users")
            .get()
            .await()

        val allUsers = snapshot.toObjects(User::class.java)

        // Step 2: Current user ko list se nikaal de (khud se match nahi karna)
        val otherUsers = allUsers.filter { it.uid != currentUser.uid }

        // Step 3: Har user ke liye match score calculate kar
        val matches = otherUsers.mapNotNull { otherUser ->
            calculateMatch(currentUser, otherUser)
        }

        // Step 4: Best match sabse upar dikhe, isliye score se sort kar
        return matches.sortedByDescending { it.matchScore }
    }

    // Ye function ek user pair ka match score nikalta hai
    private fun calculateMatch(currentUser: User, otherUser: User): SkillMatchModel? {
        // currentUser jo seekhna chahta hai (learningSkills)
        // otherUser jo sikha sakta hai (knownSkills)
        // dono lists ko lowercase kar taaki "React" aur "react" match ho jaye
        val currentWants = currentUser.learningSkills.map { it.lowercase() }
        val otherKnows = otherUser.knownSkills.map { it.lowercase() }

        // Kitne skills overlap kar rahe hain
        val overlap = currentWants.intersect(otherKnows.toSet())

        // Agar koi overlap nahi, toh match hi nahi banega
        if (overlap.isEmpty()) return null

        // Score: kitne % learning skills match hue
        val score = overlap.size.toFloat() / currentWants.size.toFloat()

        // UI ko dikhane ke liye readable reason bana
        val reason = "Can teach you: ${overlap.joinToString(", ")}"

        return SkillMatchModel(
            matchedUserId = otherUser.uid,
            matchedUserName = otherUser.name,
            matchScore = score,
            matchReason = reason
        )
    }
}