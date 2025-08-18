// Substitua o conteúdo COMPLETO do seu arquivo DivisaoDetalheActivity.kt
package com.apol.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
    private var emailUsuarioLogado: String? = null // Para saber quem é o dono

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

        // Pega o e-mail do usuário logado
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        emailUsuarioLogado = prefs.getString("LOGGED_IN_USER_EMAIL", null)

        if (emailUsuarioLogado == null) {
            Toast.makeText(this, "Erro de sessão. Faça login novamente.", Toast.LENGTH_SHORT).show()
            finish()
            return
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
                // Passa o e-mail para filtrar os resultados
                val notasDoBanco = db.treinoDao().getNotasByDivisaoId(divisaoId, email)
                runOnUiThread {
                    notaAdapter.submitList(notasDoBanco)
                }
            }
        }
    }

    private fun exibirDialogoCriarNota() {
        emailUsuarioLogado?.let { email ->
            val editText = EditText(this).apply { hint = "Título (ex: KM corridos, Anotações...)" }
            AlertDialog.Builder(this)
                .setTitle("Nova Anotação de Treino")
                .setView(editText)
                .setPositiveButton("Criar") { _, _ ->
                    val titulo = editText.text.toString().trim()
                    if (titulo.isNotEmpty()) {
                        lifecycleScope.launch {
                            // CORREÇÃO: Passa o e-mail do dono ao criar a nota
                            val novaNota = TreinoNota(userOwnerEmail = email, divisaoId = divisaoId, titulo = titulo)
                            db.treinoDao().insertTreinoNota(novaNota)
                            carregarNotas()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    // As funções abaixo (ativarModoExclusao, exibirDialogoEditarNota, etc.)
    // já estão corretas e não precisam de mudança.
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
        val tituloView = dialogView.findViewById<TextView>(R.id.titulo_dialogo_nota)
        val conteudoInput = dialogView.findViewById<EditText>(R.id.input_conteudo_nota)

        tituloView.text = nota.titulo
        conteudoInput.setText(nota.conteudo)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                nota.conteudo = conteudoInput.text.toString().trim()
                lifecycleScope.launch {
                    db.treinoDao().updateTreinoNota(nota)
                    carregarNotas()
                }
            }
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Apagar") { _, _ ->
                lifecycleScope.launch {
                    db.treinoDao().deleteTreinoNota(nota)
                    carregarNotas()
                }
            }
            .show()
    }
}