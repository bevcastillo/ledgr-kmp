package com.bev.ledgr.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeExpenseRepository(initial: List<Transaction> = emptyList()) : ExpenseRepository {
    private val expenses = MutableStateFlow(initial)
    val current: List<Transaction> get() = expenses.value
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    override fun observeExpenses(): Flow<List<Transaction>> = expenses

    override suspend fun addExpense(transaction: Transaction) {
        expenses.update { list ->
            if (transaction.id != 0L && list.any { it.id == transaction.id }) {
                list.map { if (it.id == transaction.id) transaction else it }
            } else {
                list + transaction.copy(id = nextId++)
            }
        }
    }

    override suspend fun deleteExpense(transaction: Transaction) {
        expenses.update { list -> list.filterNot { it.id == transaction.id } }
    }

    override suspend fun getExpense(id: Long): Transaction? = expenses.value.find { it.id == id }

    override fun observeCategoryTotals(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<CategoryTotal>> = expenses.map { list ->
        list.filter { it.dateMillis in startMillis..<endMillis }
            .groupBy { it.category }
            .map { (category, items) ->
                CategoryTotal(category, items.sumOf { it.amountCents })
            }
            .sortedByDescending { it.totalCents }
    }
}
