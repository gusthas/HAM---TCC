package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa o Firebase Auth
        auth = Firebase.auth

        val etEmail = findViewById<EditText>(R.id.editTextusuario)
        val etSenha = findViewById<EditText>(R.id.editTextsenha)
        val btnLogin = findViewById<Button>(R.id.buttonavancarinfousuario)
        val tvCadastrese = findViewById<TextView>(R.id.textView8)

        // Configura o banco Room com migração
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database" // Nome do banco de dados
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)  // Adicionando a migração para a versão 2
            .build()

        userDao = db.userDao()

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val senha = etSenha.text.toString()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        val authResult = auth.signInWithEmailAndPassword(email, senha).await()

                        if (authResult.user != null) {
                            val user = auth.currentUser
                            user?.let {
                                val userId = it.uid

                                // Verifica se já existe um usuário completo no banco
                                val existingUser = userDao.getUserById(userId)

                                runOnUiThread {
                                    Toast.makeText(this@MainActivity, "Login bem-sucedido", Toast.LENGTH_SHORT).show()

                                    val proximaTela = if (
                                        existingUser != null &&
                                        existingUser.nome.isNotEmpty() &&
                                        existingUser.idade > 0 &&
                                        existingUser.peso > 0 &&
                                        existingUser.altura > 0 &&
                                        existingUser.genero.isNotEmpty()
                                    ) {
                                        // Já preencheu os dados → vai direto para Bem-vindo
                                        Intent(this@MainActivity, bemvindo::class.java)
                                    } else {
                                        // Ainda não preencheu → vai para Info Usuário
                                        Intent(this@MainActivity, infousuario::class.java)
                                    }

                                    startActivity(proximaTela)
                                    finish()
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "Email ou senha incorretos", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Erro ao tentar fazer login: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }


        // Navega para a tela de cadastro
        tvCadastrese.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }
}

