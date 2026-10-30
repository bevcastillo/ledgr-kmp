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
            SELECT category, SUM(amountCents) AS totalCents
            FROM transactions
            WHERE dateMillis >= :startMillis AND dateMillis < :endMillis
            GROUP by category
            ORDER by totalCents DESC
        """
    )
    fun observeCategoryTotals(startMillis: Long, endMillis: Long): Flow<List<CategoryTotal>>
}

data class CategoryTotal(val category: String, val totalCents: Long)
