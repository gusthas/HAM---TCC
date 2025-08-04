package com.apol.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_prefs", 0)
    private val json = Json { encodeDefaults = true }


    private val _notes = MutableStateFlow<List<Note>>(loadNotes())
    val notes: StateFlow<List<Note>> get() = _notes

    fun addNote(text: String) {
        val novaNota = Note(text = text)
        val updated = listOf(novaNota) + _notes.value
        _notes.value = updated
        saveNotes(updated)
    }

    fun deleteNotes(lista: List<Note>) {
        val updated = _notes.value.filterNot { it in lista }
        _notes.value = updated
        saveNotes(updated)
    }

    fun updateNote(note: Note) {
        val updated = _notes.value.map {
            if (it.id == note.id) note else it
        }
        _notes.value = updated
        saveNotes(updated)
    }

    private fun loadNotes(): List<Note> {
        val jsonString = prefs.getString("notes", null) ?: return emptyList()
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveNotes(list: List<Note>) {
        val jsonString = json.encodeToString(list)
        prefs.edit().putString("notes", jsonString).apply()
    }

    // --- Blocos ---
    private val _blocos = MutableStateFlow<List<Bloco>>(loadBlocos())
    val blocos: StateFlow<List<Bloco>> get() = _blocos

    fun adicionarBloco(bloco: Bloco) {
        val updated = listOf(bloco) + _blocos.value
        _blocos.value = updated
        saveBlocos(updated)
    }

    fun deleteBlocos(lista: List<Bloco>) {
        val idsToDelete = lista.map { it.id }
        val updated = _blocos.value.filterNot { it.id in idsToDelete }
        _blocos.value = updated
        saveBlocos(updated)
    }

    fun updateBloco(bloco: Bloco) {
        val updated = _blocos.value.map {
            if (it.id == bloco.id) bloco else it
        }
        _blocos.value = updated
        saveBlocos(updated)
    }

    private fun loadBlocos(): List<Bloco> {
        val jsonString = prefs.getString("blocos", null) ?: return emptyList()
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveBlocos(list: List<Bloco>) {
        val jsonString = json.encodeToString(list)
        prefs.edit().putString("blocos", jsonString).apply()
    }

    fun carregarBlocosExemplo() {
        if (_blocos.value.isEmpty()) {
            val exemplo = listOf(
                Bloco(nome = "Metas"),
                Bloco(nome = "Finanças"),
                Bloco(nome = "Estudos")
            )
            _blocos.value = exemplo
            saveBlocos(exemplo)
        }
    }
}
