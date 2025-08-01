package com.apol.myapplication

import NotesAdapter
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.app.PendingIntent
import android.provider.Settings
import com.google.android.play.core.integrity.al

class anotacoes : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_SELECIONAR_BLOCO = 1001
    }

    private lateinit var viewModel: NotesViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var blocosAdapter: BlocosAdapter

    private val selectedNotes = mutableSetOf<Note>()
    private val blocosSelecionados = mutableSetOf<Bloco>() // Mutable - coleção genérica não ordenada de elementos que não suporta elementos duplicados e suporta a adição e remoção de elementeos

    private var modoExclusaoAtivo = false
    private var modoExclusaoBlocosAtivo = false
    private var modoBlocosAtivo = false

    private lateinit var botaoApagar: ImageButton
    private lateinit var clickOutsideView: View
    private lateinit var editNote: EditText
    private lateinit var buttonAdd: ImageButton
    private lateinit var newNoteBox: LinearLayout
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anotacoes)
        criarCanalNotificacao()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // NavBar - config de botões (exemplo, adapte se precisar)
        val navBar = findViewById<LinearLayout>(R.id.navigation_bar)
        navBar.findViewById<LinearLayout>(R.id.botao_inicio).setOnClickListener {
            startActivity(Intent(this, Bemvindouser::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_anotacoes).setOnClickListener { /* já está aqui */ }
        navBar.findViewById<LinearLayout>(R.id.botao_habitos).setOnClickListener {
            startActivity(Intent(this, habitos::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_treinos).setOnClickListener {
            startActivity(Intent(this, treinos::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_progresso).setOnClickListener {
            startActivity(Intent(this, progresso::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_configuracoes).setOnClickListener {
            startActivity(Intent(this, configuracoes::class.java))
        }
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        val abrirModoBlocos = intent.getBooleanExtra("modo_blocos_ativo", false)
        val abrirSelecaoBlocos = intent.getBooleanExtra("abrir_selecao_blocos", false)
        if (abrirModoBlocos) {
            modoBlocosAtivo = true
            prefs.edit().putBoolean("modo_blocos_ativo", true).apply()
        }

        if (abrirModoBlocos && abrirSelecaoBlocos) {
            // Executa esse trecho depois que a interface for montada
            window.decorView.post {
                val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_selecionar_bloco, null)

                val dialog = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .create()

                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                val adicionarBloco: (String) -> Unit = { nomeBloco ->
                    viewModel.adicionarBloco(Bloco(nome = nomeBloco))
                    dialog.dismiss()
                }

                dialogView.findViewById<Button>(R.id.bt_financas).setOnClickListener { adicionarBloco("Finanças") }
                dialogView.findViewById<Button>(R.id.bt_estudos).setOnClickListener { adicionarBloco("Estudos") }
                dialogView.findViewById<Button>(R.id.bt_metas).setOnClickListener { adicionarBloco("Metas") }
                dialogView.findViewById<Button>(R.id.bt_trabalho).setOnClickListener { adicionarBloco("Trabalho") }
                dialogView.findViewById<Button>(R.id.bt_saude).setOnClickListener { adicionarBloco("Saúde") }

                dialog.show()
            }
        }


        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        modoBlocosAtivo = prefs.getBoolean("modo_blocos_ativo", false)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NotesViewModel::class.java)

        recyclerView = findViewById(R.id.recyclerViewNotes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        notesAdapter = NotesAdapter(
            onItemClick = { note ->
                if (modoExclusaoAtivo) toggleSelection(note)
                else openEditDialog(note)
            },
            onItemLongClick = { note ->
                if (!modoExclusaoAtivo) ativarModoExclusao()
                toggleSelection(note)
            }
        )

        blocosAdapter = BlocosAdapter(
            onItemClick = { bloco ->
                if (modoExclusaoBlocosAtivo) {
                    if (blocosSelecionados.contains(bloco)) blocosSelecionados.remove(bloco)
                    else blocosSelecionados.add(bloco)

                    atualizarBotaoApagar()
                } else {
                    abrirDialogEditarBloco(bloco)

                }
            },
            onItemLongClick = { bloco ->
                if (!modoExclusaoBlocosAtivo) ativarModoExclusaoBlocos()
                blocosSelecionados.add(bloco)

                atualizarBotaoApagar()
            }
        )

        blocosAdapter.onExclusaoModoVazio = {
            desativarModoExclusaoBlocos()
            esconderBotaoApagar()
        }

        editNote = findViewById(R.id.edit_note)
        buttonAdd = findViewById(R.id.button_add_note)
        newNoteBox = findViewById(R.id.new_note_box)

        buttonAdd.setOnClickListener {
            if (modoBlocosAtivo) {
                val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_selecionar_bloco, null)

                val dialog = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .create()

                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                val adicionarBloco: (String) -> Unit = { nomeBloco ->
                    viewModel.adicionarBloco(Bloco(nome = nomeBloco))
                    dialog.dismiss()
                }

                dialogView.findViewById<Button>(R.id.bt_financas).setOnClickListener { adicionarBloco("Finanças") }
                dialogView.findViewById<Button>(R.id.bt_estudos).setOnClickListener { adicionarBloco("Estudos") }
                dialogView.findViewById<Button>(R.id.bt_metas).setOnClickListener { adicionarBloco("Metas") }
                dialogView.findViewById<Button>(R.id.bt_trabalho).setOnClickListener { adicionarBloco("Trabalho") }
                dialogView.findViewById<Button>(R.id.bt_saude).setOnClickListener { adicionarBloco("Saúde") }

                dialog.show()
            } else {
                val text = editNote.text.toString().trim()
                if (text.isNotEmpty()) {
                    viewModel.addNote(text)
                    editNote.text.clear()
                    recyclerView.scrollToPosition(0)
                }
            }
        }

        botaoApagar = findViewById(R.id.button_delete_selected)

        botaoApagar.setOnClickListener {
            if (modoBlocosAtivo) {
                val selecionados = blocosAdapter.getSelecionados()
                if (selecionados.isNotEmpty()) {
                    confirmarExclusao(selecionados.size, "bloco(s)") {
                        viewModel.deleteBlocos(selecionados)
                        blocosAdapter.limparSelecao()
                        desativarModoExclusaoBlocos()
                    }
                }
            } else {
                val selecionados = notesAdapter.getSelecionados()
                if (selecionados.isNotEmpty()) {
                    confirmarExclusao(selecionados.size, "anotação(ões)") {
                        viewModel.deleteNotes(selecionados)
                        notesAdapter.limparSelecao()
                        desativarModoExclusao()
                    }
                }
            }
        }





        botaoApagar.visibility = View.GONE

        clickOutsideView = findViewById(R.id.click_outside_view)
        clickOutsideView.setOnClickListener {
            if (modoExclusaoAtivo) desativarModoExclusao()
            if (modoExclusaoBlocosAtivo) desativarModoExclusaoBlocos()
        }

        val botaoToggleModo = findViewById<ImageButton>(R.id.button_toggle_mode)
        botaoToggleModo.setOnClickListener {
            modoBlocosAtivo = !modoBlocosAtivo
            prefs.edit().putBoolean("modo_blocos_ativo", modoBlocosAtivo).apply()
            atualizarModoUI()
        }

        atualizarModoUI()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão de notificação concedida", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissão de notificação negada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleSelection(note: Note) {
        note.isSelected = !note.isSelected
        notesAdapter.notifyDataSetChanged()
        atualizarBotaoApagar()
        if (notesAdapter.getSelecionados().isEmpty()) desativarModoExclusao()
    }


    private fun ativarModoExclusao() {
        modoExclusaoAtivo = true
        notesAdapter.modoExclusaoAtivo = true
        notesAdapter.notifyDataSetChanged()
        mostrarBotaoApagar()
    }

    private fun desativarModoExclusao() {
        modoExclusaoAtivo = false
        notesAdapter.limparSelecao()
        esconderBotaoApagar()
    }


    private fun ativarModoExclusaoBlocos() {
        modoExclusaoBlocosAtivo = true
        blocosAdapter.modoExclusaoAtivo = true
        blocosAdapter.notifyDataSetChanged()
        mostrarBotaoApagar()
    }

    private fun desativarModoExclusaoBlocos() {
        modoExclusaoBlocosAtivo = false
        blocosAdapter.limparSelecao()
        esconderBotaoApagar()
    }


    private fun mostrarBotaoApagar() {
        botaoApagar.visibility = View.VISIBLE
    }

    private fun esconderBotaoApagar() {
        botaoApagar.visibility = View.GONE
    }

    private fun atualizarBotaoApagar() {
        val count = if (modoBlocosAtivo) blocosAdapter.getSelecionados().size else notesAdapter.getSelecionados().size
        botaoApagar.isEnabled = count > 0
        botaoApagar.visibility = if (count > 0) View.VISIBLE else View.GONE
        botaoApagar.contentDescription = "Apagar selecionados ($count)"
    }


    private fun carregarListaAnotacoes() {
        lifecycleScope.launch {
            viewModel.notes.collectLatest { notes ->
                notesAdapter.submitList(notes)
            }
        }
    }

    private fun carregarListaBlocos() {
        lifecycleScope.launch {
            viewModel.blocos.collectLatest { blocos ->
                blocosAdapter.submitList(blocos)
            }
        }
    }

    private fun openEditDialog(note: Note) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_note, null)
        val editText = dialogView.findViewById<EditText>(R.id.editNoteDialog)
        editText.setText(note.text)

        AlertDialog.Builder(this)
            .setTitle("Editar Anotação")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    val updatedNote = note.copy(text = newText, lastModified = System.currentTimeMillis())
                    viewModel.updateNote(updatedNote)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun atualizarModoUI() {
        val titulo = findViewById<TextView>(R.id.textView26)
        val iconeTitulo = findViewById<ImageView>(R.id.icon_header)

        if (modoBlocosAtivo) {
            titulo.text = "Meus Blocos"
            iconeTitulo.setImageResource(R.drawable.ic_block)
            newNoteBox.visibility = View.VISIBLE
            editNote.isEnabled = false
            editNote.hint = "Adicionar novo bloco"
            recyclerView.adapter = blocosAdapter
            carregarListaBlocos()
        } else {
            titulo.text = "Minhas Anotações"
            iconeTitulo.setImageResource(R.drawable.ic_notes)
            newNoteBox.visibility = View.VISIBLE
            editNote.isEnabled = true
            editNote.hint = "O que deseja anotar?"
            recyclerView.adapter = notesAdapter
            carregarListaAnotacoes()
        }

        // Limpa seleções e modos exclusão
        desativarModoExclusao()
        desativarModoExclusaoBlocos()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECIONAR_BLOCO && resultCode == RESULT_OK) {
            val nomeBloco = data?.getStringExtra("blocoSelecionado")
            nomeBloco?.let {
                viewModel.adicionarBloco(Bloco(nome = it))
            }
        }
    }
    private fun confirmarExclusao(quantidade: Int, tipo: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Excluir $quantidade $tipo?")
            .setMessage("Tem certeza que deseja apagar?")
            .setPositiveButton("Excluir") { _, _ -> onConfirm() }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    // Dentro da classe anotacoes

    // SUBSTITUA A FUNÇÃO ANTIGA
    private fun abrirDialogEditarBloco(bloco: Bloco) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_bloco, null)

        // Referências
        val tituloBloco = dialogView.findViewById<TextView>(R.id.titulo_bloco)
        val inputSubtitulo = dialogView.findViewById<EditText>(R.id.input_subtitulo)
        val inputAnotacoes = dialogView.findViewById<EditText>(R.id.input_anotacoes)
        val inputMensagemNotificacao = dialogView.findViewById<EditText>(R.id.input_mensagem_notificacao)
        val btnConfigurarLembrete = dialogView.findViewById<Button>(R.id.btn_configurar_lembrete)
        val btnCancelar = dialogView.findViewById<Button>(R.id.botao_cancelar)
        val btnSalvar = dialogView.findViewById<Button>(R.id.botao_salvar)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Preenche com os dados atuais
        val blocoTemporario = bloco.copy() // Trabalha com uma cópia para não alterar o original até salvar
        tituloBloco.text = blocoTemporario.nome
        inputSubtitulo.setText(blocoTemporario.subtitulo)
        inputAnotacoes.setText(blocoTemporario.anotacao)
        inputMensagemNotificacao.setText(blocoTemporario.mensagemNotificacao)

        // Ação do botão de configurar lembrete
        btnConfigurarLembrete.setOnClickListener {
            abrirDialogConfigurarLembrete(blocoTemporario) { configAlterada ->
                // Callback: Quando o lembrete for salvo no outro dialog, atualizamos a cópia temporária
                blocoTemporario.tipoLembrete = configAlterada.tipoLembrete
                blocoTemporario.diasLembrete = configAlterada.diasLembrete
                blocoTemporario.horariosLembrete = configAlterada.horariosLembrete
                blocoTemporario.segundosLembrete = configAlterada.segundosLembrete
                Toast.makeText(this, "Configuração de lembrete pronta para salvar.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnSalvar.setOnClickListener {
            // Aplica as mudanças da cópia temporária para o objeto original
            bloco.subtitulo = inputSubtitulo.text.toString().trim()
            bloco.anotacao = inputAnotacoes.text.toString().trim()
            bloco.mensagemNotificacao = inputMensagemNotificacao.text.toString().trim()
            bloco.tipoLembrete = blocoTemporario.tipoLembrete
            bloco.diasLembrete = blocoTemporario.diasLembrete
            bloco.horariosLembrete = blocoTemporario.horariosLembrete
            bloco.segundosLembrete = blocoTemporario.segundosLembrete

            viewModel.updateBloco(bloco)

            // Cancela agendamentos antigos e agenda os novos
            cancelarLembretesParaBloco(bloco)
            if (bloco.tipoLembrete != TipoLembrete.NENHUM) {
                agendarLembretesParaBloco(bloco)
            } else {
                Toast.makeText(this, "Lembrete removido.", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }
        dialog.show()
    }

    // SUBSTITUA A FUNÇÃO ANTIGA
    private fun abrirDialogConfigurarLembrete(bloco: Bloco, onSave: (Bloco) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_configurar_lembrete, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Referências
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radio_group_tipo_lembrete)
        val radioDiario = dialogView.findViewById<RadioButton>(R.id.radio_diario)
        val radioMensal = dialogView.findViewById<RadioButton>(R.id.radio_mensal)
        val radioSegundosTeste = dialogView.findViewById<RadioButton>(R.id.radio_segundos_teste)
        val layoutDias = dialogView.findViewById<View>(R.id.layout_input_dias)
        val inputDias = dialogView.findViewById<EditText>(R.id.input_dias_lembrete)
        val layoutHorarios = dialogView.findViewById<View>(R.id.layout_input_horarios)
        val inputHorarios = dialogView.findViewById<EditText>(R.id.input_horarios_lembrete)
        val layoutSegundos = dialogView.findViewById<View>(R.id.layout_input_segundos)
        val inputSegundos = dialogView.findViewById<EditText>(R.id.input_segundos_lembrete)

        fun updateUi(tipo: TipoLembrete) {
            layoutDias.visibility = if (tipo == TipoLembrete.MENSAL) View.VISIBLE else View.GONE
            layoutHorarios.visibility = if (tipo == TipoLembrete.DIARIO || tipo == TipoLembrete.MENSAL) View.VISIBLE else View.GONE
            layoutSegundos.visibility = if (tipo == TipoLembrete.SEGUNDOS_TESTE) View.VISIBLE else View.GONE
        }

        // Preencher com dados existentes
        when (bloco.tipoLembrete) {
            TipoLembrete.DIARIO -> radioDiario.isChecked = true
            TipoLembrete.MENSAL -> radioMensal.isChecked = true
            TipoLembrete.SEGUNDOS_TESTE -> radioSegundosTeste.isChecked = true
            else -> { /* Nenhum selecionado */ }
        }
        updateUi(bloco.tipoLembrete)
        inputDias.setText(bloco.diasLembrete.joinToString(", "))
        inputHorarios.setText(bloco.horariosLembrete.joinToString(", "))
        inputSegundos.setText(bloco.segundosLembrete?.toString() ?: "")

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_diario -> updateUi(TipoLembrete.DIARIO)
                R.id.radio_mensal -> updateUi(TipoLembrete.MENSAL)
                R.id.radio_segundos_teste -> updateUi(TipoLembrete.SEGUNDOS_TESTE)
            }
        }

        // Botão Salvar
        dialogView.findViewById<Button>(R.id.btn_salvar_lembrete).setOnClickListener {
            val blocoConfigurado = bloco.copy() // Cria uma cópia para passar de volta

            when (radioGroup.checkedRadioButtonId) {
                R.id.radio_diario -> {
                    blocoConfigurado.tipoLembrete = TipoLembrete.DIARIO
                    blocoConfigurado.diasLembrete = emptyList()
                    blocoConfigurado.segundosLembrete = null
                }
                R.id.radio_mensal -> {
                    blocoConfigurado.tipoLembrete = TipoLembrete.MENSAL
                    blocoConfigurado.diasLembrete = inputDias.text.toString()
                        .split(',')
                        .mapNotNull { it.trim().toIntOrNull()?.coerceIn(1, 31) }
                    blocoConfigurado.segundosLembrete = null
                }
                R.id.radio_segundos_teste -> {
                    blocoConfigurado.tipoLembrete = TipoLembrete.SEGUNDOS_TESTE
                    blocoConfigurado.segundosLembrete = inputSegundos.text.toString().toLongOrNull()
                    blocoConfigurado.diasLembrete = emptyList()
                    blocoConfigurado.horariosLembrete = emptyList()
                }
                else -> blocoConfigurado.tipoLembrete = TipoLembrete.NENHUM
            }

            if (blocoConfigurado.tipoLembrete == TipoLembrete.DIARIO || blocoConfigurado.tipoLembrete == TipoLembrete.MENSAL) {
                blocoConfigurado.horariosLembrete = inputHorarios.text.toString()
                    .split(',')
                    .map { it.trim() }
                    .filter { it.matches(Regex("\\d{1,2}:\\d{2}")) } // Valida formato HH:mm
            }

            onSave(blocoConfigurado) // Chama o callback com a nova configuração
            dialog.dismiss()
        }

        // Botão Remover
        dialogView.findViewById<Button>(R.id.btn_remover_lembrete).setOnClickListener {
            val blocoLimpo = bloco.copy(
                tipoLembrete = TipoLembrete.NENHUM,
                diasLembrete = emptyList(),
                horariosLembrete = emptyList(),
                segundosLembrete = null
            )
            onSave(blocoLimpo)
            dialog.dismiss()
        }

        dialog.show()
    }

    // SUBSTITUA A FUNÇÃO ANTIGA (ESTA É A CORREÇÃO MAIS IMPORTANTE)
    private fun agendarLembretesParaBloco(bloco: Bloco) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val context = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, "Permissão para agendar alarmes exatos é necessária.", Toast.LENGTH_LONG).show()
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also { startActivity(it) }
            return
        }

        var requestCodeCounter = 0 // Para garantir que cada alarme do mesmo bloco seja único

        when (bloco.tipoLembrete) {
            TipoLembrete.SEGUNDOS_TESTE -> {
                val segundos = bloco.segundosLembrete ?: return
                if (segundos <= 0) {
                    Toast.makeText(context, "Insira segundos válidos para o teste.", Toast.LENGTH_SHORT).show()
                    return
                }
                val triggerAtMillis = System.currentTimeMillis() + segundos * 1000
                val pendingIntent = getPendingIntent(context, bloco, bloco.id.hashCode())
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                Toast.makeText(this, "Lembrete de teste agendado para daqui a $segundos segundos!", Toast.LENGTH_SHORT).show()
            }

            TipoLembrete.DIARIO -> {
                if (bloco.horariosLembrete.isEmpty()) return
                bloco.horariosLembrete.forEach { horarioStr ->
                    val (hora, minuto) = parseHorario(horarioStr) ?: return@forEach
                    val calendar = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, hora)
                        set(java.util.Calendar.MINUTE, minuto)
                        set(java.util.Calendar.SECOND, 0)
                        if (before(java.util.Calendar.getInstance())) {
                            add(java.util.Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                    val pendingIntent = getPendingIntent(context, bloco, bloco.id.hashCode() + requestCodeCounter++)
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
                }
                Toast.makeText(this, "Lembrete(s) diário(s) agendado(s)!", Toast.LENGTH_SHORT).show()
            }

            TipoLembrete.MENSAL -> {
                if (bloco.diasLembrete.isEmpty() || bloco.horariosLembrete.isEmpty()) return
                bloco.diasLembrete.forEach { dia ->
                    bloco.horariosLembrete.forEach { horarioStr ->
                        val (hora, minuto) = parseHorario(horarioStr) ?: return@forEach
                        val proximoAgendamento = getNextMonthlyOccurrence(dia, hora, minuto)
                        proximoAgendamento?.let { calendar ->
                            val pendingIntent = getPendingIntent(context, bloco, bloco.id.hashCode() + requestCodeCounter++)
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                        }
                    }
                }
                Toast.makeText(this, "Lembrete(s) mensal(is) agendado(s)!", Toast.LENGTH_SHORT).show()
            }
            else -> {} // TipoLembrete.NENHUM
        }
    }

    // ADICIONE ESTA FUNÇÃO AUXILIAR para parsear o horário
    private fun parseHorario(horarioStr: String): Pair<Int, Int>? {
        val parts = horarioStr.split(":").mapNotNull { it.toIntOrNull() }
        return if (parts.size == 2) parts[0] to parts[1] else null
    }

// Adicione estas duas funções dentro da sua classe 'anotacoes'

    private fun getPendingIntent(context: Context, bloco: Bloco, requestCode: Int): PendingIntent {
        val intent = Intent(context, BlocoNotificationReceiver::class.java).apply {
            // Informações que o Receiver usará para criar a notificação
            putExtra("titulo", bloco.nome + if (bloco.subtitulo.isNotEmpty()) " - ${bloco.subtitulo}" else "")
            putExtra("mensagem", bloco.mensagemNotificacao.ifEmpty { "Você tem um lembrete para este bloco." })
            putExtra("bloco_id", bloco.id) // ID para identificação
        }

        // O requestCode deve ser único para cada alarme
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun cancelarLembretesParaBloco(bloco: Bloco) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // É difícil saber exatamente quantos alarmes foram criados,
        // então cancelamos uma faixa de possíveis request codes.
        // 31 dias * 24 horários = 744. Cancelar 1000 por segurança é uma boa abordagem.
        for (i in 0 until 1000) {
            val requestCode = bloco.id.hashCode() + i
            val pendingIntent = getPendingIntent(this, bloco, requestCode)
            alarmManager.cancel(pendingIntent)
        }
    }

    // Função auxiliar para encontrar a próxima ocorrência mensal válida
    private fun getNextMonthlyOccurrence(dia: Int, hora: Int, minuto: Int): java.util.Calendar? {
        val agora = java.util.Calendar.getInstance()
        var proximoAgendamento = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hora)
            set(java.util.Calendar.MINUTE, minuto)
            set(java.util.Calendar.SECOND, 0)
        }

        // Tenta no mês atual
        val maxDayOfMonth = proximoAgendamento.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val diaValido = if (dia > maxDayOfMonth) maxDayOfMonth else dia
        proximoAgendamento.set(java.util.Calendar.DAY_OF_MONTH, diaValido)

        if (proximoAgendamento.after(agora)) {
            return proximoAgendamento // Encontrou no mês atual
        }

        // Se não, tenta no próximo mês
        proximoAgendamento.add(java.util.Calendar.MONTH, 1)
        val maxDayOfNextMonth = proximoAgendamento.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val diaValidoProximoMes = if (dia > maxDayOfNextMonth) maxDayOfNextMonth else dia
        proximoAgendamento.set(java.util.Calendar.DAY_OF_MONTH, diaValidoProximoMes)

        return proximoAgendamento
    }
    private fun criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "canal_lembrete",
                "Lembretes",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }


}


