package com.bev.ledgr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bev.ledgr.data.CategoryTotal
import com.bev.ledgr.data.Transaction
import com.bev.ledgr.data.TransactionRepository
import com.bev.ledgr.data.TransactionType
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

enum class SummaryPeriod { Monthly, Annual }

private val EmptyMonths: List<Long> = List(12) { 0L }

data class SummaryUiState(
    val period: SummaryPeriod = SummaryPeriod.Monthly,
    val year: Int,
    val month: Int,
    val availableYears: List<Int> = listOf(year),
    val totals: List<CategoryTotal> = emptyList(),
    val monthlyExpenseCents: List<Long> = EmptyMonths,
    val monthlyIncomeCents: List<Long> = EmptyMonths
) {
    val grandTotalCents: Long get() = totals.sumOf { it.totalCents }
}

private data class Selection(val period: SummaryPeriod, val year: Int, val month: Int)

private data class SelectionData(
    val selection: Selection,
    val totals: List<CategoryTotal> = emptyList(),
    val monthlyExpenseCents: List<Long> = EmptyMonths,
    val monthlyIncomeCents: List<Long> = EmptyMonths
)

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
    private val availableYears: Flow<List<Int>> = repository.observeEarliestTransactionMillis()
        .map { earliestMillis ->
            val firstYear = earliestMillis
                ?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone).year }
                ?: today.year
            (minOf(firstYear, today.year)..today.year).toList()
        }

    // Totals are paired with the selection that produced them, so the screen
    // never shows a new month's label with the previous month's numbers.
    private val selectionWithData: Flow<SelectionData> = selection
        .flatMapLatest { sel ->
            val (startMillis, endMillis) = rangeFor(sel)
            when (sel.period) {
                SummaryPeriod.Monthly ->
                    repository.observeCategoryTotals(startMillis, endMillis)
                        .map { totals ->
                            SelectionData(
                                selection = sel,
                                totals = totals.sortedByDescending { it.totalCents }
                            )
                        }

                SummaryPeriod.Annual ->
                    repository.observeTransactions(startMillis, endMillis)
                        .map { transactions ->
                            SelectionData(
                                selection = sel,
                                monthlyExpenseCents = monthlyTotals(
                                    transactions,
                                    TransactionType.EXPENSE
                                ),
                                monthlyIncomeCents = monthlyTotals(
                                    transactions,
                                    TransactionType.INCOME
                                )
                            )
                        }
            }
        }

    val uiState: StateFlow<SummaryUiState> = combine(
        selectionWithData,
        availableYears
    ) { data, years ->
        val sel = data.selection
        SummaryUiState(
            period = sel.period,
            year = sel.year,
            month = sel.month,
            availableYears = (years + sel.year).distinct().sorted(),
            totals = data.totals,
            monthlyExpenseCents = data.monthlyExpenseCents,
            monthlyIncomeCents = data.monthlyIncomeCents
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

        SummaryPeriod.Annual -> {
            // January's start through December's end covers the whole year
            monthRange(sel.year, 1).startMillis to monthRange(sel.year, 12).endMillis
        }
    }

    // Buckets the year's transactions into 12 months using the device time zone,
    // the same one monthRange uses, so a month's bar matches its Monthly view.
    private fun monthlyTotals(transactions: List<Transaction>, type: TransactionType): List<Long> {
        val buckets = LongArray(12)
        transactions
            .filter { it.type == type }
            .forEach { transaction ->
                val month = Instant.fromEpochMilliseconds(transaction.dateMillis)
                    .toLocalDateTime(timeZone)
                    .month
                    .number
                buckets[month - 1] += transaction.amountCents
            }
        return buckets.toList()
    }
}
