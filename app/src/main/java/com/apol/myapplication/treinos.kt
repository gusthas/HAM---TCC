
package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.data.model.TipoTreino
import com.apol.myapplication.data.model.TreinoEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class treinos : AppCompatActivity() {

    // --- PROPRIEDADES ---
    private lateinit var recyclerViewTreinos: RecyclerView
    private lateinit var fabAddTreino: FloatingActionButton
    private lateinit var btnApagarTreinos: ImageButton
    private lateinit var clickOutsideView: View

    private val listaDeTreinos = mutableListOf<TreinoEntity>()
    private lateinit var treinosAdapter: TreinosAdapter

    private var modoExclusaoAtivo = false
    private lateinit var db: AppDatabase

    // Novo: para guardar o e-mail do usuário logado
    private var emailUsuarioLogado: String? = null

    // --- CICLO DE VIDA ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treinos)

        db = AppDatabase.getDatabase(this)

        // 1. Pega o e-mail do usuário logado no SharedPreferences
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        emailUsuarioLogado = prefs.getString("LOGGED_IN_USER_EMAIL", null)

        // Se por algum motivo não houver e-mail, encerra a tela para evitar erros
        if (emailUsuarioLogado == null) {
            Toast.makeText(this, "Erro: Nenhum usuário logado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // --- Inicialização das Views ---
        recyclerViewTreinos = findViewById(R.id.recyclerViewTreinos)
        fabAddTreino = findViewById(R.id.fab_add_treino)
        btnApagarTreinos = findViewById(R.id.btn_apagar_treinos)
        clickOutsideView = findViewById(R.id.click_outside_view)

        setupWindowInsets()
        setupNavigationBar()
        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // Recarrega os treinos sempre que a tela fica visível
        carregarTreinos()
    }

    override fun onBackPressed() {
        if (modoExclusaoAtivo) {
            desativarModoExclusao()
        } else {
            finishAffinity()
        }
    }

    // --- CONFIGURAÇÕES (SETUP) ---
    private fun setupListeners() {
        fabAddTreino.setOnClickListener {
            if (!modoExclusaoAtivo) exibirDialogoAdicionarTreino()
        }
        btnApagarTreinos.setOnClickListener {
            val selecionados = treinosAdapter.getSelecionados()
            if (selecionados.isNotEmpty()) confirmarExclusao(selecionados)
        }
        clickOutsideView.setOnClickListener {
            if (modoExclusaoAtivo) desativarModoExclusao()
        }
    }

    private fun setupRecyclerView() {
        treinosAdapter = TreinosAdapter(listaDeTreinos,
            onItemClick = { treino ->
                if (modoExclusaoAtivo) {
                    toggleSelecao(treino)
                } else {
                    val intent = Intent(this, TreinoDetalheActivity::class.java).apply {
                        putExtra("TREINO_ID", treino.id)
                        putExtra("TREINO_NOME", treino.nome)
                    }
                    startActivity(intent)
                }
            },
            onItemLongClick = { treino ->
                if (!modoExclusaoAtivo) ativarModoExclusao(treino)
            }
        )
        recyclerViewTreinos.adapter = treinosAdapter
    }

    // --- LÓGICA DE DADOS (AGORA COM FILTRO POR USUÁRIO) ---
    private fun carregarTreinos() {
        emailUsuarioLogado?.let { email ->
            lifecycleScope.launch {
                // 2. Passa o e-mail para a busca no banco
                val treinosDoUsuario = db.treinoDao().getAllTreinos(email)
                runOnUiThread {
                    treinosAdapter.submitList(treinosDoUsuario)
                }
            }
        }
    }

    private fun adicionarTreino(nome: String, iconeResId: Int, tipo: TipoTreino) {
        emailUsuarioLogado?.let { email ->
            // 3. Ao criar um novo treino, "etiqueta" com o e-mail do dono
            val novoTreino = TreinoEntity(
                userOwnerEmail = email,
                nome = nome,
                iconeResId = iconeResId,
                tipoDeTreino = tipo
            )
            lifecycleScope.launch {
                db.treinoDao().insertTreino(novoTreino)
                carregarTreinos() // Recarrega a lista para mostrar o novo item
            }
        }
    }

    // --- MODO DE EXCLUSÃO (sem alterações na lógica) ---
    private fun ativarModoExclusao(primeiroItem: TreinoEntity) {
        modoExclusaoAtivo = true
        treinosAdapter.modoExclusaoAtivo = true
        fabAddTreino.visibility = View.GONE
        btnApagarTreinos.visibility = View.VISIBLE
        clickOutsideView.visibility = View.VISIBLE
        toggleSelecao(primeiroItem)
    }

    private fun desativarModoExclusao() {
        modoExclusaoAtivo = false
        treinosAdapter.limparSelecao()
        fabAddTreino.visibility = View.VISIBLE
        btnApagarTreinos.visibility = View.GONE
        clickOutsideView.visibility = View.GONE
    }

    private fun toggleSelecao(treino: TreinoEntity) {
        treino.isSelected = !treino.isSelected
        treinosAdapter.notifyDataSetChanged()
        if (modoExclusaoAtivo && treinosAdapter.getSelecionados().isEmpty()) {
            desativarModoExclusao()
        }
    }

    private fun confirmarExclusao(treinosParaApagar: List<TreinoEntity>) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Treinos")
            .setMessage("Tem certeza que deseja apagar ${treinosParaApagar.size} treino(s)?")
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch {
                    val idsParaApagar = treinosParaApagar.map { it.id }
                    db.treinoDao().deleteTreinosByIds(idsParaApagar)
                    carregarTreinos() // Recarrega do banco
                }
                desativarModoExclusao()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- DIÁLOGOS (sem alterações na lógica) ---
    private fun exibirDialogoAdicionarTreino() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_adicionar_treino, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btn_academia).setOnClickListener {
            adicionarTreino("Academia", R.drawable.ic_academia, TipoTreino.ACADEMIA); dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_corrida).setOnClickListener {
            adicionarTreino("Corrida", R.drawable.ic_corrida, TipoTreino.CORRIDA); dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_esportes).setOnClickListener {
            adicionarTreino("Esportes", R.drawable.ic_esportes, TipoTreino.ESPORTES); dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_personalizado).setOnClickListener {
            exibirDialogoPersonalizado(); dialog.dismiss()
        }
        dialog.show()
    }

    private fun exibirDialogoPersonalizado() {
        val editText = EditText(this).apply { hint = "Nome do treino" }
        AlertDialog.Builder(this).setTitle("Treino Personalizado").setView(editText)
            .setPositiveButton("Adicionar") { _, _ ->
                val nomeTreino = editText.text.toString().trim()
                if (nomeTreino.isNotEmpty()) {
                    adicionarTreino(nomeTreino, R.drawable.ic_personalizado, TipoTreino.GENERICO)
                }
            }.setNegativeButton("Cancelar", null).show()
    }

    // --- CONFIGURAÇÕES DE UI ---
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun setupNavigationBar() {
        val navBar = findViewById<LinearLayout>(R.id.navigation_bar)
        navBar.findViewById<LinearLayout>(R.id.botao_inicio).setOnClickListener {
            startActivity(Intent(this, Bemvindouser::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_anotacoes).setOnClickListener {
            startActivity(Intent(this, anotacoes::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_habitos).setOnClickListener {
            startActivity(Intent(this, habitos::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_treinos).setOnClickListener { /* Já está aqui */ }
        navBar.findViewById<LinearLayout>(R.id.botao_cronometro).setOnClickListener {
            startActivity(Intent(this, CronometroActivity::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_sugestoes).setOnClickListener {
            startActivity(Intent(this, SugestaoUser::class.java))
        }
    }
}
