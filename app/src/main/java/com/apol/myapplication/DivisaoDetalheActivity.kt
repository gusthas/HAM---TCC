
package com.apol.myapplication

import android.os.Bundle
import android.view.LayoutInflater
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_divisao_detalhe)

        divisaoId = intent.getLongExtra("DIVISAO_ID", -1L)
        if (divisaoId == -1L) {
            Toast.makeText(this, "Erro: ID da divisão não encontrado.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        db = AppDatabase.getDatabase(this)
        findViewById<TextView>(R.id.nome_divisao_detalhe).text = intent.getStringExtra("DIVISAO_NOME") ?: "Anotações"

        setupRecyclerView()
        setupListeners()
        carregarNotas()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExercicios)
        notaAdapter = TreinoNotaAdapter(listaDeNotas) { nota ->
            exibirDialogoEditarNota(nota)
        }
        recyclerView.adapter = notaAdapter
    }

    private fun setupListeners() {
        findViewById<FloatingActionButton>(R.id.fab_add_exercicio).setOnClickListener {
            exibirDialogoCriarNota()
        }
        findViewById<ImageButton>(R.id.btn_voltar_divisao).setOnClickListener {
            finish()
        }
    }

    private fun carregarNotas() {
        lifecycleScope.launch {
            val notasDoBanco = db.treinoDao().getNotasByDivisaoId(divisaoId)
            runOnUiThread {
                notaAdapter.submitList(notasDoBanco)
            }
        }
    }

    private fun exibirDialogoCriarNota() {
        val editText = EditText(this).apply { hint = "Título (ex: KM corridos, Anotações...)" }
        AlertDialog.Builder(this)
            .setTitle("Nova Anotação de Treino")
            .setView(editText)
            .setPositiveButton("Criar") { _, _ ->
                val titulo = editText.text.toString().trim()
                if (titulo.isNotEmpty()) {
                    lifecycleScope.launch {
                        db.treinoDao().insertTreinoNota(TreinoNota(divisaoId = divisaoId, titulo = titulo))
                        carregarNotas()
                    }
                }
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