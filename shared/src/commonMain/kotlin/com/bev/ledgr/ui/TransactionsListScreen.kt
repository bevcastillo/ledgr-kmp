package com.bev.ledgr.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bev.ledgr.data.TransactionType
import com.bev.ledgr.ui.components.TransactionsRow
import com.bev.ledgr.ui.theme.primaryLight
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionListRoute(
    type: TransactionType?,
    year: Int,
    month: Int,
    onTransactionClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: ExpenseListViewModel = koinViewModel()
) {
    LaunchedEffect(year, month) {
        viewModel.onYearSelected(year)
        viewModel.onMonthSelected(month)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TransactionListScreen(
        state = state,
        type = type,
        onTransactionClick = onTransactionClick,
        onBack = onBack
    )
}

@Composable
fun TransactionListScreen(
    state: ExpenseListUiState,
    type: TransactionType?,
    onTransactionClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val title: String = when (type) {
        TransactionType.EXPENSE -> "Expenses"
        TransactionType.INCOME -> "Income"
        null -> "All Transactions"
    }

    val cardLabel: String = when (type) {
        TransactionType.EXPENSE -> "Total Expenses"
        TransactionType.INCOME -> "Total Income"
        null -> "Remaining Balance"
    }

    val cardCents: Long = when (type) {
        TransactionType.EXPENSE -> state.totalExpenseCents
        TransactionType.INCOME -> state.totalIncomeCents
        null -> state.totalIncomeCents - state.totalExpenseCents
    }

    val transactions = remember(state.transactions, type) {
        if (type == null) {
            state.transactions
        } else {
            state.transactions.filter { it.type == type }
        }
    }

    val monthLabel = "${monthName(state.selectedMonth)} ${state.selectedYear}"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (type != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = primaryLight
                        )
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp)
                        ) {
                            Text(
                                text = cardLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = formatCents(state.totalIncomeCents),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "for ${monthName(state.selectedMonth)} ${state.selectedYear}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                transactions.isEmpty() -> {
                    EmptyTransactions(
                        monthLabel = "No ${title.lowercase()} in $monthLabel"
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        items(transactions, key = { it.id }) { transaction ->
                            TransactionsRow(
                                transaction = transaction,
                                onClick = { onTransactionClick(transaction.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
