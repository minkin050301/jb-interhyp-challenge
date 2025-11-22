package de.tum.hack.jb.interhyp.challenge.di

import de.tum.hack.jb.interhyp.challenge.data.network.KtorClientFactory
import de.tum.hack.jb.interhyp.challenge.data.repository.*
import de.tum.hack.jb.interhyp.challenge.domain.model.VertexAIConfig
import de.tum.hack.jb.interhyp.challenge.presentation.dashboard.DashboardViewModel
import de.tum.hack.jb.interhyp.challenge.presentation.onboarding.OnboardingViewModel
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel
import io.ktor.client.*
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Network module providing HTTP client
 */
val networkModule = module {
    single<HttpClient> { KtorClientFactory.create() }
}

/**
 * Data module providing repository implementations
 */
val dataModule = module {
    single<PropertyRepository> { PropertyRepositoryImpl(get()) }
    single<BudgetRepository> { BudgetRepositoryImpl() }
    single<UserRepository> { UserRepositoryImpl() }
    
    // Vertex AI configuration - Update these values with your project details
    single<VertexAIConfig> {
        VertexAIConfig(
            projectId = System.getenv("VERTEX_AI_PROJECT_ID") ?: "your-project-id",
            location = System.getenv("VERTEX_AI_LOCATION") ?: "global",
            apiKey = System.getenv("VERTEX_AI_API_KEY")
        )
    }
    
    single<VertexAIRepository> { VertexAIRepositoryImpl(get(), get()) }
}

/**
 * Presentation module providing ViewModels
 */
val presentationModule = module {
    factory { OnboardingViewModel(get(), get(), get()) }
    factory { DashboardViewModel(get(), get(), get()) }
    single { ThemeViewModel() }
}

/**
 * Complete app module combining all modules
 */
val appModule = listOf(
    networkModule,
    dataModule,
    presentationModule
)
