package de.tum.hack.jb.interhyp.challenge.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import de.tum.hack.jb.interhyp.challenge.data.repository.BudgetTrackingRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.PropertyRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import de.tum.hack.jb.interhyp.challenge.data.service.BudgetCalculationService
import de.tum.hack.jb.interhyp.challenge.data.service.BudgetSyncService
import de.tum.hack.jb.interhyp.challenge.data.service.MonthlyReminderService
import de.tum.hack.jb.interhyp.challenge.data.service.MonthSimulationService
import de.tum.hack.jb.interhyp.challenge.domain.model.BankAccount
import de.tum.hack.jb.interhyp.challenge.domain.model.SavingsPlanInput
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
    val errorMessage: String? = null,
    val requiredDownPayment: Double? = null,
    val projectedSavingsData: List<ProjectedSavingsPoint> = emptyList(),
    val monthsToGoal: Int? = null,
    val goalPropertyPrice: Double? = null
)

/**
 * ViewModel for Insights screen
 * Manages budget tracking data and statistics
 */
class InsightsViewModel(
    private val budgetTrackingRepository: BudgetTrackingRepository,
    private val userRepository: UserRepository,
    private val monthlyReminderService: MonthlyReminderService,
    private val budgetSyncService: BudgetSyncService,
    private val budgetCalculationService: BudgetCalculationService,
    private val propertyRepository: PropertyRepository,
    private val monthSimulationService: MonthSimulationService
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
                
                // Ensure bank account is initialized (will create mock data if needed)
                val existingAccount = budgetTrackingRepository.getBankAccount(user.id).first()
                if (existingAccount == null) {
                    budgetSyncService.initializeBankAccountFromUser(user)
                }
                
                // Load bank account data and combine with transactions
                budgetTrackingRepository.getBankAccount(user.id)
                    .combine(budgetTrackingRepository.getTransactions(user.id)) { account, transactions ->
                        account to transactions
                    }
                    .collectLatest { (account, transactions) ->
                        val bankAccount = account
                        
                        // Calculate monthly statistics from the most recent month with transactions
                        // Find the latest transaction date to determine which month to show
                        val latestTransactionDate = transactions.maxOfOrNull { it.date }
                        
                        val (lastMonthStart, lastMonthEnd) = if (latestTransactionDate != null) {
                            // Use the month of the latest transaction
                            val latestMonthStart = getStartOfMonth(latestTransactionDate)
                            // Calculate end of that month (start of next month - 1)
                            val nextMonthStart = getStartOfMonth(latestMonthStart + 32L * 24 * 60 * 60 * 1000)
                            val latestMonthEnd = nextMonthStart - 1
                            latestMonthStart to latestMonthEnd
                        } else {
                            // No transactions yet, use last month from current time
                            val now = currentTimeMillis()
                            val currentMonthStart = getStartOfMonth(now)
                            val lastMonthStart = getStartOfMonth(now - 32L * 24 * 60 * 60 * 1000)
                            val lastMonthEnd = currentMonthStart - 1
                            lastMonthStart to lastMonthEnd
                        }
                        
                        val monthlyIncome = bankAccount?.getTotalIncome(lastMonthStart, lastMonthEnd) ?: 0.0
                        val monthlyExpenses = bankAccount?.getTotalExpenses(lastMonthStart, lastMonthEnd) ?: 0.0
                        val monthlySavings = monthlyIncome - monthlyExpenses
                        
                        // Get expected values from user profile (for projections and goal calculations)
                        // Use expected values instead of actual recent month to avoid
                        // projecting negative savings or incorrect goal dates after bad months
                        val expectedMonthlyIncome = user.netIncome
                        val expectedMonthlyExpenses = user.expenses
                        val expectedMonthlySavings = user.getMonthlySavings()
                        
                        // Get recent transactions (last 20, sorted by date descending)
                        val recentTransactions = transactions
                            .sortedByDescending { it.date }
                            .take(20)
                        
                        // Calculate goal timeline data with historical months
                        // Determine the "current" month based on latest transaction or historical balance
                        val latestHistoricalMonth = bankAccount?.historicalBalances?.keys?.maxOrNull()
                        val referenceMonthStart = when {
                            latestHistoricalMonth != null && latestTransactionDate != null -> {
                                val latestTransactionMonth = getStartOfMonth(latestTransactionDate)
                                maxOf(latestHistoricalMonth, latestTransactionMonth)
                            }
                            latestHistoricalMonth != null -> latestHistoricalMonth
                            latestTransactionDate != null -> getStartOfMonth(latestTransactionDate)
                            else -> getStartOfMonth(currentTimeMillis())
                        }
                        
                        val goalData = calculateGoalTimeline(
                            user = user,
                            bankAccount = bankAccount,
                            currentBalance = bankAccount?.balance ?: 0.0,
                            monthlyIncome = monthlyIncome,
                            monthlyExpenses = monthlyExpenses,
                            monthlySavings = monthlySavings, // Actual savings for display
                            expectedMonthlyIncome = expectedMonthlyIncome, // Expected income for goal calculation
                            expectedMonthlyExpenses = expectedMonthlyExpenses, // Expected expenses for goal calculation
                            expectedMonthlySavings = expectedMonthlySavings, // Expected savings for projections
                            transactions = transactions,
                            referenceMonthStart = referenceMonthStart
                        )
                        
                        _uiState.update {
                            it.copy(
                                bankAccount = bankAccount,
                                currentBalance = bankAccount?.balance ?: 0.0,
                                monthlyIncome = monthlyIncome,
                                monthlyExpenses = monthlyExpenses,
                                monthlySavings = monthlySavings,
                                recentTransactions = recentTransactions,
                                isLoading = false,
                                errorMessage = null,
                                requiredDownPayment = goalData.requiredDownPayment,
                                projectedSavingsData = goalData.projectedSavingsData,
                                monthsToGoal = goalData.monthsToGoal,
                                goalPropertyPrice = goalData.goalPropertyPrice
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
    
    /**
     * Simulate a bad month where expenses exceed income
     */
    fun simulateBadMonth() {
        viewModelScope.launch {
            try {
                val user = userRepository.getUser().first()
                if (user != null) {
                    monthSimulationService.simulateBadMonth(user.id)
                    // Reload data after simulation
                    loadInsightsData()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to simulate bad month: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Calculate goal timeline data including required down payment and projected savings
     */
    private data class GoalTimelineData(
        val requiredDownPayment: Double?,
        val projectedSavingsData: List<ProjectedSavingsPoint>,
        val monthsToGoal: Int?,
        val goalPropertyPrice: Double?
    )
    
    private suspend fun calculateGoalTimeline(
        user: de.tum.hack.jb.interhyp.challenge.domain.model.User,
        bankAccount: de.tum.hack.jb.interhyp.challenge.domain.model.BankAccount?,
        currentBalance: Double,
        monthlyIncome: Double,
        monthlyExpenses: Double,
        monthlySavings: Double, // Actual savings from most recent month (for display)
        expectedMonthlyIncome: Double, // Expected income from user profile (for goal calculation)
        expectedMonthlyExpenses: Double, // Expected expenses from user profile (for goal calculation)
        expectedMonthlySavings: Double, // Expected savings from user profile (for projections)
        transactions: List<Transaction>,
        referenceMonthStart: Long
    ): GoalTimelineData {
        // Use saved goal price if available, otherwise calculate average (same logic as DashboardViewModel)
        val goalPropertyPrice = if (user.goalPropertyPrice != null && user.goalPropertyPrice!! > 0) {
            user.goalPropertyPrice!!
        } else {
            // Fall back to calculating average price
            val userProfile = de.tum.hack.jb.interhyp.challenge.domain.model.UserProfile(
                userId = user.id,
                name = user.name,
                age = user.age,
                monthlyIncome = user.netIncome,
                monthlyExpenses = user.expenses,
                currentEquity = user.wealth,
                desiredLocation = "Munich", // Default, should be stored
                desiredPropertySize = user.goalPropertySize ?: 80.0
            )
            
            // Get average property price
            val result = propertyRepository.getAveragePrice(
                userProfile.desiredLocation,
                userProfile.desiredPropertySize
            ).first()
            
            when (result) {
                is de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult.Success -> result.data
                else -> {
                    // Use default fallback
                    5000.0 * userProfile.desiredPropertySize
                }
            }
        }
        
        // If calculated price is still invalid, return empty data
        if (goalPropertyPrice <= 0) {
            return GoalTimelineData(
                requiredDownPayment = null,
                projectedSavingsData = emptyList(),
                monthsToGoal = null,
                goalPropertyPrice = null
            )
        }
        
        // Calculate required down payment using BudgetCalculationService
        // Use expected income/expenses from user profile, not actual recent month
        // This ensures consistent goal calculation regardless of bad months
        val savingsPlanInput = SavingsPlanInput(
            targetPropertyPrice = goalPropertyPrice,
            currentAge = user.age,
            currentSavings = currentBalance,
            monthlyIncome = expectedMonthlyIncome,
            monthlyExpenses = expectedMonthlyExpenses
        )
        
        val savingsPlanResult = budgetCalculationService.calculateSavingsPlan(savingsPlanInput)
        val requiredDownPayment = savingsPlanResult.requiredSavings
        
        // If goal is already reached or unachievable
        if (requiredDownPayment <= 0 || !savingsPlanResult.isAchievable) {
            val projectedData = if (requiredDownPayment <= 0) {
                // Goal already reached - show current balance
                listOf(ProjectedSavingsPoint(0, currentBalance))
            } else {
                // Unachievable - still show projection for visualization
                // Use expected savings instead of actual (which could be negative after bad month)
                generateProjectedSavingsData(
                    currentBalance = currentBalance,
                    monthlySavings = expectedMonthlySavings,
                    maxMonths = 120
                )
            }
            
            // Generate historical data points from stored historical balances
            val historicalData = generateHistoricalSavingsData(
                bankAccount = bankAccount,
                currentBalance = currentBalance,
                referenceMonthStart = referenceMonthStart
            )
            
            // Combine historical and projected data
            val allDataPoints = (historicalData + projectedData).sortedBy { it.monthIndex }
            
            return GoalTimelineData(
                requiredDownPayment = if (requiredDownPayment <= 0) 0.0 else requiredDownPayment,
                projectedSavingsData = allDataPoints,
                monthsToGoal = if (requiredDownPayment <= 0) 0 else null,
                goalPropertyPrice = goalPropertyPrice
            )
        }
        
        // Generate historical data points from stored historical balances
        val historicalData = generateHistoricalSavingsData(
            bankAccount = bankAccount,
            currentBalance = currentBalance,
            referenceMonthStart = referenceMonthStart
        )
        
        // Calculate projected savings data
        // Generate points until we reach the goal or hit 10 years (120 months)
        // Use expected savings instead of actual (which could be negative after bad month)
        val maxMonths = 120
        val projectedData = generateProjectedSavingsData(
            currentBalance = currentBalance,
            monthlySavings = expectedMonthlySavings,
            maxMonths = maxMonths,
            targetAmount = requiredDownPayment
        )
        
        // Combine historical and projected data
        val allDataPoints = (historicalData + projectedData).sortedBy { it.monthIndex }
        
        // Find when goal is reached (only in projected data, not historical)
        val monthsToGoal = projectedData.firstOrNull { it.cumulativeSavings >= requiredDownPayment }?.monthIndex
        
        return GoalTimelineData(
            requiredDownPayment = requiredDownPayment,
            projectedSavingsData = allDataPoints,
            monthsToGoal = monthsToGoal,
            goalPropertyPrice = goalPropertyPrice
        )
    }
    
    /**
     * Generate historical savings data points from stored historical balances
     * Returns data points with negative month indices (e.g., -3 = 3 months ago)
     */
    private fun generateHistoricalSavingsData(
        bankAccount: de.tum.hack.jb.interhyp.challenge.domain.model.BankAccount?,
        currentBalance: Double,
        referenceMonthStart: Long
    ): List<ProjectedSavingsPoint> {
        if (bankAccount == null || bankAccount.historicalBalances.isEmpty()) {
            return emptyList()
        }
        
        // Convert stored historical balances to ProjectedSavingsPoint format
        // Use referenceMonthStart (latest simulated month) as the "current" month for calculation
        val historicalPoints = bankAccount.historicalBalances.mapNotNull { (monthStart, balance) ->
            // Only include months before the reference month (historical months)
            if (monthStart >= referenceMonthStart) {
                return@mapNotNull null
            }
            
            // Calculate months ago relative to reference month
            val monthsAgo = calculateMonthsBetween(monthStart, referenceMonthStart)
            
            // Only include valid past months (monthsAgo > 0)
            if (monthsAgo <= 0) {
                return@mapNotNull null
            }
            
            ProjectedSavingsPoint(-monthsAgo, balance)
        }
        
        // Sort by month index (oldest first, which means most negative first)
        return historicalPoints.sortedBy { it.monthIndex }
    }
    
    /**
     * Calculate number of months between two timestamps
     */
    private fun calculateMonthsBetween(startTimestamp: Long, endTimestamp: Long): Int {
        val startYear = getYear(startTimestamp)
        val startMonth = getMonth(startTimestamp)
        val endYear = getYear(endTimestamp)
        val endMonth = getMonth(endTimestamp)
        
        return (endYear - startYear) * 12 + (endMonth - startMonth)
    }
    
    /**
     * Generate projected savings data points
     */
    private fun generateProjectedSavingsData(
        currentBalance: Double,
        monthlySavings: Double,
        maxMonths: Int,
        targetAmount: Double? = null
    ): List<ProjectedSavingsPoint> {
        val dataPoints = mutableListOf<ProjectedSavingsPoint>()
        
        // Always include current month (month 0)
        dataPoints.add(ProjectedSavingsPoint(0, currentBalance))
        
        // If monthly savings is negative or zero, just return current point
        if (monthlySavings <= 0) {
            return dataPoints
        }
        
        // Generate points for each month
        for (month in 1..maxMonths) {
            val cumulativeSavings = currentBalance + (monthlySavings * month)
            dataPoints.add(ProjectedSavingsPoint(month, cumulativeSavings))
            
            // Stop early if we've reached the target
            if (targetAmount != null && cumulativeSavings >= targetAmount) {
                break
            }
        }
        
        return dataPoints
    }
}

