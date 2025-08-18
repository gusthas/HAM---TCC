package com.apol.myapplication

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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.Habito
import com.apol.myapplication.data.model.HabitUI
import com.apol.myapplication.data.model.HabitoProgresso
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class habitos : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var habitsAdapter: HabitsAdapter
    private var emailUsuarioLogado: String? = null
    private var mostrandoHabitosBons = true
    private lateinit var habitsTitle: TextView
    private val allDays = setOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    // --- VARIÁVEIS PARA O MODO DE EXCLUSÃO ---
    private var modoExclusaoAtivo = false
    private lateinit var btnDeleteSelected: ImageButton
    private lateinit var clickOutsideView: View
    private lateinit var fabAddHabit: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habitos)

        db = AppDatabase.getDatabase(this)
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        emailUsuarioLogado = prefs.getString("LOGGED_IN_USER_EMAIL", null)

        if (emailUsuarioLogado == null) {
            Toast.makeText(this, "Erro: Usuário não encontrado.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        habitsTitle = findViewById(R.id.habits_title)
        btnDeleteSelected = findViewById(R.id.btn_delete_selected)
        clickOutsideView = findViewById(R.id.click_outside_view)
        fabAddHabit = findViewById(R.id.fab_add_habit)

        setupRecyclerView()
        setupListeners()
        configurarNavigationBar()
    }

    override fun onResume() {
        super.onResume()
        if (modoExclusaoAtivo) {
            desativarModoExclusao()
        }
        atualizarTelaDeHabitos()
    }

    override fun onBackPressed() {
        if (modoExclusaoAtivo) {
            desativarModoExclusao()
        } else {
            super.onBackPressed()
        }
    }

    private fun atualizarTelaDeHabitos() {
        habitsTitle.text = if (mostrandoHabitosBons) "Seus Hábitos Bons" else "Seus Hábitos Ruins"
        findViewById<ImageButton>(R.id.button_toggle_mode)?.setImageResource(R.drawable.ic_refresh)
        carregarHabitosDoBanco()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHabits)
        habitsAdapter = HabitsAdapter(
            onItemClick = { habit ->
                if (modoExclusaoAtivo) {
                    toggleSelecao(habit)
                } else {
                    mostrarOpcoesHabito(habit)
                }
            },
            onItemLongClick = { habit ->
                if (!modoExclusaoAtivo) {
                    ativarModoExclusao(habit)
                }
            },
            onMarkDone = { habito -> marcarHabito(habito, true) },
            onUndoDone = { habito -> marcarHabito(habito, false) },
            onToggleFavorite = { habito -> toggleFavorito(habito) }
        )
        recyclerView.adapter = habitsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        fabAddHabit.setOnClickListener {
            if (!modoExclusaoAtivo) {
                mostrarDialogoNovoHabito()
            }
        }

        findViewById<ImageButton>(R.id.button_toggle_mode)?.setOnClickListener {
            if (modoExclusaoAtivo) desativarModoExclusao()
            mostrandoHabitosBons = !mostrandoHabitosBons
            atualizarTelaDeHabitos()
        }

        btnDeleteSelected.setOnClickListener {
            val selecionados = habitsAdapter.getSelecionados()
            if (selecionados.isNotEmpty()) {
                confirmarExclusao(selecionados)
            }
        }

        clickOutsideView.setOnClickListener {
            desativarModoExclusao()
        }
    }

    private fun ativarModoExclusao(primeiroItem: HabitUI) {
        modoExclusaoAtivo = true
        habitsAdapter.modoExclusaoAtivo = true
        fabAddHabit.visibility = View.GONE
        btnDeleteSelected.visibility = View.VISIBLE
        clickOutsideView.visibility = View.VISIBLE
        toggleSelecao(primeiroItem)
    }

    private fun desativarModoExclusao() {
        modoExclusaoAtivo = false
        habitsAdapter.modoExclusaoAtivo = false
        habitsAdapter.limparSelecao()
        fabAddHabit.visibility = View.VISIBLE
        btnDeleteSelected.visibility = View.GONE
        clickOutsideView.visibility = View.GONE
    }

    private fun toggleSelecao(habit: HabitUI) {
        habitsAdapter.toggleSelecao(habit)
        if (habitsAdapter.getSelecionados().isEmpty() && modoExclusaoAtivo) {
            desativarModoExclusao()
        }
    }

    private fun confirmarExclusao(habitosParaApagar: List<HabitUI>) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Hábitos")
            .setMessage("Tem certeza que deseja apagar ${habitosParaApagar.size} hábito(s)?")
            .setPositiveButton("Excluir") { _, _ ->
                executarExclusao(habitosParaApagar)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun executarExclusao(habitosParaApagar: List<HabitUI>) {
        lifecycleScope.launch {
            val idsParaApagar = habitosParaApagar.mapNotNull { it.id.toLongOrNull() }
            if (idsParaApagar.isNotEmpty()) {
                db.habitoDao().deleteHabitosByIds(idsParaApagar)
            }
            runOnUiThread {
                Toast.makeText(this@habitos, "Hábitos apagados", Toast.LENGTH_SHORT).show()
                desativarModoExclusao()
                carregarHabitosDoBanco()
            }
        }
    }

    private fun carregarHabitosDoBanco() {
        emailUsuarioLogado?.let { email ->
            lifecycleScope.launch {
                val habitosDoBanco: List<Habito> = db.habitoDao().getHabitosByUser(email)
                val habitosFiltrados = habitosDoBanco.filter { it.isGoodHabit == mostrandoHabitosBons }
                val hoje = getHojeString()

                // Converte a lista de Habito (BD) para HabitUI (Tela)
                val listaParaAdapter = habitosFiltrados.map { habitoDB ->
                    val progressos = db.habitoDao().getProgressoForHabito(habitoDB.id)
                    val concluidoHoje = progressos.any { it.data == hoje }
                    val sequencia = calcularSequencia(progressos)
                    HabitUI(
                        id = habitoDB.id.toString(),
                        name = habitoDB.nome,
                        streakDays = sequencia,
                        message = gerarMensagemMotivacional(sequencia),
                        count = if (concluidoHoje) 1 else 0,
                        isFavorited = habitoDB.isFavorito
                    )
                }

                runOnUiThread {
                    habitsAdapter.submitList(listaParaAdapter)
                }
            }
        }
    }

    private fun adicionarHabito(nome: String, diasProgramados: Set<String>) {
        emailUsuarioLogado?.let { email ->
            lifecycleScope.launch {
                val habitosExistentes = db.habitoDao().getHabitosByUser(email)
                if (habitosExistentes.any { it.nome.equals(nome, ignoreCase = true) }) {
                    runOnUiThread { Toast.makeText(this@habitos, "Hábito '$nome' já existe.", Toast.LENGTH_SHORT).show() }
                    return@launch
                }

                // Cria uma instância de Habito (a entidade do banco de dados)
                val novoHabito = Habito(
                    userOwnerEmail = email,
                    nome = nome,
                    diasProgramados = diasProgramados.joinToString(","),
                    isFavorito = false,
                    isGoodHabit = mostrandoHabitosBons
                )
                db.habitoDao().insertHabito(novoHabito)
                carregarHabitosDoBanco()
            }
        }
    }

    private fun marcarHabito(habit: HabitUI, concluir: Boolean) {
        val habitoId = habit.id.toLongOrNull() ?: return
        val hoje = getHojeString()
        lifecycleScope.launch {
            if (concluir) {
                db.habitoDao().insertProgresso(HabitoProgresso(habitoId = habitoId, data = hoje))
            } else {
                db.habitoDao().deleteProgresso(habitoId = habitoId, data = hoje)
            }
            carregarHabitosDoBanco()
        }
    }

    private fun toggleFavorito(habit: HabitUI) {
        val habitoId = habit.id.toLongOrNull() ?: return
        emailUsuarioLogado?.let { email ->
            lifecycleScope.launch {
                val habitosDoBanco = db.habitoDao().getHabitosByUser(email)
                val habitoDB = habitosDoBanco.find { it.id == habitoId }

                habitoDB?.let {
                    val totalFavoritos = habitosDoBanco.count { it.isFavorito && it.id != it.id }
                    if (!it.isFavorito && totalFavoritos >= 3) {
                        runOnUiThread { Toast.makeText(this@habitos, "Você pode favoritar no máximo 3 hábitos.", Toast.LENGTH_SHORT).show() }
                        return@launch
                    }

                    it.isFavorito = !it.isFavorito
                    db.habitoDao().updateHabito(it)
                    carregarHabitosDoBanco()
                }
            }
        }
    }

    private fun mostrarOpcoesHabito(habit: HabitUI) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_opcoes_habito, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<TextView>(R.id.dialog_options_title)?.text = habit.name

        dialog.findViewById<Button>(R.id.btn_ver_progresso)?.setOnClickListener {
            val intent = Intent(this, activity_progresso_habito::class.java)
            intent.putExtra("habit_name", habit.name)
            startActivity(intent)
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.btn_editar_habito)?.setOnClickListener {
            Toast.makeText(this, "Edição não implementada.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun mostrarDialogoNovoHabito() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_novo_habito, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent).setView(dialogView).create()

        val etHabitName = dialogView.findViewById<EditText>(R.id.et_habit_name)
        val btnAdicionar = dialogView.findViewById<Button>(R.id.btn_adicionar_habito)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btn_cancelar_habito)
        val toggles = mapOf(
            "SUN" to dialogView.findViewById<ToggleButton>(R.id.toggle_dom), "MON" to dialogView.findViewById<ToggleButton>(R.id.toggle_seg),
            "TUE" to dialogView.findViewById<ToggleButton>(R.id.toggle_ter), "WED" to dialogView.findViewById<ToggleButton>(R.id.toggle_qua),
            "THU" to dialogView.findViewById<ToggleButton>(R.id.toggle_qui), "FRI" to dialogView.findViewById<ToggleButton>(R.id.toggle_sex),
            "SAT" to dialogView.findViewById<ToggleButton>(R.id.toggle_sab)
        )

        btnAdicionar.setOnClickListener {
            val nome = etHabitName.text.toString().trim()
            if (nome.isNotEmpty()) {
                val selectedDays = toggles.filter { it.value.isChecked }.keys
                val daysToSave = if (selectedDays.isEmpty()) allDays else selectedDays
                adicionarHabito(nome, daysToSave)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "O nome do hábito não pode ser vazio.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getHojeString(): String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    private fun calcularSequencia(progressos: List<HabitoProgresso>): Int {
        if (progressos.isEmpty()) return 0
        val datasConcluidas = progressos.map { it.data }.toSet()
        var sequencia = 0
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        if (datasConcluidas.contains(sdf.format(calendar.time))) {
            sequencia++
        }

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        while (datasConcluidas.contains(sdf.format(calendar.time))) {
            sequencia++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return sequencia
    }

    private fun gerarMensagemMotivacional(diasSeguidos: Int): String {
        return when {
            diasSeguidos == 0 -> "Comece hoje! Você consegue!"
            diasSeguidos == 1 -> "Primeiro dia! Continue assim."
            diasSeguidos in 2..6 -> "$diasSeguidos dias! Mantenha o ritmo."
            diasSeguidos >= 7 -> "Uma semana! Incrível!"
            else -> "Continue firme!"
        }
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
        navBar.findViewById<View>(R.id.botao_cronometro)?.setOnClickListener {
            startActivity(Intent(this, CronometroActivity::class.java))
        }
        navBar.findViewById<View>(R.id.botao_sugestoes)?.setOnClickListener {
            startActivity(Intent(this, SugestaoUser::class.java))
        }
    }
}