package com.bev.ledgr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bev.ledgr.data.Category
import com.bev.ledgr.data.Transaction
import com.bev.ledgr.data.TransactionType
import com.bev.ledgr.ui.formatCents
import com.bev.ledgr.ui.formatShortDate
import com.bev.ledgr.ui.theme.primaryColor

private const val TINT_ALPHA = 0.15f

private data class RowCategoryStyle(val label: String, val icon: ImageVector, val color: Color)

private fun styleFor(transaction: Transaction): RowCategoryStyle =
    if (transaction.type == TransactionType.EXPENSE) {
        val category = Category.fromStored(transaction.category)
        RowCategoryStyle(category.label, category.icon, category.color)
    } else {
        RowCategoryStyle("Income", Icons.Rounded.AccountBalance, primaryColor)
    }

@Composable
fun TransactionsRow(transaction: Transaction, onClick: () -> Unit, showDivider: Boolean = true) {
    val style = styleFor(transaction)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconCircle(icon = style.icon, color = style.color)

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                CategoryPill(label = style.label, color = style.color)
                Spacer(Modifier.height(6.dp))

                Text(
                    text = formatShortDate(transaction.dateMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = formatCents(transaction.amountCents),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
private fun CategoryIconCircle(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(color.copy(alpha = TINT_ALPHA), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun CategoryPill(label: String, color: Color) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = TINT_ALPHA), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}
