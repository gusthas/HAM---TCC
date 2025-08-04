// Em Bloco.kt
package com.apol.myapplication

import kotlinx.serialization.Serializable

@Serializable
data class Bloco(
    val id: String = java.util.UUID.randomUUID().toString(),
    var nome: String,
    var subtitulo: String = "",
    var anotacao: String = "",
    var mensagemNotificacao: String = "",
    var isSelected: Boolean = false,


    var tipoLembrete: TipoLembrete = TipoLembrete.NENHUM,
    var diasLembrete: List<Int> = emptyList(),      // Para lembretes mensais (ex: [1, 15, 30])
    var horariosLembrete: List<String> = emptyList(), // Para diário e mensal (ex: ["09:00", "18:30"])
    var segundosLembrete: Long? = null                // Apenas para o modo de teste
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Bloco
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}