package com.bev.ledgr.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bev.ledgr.data.Expense
import com.bev.ledgr.ui.components.MonthSelector
import com.bev.ledgr.ui.components.RemainingBalanceCard
import com.bev.ledgr.ui.components.SummaryCardsRow
import com.bev.ledgr.ui.components.YearPicker
import com.bev.ledgr.ui.theme.primaryColor
import kotlin.math.abs
import kotlinx.datetime.Month
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExpenseListRoute(
    onAddClick: () -> Unit,
    onExpenseClick: (Long) -> Unit,
    onSummaryClick: () -> Unit,
    viewModel: ExpenseListViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ExpenseListScreen(
        state = state,
        onAddClick = onAddClick,
        onExpenseClick = onExpenseClick,
        onSummaryClick = onSummaryClick,
        onYearSelected = viewModel::onYearSelected,
        onMonthSelected = viewModel::onMonthSelected
    )
}

@Composable
fun ExpenseListScreen(
    state: ExpenseListUiState,
    onAddClick: () -> Unit,
    onExpenseClick: (Long) -> Unit,
    onSummaryClick: () -> Unit,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Expenses", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    TextButton(onClick = onSummaryClick) {
                        Text(
                            text = "Summary",
                            color = primaryColor,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                actions = {
                    YearPicker(
                        selectedYear = state.selectedYear,
                        years = state.availableYears,
                        onYearSelected = onYearSelected
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = primaryColor,
                contentColor = Color.White
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MonthSelector(
                selectedMonth = state.selectedMonth,
                availableMonths = state.availableMonths,
                onMonthSelected = onMonthSelected
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                SummaryCardsRow(
                    incomeCents = state.totalIncomeCents,
                    expensesCents = state.totalCents
                )

                Spacer(Modifier.height(16.dp))

                RemainingBalanceCard(
                    incomeCents = state.totalIncomeCents,
                    expensesCents = state.totalIncomeCents
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(16.dp))

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    state.expenses.isEmpty() -> {
                        EmptyExpenses(
                            monthLabel = "${monthName(state.selectedMonth)} ${state.selectedYear}"
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.expenses, key = { it.id }) { expense ->
                                ExpenseRow(
                                    expense = expense,
                                    onClick = { onExpenseClick(expense.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatCents(expense.amountCents),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun EmptyExpenses(monthLabel: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No expenses in $monthLabel",
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black.copy(0.5f)
            )
        }
    }
}

private fun monthName(month: Int): String =
    Month(month).name.lowercase().replaceFirstChar { it.uppercase() }

fun formatCents(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val absolute = abs(cents)
    val pesos =
        (absolute / 100)
            .toString()
            .reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()
    val centavos = (absolute % 100).toString().padStart(2, '0')
    return "$sign₱$pesos.$centavos"
}
