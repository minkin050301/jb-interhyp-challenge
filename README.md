# DreamBuilder - HackaTUM 2025 Interhyp Challenge

DreamBuilder is a gamified budget tracker and property visualization application built with Kotlin Multiplatform (KMP). It helps users plan their financial future and visualize their dream home using AI.

## 📋 Overview

DreamBuilder guides users through an interactive onboarding process to capture their financial status and housing preferences. It then provides a dashboard to track savings progress towards a down payment, simulating the home-building process.

**Key Features:**
- **Cross-Platform**: Runs on Android, iOS, and Web.
- **Gamified Savings**: Visualizes progress as a house being built (Foundation → Walls → Roof).
- **AI Visualization**: Uses Google Vertex AI (Gemini 3 Pro) to transform property images based on prompts.
- **Selfie Integration**: Personalizes the experience with user photos.
- **Localization**: Supports English and German.
- **Offline Capable**: Fully functional without a network connection (except for AI features).

---

## 🛠 Technology Stack

- **Language**: Kotlin Multiplatform (KMP)
- **UI**: Compose Multiplatform
- **Architecture**: Clean Architecture (Presentation, Domain, Data)
- **Dependency Injection**: Koin 4.0.1
- **Networking**: Ktor Client 3.0.2
- **AI**: Google Cloud Vertex AI (Gemini 3 Pro Image Model)
- **Navigation**: Voyager
- **Serialization**: kotlinx-serialization
- **Build System**: Gradle

---

## 🚀 Getting Started

### Prerequisites

To use the AI image generation features, you need Google Cloud Vertex AI credentials.

1.  **Get Credentials**:
    *   Go to [Google Cloud Console](https://console.cloud.google.com/).
    *   Create a project and enable the **Vertex AI API**.
    *   Create an API Key.

2.  **Configure Environment**:
    Set the following environment variables locally or in your CI/CD pipeline:
    ```bash
    export VERTEX_AI_PROJECT_ID="your-project-id"
    export VERTEX_AI_LOCATION="global" # Optional
    ```
    *Alternatively, for development, you can temporarily hardcode these in `composeApp/src/commonMain/kotlin/de/tum/hack/jb/interhyp/challenge/di/AppModule.kt` (not recommended).*

### Running the App

**Android:**
```bash
./gradlew :composeApp:installDebug
```

**iOS (Simulator):**
Open `iosApp/iosApp.xcodeproj` in Xcode and run, or use the KMP wizard run configurations if available.

**Web (Development):**
```bash
./gradlew :composeApp:jsBrowserDevelopmentRun
```
The app will be available at `http://localhost:8080`.

---

## 🏗 Architecture

The project follows Clean Architecture principles:

```
composeApp/src/commonMain/kotlin/de/tum/hack/jb/interhyp/challenge/
├── presentation/       # UI logic (ViewModels)
│   ├── onboarding/
│   └── dashboard/
├── domain/            # Business models & entities
│   └── model/
├── data/              # Data sources & repositories
│   ├── network/
│   └── repository/
└── di/                # Koin dependency injection modules
```

### Key Repositories
*   **`PropertyRepository`**: Fetches property data (simulated ThinkImmo API).
*   **`BudgetRepository`**: logic for loan capacity and savings gaps.
*   **`UserRepository`**: Manages user persistence.
*   **`VertexAIRepository`**: Handles interaction with Gemini 3 Pro for image generation.

---

## ✨ Features in Detail

### 1. Onboarding & Selfie
A 4-step flow collecting user data:
1.  **Welcome**: Basic info.
2.  **Target**: Desired property type and location.
3.  **Financials**: Income, expenses, equity.
4.  **Selfie**: Take or upload a profile photo (stored as Base64).

### 2. Dashboard & Gamification
*   **Progress**: Visualizes savings as a house under construction.
    *   Foundation: 0-33%
    *   Walls: 34-66%
    *   Roof: 67-100%
*   **Budget**: Calculates max loan amount, required equity, and savings gap based on 4% interest and 30-year term.

### 3. Vertex AI Image Generation
Transform property images using text prompts (e.g., "Modernize this kitchen", "Add a pool").

*   **Integration**: The `VertexAIRepository` handles API calls.
*   **Usage**: Inject the repository into ViewModels.
*   **Models**: Uses `Gemini 3 Pro Image Preview`.
*   **Platform Support**: Native Base64 encoding for Android, iOS, JS, and Wasm.

**Example Usage:**
```kotlin
vertexAIRepository.generateImage(
    prompt = "Transform into modern villa",
    inputImageBase64 = base64String,
    temperature = 0.8
).collect { result -> /* Handle Success/Error/Loading */ }
```

### 4. Localization
The app supports **English (en)** and **German (de)**.
*   Resources located in `composeApp/src/commonMain/composeResources/values/strings.xml` (and `values-de`).
*   Usage: `stringResource(Res.string.key)`.

---

## 🌍 Web Deployment

To build the web version for deployment:

1.  **Build Production Bundle**:
    ```bash
    ./gradlew :composeApp:jsBrowserDistribution
    ```
    or use the convenience task:
    ```bash
    ./gradlew :composeApp:webDist
    ```

2.  **Deploy**:
    Upload the contents of `composeApp/build/dist/web` to any static host (GitHub Pages, Netlify, Vercel).
