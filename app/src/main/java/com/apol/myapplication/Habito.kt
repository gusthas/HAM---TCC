package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habitos")
data class Habito(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userOwnerEmail: String,
    val nome: String,
    val diasProgramados: String,
    var isFavorito: Boolean = false,
    val isGoodHabit: Boolean // <-- O campo que faltava
)