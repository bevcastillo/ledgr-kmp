package com.bev.ledgr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType { EXPENSE, INCOME }

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amountCents: Long,
    val category: String,
    val dateMillis: Long,
    val note: String? = null,
    val type: TransactionType
)
