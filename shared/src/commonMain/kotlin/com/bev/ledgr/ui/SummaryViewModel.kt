package com.bev.ledgr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bev.ledgr.data.CategoryTotal
import com.bev.ledgr.data.ExpenseRepository
import com.bev.ledgr.data.monthRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlin.time.Clock

data class SelectedMonth(val year: Int, val month: Int)

data class SummaryUiState(
    val selected: SelectedMonth,
    val totals: List<CategoryTotal> = emptyList()
) {
    val grandTotalCents: Long get() = totals.sumOf { it.totalCents }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SummaryViewModel(
    private val repository: ExpenseRepository,
    clock: Clock = Clock.System
): ViewModel() {

    private val today = clock.todayIn(TimeZone.currentSystemDefault())
    private val selected = MutableStateFlow(SelectedMonth(today.year, today.month.number))

    val uiState: StateFlow<SummaryUiState> = selected
        .flatMapLatest { month ->
            val range = monthRange(month.year, month.month)
            repository.observeCategoryTotals(range.startMillis, range.endMillis)
                .map { totals -> SummaryUiState(month, totals) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SummaryUiState(selected.value)
        )

    fun previousMonth() = selected.update {
        if (it.month == 1) SelectedMonth(it.year - 1, 12) else it.copy(month = it.month - 1)
    }

    fun nextMonth() = selected.update {
        if (it.month == 12) SelectedMonth(it.year + 1, 1) else it.copy(month = it.month + 1)
    }
}