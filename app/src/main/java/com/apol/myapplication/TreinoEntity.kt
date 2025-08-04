
package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

// O enum permanece o mesmo
enum class TipoDivisao {
    NAO_DEFINIDO,
    DIAS_DA_SEMANA,
    LETRAS
}

@Entity(tableName = "treinos")
data class TreinoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val iconeResId: Int,
    val tipoDivisao: TipoDivisao = TipoDivisao.NAO_DEFINIDO,
    val detalhes: String = "Toque para adicionar detalhes"
) {

    @Ignore // <-- Diz ao Room para IGNORAR este campo no banco de dados.
    var isSelected: Boolean = false
}