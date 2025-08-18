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

    // Esta função define o usuário e dispara o carregamento inicial
    fun setCurrentUser(email: String) {
        currentUserEmail = email
        loadNotes()
        loadBlocos()
    }

    // --- FUNÇÕES CORRIGIDAS ---
    private fun loadNotes() {
        currentUserEmail?.let { email ->
            viewModelScope.launch {
                // "Escuta" o fluxo de dados e atualiza a lista sempre que houver mudanças
                notesDao.getNotesByUser(email).collect { notesFromDb ->
                    _notes.value = notesFromDb
                }
            }
        }
    }

    private fun loadBlocos() {
        currentUserEmail?.let { email ->
            viewModelScope.launch {
                // "Escuta" o fluxo de dados e atualiza a lista sempre que houver mudanças
                notesDao.getBlocosByUser(email).collect { blocosFromDb ->
                    _blocos.value = blocosFromDb
                }
            }
        }
    }

    // As funções abaixo agora não precisam mais chamar 'loadNotes' ou 'loadBlocos',
    // pois o Flow já faz a atualização automática.
    fun addNote(text: String) {
        currentUserEmail?.let { email ->
            viewModelScope.launch {
                val newNote = Note(userOwnerEmail = email, text = text)
                notesDao.insertNote(newNote)
            }
        }
    }

    fun adicionarBloco(bloco: Bloco) {
        currentUserEmail?.let { email ->
            val blocoComDono = bloco.copy(userOwnerEmail = email)
            viewModelScope.launch {
                notesDao.insertBloco(blocoComDono)
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            notesDao.updateNote(note)
        }
    }

    fun updateBloco(bloco: Bloco) {
        viewModelScope.launch {
            notesDao.updateBloco(bloco)
        }
    }

    fun deleteNotes(notesToDelete: List<Note>) {
        viewModelScope.launch {
            notesDao.deleteNotes(notesToDelete)
        }
    }

    fun deleteBlocos(blocosToDelete: List<Bloco>) {
        viewModelScope.launch {
            notesDao.deleteBlocos(blocosToDelete)
        }
    }
}