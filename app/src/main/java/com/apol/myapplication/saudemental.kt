package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.Habito
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class saudemental : AppCompatActivity() {

    // --- NOVO: Acesso ao Banco de Dados e Sessão ---
    private lateinit var db: AppDatabase
    private var emailUsuarioLogado: String? = null

    // "Dicionário" para transformar os hábitos ruins em metas positivas
    private val mapaDeHabitosRuins = mapOf(
        "Fumar" to "🚭 Fumar Menos",
        "Beber" to "🚱 Não Beber",
        "Sono ruim" to "😴 Dormir Melhor",
        "Procrastinação" to "✅ Não Procrastinar",
        "Uso excessivo do celular" to "📵 Usar Menos o Celular"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saudemental)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- INICIALIZAÇÃO DO BANCO DE DADOS E SESSÃO ---
        db = AppDatabase.getDatabase(this)
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        emailUsuarioLogado = prefs.getString("LOGGED_IN_USER_EMAIL", null)

        if (emailUsuarioLogado == null) {
            Toast.makeText(this, "Erro de sessão, por favor, faça login novamente.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Referências para as checkboxes
        val checkFumar = findViewById<CheckBox>(R.id.checkBoxfumar)
        val checkBeber = findViewById<CheckBox>(R.id.checkBox2beber)
        val checkSonoRuim = findViewById<CheckBox>(R.id.checkBox3sonoruim)
        val checkProcrastinacao = findViewById<CheckBox>(R.id.checkBox4procastinacao)
        val checkUsoCelular = findViewById<CheckBox>(R.id.checkBox5usoexcessivodocelular)
        val checkNenhumHabito = findViewById<CheckBox>(R.id.checkBoxNenhumHabito)
        val listaHabitosChecks = listOf(checkFumar, checkBeber, checkSonoRuim, checkProcrastinacao, checkUsoCelular)

        val checkAnsiedade = findViewById<CheckBox>(R.id.checkBox6ansiedade)
        val checkDepressao = findViewById<CheckBox>(R.id.checkBox7depressao)
        val checkEstresse = findViewById<CheckBox>(R.id.checkBox8estresse)
        val checkFaltaMotivacao = findViewById<CheckBox>(R.id.checkBox9faltademotivacao)
        val checkSemProblema = findViewById<CheckBox>(R.id.checkBoxSemProblema)
        val listaEmocional = listOf(checkAnsiedade, checkDepressao, checkEstresse, checkFaltaMotivacao)

        // Lógica de interação (sem alterações)
        checkNenhumHabito.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                listaHabitosChecks.forEach { it.isChecked = false; it.isEnabled = false }
            } else {
                listaHabitosChecks.forEach { it.isEnabled = true }
            }
        }
        listaHabitosChecks.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) checkNenhumHabito.isChecked = false
            }
        }

        checkSemProblema.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                listaEmocional.forEach { it.isChecked = false; it.isEnabled = false }
            } else {
                listaEmocional.forEach { it.isEnabled = true }
            }
        }
        listaEmocional.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) checkSemProblema.isChecked = false
            }
        }

        // --- LÓGICA DO BOTÃO TOTALMENTE ATUALIZADA ---
        val btnAvancar = findViewById<Button>(R.id.buttonavancarsaudemental)
        btnAvancar.setOnClickListener {
            if (validarRespostas(listaHabitosChecks, checkNenhumHabito, listaEmocional, checkSemProblema)) {

                // Pega os hábitos marcados
                val habitosMarcados = listaHabitosChecks
                    .filter { it.isChecked }
                    .map { it.text.toString() }

                // Inicia o processo para salvar no banco de dados
                salvarHabitosNoBanco(habitosMarcados)

            } else {
                Toast.makeText(this, "Por favor, selecione uma opção para cada pergunta!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validarRespostas(
        habitos: List<CheckBox>, nenhumHabito: CheckBox,
        emocional: List<CheckBox>, semProblema: CheckBox
    ): Boolean {
        val pergunta1Respondida = habitos.any { it.isChecked } || nenhumHabito.isChecked
        val pergunta2Respondida = emocional.any { it.isChecked } || semProblema.isChecked
        return pergunta1Respondida && pergunta2Respondida
    }

    // --- FUNÇÃO ATUALIZADA PARA SALVAR NO BANCO DE DADOS (ROOM) ---
    private fun salvarHabitosNoBanco(habitosSelecionados: List<String>) {
        lifecycleScope.launch {
            val allDays = "SUN,MON,TUE,WED,THU,FRI,SAT"
            val novasMetas = habitosSelecionados.mapNotNull { mapaDeHabitosRuins[it] }

            novasMetas.forEach { meta ->
                // Cria uma instância de Habito (a entidade do banco de dados)
                val novoHabito = Habito(
                    userOwnerEmail = emailUsuarioLogado!!,
                    nome = meta,
                    diasProgramados = allDays,
                    isFavorito = false,
                    isGoodHabit = false // Marcamos como um "hábito a mudar" (ruim)
                )
                // Insere o novo hábito no banco de dados
                db.habitoDao().insertHabito(novoHabito)
            }

            // Após salvar, navega para a próxima tela
            runOnUiThread {
                val intent = Intent(this@saudemental, pergunta01::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}