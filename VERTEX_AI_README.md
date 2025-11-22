# 🎨 Vertex AI Image Generation - Backend Integration

## ✅ What's Been Implemented

A fully functional Google Cloud Vertex AI **backend integration** for generating images using Gemini 3 Pro Image model. The repository layer is ready to use from your existing screens and ViewModels across all platforms (Android, iOS, Web, Desktop).

**Note:** Only the backend/repository layer is implemented. Integrate this into your existing UI screens and ViewModels.

## 📁 Project Structure

```
composeApp/src/
├── commonMain/kotlin/.../
│   ├── data/
│   │   ├── network/
│   │   │   ├── Base64Encoder.kt           ← Platform interface
│   │   │   ├── ImageUtils.kt              ← Helper utilities ⭐
│   │   │   ├── KtorClientFactory.kt       ← HTTP client
│   │   │   └── NetworkResult.kt           ← Result wrapper
│   │   └── repository/
│   │       ├── VertexAIRepository.kt      ← Core API integration ⭐
│   │       └── VertexAIRepositoryExample.kt ← Usage examples
│   ├── domain/model/
│   │   └── VertexAIModels.kt              ← Data models
│   └── di/
│       └── AppModule.kt                   ← DI configuration (updated)
│
├── androidMain/kotlin/.../data/network/
│   └── Base64Encoder.android.kt           ← Android implementation
│
├── iosMain/kotlin/.../data/network/
│   └── Base64Encoder.ios.kt               ← iOS implementation
│
├── jsMain/kotlin/.../data/network/
│   └── Base64Encoder.js.kt                ← JavaScript implementation
│
└── wasmJsMain/kotlin/.../data/network/
    └── Base64Encoder.wasmJs.kt            ← WebAssembly implementation

Documentation:
├── VERTEX_AI_README.md                    ← This file
├── VERTEX_AI_BACKEND_USAGE.md             ← Usage guide ⭐
├── VERTEX_AI_QUICKSTART.md                ← 5-minute setup guide
├── VERTEX_AI_USAGE.md                     ← Detailed API documentation
├── IMPLEMENTATION_SUMMARY.md               ← Technical summary
└── vertex-ai-config.template              ← Configuration template
```

## 🚀 Quick Start

### Step 1: Configure Credentials

```bash
export VERTEX_AI_PROJECT_ID="your-google-cloud-project-id"
export VERTEX_AI_API_KEY="your-api-key"
```

**Get credentials:** [Google Cloud Console](https://console.cloud.google.com/)

### Step 2: Use in Your Existing Code

```kotlin
import de.tum.hack.jb.interhyp.challenge.data.repository.VertexAIRepository
import de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils
import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import org.koin.core.component.inject

class YourViewModel(
    private val vertexAIRepository: VertexAIRepository
) : ViewModel() {
    
    fun generateImage(imageBytes: ByteArray, prompt: String) {
        viewModelScope.launch {
            val base64 = ImageUtils.encodeImageToBase64(imageBytes)
            
            vertexAIRepository.generateImage(
                prompt = prompt,
                inputImageBase64 = base64
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val imageData = ImageUtils.decodeBase64ToImage(
                            result.data.first().base64Data
                        )
                        // Use imageData in your UI
                    }
                    is NetworkResult.Error -> {
                        // Handle error
                    }
                    is NetworkResult.Loading -> {
                        // Show loading
                    }
                }
            }
        }
    }
}
```

### Step 3: Done! 🎉

The repository is injected via Koin and ready to use.

## 💡 Usage in Existing Screens

### Add to Your ViewModel

```kotlin
class PropertyViewModel(
    private val vertexAIRepository: VertexAIRepository,
    // ... your other dependencies
) : ViewModel() {
    
    private val _generatedImage = MutableStateFlow<ByteArray?>(null)
    val generatedImage = _generatedImage.asStateFlow()
    
    fun transformPropertyImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            val base64 = ImageUtils.encodeImageToBase64(imageBytes)
            
            vertexAIRepository.generateImage(
                prompt = "Transform into modern villa with swimming pool",
                inputImageBase64 = base64,
                temperature = 1.0
            ).collect { result ->
                if (result is NetworkResult.Success) {
                    _generatedImage.value = ImageUtils.decodeBase64ToImage(
                        result.data.first().base64Data
                    )
                }
            }
        }
    }
}
```

### Use in Your Existing Composable

```kotlin
@Composable
fun PropertyDetailScreen() {
    val viewModel: PropertyViewModel = koinViewModel()
    val generatedImage by viewModel.generatedImage.collectAsState()
    
    Column {
        // Your existing UI
        PropertyImage(property.imageUrl)
        
        // Add AI transformation button
        Button(onClick = {
            val imageBytes = loadPropertyImage()
            viewModel.transformPropertyImage(imageBytes)
        }) {
            Text("AI Transform")
        }
        
        // Display generated image
        generatedImage?.let { DisplayImage(it) }
    }
}
```

## 🔧 API Reference

### VertexAIRepository

```kotlin
interface VertexAIRepository {
    suspend fun generateImage(
        prompt: String,              // What to generate
        inputImageBase64: String,    // Base64 encoded image
        mimeType: String = "image/jpeg",
        temperature: Double = 1.0    // 0.0-2.0 (creativity)
    ): Flow<NetworkResult<List<GeneratedImage>>>
}
```

### ImageUtils

```kotlin
object ImageUtils {
    fun encodeImageToBase64(imageBytes: ByteArray): String
    fun decodeBase64ToImage(base64String: String): ByteArray
    fun getMimeTypeFromExtension(extension: String): String
}
```

### Temperature Guide

| Value | Behavior | Use Case |
|-------|----------|----------|
| 0.0 - 0.5 | Conservative, predictable | Professional listings |
| 0.5 - 1.0 | Balanced | General use |
| 1.0 - 1.5 | Creative | Variations |
| 1.5 - 2.0 | Experimental | Artistic ideas |

## 🎯 Real-World Examples

### Real Estate Property Transformation

```kotlin
vertexAIRepository.generateImage(
    prompt = "Transform this house: add modern windows, fresh white paint, " +
             "landscaped garden with fountain, and wraparound deck",
    inputImageBase64 = base64Image,
    temperature = 0.7
)
```

### Interior Design Visualization

```kotlin
vertexAIRepository.generateImage(
    prompt = "Redesign as modern minimalist living room with " +
             "neutral colors and contemporary furniture",
    inputImageBase64 = base64Image,
    temperature = 1.2
)
```

## 📚 Documentation

- **[VERTEX_AI_BACKEND_USAGE.md](./VERTEX_AI_BACKEND_USAGE.md)** - Complete usage guide ⭐
- **[VERTEX_AI_QUICKSTART.md](./VERTEX_AI_QUICKSTART.md)** - Fast 5-minute setup
- **[VERTEX_AI_USAGE.md](./VERTEX_AI_USAGE.md)** - Detailed API documentation
- **[IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md)** - Technical details
- **[VertexAIRepositoryExample.kt](./composeApp/src/commonMain/kotlin/de/tum/hack/jb/interhyp/challenge/data/repository/VertexAIRepositoryExample.kt)** - 6 code examples

## 🔐 Security Best Practices

⚠️ **Important:**

1. **Never commit API keys** to version control
2. Use environment variables for credentials
3. Add `vertex-ai-config.sh` to `.gitignore`
4. Use OAuth 2.0 tokens in production
5. Validate user prompts before sending

```bash
# Add to .gitignore
echo "vertex-ai-config.sh" >> .gitignore
```

## 🌐 Platform Support

✅ **Android** - Uses `android.util.Base64`  
✅ **iOS** - Uses `Foundation.NSData`  
✅ **Web/JS** - Uses browser's `btoa`/`atob`  
✅ **WebAssembly** - JavaScript interop  

**No platform-specific code needed in your app!**

## ❗ Troubleshooting

### Error: "API request failed with status 401"
**Solution:** Check your API key is correct and set in environment variables

### Error: "API request failed with status 403"
**Solution:** 
- Enable Vertex AI API in Google Cloud Console
- Ensure billing is enabled
- Check API key permissions

### Error: "Invalid input: Input image data cannot be blank"
**Solution:** Verify image is properly loaded and encoded to Base64

### Error: "Rate limit exceeded" (429)
**Solution:** Implement exponential backoff or wait before retrying

## 📊 Model Specifications

- **Model:** Gemini 3 Pro Image Preview
- **Context:** 65,536 tokens
- **Max Image Size:** 7 MB
- **Output Tokens:** 32,768
- **Regions:** Global, US, Europe, Asia
- **Knowledge Cutoff:** January 2025
- **Supported Formats:** JPEG, PNG, WebP, HEIC, HEIF

## ✨ Features Summary

✅ Generate images from text + image input  
✅ Multi-platform support (Android/iOS/Web/Desktop)  
✅ Complete error handling  
✅ Configurable creativity (temperature)  
✅ Multiple image format support  
✅ Type-safe Kotlin API  
✅ Flow-based reactive streams  
✅ Dependency injection with Koin  
✅ Complete documentation  
✅ Working examples  

## 🆘 Need Help?

1. **Quick Setup:** See [VERTEX_AI_BACKEND_USAGE.md](./VERTEX_AI_BACKEND_USAGE.md)
2. **API Details:** See [VERTEX_AI_USAGE.md](./VERTEX_AI_USAGE.md)
3. **Code Examples:** See [VertexAIRepositoryExample.kt](./composeApp/src/commonMain/kotlin/de/tum/hack/jb/interhyp/challenge/data/repository/VertexAIRepositoryExample.kt)
4. **Google Cloud:** [Vertex AI Documentation](https://cloud.google.com/vertex-ai/docs)

## 🎉 You're All Set!

The backend integration is complete. Inject `VertexAIRepository` into your existing ViewModels and start generating images!

**Happy image generating!** 🚀
