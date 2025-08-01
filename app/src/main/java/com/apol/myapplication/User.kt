package com.apol.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // ID auto-incrementado
    val email: String,
    val password: String,  // Senha do usuário
    val userId: String,    // UID do Firebase
    val nome: String,      // Nome do usuário
    val idade: Int,        // Idade do usuário
    val peso: Int,         // Peso do usuário
    val altura: Float,     // Altura do usuário
    val genero: String     // Gênero do usuário (Masculino ou Feminino)
)

