package com.apol.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class habitos : AppCompatActivity() {

    private lateinit var recyclerViewHabits: RecyclerView
    private lateinit var habitsAdapter: HabitsAdapter
    private lateinit var fabAddHabit: FloatingActionButton
    private lateinit var btnDeleteSelected: ImageButton

    private val PREFS_NAME = "habitos_prefs"
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val allDays = setOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    private var isDeleteMode = false

    private val habitNames = mutableListOf<String>()
    private val habitDailyCounts = mutableMapOf<String, Int>()
    private val habitStreakCounts = mutableMapOf<String, Int>()
    private val habitLastMarkedDate = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_habitos)

        val mainLayout = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerViewHabits = findViewById(R.id.recyclerViewHabits)
        fabAddHabit = findViewById(R.id.fab_add_habit)
        btnDeleteSelected = findViewById(R.id.btn_delete_selected)
        configurarNavigationBar()

        habitsAdapter = HabitsAdapter(
            onItemClick = { habit ->
                if (!isDeleteMode) {
                    mostrarOpcoesHabito(habit)
                }
            },
            onItemLongClick = { habitos_modoExclusao(true) },
            onMarkDone = { habit -> atualizarContagemDoHabito(habit.name, 1) },
            onUndoDone = { habit -> atualizarContagemDoHabito(habit.name, -1) }
        )
        habitsAdapter.onExclusaoModoVazio = {
            habitos_modoExclusao(false)
        }

        recyclerViewHabits.adapter = habitsAdapter
        recyclerViewHabits.layoutManager = LinearLayoutManager(this)
        btnDeleteSelected.setOnClickListener { onBotaoApagarClick() }
        btnDeleteSelected.visibility = View.GONE

        carregarHabitosSalvos()
        carregarSequencias()
        atualizarAdapter()

        fabAddHabit.setOnClickListener { mostrarDialogoNovoHabito() }

        onBackPressedDispatcher.addCallback(this) {
            if (isDeleteMode) {
                habitos_modoExclusao(false)
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
        mainLayout.setOnClickListener {
            if (isDeleteMode) {
                habitos_modoExclusao(false)
            }
        }

        // Verifica se a tela foi aberta com a intenção de criar um novo hábito
        val deveAbrirDialogo = intent.getBooleanExtra("abrir_dialogo_novo_habito", false)
        if (deveAbrirDialogo) {
            mostrarDialogoNovoHabito()
        }
    }

    private fun mostrarOpcoesHabito(habit: Habit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_opcoes_habito, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title = dialogView.findViewById<TextView>(R.id.dialog_options_title)
        title.text = habit.name

        val btnProgresso = dialogView.findViewById<Button>(R.id.btn_ver_progresso)
        val btnEditar = dialogView.findViewById<Button>(R.id.btn_editar_habito)

        btnProgresso.setOnClickListener {
            val intent = Intent(this, activity_progresso_habito::class.java)
            intent.putExtra("habit_name", habit.name)
            startActivity(intent)
            dialog.dismiss()
        }

        btnEditar.setOnClickListener {
            mostrarDialogoEditarHabito(habit)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarDialogoEditarHabito(habit: Habit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_novo_habito, null)
        val etHabitName = dialogView.findViewById<EditText>(R.id.et_habit_name)
        val title = dialogView.findViewById<TextView>(R.id.dialog_title)
        title.text = "Editar Hábito"
        etHabitName.setText(habit.name)
        etHabitName.isEnabled = true
        etHabitName.alpha = 1.0f

        val toggles = mapOf(
            "SUN" to dialogView.findViewById<ToggleButton>(R.id.toggle_dom),
            "MON" to dialogView.findViewById<ToggleButton>(R.id.toggle_seg),
            "TUE" to dialogView.findViewById<ToggleButton>(R.id.toggle_ter),
            "WED" to dialogView.findViewById<ToggleButton>(R.id.toggle_qua),
            "THU" to dialogView.findViewById<ToggleButton>(R.id.toggle_qui),
            "FRI" to dialogView.findViewById<ToggleButton>(R.id.toggle_sex),
            "SAT" to dialogView.findViewById<ToggleButton>(R.id.toggle_sab)
        )

        val diasAtuais = carregarDiasProgramados(habit.name)
        toggles.forEach { (dia, toggle) ->
            if (diasAtuais.contains(dia)) {
                toggle.isChecked = true
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            positiveButton.setTextColor(resources.getColor(R.color.roxo, theme))
            negativeButton.setTextColor(resources.getColor(R.color.roxo, theme))

            positiveButton.setOnClickListener {
                val novoNome = etHabitName.text.toString().trim()
                val nomeAntigo = habit.name

                if (novoNome.isEmpty()) {
                    Toast.makeText(this, "O nome não pode ser vazio.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (novoNome != nomeAntigo && habitNames.contains(novoNome)) {
                    Toast.makeText(this, "Este nome de hábito já existe.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (novoNome != nomeAntigo) {
                    renomearHabito(nomeAntigo, novoNome)
                }

                val selectedDays = toggles.filter { it.value.isChecked }.keys
                val daysToSave = if (selectedDays.isEmpty()) allDays else selectedDays
                salvarDiasProgramados(novoNome, daysToSave)

                Toast.makeText(this, "Hábito atualizado!", Toast.LENGTH_SHORT).show()

                carregarHabitosSalvos()
                carregarSequencias()
                atualizarAdapter()

                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun mostrarDialogoNovoHabito() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_novo_habito, null)
        val etHabitName = dialogView.findViewById<EditText>(R.id.et_habit_name)
        val title = dialogView.findViewById<TextView>(R.id.dialog_title)
        title.text = "Novo Hábito"

        val toggles = mapOf(
            "SUN" to dialogView.findViewById<ToggleButton>(R.id.toggle_dom),
            "MON" to dialogView.findViewById<ToggleButton>(R.id.toggle_seg),
            "TUE" to dialogView.findViewById<ToggleButton>(R.id.toggle_ter),
            "WED" to dialogView.findViewById<ToggleButton>(R.id.toggle_qua),
            "THU" to dialogView.findViewById<ToggleButton>(R.id.toggle_qui),
            "FRI" to dialogView.findViewById<ToggleButton>(R.id.toggle_sex),
            "SAT" to dialogView.findViewById<ToggleButton>(R.id.toggle_sab)
        )

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Adicionar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            positiveButton.setTextColor(resources.getColor(R.color.roxo, theme))
            negativeButton.setTextColor(resources.getColor(R.color.roxo, theme))

            positiveButton.setOnClickListener {
                val texto = etHabitName.text.toString().trim()
                if (texto.isNotEmpty()) {
                    val selectedDays = toggles.filter { it.value.isChecked }.keys
                    val daysToSave = if (selectedDays.isEmpty()) allDays else selectedDays
                    adicionarHabito(texto, daysToSave)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Nome inválido.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun adicionarHabito(nome: String, diasProgramados: Set<String>) {
        if (!habitNames.contains(nome)) {
            habitNames.add(nome)
            salvarHabitos()
            salvarDiasProgramados(nome, diasProgramados)
            atualizarAdapter()
        } else {
            Toast.makeText(this, "Hábito '$nome' já existe.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renomearHabito(nomeAntigo: String, novoNome: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val chavesParaRemover = mutableListOf<String>()

        prefs.all.forEach { (chave, valor) ->
            if (chave.startsWith("${nomeAntigo}_")) {
                val novaChave = chave.replaceFirst(nomeAntigo, novoNome)
                when (valor) {
                    is Int -> editor.putInt(novaChave, valor)
                    is String -> editor.putString(novaChave, valor)
                    is Set<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        editor.putStringSet(novaChave, valor as? Set<String>)
                    }
                    is Boolean -> editor.putBoolean(novaChave, valor)
                    is Float -> editor.putFloat(novaChave, valor)
                    is Long -> editor.putLong(novaChave, valor)
                }
                chavesParaRemover.add(chave)
            }
        }

        chavesParaRemover.forEach { editor.remove(it) }

        val habitSet = prefs.getStringSet("habits_list", mutableSetOf())?.toMutableSet()
        if (habitSet != null) {
            habitSet.remove(nomeAntigo)
            habitSet.add(novoNome)
            editor.putStringSet("habits_list", habitSet)
        }
        editor.apply()

        val index = habitNames.indexOf(nomeAntigo)
        if (index != -1) {
            habitNames[index] = novoNome
        }
    }

    private fun salvarDiasProgramados(nomeHabito: String, dias: Set<String>) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hojeFormatado = dateFormat.format(Date())
        val chave = "${nomeHabito}_scheduled_days_$hojeFormatado"
        prefs.edit().putStringSet(chave, dias).apply()
    }

    private fun carregarDiasProgramados(nomeHabito: String): Set<String> {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val allKeys = prefs.all.keys
        val scheduleKeys = allKeys
            .filter { it.startsWith("${nomeHabito}_scheduled_days") }
            .sortedDescending()

        return if (scheduleKeys.isNotEmpty()) {
            prefs.getStringSet(scheduleKeys.first(), allDays) ?: allDays
        } else {
            allDays
        }
    }

    private fun habitos_modoExclusao(ativo: Boolean) {
        isDeleteMode = ativo
        habitsAdapter.modoExclusaoAtivo = ativo
        habitsAdapter.notifyDataSetChanged()

        if (ativo) {
            fabAddHabit.hide()
            btnDeleteSelected.visibility = View.VISIBLE
        } else {
            fabAddHabit.show()
            btnDeleteSelected.visibility = View.GONE
            habitsAdapter.limparSelecao()
        }
    }

    private fun onBotaoApagarClick() {
        val selecionados = habitsAdapter.getSelecionados()
        if (selecionados.isEmpty()) {
            Toast.makeText(this, "Nenhum hábito selecionado", Toast.LENGTH_SHORT).show()
            habitos_modoExclusao(false)
            return
        }
        val prefsEditor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        selecionados.forEach { habit ->
            habitNames.remove(habit.name)
            val allPrefsKeys = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).all.keys
            allPrefsKeys.filter { it.startsWith("${habit.name}_") }.forEach { keyToRemove ->
                prefsEditor.remove(keyToRemove)
            }
        }
        prefsEditor.apply()
        salvarHabitos()
        habitos_modoExclusao(false)
        atualizarAdapter()
        Toast.makeText(this, "Hábitos apagados", Toast.LENGTH_SHORT).show()
    }

    private fun configurarNavigationBar() {
        val navBar = findViewById<LinearLayout>(R.id.navigation_bar)
        navBar.findViewById<LinearLayout>(R.id.botao_inicio).setOnClickListener {
            startActivity(Intent(this, Bemvindouser::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_anotacoes).setOnClickListener {
            startActivity(Intent(this, anotacoes::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_habitos).setOnClickListener {}
        navBar.findViewById<LinearLayout>(R.id.botao_treinos).setOnClickListener {
            startActivity(Intent(this, treinos::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_cronometro).setOnClickListener {
            startActivity(Intent(this, CronometroActivity::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_configuracoes).setOnClickListener {
            startActivity(Intent(this, configuracoes::class.java))
        }
    }

    private fun carregarHabitosSalvos() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val habitSet = prefs.getStringSet("habits_list", null)
        habitNames.clear()

        if (habitSet == null || habitSet.isEmpty()) {
            habitNames.addAll(listOf("📖 Ler 10 páginas", "🏋️ Exercício", "🧘 Meditar"))
            salvarHabitos()
            habitNames.forEach { salvarDiasProgramados(it, allDays) }
        } else {
            habitNames.addAll(habitSet)
        }

        val hoje = getHoje()
        habitNames.forEach { nome ->
            val chave = "${nome}_$hoje"
            habitDailyCounts[chave] = prefs.getInt(chave, 0)
        }
    }

    private fun carregarSequencias() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        habitNames.forEach { nome ->
            habitStreakCounts[nome] = prefs.getInt("${nome}_streak", 0)
            habitLastMarkedDate[nome] = prefs.getString("${nome}_last_date", null) ?: ""
        }
    }

    private fun atualizarAdapter() {
        val hoje = getHoje()
        val listaHabitos = habitNames.map { nome ->
            val chave = "${nome}_$hoje"
            val contagem = habitDailyCounts[chave] ?: 0
            val sequencia = habitStreakCounts[nome] ?: 0
            val msg = gerarMensagemMotivacional(sequencia)
            Habit(id = nome, name = nome, streakDays = sequencia, message = msg, count = contagem)
        }
        habitsAdapter.submitList(listaHabitos)
    }

    private fun salvarHabitos() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet("habits_list", habitNames.toSet()).apply()
    }

    private fun gerarMensagemMotivacional(diasSeguidos: Int): String {
        return when {
            diasSeguidos == 0 -> "Vamos começar? Você consegue!"
            diasSeguidos in 1..3 -> "Bons começos! Continue firme."
            diasSeguidos in 4..6 -> "Você está melhorando! Continue com esse ritmo."
            diasSeguidos >= 7 -> "Incrível! Você está construindo um hábito forte!"
            else -> "Continue firme!"
        }
    }

    private fun getHoje(): String = dateFormat.format(Date())

    private fun salvarContagem(chave: String, contagem: Int) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(chave, contagem).apply()
    }

    private fun salvarSequencia(nomeHabito: String, sequencia: Int, data: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("${nomeHabito}_streak", sequencia)
            .putString("${nomeHabito}_last_date", data)
            .apply()
    }

    private fun atualizarContagemDoHabito(nomeHabito: String, delta: Int) {
        val hoje = getHoje()
        val chave = "${nomeHabito}_$hoje"
        val contagemAtual = habitDailyCounts[chave] ?: 0
        val novaContagem = (contagemAtual + delta).coerceAtLeast(0)
        habitDailyCounts[chave] = novaContagem
        salvarContagem(chave, novaContagem)

        if (delta > 0) {
            val ultimaData = habitLastMarkedDate[nomeHabito]
            val hojeDate = dateFormat.parse(hoje)!!
            val lastDateParsed = if (!ultimaData.isNullOrEmpty()) dateFormat.parse(ultimaData) else null

            val diff = if (lastDateParsed != null) {
                ((hojeDate.time - lastDateParsed.time) / (1000 * 60 * 60 * 24)).toInt()
            } else {
                -1
            }

            val sequenciaAtual = habitStreakCounts[nomeHabito] ?: 0
            val novaSequencia = when {
                diff == 1 -> sequenciaAtual + 1
                diff > 1 || diff < 0 -> 1
                diff == 0 -> sequenciaAtual
                else -> 1
            }

            habitStreakCounts[nomeHabito] = novaSequencia
            habitLastMarkedDate[nomeHabito] = hoje
            salvarSequencia(nomeHabito, novaSequencia, hoje)
        }

        atualizarAdapter()
    }
}