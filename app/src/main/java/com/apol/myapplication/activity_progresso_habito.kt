package com.apol.myapplication

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.Habito
import com.apol.myapplication.data.model.HabitoAgendamento
import com.apol.myapplication.data.model.HabitoProgresso
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class activity_progresso_habito : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var habitId: Long = -1L
    private var habitoAtual: Habito? = null
    private var listaDeProgresso: List<HabitoProgresso> = emptyList()
    private var listaDeAgendamentos: List<HabitoAgendamento> = emptyList()

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

        chipGroupFilters.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chip_semana -> atualizarGraficoEConstancia(DIAS_SEMANA, "Semana")
                R.id.chip_mes -> atualizarGraficoEConstancia(DIAS_MES, "Mês")
                R.id.chip_ano -> atualizarGraficoEConstancia(DIAS_ANO, "Ano")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        carregarDadosDoBanco()
    }

    private fun carregarDadosDoBanco() {
        lifecycleScope.launch {
            habitoAtual = db.habitoDao().getHabitoById(habitId)
            listaDeProgresso = db.habitoDao().getProgressoForHabito(habitId)
            listaDeAgendamentos = db.habitoDao().getAgendamentosParaHabito(habitId)

            runOnUiThread {
                if (habitoAtual != null && listaDeAgendamentos.isNotEmpty()) {
                    findViewById<TextView>(R.id.title_progresso).text = habitoAtual!!.nome
                    atualizarDiasSeguidos()
                    if (chipGroupFilters.checkedChipId == View.NO_ID) {
                        chipGroupFilters.check(R.id.chip_mes)
                    } else {
                        val checkedId = chipGroupFilters.checkedChipId
                        atualizarGraficoEConstanciaPeloId(checkedId)
                    }
                } else {
                    Toast.makeText(this@activity_progresso_habito, "Hábito não encontrado.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun atualizarGraficoEConstanciaPeloId(checkedId: Int) {
        when (checkedId) {
            R.id.chip_semana -> atualizarGraficoEConstancia(DIAS_SEMANA, "Semana")
            R.id.chip_mes -> atualizarGraficoEConstancia(DIAS_MES, "Mês")
            R.id.chip_ano -> atualizarGraficoEConstancia(DIAS_ANO, "Ano")
        }
    }

    private fun atualizarGraficoEConstancia(diasParaTras: Int, periodoLabel: String) {
        val (dados, legendas) = gerarDadosDoGrafico(listaDeProgresso, diasParaTras)
        simpleLineChart.setData(dados, legendas)

        val constancia = calcularConstancia(listaDeProgresso, diasParaTras)
        tvConstanciaGeral.text = "$constancia%"
        tvConstanciaLabel.text = "Constância ($periodoLabel)"
    }

    private fun getAgendamentoParaData(data: Calendar, agendamentos: List<HabitoAgendamento>): Set<String> {
        val dataFormatada = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(data.time)
        val agendamentoCorreto = agendamentos.find { it.dataDeInicio <= dataFormatada }
        return agendamentoCorreto?.diasProgramados?.split(',')?.toSet() ?: emptySet()
    }

    private fun calcularConstancia(progresso: List<HabitoProgresso>, diasParaTras: Int): Int {
        val datasConcluidas = progresso.map { it.data }.toSet()
        var diasFeitos = 0
        var diasProgramadosConsiderados = 0
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val hojeStr = sdf.format(Date())

        for (i in 0 until diasParaTras) {
            val dia = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dataFormatada = sdf.format(dia.time)

            if (dataFormatada <= hojeStr) {
                val diasProgramadosNesteDia = getAgendamentoParaData(dia, listaDeAgendamentos)
                if (diasProgramadosNesteDia.contains(getDayOfWeekString(dia))) {
                    diasProgramadosConsiderados++
                    if (datasConcluidas.contains(dataFormatada)) {
                        diasFeitos++
                    }
                }
            }
        }
        if (diasProgramadosConsiderados == 0) return 100
        return (diasFeitos * 100) / diasProgramadosConsiderados
    }

    private fun gerarDadosDoGrafico(progresso: List<HabitoProgresso>, diasParaTras: Int): Pair<List<Int>, List<String>> {
        val datasConcluidas = progresso.map { it.data }.toSet()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val labelFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val hojeStr = sdf.format(Date())

        val pontos = mutableListOf<Int>()
        val legendas = mutableListOf<String>()
        var saldo = 0

        for (i in (diasParaTras - 1) downTo 0) {
            val diaDoLoop = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val diaDaSemana = getDayOfWeekString(diaDoLoop)
            val dataFormatada = sdf.format(diaDoLoop.time)

            val diasProgramadosNesteDia = getAgendamentoParaData(diaDoLoop, listaDeAgendamentos)

            if (diasProgramadosNesteDia.contains(diaDaSemana)) {
                if (datasConcluidas.contains(dataFormatada)) {
                    saldo++
                } else if (dataFormatada < hojeStr) {
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

        while (datasConcluidas.contains(sdf.format(calendar.time))) {
            sequencia++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return sequencia
    }
}