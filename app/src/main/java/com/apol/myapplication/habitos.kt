package com.apol.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
import com.apol.myapplication.data.model.*
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

        setupRecyclerView()
        setupListeners()
        configurarNavigationBar()
    }

    override fun onResume() {
        super.onResume()
        atualizarTelaDeHabitos()
    }

    private fun atualizarTelaDeHabitos() {
        habitsTitle.text = if (mostrandoHabitosBons) "Seus Hábitos Bons" else "Hábitos a Mudar"
        findViewById<ImageButton>(R.id.button_toggle_mode).setImageResource(
            if(mostrandoHabitosBons) R.drawable.ic_good_habit else R.drawable.ic_bad_habit
        )
        carregarHabitosDoBanco()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHabits)
        habitsAdapter = HabitsAdapter(
            mutableListOf(),
            onItemClick = { habit -> mostrarOpcoesHabito(habit) },
            onMarkDone = { habito -> marcarHabito(habito, true) },
            onUndoDone = { habito -> marcarHabito(habito, false) },
            onToggleFavorite = { habito -> toggleFavorito(habito) }
        )
        recyclerView.adapter = habitsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        findViewById<FloatingActionButton>(R.id.fab_add_habit).setOnClickListener {
            mostrarDialogoNovoHabito()
        }
        findViewById<ImageButton>(R.id.button_toggle_mode).setOnClickListener {
            mostrandoHabitosBons = !mostrandoHabitosBons
            atualizarTelaDeHabitos()
        }
    }

    private fun carregarHabitosDoBanco() {
        emailUsuarioLogado?.let { email ->
            lifecycleScope.launch {
                val habitosDoBanco = db.habitoDao().getHabitosByUser(email)

                // FILTRA A LISTA ANTES DE EXIBIR
                val habitosFiltrados = habitosDoBanco.filter { it.isGoodHabit == mostrandoHabitosBons }

                val listaParaAdapter = habitosFiltrados.map { habitoDB ->
                    val progressos = db.habitoDao().getProgressoForHabito(habitoDB.id)
                    val concluidoHoje = progressos.any { it.data == getHojeString() }
                    val sequencia = calcularSequencia(progressos)
                    Habit(
                        id = habitoDB.id.toString(), name = habitoDB.nome,
                        streakDays = sequencia, message = gerarMensagemMotivacional(sequencia),
                        count = if (concluidoHoje) 1 else 0, isFavorited = habitoDB.isFavorito
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
                if (habitosExistentes.any { removerEmoji(it.nome).equals(removerEmoji(nome), ignoreCase = true) }) {
                    runOnUiThread { Toast.makeText(this@habitos, "Hábito '$nome' já existe.", Toast.LENGTH_SHORT).show() }
                    return@launch
                }

                val novoHabito = Habito(
                    userOwnerEmail = email, nome = nome,
                    diasProgramados = diasProgramados.joinToString(","),
                    isFavorito = false,
                    isGoodHabit = mostrandoHabitosBons // Salva o tipo correto
                )
                db.habitoDao().insertHabito(novoHabito)
                carregarHabitosDoBanco()
            }
        }
    }

    private fun marcarHabito(habit: Habit, concluir: Boolean) {
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

    private fun toggleFavorito(habit: Habit) {
        val habitoId = habit.id.toLongOrNull() ?: return
        emailUsuarioLogado?.let { email ->
            lifecycleScope.launch {
                val habitosDoBanco = db.habitoDao().getHabitosByUser(email)
                val habitoDB = habitosDoBanco.find { it.id == habitoId }

                habitoDB?.let {
                    val totalFavoritos = habitosDoBanco.count { it.isFavorito }
                    if (!it.isFavorito && totalFavoritos >= 3) {
                        runOnUiThread { Toast.makeText(this@habitos, "Você pode favoritar no máximo 3 hábitos.", Toast.LENGTH_SHORT).show() }
                        return@launch
                    }

                    it.isFavorito = !it.isFavorito
                    db.habitoDao().updateHabito(it)
                    saveFavoritedHabitsToPrefs()
                    carregarHabitosDoBanco()
                }
            }
        }
    }

    private suspend fun saveFavoritedHabitsToPrefs() {
        emailUsuarioLogado?.let { email ->
            val habitosDoBanco = db.habitoDao().getHabitosByUser(email)
            val favoritedNames = habitosDoBanco.filter { it.isFavorito }.map { it.nome }.toSet()
            val prefs = getSharedPreferences("habitos_prefs", Context.MODE_PRIVATE)
            prefs.edit().putStringSet("favorited_habits", favoritedNames).apply()
        }
    }

    private fun mostrarOpcoesHabito(habit: Habit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_opcoes_habito, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<TextView>(R.id.dialog_options_title)?.text = removerEmoji(habit.name)

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
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        // ESTA É A LINHA QUE EU TINHA ESQUECIDO. ELA RESOLVE O PROBLEMA.
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etHabitName = dialogView.findViewById<EditText>(R.id.et_habit_name)
        val btnAdicionar = dialogView.findViewById<Button>(R.id.btn_adicionar_habito)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btn_cancelar_habito)
        val toggles = mapOf(
            "SUN" to dialogView.findViewById<ToggleButton>(R.id.toggle_dom), "MON" to dialogView.findViewById<ToggleButton>(R.id.toggle_seg),
            "TUE" to dialogView.findViewById<ToggleButton>(R.id.toggle_ter), "WED" to dialogView.findViewById<ToggleButton>(R.id.toggle_qua),
            "THU" to dialogView.findViewById<ToggleButton>(R.id.toggle_qui), "FRI" to dialogView.findViewById<ToggleButton>(R.id.toggle_sex),
            "SAT" to dialogView.findViewById<ToggleButton>(R.id.toggle_sab)
        )
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Adicionar") { _, _ ->
                val nome = etHabitName.text.toString().trim()
                if (nome.isNotEmpty()) {
                    val selectedDays = toggles.filter { it.value.isChecked }.keys
                    val daysToSave = if (selectedDays.isEmpty()) allDays else selectedDays
                    adicionarHabito(nome, daysToSave)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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

    fun extrairEmoji(texto: String): String {
        val regex = Regex("^\\p{So}")
        return regex.find(texto)?.value ?: ""
    }

    fun removerEmoji(texto: String): String {
        val regex = Regex("^\\p{So}\\s*")
        return texto.replaceFirst(regex, "")
    }

    fun TextDrawable(context: Context, text: String): Drawable {
        return object : Drawable() {
            private val paint = Paint()
            init {
                paint.color = Color.WHITE; paint.textSize = 64f; paint.isAntiAlias = true
                paint.textAlign = Paint.Align.CENTER; paint.typeface = Typeface.DEFAULT_BOLD
            }
            override fun draw(canvas: Canvas) {
                val bounds = bounds
                val x = bounds.centerX().toFloat()
                val y = bounds.centerY() - (paint.descent() + paint.ascent()) / 2
                canvas.drawText(text, x, y, paint)
            }
            override fun setAlpha(alpha: Int) { paint.alpha = alpha }
            override fun getOpacity(): Int = PixelFormat.TRANSPARENT
            override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
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