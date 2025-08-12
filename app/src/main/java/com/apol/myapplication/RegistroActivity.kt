package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        val btnRegistrar = findViewById<Button>(R.id.buttonRegistrar)
        val btnVoltar = findViewById<Button>(R.id.buttonVoltar)
        // O botão de deletar todos pode ser mantido se você ainda o estiver usando para testes
        // val btnDeleteAll = findViewById<Button>(R.id.buttonDeleteAllUsers)

        btnRegistrar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()
            val pergunta = findViewById<EditText>(R.id.editTextPergunta).text.toString().trim()
            val resposta = findViewById<EditText>(R.id.editTextResposta).text.toString().trim()

            if (email.isEmpty() || senha.isEmpty() || pergunta.isEmpty() || resposta.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- INÍCIO DA VALIDAÇÃO ---
            if (!isEmailValid(email)) {
                Toast.makeText(this, "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isPasswordValid(senha)) {
                Toast.makeText(this, "A senha deve ter entre 6 e 15 caracteres.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // --- FIM DA VALIDAÇÃO ---

            lifecycleScope.launch {
                val existingUser = userDao.getUserByEmail(email)

                if (existingUser == null) {
                    val newUser = User(
                        email = email,
                        password = senha,
                        userId = email,
                        perguntaSecreta = pergunta, // Salva a pergunta
                        respostaSecreta = resposta, // Salva a resposta
                        nome = "", idade = 0, peso = 0, altura = 0f, genero = ""
                    )
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

    // Função auxiliar para validar o e-mail
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Função auxiliar para validar a senha
    private fun isPasswordValid(password: String): Boolean {
        return password.length in 6..15
    }
}