# DreamBuilder Backend Implementation

## Overview
This document describes the backend architecture and implementation for the DreamBuilder KMP application - a gamified budget tracker for the HackaTUM 2025 Interhyp Challenge.

## Architecture

### Clean Architecture Layers

```
presentation/       # ViewModels with StateFlow (UI logic)
  ├── onboarding/
  └── dashboard/

domain/            # Business models and entities
  └── model/

data/              # Data sources and repositories
  ├── network/     # HTTP client and API handling
  └── repository/  # Repository implementations

di/                # Dependency injection (Koin modules)
```

## Technology Stack

- **Language**: Kotlin Multiplatform
- **UI**: Compose Multiplatform (Android, iOS, Web)
- **Networking**: Ktor Client 3.0.2
  - ContentNegotiation with JSON serialization
  - Platform-specific engines (Android, Darwin, JS)
- **DI**: Koin 4.0.1
- **Navigation**: Voyager 1.1.0-beta03
- **Serialization**: kotlinx-serialization 1.7.3
- **Persistence**: DataStore 1.1.1 / SQLDelight 2.0.2

## Domain Models

### User Entity
```kotlin
data class User(
    val id: String,
    val name: String,
    val age: Int,
    val netIncome: Double,
    val expenses: Double,
    val wealth: Double,
    val image: String? = null,
    val coupons: List<String> = emptyList(),
    val familyMembers: List<Person> = emptyList(),
    val savedPropertyId: String? = null
)
```

### Property Entity
```kotlin
data class Property(
    val id: String,
    val location: String,
    val price: Double,
    val images: List<String> = emptyList(),
    val size: Double? = null,
    val type: PropertyType = PropertyType.APARTMENT,
    val description: String? = null
)
```

### Person Entity
```kotlin
data class Person(
    val id: String,
    val age: Int,
    val gender: Gender,
    val image: String? = null
)
```

### UserProfile (Onboarding Data)
```kotlin
data class UserProfile(
    val userId: String,
    val name: String,
    val age: Int,
    val monthlyIncome: Double,
    val monthlyExpenses: Double,
    val currentEquity: Double,
    val desiredLocation: String,
    val desiredPropertySize: Double,
    val desiredPropertyType: PropertyType = PropertyType.APARTMENT,
    val familyMembers: List<Person> = emptyList()
)
```

### PropertyListing (API Response)
```kotlin
data class PropertyListing(
    val id: String,
    val location: String,
    val averagePrice: Double,
    val pricePerSqm: Double,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val availableProperties: Int = 0,
    val marketTrend: MarketTrend = MarketTrend.STABLE
)
```

## Network Layer

### NetworkResult Wrapper
```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}
```

### Ktor Client Configuration
- JSON serialization with kotlinx-serialization
- Logging for debugging
- Content negotiation
- Platform-specific engines

## Repository Layer

### PropertyRepository
**Purpose**: Fetch property data from ThinkImmo API with fallback values

**Methods**:
- `getPropertyListings(location: String)`: Get properties for location
- `getAveragePrice(location: String, size: Double)`: Calculate property price

**Fallback Prices** (EUR per sqm):
- Munich: 9,000
- Berlin: 6,000
- Hamburg: 5,500
- Frankfurt: 6,500
- Cologne: 4,500
- Stuttgart: 5,000
- Default: 5,000

### BudgetRepository
**Purpose**: Simulate Interhyp Budget Calculator logic

**Methods**:
- `calculateBudget(userProfile: UserProfile, propertyPrice: Double)`: Calculate loan capacity and savings gap

**Calculation Parameters**:
- Interest Rate: 4% annually
- Loan Term: 30 years
- Max Debt-to-Income Ratio: 35%
- Minimum Equity Required: 20%

**BudgetCalculation Result**:
```kotlin
data class BudgetCalculation(
    val maxLoanAmount: Double,
    val monthlyPayment: Double,
    val totalPropertyBudget: Double,
    val requiredEquity: Double,
    val savingsGap: Double,
    val monthsToTarget: Int
)
```

### UserRepository
**Purpose**: Manage user data persistence

**Methods**:
- `saveUser(user: User)`: Save user profile
- `getUser()`: Get current user (Flow)
- `updateWealth(userId: String, newWealth: Double)`: Update savings
- `clearUser()`: Clear user data
- `hasUser()`: Check if user exists

## Presentation Layer

### OnboardingViewModel
**Purpose**: Handle user profile data collection

**State**: `OnboardingUiState`
- User input fields (name, age, income, expenses, equity, location, size)
- Loading/error states
- Completion status

**Actions**:
- `updateName()`, `updateAge()`, etc. - Update form fields
- `submitOnboarding()` - Validate and save user profile

### DashboardViewModel
**Purpose**: Display house building progress and budget tracking

**State**: `DashboardUiState`
- Current user and savings
- Target savings and progress (0-100%)
- House building state (Foundation/Walls/Roof)
- Budget calculation results

**House State Logic**:
- Foundation: 0-33% of target savings
- Walls: 34-66% of target savings
- Roof: 67-100% of target savings

**Actions**:
- `updateSavings()` - Update user wealth
- `refresh()` - Reload dashboard data

## Dependency Injection (Koin)

### Network Module
```kotlin
val networkModule = module {
    single<HttpClient> { KtorClientFactory.create() }
}
```

### Data Module
```kotlin
val dataModule = module {
    single<PropertyRepository> { PropertyRepositoryImpl(get()) }
    single<BudgetRepository> { BudgetRepositoryImpl() }
    single<UserRepository> { UserRepositoryImpl() }
}
```

### Presentation Module
```kotlin
val presentationModule = module {
    factory { OnboardingViewModel(get(), get(), get()) }
    factory { DashboardViewModel(get(), get(), get()) }
}
```

## Key Features

### 1. Onboarding Flow
- Collect: Income, Equity, Location, Desired Size
- Validate input data
- Save user profile locally
- Calculate initial budget

### 2. API Integration
- **ThinkImmo API**: Fetch real estate prices
- **Fallback Strategy**: Use default values on API failure
- **Error Handling**: NetworkResult wrapper for all API calls

### 3. Budget Calculator
- Simulate Interhyp logic
- Calculate max loan amount using annuity formula
- Determine required equity (20% of property price)
- Calculate savings gap and months to target

### 4. Dashboard Visualization
- Display house building progress (Foundation → Walls → Roof)
- Show current vs target savings
- Calculate savings percentage
- Display budget breakdown

### 5. Offline Support
- UserRepository provides basic in-memory persistence
- Can be extended with DataStore or SQLDelight
- App works without network connectivity

## Usage

### Initialize Koin in App
```kotlin
fun main() {
    startKoin {
        modules(appModule)
    }
}
```

### Use ViewModels in Screens
```kotlin
@Composable
fun OnboardingScreen() {
    val viewModel: OnboardingViewModel = koinInject()
    val state by viewModel.uiState.collectAsState()
    
    // UI implementation
}
```

## Future Enhancements

1. **DataStore Implementation**: Replace in-memory user storage with DataStore Preferences
2. **SQLDelight Integration**: Store property listings and transaction history
3. **Real ThinkImmo API**: Implement actual API endpoints when available
4. **Gamification**: Add achievements, badges, and rewards system
5. **Multi-user Support**: Family member management and shared goals
6. **Cloud Sync**: Backend API for cross-device synchronization

## File Structure

```
composeApp/src/commonMain/kotlin/de/tum/hack/jb/interhyp/challenge/
├── data/
│   ├── network/
│   │   ├── KtorClientFactory.kt
│   │   └── NetworkResult.kt
│   └── repository/
│       ├── BudgetRepository.kt
│       ├── PropertyRepository.kt
│       └── UserRepository.kt
├── di/
│   └── AppModule.kt
├── domain/
│   └── model/
│       ├── Person.kt
│       ├── Property.kt
│       ├── PropertyListing.kt
│       ├── User.kt
│       └── UserProfile.kt
└── presentation/
    ├── dashboard/
    │   └── DashboardViewModel.kt
    └── onboarding/
        └── OnboardingViewModel.kt
```

## Testing

- All repositories return Flow for reactive data streams
- NetworkResult wrapper allows easy testing of success/error states
- ViewModels use StateFlow for predictable state management
- Koin modules can be replaced with test doubles

## License

HackaTUM 2025 - Interhyp Challenge
