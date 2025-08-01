package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class livro : AppCompatActivity() {

    // Variáveis globais para armazenar as respostas
    private var respostaLeitura: String? = null
    private var respostaDieta: String? = null
    private var respostaSeguir: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_livro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Coleta as respostas dos RadioGroups
        val radioGroup1 = findViewById<RadioGroup>(R.id.radioGroup02)
        val radioGroup2 = findViewById<RadioGroup>(R.id.RadioGroup1)
        val radioGroup3 = findViewById<RadioGroup>(R.id.radioGroup3)

        // Listener para o primeiro grupo de rádio
        radioGroup1.setOnCheckedChangeListener { _, checkedId ->
            respostaLeitura = findViewById<RadioButton>(checkedId).text.toString()
        }

        // Listener para o segundo grupo de rádio
        radioGroup2.setOnCheckedChangeListener { _, checkedId ->
            respostaDieta = findViewById<RadioButton>(checkedId).text.toString()
        }

        // Listener para o terceiro grupo de rádio
        radioGroup3.setOnCheckedChangeListener { _, checkedId ->
            respostaSeguir = findViewById<RadioButton>(checkedId).text.toString()
        }

        // Pega o id do botão e manda para a próxima tela quando clicar
        val btnavancar = findViewById<Button>(R.id.buttonavancarlivro)
        btnavancar.setOnClickListener{
            if (validarRespostas()) {
                val intent = Intent(this, saudemental::class.java)
                startActivity(intent)
            } else {
                // Exibe uma mensagem se alguma pergunta não foi respondida
                Toast.makeText(this, "Por favor, responda todas as perguntas!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função de validação das respostas
    private fun validarRespostas(): Boolean {
        return !respostaLeitura.isNullOrEmpty() && !respostaDieta.isNullOrEmpty() && !respostaSeguir.isNullOrEmpty()
    }
}