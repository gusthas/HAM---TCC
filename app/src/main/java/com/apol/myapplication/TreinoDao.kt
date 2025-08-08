
package com.apol.myapplication

import androidx.room.*
import com.apol.myapplication.data.model.DivisaoTreino
import com.apol.myapplication.data.model.TreinoEntity
import com.apol.myapplication.data.model.TreinoNota

@Dao
interface TreinoDao {

    // --- Treinos ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreino(treino: TreinoEntity): Long
    @Query("SELECT * FROM treinos")
    suspend fun getAllTreinos(): List<TreinoEntity>
    @Query("SELECT * FROM treinos WHERE id = :treinoId")
    suspend fun getTreinoById(treinoId: Long): TreinoEntity?
    @Update
    suspend fun updateTreino(treino: TreinoEntity)
    @Query("DELETE FROM treinos WHERE id IN (:treinoIds)")
    suspend fun deleteTreinosByIds(treinoIds: List<Long>)

    // --- Divisões ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDivisao(divisao: DivisaoTreino)
    @Query("SELECT * FROM divisoes_treino WHERE treinoId = :treinoId ORDER BY ordem ASC")
    suspend fun getDivisoesByTreinoId(treinoId: Long): List<DivisaoTreino>
    @Update
    suspend fun updateDivisao(divisao: DivisaoTreino)
    @Delete
    suspend fun deleteDivisoes(divisoes: List<DivisaoTreino>)

    // --- Notas de Treino (a nova lógica) ---
    @Insert
    suspend fun insertTreinoNota(nota: TreinoNota)
    @Update
    suspend fun updateTreinoNota(nota: TreinoNota)
    @Delete
    suspend fun deleteTreinoNota(nota: TreinoNota)
    @Query("SELECT * FROM treino_notas WHERE divisaoId = :divisaoId")
    suspend fun getNotasByDivisaoId(divisaoId: Long): List<TreinoNota>
    @Delete
    suspend fun deleteTreinoNotas(notas: List<TreinoNota>)
}