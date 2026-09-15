package com.sahilmaske.peerlearn.API

// ---- Request: jo hum Gemini ko bhejenge ----
data class EmbeddingRequest(
    val model: String = "models/gemini-embedding-001",
    val content: Content
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

// ---- Response: jo Gemini hume wapas dega ----
data class EmbeddingResponse(
    val embedding: Embedding
)

data class Embedding(
    val values: List<Float>
)