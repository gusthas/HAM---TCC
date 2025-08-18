package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes") // <-- ADICIONE ESTA LINHA
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userOwnerEmail: String,
    var text: String,
    val lastModified: Long = System.currentTimeMillis(),
    var isSelected: Boolean = false
)