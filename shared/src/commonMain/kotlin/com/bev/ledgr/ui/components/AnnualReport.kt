package com.bev.ledgr.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bev.ledgr.ui.theme.primaryColor
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

private val IncomeBarColor = Color(0xFF0D9488)
private val ExpenseBarColor = Color(0xFF4E884D)

private val BarShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)

private const val AXIS_STEPS = 4
private const val BAR_ANIMATION_MILLIS = 600

private val NiceStepMultipliers = listOf(1.0, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0)

private val ReportMonthLabels = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

private val ReportMonthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

/**
 * Annual report: four headline stats plus a grouped income vs. expenses bar chart.
 *
 * Trailing months with no income and no expenses are left out, so the current
 * year shows Jan through the latest month with data instead of empty future months.
 *
 * It doesn't scroll by itself: SummaryScreen already scrolls the whole page.
 *
 * [onMonthClick] receives the 1-based month number of the tapped column.
 */
@Composable
fun AnnualReport(
    year: Int,
    monthlyIncomeCents: List<Long>,
    monthlyExpenseCents: List<Long>,
    onMonthClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthCount = reportedMonthCount(monthlyIncomeCents, monthlyExpenseCents)
    val income = List(monthCount) { monthlyIncomeCents.getOrElse(it) { 0L } }
    val expenses = List(monthCount) { monthlyExpenseCents.getOrElse(it) { 0L } }

    val totalIncome = income.sum()
    val totalExpenses = expenses.sum()
    val balance = totalIncome - totalExpenses

    val range = if (monthCount == 1) {
        ReportMonthLabels[0]
    } else {
        "${ReportMonthLabels[0]}–${ReportMonthLabels[monthCount - 1]}"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "$year overview, $range",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            StatsGrid(
                totalIncome = totalIncome,
                totalExpenses = totalExpenses,
                balance = balance,
            )
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Income vs. expenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))
            IncomeExpenseChart(
                income = income,
                expenses = expenses,
                onMonthClick = onMonthClick,
            )
            Spacer(Modifier.height(12.dp))
            Legend()
        }
    }
}

@Composable
private fun StatsGrid(
    totalIncome: Long,
    totalExpenses: Long,
    balance: Long,
) {
    val balanceColor = if (balance < 0) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val remaining = if (totalIncome > 0) {
        formatPercent(balance * 100.0 / totalIncome)
    } else {
        "—"
    }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Stat(
                label = "Total income",
                value = formatPesoCompact(totalIncome),
                valueColor = primaryColor,
                modifier = Modifier.weight(1f),
            )
            Stat(
                label = "Total expenses",
                value = formatPesoCompact(totalExpenses),
                valueColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
        Row(Modifier.fillMaxWidth()) {
            Stat(
                label = "Remaining balance",
                value = formatPesoCompact(balance),
                valueColor = balanceColor,
                modifier = Modifier.weight(1f),
            )
            Stat(
                label = "Income remaining",
                value = remaining,
                valueColor = balanceColor,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Stat(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = "$label: $value" },
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            maxLines = 1,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun IncomeExpenseChart(
    income: List<Long>,
    expenses: List<Long>,
    onMonthClick: (Int) -> Unit,
    chartHeight: Dp = 200.dp,
) {
    val maxCents = maxOf(income.maxOrNull() ?: 0L, expenses.maxOrNull() ?: 0L)
    val axisMaxCents = niceAxisMax(maxCents)
    val ticks = (0..AXIS_STEPS).map { axisMaxCents * it / AXIS_STEPS }
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        YAxisLabels(ticks = ticks, height = chartHeight)
        Spacer(Modifier.width(8.dp))
        Row(
            modifier = Modifier
                .weight(1f)
                .drawBehind {
                    // Gridlines cover only the plot area, not the month labels below it
                    val plotHeight = chartHeight.toPx()
                    val dash = PathEffect.dashPathEffect(
                        floatArrayOf(4.dp.toPx(), 4.dp.toPx()),
                    )
                    for (step in 0..AXIS_STEPS) {
                        val y = plotHeight - plotHeight * step / AXIS_STEPS
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = if (step == 0) null else dash,
                        )
                    }
                },
        ) {
            income.indices.forEach { index ->
                MonthColumn(
                    index = index,
                    incomeCents = income[index],
                    expenseCents = expenses[index],
                    axisMaxCents = axisMaxCents,
                    chartHeight = chartHeight,
                    onClick = { onMonthClick(index + 1) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun YAxisLabels(
    ticks: List<Long>,
    height: Dp,
) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall

    // Custom layout so each label is vertically centered on its gridline
    Layout(
        content = {
            ticks.forEach { tick ->
                Text(
                    text = formatPesoCompact(tick),
                    style = labelStyle,
                    color = labelColor,
                    maxLines = 1,
                )
            }
        },
        modifier = Modifier
            .height(height)
            .clearAndSetSemantics { },
    ) { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }
        val width = placeables.maxOfOrNull { it.width } ?: 0
        val plotHeight = constraints.maxHeight
        val lastIndex = (placeables.size - 1).coerceAtLeast(1)

        layout(width, plotHeight) {
            placeables.forEachIndexed { index, placeable ->
                val lineY = plotHeight - plotHeight * index / lastIndex
                placeable.place(
                    x = width - placeable.width,
                    y = lineY - placeable.height / 2,
                )
            }
        }
    }
}

@Composable
private fun MonthColumn(
    index: Int,
    incomeCents: Long,
    expenseCents: Long,
    axisMaxCents: Long,
    chartHeight: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = "${ReportMonthNames[index]}: income ${formatPesoWhole(incomeCents)}, " +
            "expenses ${formatPesoWhole(expenseCents)}"

    Column(
        modifier = modifier
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
                onClick(label = "Show ${ReportMonthNames[index]}") {
                    onClick()
                    true
                }
            }
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            AnimatedBar(
                fraction = barFraction(incomeCents, axisMaxCents),
                color = IncomeBarColor,
                modifier = Modifier.weight(1f),
            )
            AnimatedBar(
                fraction = barFraction(expenseCents, axisMaxCents),
                color = ExpenseBarColor,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = ReportMonthLabels[index],
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun AnimatedBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(fraction) {
        animated.animateTo(fraction, tween(durationMillis = BAR_ANIMATION_MILLIS))
    }
    Box(
        modifier = modifier
            .fillMaxHeight(animated.value)
            .clip(BarShape)
            .background(color),
    )
}

@Composable
private fun Legend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(label = "Income", color = IncomeBarColor)
        Spacer(Modifier.width(20.dp))
        LegendItem(label = "Expenses", color = ExpenseBarColor)
    }
}

@Composable
private fun LegendItem(
    label: String,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun reportedMonthCount(
    income: List<Long>,
    expenses: List<Long>,
): Int {
    val months = maxOf(income.size, expenses.size).coerceIn(1, 12)
    val lastWithData = (months - 1 downTo 0).firstOrNull { i ->
        income.getOrElse(i) { 0L } != 0L || expenses.getOrElse(i) { 0L } != 0L
    }
    return (lastWithData ?: (months - 1)) + 1
}

private fun barFraction(
    cents: Long,
    axisMaxCents: Long,
): Float = (cents.coerceAtLeast(0L).toFloat() / axisMaxCents).coerceIn(0f, 1f)

// Rounds the axis top up to a readable step, e.g. a ₱190K max gives 0/50K/100K/150K/200K
private fun niceAxisMax(maxCents: Long): Long {
    if (maxCents <= 0L) return 100_000L * AXIS_STEPS // ₱1,000 per step when there's no data

    val rawStepPesos = maxCents / 100.0 / AXIS_STEPS
    val magnitude = 10.0.pow(floor(log10(rawStepPesos)))
    val multiplier = NiceStepMultipliers.first { it >= rawStepPesos / magnitude - 1e-9 }
    return (multiplier * magnitude * AXIS_STEPS * 100).roundToLong()
}

private fun formatPesoCompact(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val pesos = abs(cents) / 100.0
    val body = when {
        pesos >= 999_950.0 -> formatDecimal(pesos / 1_000_000, 2) + "M"
        pesos >= 999.95 -> {
            val thousands = pesos / 1_000
            formatDecimal(thousands, if (thousands >= 100) 0 else 1) + "K"
        }
        else -> formatDecimal(pesos, 0)
    }
    return "$sign₱$body"
}

private fun formatPesoWhole(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val pesos = (abs(cents) + 50) / 100
    val grouped = pesos.toString().reversed().chunked(3).joinToString(",").reversed()
    return "$sign₱$grouped"
}

private fun formatPercent(value: Double): String {
    val sign = if (value < 0) "-" else ""
    return "$sign${formatDecimal(abs(value), 1)}%"
}

// Common code has no String.format, so decimals are built by hand
private fun formatDecimal(
    value: Double,
    decimals: Int,
): String {
    val factor = 10.0.pow(decimals).toLong()
    val scaled = (value * factor).roundToLong()
    val whole = scaled / factor
    val fraction = scaled % factor
    if (decimals == 0 || fraction == 0L) return whole.toString()
    val fractionText = fraction.toString().padStart(decimals, '0').trimEnd('0')
    return "$whole.$fractionText"
}
