package com.bev.ledgr.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Upsert
    suspend fun upsert(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun observeAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Query(
        """
            SELECT category, SUM(amountCents) AS totalCents, COUNT(*) AS transactionCount
            FROM transactions
            WHERE type = :type
              AND category IS NOT NULL
              AND dateMillis >= :startMillis AND dateMillis < :endMillis
            GROUP BY category
        """
    )
    fun observeCategoryTotals(
        startMillis: Long,
        endMillis: Long,
        type: TransactionType
    ): Flow<List<CategoryTotalRow>>

    @Query("SELECT MIN(dateMillis) FROM transactions WHERE type = :type")
    fun observeEarliestDateMillis(type: TransactionType): Flow<Long?>
}

data class CategoryTotalRow(val category: String, val totalCents: Long, val transactionCount: Int)

data class CategoryTotal(val category: Category, val totalCents: Long, val transactionCount: Int)
