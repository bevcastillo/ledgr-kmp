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

private val ExpandedWidth = 840.dp

@Composable
fun ExpenseListDetail(
    onAddClickCompact: () -> Unit,
    onExpenseListCompact: (Long) -> Unit,
    onSummaryClick: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < ExpandedWidth) {
            ExpenseListRoute(
                onAddClick = onAddClickCompact,
                onExpenseClick = onExpenseListCompact,
                onSummaryClick = onSummaryClick
            )
        } else {
            TwoPaneLayout(onSummaryClick = onSummaryClick)
        }
    }
}

@Composable
private fun TwoPaneLayout(onSummaryClick: () -> Unit) {
    var showForm by rememberSaveable { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var formSession by rememberSaveable { mutableIntStateOf(0) }

    fun openForm(id: Long?) {
        selectedId = id
        formSession++
        showForm = true
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            ExpenseListRoute(
                onAddClick = { openForm(null) },
                onExpenseClick = { id -> openForm(id) },
                onSummaryClick = onSummaryClick
            )
        }
        VerticalDivider(Modifier.fillMaxHeight())
        Box(Modifier.weight(1f)) {
            if (showForm) {
                AddExpenseRoute(
                    expenseId = selectedId,
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
