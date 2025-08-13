package com.apol.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

class saudemental : AppCompatActivity() {

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

        // Lógica de interação
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

        // Botão Avançar
        val btnAvancar = findViewById<Button>(R.id.buttonavancarsaudemental)
        btnAvancar.setOnClickListener {
            if (validarRespostas(listaHabitosChecks, checkNenhumHabito, listaEmocional, checkSemProblema)) {

                // Pega os hábitos marcados
                val habitosMarcados = listaHabitosChecks
                    .filter { it.isChecked }
                    .map { it.text.toString() }

                if (habitosMarcados.isNotEmpty()) {
                    salvarHabitosSelecionados(habitosMarcados)
                }

                val intent = Intent(this, pergunta01::class.java) // Navega para a próxima tela
                startActivity(intent)
                finish()
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

    private fun salvarHabitosSelecionados(habitosSelecionados: List<String>) {
        val prefs = getSharedPreferences("habitos_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // --- LÓGICA PRINCIPAL ---
        // 1. Carrega a lista de hábitos que já existem
        val habitsString = prefs.getString("habits_list_ordered", null)
        val listaAtual = if (!habitsString.isNullOrEmpty()) habitsString.split(";;;").toMutableList() else mutableListOf()

        // 2. Carrega a lista de hábitos RUINS que já existem
        val badHabitsSet = prefs.getStringSet("bad_habits_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        // 3. Transforma os hábitos ruins selecionados em metas positivas
        val novasMetas = habitosSelecionados.mapNotNull { mapaDeHabitosRuins[it] }

        // 4. Adiciona as novas metas à lista principal e à lista de hábitos ruins
        novasMetas.forEach { meta ->
            if (!listaAtual.contains(meta)) {
                listaAtual.add(meta) // Adiciona na lista principal
                badHabitsSet.add(meta) // Adiciona na lista de hábitos ruins
            }
        }

        // 5. Salva as duas listas atualizadas
        editor.putString("habits_list_ordered", listaAtual.joinToString(";;;"))
        editor.putStringSet("bad_habits_list", badHabitsSet)

        // 6. Salva a configuração de dias padrão para cada nova meta
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val hojeFormatado = dateFormat.format(Date())
        val allDays = setOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

        novasMetas.forEach { nomeDaMeta ->
            val chaveDias = "${nomeDaMeta}_scheduled_days_$hojeFormatado"
            editor.putStringSet(chaveDias, allDays)
        }

        editor.apply()
    }
}