
package com.apol.myapplication

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.data.model.TreinoEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ConfigurarTemplateActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: TemplateColumnAdapter
    private val listaColunas = mutableListOf<String>()
    private var treinoId: Long = -1L
    private var treinoAtual: TreinoEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configurar_template)

        treinoId = intent.getLongExtra("TREINO_ID", -1L)
        if (treinoId == -1L) {
            finish(); return
        }

        db = AppDatabase.getDatabase(this)

        setupUI()
        carregarTemplate()
    }

    // Salva o template ao sair da tela
    override fun onPause() {
        super.onPause()
        salvarTemplate()
    }

    private fun setupUI() {
        findViewById<ImageButton>(R.id.btn_voltar_template).setOnClickListener { finish() }
        findViewById<FloatingActionButton>(R.id.fab_add_coluna).setOnClickListener {
            exibirDialogoAdicionarColuna()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewColunas)
        adapter = TemplateColumnAdapter(listaColunas) { position ->
            listaColunas.removeAt(position)
            adapter.notifyItemRemoved(position)
        }
        recyclerView.adapter = adapter
    }

    private fun carregarTemplate() {
        lifecycleScope.launch {
            treinoAtual = db.treinoDao().getTreinoById(treinoId)
            treinoAtual?.let { treino ->
                findViewById<TextView>(R.id.titulo_template).text = "Colunas para ${treino.nome}"

                // Se já existe um template salvo, decodifica o JSON
                treino.templateJson?.let { json ->
                    val colunasSalvas = Json.decodeFromString<List<String>>(json)
                    listaColunas.addAll(colunasSalvas)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun salvarTemplate() {
        // Converte a lista de colunas para uma string JSON
        val jsonString = Json.encodeToString(listaColunas)

        treinoAtual?.let {
            it.templateJson = jsonString
            lifecycleScope.launch {
                db.treinoDao().updateTreino(it)
            }
        }
    }

    private fun exibirDialogoAdicionarColuna() {
        val editText = EditText(this).apply { hint = "Nome da coluna" }
        AlertDialog.Builder(this)
            .setTitle("Nova Coluna")
            .setView(editText)
            .setPositiveButton("Adicionar") { _, _ ->
                val nomeColuna = editText.text.toString().trim()
                if (nomeColuna.isNotEmpty()) {
                    listaColunas.add(nomeColuna)
                    adapter.notifyItemInserted(listaColunas.size - 1)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}