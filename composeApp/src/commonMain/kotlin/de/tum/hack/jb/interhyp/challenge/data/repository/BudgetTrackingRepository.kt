package de.tum.hack.jb.interhyp.challenge.data.repository

import de.tum.hack.jb.interhyp.challenge.domain.model.BankAccount
import de.tum.hack.jb.interhyp.challenge.domain.model.Transaction
import de.tum.hack.jb.interhyp.challenge.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Repository interface for budget tracking operations.
 * Manages bank account and transactions.
 */
interface BudgetTrackingRepository {
    /**
     * Get bank account for a user
     */
    fun getBankAccount(userId: String): Flow<BankAccount?>
    
    /**
     * Add a new transaction
     */
    suspend fun addTransaction(userId: String, transaction: Transaction)
    
    /**
     * Remove a transaction
     */
    suspend fun removeTransaction(userId: String, transactionId: String)
    
    /**
     * Get all transactions for a user
     */
    fun getTransactions(userId: String): Flow<List<Transaction>>
    
    /**
     * Get transactions for a specific month
     */
    fun getTransactionsForMonth(userId: String, year: Int, month: Int): Flow<List<Transaction>>
    
    /**
     * Update bank account balance (sync with user wealth)
     */
    suspend fun updateBalance(userId: String, newBalance: Double)
    
    /**
     * Process monthly recurring transactions
     */
    suspend fun processMonthlyRecurringTransactions(userId: String)
    
    /**
     * Initialize bank account for a user
     */
    suspend fun initializeBankAccount(userId: String, initialBalance: Double)
}

/**
 * Implementation of BudgetTrackingRepository with in-memory storage.
 * TODO: Replace with DataStore or SQLDelight for persistence
 */
class BudgetTrackingRepositoryImpl : BudgetTrackingRepository {
    
    // In-memory storage: userId -> BankAccount
    private val bankAccounts = mutableMapOf<String, MutableStateFlow<BankAccount?>>()
    
    override fun getBankAccount(userId: String): Flow<BankAccount?> {
        val accountFlow = bankAccounts.getOrPut(userId) {
            MutableStateFlow(null)
        }
        return accountFlow.asStateFlow()
    }
    
    override suspend fun addTransaction(userId: String, transaction: Transaction) {
        val accountFlow = bankAccounts.getOrPut(userId) {
            MutableStateFlow(null)
        }
        
        accountFlow.update { currentAccount ->
            if (currentAccount == null) {
                // Create new account if it doesn't exist
                BankAccount(
                    id = "account_$userId",
                    userId = userId,
                    balance = transaction.getSignedAmount(),
                    transactions = listOf(transaction),
                    lastUpdated = currentTimeMillis()
                )
            } else {
                // Add transaction and update balance
                val newTransactions = currentAccount.transactions + transaction
                val balanceChange = transaction.getSignedAmount()
                val newBalance = currentAccount.balance + balanceChange
                
                currentAccount.copy(
                    balance = newBalance,
                    transactions = newTransactions,
                    lastUpdated = currentTimeMillis()
                )
            }
        }
    }
    
    override suspend fun removeTransaction(userId: String, transactionId: String) {
        val accountFlow = bankAccounts[userId] ?: return
        
        accountFlow.update { currentAccount ->
            currentAccount?.let { account ->
                val transaction = account.transactions.find { it.id == transactionId }
                if (transaction != null) {
                    val newTransactions = account.transactions.filter { it.id != transactionId }
                    val balanceChange = -transaction.getSignedAmount() // Reverse the transaction
                    val newBalance = account.balance + balanceChange
                    
                    account.copy(
                        balance = newBalance,
                        transactions = newTransactions,
                        lastUpdated = currentTimeMillis()
                    )
                } else {
                    account
                }
            }
        }
    }
    
    override fun getTransactions(userId: String): Flow<List<Transaction>> {
        return getBankAccount(userId).map { account ->
            account?.transactions ?: emptyList()
        }
    }
    
    override fun getTransactionsForMonth(userId: String, year: Int, month: Int): Flow<List<Transaction>> {
        return getBankAccount(userId).map { account ->
            account?.transactions?.filter { transaction ->
                getYear(transaction.date) == year &&
                getMonth(transaction.date) == month - 1 // Months are 0-based
            } ?: emptyList()
        }
    }
    
    override suspend fun updateBalance(userId: String, newBalance: Double) {
        val accountFlow = bankAccounts.getOrPut(userId) {
            MutableStateFlow(null)
        }
        
        accountFlow.update { currentAccount ->
            currentAccount?.copy(
                balance = newBalance,
                lastUpdated = currentTimeMillis()
            ) ?: BankAccount(
                id = "account_$userId",
                userId = userId,
                balance = newBalance,
                transactions = emptyList(),
                lastUpdated = currentTimeMillis()
            )
        }
    }
    
    override suspend fun processMonthlyRecurringTransactions(userId: String) {
        val accountFlow = bankAccounts[userId] ?: return
        val currentAccount = accountFlow.value ?: return
        
        val now = currentTimeMillis()
        val currentDay = getDayOfMonth(now)
        val currentMonth = getMonth(now)
        val currentYear = getYear(now)
        
        // Find recurring transactions that should be processed this month
        val recurringTransactions = currentAccount.transactions
            .filter { it.isRecurring && it.recurringDay != null }
            .filter { it.recurringDay == currentDay }
        
        // Check if we already processed this month's recurring transactions
        val lastProcessedMonth = currentAccount.transactions
            .filter { it.isRecurring }
            .maxOfOrNull { transaction ->
                getMonth(transaction.date)
            }
        
        // Only process if we haven't processed this month yet
        if (lastProcessedMonth != currentMonth) {
            recurringTransactions.forEach { originalTransaction ->
                val newTransaction = originalTransaction.copy(
                    id = "${originalTransaction.id}_${currentYear}_${currentMonth + 1}",
                    date = now
                )
                addTransaction(userId, newTransaction)
            }
        }
    }
    
    override suspend fun initializeBankAccount(userId: String, initialBalance: Double) {
        val accountFlow = bankAccounts.getOrPut(userId) {
            MutableStateFlow(null)
        }
        
        if (accountFlow.value == null) {
            accountFlow.value = BankAccount(
                id = "account_$userId",
                userId = userId,
                balance = initialBalance,
                transactions = emptyList(),
                lastUpdated = currentTimeMillis()
            )
        }
    }
}

