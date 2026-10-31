package com.bev.ledgr.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bev.ledgr.data.Category
import com.bev.ledgr.data.CategoryTotal
import com.bev.ledgr.ui.components.color
import com.bev.ledgr.ui.theme.primaryColor
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SummaryRoute(
    onBack: () -> Unit,
    onSeeAllTransactions: () -> Unit,
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
    onSeeAllTransactions: () -> Unit,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            PeriodToggle(
                selected = state.period,
                onSelect = onPeriodChange
            )

            if (state.period == SummaryPeriod.Monthly) {
                Spacer(Modifier.height(24.dp))
                MonthSelector(
                    selectedMonth = state.month,
                    onMonthSelect = onMonthSelect
                )
            }

            Spacer(Modifier.height(32.dp))
            SpendingDonut(
                totals = state.totals,
                modifier = Modifier.fillMaxWidth(0.66f)
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
            Text(
                text = "See all transactions",
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = TextDecoration.Underline
                ),
                fontWeight = FontWeight.Medium,
                color = primaryColor,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(role = Role.Button, onClick = onSeeAllTransactions)
                    .padding(4.dp)
            )

            Spacer(Modifier.height(8.dp))
            if (state.totals.isEmpty()) {
                Text(
                    text = "No expenses for thie period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                state.totals.forEach { item ->
                    CategoryRow(
                        item = item,
                        onClick = { onCategoryClick(item.category) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
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
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.toString(),
                            fontWeight = if (option ==
                                year
                            ) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                            color = if (option ==
                                year
                            ) {
                                primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
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
            .clip(RoundedCornerShape(10.dp))
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
                        .padding(vertical = 8.dp)
                        .width(1.dp)
                        .background(primaryColor.copy(alpha = 0.4f))
                )
            }

            val isSelected = period == selected
            Text(
                text = period.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) {
                    primaryColor
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.5f
                    )
                },
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onSelect(period) }
                    )
            )
        }
    }
}

@Composable
private fun MonthSelector(selectedMonth: Int, onMonthSelect: (Int) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val itemWidth = maxWidth / 3
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedMonth - 1)

        LaunchedEffect(selectedMonth) {
            listState.animateScrollToItem(selectedMonth - 1)
        }

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = itemWidth),
            modifier = Modifier.selectableGroup()
        ) {
            items((1..12).toList()) { month ->
                val isSelected = month == selectedMonth
                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = monthName(month),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) {
                            primaryColor
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.5f
                            )
                        },
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) {
                                    primaryColor.copy(
                                        alpha = 0.15f
                                    )
                                } else {
                                    Color.Transparent
                                }
                            )
                            .selectable(
                                selected = isSelected,
                                role = Role.Tab,
                                onClick = { onMonthSelect(month) }
                            )
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpendingDonut(
    totals: List<CategoryTotal>,
    modifier: Modifier = Modifier,
    centerContent: @Composable () -> Unit
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val grandTotal = totals.sumOf { it.totalCents }.toFloat()
    val description = if (totals.isEmpty()) {
        "No spending this period"
    } else {
        totals.take(3).joinToString(prefix = "Spending by category: ") {
            "${it.category} ${formatPeso(it.totalCents)}"
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
                startAngle += sweep
            }
        }
        centerContent()
    }
}

@Composable
private fun CategoryRow(item: CategoryTotal, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(item.category.color)
        )
        Spacer(Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.padding(vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.category.displayName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = transactionLabel(item.transactionCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = formatPeso(item.totalCents),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(16.dp))
                Chevron(
                    direction = ChevronDirection.Right,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(width = 8.dp, height = 14.dp)
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

private enum class ChevronDirection { Left, Right, Up, Down }

@Composable
private fun Chevron(
    direction: ChevronDirection,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val inset = strokeWidth.toPx() / 2
        val left = inset
        val right = size.width - inset
        val top = inset
        val bottom = size.height - inset
        val midX = size.width / 2
        val midY = size.height / 2

        val points = when (direction) {
            ChevronDirection.Left -> listOf(
                Offset(right, top),
                Offset(left, midY),
                Offset(right, bottom)
            )
            ChevronDirection.Right -> listOf(
                Offset(left, top),
                Offset(right, midY),
                Offset(left, bottom)
            )
            ChevronDirection.Up -> listOf(
                Offset(left, bottom),
                Offset(midX, top),
                Offset(right, bottom)
            )
            ChevronDirection.Down -> listOf(
                Offset(left, top),
                Offset(midX, bottom),
                Offset(right, top)
            )
        }

        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            lineTo(points[1].x, points[1].y)
            lineTo(points[2].x, points[2].y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

private fun formatPeso(cents: Long): String {
    val pesos = (cents + 50) / 100
    val grouped = pesos.toString().reversed().chunked(3).joinToString(",").reversed()
    return "₱$grouped"
}

private fun transactionLabel(count: Int): String =
    if (count == 1) "1 Transaction" else "$count Transactions"

private fun Category.displayName(): String = name.lowercase()
    .split('_')
    .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
