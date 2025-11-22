package de.tum.hack.jb.interhyp.challenge.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.hack.jb.interhyp.challenge.data.repository.BudgetTrackingRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import de.tum.hack.jb.interhyp.challenge.data.service.MonthlyReminderService
import de.tum.hack.jb.interhyp.challenge.domain.model.BankAccount
import de.tum.hack.jb.interhyp.challenge.domain.model.Transaction
import de.tum.hack.jb.interhyp.challenge.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State for Insights screen
 */
data class InsightsUiState(
    val bankAccount: BankAccount? = null,
    val currentBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val monthlySavings: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for Insights screen
 * Manages budget tracking data and statistics
 */
class InsightsViewModel(
    private val budgetTrackingRepository: BudgetTrackingRepository,
    private val userRepository: UserRepository,
    private val monthlyReminderService: MonthlyReminderService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()
    
    init {
        loadInsightsData()
    }
    
    /**
     * Load insights data from repositories
     */
    private fun loadInsightsData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // Get current user
                val user = userRepository.getUser().first()
                if (user == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "No user profile found"
                        )
                    }
                    return@launch
                }
                
                // Load bank account data and combine with transactions
                budgetTrackingRepository.getBankAccount(user.id)
                    .combine(budgetTrackingRepository.getTransactions(user.id)) { account, transactions ->
                        account to transactions
                    }
                    .collectLatest { (account, transactions) ->
                        val bankAccount = account
                        
                        // Calculate monthly statistics from current month transactions
                        val now = currentTimeMillis()
                        val monthStart = getStartOfMonth(now)
                        
                        val monthlyIncome = bankAccount?.getTotalIncome(monthStart, now) ?: 0.0
                        val monthlyExpenses = bankAccount?.getTotalExpenses(monthStart, now) ?: 0.0
                        val monthlySavings = monthlyIncome - monthlyExpenses
                        
                        // Get recent transactions (last 20, sorted by date descending)
                        val recentTransactions = transactions
                            .sortedByDescending { it.date }
                            .take(20)
                        
                        _uiState.update {
                            it.copy(
                                bankAccount = bankAccount,
                                currentBalance = bankAccount?.balance ?: 0.0,
                                monthlyIncome = monthlyIncome,
                                monthlyExpenses = monthlyExpenses,
                                monthlySavings = monthlySavings,
                                recentTransactions = recentTransactions,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load insights: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Refresh insights data
     */
    fun refresh() {
        viewModelScope.launch {
            // Check and process monthly updates
            val user = userRepository.getUser().first()
            user?.let {
                monthlyReminderService.checkAndProcessMonthlyUpdate(it.id)
            }
            
            // Reload data
            loadInsightsData()
        }
    }
}

