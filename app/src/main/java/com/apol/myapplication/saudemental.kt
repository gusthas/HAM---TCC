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

        // Referência para os checkboxes da primeira pergunta
        val checkBoxFumar = findViewById<CheckBox>(R.id.checkBoxfumar)
        val checkBoxBeber = findViewById<CheckBox>(R.id.checkBox2beber)
        val checkBoxSonoRuim = findViewById<CheckBox>(R.id.checkBox3sonoruim)
        val checkBoxProcrastinacao = findViewById<CheckBox>(R.id.checkBox4procastinacao)
        val checkBoxUsoExcessivoCelular = findViewById<CheckBox>(R.id.checkBox5usoexcessivodocelular)

        // Referência para os checkboxes da segunda pergunta
        val checkBoxAnsiedade = findViewById<CheckBox>(R.id.checkBox6ansiedade)
        val checkBoxDepressao = findViewById<CheckBox>(R.id.checkBox7depressao)
        val checkBoxEstresse = findViewById<CheckBox>(R.id.checkBox8estresse)
        val checkBoxFaltaDeMotivacao = findViewById<CheckBox>(R.id.checkBox9faltademotivacao)

        val btnAvancar = findViewById<Button>(R.id.buttonavancarsaudemental)
        btnAvancar.setOnClickListener {
            if (validarRespostas(
                    checkBoxFumar, checkBoxBeber, checkBoxSonoRuim, checkBoxProcrastinacao, checkBoxUsoExcessivoCelular,
                    checkBoxAnsiedade, checkBoxDepressao, checkBoxEstresse, checkBoxFaltaDeMotivacao
                )) {
                val intent = Intent(this, sujestao::class.java)
                startActivity(intent)
            } else {
                // Exibe uma mensagem se nenhuma checkbox foi selecionada
                Toast.makeText(this, "Por favor, selecione pelo menos uma opção em cada pergunta!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função de validação para garantir que pelo menos uma checkbox foi selecionada em cada pergunta
    private fun validarRespostas(
        checkBoxFumar: CheckBox, checkBoxBeber: CheckBox, checkBoxSonoRuim: CheckBox, checkBoxProcrastinacao: CheckBox,
        checkBoxUsoExcessivoCelular: CheckBox, checkBoxAnsiedade: CheckBox, checkBoxDepressao: CheckBox,
        checkBoxEstresse: CheckBox, checkBoxFaltaDeMotivacao: CheckBox
    ): Boolean {
        // Valida a primeira pergunta
        val pergunta1Respondida = checkBoxFumar.isChecked || checkBoxBeber.isChecked || checkBoxSonoRuim.isChecked ||
                checkBoxProcrastinacao.isChecked || checkBoxUsoExcessivoCelular.isChecked

        // Valida a segunda pergunta
        val pergunta2Respondida = checkBoxAnsiedade.isChecked || checkBoxDepressao.isChecked ||
                checkBoxEstresse.isChecked || checkBoxFaltaDeMotivacao.isChecked

        // Retorna true se ambas as perguntas tiverem ao menos uma opção selecionada
        return pergunta1Respondida && pergunta2Respondida
    }
}