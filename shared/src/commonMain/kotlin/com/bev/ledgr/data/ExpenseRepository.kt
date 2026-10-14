package com.bev.ledgr.data

import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(): Flow<List<Expense>>
    suspend fun addExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
}

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao
): ExpenseRepository {
    override fun observeExpenses(): Flow<List<Expense>> = dao.observeAll()
    override suspend fun addExpense(expense: Expense) = dao.upsert(expense)
    override suspend fun deleteExpense(expense: Expense) = dao.delete(expense)
}