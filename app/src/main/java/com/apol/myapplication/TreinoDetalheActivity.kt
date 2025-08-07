// Substitua o conteúdo COMPLETO do seu arquivo TreinoDetalheActivity.kt por este:
package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
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
import com.apol.myapplication.data.model.DivisaoTreino
import com.apol.myapplication.data.model.TipoDivisao
import com.apol.myapplication.data.model.TipoTreino
import com.apol.myapplication.data.model.TreinoEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TreinoDetalheActivity : AppCompatActivity() {

    // --- PROPRIEDADES ---
    private lateinit var db: AppDatabase
    private lateinit var divisaoAdapter: DivisaoAdapter
    private lateinit var fabAddDivisao: FloatingActionButton
    private lateinit var btnVoltar: ImageButton
    private lateinit var nomeTreinoTextView: TextView
    private lateinit var recyclerViewDivisoes: RecyclerView
    private lateinit var btnApagarDivisoes: ImageButton

    private val listaDivisoes = mutableListOf<DivisaoTreino>()
    private var treinoId: Long = -1L
    private var treinoAtual: TreinoEntity? = null
    private var modoExclusaoAtivo = false

    // --- CICLO DE VIDA ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treino_detalhe)

        treinoId = intent.getLongExtra("TREINO_ID", -1L)
        val treinoNome = intent.getStringExtra("TREINO_NOME") ?: "Detalhes"
        if (treinoId == -1L) {
            Toast.makeText(this, "Erro: ID do treino inválido.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = AppDatabase.getDatabase(this)
        nomeTreinoTextView = findViewById(R.id.nome_treino_detalhe)
        recyclerViewDivisoes = findViewById(R.id.recyclerViewDivisoes)
        fabAddDivisao = findViewById(R.id.fab_add_divisao)
        btnVoltar = findViewById(R.id.btn_voltar_detalhe)
        btnApagarDivisoes = findViewById(R.id.btn_apagar_divisoes)

        nomeTreinoTextView.text = treinoNome

        setupRecyclerView()
        setupListeners()
        carregarDadosIniciais()
    }

    override fun onBackPressed() {
        if (modoExclusaoAtivo) {
            desativarModoExclusao()
        } else {
            super.onBackPressed()
        }
    }

    // --- CONFIGURAÇÕES (SETUP) ---
    private fun setupListeners() {
        btnVoltar.setOnClickListener { finish() }
        fabAddDivisao.setOnClickListener { adicionarNovaDivisaoLetra() }
        btnApagarDivisoes.setOnClickListener {
            val selecionados = divisaoAdapter.getSelecionados()
            if (selecionados.isNotEmpty()) {
                confirmarExclusao(selecionados)
            }
        }
    }

    private fun setupRecyclerView() {
        divisaoAdapter = DivisaoAdapter(listaDivisoes,
            onItemClick = { divisao ->
                if (modoExclusaoAtivo) {
                    toggleSelecao(divisao)
                } else {
                    treinoAtual?.let { treino ->
                        val proximaTela = when (treino.tipoDeTreino) {
                            TipoTreino.ACADEMIA -> DivisaoDetalheActivity::class.java // A tela antiga de exercícios
                            // Para os outros tipos, verificamos se o template já foi criado
                            else -> {
                                if (treino.templateJson.isNullOrBlank()) {
                                    ConfigurarTemplateActivity::class.java // Se não tem template, vai para a configuração
                                } else {
                                    LogActivity::class.java // Se já tem, vai para a tela de log
                                }
                            }
                        }

                        val intent = Intent(this, proximaTela).apply {
                            putExtra("TREINO_ID", treino.id)
                            putExtra("DIVISAO_ID", divisao.id)
                            putExtra("DIVISAO_NOME", divisao.nome)
                        }
                        startActivity(intent)
                    }
                }
            },
            onItemLongClick = { divisao ->
                if (!modoExclusaoAtivo) {
                    ativarModoExclusao(divisao)
                }
            },
            onEditClick = { divisao ->
                if (!modoExclusaoAtivo) {
                    exibirDialogoRenomearDivisao(divisao)
                }
            }
        )
        recyclerViewDivisoes.adapter = divisaoAdapter
    }

    // --- LÓGICA DE DADOS ---
    private fun carregarDadosIniciais() {
        lifecycleScope.launch {
            treinoAtual = db.treinoDao().getTreinoById(treinoId)
            treinoAtual?.let { treino ->
                runOnUiThread {
                    if (treino.tipoDivisao == TipoDivisao.NAO_DEFINIDO) {
                        exibirDialogoEscolhaDeDivisao(treino)
                    } else {
                        fabAddDivisao.visibility = if(treino.tipoDivisao == TipoDivisao.LETRAS) View.VISIBLE else View.GONE
                        carregarDivisoesDoBanco()
                    }
                }
            }
        }
    }

    private fun carregarDivisoesDoBanco() {
        lifecycleScope.launch {
            val divisoesDoBanco = db.treinoDao().getDivisoesByTreinoId(treinoId)
            runOnUiThread {
                divisaoAdapter.submitList(divisoesDoBanco)
                listaDivisoes.clear()
                listaDivisoes.addAll(divisoesDoBanco)
            }
        }
    }

    private fun adicionarNovaDivisaoLetra() {
        val proximaLetraChar = ('A' + listaDivisoes.size).toChar()
        val novaDivisao = DivisaoTreino(treinoId = treinoId, nome = "Treino $proximaLetraChar", ordem = listaDivisoes.size)
        lifecycleScope.launch {
            db.treinoDao().insertDivisao(novaDivisao)
            carregarDivisoesDoBanco()
        }
    }

    // --- MODO DE EXCLUSÃO ---
    private fun ativarModoExclusao(primeiraDivisao: DivisaoTreino) {
        modoExclusaoAtivo = true
        divisaoAdapter.modoExclusaoAtivo = true
        fabAddDivisao.visibility = View.GONE
        btnApagarDivisoes.visibility = View.VISIBLE
        toggleSelecao(primeiraDivisao)
    }

    private fun desativarModoExclusao() {
        modoExclusaoAtivo = false
        divisaoAdapter.limparSelecao()
        if (treinoAtual?.tipoDivisao == TipoDivisao.LETRAS) {
            fabAddDivisao.visibility = View.VISIBLE
        }
        btnApagarDivisoes.visibility = View.GONE
    }

    private fun toggleSelecao(divisao: DivisaoTreino) {
        divisao.isSelected = !divisao.isSelected
        divisaoAdapter.notifyDataSetChanged()
        if (modoExclusaoAtivo && divisaoAdapter.getSelecionados().isEmpty()) {
            desativarModoExclusao()
        }
    }

    private fun confirmarExclusao(divisoesParaApagar: List<DivisaoTreino>) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Divisões")
            .setMessage("Tem certeza que deseja apagar ${divisoesParaApagar.size} divisão(ões)?")
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch {
                    db.treinoDao().deleteDivisoes(divisoesParaApagar)
                    carregarDivisoesDoBanco()
                }
                desativarModoExclusao()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- DIÁLOGOS ---
    private fun exibirDialogoEscolhaDeDivisao(treino: TreinoEntity) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_escolher_divisao, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val clickListener = View.OnClickListener { view ->
            val tipoEscolhido = when(view.id) {
                R.id.btn_dias_da_semana -> TipoDivisao.DIAS_DA_SEMANA
                R.id.btn_letras -> TipoDivisao.LETRAS
                else -> null
            }
            tipoEscolhido?.let { configurarDivisoes(treino, it) }
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_dias_da_semana).setOnClickListener(clickListener)
        dialogView.findViewById<Button>(R.id.btn_letras).setOnClickListener(clickListener)
        dialogView.findViewById<Button>(R.id.btn_cancelar_dialogo).setOnClickListener { dialog.dismiss() }

        dialog.setOnDismissListener {
            lifecycleScope.launch {
                val treinoVerificado = db.treinoDao().getTreinoById(treino.id)
                if (treinoVerificado?.tipoDivisao == TipoDivisao.NAO_DEFINIDO) {
                    finish()
                }
            }
        }
        dialog.show()
    }

    private fun configurarDivisoes(treino: TreinoEntity, tipo: TipoDivisao) {
        lifecycleScope.launch {
            val treinoAtualizado = treino.copy(tipoDivisao = tipo)
            db.treinoDao().updateTreino(treinoAtualizado)
            treinoAtual = treinoAtualizado

            if (tipo == TipoDivisao.DIAS_DA_SEMANA) {
                val dias = listOf("Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado")
                dias.forEachIndexed { index, nome -> db.treinoDao().insertDivisao(DivisaoTreino(treinoId = treino.id, nome = nome, ordem = index)) }
            } else { // LETRAS
                val letras = listOf("Treino A", "Treino B", "Treino C")
                letras.forEachIndexed { index, nome -> db.treinoDao().insertDivisao(DivisaoTreino(treinoId = treino.id, nome = nome, ordem = index)) }
            }
            carregarDadosIniciais()
        }
    }

    private fun exibirDialogoRenomearDivisao(divisao: DivisaoTreino) {
        val editText = EditText(this).apply { setText(divisao.nome) }
        AlertDialog.Builder(this)
            .setTitle("Renomear Divisão")
            .setView(editText)
            .setPositiveButton("Salvar") { _, _ ->
                val novoNome = editText.text.toString().trim()
                if (novoNome.isNotEmpty()) {
                    lifecycleScope.launch {
                        divisao.nome = novoNome
                        db.treinoDao().updateDivisao(divisao)
                        carregarDivisoesDoBanco()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}