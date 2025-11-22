package de.tum.hack.jb.interhyp.challenge.data.service

import de.tum.hack.jb.interhyp.challenge.data.repository.BudgetTrackingRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import de.tum.hack.jb.interhyp.challenge.domain.model.User
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
        // Initialize bank account with user's current wealth
        budgetTrackingRepository.initializeBankAccount(user.id, user.wealth)
        
        // Optionally create initial transactions based on user profile
        // This could be done through MonthlyReminderService
    }
}

