
package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_history")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userEmail: String,
    val weight: Float,
    val date: Long = System.currentTimeMillis()
)