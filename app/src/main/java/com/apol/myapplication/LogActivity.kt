
package com.apol.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.data.model.LogEntry
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class LogActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var logAdapter: LogAdapter
    private lateinit var recyclerView: RecyclerView
    private val listaDeLogs = mutableListOf<LogEntry>()
    private var divisaoId: Long = -1L
    private var treinoId: Long = -1L
    private var templateColunas = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_divisao_detalhe)

        divisaoId = intent.getLongExtra("DIVISAO_ID", -1L)
        treinoId = intent.getLongExtra("TREINO_ID", -1L)
        val divisaoNome = intent.getStringExtra("DIVISAO_NOME") ?: "Log"

        if (divisaoId == -1L || treinoId == -1L) {
            Toast.makeText(this, "Erro: IDs não encontrados.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        db = AppDatabase.getDatabase(this)
        findViewById<TextView>(R.id.nome_divisao_detalhe).text = divisaoNome
        recyclerView = findViewById(R.id.recyclerViewExercicios)

        setupRecyclerView()
        setupListeners()
        carregarTemplateELogs()
    }

    override fun onPause() {
        super.onPause()
        salvarLogs()
    }

    private fun setupRecyclerView() {
        logAdapter = LogAdapter(listaDeLogs, templateColunas) { log, position ->
            val logParaApagar = listaDeLogs[position]
            listaDeLogs.removeAt(position)
            logAdapter.notifyItemRemoved(position)
            logAdapter.notifyItemRangeChanged(position, listaDeLogs.size)
            lifecycleScope.launch { db.treinoDao().deleteLogEntry(logParaApagar) }
        }
        recyclerView.adapter = logAdapter
    }

    private fun setupListeners() {
        findViewById<FloatingActionButton>(R.id.fab_add_exercicio).setOnClickListener {
            val novoLog = LogEntry(divisaoId = divisaoId)
            listaDeLogs.add(novoLog)
            logAdapter.notifyItemInserted(listaDeLogs.size - 1)
            recyclerView.scrollToPosition(listaDeLogs.size - 1)
        }
        findViewById<ImageButton>(R.id.btn_voltar_divisao).setOnClickListener {
            finish()
        }
    }

    private fun carregarTemplateELogs() {
        lifecycleScope.launch {
            val treino = db.treinoDao().getTreinoById(treinoId)
            treino?.templateJson?.let { json ->
                templateColunas = Json.decodeFromString(json)
                logAdapter.templateColunas = templateColunas
            }
            carregarLogsDoBanco()
        }
    }

    private fun carregarLogsDoBanco() {
        lifecycleScope.launch {
            val logsDoBanco = db.treinoDao().getLogEntriesByDivisaoId(divisaoId)
            listaDeLogs.clear()
            listaDeLogs.addAll(logsDoBanco)
            if (listaDeLogs.isEmpty()) {
                listaDeLogs.add(LogEntry(divisaoId = divisaoId))
            }
            runOnUiThread { logAdapter.notifyDataSetChanged() }
        }
    }

    private fun salvarLogs() {
        // ETAPA 1: LER OS DADOS DA TELA E ATUALIZAR A LISTA EM MEMÓRIA
        for (i in 0 until listaDeLogs.size) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? LogAdapter.LogViewHolder
            viewHolder?.let { holder ->
                val log = listaDeLogs[i]
                log.campo1 = holder.inputs[0].text.toString()
                log.campo2 = holder.inputs[1].text.toString()
                log.campo3 = holder.inputs[2].text.toString()
                log.campo4 = holder.inputs[3].text.toString()
            }
        }

        // ETAPA 2: AGORA, COM A LISTA ATUALIZADA, SALVAR NO BANCO
        lifecycleScope.launch {
            val logsParaSalvar = listaDeLogs.filter {
                it.campo1.isNotBlank() || it.campo2.isNotBlank() || it.campo3.isNotBlank() || it.campo4.isNotBlank()
            }

            db.treinoDao().deleteAllLogsByDivisaoId(divisaoId)
            if (logsParaSalvar.isNotEmpty()) {
                val logsParaInserir = logsParaSalvar.map { it.copy(id = 0) }
                db.treinoDao().upsertLogEntries(logsParaInserir)
            }
        }
    }
}