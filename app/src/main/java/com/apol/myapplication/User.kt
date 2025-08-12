package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val password: String,
    val userId: String,
    val nome: String,
    val idade: Int,
    val peso: Int,
    val altura: Float,
    val genero: String,
    val perguntaSecreta: String = "",
    val respostaSecreta: String = "",

    // ▼▼▼ CAMPO ADICIONADO PARA A FOTO DE PERFIL ▼▼▼
    val profilePicUri: String? = null
)