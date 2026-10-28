package com.sahilmaske.peerlearn.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sahilmaske.peerlearn.API.Content
import com.sahilmaske.peerlearn.API.EmbeddingRequest
import com.sahilmaske.peerlearn.API.Part
import com.sahilmaske.peerlearn.API.RetrofitClient
import com.sahilmaske.peerlearn.BuildConfig
import com.sahilmaske.peerlearn.data.model.SkillMatchModel
import com.sahilmaske.peerlearn.data.model.User
import com.sahilmaske.peerlearn.util.SkillAliasMapper
import kotlinx.coroutines.tasks.await

class SkillMatchRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Step 1: Fetch users from Firestore (current user excluded)
    // FIX (perf): limit the fetch instead of pulling the entire collection.
    // TODO: replace with a proper Firestore query — e.g.
    //   .whereArrayContainsAny("knownSkills", currentUser.learningSkills)
    // once skill strings are normalized at write-time, so Firestore itself
    // narrows the candidate set instead of Kotlin filtering everyone.
    suspend fun getRuleBasedMatches(currentUser: User): List<SkillMatchModel> {
        val snapshot = firestore.collection("users")
            .limit(200)
            .get()
            .await()

        val allUsers = snapshot.toObjects(User::class.java)

        // Step 2: Remove current user from the list (don't match with self)
        val otherUsers = allUsers.filter { it.uid != currentUser.uid }

        // FIX (perf): normalize the current user's skills ONCE, outside the loop,
        // instead of re-normalizing them for every other user compared.
        val currentWantsNormalized = SkillAliasMapper
            .normalizeList(currentUser.learningSkills)
            .map { it.lowercase() }

        val currentHasEmbedding = currentUser.learningSkillsEmbedding.isNotEmpty()

        // Step 3: Calculate match score for each user
        val matches = otherUsers.mapNotNull { otherUser ->
            calculateMatch(currentUser, otherUser, currentWantsNormalized, currentHasEmbedding)
        }

        // Step 4: Sort so best match shows first
        return matches.sortedByDescending { it.matchScore }
    }

    // Combines exact keyword match (rule-based) with semantic similarity (AI embeddings)
    private fun calculateMatch(
        currentUser: User,
        otherUser: User,
        currentWantsNormalized: List<String>,
        currentHasEmbedding: Boolean
    ): SkillMatchModel? {
        val otherKnowsNormalized = SkillAliasMapper
            .normalizeList(otherUser.knownSkills)
            .map { it.lowercase() }

        val overlap = currentWantsNormalized.intersect(otherKnowsNormalized.toSet())

        val ruleScore = if (currentWantsNormalized.isEmpty()) 0f
        else overlap.size.toFloat() / currentWantsNormalized.size.toFloat()

        // FIX (correctness): only trust semanticScore if BOTH users actually have
        // embeddings generated. Otherwise cosineSimilarity silently returns 0f,
        // which would wrongly kill a valid rule-based match below.
        val otherHasEmbedding = otherUser.knownSkillsEmbedding.isNotEmpty()
        val bothHaveEmbeddings = currentHasEmbedding && otherHasEmbedding

        val semanticScore = if (bothHaveEmbeddings) {
            cosineSimilarity(currentUser.learningSkillsEmbedding, otherUser.knownSkillsEmbedding)
        } else {
            null // signals "unknown", not "zero similarity"
        }

        val finalScore = if (semanticScore != null) {
            maxOf(ruleScore, semanticScore * 0.9f)
        } else {
            ruleScore
        }

        // FIX (correctness): the semantic threshold only applies when we actually
        // have a semantic score to judge. If embeddings aren't ready yet, fall back
        // to rule-based matching alone instead of discarding the match.
        val failsSemanticThreshold = semanticScore != null && semanticScore < 0.75f
        if (ruleScore == 0f && failsSemanticThreshold) return null
        if (ruleScore == 0f && semanticScore == null) return null

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

    // Cosine similarity between two embedding vectors (0 = unrelated, 1 = identical/related)
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

    // Fetches an embedding vector for a given text (e.g. "Kotlin, Firebase") via Gemini API
    // TODO: replace Log.e with a proper crash-reporting/monitoring call for production
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