package de.tum.hack.jb.interhyp.challenge.data.repository

import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import de.tum.hack.jb.interhyp.challenge.data.service.BudgetCalculationService
import de.tum.hack.jb.interhyp.challenge.domain.model.SavingsPlanInput
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
 * Uses reverse budget calculator to determine down payment goal based on property price and user's financial capacity.
 */
class BudgetRepositoryImpl(
    private val budgetCalculationService: BudgetCalculationService
) : BudgetRepository {
    
    companion object {
        // Standard loan calculation parameters
        private const val INTEREST_RATE = 0.04 // 4% annual interest rate
        private const val LOAN_TERM_YEARS = 30
        private const val MAX_DEBT_TO_INCOME_RATIO = 0.35 // Max 35% of income for loan payment
        // Minimum equity threshold when property is affordable with loan alone
        private const val MIN_EQUITY_THRESHOLD = 0.0
    }
    
    override suspend fun calculateBudget(
        userProfile: UserProfile,
        propertyPrice: Double
    ): Flow<NetworkResult<BudgetCalculation>> = flow {
        emit(NetworkResult.Loading)
        
        try {
            val monthlySavings = userProfile.calculateMonthlySavings()
            val currentEquity = userProfile.currentEquity
            
            // Use reverse budget calculator to determine required down payment
            val savingsPlanInput = SavingsPlanInput(
                targetPropertyPrice = propertyPrice,
                currentAge = userProfile.age,
                currentSavings = currentEquity,
                monthlyIncome = userProfile.monthlyIncome,
                monthlyExpenses = userProfile.monthlyExpenses
            )
            
            val savingsPlanResult = budgetCalculationService.calculateSavingsPlan(savingsPlanInput)
            
            // Calculate maximum monthly payment (35% of net income)
            val maxMonthlyPayment = userProfile.monthlyIncome * MAX_DEBT_TO_INCOME_RATIO
            
            // Calculate maximum loan amount based on monthly payment capacity
            val monthlyInterestRate = INTEREST_RATE / 12
            val totalPayments = LOAN_TERM_YEARS * 12
            val maxLoanAmount = calculateLoanAmount(maxMonthlyPayment, monthlyInterestRate, totalPayments)
            
            // Use required savings from reverse calculator, but ensure it's not negative
            // If property is affordable with loan alone (requiredSavings <= 0), set to minimum threshold
            val requiredEquity = if (savingsPlanResult.requiredSavings <= 0) {
                MIN_EQUITY_THRESHOLD
            } else {
                savingsPlanResult.requiredSavings
            }
            
            // Calculate total property budget (loan + current equity)
            val totalPropertyBudget = maxLoanAmount + currentEquity
            
            // Calculate savings gap (required equity - current equity)
            val savingsGap = if (currentEquity < requiredEquity) {
                requiredEquity - currentEquity
            } else {
                0.0
            }
            
            // Use months to save from savings plan result, or calculate if not available
            // Handle edge cases: unachievable (Int.MAX_VALUE), already achieved (negative or 0), or valid positive value
            val monthsToTarget = when {
                savingsPlanResult.monthsToSave == Int.MAX_VALUE -> {
                    // Goal is unachievable, calculate based on savings gap as fallback
                    if (savingsGap > 0 && monthlySavings > 0) {
                        (savingsGap / monthlySavings).toInt()
                    } else {
                        Int.MAX_VALUE // Still unachievable
                    }
                }
                savingsPlanResult.monthsToSave <= 0 -> {
                    // User already has enough savings (negative means they have more than needed)
                    0
                }
                else -> {
                    // Valid positive value
                    savingsPlanResult.monthsToSave
                }
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
