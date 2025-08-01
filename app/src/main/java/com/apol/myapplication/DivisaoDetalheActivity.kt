// Crie um novo arquivo: DivisaoDetalheActivity.kt
package com.apol.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.data.model.Exercicio
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class DivisaoDetalheActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var exercicioAdapter: ExercicioAdapter
    private val listaExercicios = mutableListOf<Exercicio>()
    private var divisaoId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_divisao_detalhe)

        divisaoId = intent.getLongExtra("DIVISAO_ID", -1L)
        val divisaoNome = intent.getStringExtra("DIVISAO_NOME") ?: "Exercícios"

        if (divisaoId == -1L) {
            Toast.makeText(this, "Erro: ID da divisão não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = AppDatabase.getDatabase(this)
        findViewById<TextView>(R.id.nome_divisao_detalhe).text = divisaoNome

        setupRecyclerView()
        setupListeners()

        carregarExercicios()
    }

    // Salva tudo automaticamente ao pausar/sair da tela
    override fun onPause() {
        super.onPause()
        salvarExercicios()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExercicios)
        exercicioAdapter = ExercicioAdapter(listaExercicios) { exercicio, position ->
            // Ação de apagar um exercício
            listaExercicios.removeAt(position)
            exercicioAdapter.notifyItemRemoved(position)
            exercicioAdapter.notifyItemRangeChanged(position, listaExercicios.size)
            lifecycleScope.launch {
                db.treinoDao().deleteExercicio(exercicio)
            }
        }
        recyclerView.adapter = exercicioAdapter
    }

    private fun setupListeners() {
        findViewById<FloatingActionButton>(R.id.fab_add_exercicio).setOnClickListener {
            val novoExercicio = Exercicio(
                divisaoId = divisaoId,
                nome = "",
                carga = "",
                series = "",
                repeticoes = ""
            )
            listaExercicios.add(novoExercicio)
            exercicioAdapter.notifyItemInserted(listaExercicios.size - 1)
        }

        findViewById<ImageButton>(R.id.btn_voltar_divisao).setOnClickListener {
            finish()
        }
    }

    private fun carregarExercicios() {
        lifecycleScope.launch {
            val exerciciosDoBanco = db.treinoDao().getExerciciosByDivisaoId(divisaoId)
            listaExercicios.clear()
            listaExercicios.addAll(exerciciosDoBanco)

            // Se a lista estiver vazia, adiciona um primeiro item em branco
            if (listaExercicios.isEmpty()) {
                listaExercicios.add(Exercicio(divisaoId = divisaoId, nome = "", carga = "", series = "", repeticoes = ""))
            }

            runOnUiThread {
                exercicioAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun salvarExercicios() {
        lifecycleScope.launch {
            // Filtra para salvar apenas os que têm nome (evita salvar linhas vazias)
            val exerciciosParaSalvar = listaExercicios.filter { it.nome.isNotBlank() }
            db.treinoDao().upsertExercicios(exerciciosParaSalvar)
        }
    }
}