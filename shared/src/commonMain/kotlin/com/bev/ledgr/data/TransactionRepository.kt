package com.bev.ledgr.data

import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(): Flow<List<Transaction>>
    suspend fun addExpense(transaction: Transaction)
    suspend fun deleteExpense(transaction: Transaction)
    suspend fun getExpense(id: Long): Transaction?
    fun observeCategoryTotals(startMillis: Long, endMillis: Long): Flow<List<CategoryTotal>>
}

class ExpenseRepositoryImpl(private val dao: TransactionDao) : ExpenseRepository {
    override fun observeExpenses(): Flow<List<Transaction>> = dao.observeAll()
    override suspend fun addExpense(transaction: Transaction) = dao.upsert(transaction)
    override suspend fun deleteExpense(transaction: Transaction) = dao.delete(transaction)
    override suspend fun getExpense(id: Long): Transaction? = dao.getById(id)
    override fun observeCategoryTotals(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<CategoryTotal>> = dao.observeCategoryTotals(startMillis, endMillis)
}
