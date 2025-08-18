package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habitos")
data class Habito(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val userOwnerEmail: String,
    var nome: String,
    var isFavorito: Boolean = false,
    val isGoodHabit: Boolean
)