# Vertex AI Integration Guide

This guide explains how to use the Vertex AI Gemini 3 Pro Image integration for generating images based on text prompts and input images.

## Setup

### 1. Configure Vertex AI Credentials

You need to set up your Google Cloud Vertex AI credentials. You have two options:

#### Option A: Using Environment Variables (Recommended for Development)

Set the following environment variables:

```bash
export VERTEX_AI_PROJECT_ID="your-google-cloud-project-id"
export VERTEX_AI_LOCATION="global"  # or your preferred location
export VERTEX_AI_API_KEY="your-api-key"
```

#### Option B: Hardcode in AppModule.kt (Not Recommended for Production)

Update the `VertexAIConfig` in `di/AppModule.kt`:

```kotlin
single<VertexAIConfig> {
    VertexAIConfig(
        projectId = "your-project-id",
        location = "global",
        apiKey = "your-api-key"
    )
}
```

### 2. Get Your Google Cloud Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Vertex AI API
4. Create an API key or OAuth 2.0 credentials
5. Use the credentials in your configuration

## Usage

### Basic Example

Here's how to use the Vertex AI repository to generate images:

```kotlin
import de.tum.hack.jb.interhyp.challenge.data.repository.VertexAIRepository
import de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils
import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import kotlinx.coroutines.flow.collect
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ImageGenerationExample : KoinComponent {
    private val vertexAIRepository: VertexAIRepository by inject()
    
    suspend fun generateImage() {
        // 1. Load your input image as ByteArray
        val imageBytes: ByteArray = loadImageFromFile() // Your implementation
        
        // 2. Encode to Base64
        val base64Image = ImageUtils.encodeImageToBase64(imageBytes)
        
        // 3. Create a prompt
        val prompt = "Transform this house into a modern villa with a pool"
        
        // 4. Generate image
        vertexAIRepository.generateImage(
            prompt = prompt,
            inputImageBase64 = base64Image,
            mimeType = "image/jpeg",
            temperature = 1.0
        ).collect { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    println("Generating image...")
                }
                is NetworkResult.Success -> {
                    val generatedImages = result.data
                    generatedImages.forEach { image ->
                        // Decode the generated image
                        val imageData = ImageUtils.decodeBase64ToImage(image.base64Data)
                        // Save or display the image
                        saveImage(imageData, image.mimeType)
                    }
                }
                is NetworkResult.Error -> {
                    println("Error: ${result.message}")
                }
            }
        }
    }
    
    private fun loadImageFromFile(): ByteArray {
        // Platform-specific implementation
        TODO("Implement image loading")
    }
    
    private fun saveImage(imageData: ByteArray, mimeType: String) {
        // Platform-specific implementation
        TODO("Implement image saving")
    }
}
```

### Using in a ViewModel

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.hack.jb.interhyp.challenge.data.repository.VertexAIRepository
import de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils
import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImageGenerationViewModel(
    private val vertexAIRepository: VertexAIRepository
) : ViewModel() {
    
    private val _generatedImage = MutableStateFlow<ByteArray?>(null)
    val generatedImage: StateFlow<ByteArray?> = _generatedImage
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    fun generateImage(imageBytes: ByteArray, prompt: String) {
        viewModelScope.launch {
            val base64Image = ImageUtils.encodeImageToBase64(imageBytes)
            
            vertexAIRepository.generateImage(
                prompt = prompt,
                inputImageBase64 = base64Image,
                mimeType = "image/jpeg",
                temperature = 1.0
            ).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                        _error.value = null
                    }
                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        val firstImage = result.data.firstOrNull()
                        if (firstImage != null) {
                            val imageData = ImageUtils.decodeBase64ToImage(firstImage.base64Data)
                            _generatedImage.value = imageData
                        }
                    }
                    is NetworkResult.Error -> {
                        _isLoading.value = false
                        _error.value = result.message
                    }
                }
            }
        }
    }
}
```

### Register ViewModel in Koin

Add to your `di/AppModule.kt`:

```kotlin
val presentationModule = module {
    // ... existing ViewModels
    factory { ImageGenerationViewModel(get()) }
}
```

## API Parameters

### `generateImage()` Parameters

- **`prompt`** (String, required): Text description for how to transform/generate the image
- **`inputImageBase64`** (String, required): Base64 encoded input image
- **`mimeType`** (String, optional): MIME type of the input image (default: "image/jpeg")
  - Supported: `image/jpeg`, `image/png`, `image/webp`, `image/heic`, `image/heif`
- **`temperature`** (Double, optional): Controls randomness (0.0-2.0, default: 1.0)
  - Lower values (0.0-0.5): More deterministic, conservative
  - Medium values (0.5-1.5): Balanced creativity
  - Higher values (1.5-2.0): More creative, unpredictable

## Model Specifications

- **Model**: Gemini 3 Pro Image Preview
- **Context Window**: 65,536 tokens
- **Maximum Image Size**: 7 MB
- **Maximum Output Tokens**: 32,768
- **Supported Aspect Ratios**: 1:1, 3:2, 2:3, 3:4, 4:3, 4:5, 5:4, 9:16, 16:9, 21:9
- **Knowledge Cutoff**: January 2025

## Error Handling

The repository returns a `NetworkResult` sealed class with three states:

1. **`NetworkResult.Loading`**: Request in progress
2. **`NetworkResult.Success<List<GeneratedImage>>`**: Successful response with generated images
3. **`NetworkResult.Error`**: Error occurred with message and optional exception

Always handle all three states when collecting the Flow:

```kotlin
vertexAIRepository.generateImage(...).collect { result ->
    when (result) {
        is NetworkResult.Loading -> { /* Show loading indicator */ }
        is NetworkResult.Success -> { /* Handle success */ }
        is NetworkResult.Error -> { /* Show error message */ }
    }
}
```

## Common Use Cases

### 1. Real Estate Property Visualization

```kotlin
val prompt = "Show this house with modern renovations, new windows, and fresh paint"
```

### 2. Interior Design

```kotlin
val prompt = "Transform this room into a modern minimalist living space with neutral colors"
```

### 3. Exterior Enhancement

```kotlin
val prompt = "Add a beautiful garden, swimming pool, and outdoor patio to this property"
```

### 4. Seasonal Variations

```kotlin
val prompt = "Show how this property would look in different seasons - winter with snow"
```

## Platform-Specific Considerations

The implementation uses platform-specific Base64 encoding:

- **Android**: Uses `android.util.Base64`
- **iOS**: Uses `Foundation.NSData`
- **JS/Web**: Uses browser's `btoa`/`atob` functions
- **WasmJS**: Uses JavaScript interop

All platforms are supported with no additional configuration needed.

## Security Notes

⚠️ **Important Security Considerations:**

1. **Never commit API keys** to version control
2. Use environment variables or secure secret management
3. For production, use OAuth 2.0 tokens instead of API keys
4. Implement rate limiting on the client side
5. Validate and sanitize user prompts before sending to the API

## Troubleshooting

### "API request failed with status 401"
- Check that your API key or OAuth token is valid
- Ensure the Vertex AI API is enabled in your Google Cloud project

### "API request failed with status 403"
- Verify your project has billing enabled
- Check that your API key has permission to access Vertex AI

### "Invalid input: Input image data cannot be blank"
- Ensure you're properly encoding the image to Base64
- Verify the image file is not corrupt

### "No images generated"
- Check the `finishReason` in the error message
- The prompt might violate content policies
- Try adjusting the temperature parameter

## Additional Resources

- [Vertex AI Documentation](https://cloud.google.com/vertex-ai/docs)
- [Gemini 3 Pro Image Model Details](https://docs.cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/3-pro-image)
- [Google Cloud Console](https://console.cloud.google.com/)

## Support

For issues specific to this integration, please check:
1. Your Google Cloud configuration
2. API quotas and limits
3. Network connectivity
4. Input validation (image size, format, prompt length)

