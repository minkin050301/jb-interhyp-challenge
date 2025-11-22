package de.tum.hack.jb.interhyp.challenge.data.repository

import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import de.tum.hack.jb.interhyp.challenge.domain.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository interface for Vertex AI operations.
 */
interface VertexAIRepository {
    /**
     * Generate an image based on a text prompt and an input image.
     * 
     * @param prompt Text description for image generation
     * @param inputImageBase64 Base64 encoded input image
     * @param mimeType MIME type of the input image (e.g., "image/jpeg", "image/png")
     * @param temperature Controls randomness (0.0-2.0, default 1.0)
     * @return Flow emitting NetworkResult with generated images
     */
    suspend fun generateImage(
        prompt: String,
        inputImageBase64: String,
        mimeType: String = "image/jpeg",
        temperature: Double = 1.0
    ): Flow<NetworkResult<List<GeneratedImage>>>
}

/**
 * Implementation of VertexAIRepository using Google Cloud Vertex AI.
 */
class VertexAIRepositoryImpl(
    private val httpClient: HttpClient,
    private val config: VertexAIConfig
) : VertexAIRepository {
    
    companion object {
        private const val MODEL_NAME = "gemini-3-pro-image-preview"
        private const val API_ENDPOINT = "aiplatform.googleapis.com"
    }
    
    /**
     * Build the Vertex AI API endpoint URL
     */
    private fun buildEndpointUrl(): String {
        return "https://${config.location}-$API_ENDPOINT/v1/projects/${config.projectId}/locations/${config.location}/publishers/google/models/$MODEL_NAME:generateContent"
    }
    
    override suspend fun generateImage(
        prompt: String,
        inputImageBase64: String,
        mimeType: String,
        temperature: Double
    ): Flow<NetworkResult<List<GeneratedImage>>> = flow {
        emit(NetworkResult.Loading)
        
        try {
            // Validate inputs
            require(prompt.isNotBlank()) { "Prompt cannot be blank" }
            require(inputImageBase64.isNotBlank()) { "Input image data cannot be blank" }
            require(temperature in 0.0..2.0) { "Temperature must be between 0.0 and 2.0" }
            
            // Prepare request
            val request = VertexAIRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(
                            // First part: input image
                            Part(
                                inlineData = InlineData(
                                    mimeType = mimeType,
                                    data = inputImageBase64
                                )
                            ),
                            // Second part: text prompt
                            Part(
                                text = prompt
                            )
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = temperature,
                    candidateCount = 1
                )
            )
            
            // Make API call
            val response = httpClient.post(buildEndpointUrl()) {
                contentType(ContentType.Application.Json)
                
                // Add authentication header
                if (config.apiKey != null) {
                    parameter("key", config.apiKey)
                } else if (config.accessToken != null) {
                    header("Authorization", "Bearer ${config.accessToken}")
                }
                
                setBody(request)
            }
            
            if (response.status.isSuccess()) {
                val vertexResponse: VertexAIResponse = response.body()
                
                // Extract generated images from response
                val generatedImages = vertexResponse.candidates
                    ?.mapNotNull { candidate ->
                        candidate.content?.parts?.mapNotNull { part ->
                            part.inlineData?.let {
                                GeneratedImage(
                                    base64Data = it.data,
                                    mimeType = it.mimeType,
                                    finishReason = candidate.finishReason
                                )
                            }
                        }
                    }
                    ?.flatten()
                    ?: emptyList()
                
                if (generatedImages.isEmpty()) {
                    emit(NetworkResult.Error("No images generated. Response: ${vertexResponse.candidates?.firstOrNull()?.finishReason ?: "Unknown reason"}"))
                } else {
                    emit(NetworkResult.Success(generatedImages))
                }
            } else {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    "Unable to parse error response"
                }
                emit(NetworkResult.Error("API request failed with status ${response.status.value}: $errorBody"))
            }
        } catch (e: IllegalArgumentException) {
            emit(NetworkResult.Error("Invalid input: ${e.message}", e))
        } catch (e: Exception) {
            emit(NetworkResult.Error("Failed to generate image: ${e.message}", e))
        }
    }
}

