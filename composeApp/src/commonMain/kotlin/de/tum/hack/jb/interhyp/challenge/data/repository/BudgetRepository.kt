package de.tum.hack.jb.interhyp.challenge.data.repository

import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import de.tum.hack.jb.interhyp.challenge.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.pow

/**
 * Data class representing budget calculation result
 */
data class BudgetCalculation(
    val maxLoanAmount: Double,
    val monthlyPayment: Double,
    val totalPropertyBudget: Double,
    val requiredEquity: Double,
    val savingsGap: Double,
    val monthsToTarget: Int
)

/**
 * Repository interface for budget calculation operations.
 */
interface BudgetRepository {
    /**
     * Calculate maximum loan capacity and savings gap.
     * Simulates Interhyp Budget Calculator logic.
     */
    suspend fun calculateBudget(
        userProfile: UserProfile,
        propertyPrice: Double
    ): Flow<NetworkResult<BudgetCalculation>>
}

/**
 * Implementation of BudgetRepository with Interhyp logic simulation.
 */
class BudgetRepositoryImpl : BudgetRepository {
    
    companion object {
        // Standard loan calculation parameters
        private const val INTEREST_RATE = 0.04 // 4% annual interest rate
        private const val LOAN_TERM_YEARS = 30
        private const val MAX_DEBT_TO_INCOME_RATIO = 0.35 // Max 35% of income for loan payment
        private const val MIN_EQUITY_RATIO = 0.20 // Minimum 20% equity required
    }
    
    override suspend fun calculateBudget(
        userProfile: UserProfile,
        propertyPrice: Double
    ): Flow<NetworkResult<BudgetCalculation>> = flow {
        emit(NetworkResult.Loading)
        
        try {
            val monthlySavings = userProfile.calculateMonthlySavings()
            val currentEquity = userProfile.currentEquity
            
            // Calculate required equity (20% of property price)
            val requiredEquity = propertyPrice * MIN_EQUITY_RATIO
            
            // Calculate maximum monthly payment (35% of net income)
            val maxMonthlyPayment = userProfile.monthlyIncome * MAX_DEBT_TO_INCOME_RATIO
            
            // Calculate maximum loan amount based on monthly payment capacity
            val monthlyInterestRate = INTEREST_RATE / 12
            val totalPayments = LOAN_TERM_YEARS * 12
            val maxLoanAmount = calculateLoanAmount(maxMonthlyPayment, monthlyInterestRate, totalPayments)
            
            // Calculate total property budget (loan + equity)
            val totalPropertyBudget = maxLoanAmount + currentEquity
            
            // Calculate savings gap
            val savingsGap = if (currentEquity < requiredEquity) {
                requiredEquity - currentEquity
            } else {
                0.0
            }
            
            // Calculate months to reach target equity
            val monthsToTarget = if (savingsGap > 0 && monthlySavings > 0) {
                (savingsGap / monthlySavings).toInt()
            } else {
                0
            }
            
            val calculation = BudgetCalculation(
                maxLoanAmount = maxLoanAmount,
                monthlyPayment = maxMonthlyPayment,
                totalPropertyBudget = totalPropertyBudget,
                requiredEquity = requiredEquity,
                savingsGap = savingsGap,
                monthsToTarget = monthsToTarget
            )
            
            emit(NetworkResult.Success(calculation))
        } catch (e: Exception) {
            emit(NetworkResult.Error("Failed to calculate budget: ${e.message}", e))
        }
    }
    
    /**
     * Calculate loan amount using present value of annuity formula.
     * PV = PMT × [(1 - (1 + r)^-n) / r]
     */
    private fun calculateLoanAmount(
        monthlyPayment: Double,
        monthlyInterestRate: Double,
        totalPayments: Int
    ): Double {
        if (monthlyInterestRate == 0.0) {
            return monthlyPayment * totalPayments
        }
        
        val factor = (1 - (1 + monthlyInterestRate).pow(-totalPayments.toDouble())) / monthlyInterestRate
        return monthlyPayment * factor
    }
}
