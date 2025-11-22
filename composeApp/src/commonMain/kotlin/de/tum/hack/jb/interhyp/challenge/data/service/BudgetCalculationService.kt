package de.tum.hack.jb.interhyp.challenge.data.service

import de.tum.hack.jb.interhyp.challenge.domain.model.AffordabilityInput
import de.tum.hack.jb.interhyp.challenge.domain.model.AffordabilityResult
import de.tum.hack.jb.interhyp.challenge.domain.model.SavingsPlanInput
import de.tum.hack.jb.interhyp.challenge.domain.model.SavingsPlanResult
import kotlin.math.pow

/**
 * Service interface for budget calculation operations.
 * Provides forward and reverse calculations for property affordability.
 */
interface BudgetCalculationService {
    /**
     * Calculate maximum affordable property price based on user's financial profile.
     * Forward calculation: Given age, purchase age, current savings, income, and saving rate,
     * determine the maximum property price the person can afford.
     */
    fun calculateMaxAffordableProperty(input: AffordabilityInput): AffordabilityResult

    /**
     * Calculate required savings amount and duration to afford a target property.
     * Reverse calculation: Given a dream property price, determine how much and for how long
     * the person needs to save.
     */
    fun calculateSavingsPlan(input: SavingsPlanInput): SavingsPlanResult
}

/**
 * Implementation of BudgetCalculationService with Interhyp-style calculation logic.
 */
class BudgetCalculationServiceImpl : BudgetCalculationService {

    companion object {
        // Standard loan calculation parameters (matching BudgetRepository)
        private const val INTEREST_RATE = 0.04 // 4% annual interest rate
        private const val LOAN_TERM_YEARS = 30
        private const val MAX_DEBT_TO_INCOME_RATIO = 0.35 // Max 35% of income for loan payment
        private const val MIN_EQUITY_RATIO = 0.20 // Minimum 20% equity required
    }

    override fun calculateMaxAffordableProperty(input: AffordabilityInput): AffordabilityResult {
        // Calculate years until purchase
        val yearsUntilPurchase = input.purchaseAge - input.age
        
        // Calculate monthly savings based on saving rate
        // savingRate is a percentage (0.0 to 1.0) of income that is saved
        val monthlySavings = input.monthlyIncome * input.savingRate
        
        // Calculate future savings at purchase age
        // Future savings = current savings + (monthly savings × 12 × years)
        val futureSavings = input.currentSavings + (monthlySavings * 12 * yearsUntilPurchase)
        
        // Calculate maximum monthly payment (35% of income - DTI ratio)
        val maxMonthlyPayment = input.monthlyIncome * MAX_DEBT_TO_INCOME_RATIO
        
        // Calculate maximum loan amount using present value of annuity formula
        val monthlyInterestRate = INTEREST_RATE / 12
        val totalPayments = LOAN_TERM_YEARS * 12
        val maxLoanAmount = calculateLoanAmount(maxMonthlyPayment, monthlyInterestRate, totalPayments)
        
        // Calculate maximum property price (loan + future savings)
        val maxPropertyPrice = maxLoanAmount + futureSavings
        
        // Calculate required equity (20% of max property price)
        val requiredEquity = maxPropertyPrice * MIN_EQUITY_RATIO
        
        return AffordabilityResult(
            maxPropertyPrice = maxPropertyPrice,
            futureSavings = futureSavings,
            maxLoanAmount = maxLoanAmount,
            monthlyPayment = maxMonthlyPayment,
            requiredEquity = requiredEquity
        )
    }

    override fun calculateSavingsPlan(input: SavingsPlanInput): SavingsPlanResult {
        // Calculate maximum monthly payment (35% of income - DTI ratio)
        val maxMonthlyPayment = input.monthlyIncome * MAX_DEBT_TO_INCOME_RATIO
        
        // Calculate maximum loan amount using present value of annuity formula
        val monthlyInterestRate = INTEREST_RATE / 12
        val totalPayments = LOAN_TERM_YEARS * 12
        val maxLoanAmount = calculateLoanAmount(maxMonthlyPayment, monthlyInterestRate, totalPayments)
        
        // Calculate required equity (property price - max loan amount)
        val requiredEquity = input.targetPropertyPrice - maxLoanAmount
        
        // Calculate savings gap (required equity - current savings)
        val savingsGap = requiredEquity - input.currentSavings
        
        // Calculate monthly savings capacity
        val monthlySavings = input.monthlyIncome - input.monthlyExpenses
        
        // Determine if the goal is achievable
        // If requiredEquity <= 0, property is affordable with loan alone (no additional savings needed)
        // If requiredEquity > 0, check if we can save enough and if loan is available
        val isAchievable = if (requiredEquity <= 0) {
            true // Property is affordable with loan alone
        } else {
            savingsGap <= 0 || (monthlySavings > 0 && maxLoanAmount > 0) // Either already have enough savings or can save enough
        }
        
        // Calculate time needed to save
        val monthsToSave = if (isAchievable && monthlySavings > 0) {
            (savingsGap / monthlySavings).toInt()
        } else {
            Int.MAX_VALUE // Unachievable
        }
        
        val yearsToSave = if (monthsToSave != Int.MAX_VALUE) {
            (monthsToSave / 12.0).toInt()
        } else {
            Int.MAX_VALUE
        }
        
        return SavingsPlanResult(
            requiredSavings = requiredEquity,
            monthlySavingsNeeded = monthlySavings,
            yearsToSave = yearsToSave,
            monthsToSave = monthsToSave,
            isAchievable = isAchievable
        )
    }

    /**
     * Calculate loan amount using present value of annuity formula.
     * PV = PMT × [(1 - (1 + r)^-n) / r]
     * 
     * @param monthlyPayment The monthly payment amount
     * @param monthlyInterestRate The monthly interest rate (annual rate / 12)
     * @param totalPayments The total number of payments (years × 12)
     * @return The maximum loan amount that can be borrowed
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

