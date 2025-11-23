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
     * Generate an image based on a text prompt and multiple input images.
     *
     * @param prompt Text description for image generation
     * @param inputImages List of Base64 encoded input images with their mime types
     * @param temperature Controls randomness (0.0-2.0, default 1.0)
     * @param aspectRatio Aspect ratio for the generated image (e.g., "9:16", "16:9", "1:1")
     * @return Flow emitting NetworkResult with generated images
     */
    suspend fun generateImage(
        prompt: String,
        inputImages: List<Pair<String, String>>, // Pair of (base64Data, mimeType)
        temperature: Double = 1.0,
        aspectRatio: String? = null
    ): Flow<NetworkResult<List<GeneratedImage>>>
    
    /**
     * Generate an image based on a text prompt and an input image (Legacy overload).
     */
    suspend fun generateImage(
        prompt: String,
        inputImageBase64: String,
        mimeType: String = "image/jpeg",
        temperature: Double = 1.0,
        aspectRatio: String? = null
    ): Flow<NetworkResult<List<GeneratedImage>>>

    /**
     * Generate content using flexible parts (text, inline images, file URIs).
     * This allows constructing complex requests with specific order of parts.
     */
    suspend fun generateContent(
        parts: List<VertexAIInputPart>,
        temperature: Double = 1.0,
        aspectRatio: String? = null
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
        private const val MODEL_NAME = "gemini-2.5-flash-image"
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
        temperature: Double,
        aspectRatio: String?
    ): Flow<NetworkResult<List<GeneratedImage>>> = generateImage(
        prompt = prompt,
        inputImages = listOf(inputImageBase64 to mimeType),
        temperature = temperature,
        aspectRatio = aspectRatio
    )

    override suspend fun generateImage(
        prompt: String,
        inputImages: List<Pair<String, String>>,
        temperature: Double,
        aspectRatio: String?
    ): Flow<NetworkResult<List<GeneratedImage>>> {
        val parts = mutableListOf<VertexAIInputPart>()
        // Add input images first (as per convention for this helper)
        inputImages.forEach { (base64Data, mimeType) ->
            parts.add(VertexAIInputPart.InlineImage(base64Data, mimeType))
        }
        // Add text prompt
        parts.add(VertexAIInputPart.Text(prompt))
        
        return generateContent(parts, temperature, aspectRatio)
    }

    override suspend fun generateContent(
        parts: List<VertexAIInputPart>,
        temperature: Double,
        aspectRatio: String?
    ): Flow<NetworkResult<List<GeneratedImage>>> = flow {
        emit(NetworkResult.Loading)
        
        try {
            // Validate inputs
            require(parts.isNotEmpty()) { "At least one part is required" }
            require(temperature in 0.0..2.0) { "Temperature must be between 0.0 and 2.0" }
            
            // Map domain parts to API parts
            val apiParts = parts.map { inputPart ->
                when (inputPart) {
                    is VertexAIInputPart.Text -> Part(text = inputPart.text)
                    is VertexAIInputPart.InlineImage -> Part(
                        inlineData = InlineData(
                            mimeType = inputPart.mimeType,
                            data = inputPart.base64
                        )
                    )
                    is VertexAIInputPart.FileImage -> Part(
                        fileData = FileData(
                            mimeType = inputPart.mimeType,
                            fileUri = inputPart.uri
                        )
                    )
                }
            }
            
            // Prepare request
            val request = VertexAIRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = apiParts
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = temperature,
                    maxOutputTokens = 32768,
                    responseModalities = listOf("TEXT", "IMAGE"),
                    topP = 0.95,
                    imageConfig = ImageConfig(
                        aspectRatio = aspectRatio ?: "9:16",
                        imageSize = "1K",
                        imageOutputOptions = ImageOutputOptions(
                            mimeType = "image/png"
                        ),
                        personGeneration = "ALLOW_ALL"
                    )
                )
            )
            
            // Make API call
            val response = httpClient.post(buildEndpointUrl()) {
                contentType(ContentType.Application.Json)
                
                // Add authentication header
                if (config.accessToken != null) {
                    header("Authorization", "Bearer ${config.accessToken}")
                } else if (config.apiKey != null) {
                    parameter("key", config.apiKey)
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
            emit(NetworkResult.Error("Failed to generate content: ${e.message}", e))
        }
    }
}
