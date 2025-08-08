package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class pergunta01 : AppCompatActivity() {

    // Variáveis globais para armazenar as respostas
    private var praticaAtividade = ""
    private var tempoDisponivel = ""
    private var espacoDisponivel = ""
    private var preferenciaAtividade = ""

    // Variáveis para o tempo
    private var horaSelecionada = -1
    private var minutoSelecionado = -1
    private var horaConfirmada = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pergunta01)

        // Lógica para o botão de Tempo
        val btnPickTime = findViewById<Button>(R.id.btnPickTime)
        btnPickTime.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null)
            val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
            val btnConfirmTime = dialogView.findViewById<Button>(R.id.btnConfirmTime)
            val btnCancelTime = dialogView.findViewById<Button>(R.id.btnCancelTime)

            // Configura o TimePicker para exibir no formato de 24 horas
            timePicker.setIs24HourView(true)

            // Aplica o tema customizado ao AlertDialog
            val alertDialog = AlertDialog.Builder(this, R.style.TimePickerDialogCustom)
                .setView(dialogView)
                .create()

            // Ação para o botão "Confirmar"
            btnConfirmTime.setOnClickListener {
                horaSelecionada = timePicker.hour
                minutoSelecionado = timePicker.minute
                horaConfirmada = "%02d:%02d".format(horaSelecionada, minutoSelecionado)

                // Salva a resposta na variável global
                tempoDisponivel = horaConfirmada

                // Alterando o texto do botão para a hora confirmada
                btnPickTime.text = "Tempo Disponível: $horaConfirmada"
                Toast.makeText(this, "Hora confirmada: $horaConfirmada", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }

            // Ação para o botão "Cancelar"
            btnCancelTime.setOnClickListener {
                alertDialog.dismiss()
            }

            // Exibe o diálogo
            alertDialog.show()
        }

        // Lógica para a pergunta "Pratica alguma atividade?"
        val switchSim = findViewById<Switch>(R.id.switch1sim)
        val switchNao = findViewById<Switch>(R.id.switch2nao)

        switchSim.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                praticaAtividade = "Sim"
                switchNao.isChecked = false // Garantir que apenas um seja marcado
            }
        }

        switchNao.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                praticaAtividade = "Não"
                switchSim.isChecked = false // Garantir que apenas um seja marcado
            }
        }

        // Referências para as checkboxes de espaço e preferências
        val checkBoxNenhum = findViewById<CheckBox>(R.id.checkBoxcasa5nenhum)
        val checkBoxEsporte = findViewById<CheckBox>(R.id.checkBoxcasa2esporte)
        val checkBoxCorrida = findViewById<CheckBox>(R.id.checkBoxcasa3corrida)
        val checkBoxAcademia = findViewById<CheckBox>(R.id.checkBoxcasa4academia)
        val checkBoxOutros = findViewById<CheckBox>(R.id.checkBoxcasa5outros)

        // Lógica para desabilitar as checkboxes de preferência quando "Nenhuma atividade extra" for marcada
        checkBoxNenhum.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Desmarcar e desabilitar as outras checkboxes
                checkBoxEsporte.isChecked = false
                checkBoxCorrida.isChecked = false
                checkBoxAcademia.isChecked = false
                checkBoxOutros.isChecked = false

                checkBoxEsporte.isEnabled = false
                checkBoxCorrida.isEnabled = false
                checkBoxAcademia.isEnabled = false
                checkBoxOutros.isEnabled = false
            } else {
                // Habilitar as checkboxes novamente
                checkBoxEsporte.isEnabled = true
                checkBoxCorrida.isEnabled = true
                checkBoxAcademia.isEnabled = true
                checkBoxOutros.isEnabled = true
            }
        }

        // Lógica para o botão de Avançar
        val btnAvancar = findViewById<Button>(R.id.buttonavancaratividades)
        btnAvancar.setOnClickListener {
            if (validarCampos()) {
                val intent = Intent(this, sujestao::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função para validar as perguntas
    private fun validarCampos(): Boolean {
        // Verificar se foi selecionado "Sim" ou "Não"
        if (praticaAtividade.isEmpty()) {
            Toast.makeText(this, "Você precisa responder se pratica alguma atividade.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verificar se o tempo foi selecionado
        if (tempoDisponivel.isEmpty()) {
            Toast.makeText(this, "Você precisa selecionar o tempo disponível.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verificar se pelo menos uma opção de espaço foi selecionada
        val espacoCheckBoxs = arrayOf(
            findViewById<CheckBox>(R.id.checkBoxcasa),
            findViewById<CheckBox>(R.id.checkBox2academia),
            findViewById<CheckBox>(R.id.checkBox3parque),
            findViewById<CheckBox>(R.id.checkBox4outros)
        )
        espacoDisponivel = ""
        for (checkBox in espacoCheckBoxs) {
            if (checkBox.isChecked) {
                espacoDisponivel = checkBox.text.toString()
                break
            }
        }

        if (espacoDisponivel.isEmpty()) {
            Toast.makeText(this, "Você precisa selecionar pelo menos um espaço disponível.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verificar se pelo menos uma preferência foi selecionada
        val preferenciaCheckBoxs = arrayOf(
            findViewById<CheckBox>(R.id.checkBoxcasa2esporte),
            findViewById<CheckBox>(R.id.checkBoxcasa3corrida),
            findViewById<CheckBox>(R.id.checkBoxcasa4academia),
            findViewById<CheckBox>(R.id.checkBoxcasa5outros), // Inclui o "Outros"
            findViewById<CheckBox>(R.id.checkBoxcasa5nenhum)
        )

        preferenciaAtividade = ""
        for (checkBox in preferenciaCheckBoxs) {
            if (checkBox.isChecked) {
                preferenciaAtividade = checkBox.text.toString()
                break
            }
        }

        // Se não for selecionada nenhuma preferência, mostrar a mensagem correta
        if (preferenciaAtividade.isEmpty()) {
            Toast.makeText(this, "Você precisa selecionar pelo menos uma preferência de atividade.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Se todas as validações passarem, retorna true
        return true
    }
}