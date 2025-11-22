package de.tum.hack.jb.interhyp.challenge.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request model for Vertex AI Gemini image generation
 */
@Serializable
data class VertexAIRequest(
    @SerialName("contents")
    val contents: List<Content>,
    @SerialName("generationConfig")
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class Content(
    @SerialName("role")
    val role: String = "user",
    @SerialName("parts")
    val parts: List<Part>
)

@Serializable
data class Part(
    @SerialName("text")
    val text: String? = null,
    @SerialName("inlineData")
    val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    @SerialName("mimeType")
    val mimeType: String,
    @SerialName("data")
    val data: String // Base64 encoded image data
)

@Serializable
data class GenerationConfig(
    @SerialName("temperature")
    val temperature: Double? = null,
    @SerialName("topP")
    val topP: Double? = null,
    @SerialName("topK")
    val topK: Int? = null,
    @SerialName("candidateCount")
    val candidateCount: Int? = 1,
    @SerialName("maxOutputTokens")
    val maxOutputTokens: Int? = null
)

/**
 * Response model from Vertex AI Gemini
 */
@Serializable
data class VertexAIResponse(
    @SerialName("candidates")
    val candidates: List<Candidate>? = null,
    @SerialName("usageMetadata")
    val usageMetadata: UsageMetadata? = null
)

@Serializable
data class Candidate(
    @SerialName("content")
    val content: Content? = null,
    @SerialName("finishReason")
    val finishReason: String? = null,
    @SerialName("safetyRatings")
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class SafetyRating(
    @SerialName("category")
    val category: String,
    @SerialName("probability")
    val probability: String
)

@Serializable
data class UsageMetadata(
    @SerialName("promptTokenCount")
    val promptTokenCount: Int? = null,
    @SerialName("candidatesTokenCount")
    val candidatesTokenCount: Int? = null,
    @SerialName("totalTokenCount")
    val totalTokenCount: Int? = null
)

/**
 * Configuration for Vertex AI client
 */
data class VertexAIConfig(
    val projectId: String,
    val location: String = "global",
    val apiKey: String? = null,
    val accessToken: String? = null
) {
    init {
        require(apiKey != null || accessToken != null) {
            "Either API key or access token must be provided"
        }
    }
}

/**
 * Result of image generation
 */
data class GeneratedImage(
    val base64Data: String,
    val mimeType: String,
    val finishReason: String? = null
)

