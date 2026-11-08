package com.bev.ledgr.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowRightAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bev.ledgr.data.Category
import com.bev.ledgr.data.TransactionType
import com.bev.ledgr.ui.components.EmptyTransactionsCard
import com.bev.ledgr.ui.components.ExpandableFabMenu
import com.bev.ledgr.ui.components.FabMenuItem
import com.bev.ledgr.ui.components.MonthSelector
import com.bev.ledgr.ui.components.RemainingBalanceCard
import com.bev.ledgr.ui.components.SummaryCardsRow
import com.bev.ledgr.ui.components.TransactionsRow
import com.bev.ledgr.ui.components.YearPicker
import com.bev.ledgr.ui.theme.primaryColor
import kotlin.math.abs
import kotlin.time.Instant
import kotlinx.coroutines.launch
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

private const val RECENT_TRANSACTION_LIMIT = 5

@Composable
fun HomeRoute(
    onAddClick: (TransactionType) -> Unit,
    onTransactionItemClick: (Long) -> Unit,
    onSummaryClick: () -> Unit,
    onTransactionClick: (
        type: TransactionType?,
        category: Category?,
        year: Int,
        month: Int
    ) -> Unit,
    viewModel: TransactionListViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onAddClick = onAddClick,
        onTransactionItemClick = onTransactionItemClick,
        onSummaryClick = onSummaryClick,
        onTransactionClick = onTransactionClick,
        onYearSelected = viewModel::onYearSelected,
        onMonthSelected = viewModel::onMonthSelected
    )
}

@Composable
fun HomeScreen(
    state: TransactionListUiState,
    onAddClick: (TransactionType) -> Unit,
    onTransactionItemClick: (Long) -> Unit,
    onSummaryClick: () -> Unit,
    onTransactionClick: (
        type: TransactionType?,
        category: Category?,
        year: Int,
        month: Int
    ) -> Unit,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit
) {
    var fabExpanded by rememberSaveable { mutableStateOf(false) }
    val fabItems = listOf(
        FabMenuItem(label = "Add expense", onClick = { onAddClick(TransactionType.EXPENSE) }),
        FabMenuItem(label = "Add income", onClick = { onAddClick(TransactionType.INCOME) })
    )
    val recentTransactions = state.transactions.take(RECENT_TRANSACTION_LIMIT)
    val exceedsRecentLimit = state.transactions.size > RECENT_TRANSACTION_LIMIT

    val scope = rememberCoroutineScope()

    val snackBarHostState = remember { SnackbarHostState() }

    fun showSnackBarMessage(message: String) {
        scope.launch {
            snackBarHostState.currentSnackbarData?.dismiss()
            snackBarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Home",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onSummaryClick) {
                        Text(
                            text = "Summary",
                            color = primaryColor,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
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
            ExpandableFabMenu(
                items = fabItems,
                expanded = fabExpanded,
                onExpandedChange = { fabExpanded = it },
                containerColor = primaryColor,
                contentColor = Color.White
            )
        }
    ) { padding ->
        val remainingBalance: Long = state.totalIncomeCents - state.totalExpenseCents
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

            /*
             * Everything below the month selector scrolls together, so the screen stays
             * usable in landscape. The Column is at least as tall as the visible area
             * (heightIn(min = maxHeight)), which gives the weighted Spacers real space to
             * fill: they center the empty/loading state in the lower part of the screen.
             * When content is taller than the screen, the Spacers shrink to zero and the
             * Column scrolls. Bottom padding keeps the FAB from covering the last item.
             */
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight)
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp)
                ) {
                    SummaryCardsRow(
                        incomeCents = state.totalIncomeCents,
                        expensesCents = state.totalExpenseCents,
                        onIncomeClick = {
                            if (state.totalIncomeCents == 0L) {
                                showSnackBarMessage("No income recorded yet")
                            } else {
                                onTransactionClick(
                                    TransactionType.INCOME,
                                    null,
                                    state.selectedYear,
                                    state.selectedMonth
                                )
                            }
                        },
                        onExpensesClick = {
                            if (state.totalExpenseCents == 0L) {
                                showSnackBarMessage("No expenses recorded yet")
                            } else {
                                onTransactionClick(
                                    TransactionType.EXPENSE,
                                    null,
                                    state.selectedYear,
                                    state.selectedMonth
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    RemainingBalanceCard(
                        remainingBalance = formatCents(remainingBalance)
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (exceedsRecentLimit) {
                            TextButton(onClick = {
                                onTransactionClick(
                                    null,
                                    null,
                                    state.selectedYear,
                                    state.selectedMonth
                                )
                            }) {
                                Text(text = "See All", color = primaryColor)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    when {
                        state.isLoading -> {
                            Spacer(Modifier.weight(1f))
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(Modifier.weight(1f))
                        }
                        state.transactions.isEmpty() -> {
                            Spacer(Modifier.weight(1f))
                            EmptyTransactionsCard(
                                message = "No transactions in ${monthName(
                                    state.selectedMonth
                                )} ${state.selectedYear}",
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.weight(1f))
                        }
                        else -> {
                            recentTransactions.forEachIndexed { index, transaction ->
                                TransactionsRow(
                                    transaction = transaction,
                                    onClick = { onTransactionItemClick(transaction.id) },
                                    showDivider = index < recentTransactions.lastIndex
                                )
                            }

                            if (exceedsRecentLimit) {
                                SeeAllButton(
                                    onSeeAllClick = {
                                        onTransactionClick(
                                            null,
                                            null,
                                            state.selectedYear,
                                            state.selectedMonth
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = fabExpanded, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.32f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { fabExpanded = false }
                )
            }
        }
    }
}

@Composable
fun SeeAllButton(onSeeAllClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = onSeeAllClick) {
            Text(text = "See All", color = primaryColor)
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowRightAlt,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
        }
    }
}

fun monthName(month: Int): String =
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

/** 1760054400000 -> "10/10/25" (month/day/two-digit year, local time zone). */
fun formatShortDate(millis: Long): String {
    val date = Instant.fromEpochMilliseconds(millis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    val month = date.month.number.toString().padStart(2, '0')
    val day = date.day.toString().padStart(2, '0')
    val year = (date.year % 100).toString().padStart(2, '0')

    return "$month/$day/$year"
}
