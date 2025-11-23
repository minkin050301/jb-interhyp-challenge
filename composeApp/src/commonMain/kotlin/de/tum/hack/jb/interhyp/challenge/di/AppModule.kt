package de.tum.hack.jb.interhyp.challenge.di

import de.tum.hack.jb.interhyp.challenge.data.network.KtorClientFactory
import de.tum.hack.jb.interhyp.challenge.data.repository.*
import de.tum.hack.jb.interhyp.challenge.data.service.BudgetCalculationService
import de.tum.hack.jb.interhyp.challenge.data.service.BudgetCalculationServiceImpl
import de.tum.hack.jb.interhyp.challenge.data.service.BudgetSyncService
import de.tum.hack.jb.interhyp.challenge.data.service.BudgetSyncServiceImpl
import de.tum.hack.jb.interhyp.challenge.data.service.MonthlyReminderService
import de.tum.hack.jb.interhyp.challenge.data.service.MonthlyReminderServiceImpl
import de.tum.hack.jb.interhyp.challenge.data.service.MonthSimulationService
import de.tum.hack.jb.interhyp.challenge.data.service.MonthSimulationServiceImpl
import de.tum.hack.jb.interhyp.challenge.domain.model.VertexAIConfig
import de.tum.hack.jb.interhyp.challenge.presentation.dashboard.DashboardViewModel
import de.tum.hack.jb.interhyp.challenge.presentation.goal.GoalSelectionViewModel
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
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl() }
    single<BudgetCalculationService> { BudgetCalculationServiceImpl() }
    single<BudgetTrackingRepository> { BudgetTrackingRepositoryImpl() }
    single<MonthlyReminderService> { MonthlyReminderServiceImpl(get()) }
    single<BudgetSyncService> { BudgetSyncServiceImpl(get(), get()) }
    single<MonthSimulationService> { MonthSimulationServiceImpl(get(), get()) }

    // Vertex AI configuration - Update these values with your project details
    single<VertexAIConfig> {
        VertexAIConfig(
            projectId = "hackatum25mun-1100",
            location = "us-central1",
            accessToken = "ya29.a0ATi6K2sf9zci-i1MMYV2i56f1wTSqnooLqQ8dGSvrJWG0EMnysvg2t_2yDDNozdvFEucKgNhks03LrKgEw4JMty8pZCYC345kFBxybcEdSdqz2BwaYm5XgPZytfPtzwi4HzH7UsKujPxA-HxJkGKjr1UC3ufmyHlc50QNkCfUqP-_r7o4mJAGgUnPCcasKMzxO2Ffqo1uLVIvAaCgYKAakSARcSFQHGX2Mi4nH27Pcd8zSGira2CY9Xpg0213"
        )
    }
    
    single<VertexAIRepository> { VertexAIRepositoryImpl(get(), get()) }
}

/**
 * Presentation module providing ViewModels
 */
val presentationModule = module {
    factory { OnboardingViewModel(get(), get(), get(), get()) }
    single { DashboardViewModel(get(), get(), get(), get()) }
    factory { ProfileViewModel(get()) }
    factory { InsightsViewModel(get(), get(), get(), get()) }
    factory { GoalSelectionViewModel(get(), get()) }
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
