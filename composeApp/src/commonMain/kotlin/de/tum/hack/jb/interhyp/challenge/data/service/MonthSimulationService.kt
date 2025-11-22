package de.tum.hack.jb.interhyp.challenge.data.service

import de.tum.hack.jb.interhyp.challenge.data.repository.BudgetTrackingRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import de.tum.hack.jb.interhyp.challenge.domain.model.Transaction
import de.tum.hack.jb.interhyp.challenge.domain.model.TransactionCategory
import de.tum.hack.jb.interhyp.challenge.domain.model.TransactionType
import de.tum.hack.jb.interhyp.challenge.util.*
import kotlinx.coroutines.flow.first
import kotlin.random.Random

/**
 * Service to simulate a month of transactions for testing/development
 */
interface MonthSimulationService {
    /**
     * Simulate transactions for the next month based on user's financial data
     * Includes salary at beginning of month and various expenses throughout
     * Final savings will be within ±10% of expected savings
     */
    suspend fun simulateNextMonth(userId: String)
}

/**
 * Implementation of MonthSimulationService
 */
class MonthSimulationServiceImpl(
    private val userRepository: UserRepository,
    private val budgetTrackingRepository: BudgetTrackingRepository
) : MonthSimulationService {
    
    override suspend fun simulateNextMonth(userId: String) {
        val user = userRepository.getUser().first()
        if (user == null) {
            println("Cannot simulate month: User not found")
            return
        }
        
        // Find the latest transaction date to determine which month to simulate next
        val existingTransactions = budgetTrackingRepository.getTransactions(userId).first()
        val latestTransactionDate = existingTransactions.maxOfOrNull { it.date }
        
        // Calculate target month: if we have transactions, simulate the month after the latest one
        // Otherwise, simulate next month from current time
        val targetDate = if (latestTransactionDate != null) {
            // Get the start of the month containing the latest transaction
            val latestMonthStart = getStartOfMonth(latestTransactionDate)
            
            // Add one month to the latest transaction date
            // Add ~32 days to get to next month, then get start of that month
            getStartOfMonth(latestMonthStart + 32L * 24 * 60 * 60 * 1000)
        } else {
            // No existing transactions, simulate next month from current time
            val now = currentTimeMillis()
            getStartOfMonth(now + 32L * 24 * 60 * 60 * 1000)
        }
        
        // Calculate month and year for the target date
        val targetYear = getYear(targetDate)
        val targetMonth = getMonth(targetDate)
        val targetMonthDisplay = targetMonth + 1 // For display/ID purposes (1-12)
        
        val nextMonthStart = targetDate
        
        // Expected monthly savings
        val expectedSavings = user.getMonthlySavings()
        
        // Add ±10% fluctuation to savings
        val fluctuation = Random.nextDouble(-0.10, 0.10)
        val targetSavings = expectedSavings * (1 + fluctuation)
        
        // Calculate target expenses (income - target savings)
        val targetExpenses = user.netIncome - targetSavings
        
        val transactions = mutableListOf<Transaction>()
        
        // 1. Add salary at beginning of month (between 1st and 6th)
        val salaryDay = Random.nextInt(1, 7) // 1-6
        val salaryDate = nextMonthStart + (salaryDay - 1) * 24L * 60 * 60 * 1000
        transactions.add(
            Transaction(
                id = "sim_salary_${userId}_${targetYear}_${targetMonthDisplay}_${currentTimeMillis()}",
                userId = userId,
                type = TransactionType.INCOME,
                amount = user.netIncome,
                category = TransactionCategory.SALARY,
                description = "Monthly Salary",
                date = salaryDate,
                isRecurring = false
            )
        )
        
        // 2. Add recurring expenses (rent/mortgage, utilities, etc.)
        val recurringExpenses = listOf(
            TransactionCategory.HOUSING to 0.35, // 35% of expenses
            TransactionCategory.UTILITIES to 0.10, // 10% of expenses
        )
        
        var expenseDay = 3
        recurringExpenses.forEach { (category, percentage) ->
            val baseAmount = user.expenses * percentage
            // Add small random variation (±5%)
            val variation = Random.nextDouble(-0.05, 0.05)
            val amount = baseAmount * (1 + variation)
            
            val expenseDate = nextMonthStart + (expenseDay - 1) * 24L * 60 * 60 * 1000L
            transactions.add(
                Transaction(
                    id = "sim_${category.name.lowercase()}_${userId}_${targetYear}_${targetMonthDisplay}_${expenseDay}_${currentTimeMillis()}",
                    userId = userId,
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    category = category,
                    description = when (category) {
                        TransactionCategory.HOUSING -> "Rent/Mortgage"
                        TransactionCategory.UTILITIES -> "Utilities"
                        else -> "Recurring Expense"
                    },
                    date = expenseDate,
                    isRecurring = false
                )
            )
            expenseDay += 2
        }
        
        // 3. Add variable expenses throughout the month
        // Distribute remaining expenses across various categories
        val remainingExpenseBudget = targetExpenses - transactions
            .filter { it.isExpense() }
            .sumOf { it.amount }
        
        val variableExpenseCategories = listOf(
            TransactionCategory.FOOD to 0.25, // 25% of remaining
            TransactionCategory.TRANSPORTATION to 0.20, // 20% of remaining
            TransactionCategory.ENTERTAINMENT to 0.15, // 15% of remaining
            TransactionCategory.SHOPPING to 0.15, // 15% of remaining
            TransactionCategory.HEALTHCARE to 0.10, // 10% of remaining
            TransactionCategory.OTHER_EXPENSE to 0.15 // 15% of remaining
        )
        
        variableExpenseCategories.forEach { (category, percentage) ->
            val baseAmount = remainingExpenseBudget * percentage
            // Add random variation (±15% for variability)
            val variation = Random.nextDouble(-0.15, 0.15)
            val amount = baseAmount * (1 + variation)
            
            // Distribute throughout the month (between day 7 and 28)
            val day = Random.nextInt(7, 29)
            val expenseDate = nextMonthStart + (day - 1) * 24L * 60 * 60 * 1000L
            
            transactions.add(
                Transaction(
                    id = "sim_${category.name.lowercase()}_${userId}_${targetYear}_${targetMonthDisplay}_${day}_${currentTimeMillis()}",
                    userId = userId,
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    category = category,
                    description = when (category) {
                        TransactionCategory.FOOD -> "Groceries & Dining"
                        TransactionCategory.TRANSPORTATION -> "Transportation"
                        TransactionCategory.ENTERTAINMENT -> "Entertainment"
                        TransactionCategory.SHOPPING -> "Shopping"
                        TransactionCategory.HEALTHCARE -> "Healthcare"
                        else -> "Other Expenses"
                    },
                    date = expenseDate,
                    isRecurring = false
                )
            )
        }
        
        // 4. Adjust expenses to match target savings exactly
        val currentTotalExpenses = transactions.filter { it.isExpense() }.sumOf { it.amount }
        val currentTotalIncome = transactions.filter { it.isIncome() }.sumOf { it.amount }
        val currentSavings = currentTotalIncome - currentTotalExpenses
        val adjustmentNeeded = targetSavings - currentSavings
        
        if (kotlin.math.abs(adjustmentNeeded) > 0.01) {
            // Adjust the last expense transaction to match target
            val lastExpenseIndex = transactions.indexOfLast { it.isExpense() }
            if (lastExpenseIndex >= 0) {
                val lastExpense = transactions[lastExpenseIndex]
                val adjustedAmount = (lastExpense.amount - adjustmentNeeded).coerceAtLeast(0.01)
                transactions[lastExpenseIndex] = lastExpense.copy(amount = adjustedAmount)
            }
        }
        
        // 5. Sort transactions by date
        val sortedTransactions = transactions.sortedBy { it.date }
        
        // 6. Get current bank account balance
        val currentAccount = budgetTrackingRepository.getBankAccount(userId).first()
        val currentBalance = currentAccount?.balance ?: user.wealth
        
        // 7. Add all transactions
        sortedTransactions.forEach { transaction ->
            budgetTrackingRepository.addTransaction(userId, transaction)
        }
        
        // 8. Update user wealth based on new balance
        val finalAccount = budgetTrackingRepository.getBankAccount(userId).first()
        if (finalAccount != null) {
            userRepository.updateWealth(userId, finalAccount.balance)
        }
        
        println("Simulated ${sortedTransactions.size} transactions for ${targetMonthDisplay}/${targetYear}")
        println("Expected savings: $expectedSavings, Actual savings: $targetSavings")
    }
}

