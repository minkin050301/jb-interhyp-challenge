package de.tum.hack.jb.interhyp.challenge.ui.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.hack.jb.interhyp.challenge.domain.model.Transaction
import de.tum.hack.jb.interhyp.challenge.domain.model.TransactionCategory
import de.tum.hack.jb.interhyp.challenge.presentation.insights.InsightsViewModel
import de.tum.hack.jb.interhyp.challenge.ui.components.AppScaffold
import de.tum.hack.jb.interhyp.challenge.ui.components.Insights
import de.tum.hack.jb.interhyp.challenge.ui.components.NavItem
import de.tum.hack.jb.interhyp.challenge.ui.components.Settings
import de.tum.hack.jb.interhyp.challenge.util.formatCurrency
import de.tum.hack.jb.interhyp.challenge.util.formatDate
import jb_interhyp_challenge.composeapp.generated.resources.*
import jb_interhyp_challenge.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    onNavigate: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    AppScaffold(
        navItemsLeft = listOf(NavItem(id = "insights", label = stringResource(Res.string.insights_title), icon = Insights)),
        navItemsRight = listOf(NavItem(id = "settings", label = stringResource(Res.string.settings_title), icon = Settings)),
        selectedItemId = "insights",
        onItemSelected = { id ->
            onNavigate(id)
        },
        onHomeClick = { onNavigate("home") }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.error),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage ?: stringResource(Res.string.unknown_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.refresh() }) {
                    Text(stringResource(Res.string.retry))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = stringResource(Res.string.insights_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Balance Card
                item {
                    BalanceCard(balance = uiState.currentBalance)
                }
                
                // Monthly Summary Card
                item {
                    MonthlySummaryCard(
                        income = uiState.monthlyIncome,
                        expenses = uiState.monthlyExpenses,
                        savings = uiState.monthlySavings
                    )
                }
                
                // Balance Timeline Chart
                item {
                    BalanceTimelineChart(
                        projectedSavingsData = uiState.projectedSavingsData,
                        requiredDownPayment = uiState.requiredDownPayment,
                        monthsToGoal = uiState.monthsToGoal
                    )
                }
                
                // Recent Transactions Card
                item {
                    Text(
                        text = stringResource(Res.string.recent_transactions),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (uiState.recentTransactions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = stringResource(Res.string.no_transactions_yet),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(uiState.recentTransactions) { transaction ->
                        TransactionItem(transaction = transaction)
                    }
                }
                
                // Action buttons
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.refresh() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.refresh))
                        }
                        Button(
                            onClick = { viewModel.simulateBadMonth() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text("Simulate Bad Month")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.current_balance),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCurrency(balance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    income: Double,
    expenses: Double,
    savings: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.last_month_summary),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            SummaryRow(
                label = stringResource(Res.string.income),
                amount = income,
                isPositive = true
            )
            
            SummaryRow(
                label = stringResource(Res.string.expenses),
                amount = expenses,
                isPositive = false
            )
            
            HorizontalDivider()
            
            SummaryRow(
                label = stringResource(Res.string.net_savings),
                amount = savings,
                isPositive = savings >= 0,
                isBold = true
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    amount: Double,
    isPositive: Boolean,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = formatCurrency(amount),
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isPositive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(getCategoryStringResource(transaction.category)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate(transaction.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = if (transaction.isIncome()) {
                    "+${formatCurrency(transaction.amount)}"
                } else {
                    "-${formatCurrency(transaction.amount)}"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isIncome()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

/**
 * Maps TransactionCategory to its corresponding string resource
 */
private fun getCategoryStringResource(category: TransactionCategory): StringResource {
    return when (category) {
        TransactionCategory.SALARY -> Res.string.category_salary
        TransactionCategory.BONUS -> Res.string.category_bonus
        TransactionCategory.INVESTMENT -> Res.string.category_investment
        TransactionCategory.RENTAL_INCOME -> Res.string.category_rental_income
        TransactionCategory.OTHER_INCOME -> Res.string.category_other_income
        TransactionCategory.HOUSING -> Res.string.category_housing
        TransactionCategory.FOOD -> Res.string.category_food
        TransactionCategory.TRANSPORTATION -> Res.string.category_transportation
        TransactionCategory.UTILITIES -> Res.string.category_utilities
        TransactionCategory.ENTERTAINMENT -> Res.string.category_entertainment
        TransactionCategory.HEALTHCARE -> Res.string.category_healthcare
        TransactionCategory.EDUCATION -> Res.string.category_education
        TransactionCategory.SHOPPING -> Res.string.category_shopping
        TransactionCategory.OTHER_EXPENSE -> Res.string.category_other_expense
    }
}


