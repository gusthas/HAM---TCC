package com.apol.myapplication.data.model

// Modelo usado SOMENTE para exibir dados na tela e controlar a seleção.
data class HabitUI(
    val id: String,
    val name: String,
    val streakDays: Int,
    val message: String,
    val count: Int,
    val isFavorited: Boolean,
    var isSelected: Boolean = false // Essencial para o modo de exclusão
)