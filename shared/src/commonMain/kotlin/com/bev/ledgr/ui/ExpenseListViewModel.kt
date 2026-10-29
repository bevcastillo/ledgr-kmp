package com.bev.ledgr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bev.ledgr.data.Expense
import com.bev.ledgr.data.ExpenseRepository
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

data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val totalCents: Long = 0,
    val totalIncomeCents: Long = 0,
    val isLoading: Boolean = true,
    val selectedYear: Int,
    val selectedMonth: Int,
    val availableYears: List<Int> = listOf(selectedYear),
    val availableMonths: List<Int> = (1..selectedMonth).toList()
)

class ExpenseListViewModel(
    private val repository: ExpenseRepository,
    clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) : ViewModel() {
    private val today = clock.todayIn(timeZone)
    private val currentYear = today.year
    private val currentMonth = today.month.number

    private val selection = MutableStateFlow(SelectedMonth(currentYear, currentMonth))

    val uiState: StateFlow<ExpenseListUiState> =
        combine(repository.observeExpenses(), selection) { allExpenses, selected ->
            val oldestYear = allExpenses.minOfOrNull { dateOf(it).year } ?: currentYear
            val availableYears = (minOf(oldestYear, currentYear)..currentYear).toList().reversed()
            val availableMonths: List<Int> = AllMonths

            val expensesInMonth =
                allExpenses.filter { expense ->
                    val date = dateOf(expense)
                    date.year == selected.year && date.month.number == selected.month
                }

            ExpenseListUiState(
                expenses = expensesInMonth,
                totalCents = expensesInMonth.sumOf { it.amountCents },
                totalIncomeCents = 0,
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

    private fun dateOf(expense: Expense): LocalDate =
        Instant.fromEpochMilliseconds(expense.dateMillis).toLocalDateTime(timeZone).date
}
