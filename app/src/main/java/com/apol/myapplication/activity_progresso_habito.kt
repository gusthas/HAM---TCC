package com.apol.myapplication

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class activity_progresso_habito : AppCompatActivity() {

    private lateinit var tvDiasSeguidos: TextView
    private lateinit var simpleLineChart: SimpleLineChart
    private lateinit var tvConstanciaGeral: TextView
    private lateinit var tvConstanciaLabel: TextView

    private lateinit var chipGroupFilters: ChipGroup

    private val PREFS_NAME = "habitos_prefs"
    private lateinit var habitName: String

    private val DIAS_SEMANA = 7
    private val DIAS_MES = 30
    private val DIAS_ANO = 365

    private val scheduleCache = mutableMapOf<String, Set<String>>()
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_progresso_habito)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        habitName = intent.getStringExtra("habit_name") ?: "Hábito"
        findViewById<TextView>(R.id.title_progresso).text = habitName

        // Referências da nova UI
        tvDiasSeguidos = findViewById(R.id.tv_dias_seguidos)
        simpleLineChart = findViewById(R.id.simpleLineChart)
        tvConstanciaGeral = findViewById(R.id.tv_constancia_geral)
        tvConstanciaLabel = findViewById(R.id.tv_constancia_label)
        chipGroupFilters = findViewById(R.id.chip_group_filters)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        atualizarDiasSeguidos()

        // Nova lógica para os filtros (ChipGroup)
        chipGroupFilters.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip_semana -> atualizarGraficoEConstancia(DIAS_SEMANA, "Semana")
                R.id.chip_mes -> atualizarGraficoEConstancia(DIAS_MES, "Mês")
                R.id.chip_ano -> atualizarGraficoEConstancia(DIAS_ANO, "Ano")
            }
        }

        // Carga inicial
        atualizarGraficoEConstancia(DIAS_MES, "Mês")
    }

    private fun atualizarGraficoEConstancia(diasParaTras: Int, periodoLabel: String) {
        val (dados, legendas) = carregarDadosDoHabito(habitName, diasParaTras)
        simpleLineChart.setData(dados, legendas)

        val constancia = calcularConstancia(habitName, diasParaTras)
        tvConstanciaGeral.text = "$constancia%"
        tvConstanciaLabel.text = "Constância ($periodoLabel)"
    }

    private fun getScheduleForDate(habitName: String, targetDate: Calendar): Set<String> {
        val targetDateString = dateFormat.format(targetDate.time)
        if (scheduleCache.containsKey(targetDateString)) {
            return scheduleCache[targetDateString]!!
        }

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val allKeys = prefs.all.keys
        val allDays = setOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

        val scheduleKeys = allKeys
            .filter { it.startsWith("${habitName}_scheduled_days") }
            .mapNotNull { key ->
                val dateStr = key.substringAfterLast('_')
                try {
                    dateFormat.parse(dateStr)
                    dateStr to key
                } catch (e: Exception) {
                    null
                }
            }
            .sortedBy { it.first }

        var correctKey: String? = null
        for ((dateStr, key) in scheduleKeys) {
            if (dateStr <= targetDateString) {
                correctKey = key
            } else {
                break
            }
        }

        val result = if (correctKey != null) {
            prefs.getStringSet(correctKey, allDays) ?: allDays
        } else {
            prefs.getStringSet("${habitName}_scheduled_days", allDays) ?: allDays
        }

        scheduleCache[targetDateString] = result
        return result
    }

    private fun calcularConstancia(habitName: String, diasParaTras: Int): Int {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        var diasFeitos = 0
        var diasProgramadosConsiderados = 0
        val hoje = Calendar.getInstance()
        scheduleCache.clear()

        for (i in 0 until diasParaTras) {
            val dia = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val scheduledDays = getScheduleForDate(habitName, dia)
            val diaDaSemana = getDayOfWeekString(dia)

            val hojeSemHoras = (Calendar.getInstance().clone() as Calendar).apply{ clear(Calendar.HOUR); clear(Calendar.MINUTE); clear(Calendar.SECOND) }
            val diaSemHoras = (dia.clone() as Calendar).apply{ clear(Calendar.HOUR); clear(Calendar.MINUTE); clear(Calendar.SECOND) }

            if (!diaSemHoras.after(hojeSemHoras) && scheduledDays.contains(diaDaSemana)) {
                diasProgramadosConsiderados++
                val dataFormatada = sdf.format(dia.time)
                val chave = "${habitName}_$dataFormatada"
                if (prefs.getInt(chave, 0) > 0) {
                    diasFeitos++
                }
            }
        }
        if (diasProgramadosConsiderados == 0) return 100
        return (diasFeitos * 100) / diasProgramadosConsiderados
    }

    private fun carregarDadosDoHabito(habitName: String, diasParaTras: Int): Pair<List<Int>, List<String>> {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val labelFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val hoje = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }
        scheduleCache.clear()

        val pontos = mutableListOf<Int>()
        val legendas = mutableListOf<String>()
        var saldo = 0

        for (i in (diasParaTras - 1) downTo 0) {
            val diaDoLoop = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val scheduledDays = getScheduleForDate(habitName, diaDoLoop)
            val diaSemHoras = (diaDoLoop.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }
            val diaDaSemana = getDayOfWeekString(diaDoLoop)

            if (scheduledDays.contains(diaDaSemana)) {
                val dataFormatada = sdf.format(diaDoLoop.time)
                val chave = "${habitName}_$dataFormatada"
                if (prefs.getInt(chave, 0) > 0) {
                    saldo++
                } else if (diaSemHoras.before(hoje)) {
                    saldo--
                }
            }

            saldo = saldo.coerceAtLeast(0)
            pontos.add(saldo)
            legendas.add(labelFormat.format(diaDoLoop.time))
        }
        return Pair(pontos, legendas)
    }

    private fun getDayOfWeekString(calendar: Calendar): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "SUN"
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            else -> ""
        }
    }

    private fun calcularSequenciaAtual(habitName: String): Int {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("${habitName}_streak", 0)
    }

    private fun atualizarDiasSeguidos() {
        val diasAtuaisSeguidos = calcularSequenciaAtual(habitName)
        tvDiasSeguidos.text = "$diasAtuaisSeguidos dias"
    }
}