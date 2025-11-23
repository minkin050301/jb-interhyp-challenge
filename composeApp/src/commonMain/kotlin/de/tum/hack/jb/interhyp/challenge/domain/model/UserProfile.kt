package de.tum.hack.jb.interhyp.challenge.domain.model

import kotlinx.serialization.Serializable

/**
 * UserProfile data class for collecting onboarding information.
 * Used to gather initial user data: Income, Equity, Location, and Desired Size.
 */
@Serializable
data class UserProfile(
    val userId: String,
    val name: String,
    val age: Int,
    val monthlyIncome: Double,
    val monthlyExpenses: Double,
    val currentEquity: Double, // Current wealth/savings
    val desiredLocation: String,
    val desiredPropertySize: Double, // in square meters
    val desiredPropertyType: PropertyType = PropertyType.APARTMENT,
    val goalPropertyPrice: Double? = null,
    val goalPropertySize: Double? = null,
    val goalPropertyImageUrl: String? = null
) {
    /**
     * Calculate monthly savings capacity
     */
    fun calculateMonthlySavings(): Double {
        return monthlyIncome - monthlyExpenses
    }
    
    /**
     * Convert UserProfile to User entity
     */
    fun toUser(): User {
        return User(
            id = userId,
            name = name,
            age = age,
            netIncome = monthlyIncome,
            expenses = monthlyExpenses,
            wealth = currentEquity,
            goalPropertyPrice = goalPropertyPrice,
            goalPropertySize = goalPropertySize,
            goalPropertyImageUrl = goalPropertyImageUrl
        )
    }
}
