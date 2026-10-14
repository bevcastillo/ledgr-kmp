package com.bev.ledgr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amountCents: Long,
    val category: String,
    val dateMillis: Long,
    val note: String? = null
)