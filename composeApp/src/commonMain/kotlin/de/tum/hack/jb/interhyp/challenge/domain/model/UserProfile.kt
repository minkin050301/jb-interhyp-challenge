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
    val futureMonthlyIncome: Double? = null, // Expected future income
    val monthlyExpenses: Double,
    val currentEquity: Double, // Current wealth/savings
    val existingCredits: Double = 0.0, // Existing credit obligations (monthly)
    val desiredLocation: String,
    val desiredPropertySize: Double, // in square meters
    val desiredPropertyType: PropertyType = PropertyType.APARTMENT,
    val targetDate: String? = null, // When user wants to purchase (e.g., "2026-12")
    val desiredChildren: Int = 0, // Future children planned
    val avatarImage: String? = null, // Base64 encoded image or URL
    val familyMembers: List<Person> = emptyList(),
    val goalPropertyPrice: Double? = null,
    val goalPropertySize: Double? = null
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
            familyMembers = familyMembers,
            goalPropertyPrice = goalPropertyPrice,
            goalPropertySize = goalPropertySize
        )
    }
}
