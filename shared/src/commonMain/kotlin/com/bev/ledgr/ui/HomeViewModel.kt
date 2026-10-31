package com.bev.ledgr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bev.ledgr.data.Transaction
import com.bev.ledgr.data.TransactionRepository
import com.bev.ledgr.data.TransactionType
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

private val AllMonths = (1..12).toList()

data class SelectedMonth(val year: Int, val month: Int)

data class ExpenseListUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalExpenseCents: Long = 0,
    val totalIncomeCents: Long = 0,
    val isLoading: Boolean = true,
    val selectedYear: Int,
    val selectedMonth: Int,
    val availableYears: List<Int> = listOf(selectedYear),
    val availableMonths: List<Int> = (1..selectedMonth).toList(),
    val type: TransactionType = TransactionType.EXPENSE
)

class ExpenseListViewModel(
    private val repository: TransactionRepository,
    clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) : ViewModel() {
    private val today = clock.todayIn(timeZone)
    private val currentYear = today.year
    private val currentMonth = today.month.number

    private val selection = MutableStateFlow(SelectedMonth(currentYear, currentMonth))

    val uiState: StateFlow<ExpenseListUiState> =
        combine(repository.observeExpenses(), selection) { allTransactions, selected ->
            val oldestYear = allTransactions.minOfOrNull { dateOf(it).year } ?: currentYear
            val availableYears = (minOf(oldestYear, currentYear)..currentYear).toList().reversed()
            val availableMonths: List<Int> = AllMonths

            val transactionsInMonth =
                allTransactions.filter { transaction ->
                    val date = dateOf(transaction)
                    date.year == selected.year && date.month.number == selected.month
                }

            // Sum of each transaction type
            val totalExpenseCents = transactionsInMonth
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amountCents }

            val totalIncomeCents = transactionsInMonth
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amountCents }

            ExpenseListUiState(
                transactions = transactionsInMonth,
                totalExpenseCents = totalExpenseCents,
                totalIncomeCents = totalIncomeCents,
                isLoading = false,
                selectedYear = selected.year,
                selectedMonth = selected.month,
                availableYears = availableYears,
                availableMonths = AllMonths
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue =
            ExpenseListUiState(
                selectedYear = currentYear,
                selectedMonth = currentMonth
            )
        )

    fun onYearSelected(year: Int) {
        selection.update { it.copy(year = year) }
    }

    fun onMonthSelected(month: Int) {
        selection.update { it.copy(month = month) }
    }

    private fun dateOf(transaction: Transaction): LocalDate =
        Instant.fromEpochMilliseconds(transaction.dateMillis).toLocalDateTime(timeZone).date
}
