package com.bev.ledgr.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bev.ledgr.data.TransactionType

private val ExpandedWidth = 840.dp

@Composable
fun ExpenseListDetail(
    onAddClickCompact: (TransactionType) -> Unit,
    onExpenseListCompact: (Long) -> Unit,
    onSummaryClick: () -> Unit,
    onTransactionClick: (type: TransactionType?, year: Int, month: Int) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < ExpandedWidth) {
            HomeRoute(
                onAddClick = onAddClickCompact,
                onTransactionItemClick = onExpenseListCompact,
                onSummaryClick = onSummaryClick,
                onTransactionClick = onTransactionClick
            )
        } else {
            TwoPaneLayout(
                onSummaryClick = onSummaryClick,
                onTransactionClick = onTransactionClick
            )
        }
    }
}

@Composable
private fun TwoPaneLayout(
    onSummaryClick: () -> Unit,
    onTransactionClick: (type: TransactionType?, year: Int, month: Int) -> Unit
) {
    var showForm by rememberSaveable { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedType by rememberSaveable {
        mutableStateOf<TransactionType?>(TransactionType.EXPENSE)
    }
    var formSession by rememberSaveable { mutableIntStateOf(0) }

    fun openForm(id: Long?, type: TransactionType?) {
        selectedId = id
        selectedType = type
        formSession++
        showForm = true
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            HomeRoute(
                onAddClick = { type -> openForm(id = null, type = type) },
                onTransactionItemClick = { id -> openForm(id, type = null) },
                onSummaryClick = onSummaryClick,
                onTransactionClick = onTransactionClick
            )
        }
        VerticalDivider(Modifier.fillMaxHeight())
        Box(Modifier.weight(1f)) {
            if (showForm) {
                AddTransactionRoute(
                    transactionId = selectedId,
                    type = selectedType,
                    viewModelKey = "expense-form-$formSession",
                    onDone = {
                        showForm = false
                        selectedId = null
                    }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Select an expense or tap + to add one",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
