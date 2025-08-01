package com.apol.myapplication

import kotlinx.serialization.Serializable

@Serializable
enum class TipoLembrete {
    NENHUM,
    DIARIO,
    MENSAL,
    SEGUNDOS_TESTE // Para depuração rápida
}