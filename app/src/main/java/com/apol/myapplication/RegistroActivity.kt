package com.apol.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.content.Intent

class RegistroActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicializa o Firebase Auth
        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val senhaEditText = findViewById<EditText>(R.id.editTextPassword)
        val registrarButton = findViewById<Button>(R.id.buttonRegistrar)
        val voltarButton = findViewById<Button>(R.id.buttonVoltar)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).addMigrations(AppDatabase.MIGRATION_1_2) // Adicionando a migração para versão 2
            .build()

        userDao = db.userDao()

        registrarButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val senha = senhaEditText.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Registro com Firebase Authentication
            auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Usuário registrado com sucesso no Firebase
                        val user = auth.currentUser
                        user?.let {
                            // Usuário criado com sucesso no Firebase
                            val userId = user.uid // UID do Firebase

                            // Salva o usuário no Room, mas sem dados adicionais (nome, idade, etc.)
                            val novoUser = User(
                                email = email,
                                password = senha,
                                userId = userId,
                                nome = "",  // Nome vazio por enquanto
                                idade = 0,  // Idade vazia por enquanto
                                peso = 0,   // Peso vazio por enquanto
                                altura = 0f, // Altura vazia por enquanto
                                genero = ""  // Gênero vazio por enquanto
                            )

                            lifecycleScope.launch {
                                userDao.insertUser(novoUser)

                                runOnUiThread {
                                    Toast.makeText(
                                        this@RegistroActivity,
                                        "Usuário registrado com sucesso!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(this@RegistroActivity, MainActivity::class.java))
                                    finish()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@RegistroActivity,
                            "Erro ao registrar usuário: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        voltarButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
