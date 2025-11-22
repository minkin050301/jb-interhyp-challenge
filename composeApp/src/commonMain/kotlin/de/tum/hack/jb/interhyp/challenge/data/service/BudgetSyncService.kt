package de.tum.hack.jb.interhyp.challenge.data.service

import de.tum.hack.jb.interhyp.challenge.data.repository.BudgetTrackingRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import de.tum.hack.jb.interhyp.challenge.domain.model.Transaction
import de.tum.hack.jb.interhyp.challenge.domain.model.TransactionCategory
import de.tum.hack.jb.interhyp.challenge.domain.model.TransactionType
import de.tum.hack.jb.interhyp.challenge.domain.model.User
import de.tum.hack.jb.interhyp.challenge.util.*
import kotlinx.coroutines.flow.first

/**
 * Service to synchronize bank account balance with user wealth
 */
interface BudgetSyncService {
    /**
     * Sync bank account balance to user wealth
     */
    suspend fun syncBalanceToWealth(userId: String)
    
    /**
     * Sync user wealth to bank account balance
     */
    suspend fun syncWealthToBalance(userId: String)
    
    /**
     * Initialize bank account from user profile
     */
    suspend fun initializeBankAccountFromUser(user: User)
}

/**
 * Implementation of BudgetSyncService
 */
class BudgetSyncServiceImpl(
    private val userRepository: UserRepository,
    private val budgetTrackingRepository: BudgetTrackingRepository
) : BudgetSyncService {
    
    override suspend fun syncBalanceToWealth(userId: String) {
        val bankAccount = budgetTrackingRepository.getBankAccount(userId).first()
        bankAccount?.let { account ->
            userRepository.updateWealth(userId, account.balance)
        }
    }
    
    override suspend fun syncWealthToBalance(userId: String) {
        val user = userRepository.getUser().first()
        user?.let { userData ->
            budgetTrackingRepository.updateBalance(userId, userData.wealth)
        }
    }
    
    override suspend fun initializeBankAccountFromUser(user: User) {
        // Check if bank account already exists
        val existingAccount = budgetTrackingRepository.getBankAccount(user.id).first()
        if (existingAccount != null) {
            // Account already exists, just sync the balance
            syncWealthToBalance(user.id)
            return
        }
        
        // Create mock transactions for the last month first (to calculate starting balance)
        createMockTransactionsForLastMonth(user)
    }
    
    /**
     * Create mock transactions for the last month based on user's income and expenses
     */
    private suspend fun createMockTransactionsForLastMonth(user: User) {
        val now = currentTimeMillis()
        val currentMonth = getMonth(now)
        val currentYear = getYear(now)
        
        // Calculate start of previous month (subtract ~32 days to get to previous month)
        val prevMonthStart = getStartOfMonth(now - 32L * 24 * 60 * 60 * 1000)
        
        val transactions = mutableListOf<Transaction>()
        
        // Add monthly salary (income) - typically on the 1st of the month
        val salaryDate = prevMonthStart + (1 - 1) * 24 * 60 * 60 * 1000L // 1st of month
        transactions.add(
            Transaction(
                id = "tx_salary_${user.id}_${currentYear}_${currentMonth}",
                userId = user.id,
                type = TransactionType.INCOME,
                amount = user.netIncome,
                category = TransactionCategory.SALARY,
                description = "Monthly Salary",
                date = salaryDate,
                isRecurring = true,
                recurringDay = 1
            )
        )
        
        // Add recurring expenses throughout the month
        val monthlyExpenses = user.expenses
        val expenseCategories = listOf(
            TransactionCategory.HOUSING to 0.35, // 35% of expenses
            TransactionCategory.FOOD to 0.20,   // 20% of expenses
            TransactionCategory.TRANSPORTATION to 0.15, // 15% of expenses
            TransactionCategory.UTILITIES to 0.10, // 10% of expenses
            TransactionCategory.ENTERTAINMENT to 0.10, // 10% of expenses
            TransactionCategory.OTHER_EXPENSE to 0.10 // 10% of expenses
        )
        
        var expenseDay = 3 // Start expenses on 3rd
        expenseCategories.forEach { (category, percentage) ->
            val amount = monthlyExpenses * percentage
            if (amount > 0) {
                val expenseDate = prevMonthStart + (expenseDay - 1) * 24 * 60 * 60 * 1000L
                transactions.add(
                    Transaction(
                        id = "tx_${category.name.lowercase()}_${user.id}_${currentYear}_${currentMonth}_$expenseDay",
                        userId = user.id,
                        type = TransactionType.EXPENSE,
                        amount = amount,
                        category = category,
                        description = when (category) {
                            TransactionCategory.HOUSING -> "Rent/Mortgage"
                            TransactionCategory.FOOD -> "Groceries & Dining"
                            TransactionCategory.TRANSPORTATION -> "Transportation"
                            TransactionCategory.UTILITIES -> "Utilities"
                            TransactionCategory.ENTERTAINMENT -> "Entertainment"
                            else -> "Other Expenses"
                        },
                        date = expenseDate,
                        isRecurring = category == TransactionCategory.HOUSING, // Only housing is recurring
                        recurringDay = if (category == TransactionCategory.HOUSING) expenseDay else null
                    )
                )
                expenseDay += 5 // Space out expenses by 5 days
            }
        }
        
        // Add a few random one-time expenses
        val randomExpenses = listOf(
            TransactionCategory.SHOPPING to 150.0,
            TransactionCategory.HEALTHCARE to 80.0,
            TransactionCategory.ENTERTAINMENT to 120.0
        )
        
        randomExpenses.forEachIndexed { index, (category, amount) ->
            val expenseDate = prevMonthStart + ((10 + index * 7) - 1) * 24 * 60 * 60 * 1000L
            transactions.add(
                Transaction(
                    id = "tx_random_${category.name.lowercase()}_${user.id}_${currentYear}_${currentMonth}_${10 + index * 7}",
                    userId = user.id,
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    category = category,
                    description = when (category) {
                        TransactionCategory.SHOPPING -> "Shopping"
                        TransactionCategory.HEALTHCARE -> "Healthcare"
                        TransactionCategory.ENTERTAINMENT -> "Entertainment"
                        else -> "Miscellaneous"
                    },
                    date = expenseDate,
                    isRecurring = false
                )
            )
        }
        
        // Calculate net change from transactions
        val totalIncome = transactions.filter { it.isIncome() }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.isExpense() }.sumOf { it.amount }
        val netChange = totalIncome - totalExpenses
        
        // Calculate starting balance: current wealth - net change from last month
        // This ensures that after adding all transactions, the balance equals user.wealth
        val startingBalance = user.wealth - netChange
        
        // Initialize account with starting balance
        budgetTrackingRepository.initializeBankAccount(user.id, startingBalance)
        
        // Add all transactions in chronological order
        // The balance will be updated correctly as transactions are added
        transactions.sortedBy { it.date }.forEach { transaction ->
            budgetTrackingRepository.addTransaction(user.id, transaction)
        }
        
        // Verify final balance matches user.wealth (should be correct due to starting balance calculation)
        // If there's a small discrepancy due to rounding, sync it
        val finalAccount = budgetTrackingRepository.getBankAccount(user.id).first()
        if (finalAccount != null && kotlin.math.abs(finalAccount.balance - user.wealth) > 0.01) {
            budgetTrackingRepository.updateBalance(user.id, user.wealth)
        }
    }
}

