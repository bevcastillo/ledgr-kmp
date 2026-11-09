package com.bev.ledgr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bev.ledgr.data.Category
import com.bev.ledgr.data.Transaction
import com.bev.ledgr.data.TransactionRepository
import com.bev.ledgr.data.TransactionType
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddTransactionUiState(
    val title: String = "",
    val amount: String = "",
    val category: Category = Category.FOOD,
    val note: String = "",
    val dateMillis: Long = Clock.System.now().toEpochMilliseconds(),
    val titleError: String? = null,
    val amountError: String? = null,
    val isSaved: Boolean = false,
    val isEditing: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE
)

class AddTransactionViewModel(
    private val transactionId: Long?,
    private val repository: TransactionRepository,
    type: TransactionType?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddTransactionUiState(type = type ?: TransactionType.EXPENSE)
    )
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private var loadedTransactions: Transaction? = null

    init {
        if (transactionId != null) {
            viewModelScope.launch {
                val transaction = repository.getTransactionById(transactionId) ?: return@launch
                loadedTransactions = transaction
                _uiState.update {
                    it.copy(
                        title = transaction.title,
                        amount = centsToInput(transaction.amountCents),
                        category = transaction.category ?: Category.FOOD,
                        note = transaction.note.orEmpty(),
                        dateMillis = transaction.dateMillis,
                        isEditing = true,
                        type = transaction.type
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, titleError = null) }

    fun onAmountChange(value: String) =
        _uiState.update { it.copy(amount = value, amountError = null) }

    fun onCategoryChange(value: Category) = _uiState.update { it.copy(category = value) }

    fun onNoteChange(value: String) = _uiState.update { it.copy(note = value) }

    fun onDateChange(value: Long) = _uiState.update { it.copy(dateMillis = value) }

    fun onSave() {
        val state = _uiState.value
        val cents = parseAmountToCents(state.amount)

        val titleError = if (state.title.isBlank()) "Enter a title" else null
        val amountError = when (cents) {
            null -> "Enter a valid amount, like 150 or 150.75"
            0L -> "Amount must be more than zero"
            else -> null
        }

        val category: Category? = if (state.type ==
            TransactionType.EXPENSE
        ) {
            state.category
        } else {
            null
        }

        if (titleError != null || amountError != null) {
            _uiState.update { it.copy(titleError = titleError, amountError = amountError) }
            return
        }

        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    id = loadedTransactions?.id ?: 0,
                    title = state.title.trim(),
                    amountCents = cents!!,
                    category = category,
                    dateMillis = state.dateMillis,
                    note = state.note.trim().ifBlank { null },
                    type = state.type
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun onDelete() {
        val transactions = loadedTransactions ?: return
        viewModelScope.launch {
            repository.deleteTransaction(transactions)
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
