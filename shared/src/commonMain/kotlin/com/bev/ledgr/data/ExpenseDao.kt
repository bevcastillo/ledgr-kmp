package com.bev.ledgr.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Upsert
    suspend fun upsert(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun observeAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    @Query(
        """
            SELECT category, SUM(amountCents) AS totalCents
            FROM expenses
            WHERE dateMillis >= :startMillis AND dateMillis < :endMillis
            GROUP by category
            ORDER by totalCents DESC
        """
    )
    fun observeCategoryTotals(startMillis: Long, endMillis: Long): Flow<List<CategoryTotal>>
}

data class CategoryTotal(
    val category: String,
    val totalCents: Long
)