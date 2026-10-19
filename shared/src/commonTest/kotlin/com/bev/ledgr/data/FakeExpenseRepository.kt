package com.bev.ledgr.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeExpenseRepository(
    initial: List<Expense> = emptyList()
) : ExpenseRepository {
    private val expenses = MutableStateFlow(initial)
    val current: List<Expense> get() = expenses.value
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    override fun observeExpenses(): Flow<List<Expense>> = expenses

    override suspend fun addExpense(expense: Expense) {
        expenses.update { list ->
            if (expense.id != 0L && list.any { it.id == expense.id }) {
                list.map { if (it.id == expense.id) expense else it }
            } else {
                list + expense.copy(id = nextId++)
            }
        }
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenses.update { list -> list.filterNot { it.id == expense.id } }
    }

    override suspend fun getExpense(id: Long): Expense? =
        expenses.value.find { it.id == id }

    override fun observeCategoryTotals(startMillis: Long, endMillis: Long): Flow<List<CategoryTotal>> =
        expenses.map { list ->
            list.filter { it.dateMillis in startMillis..<endMillis }
                .groupBy{ it.category }
                .map { (category, items) -> CategoryTotal(category, items.sumOf { it.amountCents }) }
                .sortedByDescending { it.totalCents }
        }

}