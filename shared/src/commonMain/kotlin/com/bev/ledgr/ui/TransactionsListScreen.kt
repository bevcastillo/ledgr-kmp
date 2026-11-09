package com.bev.ledgr.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bev.ledgr.data.Category
import com.bev.ledgr.data.TransactionType
import com.bev.ledgr.ui.components.EmptyTransactionsCard
import com.bev.ledgr.ui.components.TransactionsRow
import com.bev.ledgr.ui.components.color
import com.bev.ledgr.ui.components.illustrations
import com.bev.ledgr.ui.theme.primaryLight
import ledgr.shared.generated.resources.Res
import ledgr.shared.generated.resources.receipt_peso
import ledgr.shared.generated.resources.wallet_peso
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionListRoute(
    type: TransactionType?,
    year: Int,
    month: Int,
    onTransactionClick: (Long) -> Unit,
    category: Category?,
    onBack: () -> Unit,
    viewModel: TransactionListViewModel = koinViewModel()
) {
    LaunchedEffect(year, month) {
        viewModel.onYearSelected(year)
        viewModel.onMonthSelected(month)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TransactionListScreen(
        state = state,
        type = type,
        category = category,
        onTransactionClick = onTransactionClick,
        onBack = onBack
    )
}

@Composable
fun TransactionListScreen(
    state: TransactionListUiState,
    type: TransactionType?,
    category: Category?,
    onTransactionClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val categoryLabel = category?.label ?: "Transactions"

    val title: String = when (type) {
        TransactionType.EXPENSE -> "Expenses"
        TransactionType.INCOME -> "Income"
        null -> if (category == Category.OTHERS) {
            categoryLabel
        } else {
            "All $categoryLabel"
        }
    }

    val cardLabel: String = when (type) {
        TransactionType.EXPENSE -> "You spent"
        TransactionType.INCOME -> "You received"
        null -> "You spent"
    }

    val monthLabel = "${monthName(state.selectedMonth)} ${state.selectedYear}"
    val cardBottomLabel: String =
        if (category != null) {
            "on ${categoryLabel.lowercase()} in $monthLabel"
        } else {
            "in $monthLabel"
        }

    val cardCents: String = formatCents(
        if (type == TransactionType.INCOME) {
            state.totalIncomeCents
        } else {
            state.totalExpenseCents
        }
    )

    val transactions = remember(state.transactions, type, category) {
        state.transactions.filter { transaction ->
            val matchesType = type == null || transaction.type == type
            val matchesCategory = category == null || transaction.category == category
            matchesType && matchesCategory
        }
    }

    val cardColor: Color =
        category?.color
            ?.copy(alpha = 0.15f)
            ?.compositeOver(MaterialTheme.colorScheme.surface)
            ?: primaryLight

    val cardIllustration: DrawableResource = when {
        category != null -> category.illustrations
        type == TransactionType.INCOME -> Res.drawable.wallet_peso
        else -> Res.drawable.receipt_peso
    }

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
            val expensePerCategoryCents: Long = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amountCents }

            if (type != null) {
                MonthlyTotalCard(
                    cardLabel = cardLabel,
                    cardCents = cardCents,
                    cardBottomLabel = cardBottomLabel,
                    cardIllustration = cardIllustration
                )
            } else {
                MonthlyTotalCard(
                    cardLabel = cardLabel,
                    cardCents = formatCents(expensePerCategoryCents),
                    cardBottomLabel = cardBottomLabel,
                    containerColor = cardColor,
                    cardIllustration = cardIllustration
                )
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
                    EmptyTransactionsCard("No ${title.lowercase()} in $monthLabel")
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        itemsIndexed(transactions, key = { _, transaction -> transaction.id }) {
                                index,
                                transaction
                            ->
                            TransactionsRow(
                                transaction = transaction,
                                onClick = { onTransactionClick(transaction.id) },
                                showDivider = index < transactions.lastIndex,
                                showCategoryPill = category == null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyTotalCard(
    cardLabel: String,
    cardCents: String,
    cardBottomLabel: String,
    cardIllustration: DrawableResource,
    containerColor: Color = primaryLight
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Text(
                    text = cardLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Black.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = cardCents,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = cardBottomLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }

            Image(
                painter = painterResource(cardIllustration),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}
