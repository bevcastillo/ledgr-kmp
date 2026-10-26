package com.bev.ledgr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bev.ledgr.data.Expense
import com.bev.ledgr.data.ExpenseRepository
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val Categories = listOf(
    "Food",
    "Transport",
    "Bills",
    "Shopping",
    "Health",
    "Personal",
    "Subscriptions",
    "Credit card",
    "Loans",
    "Others"
)

data class AddExpenseUiState(
    val title: String = "",
    val amount: String = "",
    val category: String = Categories.first(),
    val note: String = "",
    val titleError: String? = null,
    val amountError: String? = null,
    val isSaved: Boolean = false,
    val isEditing: Boolean = false
)

class AddExpenseViewModel(private val expenseId: Long?, private val repository: ExpenseRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private var loadedExpense: Expense? = null

    init {
        if (expenseId != null) {
            viewModelScope.launch {
                val expense = repository.getExpense(expenseId) ?: return@launch
                loadedExpense = expense
                _uiState.update {
                    it.copy(
                        title = expense.title,
                        amount = centsToInput(expense.amountCents),
                        category = expense.category,
                        note = expense.note.orEmpty(),
                        isEditing = true
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, titleError = null) }

    fun onAmountChange(value: String) =
        _uiState.update { it.copy(amount = value, amountError = null) }

    fun onCategoryChange(value: String) = _uiState.update { it.copy(category = value) }

    fun onNoteChange(value: String) = _uiState.update { it.copy(note = value) }

    fun onSave() {
        val state = _uiState.value
        val cents = parseAmountToCents(state.amount)

        val titleError = if (state.title.isBlank()) "Enter a title" else null
        val amountError = when {
            cents == null -> "Enter a valid amount, like 150 or 150.75"
            cents == 0L -> "Amount must be more than zero"
            else -> null
        }

        if (titleError != null || amountError != null) {
            _uiState.update { it.copy(titleError = titleError, amountError = amountError) }
            return
        }

        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    id = loadedExpense?.id ?: 0,
                    title = state.title.trim(),
                    amountCents = cents!!,
                    category = state.category,
                    dateMillis = loadedExpense?.dateMillis
                        ?: Clock.System.now().toEpochMilliseconds(),
                    note = state.note.trim().ifBlank { null }
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun onDelete() {
        val expense = loadedExpense ?: return
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}

fun parseAmountToCents(input: String): Long? {
    val trimmed = input.trim()
    if (!Regex("""^\d+(\.\d{1,2})?$""").matches(trimmed)) return null
    val parts = trimmed.split(".")
    val whole = parts[0].toLongOrNull() ?: return null
    val fraction = parts.getOrNull(1)?.padEnd(2, '0')?.toLong() ?: 0L
    return whole * 100 + fraction
}

// 15075 -> "150.75", for pre-filling the amount field
fun centsToInput(cents: Long): String =
    "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
