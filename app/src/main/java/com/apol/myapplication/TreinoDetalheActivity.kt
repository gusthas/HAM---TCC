package com.apol.myapplication

import android.content.Intent
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
import com.apol.myapplication.data.model.DivisaoTreino
import com.apol.myapplication.data.model.TipoDivisao
import com.apol.myapplication.data.model.TreinoEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TreinoDetalheActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var divisaoAdapter: DivisaoAdapter
    private val listaDivisoes = mutableListOf<DivisaoTreino>()
    private var treinoId: Long = -1L
    private var treinoAtual: TreinoEntity? = null
    private var modoExclusaoAtivo = false
    private var emailUsuarioLogado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treino_detalhe)

        db = AppDatabase.getDatabase(this)
        treinoId = intent.getLongExtra("TREINO_ID", -1L)
        findViewById<TextView>(R.id.nome_treino_detalhe).text = intent.getStringExtra("TREINO_NOME") ?: "Detalhes"

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        emailUsuarioLogado = prefs.getString("LOGGED_IN_USER_EMAIL", null)

        if (emailUsuarioLogado == null) {
            Toast.makeText(this, "Erro de sessão. Faça login novamente.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        carregarDadosIniciais()
    }

    override fun onBackPressed() {
        if (modoExclusaoAtivo) {
            desativarModoExclusao()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btn_voltar_detalhe).setOnClickListener { finish() }
        findViewById<FloatingActionButton>(R.id.fab_add_divisao).setOnClickListener { adicionarNovaDivisaoLetra() }
        findViewById<ImageButton>(R.id.btn_apagar_divisoes).setOnClickListener {
            val selecionados = divisaoAdapter.getSelecionados()
            if (selecionados.isNotEmpty()) {
                confirmarExclusao(selecionados)
            }
        }
    }

    private fun setupRecyclerView() {
        val recyclerViewDivisoes = findViewById<RecyclerView>(R.id.recyclerViewDivisoes)
        divisaoAdapter = DivisaoAdapter(listaDivisoes,
            onItemClick = { divisao ->
                if (modoExclusaoAtivo) {
                    toggleSelecao(divisao)
                } else {
                    val intent = Intent(this, DivisaoDetalheActivity::class.java).apply {
                        putExtra("TREINO_ID", treinoId)
                        putExtra("DIVISAO_ID", divisao.id)
                        putExtra("DIVISAO_NOME", divisao.nome)
                    }
                    startActivity(intent)
                }
            },
            onItemLongClick = { divisao ->
                if (!modoExclusaoAtivo && treinoAtual?.tipoDivisao == TipoDivisao.LETRAS) {
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

    private fun carregarDadosIniciais() {
        lifecycleScope.launch {
            treinoAtual = db.treinoDao().getTreinoById(treinoId)
            treinoAtual?.let { treino ->
                runOnUiThread {
                    if (treino.tipoDivisao == TipoDivisao.NAO_DEFINIDO) {
                        exibirDialogoEscolhaDeDivisao(treino)
                    } else {
                        findViewById<FloatingActionButton>(R.id.fab_add_divisao).visibility = if(treino.tipoDivisao == TipoDivisao.LETRAS) View.VISIBLE else View.GONE
                        carregarDivisoesDoBanco()
                    }
                }
            }
        }
    }

    private fun carregarDivisoesDoBanco() {
        emailUsuarioLogado?.let { email ->
            lifecycleScope.launch {
                val divisoesDoBanco = db.treinoDao().getDivisoesByTreinoId(treinoId) // Assumindo que esta query filtra por treinoId, o que é suficiente.
                runOnUiThread {
                    divisaoAdapter.submitList(divisoesDoBanco)
                    listaDivisoes.clear()
                    listaDivisoes.addAll(divisoesDoBanco)
                }
            }
        }
    }

    private fun adicionarNovaDivisaoLetra() {
        emailUsuarioLogado?.let { email ->
            val proximaLetraChar = ('A' + listaDivisoes.size).toChar()
            val novaDivisao = DivisaoTreino(userOwnerEmail = email, treinoId = treinoId, nome = "Treino $proximaLetraChar", ordem = listaDivisoes.size)
            lifecycleScope.launch {
                db.treinoDao().insertDivisao(novaDivisao)
                carregarDivisoesDoBanco()
            }
        }
    }

    private fun ativarModoExclusao(primeiraDivisao: DivisaoTreino) {
        modoExclusaoAtivo = true
        divisaoAdapter.modoExclusaoAtivo = true
        findViewById<FloatingActionButton>(R.id.fab_add_divisao).visibility = View.GONE
        findViewById<ImageButton>(R.id.btn_apagar_divisoes).visibility = View.VISIBLE
        toggleSelecao(primeiraDivisao)
    }

    private fun desativarModoExclusao() {
        modoExclusaoAtivo = false
        divisaoAdapter.limparSelecao()
        if (treinoAtual?.tipoDivisao == TipoDivisao.LETRAS) {
            findViewById<FloatingActionButton>(R.id.fab_add_divisao).visibility = View.VISIBLE
        }
        findViewById<ImageButton>(R.id.btn_apagar_divisoes).visibility = View.GONE
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

    private fun exibirDialogoEscolhaDeDivisao(treino: TreinoEntity) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_escolher_divisao, null)
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
        emailUsuarioLogado?.let { email ->
            lifecycleScope.launch {
                val treinoAtualizado = treino.copy(tipoDivisao = tipo)
                db.treinoDao().updateTreino(treinoAtualizado)
                treinoAtual = treinoAtualizado

                if (tipo == TipoDivisao.DIAS_DA_SEMANA) {
                    val dias = listOf("Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado")
                    dias.forEachIndexed { index, nome ->
                        db.treinoDao().insertDivisao(DivisaoTreino(userOwnerEmail = email, treinoId = treino.id, nome = nome, ordem = index))
                    }
                } else { // LETRAS
                    val letras = listOf("Treino A", "Treino B", "Treino C")
                    letras.forEachIndexed { index, nome ->
                        db.treinoDao().insertDivisao(DivisaoTreino(userOwnerEmail = email, treinoId = treino.id, nome = nome, ordem = index))
                    }
                }
                carregarDadosIniciais()
            }
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