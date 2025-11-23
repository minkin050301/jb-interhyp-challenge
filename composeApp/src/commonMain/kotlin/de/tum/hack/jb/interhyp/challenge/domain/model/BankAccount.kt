package de.tum.hack.jb.interhyp.challenge.domain.model

import de.tum.hack.jb.interhyp.challenge.util.currentTimeMillis
import kotlinx.serialization.Serializable

/**
 * Represents a mocked bank account with balance and transaction history
 */
@Serializable
data class BankAccount(
    val id: String,
    val userId: String,
    val accountName: String = "Main Account",
    val balance: Double,
    val transactions: List<Transaction> = emptyList(),
    val lastUpdated: Long = currentTimeMillis(), // Unix timestamp
    val historicalBalances: Map<Long, Double> = emptyMap() // Map of month start timestamp -> balance at end of that month
) {
    /**
     * Calculate current balance from transactions
     */
    fun calculateBalanceFromTransactions(): Double {
        return transactions.sumOf { it.getSignedAmount() }
    }
    
    /**
     * Get total income for a given period
     */
    fun getTotalIncome(startDate: Long? = null, endDate: Long? = null): Double {
        return transactions
            .filter { it.isIncome() }
            .filter { transaction ->
                when {
                    startDate != null && endDate != null -> 
                        transaction.date >= startDate && transaction.date <= endDate
                    startDate != null -> transaction.date >= startDate
                    endDate != null -> transaction.date <= endDate
                    else -> true
                }
            }
            .sumOf { it.amount }
    }
    
    /**
     * Get total expenses for a given period
     */
    fun getTotalExpenses(startDate: Long? = null, endDate: Long? = null): Double {
        return transactions
            .filter { it.isExpense() }
            .filter { transaction ->
                when {
                    startDate != null && endDate != null -> 
                        transaction.date >= startDate && transaction.date <= endDate
                    startDate != null -> transaction.date >= startDate
                    endDate != null -> transaction.date <= endDate
                    else -> true
                }
            }
            .sumOf { it.amount }
    }
    
    /**
     * Get net savings (income - expenses) for a given period
     */
    fun getNetSavings(startDate: Long? = null, endDate: Long? = null): Double {
        return getTotalIncome(startDate, endDate) - getTotalExpenses(startDate, endDate)
    }
    
    /**
     * Get transactions for current month
     */
    fun getCurrentMonthTransactions(): List<Transaction> {
        val now = currentTimeMillis()
        val monthStart = de.tum.hack.jb.interhyp.challenge.util.getStartOfMonth(now)
        
        return transactions.filter { it.date >= monthStart }
    }
}

