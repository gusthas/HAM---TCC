// Crie um novo arquivo: Exercicio.kt
package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercicios",
    foreignKeys = [ForeignKey(
        entity = DivisaoTreino::class,
        parentColumns = ["id"],
        childColumns = ["divisaoId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Exercicio(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val divisaoId: Long, // Chave estrangeira para ligar à DivisaoTreino
    var nome: String,
    var carga: String, // Usando String para flexibilidade (ex: "40kg", "10/12/15 reps")
    var series: String,
    var repeticoes: String
)