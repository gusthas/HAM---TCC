// Substitua o conteúdo COMPLETO do seu arquivo sujestao.kt
package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class sujestao : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sujestao)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Referências para as Views ---
        // Checkboxes (como você já tinha)
        val checkBoxRespiracao = findViewById<CheckBox>(R.id.checkBoxrespiracao)
        val checkBoxMeditacao = findViewById<CheckBox>(R.id.checkBox2meditacao)
        val checkBoxPodcasts = findViewById<CheckBox>(R.id.checkBox3podcasts)
        val checkBoxExerciciosMentais = findViewById<CheckBox>(R.id.checkBox4exerciciomentais)
        val checkBoxNenhumaAtividade = findViewById<CheckBox>(R.id.checkBox5nenhumaatividade)

        // Cards clicáveis (NOVO)
        val cardRespiracao = findViewById<LinearLayout>(R.id.card_respiracao)
        val cardMeditacao = findViewById<LinearLayout>(R.id.card_meditacao)
        val cardPodcasts = findViewById<LinearLayout>(R.id.card_podcasts)
        val cardExerciciosMentais = findViewById<LinearLayout>(R.id.card_exercicios_mentais)
        val cardNenhuma = findViewById<LinearLayout>(R.id.card_nenhuma)

        // Botão de avançar
        val btnAvancar = findViewById<Button>(R.id.buttonavancarsujestao)

        // --- Lógica de Interação ---

        // NOVO: Faz com que clicar no card marque/desmarque o CheckBox correspondente
        cardRespiracao.setOnClickListener { checkBoxRespiracao.isChecked = !checkBoxRespiracao.isChecked }
        cardMeditacao.setOnClickListener { checkBoxMeditacao.isChecked = !checkBoxMeditacao.isChecked }
        cardPodcasts.setOnClickListener { checkBoxPodcasts.isChecked = !checkBoxPodcasts.isChecked }
        cardExerciciosMentais.setOnClickListener { checkBoxExerciciosMentais.isChecked = !checkBoxExerciciosMentais.isChecked }
        cardNenhuma.setOnClickListener { checkBoxNenhumaAtividade.isChecked = !checkBoxNenhumaAtividade.isChecked }

        // MANTIDO: Sua lógica inteligente para a opção "Nenhuma"
        checkBoxNenhumaAtividade.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Desmarcar e desabilitar as outras checkboxes
                checkBoxRespiracao.isChecked = false
                checkBoxMeditacao.isChecked = false
                checkBoxPodcasts.isChecked = false
                checkBoxExerciciosMentais.isChecked = false
                checkBoxRespiracao.isEnabled = false
                checkBoxMeditacao.isEnabled = false
                checkBoxPodcasts.isEnabled = false
                checkBoxExerciciosMentais.isEnabled = false
            } else {
                // Habilitar as outras checkboxes novamente
                checkBoxRespiracao.isEnabled = true
                checkBoxMeditacao.isEnabled = true
                checkBoxPodcasts.isEnabled = true
                checkBoxExerciciosMentais.isEnabled = true
            }
        }

        // MANTIDO: Sua lógica de validação no botão de avançar
        btnAvancar.setOnClickListener {
            if (validarRespostas(
                    checkBoxRespiracao, checkBoxMeditacao, checkBoxPodcasts, checkBoxExerciciosMentais, checkBoxNenhumaAtividade
                )) {

                // AQUI você pode salvar as respostas do usuário antes de avançar

                val intent = Intent(this, Bemvindouser::class.java)
                startActivity(intent)
                finish() // Adicionado para não permitir voltar a esta tela
            } else {
                Toast.makeText(this, "Por favor, selecione ao menos uma opção!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // MANTIDO: Sua função de validação, está perfeita
    private fun validarRespostas(
        checkBoxRespiracao: CheckBox, checkBoxMeditacao: CheckBox, checkBoxPodcasts: CheckBox,
        checkBoxExerciciosMentais: CheckBox, checkBoxNenhumaAtividade: CheckBox
    ): Boolean {
        return if (checkBoxNenhumaAtividade.isChecked) {
            true
        } else {
            checkBoxRespiracao.isChecked || checkBoxMeditacao.isChecked || checkBoxPodcasts.isChecked || checkBoxExerciciosMentais.isChecked
        }
    }
}