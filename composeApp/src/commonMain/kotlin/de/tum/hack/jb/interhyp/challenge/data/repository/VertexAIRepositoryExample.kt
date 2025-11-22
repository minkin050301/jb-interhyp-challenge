package de.tum.hack.jb.interhyp.challenge.data.repository

import de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils
import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import kotlinx.coroutines.flow.collect

/**
 * Example usage of VertexAIRepository for testing and reference
 * 
 * This is a documentation/example class showing how to use the Vertex AI integration.
 * Remove or comment out if not needed.
 */
class VertexAIRepositoryExample(
    private val repository: VertexAIRepository
) {
    /**
     * Example 1: Basic image generation
     */
    suspend fun basicExample(inputImageBytes: ByteArray) {
        // Encode image to Base64
        val base64Image = ImageUtils.encodeImageToBase64(inputImageBytes)
        
        // Generate image
        repository.generateImage(
            prompt = "Transform this house into a modern villa with a swimming pool",
            inputImageBase64 = base64Image,
            mimeType = "image/jpeg",
            temperature = 1.0
        ).collect { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    println("Generating image...")
                }
                is NetworkResult.Success -> {
                    println("Success! Generated ${result.data.size} image(s)")
                    result.data.forEach { image ->
                        val imageBytes = ImageUtils.decodeBase64ToImage(image.base64Data)
                        println("Image size: ${imageBytes.size} bytes")
                        println("MIME type: ${image.mimeType}")
                        println("Finish reason: ${image.finishReason}")
                        // Save or display the image here
                    }
                }
                is NetworkResult.Error -> {
                    println("Error: ${result.message}")
                    result.exception?.printStackTrace()
                }
            }
        }
    }
    
    /**
     * Example 2: Conservative/deterministic generation
     */
    suspend fun conservativeExample(inputImageBytes: ByteArray) {
        val base64Image = ImageUtils.encodeImageToBase64(inputImageBytes)
        
        repository.generateImage(
            prompt = "Add professional landscaping and fresh paint",
            inputImageBase64 = base64Image,
            temperature = 0.3 // Low temperature for more predictable results
        ).collect { result ->
            // Handle result...
        }
    }
    
    /**
     * Example 3: Creative/experimental generation
     */
    suspend fun creativeExample(inputImageBytes: ByteArray) {
        val base64Image = ImageUtils.encodeImageToBase64(inputImageBytes)
        
        repository.generateImage(
            prompt = "Reimagine this property as a futuristic eco-home",
            inputImageBase64 = base64Image,
            temperature = 1.8 // High temperature for creative variations
        ).collect { result ->
            // Handle result...
        }
    }
    
    /**
     * Example 4: Error handling
     */
    suspend fun errorHandlingExample(inputImageBytes: ByteArray) {
        val base64Image = ImageUtils.encodeImageToBase64(inputImageBytes)
        
        try {
            repository.generateImage(
                prompt = "Your prompt here",
                inputImageBase64 = base64Image
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        // Success case
                        if (result.data.isEmpty()) {
                            println("Warning: No images generated")
                        } else {
                            println("Generated ${result.data.size} images")
                        }
                    }
                    is NetworkResult.Error -> {
                        // Handle specific error cases
                        when {
                            result.message.contains("401") -> {
                                println("Authentication error. Check your API key.")
                            }
                            result.message.contains("403") -> {
                                println("Permission denied. Check project settings.")
                            }
                            result.message.contains("429") -> {
                                println("Rate limit exceeded. Try again later.")
                            }
                            else -> {
                                println("Error: ${result.message}")
                            }
                        }
                    }
                    is NetworkResult.Loading -> {
                        // Show loading indicator
                    }
                }
            }
        } catch (e: Exception) {
            println("Unexpected error: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Example 5: Processing different image formats
     */
    suspend fun multipleFormatsExample() {
        val formats = mapOf(
            "image/jpeg" to byteArrayOf(/* JPEG bytes */),
            "image/png" to byteArrayOf(/* PNG bytes */),
            "image/webp" to byteArrayOf(/* WebP bytes */)
        )
        
        formats.forEach { (mimeType, imageBytes) ->
            val base64Image = ImageUtils.encodeImageToBase64(imageBytes)
            
            repository.generateImage(
                prompt = "Modernize this property",
                inputImageBase64 = base64Image,
                mimeType = mimeType
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        println("Successfully generated image from $mimeType")
                    }
                    is NetworkResult.Error -> {
                        println("Failed for $mimeType: ${result.message}")
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }
    
    /**
     * Example 6: Using with different prompt strategies
     */
    suspend fun promptStrategiesExample(inputImageBytes: ByteArray) {
        val base64Image = ImageUtils.encodeImageToBase64(inputImageBytes)
        
        val prompts = listOf(
            // Descriptive prompt
            "A modern two-story house with large windows, minimalist design, and a manicured lawn",
            
            // Action-based prompt
            "Transform this house: add solar panels, repaint in white, install new windows, add deck",
            
            // Style-based prompt
            "Reimagine in contemporary architectural style with clean lines and natural materials",
            
            // Comparative prompt
            "Make this house look like a luxury property featured in architectural magazines"
        )
        
        prompts.forEach { prompt ->
            println("Trying prompt: $prompt")
            repository.generateImage(
                prompt = prompt,
                inputImageBase64 = base64Image,
                temperature = 1.0
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        println("✓ Success with this prompt")
                    }
                    is NetworkResult.Error -> {
                        println("✗ Failed: ${result.message}")
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }
}

/**
 * Quick test function to verify the integration works
 */
suspend fun testVertexAIIntegration(
    repository: VertexAIRepository,
    testImageBytes: ByteArray
) {
    println("Testing Vertex AI integration...")
    
    val base64Image = ImageUtils.encodeImageToBase64(testImageBytes)
    
    repository.generateImage(
        prompt = "Add a red door to this house",
        inputImageBase64 = base64Image,
        mimeType = "image/jpeg",
        temperature = 1.0
    ).collect { result ->
        when (result) {
            is NetworkResult.Loading -> {
                println("⏳ Loading...")
            }
            is NetworkResult.Success -> {
                println("✅ Integration test PASSED")
                println("   Generated ${result.data.size} image(s)")
                result.data.forEachIndexed { index, image ->
                    println("   Image $index: ${image.mimeType}, ${ImageUtils.decodeBase64ToImage(image.base64Data).size} bytes")
                }
            }
            is NetworkResult.Error -> {
                println("❌ Integration test FAILED")
                println("   Error: ${result.message}")
            }
        }
    }
}

