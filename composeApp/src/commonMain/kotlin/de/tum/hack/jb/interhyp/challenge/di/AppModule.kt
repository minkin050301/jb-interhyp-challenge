package de.tum.hack.jb.interhyp.challenge.di

import de.tum.hack.jb.interhyp.challenge.data.network.KtorClientFactory
import de.tum.hack.jb.interhyp.challenge.data.repository.*
import de.tum.hack.jb.interhyp.challenge.data.service.BudgetCalculationService
import de.tum.hack.jb.interhyp.challenge.data.service.BudgetCalculationServiceImpl
import de.tum.hack.jb.interhyp.challenge.data.service.BudgetSyncService
import de.tum.hack.jb.interhyp.challenge.data.service.BudgetSyncServiceImpl
import de.tum.hack.jb.interhyp.challenge.data.service.MonthlyReminderService
import de.tum.hack.jb.interhyp.challenge.data.service.MonthlyReminderServiceImpl
import de.tum.hack.jb.interhyp.challenge.domain.model.VertexAIConfig
import de.tum.hack.jb.interhyp.challenge.presentation.dashboard.DashboardViewModel
import de.tum.hack.jb.interhyp.challenge.presentation.insights.InsightsViewModel
import de.tum.hack.jb.interhyp.challenge.presentation.onboarding.OnboardingViewModel
import de.tum.hack.jb.interhyp.challenge.presentation.profile.ProfileViewModel
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel
import de.tum.hack.jb.interhyp.challenge.presentation.locale.LocaleViewModel
import io.ktor.client.*
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
    single<BudgetCalculationService> { BudgetCalculationServiceImpl() }
    single<BudgetTrackingRepository> { BudgetTrackingRepositoryImpl() }
    single<MonthlyReminderService> { MonthlyReminderServiceImpl(get()) }
    single<BudgetSyncService> { BudgetSyncServiceImpl(get(), get()) }

    // Vertex AI configuration - Update these values with your project details
    single<VertexAIConfig> {
        VertexAIConfig(
            projectId = "hackatum25mun-1100",
            location = "us-central1",
            accessToken = "ya29.a0ATi6K2uHUjwiaSNcF6LPC9VkADu9z7uQIpvSjWAdFPwu_2pl9VV4IrC9GKOFlZNT4P_7Az7KwOQQWn3UuCTvbGGLTLZLUPf8nQA8u8YcZNBE6zyt4wogJbjHu01SlX-XQDMy75msFAFFA5vnr_aTuk2pfZz4k8uViiDU8kcL69JHBcDXazfvEfawNADK8cE9dPZtLmwRWnNIZAaCgYKAUUSARcSFQHGX2MiUs7zhTT61pWimE3EGtCiXA0213"
        )
    }
    
    single<VertexAIRepository> { VertexAIRepositoryImpl(get(), get()) }
}

/**
 * Presentation module providing ViewModels
 */
val presentationModule = module {
    factory { OnboardingViewModel(get(), get(), get(), get()) }
    factory { DashboardViewModel(get(), get(), get()) }
    factory { ProfileViewModel(get()) }
    factory { InsightsViewModel(get(), get(), get(), get()) }
    single { ThemeViewModel() }
    single { LocaleViewModel() }
}

/**
 * Complete app module combining all modules
 */
val appModule = listOf(
    networkModule,
    dataModule,
    presentationModule
)
