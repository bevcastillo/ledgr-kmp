package com.bev.ledgr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bev.ledgr.data.CategoryTotal
import com.bev.ledgr.data.TransactionRepository
import com.bev.ledgr.data.monthRange
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

enum class SummaryPeriod { Monthly, Annually }

data class SummaryUiState(
    val period: SummaryPeriod = SummaryPeriod.Monthly,
    val year: Int,
    val month: Int,
    val availableYears: List<Int> = listOf(year),
    val totals: List<CategoryTotal> = emptyList()
) {
    val grandTotalCents: Long get() = totals.sumOf { it.totalCents }
}

private data class Selection(val period: SummaryPeriod, val year: Int, val month: Int)

@OptIn(ExperimentalCoroutinesApi::class)
class SummaryViewModel(private val repository: TransactionRepository, clock: Clock = Clock.System) :
    ViewModel() {
    private val timeZone = TimeZone.currentSystemDefault()

    private val today = clock.todayIn(timeZone)
    private val selection = MutableStateFlow(
        Selection(
            period = SummaryPeriod.Monthly,
            year = today.year,
            month = today.month.number
        )
    )

    // Years from the oldest expense up to this year, for the year dropdown
    private val availableYears: Flow<List<Int>> = repository.observeEarliestExpenseMillis()
        .map { earliestMillis ->
            val firstYear = earliestMillis
                ?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone).year }
                ?: today.year
            (minOf(firstYear, today.year)..today.year).toList()
        }

    // Totals are paired with the selection that produced them, so the screen
    // never shows a new month's label with the previous month's numbers.
    private val selectionWithTotals: Flow<Pair<Selection, List<CategoryTotal>>> = selection
        .flatMapLatest { sel ->
            val (startMillis, endMillis) = rangeFor(sel)
            repository.observeCategoryTotals(startMillis, endMillis)
                .map { totals -> sel to totals.sortedByDescending { it.totalCents } }
        }

    val uiState: StateFlow<SummaryUiState> = combine(
        selectionWithTotals,
        availableYears
    ) { (sel, totals), years ->
        SummaryUiState(
            period = sel.period,
            year = sel.year,
            month = sel.month,
            availableYears = (years + sel.year).distinct().sorted(),
            totals = totals
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SummaryUiState(
            year = today.year,
            month = today.month.number
        )
    )

    fun selectPeriod(period: SummaryPeriod) = selection.update { it.copy(period = period) }

    fun selectYear(year: Int) = selection.update { it.copy(year = year) }

    fun selectMonth(month: Int) = selection.update { it.copy(month = month) }

    private fun rangeFor(sel: Selection): Pair<Long, Long> = when (sel.period) {
        SummaryPeriod.Monthly -> {
            val range = monthRange(sel.year, sel.month)
            range.startMillis to range.endMillis
        }

        SummaryPeriod.Annually -> {
            // January's start through December's end covers the whole year
            monthRange(sel.year, 1).startMillis to monthRange(sel.year, 12).endMillis
        }
    }
}
