package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apol.myapplication.data.model.User
import kotlinx.coroutines.launch

class RecuperarSenhaActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var userParaRecuperar: User? = null

    // Referências para os layouts de cada etapa
    private lateinit var layoutEtapaEmail: LinearLayout
    private lateinit var layoutEtapaPergunta: LinearLayout
    private lateinit var layoutEtapaNovaSenha: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_senha)

        db = AppDatabase.getDatabase(this)

        // --- Encontra os componentes do layout ---
        layoutEtapaEmail = findViewById(R.id.layoutEtapaEmail)
        layoutEtapaPergunta = findViewById(R.id.layoutEtapaPergunta)
        layoutEtapaNovaSenha = findViewById(R.id.layoutEtapaNovaSenha)

        val editTextEmailRecuperar = findViewById<EditText>(R.id.editTextEmailRecuperar)
        val btnVerificarEmail = findViewById<Button>(R.id.btnVerificarEmail)

        val textViewPerguntaSecreta = findViewById<TextView>(R.id.textViewPerguntaSecreta)
        val editTextRespostaSecreta = findViewById<EditText>(R.id.editTextRespostaSecreta)
        val btnVerificarResposta = findViewById<Button>(R.id.btnVerificarResposta)

        val editTextNovaSenha = findViewById<EditText>(R.id.editTextNovaSenha)
        val btnSalvarNovaSenha = findViewById<Button>(R.id.btnSalvarNovaSenha)

        val btnVoltarLogin = findViewById<Button>(R.id.buttonVoltarLogin)

        // --- Configura os cliques dos botões ---

        // ETAPA 1: Verificar E-mail
        btnVerificarEmail.setOnClickListener {
            val email = editTextEmailRecuperar.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, digite seu e-mail.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = db.userDao().getUserByEmail(email)
                runOnUiThread {
                    if (user != null) {
                        userParaRecuperar = user
                        textViewPerguntaSecreta.text = user.perguntaSecreta
                        layoutEtapaEmail.visibility = View.GONE
                        layoutEtapaPergunta.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this@RecuperarSenhaActivity, "E-mail não encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // ETAPA 2: Verificar Resposta
        btnVerificarResposta.setOnClickListener {
            val resposta = editTextRespostaSecreta.text.toString().trim()
            if (resposta.isEmpty()) {
                Toast.makeText(this, "Por favor, digite sua resposta.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (resposta.equals(userParaRecuperar?.respostaSecreta, ignoreCase = true)) {
                layoutEtapaPergunta.visibility = View.GONE
                layoutEtapaNovaSenha.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Resposta secreta incorreta.", Toast.LENGTH_SHORT).show()
            }
        }

        // ETAPA 3: Salvar Nova Senha
        btnSalvarNovaSenha.setOnClickListener {
            val novaSenha = editTextNovaSenha.text.toString().trim()

            if (!isPasswordValid(novaSenha)) {
                Toast.makeText(this, "A nova senha deve ter entre 6 e 15 caracteres.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                userParaRecuperar?.let { user ->
                    val updatedUser = user.copy(password = novaSenha)
                    db.userDao().updateUser(updatedUser)
                    runOnUiThread {
                        Toast.makeText(this@RecuperarSenhaActivity, "Senha alterada com sucesso! Faça o login.", Toast.LENGTH_LONG).show()
                        finish() // Volta para a tela de login
                    }
                }
            }
        }

        btnVoltarLogin.setOnClickListener {
            finish() // Fecha a tela atual e volta para a de login
        }
    }

    // Função de validação de senha que já usamos antes
    private fun isPasswordValid(password: String): Boolean {
        return password.length in 6..15
    }
}