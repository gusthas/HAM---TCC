package com.apol.myapplication

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apol.myapplication.data.model.User
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        val etEmail = findViewById<EditText>(R.id.editTextEmail)
        val etSenha = findViewById<EditText>(R.id.editTextPassword)
        val etPergunta = findViewById<EditText>(R.id.editTextPergunta)
        val etResposta = findViewById<EditText>(R.id.editTextResposta)
        val btnRegistrar = findViewById<Button>(R.id.buttonRegistrar)
        val btnVoltar = findViewById<Button>(R.id.buttonVoltar)

        btnRegistrar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()
            val pergunta = etPergunta.text.toString().trim()
            val resposta = etResposta.text.toString().trim()

            // --- Validações ---
            if (!isEmailValid(email)) {
                Toast.makeText(this, "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isPasswordValid(senha)) {
                Toast.makeText(this, "A senha deve ter entre 6 e 15 caracteres.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pergunta.isEmpty() || resposta.isEmpty()) {
                Toast.makeText(this, "Preencha a pergunta e resposta secretas.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- Lógica de Banco de Dados (Corrigida) ---
            // Todo o bloco de acesso ao DB está dentro de uma coroutine
            lifecycleScope.launch {
                val existingUser = userDao.getUserByEmail(email)

                if (existingUser == null) {
                    // A variável 'newUser' é criada aqui dentro
                    val newUser = User(
                        email = email,
                        password = senha,
                        userId = email,
                        perguntaSecreta = pergunta,
                        respostaSecreta = resposta,
                        nome = "", idade = 0, peso = 0, altura = 0f, genero = ""
                    )
                    // A chamada 'suspend' é feita aqui dentro
                    userDao.insertUser(newUser)

                    runOnUiThread {
                        Toast.makeText(this@RegistroActivity, "Usuário criado! Faça o login.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@RegistroActivity, "Este e-mail já está em uso.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length in 6..15
    }
}