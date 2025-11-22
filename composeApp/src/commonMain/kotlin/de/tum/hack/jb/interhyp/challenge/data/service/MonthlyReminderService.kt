package de.tum.hack.jb.interhyp.challenge.data.service

import de.tum.hack.jb.interhyp.challenge.data.repository.BudgetTrackingRepository
import de.tum.hack.jb.interhyp.challenge.domain.model.Transaction
import de.tum.hack.jb.interhyp.challenge.domain.model.TransactionCategory
import de.tum.hack.jb.interhyp.challenge.domain.model.TransactionType
import de.tum.hack.jb.interhyp.challenge.util.*

/**
 * Service interface for handling monthly reminders and updates
 */
interface MonthlyReminderService {
    /**
     * Check if monthly update is due and process it
     */
    suspend fun checkAndProcessMonthlyUpdate(userId: String)
    
    /**
     * Create a monthly reminder for a user
     */
    suspend fun createMonthlyReminder(userId: String, reminderDay: Int)
    
    /**
     * Process monthly recurring transactions for a user
     */
    suspend fun processMonthlyTransactions(userId: String)
    
    /**
     * Get next reminder date for a user
     */
    suspend fun getNextReminderDate(userId: String): Long?
    
    /**
     * Check if it's time for monthly update
     */
    suspend fun isMonthlyUpdateDue(userId: String): Boolean
}

/**
 * Data class representing a monthly reminder
 */
data class MonthlyReminder(
    val userId: String,
    val reminderDay: Int, // Day of month (1-31)
    val lastProcessed: Long? = null, // Last time reminder was processed
    val isActive: Boolean = true
)

/**
 * Implementation of MonthlyReminderService
 */
class MonthlyReminderServiceImpl(
    private val budgetTrackingRepository: BudgetTrackingRepository
) : MonthlyReminderService {
    
    // In-memory storage for reminders: userId -> MonthlyReminder
    private val reminders = mutableMapOf<String, MonthlyReminder>()
    
    override suspend fun checkAndProcessMonthlyUpdate(userId: String) {
        if (isMonthlyUpdateDue(userId)) {
            processMonthlyTransactions(userId)
            updateLastProcessed(userId)
        }
    }
    
    override suspend fun createMonthlyReminder(userId: String, reminderDay: Int) {
        reminders[userId] = MonthlyReminder(
            userId = userId,
            reminderDay = reminderDay.coerceIn(1, 31),
            isActive = true
        )
    }
    
    override suspend fun processMonthlyTransactions(userId: String) {
        // Process recurring transactions
        budgetTrackingRepository.processMonthlyRecurringTransactions(userId)
        
        // Optionally create default monthly transactions based on user profile
        // This could be salary, rent, etc.
    }
    
    override suspend fun getNextReminderDate(userId: String): Long? {
        val reminder = reminders[userId] ?: return null
        if (!reminder.isActive) return null
        
        val now = currentTimeMillis()
        
        // Get start of current month
        val monthStart = getStartOfMonth(now)
        
        // Calculate reminder date this month (simplified - just use month start + reminder day)
        // For a more accurate implementation, we'd need to add days to month start
        // This is a simplified version that works for basic use cases
        val reminderThisMonth = monthStart + (reminder.reminderDay - 1) * 24 * 60 * 60 * 1000L
        
        // If reminder day has passed this month, move to next month
        return if (reminderThisMonth < now) {
            // Next month - simplified calculation
            val nextMonthStart = getStartOfMonth(now + 32L * 24 * 60 * 60 * 1000) // Add ~32 days to get next month
            nextMonthStart + (reminder.reminderDay - 1) * 24 * 60 * 60 * 1000L
        } else {
            reminderThisMonth
        }
    }
    
    override suspend fun isMonthlyUpdateDue(userId: String): Boolean {
        val reminder = reminders[userId] ?: return false
        if (!reminder.isActive) return false
        
        val now = currentTimeMillis()
        val currentDay = getDayOfMonth(now)
        val currentMonth = getMonth(now)
        val currentYear = getYear(now)
        
        // Check if reminder day matches today
        if (currentDay != reminder.reminderDay) {
            return false
        }
        
        // Check if we already processed this month
        val lastProcessed = reminder.lastProcessed
        if (lastProcessed != null) {
            val lastProcessedMonth = getMonth(lastProcessed)
            val lastProcessedYear = getYear(lastProcessed)
            
            // Already processed this month
            if (lastProcessedMonth == currentMonth && lastProcessedYear == currentYear) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Update last processed timestamp for a user
     */
    private fun updateLastProcessed(userId: String) {
        val reminder = reminders[userId] ?: return
        reminders[userId] = reminder.copy(lastProcessed = currentTimeMillis())
    }
    
    /**
     * Create a default monthly income transaction (e.g., salary)
     */
    suspend fun createDefaultMonthlyIncome(
        userId: String,
        amount: Double,
        description: String = "Monthly Salary",
        dayOfMonth: Int = 1
    ) {
        val transaction = Transaction(
            id = "monthly_income_${userId}_${currentTimeMillis()}",
            userId = userId,
            type = TransactionType.INCOME,
            amount = amount,
            category = TransactionCategory.SALARY,
            description = description,
            date = currentTimeMillis(),
            isRecurring = true,
            recurringDay = dayOfMonth
        )
        
        budgetTrackingRepository.addTransaction(userId, transaction)
    }
    
    /**
     * Create a default monthly expense transaction (e.g., rent)
     */
    suspend fun createDefaultMonthlyExpense(
        userId: String,
        amount: Double,
        description: String = "Monthly Rent",
        category: TransactionCategory = TransactionCategory.HOUSING,
        dayOfMonth: Int = 1
    ) {
        val transaction = Transaction(
            id = "monthly_expense_${userId}_${currentTimeMillis()}",
            userId = userId,
            type = TransactionType.EXPENSE,
            amount = amount,
            category = category,
            description = description,
            date = currentTimeMillis(),
            isRecurring = true,
            recurringDay = dayOfMonth
        )
        
        budgetTrackingRepository.addTransaction(userId, transaction)
    }
}

