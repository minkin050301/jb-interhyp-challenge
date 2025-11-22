package de.tum.hack.jb.interhyp.challenge.domain.model

import kotlinx.serialization.Serializable

/**
 * Transaction type: Income or Expense
 */
enum class TransactionType {
    INCOME,
    EXPENSE
}

/**
 * Transaction category for better organization
 */
enum class TransactionCategory {
    // Income categories
    SALARY,
    BONUS,
    INVESTMENT,
    RENTAL_INCOME,
    OTHER_INCOME,
    
    // Expense categories
    HOUSING,
    FOOD,
    TRANSPORTATION,
    UTILITIES,
    ENTERTAINMENT,
    HEALTHCARE,
    EDUCATION,
    SHOPPING,
    OTHER_EXPENSE
}

/**
 * Represents a single financial transaction (income or expense)
 */
@Serializable
data class Transaction(
    val id: String,
    val userId: String,
    val type: TransactionType,
    val amount: Double,
    val category: TransactionCategory,
    val description: String,
    val date: Long, // Unix timestamp in milliseconds
    val isRecurring: Boolean = false, // For monthly recurring transactions
    val recurringDay: Int? = null // Day of month for recurring transactions (1-31)
) {
    /**
     * Check if transaction is income
     */
    fun isIncome(): Boolean = type == TransactionType.INCOME
    
    /**
     * Check if transaction is expense
     */
    fun isExpense(): Boolean = type == TransactionType.EXPENSE
    
    /**
     * Get signed amount (positive for income, negative for expense)
     */
    fun getSignedAmount(): Double {
        return if (isIncome()) amount else -amount
    }
}

