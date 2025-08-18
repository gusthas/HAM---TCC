package com.apol.myapplication.data.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    // --- Funções para Notes ---
    @Insert
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNotes(notes: List<Note>)

    @Query("SELECT * FROM notes WHERE userOwnerEmail = :email ORDER BY lastModified DESC")
    fun getNotesByUser(email: String): Flow<List<Note>>


    // --- Funções para Blocos ---
    @Insert
    suspend fun insertBloco(bloco: Bloco)

    @Update
    suspend fun updateBloco(bloco: Bloco)

    @Delete
    suspend fun deleteBlocos(blocos: List<Bloco>)

    @Query("SELECT * FROM blocos WHERE userOwnerEmail = :email")
    fun getBlocosByUser(email: String): Flow<List<Bloco>>
}