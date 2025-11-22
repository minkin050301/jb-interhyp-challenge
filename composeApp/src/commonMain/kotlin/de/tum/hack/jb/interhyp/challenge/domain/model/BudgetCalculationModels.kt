package de.tum.hack.jb.interhyp.challenge.domain.model

import kotlinx.serialization.Serializable

/**
 * Input parameters for calculating maximum affordable property price.
 */
@Serializable
data class AffordabilityInput(
    val age: Int,
    val purchaseAge: Int,
    val currentSavings: Double,
    val monthlyIncome: Double,
    val monthlyExpenses: Double,
    val savingRate: Double // Percentage of income saved (0.0 to 1.0)
)

/**
 * Result of affordability calculation showing maximum property price a person can afford.
 */
@Serializable
data class AffordabilityResult(
    val maxPropertyPrice: Double,
    val futureSavings: Double,
    val maxLoanAmount: Double,
    val monthlyPayment: Double,
    val requiredEquity: Double
)

/**
 * Input parameters for calculating savings plan to afford a target property.
 */
@Serializable
data class SavingsPlanInput(
    val targetPropertyPrice: Double,
    val currentAge: Int,
    val currentSavings: Double,
    val monthlyIncome: Double,
    val monthlyExpenses: Double
)

/**
 * Result of savings plan calculation showing required savings and time needed.
 */
@Serializable
data class SavingsPlanResult(
    val requiredSavings: Double,
    val monthlySavingsNeeded: Double,
    val yearsToSave: Int,
    val monthsToSave: Int,
    val isAchievable: Boolean
)

