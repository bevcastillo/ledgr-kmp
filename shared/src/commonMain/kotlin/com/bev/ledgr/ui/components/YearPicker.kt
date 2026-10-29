package com.bev.ledgr.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bev.ledgr.ui.theme.primaryColor
import ledgr.shared.generated.resources.Res
import ledgr.shared.generated.resources.unfold_more
import org.jetbrains.compose.resources.painterResource

@Composable
fun YearPicker(
    selectedYear: Int,
    years: List<Int>,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(onClick = { expanded = true }) {
            Text(
                text = selectedYear.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = primaryColor
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(Res.drawable.unfold_more),
                contentDescription = "Change year",
                modifier = Modifier.size(20.dp),
                tint = primaryColor
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    },
                    trailingIcon = if (year == selectedYear) {
                        { Icon(Icons.Filled.Check, contentDescription = "Selected") }
                    } else {
                        null
                    }
                )
            }
        }
    }
}
