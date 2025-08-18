package com.apol.myapplication.data.model

import androidx.room.*

@Dao
interface HabitoDao {
    @Insert
    suspend fun insertHabito(habito: Habito)

    @Update
    suspend fun updateHabito(habito: Habito)

    @Delete
    suspend fun deleteHabito(habito: Habito)

    @Query("SELECT * FROM habitos WHERE userOwnerEmail = :email")
    suspend fun getHabitosByUser(email: String): List<Habito>

    @Insert
    suspend fun insertProgresso(progresso: HabitoProgresso)

    @Query("DELETE FROM habito_progresso WHERE habitoId = :habitoId AND data = :data")
    suspend fun deleteProgresso(habitoId: Long, data: String)

    @Query("SELECT * FROM habito_progresso WHERE habitoId = :habitoId")
    suspend fun getProgressoForHabito(habitoId: Long): List<HabitoProgresso>
}