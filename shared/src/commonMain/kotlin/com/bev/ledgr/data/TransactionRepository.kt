package com.bev.ledgr.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun getTransactionById(id: Long): Transaction?
    fun observeCategoryTotals(startMillis: Long, endMillis: Long): Flow<List<CategoryTotal>>
    fun observeTransactions(startMillis: Long, endMillis: Long): Flow<List<Transaction>>
    fun observeEarliestTransactionMillis(): Flow<Long?>
}

class TransactionRepositoryImpl(private val dao: TransactionDao) : TransactionRepository {
    override fun observeTransactions(): Flow<List<Transaction>> = dao.observeAll()
    override suspend fun addTransaction(transaction: Transaction) = dao.upsert(transaction)
    override suspend fun deleteTransaction(transaction: Transaction) = dao.delete(transaction)
    override suspend fun getTransactionById(id: Long): Transaction? = dao.getById(id)
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

    override fun observeTransactions(startMillis: Long, endMillis: Long): Flow<List<Transaction>> =
        dao.observeBetween(startMillis, endMillis)

    override fun observeEarliestTransactionMillis(): Flow<Long?> = dao.observeEarliestDateMillis()
}
