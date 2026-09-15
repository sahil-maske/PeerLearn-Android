package com.sahilmaske.peerlearn.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sahilmaske.peerlearn.API.Content
import com.sahilmaske.peerlearn.API.EmbeddingRequest
import com.sahilmaske.peerlearn.API.Part
import com.sahilmaske.peerlearn.API.RetrofitClient
import com.sahilmaske.peerlearn.BuildConfig
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

    // UPDATED: Ab ye function exact keyword match (rule-based) aur
    // semantic similarity (AI embeddings) dono ko combine karta hai
    private fun calculateMatch(currentUser: User, otherUser: User): SkillMatchModel? {
        val currentWants = currentUser.learningSkills.map { it.lowercase() }
        val otherKnows = otherUser.knownSkills.map { it.lowercase() }
        val overlap = currentWants.intersect(otherKnows.toSet())

        val ruleScore = if (currentWants.isEmpty()) 0f
        else overlap.size.toFloat() / currentWants.size.toFloat()

        // FIX: current user ki "learningSkills" embedding VS
        // other user ki "knownSkills" embedding compare kar rahe hain —
        // ye specific aur accurate hai, poore profile ki jagah
        val semanticScore = cosineSimilarity(
            currentUser.learningSkillsEmbedding,
            otherUser.knownSkillsEmbedding
        )

        val finalScore = maxOf(ruleScore, semanticScore * 0.9f)

        if (ruleScore == 0f && semanticScore < 0.75f) return null

        val reason = if (overlap.isNotEmpty()) {
            "Can teach you: ${overlap.joinToString(", ")}"
        } else {
            "Skills related to what you're learning"
        }

        return SkillMatchModel(
            matchedUserId = otherUser.uid,
            matchedUserName = otherUser.name,
            matchScore = finalScore,
            matchReason = reason
        )
    }

    // NAYA: Do embedding vectors kitne "similar" hain, ye nikalta hai (0 se 1 ke beech)
    // 1 ka matlab bilkul same/related, 0 ka matlab bilkul unrelated
    private fun cosineSimilarity(vec1: List<Float>, vec2: List<Float>): Float {
        if (vec1.isEmpty() || vec2.isEmpty() || vec1.size != vec2.size) return 0f

        var dotProduct = 0f
        var magnitude1 = 0f
        var magnitude2 = 0f

        for (i in vec1.indices) {
            dotProduct += vec1[i] * vec2[i]
            magnitude1 += vec1[i] * vec1[i]
            magnitude2 += vec2[i] * vec2[i]
        }

        val denominator = kotlin.math.sqrt(magnitude1) * kotlin.math.sqrt(magnitude2)
        return if (denominator == 0f) 0f else dotProduct / denominator
    }

    // ---------------------------------------------------------
    // AI/Embeddings ke liye — ek text (jaise "Kotlin, Firebase")
    // leke uska embedding vector nikalta hai Gemini API se
    // ---------------------------------------------------------
    suspend fun getEmbedding(text: String): List<Float>? {
        return try {
            val request = EmbeddingRequest(
                content = Content(parts = listOf(Part(text = text)))
            )
            val response = RetrofitClient.geminiApi.getEmbedding(
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )
            response.embedding.values
        } catch (e: Exception) {
            android.util.Log.e("SkillMatchRepo", "Embedding fetch failed: ${e.message}", e)
            null
        }
    }
}