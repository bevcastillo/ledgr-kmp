package com.bev.ledgr.ui

import androidx.collection.longListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bev.ledgr.data.Expense
import com.bev.ledgr.data.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val totalCents: Long = 0,
    val isLoading: Boolean = true
)

class ExpenseListViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {
    val uiState: StateFlow<ExpenseListUiState> = repository.observeExpenses()
        .map { list ->
            ExpenseListUiState(
                expenses = list,
                totalCents = list.sumOf { it.amountCents },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExpenseListUiState()
        )

    @OptIn(ExperimentalTime::class)
    fun addSampleExpense() {
        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    title = "Coffee",
                    amountCents = 15075,
                    category = "Food",
                    dateMillis = Clock.System.now().toEpochMilliseconds()
                )
            )
        }
    }
}