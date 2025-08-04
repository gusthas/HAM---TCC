
package com.apol.myapplication

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.apol.myapplication.data.model.DivisaoTreino
import com.apol.myapplication.data.model.Exercicio
import com.apol.myapplication.data.model.TreinoEntity

@Dao
interface TreinoDao {

    // --- Funções para Treinos ---
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


    // --- Funções para Divisões de Treino ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDivisao(divisao: DivisaoTreino)

    @Query("SELECT * FROM divisoes_treino WHERE treinoId = :treinoId ORDER BY ordem ASC")
    suspend fun getDivisoesByTreinoId(treinoId: Long): List<DivisaoTreino>

    @Update
    suspend fun updateDivisao(divisao: DivisaoTreino)


    // --- Funções para Exercícios ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercicio(exercicio: Exercicio)

    @Query("SELECT * FROM exercicios WHERE divisaoId = :divisaoId")
    suspend fun getExerciciosByDivisaoId(divisaoId: Long): List<Exercicio>

    @Upsert // "Upsert" = Update or Insert. Perfeito para salvar.
    suspend fun upsertExercicios(exercicios: List<Exercicio>)

    @Delete
    suspend fun deleteExercicio(exercicio: Exercicio)

    @Delete
    suspend fun deleteDivisoes(divisoes: List<DivisaoTreino>)

}