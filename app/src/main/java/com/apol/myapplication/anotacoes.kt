package com.apol.myapplication

import NotesAdapter
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.data.model.Bloco
import com.apol.myapplication.data.model.Note
import com.apol.myapplication.data.model.TipoLembrete
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class anotacoes : AppCompatActivity() {

    private lateinit var viewModel: NotesViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var blocosAdapter: BlocosAdapter

    private var modoExclusaoAtivo = false
    private var modoExclusaoBlocosAtivo = false
    private var modoBlocosAtivo = false

    private lateinit var botaoApagar: ImageButton
    private lateinit var clickOutsideView: View
    private lateinit var editNote: EditText
    private lateinit var buttonAdd: ImageButton
    private lateinit var prefs: android.content.SharedPreferences
    private var emailUsuarioLogado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anotacoes)
    /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        emailUsuarioLogado = prefs.getString("LOGGED_IN_USER_EMAIL", null)

        if (emailUsuarioLogado == null) {
            Toast.makeText(this, "Erro de sessão. Faça login novamente.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        viewModel.setCurrentUser(emailUsuarioLogado!!)

        recyclerView = findViewById(R.id.recyclerViewNotes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        editNote = findViewById(R.id.edit_note)
        buttonAdd = findViewById(R.id.button_add_note)
        botaoApagar = findViewById(R.id.button_delete_selected)
        clickOutsideView = findViewById(R.id.click_outside_view)

        setupAdapters()
        setupListeners()

        modoBlocosAtivo = prefs.getBoolean("modo_blocos_ativo_${emailUsuarioLogado}", false)
        atualizarModoUI()
        verificarComandosDeEntrada()
        criarCanalNotificacao()
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    private fun setupAdapters() {
        notesAdapter = NotesAdapter(
            onItemClick = { note ->
                if (modoExclusaoAtivo) toggleSelection(note) else openEditDialog(note)
            },
            onItemLongClick = { note ->
                if (!modoExclusaoAtivo) ativarModoExclusao()
                toggleSelection(note)
            }
        )

        blocosAdapter = BlocosAdapter(
            onItemClick = { bloco ->
                if (modoExclusaoBlocosAtivo) {
                    bloco.isSelected = !bloco.isSelected
                    blocosAdapter.notifyDataSetChanged()
                    atualizarBotaoApagar()
                } else {
                    abrirDialogEditarBloco(bloco)
                }
            },
            onItemLongClick = { bloco ->
                if (!modoExclusaoBlocosAtivo) ativarModoExclusaoBlocos()
                bloco.isSelected = true
                blocosAdapter.notifyDataSetChanged()
                atualizarBotaoApagar()
            }
        )
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.button_toggle_mode).setOnClickListener {
            modoBlocosAtivo = !modoBlocosAtivo
            prefs.edit().putBoolean("modo_blocos_ativo_${emailUsuarioLogado}", modoBlocosAtivo).apply()
            atualizarModoUI()
        }

        botaoApagar.setOnClickListener {
            if (modoBlocosAtivo) {
                val selecionados = blocosAdapter.getSelecionados()
                if (selecionados.isNotEmpty()) {
                    confirmarExclusao("bloco(s)", selecionados.size) {
                        viewModel.deleteBlocos(selecionados)
                        desativarModoExclusaoBlocos()
                    }
                }
            } else {
                val selecionados = notesAdapter.getSelecionados()
                if (selecionados.isNotEmpty()) {
                    confirmarExclusao("anotação(ões)", selecionados.size) {
                        viewModel.deleteNotes(selecionados)
                        desativarModoExclusao()
                    }
                }
            }
        }

        clickOutsideView.setOnClickListener {
            if (modoExclusaoAtivo) desativarModoExclusao()
            if (modoExclusaoBlocosAtivo) desativarModoExclusaoBlocos()
        }

        configurarNavBar()
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

    private fun atualizarModoUI() {
        val titulo = findViewById<TextView>(R.id.textView26)
        val iconeTitulo = findViewById<ImageView>(R.id.icon_header)

        if (modoBlocosAtivo) {
            titulo.text = "Meus Blocos"
            iconeTitulo.setImageResource(R.drawable.ic_block)
            editNote.hint = "Clique para adicionar novo bloco"
            editNote.isFocusable = false
            editNote.isClickable = true
            editNote.setOnClickListener { mostrarDialogoCriarBloco() }
            buttonAdd.setOnClickListener { mostrarDialogoCriarBloco() }
            recyclerView.adapter = blocosAdapter
            carregarListaBlocos()
        } else {
            titulo.text = "Minhas Anotações"
            iconeTitulo.setImageResource(R.drawable.ic_notes)
            editNote.hint = "O que deseja anotar?"
            editNote.isFocusableInTouchMode = true
            editNote.isClickable = false
            editNote.setOnClickListener(null)
            buttonAdd.setOnClickListener {
                val text = editNote.text.toString().trim()
                if (text.isNotEmpty()) {
                    viewModel.addNote(text)
                    editNote.text.clear()
                }
            }
            recyclerView.adapter = notesAdapter
            carregarListaAnotacoes()
        }
        desativarModoExclusao()
        desativarModoExclusaoBlocos()
    }

    private fun confirmarExclusao(tipo: String, quantidade: Int, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Excluir $tipo")
            .setMessage("Tem certeza que deseja apagar $quantidade item(ns)?")
            .setPositiveButton("Excluir") { _, _ -> onConfirm() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun toggleSelection(note: Note) {
        note.isSelected = !note.isSelected
        notesAdapter.notifyDataSetChanged()
        atualizarBotaoApagar()
        if (notesAdapter.getSelecionados().isEmpty()) desativarModoExclusao()
    }

    private fun ativarModoExclusao() {
        modoExclusaoAtivo = true; notesAdapter.modoExclusaoAtivo = true
        mostrarBotaoApagar(); notesAdapter.notifyDataSetChanged()
    }

    private fun desativarModoExclusao() {
        modoExclusaoAtivo = false; notesAdapter.limparSelecao()
        esconderBotaoApagar()
    }

    private fun ativarModoExclusaoBlocos() {
        modoExclusaoBlocosAtivo = true; blocosAdapter.modoExclusaoAtivo = true
        mostrarBotaoApagar(); blocosAdapter.notifyDataSetChanged()
    }

    private fun desativarModoExclusaoBlocos() {
        modoExclusaoBlocosAtivo = false; blocosAdapter.limparSelecao()
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
        botaoApagar.visibility = if (count > 0) View.VISIBLE else View.GONE
    }

    private fun mostrarDialogoCriarBloco() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_selecionar_bloco, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent).setView(dialogView).create()

        val adicionarBloco: (String) -> Unit = { nomeBloco ->
            viewModel.adicionarBloco(Bloco(userOwnerEmail = "", nome = nomeBloco))
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.bt_financas).setOnClickListener { adicionarBloco("Finanças") }
        dialogView.findViewById<Button>(R.id.bt_estudos).setOnClickListener { adicionarBloco("Estudos") }
        dialogView.findViewById<Button>(R.id.bt_metas).setOnClickListener { adicionarBloco("Metas") }
        dialogView.findViewById<Button>(R.id.bt_trabalho).setOnClickListener { adicionarBloco("Trabalho") }
        dialogView.findViewById<Button>(R.id.bt_saude).setOnClickListener { adicionarBloco("Saúde") }
        dialog.show()
    }

    private fun openEditDialog(note: Note) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_note, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent)
            .setView(dialogView)
            .create()

        val editText = dialogView.findViewById<EditText>(R.id.editNoteDialog)
        val btnSalvar = dialogView.findViewById<Button>(R.id.btn_salvar_edit_note)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btn_cancelar_edit_note)

        editText.setText(note.text)

        btnSalvar.setOnClickListener {
            val newText = editText.text.toString().trim()
            if (newText.isNotEmpty()) {
                viewModel.updateNote(note.copy(text = newText))
            }
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun abrirDialogEditarBloco(bloco: Bloco) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_bloco, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent).setView(dialogView).create()

        val tituloBloco = dialogView.findViewById<TextView>(R.id.titulo_bloco)
        val inputSubtitulo = dialogView.findViewById<EditText>(R.id.input_subtitulo)
        val inputAnotacoes = dialogView.findViewById<EditText>(R.id.input_anotacoes)
        val inputMensagemNotificacao = dialogView.findViewById<EditText>(R.id.input_mensagem_notificacao)
        val btnConfigurarLembrete = dialogView.findViewById<Button>(R.id.btn_configurar_lembrete)
        val btnCancelar = dialogView.findViewById<Button>(R.id.botao_cancelar)
        val btnSalvar = dialogView.findViewById<Button>(R.id.botao_salvar)

        val blocoTemporario = bloco.copy()
        tituloBloco.text = blocoTemporario.nome
        inputSubtitulo.setText(blocoTemporario.subtitulo)
        inputAnotacoes.setText(blocoTemporario.anotacao)
        inputMensagemNotificacao.setText(blocoTemporario.mensagemNotificacao)

        btnConfigurarLembrete.setOnClickListener {
            abrirDialogConfigurarLembrete(blocoTemporario) { configAlterada ->
                blocoTemporario.tipoLembrete = configAlterada.tipoLembrete
                blocoTemporario.diasLembrete = configAlterada.diasLembrete
                blocoTemporario.horariosLembrete = configAlterada.horariosLembrete
                blocoTemporario.segundosLembrete = configAlterada.segundosLembrete
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnSalvar.setOnClickListener {
            val blocoAtualizado = bloco.copy(
                subtitulo = inputSubtitulo.text.toString().trim(),
                anotacao = inputAnotacoes.text.toString().trim(),
                mensagemNotificacao = inputMensagemNotificacao.text.toString().trim(),
                tipoLembrete = blocoTemporario.tipoLembrete,
                diasLembrete = blocoTemporario.diasLembrete,
                horariosLembrete = blocoTemporario.horariosLembrete,
                segundosLembrete = blocoTemporario.segundosLembrete
            )
            viewModel.updateBloco(blocoAtualizado)
            cancelarLembretesParaBloco(blocoAtualizado)
            if (blocoAtualizado.tipoLembrete != TipoLembrete.NENHUM) {
                agendarLembretesParaBloco(blocoAtualizado)
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun configurarNavBar() {
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
        navBar.findViewById<View>(R.id.botao_cronometro)?.setOnClickListener {
            startActivity(Intent(this, CronometroActivity::class.java))
        }
        navBar.findViewById<View>(R.id.botao_sugestoes)?.setOnClickListener {
            startActivity(Intent(this, SugestaoUser::class.java))
        }
    }

    private fun verificarComandosDeEntrada() {
        val deveAbrirDialogo = intent.getBooleanExtra("abrir_selecao_blocos", false)
        if (deveAbrirDialogo) {
            window.decorView.post { mostrarDialogoCriarBloco() }
            intent.removeExtra("abrir_selecao_blocos")
        }
    }
    private fun abrirDialogConfigurarLembrete(bloco: Bloco, onSave: (Bloco) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_configurar_lembrete, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent)
            .setView(dialogView)
            .create()

        // Referências às views do novo layout
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radio_group_tipo_lembrete)
        val radioNenhum = dialogView.findViewById<RadioButton>(R.id.radio_nenhum)
        val radioDiario = dialogView.findViewById<RadioButton>(R.id.radio_diario)
        val radioMensal = dialogView.findViewById<RadioButton>(R.id.radio_mensal)
        val layoutDia = dialogView.findViewById<View>(R.id.layout_seletor_dia)
        val textDia = dialogView.findViewById<TextView>(R.id.text_dia_selecionado)
        val layoutHora = dialogView.findViewById<View>(R.id.layout_seletor_hora)
        val textHora = dialogView.findViewById<TextView>(R.id.text_hora_selecionada)
        val btnSalvar = dialogView.findViewById<Button>(R.id.btn_salvar_lembrete)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btn_cancelar_lembrete)

        // Variáveis para guardar a seleção do usuário
        var diaSelecionado = bloco.diasLembrete.firstOrNull() ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        var horaSelecionada = 0
        var minutoSelecionado = 0

        if (bloco.horariosLembrete.isNotEmpty()) {
            val parts = bloco.horariosLembrete.first().split(":")
            if (parts.size == 2) {
                horaSelecionada = parts[0].toInt()
                minutoSelecionado = parts[1].toInt()
            }
        }

        // Função para atualizar a UI do diálogo
        fun updateUi(tipo: TipoLembrete) {
            layoutDia.visibility = if (tipo == TipoLembrete.MENSAL) View.VISIBLE else View.GONE
            layoutHora.visibility = if (tipo == TipoLembrete.DIARIO || tipo == TipoLembrete.MENSAL) View.VISIBLE else View.GONE
        }

        // Define o estado inicial do diálogo com base nos dados salvos
        textDia.text = "Dia $diaSelecionado"
        textHora.text = String.format(Locale.getDefault(), "%02d:%02d", horaSelecionada, minutoSelecionado)
        when (bloco.tipoLembrete) {
            TipoLembrete.NENHUM -> radioNenhum.isChecked = true
            TipoLembrete.DIARIO -> radioDiario.isChecked = true
            TipoLembrete.MENSAL -> radioMensal.isChecked = true
            else -> radioNenhum.isChecked = true
        }
        updateUi(bloco.tipoLembrete)

        // Listeners para os botões de rádio
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_nenhum -> updateUi(TipoLembrete.NENHUM)
                R.id.radio_diario -> updateUi(TipoLembrete.DIARIO)
                R.id.radio_mensal -> updateUi(TipoLembrete.MENSAL)
            }
        }

        // Abre o seletor de DIA
        textDia.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, R.style.AppTheme_Dialog_Picker, { _, _, _, dayOfMonth ->
                diaSelecionado = dayOfMonth
                textDia.text = "Dia $diaSelecionado"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Abre o seletor de HORA
        textHora.setOnClickListener {
            TimePickerDialog(
                this,
                R.style.AppTheme_TimePickerDialog, // <-- APLICA O NOVO TEMA
                { _, hourOfDay, minute ->
                    horaSelecionada = hourOfDay
                    minutoSelecionado = minute
                    textHora.text = String.format(Locale.getDefault(), "%02d:%02d", horaSelecionada, minutoSelecionado)
                },
                horaSelecionada,
                minutoSelecionado,
                true
            ).show()
        }

        btnSalvar.setOnClickListener {
            val blocoConfigurado = bloco.copy()
            when (radioGroup.checkedRadioButtonId) {
                R.id.radio_diario -> {
                    blocoConfigurado.tipoLembrete = TipoLembrete.DIARIO
                    blocoConfigurado.diasLembrete = emptyList()
                    blocoConfigurado.horariosLembrete = listOf(String.format(Locale.getDefault(), "%02d:%02d", horaSelecionada, minutoSelecionado))
                }
                R.id.radio_mensal -> {
                    blocoConfigurado.tipoLembrete = TipoLembrete.MENSAL
                    blocoConfigurado.diasLembrete = listOf(diaSelecionado)
                    blocoConfigurado.horariosLembrete = listOf(String.format(Locale.getDefault(), "%02d:%02d", horaSelecionada, minutoSelecionado))
                }
                else -> { // Nenhum
                    blocoConfigurado.tipoLembrete = TipoLembrete.NENHUM
                    blocoConfigurado.diasLembrete = emptyList()
                    blocoConfigurado.horariosLembrete = emptyList()
                }
            }
            onSave(blocoConfigurado)
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun agendarLembretesParaBloco(bloco: Bloco) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, "Permissão para agendar alarmes é necessária.", Toast.LENGTH_LONG).show()
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also { startActivity(it) }
            return
        }
        var requestCodeCounter = 0
        when (bloco.tipoLembrete) {
            TipoLembrete.SEGUNDOS_TESTE -> {
                val segundos = bloco.segundosLembrete ?: return
                if (segundos <= 0) {
                    Toast.makeText(this, "Insira segundos válidos para o teste.", Toast.LENGTH_SHORT).show()
                    return
                }
                val triggerAtMillis = System.currentTimeMillis() + segundos * 1000
                val pendingIntent = getPendingIntent(this, bloco, bloco.id.hashCode())
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                Toast.makeText(this, "Lembrete de teste agendado para daqui a $segundos segundos!", Toast.LENGTH_SHORT).show()
            }
            TipoLembrete.DIARIO -> {
                if (bloco.horariosLembrete.isEmpty()) return
                bloco.horariosLembrete.forEach { horarioStr ->
                    val (hora, minuto) = parseHorario(horarioStr) ?: return@forEach
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hora)
                        set(Calendar.MINUTE, minuto)
                        set(Calendar.SECOND, 0)
                        if (before(Calendar.getInstance())) {
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                    val pendingIntent = getPendingIntent(this, bloco, bloco.id.hashCode() + requestCodeCounter++)
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
                            val pendingIntent = getPendingIntent(this, bloco, bloco.id.hashCode() + requestCodeCounter++)
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                        }
                    }
                }
                Toast.makeText(this, "Lembrete(s) mensal(is) agendado(s)!", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    private fun parseHorario(horarioStr: String): Pair<Int, Int>? {
        val parts = horarioStr.split(":").mapNotNull { it.toIntOrNull() }
        return if (parts.size == 2) parts[0] to parts[1] else null
    }

    private fun getPendingIntent(context: Context, bloco: Bloco, requestCode: Int): PendingIntent {
        val intent = Intent(context, BlocoNotificationReceiver::class.java).apply {
            putExtra("titulo", bloco.nome + if (bloco.subtitulo.isNotEmpty()) " - ${bloco.subtitulo}" else "")
            putExtra("mensagem", bloco.mensagemNotificacao.ifEmpty { "Você tem um lembrete para este bloco." })
            putExtra("bloco_id", bloco.id)
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun cancelarLembretesParaBloco(bloco: Bloco) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0 until 1000) {
            val requestCode = bloco.id.hashCode() + i
            val pendingIntent = getPendingIntent(this, bloco, requestCode)
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun getNextMonthlyOccurrence(dia: Int, hora: Int, minuto: Int): Calendar? {
        val agora = Calendar.getInstance()
        val proximoAgendamento = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
        }
        val maxDayOfMonth = proximoAgendamento.getActualMaximum(Calendar.DAY_OF_MONTH)
        val diaValido = if (dia > maxDayOfMonth) maxDayOfMonth else dia
        proximoAgendamento.set(Calendar.DAY_OF_MONTH, diaValido)
        if (proximoAgendamento.after(agora)) {
            return proximoAgendamento
        }
        proximoAgendamento.add(Calendar.MONTH, 1)
        val maxDayOfNextMonth = proximoAgendamento.getActualMaximum(Calendar.DAY_OF_MONTH)
        val diaValidoProximoMes = if (dia > maxDayOfNextMonth) maxDayOfNextMonth else dia
        proximoAgendamento.set(Calendar.DAY_OF_MONTH, diaValidoProximoMes)
        return proximoAgendamento
    }

    private fun criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel("canal_lembrete", "Lembretes", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }
}