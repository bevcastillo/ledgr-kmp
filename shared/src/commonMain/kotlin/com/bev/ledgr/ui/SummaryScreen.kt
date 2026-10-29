package com.bev.ledgr.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bev.ledgr.data.CategoryTotal
import kotlinx.datetime.Month
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SummaryRoute(onBack: () -> Unit, viewModel: SummaryViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SummaryScreen(
        state = state,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onBack = onBack
    )
}

@Composable
fun SummaryScreen(
    state: SummaryUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPreviousMonth) { Text("‹ Prev") }
                Text(monthLabel(state.selected), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onNextMonth) { Text("Next ›") }
            }

            Text(
                text = "Total: ${formatCents(state.grandTotalCents)}",
                style = MaterialTheme.typography.labelLarge
            )

            if (state.totals.isEmpty()) {
                Text(
                    "No expenses this month",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.5f)
                )
            } else {
                CategoryBarChart(totals = state.totals)
            }
        }
    }
}

@Composable
fun CategoryBarChart(totals: List<CategoryTotal>, modifier: Modifier = Modifier) {
    val max = totals.maxOf { it.totalCents }.toFloat()
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        totals.forEach { item ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.category)
                    Text(formatCents(item.totalCents))
                }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                ) {
                    val radius = CornerRadius(size.height / 2)
                    drawRoundRect(color = trackColor, cornerRadius = radius)
                    drawRoundRect(
                        color = barColor,
                        size = Size(size.width * (item.totalCents / max), size.height),
                        cornerRadius = radius
                    )
                }
            }
        }
    }
}

private fun monthLabel(selected: SelectedMonth): String {
    val name = Month(selected.month).name.lowercase().replaceFirstChar { it.uppercase() }
    return "$name ${selected.year}"
}
