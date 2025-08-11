// Substitua o conteúdo COMPLETO do seu arquivo saudemental.kt
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

class saudemental : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saudemental)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Referências para as checkboxes ---
        // Pergunta 1: Hábitos
        val checkFumar = findViewById<CheckBox>(R.id.checkBoxfumar)
        val checkBeber = findViewById<CheckBox>(R.id.checkBox2beber)
        val checkSonoRuim = findViewById<CheckBox>(R.id.checkBox3sonoruim)
        val checkProcrastinacao = findViewById<CheckBox>(R.id.checkBox4procastinacao)
        val checkUsoCelular = findViewById<CheckBox>(R.id.checkBox5usoexcessivodocelular)
        val checkNenhumHabito = findViewById<CheckBox>(R.id.checkBoxNenhumHabito) // NOVO
        val listaHabitos = listOf(checkFumar, checkBeber, checkSonoRuim, checkProcrastinacao, checkUsoCelular)

        // Pergunta 2: Emocional
        val checkAnsiedade = findViewById<CheckBox>(R.id.checkBox6ansiedade)
        val checkDepressao = findViewById<CheckBox>(R.id.checkBox7depressao)
        val checkEstresse = findViewById<CheckBox>(R.id.checkBox8estresse)
        val checkFaltaMotivacao = findViewById<CheckBox>(R.id.checkBox9faltademotivacao)
        val checkSemProblema = findViewById<CheckBox>(R.id.checkBoxSemProblema) // NOVO
        val listaEmocional = listOf(checkAnsiedade, checkDepressao, checkEstresse, checkFaltaMotivacao)

        // --- Lógica de Interação ---
        // Lógica para a opção "Nenhum" da Pergunta 1
        checkNenhumHabito.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                listaHabitos.forEach { it.isChecked = false; it.isEnabled = false }
            } else {
                listaHabitos.forEach { it.isEnabled = true }
            }
        }
        listaHabitos.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) checkNenhumHabito.isChecked = false
            }
        }

        // Lógica para a opção "Nenhum" da Pergunta 2
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

        // --- Botão Avançar ---
        val btnAvancar = findViewById<Button>(R.id.buttonavancarsaudemental)
        btnAvancar.setOnClickListener {
            if (validarRespostas(listaHabitos, checkNenhumHabito, listaEmocional, checkSemProblema)) {
                // Aqui você pode salvar as respostas do usuário antes de avançar

                val intent = Intent(this, pergunta01::class.java)
                startActivity(intent)
                finish() // Adicionado para fechar a tela de perguntas
            } else {
                Toast.makeText(this, "Por favor, selecione uma opção para cada pergunta!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função de validação atualizada para a nova lógica
    private fun validarRespostas(
        habitos: List<CheckBox>, nenhumHabito: CheckBox,
        emocional: List<CheckBox>, semProblema: CheckBox
    ): Boolean {
        // Valida se pelo menos uma opção da pergunta 1 foi marcada
        val pergunta1Respondida = habitos.any { it.isChecked } || nenhumHabito.isChecked
        // Valida se pelo menos uma opção da pergunta 2 foi marcada
        val pergunta2Respondida = emocional.any { it.isChecked } || semProblema.isChecked

        return pergunta1Respondida && pergunta2Respondida
    }
}