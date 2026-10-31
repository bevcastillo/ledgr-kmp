package com.bev.ledgr.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TransactionRepository {
    fun observeExpenses(): Flow<List<Transaction>>
    suspend fun addExpense(transaction: Transaction)
    suspend fun deleteExpense(transaction: Transaction)
    suspend fun getExpense(id: Long): Transaction?
    fun observeCategoryTotals(startMillis: Long, endMillis: Long): Flow<List<CategoryTotal>>
    fun observeEarliestExpenseMillis(): Flow<Long?>
}

class TransactionRepositoryImpl(private val dao: TransactionDao) : TransactionRepository {
    override fun observeExpenses(): Flow<List<Transaction>> = dao.observeAll()
    override suspend fun addExpense(transaction: Transaction) = dao.upsert(transaction)
    override suspend fun deleteExpense(transaction: Transaction) = dao.delete(transaction)
    override suspend fun getExpense(id: Long): Transaction? = dao.getById(id)
    override fun observeCategoryTotals(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<CategoryTotal>> =
        dao.observeCategoryTotals(startMillis, endMillis, TransactionType.EXPENSE)
            .map { rows ->
                rows.groupBy { Category.fromStored(it.category) }
                    .map { (category, group) ->
                        CategoryTotal(
                            category = category,
                            totalCents = group.sumOf { it.totalCents },
                            transactionCount = group.sumOf { it.transactionCount }
                        )
                    }
                    .sortedByDescending { it.totalCents }
            }

    override fun observeEarliestExpenseMillis(): Flow<Long?> =
        dao.observeEarliestDateMillis(TransactionType.EXPENSE)
}
