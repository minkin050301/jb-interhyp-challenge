package de.tum.hack.jb.interhyp.challenge.domain.model

import kotlinx.serialization.Serializable

/**
 * User entity representing the main user of the DreamBuilder app.
 * The user saves for a property and has multiple family members.
 */
@Serializable
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
    val savedPropertyId: String? = null,
    val goalPropertyPrice: Double? = null,
    val goalPropertySize: Double? = null
) {
    /**
     * Calculates the monthly savings capacity
     */
    fun getMonthlySavings(): Double {
        return netIncome - expenses
    }
    
    /**
     * Calculates total available funds for property purchase
     */
    fun getTotalAvailableFunds(): Double {
        return wealth
    }
}
