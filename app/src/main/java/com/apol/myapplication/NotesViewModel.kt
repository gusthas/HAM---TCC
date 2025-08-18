package com.apol.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.data.model.Bloco
import com.apol.myapplication.data.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val notesDao = db.notesDao()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _blocos = MutableStateFlow<List<Bloco>>(emptyList())
    val blocos: StateFlow<List<Bloco>> = _blocos.asStateFlow()

    private var currentUserEmail: String? = null

    fun setCurrentUser(email: String) {
        if (currentUserEmail == email) return // Evita recarregar desnecessariamente
        currentUserEmail = email

        // Inicia a "escuta" do banco de dados
        viewModelScope.launch {
            notesDao.getNotesByUser(email).collect { notesFromDb ->
                _notes.value = notesFromDb
            }
        }
        viewModelScope.launch {
            notesDao.getBlocosByUser(email).collect { blocosFromDb ->
                _blocos.value = blocosFromDb
            }
        }
    }

    private fun loadNotes() {
        currentUserEmail?.let { email ->
            viewModelScope.launch {
                // Apenas busca os dados mais recentes do banco
                _notes.value = notesDao.getNotesByUserNow(email)
            }
        }
    }

    private fun loadBlocos() {
        currentUserEmail?.let { email ->
            viewModelScope.launch {
                // Apenas busca os dados mais recentes do banco
                _blocos.value = notesDao.getBlocosByUserNow(email)
            }
        }
    }

    // --- FUNÇÕES DE MODIFICAÇÃO CORRIGIDAS ---
    // Agora, após cada modificação, chamamos a função de recarregar para forçar a atualização.

    fun addNote(text: String) {
        currentUserEmail?.let { email ->
            viewModelScope.launch {
                notesDao.insertNote(Note(userOwnerEmail = email, text = text))
            }
        }
    }

    fun adicionarBloco(bloco: Bloco) {
        currentUserEmail?.let { email ->
            viewModelScope.launch {
                notesDao.insertBloco(bloco.copy(userOwnerEmail = email))
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            notesDao.updateNote(note)
            loadNotes() // Força a atualização
        }
    }

    fun updateBloco(bloco: Bloco) {
        viewModelScope.launch {
            notesDao.updateBloco(bloco)
            loadBlocos() // Força a atualização
        }
    }

    fun deleteNotes(notesToDelete: List<Note>) {
        viewModelScope.launch {
            notesDao.deleteNotes(notesToDelete)
            loadNotes() // Força a atualização
        }
    }

    fun deleteBlocos(blocosToDelete: List<Bloco>) {
        viewModelScope.launch {
            notesDao.deleteBlocos(blocosToDelete)
            loadBlocos() // Força a atualização
        }
    }
}