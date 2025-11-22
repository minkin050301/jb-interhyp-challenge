# Vertex AI Quick Start

## 1. Setup (5 minutes)

### Set Environment Variables

```bash
export VERTEX_AI_PROJECT_ID="your-google-cloud-project-id"
export VERTEX_AI_API_KEY="your-api-key"
```

Or update in `di/AppModule.kt`:

```kotlin
single<VertexAIConfig> {
    VertexAIConfig(
        projectId = "your-project-id",
        apiKey = "your-api-key"
    )
}
```

## 2. Basic Usage

### Option A: Use the Pre-built Screen (Easiest)

```kotlin
import de.tum.hack.jb.interhyp.challenge.ui.imagegen.ImageGenerationScreen

// In your Compose UI
ImageGenerationScreen(
    inputImageBytes = yourImageBytes,
    onImageGenerated = { generatedBytes ->
        // Use the generated image
        saveOrDisplayImage(generatedBytes)
    }
)
```

### Option B: Use Repository Directly

```kotlin
import de.tum.hack.jb.interhyp.challenge.data.repository.VertexAIRepository
import de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils
import org.koin.core.component.inject

class YourClass : KoinComponent {
    private val vertexAIRepository: VertexAIRepository by inject()
    
    suspend fun generate() {
        val imageBytes = loadYourImage() // Your implementation
        val base64 = ImageUtils.encodeImageToBase64(imageBytes)
        
        vertexAIRepository.generateImage(
            prompt = "Make this house modern with a pool",
            inputImageBase64 = base64
        ).collect { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val image = result.data.first()
                    val bytes = ImageUtils.decodeBase64ToImage(image.base64Data)
                    // Use bytes
                }
                is NetworkResult.Error -> println(result.message)
                is NetworkResult.Loading -> println("Loading...")
            }
        }
    }
}
```

### Option C: Use ViewModel

```kotlin
import de.tum.hack.jb.interhyp.challenge.presentation.imagegen.ImageGenerationViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun YourScreen() {
    val viewModel: ImageGenerationViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    Button(onClick = {
        viewModel.generateImage(
            imageBytes = yourImageBytes,
            prompt = "Transform into modern villa"
        )
    }) {
        Text("Generate")
    }
    
    when (uiState) {
        is ImageGenerationUiState.Success -> {
            val image = (uiState as ImageGenerationUiState.Success).imageData
            // Display image
        }
        is ImageGenerationUiState.Loading -> CircularProgressIndicator()
        is ImageGenerationUiState.Error -> Text((uiState as ImageGenerationUiState.Error).message)
        else -> {}
    }
}
```

## 3. Example Prompts

**Real Estate:**
- "Transform this house into a luxury villa with modern architecture"
- "Add a swimming pool and landscaped garden to this property"
- "Show this house with solar panels and sustainable features"

**Interior:**
- "Modernize this living room with minimalist furniture"
- "Transform this kitchen with marble countertops and modern appliances"

**Exterior:**
- "Add a wraparound deck and outdoor entertainment area"
- "Show this house with fresh paint and new windows"

## 4. Files Created

**Core Implementation:**
- `domain/model/VertexAIModels.kt` - Data models
- `data/repository/VertexAIRepository.kt` - API integration
- `data/network/Base64Encoder.kt` - Image encoding (+ platform implementations)
- `data/network/ImageUtils.kt` - Helper utilities

**UI Layer:**
- `presentation/imagegen/ImageGenerationViewModel.kt` - Business logic
- `ui/imagegen/ImageGenerationScreen.kt` - Composable UI

**Configuration:**
- `di/AppModule.kt` - Dependency injection (updated)

## 5. Need Help?

See `VERTEX_AI_USAGE.md` for detailed documentation including:
- Error handling
- Advanced parameters
- Security best practices
- Troubleshooting guide

