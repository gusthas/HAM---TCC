package com.apol.myapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.Habito
import com.apol.myapplication.data.model.HabitoProgresso
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class activity_progresso_habito : AppCompatActivity() {

    // --- MUDANÇA: Acesso ao Banco de Dados ---
    private lateinit var db: AppDatabase
    private var habitId: Long = -1L
    private var habitoAtual: Habito? = null
    private var listaDeProgresso: List<HabitoProgresso> = emptyList()

    private lateinit var tvDiasSeguidos: TextView
    private lateinit var simpleLineChart: SimpleLineChart
    private lateinit var tvConstanciaGeral: TextView
    private lateinit var tvConstanciaLabel: TextView
    private lateinit var chipGroupFilters: ChipGroup

    private val DIAS_SEMANA = 7
    private val DIAS_MES = 30
    private val DIAS_ANO = 365

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progresso_habito)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AppDatabase.getDatabase(this)
        // --- MUDANÇA: Recebendo o ID do hábito em vez do nome ---
        habitId = intent.getLongExtra("habit_id", -1L)

        if (habitId == -1L) {
            Toast.makeText(this, "Não foi possível carregar o hábito.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvDiasSeguidos = findViewById(R.id.tv_dias_seguidos)
        simpleLineChart = findViewById(R.id.simpleLineChart)
        tvConstanciaGeral = findViewById(R.id.tv_constancia_geral)
        tvConstanciaLabel = findViewById(R.id.tv_constancia_label)
        chipGroupFilters = findViewById(R.id.chip_group_filters)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        // Carrega os dados do banco e depois atualiza a tela
        carregarDadosDoBanco()

        chipGroupFilters.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chip_semana -> atualizarGraficoEConstancia(DIAS_SEMANA, "Semana")
                R.id.chip_mes -> atualizarGraficoEConstancia(DIAS_MES, "Mês")
                R.id.chip_ano -> atualizarGraficoEConstancia(DIAS_ANO, "Ano")
            }
        }
    }

    private fun carregarDadosDoBanco() {
        lifecycleScope.launch {
            habitoAtual = db.habitoDao().getHabitoById(habitId)
            listaDeProgresso = db.habitoDao().getProgressoForHabito(habitId)

            runOnUiThread {
                if (habitoAtual != null) {
                    findViewById<TextView>(R.id.title_progresso).text = habitoAtual!!.nome
                    atualizarDiasSeguidos()
                    // Define o Mês como filtro inicial
                    chipGroupFilters.check(R.id.chip_mes)
                    atualizarGraficoEConstancia(DIAS_MES, "Mês")
                } else {
                    Toast.makeText(this@activity_progresso_habito, "Hábito não encontrado.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun atualizarGraficoEConstancia(diasParaTras: Int, periodoLabel: String) {
        habitoAtual?.let { habito ->
            val (dados, legendas) = gerarDadosDoGrafico(habito, listaDeProgresso, diasParaTras)
            simpleLineChart.setData(dados, legendas)

            val constancia = calcularConstancia(habito, listaDeProgresso, diasParaTras)
            tvConstanciaGeral.text = "$constancia%"
            tvConstanciaLabel.text = "Constância ($periodoLabel)"
        }
    }

    private fun calcularConstancia(habito: Habito, progresso: List<HabitoProgresso>, diasParaTras: Int): Int {
        val datasConcluidas = progresso.map { it.data }.toSet()
        val diasProgramados = habito.diasProgramados.split(',').toSet()
        var diasFeitos = 0
        var diasProgramadosConsiderados = 0

        for (i in 0 until diasParaTras) {
            val dia = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            if (diasProgramados.contains(getDayOfWeekString(dia))) {
                diasProgramadosConsiderados++
                val dataFormatada = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(dia.time)
                if (datasConcluidas.contains(dataFormatada)) {
                    diasFeitos++
                }
            }
        }
        if (diasProgramadosConsiderados == 0) return 100
        return (diasFeitos * 100) / diasProgramadosConsiderados
    }

    private fun gerarDadosDoGrafico(habito: Habito, progresso: List<HabitoProgresso>, diasParaTras: Int): Pair<List<Int>, List<String>> {
        val datasConcluidas = progresso.map { it.data }.toSet()
        val diasProgramados = habito.diasProgramados.split(',').toSet()

        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val labelFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val hoje = Calendar.getInstance()

        val pontos = mutableListOf<Int>()
        val legendas = mutableListOf<String>()
        var saldo = 0

        for (i in (diasParaTras - 1) downTo 0) {
            val diaDoLoop = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val diaDaSemana = getDayOfWeekString(diaDoLoop)

            if (diasProgramados.contains(diaDaSemana)) {
                val dataFormatada = sdf.format(diaDoLoop.time)
                if (datasConcluidas.contains(dataFormatada)) {
                    saldo++
                } else if (diaDoLoop.before(hoje)) {
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

    private fun atualizarDiasSeguidos() {
        val sequencia = calcularSequenciaAtual(listaDeProgresso)
        tvDiasSeguidos.text = "$sequencia dias"
    }

    private fun calcularSequenciaAtual(progresso: List<HabitoProgresso>): Int {
        if (progresso.isEmpty()) return 0
        val datasConcluidas = progresso.map { it.data }.toSet()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        var sequencia = 0
        val calendar = Calendar.getInstance()

        // Verifica hoje e os dias anteriores em sequência
        while (datasConcluidas.contains(sdf.format(calendar.time))) {
            sequencia++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return sequencia
    }
}