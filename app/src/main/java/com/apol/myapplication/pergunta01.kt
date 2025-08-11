// Substitua o conteúdo COMPLETO do seu arquivo pergunta01.kt
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

    // Variáveis para armazenar as respostas
    private var praticaAtividade: String? = null
    private var tempoDisponivel: String? = null
    private var espacosDisponiveis = mutableListOf<String>()
    // As preferências de atividade foram movidas para a tela seguinte (sujestao), então não precisamos delas aqui.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pergunta01)

        // --- Referências aos componentes do NOVO LAYOUT ---
        val radioGroupPraticaAtividade = findViewById<RadioGroup>(R.id.radioGroupPraticaAtividade)
        val radioGroupTempo = findViewById<RadioGroup>(R.id.radioGroupTempo)
        val checkCasa = findViewById<CheckBox>(R.id.checkBoxcasa)
        val checkAcademia = findViewById<CheckBox>(R.id.checkBox2academia)
        val checkParque = findViewById<CheckBox>(R.id.checkBox3parque)
        val btnAvancar = findViewById<Button>(R.id.buttonavancaratividades)

        // --- Lógica do botão de avançar ---
        btnAvancar.setOnClickListener {
            // 1. Lê as respostas dos RadioGroups
            val idPraticaSelecionado = radioGroupPraticaAtividade.checkedRadioButtonId
            val idTempoSelecionado = radioGroupTempo.checkedRadioButtonId

            if (idPraticaSelecionado != -1) {
                praticaAtividade = findViewById<RadioButton>(idPraticaSelecionado).text.toString()
            }
            if (idTempoSelecionado != -1) {
                tempoDisponivel = findViewById<RadioButton>(idTempoSelecionado).text.toString()
            }

            // 2. Lê as respostas das CheckBoxes de espaço
            espacosDisponiveis.clear()
            if (checkCasa.isChecked) espacosDisponiveis.add("Casa")
            if (checkAcademia.isChecked) espacosDisponiveis.add("Academia")
            if (checkParque.isChecked) espacosDisponiveis.add("Parque / Área aberta")

            // 3. Valida se tudo foi preenchido
            if (validarCampos()) {
                // Se tudo estiver OK, você pode salvar os dados aqui
                // Exemplo:
                // salvarRespostas(praticaAtividade, tempoDisponivel, espacosDisponiveis)

                // E então navega para a próxima tela
                startActivity(Intent(this, sujestao::class.java))
                finish()
            }
            // A função validarCampos() já mostra os Toasts de erro
        }
    }

    // Função de validação adaptada para os novos componentes
    private fun validarCampos(): Boolean {
        if (praticaAtividade == null) {
            Toast.makeText(this, "Responda se pratica alguma atividade.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (tempoDisponivel == null) {
            Toast.makeText(this, "Selecione o tempo disponível.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (espacosDisponiveis.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos um espaço disponível.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // Futuramente, você pode implementar a função de salvamento
    // private fun salvarRespostas(pratica: String?, tempo: String?, espacos: List<String>) {
    //     // Lógica para salvar os dados em SharedPreferences ou no banco de dados (Room)
    // }
}