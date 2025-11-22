# Vertex AI Backend Integration - Usage Guide

This guide explains how to use the Vertex AI Gemini 3 Pro Image backend integration from your existing screens.

## What's Been Implemented

✅ **Backend/Repository Layer Only**
- Complete API integration with Vertex AI
- Platform-specific Base64 encoding (Android, iOS, JS, WebAssembly)
- Error handling and state management
- Flow-based reactive API
- Dependency injection ready

❌ **No UI Components**
- You'll integrate this into your existing screens
- No pre-built ViewModels or Composables

## Setup

### 1. Configure Vertex AI Credentials

Set environment variables:

```bash
export VERTEX_AI_PROJECT_ID="your-google-cloud-project-id"
export VERTEX_AI_LOCATION="global"  # or your preferred location
export VERTEX_AI_API_KEY="your-api-key"
```

Or update in `di/AppModule.kt`:

```kotlin
single<VertexAIConfig> {
    VertexAIConfig(
        projectId = "your-project-id",
        location = "global",
        apiKey = "your-api-key"
    )
}
```

### 2. Get Google Cloud Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create/select a project
3. Enable Vertex AI API
4. Create an API key
5. Use credentials in configuration

## Using the Repository

### Basic Usage

```kotlin
import de.tum.hack.jb.interhyp.challenge.data.repository.VertexAIRepository
import de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils
import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import kotlinx.coroutines.flow.collect
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class YourExistingScreen : KoinComponent {
    private val vertexAIRepository: VertexAIRepository by inject()
    
    suspend fun generatePropertyImage(imageBytes: ByteArray, prompt: String) {
        // 1. Encode image to Base64
        val base64Image = ImageUtils.encodeImageToBase64(imageBytes)
        
        // 2. Generate image
        vertexAIRepository.generateImage(
            prompt = prompt,
            inputImageBase64 = base64Image,
            mimeType = "image/jpeg",
            temperature = 1.0
        ).collect { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // Show loading indicator
                }
                is NetworkResult.Success -> {
                    val generatedImages = result.data
                    generatedImages.forEach { image ->
                        // Decode and use the image
                        val imageData = ImageUtils.decodeBase64ToImage(image.base64Data)
                        displayImage(imageData)
                    }
                }
                is NetworkResult.Error -> {
                    // Handle error
                    showError(result.message)
                }
            }
        }
    }
}
```

### In an Existing ViewModel

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.hack.jb.interhyp.challenge.data.repository.VertexAIRepository
import de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils
import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class YourExistingViewModel(
    private val vertexAIRepository: VertexAIRepository
) : ViewModel() {
    
    private val _aiImageState = MutableStateFlow<ByteArray?>(null)
    val aiImageState = _aiImageState
    
    fun transformPropertyImage(imageBytes: ByteArray, prompt: String) {
        viewModelScope.launch {
            val base64 = ImageUtils.encodeImageToBase64(imageBytes)
            
            vertexAIRepository.generateImage(
                prompt = prompt,
                inputImageBase64 = base64
            ).collect { result ->
                if (result is NetworkResult.Success) {
                    val image = result.data.firstOrNull()
                    image?.let {
                        _aiImageState.value = ImageUtils.decodeBase64ToImage(it.base64Data)
                    }
                }
            }
        }
    }
}
```

## API Reference

### VertexAIRepository

```kotlin
interface VertexAIRepository {
    suspend fun generateImage(
        prompt: String,              // Text description
        inputImageBase64: String,    // Base64 encoded image
        mimeType: String = "image/jpeg",  // image/jpeg, image/png, image/webp, etc.
        temperature: Double = 1.0    // 0.0-2.0 (creativity level)
    ): Flow<NetworkResult<List<GeneratedImage>>>
}
```

### ImageUtils

```kotlin
object ImageUtils {
    // Encode image bytes to Base64
    fun encodeImageToBase64(imageBytes: ByteArray): String
    
    // Decode Base64 to image bytes
    fun decodeBase64ToImage(base64String: String): ByteArray
    
    // Get MIME type from file extension
    fun getMimeTypeFromExtension(extension: String): String
    
    // Strip Base64 prefix if present
    fun stripBase64Prefix(base64String: String): String
}
```

### Temperature Guide

| Value | Behavior | Use Case |
|-------|----------|----------|
| 0.0 - 0.5 | Conservative, predictable | Professional listings |
| 0.5 - 1.0 | Balanced | General use |
| 1.0 - 1.5 | Creative | Variations |
| 1.5 - 2.0 | Experimental | Artistic ideas |

## Integration Example

Here's how to add it to an existing property detail screen:

```kotlin
@Composable
fun PropertyDetailScreen(property: Property) {
    var isGeneratingAI by remember { mutableStateOf(false) }
    var aiGeneratedImage by remember { mutableStateOf<ByteArray?>(null) }
    
    // Get repository from Koin
    val repository: VertexAIRepository = koinInject()
    val scope = rememberCoroutineScope()
    
    Column {
        // Your existing UI
        PropertyImage(property.imageUrl)
        PropertyDetails(property)
        
        // Add AI transformation button
        Button(
            onClick = {
                scope.launch {
                    isGeneratingAI = true
                    val imageBytes = loadPropertyImage(property)
                    val base64 = ImageUtils.encodeImageToBase64(imageBytes)
                    
                    repository.generateImage(
                        prompt = "Transform into modern villa with pool",
                        inputImageBase64 = base64
                    ).collect { result ->
                        when (result) {
                            is NetworkResult.Success -> {
                                aiGeneratedImage = ImageUtils.decodeBase64ToImage(
                                    result.data.first().base64Data
                                )
                                isGeneratingAI = false
                            }
                            is NetworkResult.Error -> {
                                isGeneratingAI = false
                            }
                            else -> {}
                        }
                    }
                }
            },
            enabled = !isGeneratingAI
        ) {
            Text(if (isGeneratingAI) "Generating..." else "AI Transform")
        }
        
        // Display AI result
        aiGeneratedImage?.let { DisplayImage(it) }
    }
}
```

## Examples

See `VertexAIRepositoryExample.kt` for 6 detailed examples including:
1. Basic image generation
2. Conservative/deterministic generation
3. Creative/experimental generation
4. Error handling patterns
5. Multiple image formats
6. Different prompt strategies

## Model Specifications

- **Model:** Gemini 3 Pro Image Preview
- **Context:** 65,536 tokens
- **Max Image Size:** 7 MB
- **Output Tokens:** 32,768
- **Supported MIME Types:** image/jpeg, image/png, image/webp, image/heic, image/heif
- **Knowledge Cutoff:** January 2025

## Files Implemented

**Core Backend:**
- `domain/model/VertexAIModels.kt` - Data models
- `data/repository/VertexAIRepository.kt` - Main API integration ⭐
- `data/network/Base64Encoder.kt` - Platform interface
- `data/network/ImageUtils.kt` - Helper utilities
- Platform-specific Base64 implementations (Android, iOS, JS, WebAssembly)

**Configuration:**
- `di/AppModule.kt` - Dependency injection (includes VertexAIRepository)

**Examples:**
- `data/repository/VertexAIRepositoryExample.kt` - Code examples

**Documentation:**
- `VERTEX_AI_BACKEND_USAGE.md` - This file
- `VERTEX_AI_USAGE.md` - Detailed API documentation
- `IMPLEMENTATION_SUMMARY.md` - Technical summary

## Error Handling

Always handle all three states:

```kotlin
vertexAIRepository.generateImage(...).collect { result ->
    when (result) {
        is NetworkResult.Loading -> {
            // Show loading UI
        }
        is NetworkResult.Success -> {
            // Process generated images
            result.data.forEach { image ->
                val bytes = ImageUtils.decodeBase64ToImage(image.base64Data)
                // Use bytes
            }
        }
        is NetworkResult.Error -> {
            // Show error message
            println("Error: ${result.message}")
        }
    }
}
```

## Security Best Practices

⚠️ **Important:**
1. Never commit API keys to version control
2. Use environment variables
3. For production, use OAuth 2.0 tokens instead of API keys
4. Implement rate limiting
5. Validate user prompts

## Troubleshooting

### "API request failed with status 401"
Check API key is correct and set in environment

### "API request failed with status 403"
- Enable Vertex AI API in Google Cloud Console
- Ensure billing is enabled
- Check API key permissions

### "Invalid input: Input image data cannot be blank"
Verify image is properly loaded and encoded to Base64

## Support

- Check `VertexAIRepositoryExample.kt` for code examples
- See `VERTEX_AI_USAGE.md` for detailed documentation
- Visit [Google Cloud Vertex AI Documentation](https://cloud.google.com/vertex-ai/docs)

## Next Steps

1. Configure your Vertex AI credentials
2. Inject `VertexAIRepository` into your existing ViewModels
3. Call `generateImage()` from your existing screens
4. Handle the `NetworkResult` states in your UI
5. Test with real property images

That's it! The backend is ready to use from anywhere in your app.

