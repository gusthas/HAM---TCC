package com.apol.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class configuracoes : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configuracoes)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
        navBar.findViewById<LinearLayout>(R.id.botao_treinos).setOnClickListener {
            startActivity(Intent(this, treinos::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_cronometro).setOnClickListener {
            startActivity(Intent(this, CronometroActivity::class.java))
        }

        val imagemPerfil = findViewById<ImageView>(R.id.imagem_perfil)
        val nomeUsuarioText = findViewById<TextView>(R.id.nome_usuario_text)

        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            lifecycleScope.launch {
                val user = userDao.getUserById(userId)
                user?.let {
                    runOnUiThread {
                        nomeUsuarioText.text = it.nome
                    }
                }
            }
        }

        imagemPerfil.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        val blocoTituloPerfil = findViewById<LinearLayout>(R.id.bloco_titulo_perfil)
        blocoTituloPerfil.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_perfil, null)

            val editNome = dialogView.findViewById<EditText>(R.id.edit_nome_dialog)
            val editIdade = dialogView.findViewById<EditText>(R.id.edit_idade_dialog)
            val editPeso = dialogView.findViewById<EditText>(R.id.edit_peso_dialog)
            val editAltura = dialogView.findViewById<EditText>(R.id.edit_altura_dialog)
            val editGenero = dialogView.findViewById<EditText>(R.id.edit_genero_dialog)

            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            if (userId != null) {
                lifecycleScope.launch {
                    val user = userDao.getUserById(userId)
                    user?.let {
                        editNome.setText(it.nome)
                        editIdade.setText(it.idade.toString())
                        editPeso.setText(it.peso.toString())
                        editAltura.setText(it.altura.toString())
                        editGenero.setText(it.genero)
                    }
                }
            }

            dialogView.findViewById<Button>(R.id.btn_fechar).setOnClickListener {
                alertDialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.btn_salvar).setOnClickListener {
                val nome = editNome.text.toString()
                val idade = editIdade.text.toString().toIntOrNull() ?: 0
                val peso = editPeso.text.toString().toIntOrNull() ?: 0
                val altura = editAltura.text.toString().toFloatOrNull() ?: 0f
                val genero = editGenero.text.toString()

                if (userId != null) {
                    lifecycleScope.launch {
                        val user = userDao.getUserById(userId)
                        if (user != null) {
                            val updatedUser = user.copy(
                                nome = nome,
                                idade = idade,
                                peso = peso,
                                altura = altura,
                                genero = genero
                            )
                            userDao.updateUser(updatedUser)
                            runOnUiThread {
                                Toast.makeText(this@configuracoes, "Dados atualizados!", Toast.LENGTH_SHORT).show()
                                nomeUsuarioText.text = nome
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }

            alertDialog.show()
        }

        val botaoSair = findViewById<Button>(R.id.button_sair)
        botaoSair.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        val alterarSenhaLayout = findViewById<LinearLayout>(R.id.opcao_alterar_senha)
        alterarSenhaLayout.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_alterar_senha, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            // Referências aos campos do layout
            val btnFechar = dialogView.findViewById<Button>(R.id.btn_fechar_alterar_senha)
            val btnEnviarCodigo = dialogView.findViewById<Button>(R.id.btn_enviar_codigo)
            val btnConfirmarCodigo = dialogView.findViewById<Button>(R.id.btn_confirmar_codigo)
            val layoutNovaSenha = dialogView.findViewById<LinearLayout>(R.id.layout_nova_senha)
            val btnAlterarSenha = dialogView.findViewById<Button>(R.id.btn_alterar_senha)



            btnFechar.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
        val botaoGerenciarNotificacao = findViewById<LinearLayout>(R.id.gerenciar_notificacao)
        botaoGerenciarNotificacao.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_gerenciar_notificacao, null)

            val switchNotificacoes = dialogView.findViewById<Switch>(R.id.switch_notificacoes)
            val editHorario = dialogView.findViewById<EditText>(R.id.edit_horario_notificacao)

            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)



            dialogView.findViewById<Button>(R.id.btn_fechar_notificacao).setOnClickListener {
                alertDialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.btn_salvar_notificacao).setOnClickListener {
                val notificacaoAtivada = switchNotificacoes.isChecked
                val horarioPreferido = editHorario.text.toString()



                alertDialog.dismiss()
            }

            alertDialog.show()
        }


        val sobreAppLayout = findViewById<LinearLayout>(R.id.sobre_app)
        sobreAppLayout.setOnClickListener {
            // Infla o layout do diálogo
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sobre_app, null)

            // Cria o AlertDialog e aplica fundo transparente para o layout arredondado aparecer direito
            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true) // pode fechar tocando fora
                .create()

            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            // Configura o botão fechar do diálogo
            dialogView.findViewById<Button>(R.id.btn_fechar_sobre).setOnClickListener {
                alertDialog.dismiss()
            }



            alertDialog.show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            val imagemPerfil = findViewById<ImageView>(R.id.imagem_perfil)

            // Usa Glide para carregar a imagem como circular
            Glide.with(this)
                .load(selectedImageUri)
                .circleCrop()
                .into(imagemPerfil)
        }
    }

}