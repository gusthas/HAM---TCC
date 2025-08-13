// Substitua o conteúdo COMPLETO do seu arquivo sujestao.kt
package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class sujestao : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sujestao)

        val checkBoxRespiracao = findViewById<CheckBox>(R.id.checkBoxrespiracao)
        val checkBoxMeditacao = findViewById<CheckBox>(R.id.checkBox2meditacao)
        val checkBoxPodcasts = findViewById<CheckBox>(R.id.checkBox3podcasts)
        val checkBoxExerciciosMentais = findViewById<CheckBox>(R.id.checkBox4exerciciomentais)
        val checkBoxNenhumaAtividade = findViewById<CheckBox>(R.id.checkBox5nenhumaatividade)

        val cardRespiracao = findViewById<LinearLayout>(R.id.card_respiracao)
        val cardMeditacao = findViewById<LinearLayout>(R.id.card_meditacao)
        val cardPodcasts = findViewById<LinearLayout>(R.id.card_podcasts)
        val cardExerciciosMentais = findViewById<LinearLayout>(R.id.card_exercicios_mentais)
        val cardNenhuma = findViewById<LinearLayout>(R.id.card_nenhuma)

        val btnAvancar = findViewById<Button>(R.id.buttonavancarsujestao)

        val checkBoxesAtividades = listOf(checkBoxRespiracao, checkBoxMeditacao, checkBoxPodcasts, checkBoxExerciciosMentais)

        cardRespiracao.setOnClickListener { checkBoxRespiracao.isChecked = !checkBoxRespiracao.isChecked }
        cardMeditacao.setOnClickListener { checkBoxMeditacao.isChecked = !checkBoxMeditacao.isChecked }
        cardPodcasts.setOnClickListener { checkBoxPodcasts.isChecked = !checkBoxPodcasts.isChecked }
        cardExerciciosMentais.setOnClickListener { checkBoxExerciciosMentais.isChecked = !checkBoxExerciciosMentais.isChecked }
        cardNenhuma.setOnClickListener { checkBoxNenhumaAtividade.isChecked = !checkBoxNenhumaAtividade.isChecked }

        checkBoxNenhumaAtividade.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBoxesAtividades.forEach { it.isChecked = false; it.isEnabled = false }
            } else {
                checkBoxesAtividades.forEach { it.isEnabled = true }
            }
        }

        checkBoxesAtividades.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) checkBoxNenhumaAtividade.isChecked = false
            }
        }

        btnAvancar.setOnClickListener {
            if (validarRespostas(checkBoxesAtividades, checkBoxNenhumaAtividade)) {
                // --- SALVANDO AS ESCOLHAS DO USUÁRIO ---
                val prefs = getSharedPreferences("user_onboarding_prefs", MODE_PRIVATE).edit()

                prefs.putBoolean("mostrar_card_respiracao", checkBoxRespiracao.isChecked)
                prefs.putBoolean("mostrar_card_meditacao", checkBoxMeditacao.isChecked)
                prefs.putBoolean("mostrar_card_podcasts", checkBoxPodcasts.isChecked)
                prefs.putBoolean("mostrar_card_exercicios", checkBoxExerciciosMentais.isChecked)

                prefs.apply()

                // O fluxo correto é ir para a tela de boas-vindas
                val intent = Intent(this, Bemvindouser::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Por favor, selecione ao menos uma opção!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validarRespostas(atividades: List<CheckBox>, nenhuma: CheckBox): Boolean {
        return nenhuma.isChecked || atividades.any { it.isChecked }
    }
}