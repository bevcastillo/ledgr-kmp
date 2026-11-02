package com.bev.ledgr.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTransactionRepository(initial: List<Transaction> = emptyList()) : TransactionRepository {
    private val transactions = MutableStateFlow(initial)
    val current: List<Transaction> get() = transactions.value
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    override fun observeExpenses(): Flow<List<Transaction>> = transactions

    override suspend fun addExpense(transaction: Transaction) {
        transactions.update { list ->
            if (transaction.id != 0L && list.any { it.id == transaction.id }) {
                list.map { if (it.id == transaction.id) transaction else it }
            } else {
                list + transaction.copy(id = nextId++)
            }
        }
    }

    override suspend fun deleteExpense(transaction: Transaction) {
        transactions.update { list -> list.filterNot { it.id == transaction.id } }
    }

    override suspend fun getExpense(id: Long): Transaction? = transactions.value.find { it.id == id }

    override fun observeCategoryTotals(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<CategoryTotal>> = transactions.map { list ->
        list.asSequence()
            .filter { it.type == TransactionType.EXPENSE }
            .filter { it.dateMillis in startMillis..<endMillis }
            .groupBy { Category.fromStored(it.category) }
            .map { (category, items) ->
                CategoryTotal(
                    category = category,
                    totalCents = items.sumOf { it.amountCents },
                    transactionCount = items.size
                )
            }
            .sortedByDescending { it.totalCents }
    }

    override fun observeTransactions(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<Transaction>> = transactions.map { list ->
        list.filter { it.dateMillis in startMillis..<endMillis }
    }

    override fun observeEarliestExpenseMillis(): Flow<Long?> = transactions
        .map { list -> list.minOfOrNull { it.dateMillis } }
        .distinctUntilChanged()
}
