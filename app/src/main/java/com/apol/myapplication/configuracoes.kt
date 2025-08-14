package com.apol.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.apol.myapplication.data.model.User
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class configuracoes : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var db: AppDatabase
    private lateinit var nomeUsuarioText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracoes)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AppDatabase.getDatabase(this)
        nomeUsuarioText = findViewById(R.id.nome_usuario_text)

        configurarNavBar()
        configurarBotoes()
    }

    override fun onResume() {
        super.onResume()
        // Recarrega os dados caso o usuário tenha editado o perfil e voltado
        carregarDadosDoUsuario()
    }

    private fun carregarDadosDoUsuario() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null)

        if (userEmail != null) {
            lifecycleScope.launch {
                val user = db.userDao().getUserByEmail(userEmail)
                user?.let {
                    runOnUiThread {
                        nomeUsuarioText.text = it.nome
                    }
                }
            }
        } else {
            // Se por algum motivo não encontrar um usuário logado, volta para a tela de login
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun configurarBotoes() {
        findViewById<ImageView>(R.id.imagem_perfil).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }


        findViewById<LinearLayout>(R.id.bloco_titulo_perfil).setOnClickListener {
            exibirDialogoEditarPerfil()
        }

        findViewById<LinearLayout>(R.id.opcao_calcular_imc).setOnClickListener {
            startActivity(Intent(this, ProgressoPeso::class.java))
        }

        findViewById<Button>(R.id.button_sair).setOnClickListener {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            prefs.edit().remove("LOGGED_IN_USER_EMAIL").apply()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        findViewById<LinearLayout>(R.id.opcao_alterar_senha).setOnClickListener {
            exibirDialogoAlterarSenha()
        }

        findViewById<LinearLayout>(R.id.sobre_app).setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sobre_app, null)
            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()

            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            dialogView.findViewById<Button>(R.id.btn_fechar_sobre).setOnClickListener {
                alertDialog.dismiss()
            }
            alertDialog.show()
        }
    }

    private fun exibirDialogoEditarPerfil() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_perfil, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val editNome = dialogView.findViewById<EditText>(R.id.edit_nome_dialog)
        val editIdade = dialogView.findViewById<EditText>(R.id.edit_idade_dialog)
        val editPeso = dialogView.findViewById<EditText>(R.id.edit_peso_dialog)
        val editAltura = dialogView.findViewById<EditText>(R.id.edit_altura_dialog)
        val radioGroupGenero = dialogView.findViewById<RadioGroup>(R.id.radioGroupGeneroDialog)
        val radioFeminino = dialogView.findViewById<RadioButton>(R.id.radioFemininoDialog)
        val radioMasculino = dialogView.findViewById<RadioButton>(R.id.radioMasculinoDialog)
        val btnSalvar = dialogView.findViewById<Button>(R.id.btn_salvar)
        val btnFechar = dialogView.findViewById<Button>(R.id.btn_fechar)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null) ?: return

        lifecycleScope.launch {
            val user = db.userDao().getUserByEmail(userEmail)
            user?.let {
                runOnUiThread {
                    editNome.setText(it.nome)
                    editIdade.setText(it.idade.toString())
                    editPeso.setText(it.peso.toString())
                    editAltura.setText(it.altura.toString())
                    if (it.genero.equals("Feminino", ignoreCase = true)) {
                        radioFeminino.isChecked = true
                    } else {
                        radioMasculino.isChecked = true
                    }
                }
            }
        }

        btnSalvar.setOnClickListener {
            val nome = editNome.text.toString()
            val idade = editIdade.text.toString().toIntOrNull() ?: 0
            val peso = editPeso.text.toString().toIntOrNull() ?: 0
            val altura = editAltura.text.toString().toFloatOrNull() ?: 0f
            val genero = if (radioFeminino.isChecked) "Feminino" else "Masculino"

            lifecycleScope.launch {
                val user = db.userDao().getUserByEmail(userEmail)
                if (user != null) {
                    val updatedUser = user.copy(nome = nome, idade = idade, peso = peso, altura = altura, genero = genero)
                    db.userDao().updateUser(updatedUser)
                    runOnUiThread {
                        Toast.makeText(this@configuracoes, "Dados atualizados!", Toast.LENGTH_SHORT).show()
                        nomeUsuarioText.text = nome
                        alertDialog.dismiss()
                    }
                }
            }
        }

        btnFechar.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun exibirDialogoAlterarSenha() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_alterar_senha, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val layoutSenhaAtual = dialogView.findViewById<LinearLayout>(R.id.layoutEtapaSenhaAtual)
        val layoutNovaSenha = dialogView.findViewById<LinearLayout>(R.id.layoutEtapaNovaSenhaConfig)
        val editSenhaAtual = dialogView.findViewById<EditText>(R.id.editTextSenhaAtual)
        val btnVerificarSenha = dialogView.findViewById<Button>(R.id.btnVerificarSenhaAtual)
        val editNovaSenha = dialogView.findViewById<EditText>(R.id.editTextNovaSenhaConfig)
        val btnSalvarNovaSenha = dialogView.findViewById<Button>(R.id.btnSalvarNovaSenhaConfig)
        val btnFechar = dialogView.findViewById<Button>(R.id.btn_fechar_alterar_senha)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null) ?: return

        btnVerificarSenha.setOnClickListener {
            val senhaAtualDigitada = editSenhaAtual.text.toString()
            lifecycleScope.launch {
                val user = db.userDao().getUserByEmail(userEmail)
                runOnUiThread {
                    if (user != null && user.password == senhaAtualDigitada) {
                        layoutSenhaAtual.visibility = View.GONE
                        layoutNovaSenha.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this@configuracoes, "Senha atual incorreta.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnSalvarNovaSenha.setOnClickListener {
            val novaSenha = editNovaSenha.text.toString().trim()
            if (novaSenha.length < 6 || novaSenha.length > 15) {
                Toast.makeText(this, "A nova senha deve ter entre 6 e 15 caracteres.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val user = db.userDao().getUserByEmail(userEmail)
                user?.let {
                    val updatedUser = it.copy(password = novaSenha)
                    db.userDao().updateUser(updatedUser)
                    runOnUiThread {
                        Toast.makeText(this@configuracoes, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            }
        }

        btnFechar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            val imagemPerfil = findViewById<ImageView>(R.id.imagem_perfil)
            Glide.with(this).load(selectedImageUri).circleCrop().into(imagemPerfil)
        }
    }

    private fun configurarNavBar() {
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
        navBar.findViewById<LinearLayout>(R.id.botao_sugestoes).setOnClickListener {

        }
    }
}


