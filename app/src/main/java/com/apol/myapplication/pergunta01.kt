package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class pergunta01 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pergunta01)

        val radioGroupPraticaAtividade = findViewById<RadioGroup>(R.id.radioGroupPraticaAtividade)
        val radioGroupTempo = findViewById<RadioGroup>(R.id.radioGroupTempo)
        val checkCasa = findViewById<CheckBox>(R.id.checkBoxcasa)
        val checkAcademia = findViewById<CheckBox>(R.id.checkBox2academia)
        val checkParque = findViewById<CheckBox>(R.id.checkBox3parque)
        val btnAvancar = findViewById<Button>(R.id.buttonavancaratividades)

        btnAvancar.setOnClickListener {
            val idPratica = radioGroupPraticaAtividade.checkedRadioButtonId
            val idTempo = radioGroupTempo.checkedRadioButtonId

            // Valida se as perguntas de rádio foram respondidas
            if (idPratica == -1 || idTempo == -1) {
                Toast.makeText(this, "Por favor, responda todas as perguntas.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Valida se pelo menos um espaço foi selecionado
            if (!checkCasa.isChecked && !checkAcademia.isChecked && !checkParque.isChecked) {
                Toast.makeText(this, "Selecione pelo menos um espaço disponível.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- SALVANDO AS RESPOSTAS ---
            val prefs = getSharedPreferences("user_onboarding_prefs", MODE_PRIVATE).edit()

            // Pega os textos das respostas
            val praticaAtividade = findViewById<RadioButton>(idPratica).text.toString()
            val tempoDisponivel = findViewById<RadioButton>(idTempo).text.toString()
            val espacos = mutableSetOf<String>()
            if (checkCasa.isChecked) espacos.add("Casa")
            if (checkAcademia.isChecked) espacos.add("Academia")
            if (checkParque.isChecked) espacos.add("Parque")

            // Salva no SharedPreferences
            prefs.putString("resposta_pratica_atividade", praticaAtividade)
            prefs.putString("resposta_tempo_disponivel", tempoDisponivel)
            prefs.putStringSet("resposta_espacos", espacos)
            prefs.putBoolean("sugestao_treino_criada", false) // Flag para criar o treino apenas uma vez

            prefs.apply()

            // Navega para a próxima tela
            startActivity(Intent(this, sujestao::class.java))
            finish()
        }
    }
}