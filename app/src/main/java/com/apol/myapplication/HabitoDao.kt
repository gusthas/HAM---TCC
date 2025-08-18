package com.apol.myapplication.data.model

import androidx.room.*

@Dao
interface HabitoDao {

    // --- Operações para Hábitos ---
    @Query("SELECT * FROM habitos WHERE userOwnerEmail = :email")
    suspend fun getHabitosByUser(email: String): List<Habito>

    @Insert
    suspend fun insertHabito(habito: Habito)

    @Update
    suspend fun updateHabito(habito: Habito)

    // Função necessária para o modo de exclusão
    @Query("DELETE FROM habitos WHERE id IN (:ids)")
    suspend fun deleteHabitosByIds(ids: List<Long>)

    // --- Operações para Progresso dos Hábitos ---
    @Query("SELECT * FROM habito_progresso WHERE habitoId = :habitoId")
    suspend fun getProgressoForHabito(habitoId: Long): List<HabitoProgresso>

    @Insert
    suspend fun insertProgresso(progresso: HabitoProgresso)

    @Query("DELETE FROM habito_progresso WHERE habitoId = :habitoId AND data = :data")
    suspend fun deleteProgresso(habitoId: Long, data: String)
}