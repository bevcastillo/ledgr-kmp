package com.bev.ledgr.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bev.ledgr.data.TransactionType
import com.bev.ledgr.ui.components.AmountVisualTransformation
import com.bev.ledgr.ui.components.DeleteButton
import com.bev.ledgr.ui.components.SaveButton
import com.bev.ledgr.ui.components.isValidAmountInput
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AddTransactionRoute(
    transactionId: Long?,
    type: TransactionType?,
    onDone: () -> Unit,
    viewModelKey: String? = null,
    viewModel: AddTransactionViewModel = koinViewModel(
        key = viewModelKey,
        parameters = { parametersOf(transactionId, type) }
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    AddExpenseScreen(
        state = state,
        onTitleChange = viewModel::onTitleChange,
        onAmountChange = viewModel::onAmountChange,
        onCategoryChange = viewModel::onCategoryChange,
        onNoteChange = viewModel::onNoteChange,
        onDateChange = viewModel::onDateChange,
        onSave = viewModel::onSave,
        onBack = onDone,
        onDelete = viewModel::onDelete,
        type = state.type
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    state: AddTransactionUiState,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    type: TransactionType
) {
    val action = if (state.isEditing) "Edit" else "Add"
    val noun = if (type == TransactionType.INCOME) "income" else "expense"

    var showDatePicker by remember { mutableStateOf(false) }

    // A read-only text field swallows normal clicks, so listen for presses instead.
    val dateFieldInteraction = remember { MutableInteractionSource() }
    LaunchedEffect(dateFieldInteraction) {
        dateFieldInteraction.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) showDatePicker = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "$action $noun",
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
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.amount,
                onValueChange = { input ->
                    if (isValidAmountInput(input)) onAmountChange(input)
                },
                label = { Text("Amount") },
                prefix = { Text("₱") },
                isError = state.amountError != null,
                supportingText = state.amountError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = remember { AmountVisualTransformation() }
            )
            OutlinedTextField(
                value = formatDate(state.dateMillis),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Pick a date")
                    }
                },
                interactionSource = dateFieldInteraction,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            AnimatedVisibility(
                visible = state.type == TransactionType.EXPENSE,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Categories.forEach { category ->
                        FilterChip(
                            selected = state.category == category,
                            onClick = { onCategoryChange(category) },
                            label = { Text(category) }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.weight(1f))
            SaveButton(
                onClick = onSave,
                text = "Save"
            )
            if (state.isEditing) {
                DeleteButton(
                    onClick = onDelete
                )
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dateMillis.toPickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onDateChange(it.fromPickerMillis()) }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

/*
 * The DatePicker works in UTC: it expects and returns midnight UTC of the chosen day.
 * Your saved dateMillis is a real moment in local time. These helpers convert between
 * the two, so the picker shows the right day in the Philippines (UTC+8) and anywhere else.
 */

// Saved timestamp -> the picker's "midnight UTC of the same local day"
private fun Long.toPickerMillis(): Long {
    val localDate = Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return localDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

// Picker's UTC midnight -> start of that day in the user's time zone
private fun Long.fromPickerMillis(): Long {
    val pickedDate: LocalDate = Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC).date
    return pickedDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

// e.g. "Oct 29, 2026"
private fun formatDate(millis: Long): String {
    val date = Instant.fromEpochMilliseconds(millis)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return "$month ${date.day}, ${date.year}"
}
