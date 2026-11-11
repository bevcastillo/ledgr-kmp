package com.bev.ledgr.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bev.ledgr.data.Category
import com.bev.ledgr.data.CategoryTotal
import com.bev.ledgr.data.TransactionType
import com.bev.ledgr.ui.components.AnnualReport
import com.bev.ledgr.ui.components.CategoryRowItem
import com.bev.ledgr.ui.components.Chevron
import com.bev.ledgr.ui.components.ChevronDirection
import com.bev.ledgr.ui.components.EmptyTransactionsCard
import com.bev.ledgr.ui.components.MonthSelector
import com.bev.ledgr.ui.components.color
import com.bev.ledgr.ui.components.formatPeso
import com.bev.ledgr.ui.theme.primaryColor
import com.bev.ledgr.ui.theme.primaryDark
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val MIN_LABEL_FRACTION = 0.05f

private val AllMonths = (1..12).toList()

@Composable
fun SummaryRoute(
    onBack: () -> Unit,
    onSeeAllTransactions: (
        type: TransactionType?,
        category: Category?,
        year: Int,
        month: Int
    ) -> Unit,
    onCategoryClick: (Category) -> Unit,
    viewModel: SummaryViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SummaryScreen(
        state = state,
        onBack = onBack,
        onPeriodChange = viewModel::selectPeriod,
        onYearSelect = viewModel::selectYear,
        onMonthSelect = viewModel::selectMonth,
        onSeeAllTransactions = onSeeAllTransactions,
        onCategoryClick = onCategoryClick
    )
}

@Composable
fun SummaryScreen(
    state: SummaryUiState,
    onBack: () -> Unit,
    onPeriodChange: (SummaryPeriod) -> Unit,
    onYearSelect: (Int) -> Unit,
    onMonthSelect: (Int) -> Unit,
    onSeeAllTransactions: (
        type: TransactionType?,
        category: Category?,
        year: Int,
        month: Int
    ) -> Unit,
    onCategoryClick: (Category) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Summary",
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
                },
                actions = {
                    YearSelector(
                        year = state.year,
                        years = state.availableYears,
                        onYearSelect = onYearSelect
                    )
                }
            )
        }
    ) { padding ->
        /*
         * The whole screen scrolls, so it stays usable in landscape. The Column is at least
         * as tall as the visible area (heightIn(min = maxHeight)), which gives the weighted
         * Spacers in the empty states real space to fill, centering the empty message.
         * When content is taller than the screen, those Spacers shrink to zero and it scrolls.
         */
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                PeriodToggle(
                    selected = state.period,
                    onSelect = onPeriodChange
                )

                when (state.period) {
                    SummaryPeriod.Monthly -> MonthlyContent(
                        state = state,
                        onMonthSelect = onMonthSelect,
                        onSeeAllTransactions = onSeeAllTransactions,
                        onCategoryClick = onCategoryClick,
                        hasExpenses = state.totals.isNotEmpty()
                    )

                    SummaryPeriod.Annual -> {
                        Spacer(Modifier.height(8.dp))
                        AnnualReport(
                            year = state.year,
                            monthlyIncomeCents = state.monthlyIncomeCents,
                            monthlyExpenseCents = state.monthlyExpenseCents,
                            onMonthClick = onMonthSelect
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YearSelector(year: Int, years: List<Int>, onYearSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val primary = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.padding(end = 12.dp)) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.DropdownList) { expanded = true }
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .semantics { contentDescription = "Year $year, change year" },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = year.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = primary
            )
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Chevron(
                    direction = ChevronDirection.Up,
                    color = primary,
                    modifier = Modifier.size(width = 11.dp, height = 6.dp)
                )
                Spacer(Modifier.height(3.dp))
                Chevron(
                    direction = ChevronDirection.Down,
                    color = primary,
                    modifier = Modifier.size(width = 11.dp, height = 6.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            years.sortedDescending().forEach { option ->
                val isSelected = option == year
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.toString(),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        expanded = false
                        onYearSelect(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun PeriodToggle(selected: SummaryPeriod, onSelect: (SummaryPeriod) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(primaryColor.copy(0.15f))
            .height(IntrinsicSize.Min)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SummaryPeriod.entries.forEachIndexed { index, period ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 10.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            val isSelected = period == selected
            Text(
                text = period.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) {
                    primaryDark
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.6f
                    )
                },
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onSelect(period) }
                    )
                    .padding(horizontal = 28.dp, vertical = 12.dp)
            )
        }
    }
}

// ColumnScope receiver so the empty state can use weighted Spacers to center itself.
@Composable
private fun ColumnScope.MonthlyContent(
    state: SummaryUiState,
    onMonthSelect: (Int) -> Unit,
    onSeeAllTransactions: (
        type: TransactionType?,
        category: Category?,
        year: Int,
        month: Int
    ) -> Unit,
    onCategoryClick: (Category) -> Unit,
    hasExpenses: Boolean
) {
    Spacer(Modifier.height(24.dp))
    MonthSelector(
        selectedMonth = state.month,
        availableMonths = AllMonths,
        onMonthSelected = onMonthSelect
    )

    if (hasExpenses) {
        Spacer(Modifier.height(32.dp))
        SpendingDonut(
            totals = state.totals,
            // Cap the base width so the donut doesn't fill the whole screen in landscape.
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth(0.66f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatPeso(state.grandTotalCents),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "spent",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = { onSeeAllTransactions(null, null, state.year, state.month) },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(text = "See All", color = primaryColor)
            }
        }

        Spacer(Modifier.height(8.dp))

        state.totals.forEachIndexed { index, item ->
            CategoryRowItem(
                item = item,
                onClick = {
                    onCategoryClick(item.category)
                    onSeeAllTransactions(null, item.category, state.year, state.month)
                },
                showDivider = index < state.totals.lastIndex
            )
        }
        Spacer(Modifier.height(24.dp))
    } else {
        Spacer(Modifier.weight(1f))
        EmptyTransactionsCard(
            message = "No expenses for this period",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun SpendingDonut(
    totals: List<CategoryTotal>,
    modifier: Modifier = Modifier,
    centerContent: @Composable () -> Unit
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
    val grandTotal = totals.sumOf { it.totalCents }.toFloat()

    val description = if (totals.isEmpty()) {
        "No spending this period"
    } else {
        totals.take(3).joinToString(prefix = "Spending by category: ") {
            "${it.category.label} ${formatPeso(it.totalCents)}"
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.26f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            val labelRadius = (size.minDimension - stroke) / 2f

            if (grandTotal <= 0f) {
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke)
                )
                return@Canvas
            }

            var startAngle = -90f
            totals.forEach { item ->
                val fraction = item.totalCents / grandTotal
                val sweep = 360f * item.totalCents / grandTotal
                drawArc(
                    color = item.category.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke)
                )

                if (fraction >= MIN_LABEL_FRACTION) {
                    val midAngleRad = (startAngle + sweep / 2f) * (PI.toFloat() / 180f)
                    val labelCenter = Offset(
                        x = center.x + labelRadius * cos(midAngleRad),
                        y = center.y + labelRadius * sin(midAngleRad)
                    )
                    val textColor = if (item.category.color.luminance() > 0.05f) {
                        Color.White.copy(alpha = 0.5f)
                    } else {
                        Color.White
                    }
                    val layout = textMeasurer.measure(
                        text = "${(fraction * 100.0).roundToInt()}%",
                        style = labelStyle.copy(color = textColor)
                    )
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(
                            x = labelCenter.x - layout.size.width / 2f,
                            y = labelCenter.y - layout.size.height / 2f
                        )
                    )
                }

                startAngle += sweep
            }
        }
        centerContent()
    }
}
