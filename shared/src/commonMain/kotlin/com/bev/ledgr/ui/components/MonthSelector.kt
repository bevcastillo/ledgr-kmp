package com.bev.ledgr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bev.ledgr.ui.theme.primaryDark
import com.bev.ledgr.ui.theme.primaryLight

private val MonthNames =
    listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )

@Composable
fun MonthSelector(
    selectedMonth: Int,
    availableMonths: List<Int>,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val selectedIndex = availableMonths.indexOf(selectedMonth)

    // Keep selected month centered
    LaunchedEffect(selectedIndex) {
        if (selectedIndex < 0) return@LaunchedEffect
        val layoutInfo = listState.layoutInfo
        val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedIndex }
        if (item == null) {
            listState.scrollToItem(selectedIndex)
        }
        val visibleItem =
            listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedIndex }
                ?: return@LaunchedEffect
        val viewportCenter =
            (listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportEndOffset) / 2
        val itemCenter = visibleItem.offset + visibleItem.size / 2
        listState.animateScrollBy((itemCenter - viewportCenter).toFloat())
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        itemsIndexed(availableMonths) { _, month ->
            val isSelected = month == selectedMonth
            Box(
                modifier =
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) primaryLight else Color.Transparent
                    )
                    .selectable(
                        selected = isSelected,
                        onClick = { onMonthSelected(month) },
                        role = Role.Tab
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = MonthNames[month - 1],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color =
                    if (isSelected) {
                        primaryDark
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.6f
                        )
                    }
                )
            }
        }
    }
}
