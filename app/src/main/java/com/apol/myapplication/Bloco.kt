// Garanta que o conteúdo de data/model/Bloco.kt seja este:
package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "blocos")
@Serializable
data class Bloco(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val userOwnerEmail: String,
    var nome: String,
    var subtitulo: String = "",
    var anotacao: String = "",
    var mensagemNotificacao: String = "",
    var tipoLembrete: TipoLembrete = TipoLembrete.NENHUM,
    var diasLembrete: List<Int> = emptyList(),
    var horariosLembrete: List<String> = emptyList(),
    var segundosLembrete: Long? = null,
    var isSelected: Boolean = false
)