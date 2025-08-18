// Substitua o conteúdo COMPLETO do seu arquivo WorkoutTemplateRepository.kt
package com.apol.myapplication

import com.apol.myapplication.data.model.DivisaoTreino
import com.apol.myapplication.data.model.TipoDivisao
import com.apol.myapplication.data.model.TipoTreino
import com.apol.myapplication.data.model.TreinoEntity
import com.apol.myapplication.data.model.TreinoNota

// Estruturas para organizar os treinos pré-definidos
data class PredefinedNota(val titulo: String, val conteudo: String)
data class PredefinedDivision(val nome: String, val notas: List<PredefinedNota>)
data class PredefinedWorkout(
    val nome: String,
    val tipoTreino: TipoTreino,
    val tipoDivisao: TipoDivisao,
    val iconeResId: Int,
    val divisions: List<PredefinedDivision>
)

// Nossa "Biblioteca" de treinos
object WorkoutTemplateRepository {

    val pularCordaIniciante = PredefinedWorkout(
        nome = "Iniciante: Pular Corda",
        tipoTreino = TipoTreino.GENERICO,
        tipoDivisao = TipoDivisao.LETRAS,
        iconeResId = R.drawable.ic_treinos,
        divisions = listOf(
            PredefinedDivision(
                nome = "Treino A - Foco em Resistência",
                notas = listOf(
                    PredefinedNota("Aquecimento", "5 minutos de caminhada leve e polichinelos."),
                    PredefinedNota("Pular Corda", "Pule continuamente por 1 minuto, depois descanse por 1 minuto. Repita 5 vezes."),
                    PredefinedNota("Dica de Postura", "Mantenha as costas retas e olhe para frente. Use os pulsos para girar a corda, não os braços."),
                    PredefinedNota("Vídeo de Ajuda", "Assista a este vídeo para aprender a técnica correta: https://www.youtube.com/watch?v=1BZM2Vre5oc")
                )
            )
        )
    )

    val calisteniaCasa = PredefinedWorkout(
        nome = "Calistenia em Casa",
        tipoTreino = TipoTreino.GENERICO,
        tipoDivisao = TipoDivisao.DIAS_DA_SEMANA,
        iconeResId = R.drawable.ic_treinos,
        divisions = listOf(
            PredefinedDivision("Segunda-feira: Superior", listOf(
                PredefinedNota("Flexões", "3 séries até a falha (faça quantas conseguir). Descanse 1 minuto entre as séries."),
                PredefinedNota("Dica", "Mantenha o abdômen contraído durante todo o movimento para proteger a lombar.")
            )),
            PredefinedDivision("Quarta-feira: Inferior", listOf(
                PredefinedNota("Agachamentos", "3 séries de 15 repetições. Descanse 1 minuto."),
                PredefinedNota("Afundo", "3 séries de 12 repetições para cada perna.")
            ))
        )
    )

    // --- NOVO TREINO ADICIONADO ---
    val corpoInteiroCasaIniciante = PredefinedWorkout(
        nome = "Corpo Inteiro em Casa (Iniciante)",
        tipoTreino = TipoTreino.GENERICO,
        tipoDivisao = TipoDivisao.LETRAS,
        iconeResId = R.drawable.ic_treinos,
        divisions = listOf(
            PredefinedDivision(
                nome = "Treino A",
                notas = listOf(
                    PredefinedNota("Aquecimento", "5 minutos de polichinelos e corrida no lugar."),
                    PredefinedNota("Agachamento sem peso", "3 séries de 12 repetições. Foque na postura correta."),
                    PredefinedNota("Flexão com joelhos no chão", "3 séries de 8 repetições. Se for fácil, tente a flexão normal."),
                    PredefinedNota("Prancha abdominal", "3 séries segurando a posição por 30 segundos."),
                    PredefinedNota("Dica Importante", "Descanse 1 minuto entre cada série. Beba água!")
                )
            )
        )
    )
}