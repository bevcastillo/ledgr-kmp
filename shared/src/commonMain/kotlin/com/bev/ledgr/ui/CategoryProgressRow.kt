package com.bev.ledgr.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bev.ledgr.data.CategoryTotal
import com.bev.ledgr.ui.components.color
import com.bev.ledgr.ui.components.icon
import com.bev.ledgr.util.AmountFormatter
import kotlin.math.roundToInt

/**
 * One category on the Monthly summary: icon, name, share of the month's spending,
 * amount, and a bar under the row that fills to that share.
 *
 * [grandTotalCents] is the month's total spending, used to work out the share.
 */
@Composable
fun CategoryProgressRow(
    item: CategoryTotal,
    grandTotalCents: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = item.category.color

    val share = if (grandTotalCents > 0L) {
        (item.totalCents.toFloat() / grandTotalCents.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    // The bar slides to its new length when you switch months.
    val animatedShare by animateFloatAsState(
        targetValue = share,
        animationSpec = tween(durationMillis = 500),
        label = "categoryShare"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.category.icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.category.label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatShare(item.totalCents, grandTotalCents),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = AmountFormatter.formatCentavos(centavos = item.totalCents, compact = false),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }

        Spacer(Modifier.height(10.dp))

        // Grey track with the category-colored fill on top.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (animatedShare > 0f) {
                Box(
                    modifier = Modifier
                        // Keep a sliver visible for very small categories.
                        .fillMaxWidth(animatedShare.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

/**
 * "34.3%" style text with one decimal place. Done by hand because
 * String.format isn't available in commonMain.
 */
private fun formatShare(partCents: Long, totalCents: Long): String {
    if (totalCents <= 0L) return "0%"
    val tenths = (partCents * 1000.0 / totalCents).roundToInt()
    return "${tenths / 10}.${tenths % 10}%"
}
