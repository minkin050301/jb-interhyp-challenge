SYSTEM CONTEXT:
I am building "DreamBuilder," a Kotlin Multiplatform (KMP) app for the HackaTUM 2025 Interhyp Challenge.

STACK:
- Language: Kotlin
- UI: Compose Multiplatform (Android, iOS, Web)
- Networking: Ktor Client (ContentNegotiation, Serialization)
- DI: Koin
- Navigation: Voyager
- Versioning: Gradle Version Catalogs (libs.versions.toml)

THE GOAL:
A gamified budget tracker. Instead of a progress bar, users build a "Virtual House" by saving money. The app calculates the required savings gap using real estate APIs.

CORE REQUIREMENTS:
1. Onboarding Screen: Collect Income, Equity, Location, and Desired Size.
2. API Integration 1: Connect to ThinkImmo API to get average property prices. Handle errors by falling back to default values.
3. API Integration 2: Simulate Interhyp Budget Calculator logic to find max loan capacity.
4. Dashboard: Display a house asset that changes state (Foundation -> Walls -> Roof) based on (Current Savings / Target Savings) %.
5. Persistence: Save user progress locally using DataStore or SQLDelight. App must work offline.

CODING RULES:
- Use Material 3 Design components.
- Use Unidirectional Data Flow (StateFlow in ViewModels).
- Place all UI and Business Logic in `commonMain`.
- Use `compose.resources` for images/strings.
- Create a `NetworkResult` wrapper class (Success/Error/Loading) for API calls.

TASK:
Please generate the project file structure, the `libs.versions.toml` file with KMP dependencies, and the Kotlin data classes required to model the UserProfile and PropertyListing.