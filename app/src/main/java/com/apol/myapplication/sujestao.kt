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

        // Referências para as checkboxes
        val checkBoxRespiracao = findViewById<CheckBox>(R.id.checkBoxrespiracao)
        val checkBoxMeditacao = findViewById<CheckBox>(R.id.checkBox2meditacao)
        val checkBoxPodcasts = findViewById<CheckBox>(R.id.checkBox3podcasts)
        val checkBoxExerciciosMentais = findViewById<CheckBox>(R.id.checkBox4exerciciomentais)
        val checkBoxNenhumaAtividade = findViewById<CheckBox>(R.id.checkBox5nenhumaatividade)

        // Armazenando as respostas das checkboxes
        var respostaAtividades: List<String> = listOf()

        // Listener para a checkbox "Nenhuma atividade extra"
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

        // Referência para o botão "Avançar"
        val btnAvancar = findViewById<Button>(R.id.buttonavancarsujestao)
        btnAvancar.setOnClickListener {
            if (validarRespostas(
                    checkBoxRespiracao, checkBoxMeditacao, checkBoxPodcasts, checkBoxExerciciosMentais, checkBoxNenhumaAtividade
                )) {
                val intent = Intent(this, bemvindo::class.java)
                startActivity(intent)
            } else {
                // Exibe mensagem caso não tenha nenhuma checkbox selecionada
                Toast.makeText(this, "Por favor, selecione ao menos uma atividade!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função de validação das respostas
    private fun validarRespostas(
        checkBoxRespiracao: CheckBox, checkBoxMeditacao: CheckBox, checkBoxPodcasts: CheckBox,
        checkBoxExerciciosMentais: CheckBox, checkBoxNenhumaAtividade: CheckBox
    ): Boolean {
        // Se "Nenhuma atividade extra" for marcada, desmarcar e desabilitar as outras checkboxes
        return if (checkBoxNenhumaAtividade.isChecked) {
            true  // Caso "Nenhuma atividade extra" esteja marcada, já é válido
        } else {
            // Validar se pelo menos uma das outras checkboxes foi selecionada
            checkBoxRespiracao.isChecked || checkBoxMeditacao.isChecked || checkBoxPodcasts.isChecked || checkBoxExerciciosMentais.isChecked
        }
    }
}