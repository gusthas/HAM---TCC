package com.apol.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.data.model.TreinoNota
import com.apol.myapplication.data.model.TreinoNotaAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class DivisaoDetalheActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var notaAdapter: TreinoNotaAdapter
    private val listaDeNotas = mutableListOf<TreinoNota>()
    private var divisaoId: Long = -1L
    private var emailUsuarioLogado: String? = null

    private var modoExclusaoAtivo = false
    private lateinit var btnApagarNotas: ImageButton
    private lateinit var fabAddNota: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_divisao_detalhe)

        divisaoId = intent.getLongExtra("DIVISAO_ID", -1L)
        if (divisaoId == -1L) { finish(); return }

        db = AppDatabase.getDatabase(this)
        findViewById<TextView>(R.id.nome_divisao_detalhe).text = intent.getStringExtra("DIVISAO_NOME") ?: "Anotações"
        btnApagarNotas = findViewById(R.id.btn_apagar_notas)
        fabAddNota = findViewById(R.id.fab_add_exercicio)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        emailUsuarioLogado = prefs.getString("LOGGED_IN_USER_EMAIL", null)

        if (emailUsuarioLogado == null) {
            Toast.makeText(this, "Erro de sessão. Faça login novamente.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        setupRecyclerView()
        setupListeners()
        carregarNotas()
    }

    override fun onBackPressed() {
        if (modoExclusaoAtivo) {
            desativarModoExclusao()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExercicios)
        notaAdapter = TreinoNotaAdapter(
            notas = listaDeNotas,
            onItemClick = { nota ->
                if (modoExclusaoAtivo) {
                    toggleSelecao(nota)
                } else {
                    exibirDialogoEditarNota(nota)
                }
            },
            onItemLongClick = { nota ->
                if (!modoExclusaoAtivo) {
                    ativarModoExclusao(nota)
                }
            }
        )
        recyclerView.adapter = notaAdapter
    }

    private fun setupListeners() {
        fabAddNota.setOnClickListener {
            exibirDialogoCriarNota()
        }
        findViewById<ImageButton>(R.id.btn_voltar_divisao).setOnClickListener {
            finish()
        }
        btnApagarNotas.setOnClickListener {
            val selecionados = notaAdapter.getSelecionados()
            if (selecionados.isNotEmpty()) {
                confirmarExclusao(selecionados)
            }
        }
    }

    private fun carregarNotas() {
        emailUsuarioLogado?.let { email ->
            lifecycleScope.launch {
                val notasDoBanco = db.treinoDao().getNotasByDivisaoId(divisaoId, email)
                runOnUiThread {
                    notaAdapter.submitList(notasDoBanco)
                }
            }
        }
    }

    // --- FUNÇÃO DE CRIAR NOTA CORRIGIDA ---
    private fun exibirDialogoCriarNota() {
        emailUsuarioLogado?.let { email ->
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nova_anotacao, null)
            val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent)
                .setView(dialogView)
                .create()

            val editText = dialogView.findViewById<EditText>(R.id.edit_text_new_note)
            val btnCriar = dialogView.findViewById<Button>(R.id.btn_criar_new_note)
            val btnCancelar = dialogView.findViewById<Button>(R.id.btn_cancelar_new_note)

            btnCriar.setOnClickListener {
                val titulo = editText.text.toString().trim()
                if (titulo.isNotEmpty()) {
                    lifecycleScope.launch {
                        val novaNota = TreinoNota(userOwnerEmail = email, divisaoId = divisaoId, titulo = titulo)
                        db.treinoDao().insertTreinoNota(novaNota)
                        carregarNotas() // Recarrega a lista para mostrar a nova nota
                    }
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "O título não pode ser vazio.", Toast.LENGTH_SHORT).show()
                }
            }

            btnCancelar.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun ativarModoExclusao(primeiraNota: TreinoNota) {
        modoExclusaoAtivo = true
        notaAdapter.modoExclusaoAtivo = true
        fabAddNota.visibility = View.GONE
        btnApagarNotas.visibility = View.VISIBLE
        toggleSelecao(primeiraNota)
    }

    private fun desativarModoExclusao() {
        modoExclusaoAtivo = false
        notaAdapter.limparSelecao()
        fabAddNota.visibility = View.VISIBLE
        btnApagarNotas.visibility = View.GONE
    }

    private fun toggleSelecao(nota: TreinoNota) {
        nota.isSelected = !nota.isSelected
        notaAdapter.notifyDataSetChanged()
        if (modoExclusaoAtivo && notaAdapter.getSelecionados().isEmpty()) {
            desativarModoExclusao()
        }
    }

    private fun confirmarExclusao(notasParaApagar: List<TreinoNota>) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Anotações")
            .setMessage("Tem certeza que deseja apagar ${notasParaApagar.size} anotação(ões)?")
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch {
                    db.treinoDao().deleteTreinoNotas(notasParaApagar)
                    carregarNotas()
                }
                desativarModoExclusao()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun exibirDialogoEditarNota(nota: TreinoNota) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_nota_treino, null)

        // Usa o estilo customizado para remover o fundo branco
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent)
            .setView(dialogView)
            .create()

        // Encontra os componentes no novo layout
        val tituloView = dialogView.findViewById<TextView>(R.id.titulo_dialogo_nota)
        val conteudoInput = dialogView.findViewById<EditText>(R.id.input_conteudo_nota)
        val btnSalvar = dialogView.findViewById<Button>(R.id.btn_salvar_nota)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btn_cancelar_nota)
        val btnApagar = dialogView.findViewById<Button>(R.id.btn_apagar_nota)

        tituloView.text = nota.titulo
        conteudoInput.setText(nota.conteudo)

        // Configura o clique do botão Salvar
        btnSalvar.setOnClickListener {
            nota.conteudo = conteudoInput.text.toString().trim()
            lifecycleScope.launch {
                db.treinoDao().updateTreinoNota(nota)
                carregarNotas()
            }
            dialog.dismiss()
        }

        // Configura o clique do botão Cancelar
        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        // Configura o clique do botão Apagar
        btnApagar.setOnClickListener {
            lifecycleScope.launch {
                db.treinoDao().deleteTreinoNota(nota)
                carregarNotas()
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}