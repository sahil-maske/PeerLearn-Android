package com.sahilmaske.peerlearn.API

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    @POST("v1beta/models/gemini-embedding-001:embedContent")
    suspend fun getEmbedding(
        @Query("key") apiKey: String,
        @Body request: EmbeddingRequest
    ): EmbeddingResponse
}