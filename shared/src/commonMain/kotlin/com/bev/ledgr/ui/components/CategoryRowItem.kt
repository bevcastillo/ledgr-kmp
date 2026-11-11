package com.bev.ledgr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bev.ledgr.data.Category
import com.bev.ledgr.data.CategoryTotal
enum class ChevronDirection { Left, Right, Up, Down }

@Composable
fun CategoryRowItem(
    item: CategoryTotal,
    onClick: () -> Unit, showDivider: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconCircle(
                icon = item.category.icon,
                color = item.category.color
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
                            text = item.category.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = transactionLabel(item.transactionCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
            }
        }
        if (showDivider) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun Chevron(
    direction: ChevronDirection,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val inset = strokeWidth.toPx() / 2
        val right = size.width - inset
        val bottom = size.height - inset
        val midX = size.width / 2
        val midY = size.height / 2

        val points = when (direction) {
            ChevronDirection.Left -> listOf(
                Offset(right, inset),
                Offset(inset, midY),
                Offset(right, bottom)
            )
            ChevronDirection.Right -> listOf(
                Offset(inset, inset),
                Offset(right, midY),
                Offset(inset, bottom)
            )
            ChevronDirection.Up -> listOf(
                Offset(inset, bottom),
                Offset(midX, inset),
                Offset(right, bottom)
            )
            ChevronDirection.Down -> listOf(
                Offset(inset, inset),
                Offset(midX, bottom),
                Offset(right, inset)
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

private fun transactionLabel(count: Int): String =
    if (count == 1) "1 Transaction" else "$count Transactions"
