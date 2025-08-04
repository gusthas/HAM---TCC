
package com.apol.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.data.model.LogEntry
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class DivisaoDetalheActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var logEntryAdapter: LogEntryAdapter
    private val listaDeLogs = mutableListOf<LogEntry>()
    private var divisaoId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_divisao_detalhe)

        divisaoId = intent.getLongExtra("DIVISAO_ID", -1L)
        val divisaoNome = intent.getStringExtra("DIVISAO_NOME") ?: "Exercícios"

        if (divisaoId == -1L) {
            Toast.makeText(this, "Erro: ID da divisão não encontrado.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        db = AppDatabase.getDatabase(this)
        findViewById<TextView>(R.id.nome_divisao_detalhe).text = divisaoNome

        setupRecyclerView()
        setupListeners()
        carregarLogs()
    }

    override fun onPause() {
        super.onPause()
        salvarLogs()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExercicios)


        logEntryAdapter = LogEntryAdapter(listaDeLogs) { logEntry, position ->

            listaDeLogs.removeAt(position)
            logEntryAdapter.notifyItemRemoved(position)
            logEntryAdapter.notifyItemRangeChanged(position, listaDeLogs.size)
            lifecycleScope.launch {
                db.treinoDao().deleteLogEntry(logEntry)
            }
        }
        recyclerView.adapter = logEntryAdapter
    }

    private fun setupListeners() {
        findViewById<FloatingActionButton>(R.id.fab_add_exercicio).setOnClickListener {
            val novoLog = LogEntry(divisaoId = divisaoId) // MUDOU
            listaDeLogs.add(novoLog)
            logEntryAdapter.notifyItemInserted(listaDeLogs.size - 1)
        }
        findViewById<ImageButton>(R.id.btn_voltar_divisao).setOnClickListener {
            finish()
        }
    }

    private fun carregarLogs() {
        lifecycleScope.launch {

            val logsDoBanco = db.treinoDao().getLogEntriesByDivisaoId(divisaoId)
            listaDeLogs.clear()
            listaDeLogs.addAll(logsDoBanco)

            if (listaDeLogs.isEmpty()) {
                listaDeLogs.add(LogEntry(divisaoId = divisaoId))
            }

            runOnUiThread {
                logEntryAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun salvarLogs() {
        lifecycleScope.launch {
            val logsParaSalvar = listaDeLogs.filter { it.campo1.isNotBlank() }
            db.treinoDao().upsertLogEntries(logsParaSalvar)
        }
    }
}