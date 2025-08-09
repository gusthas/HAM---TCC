
package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        val etEmail = findViewById<EditText>(R.id.editTextusuario)
        val etSenha = findViewById<EditText>(R.id.editTextsenha)
        val btnLogin = findViewById<Button>(R.id.buttonavancarinfousuario)
        val tvCadastrese = findViewById<TextView>(R.id.textView8)
        val btnAdmin = findViewById<Button>(R.id.btn_admin_users)

        btnAdmin.setOnClickListener {
            startActivity(Intent(this, AdminUsersActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // 1. Busca o usuário pelo e-mail no banco local
                val user = userDao.getUserByEmail(email)

                // 2. Verifica se o usuário foi encontrado e se a senha confere
                if (user != null && user.password == senha) {
                    // 3. Login bem-sucedido! Salva a sessão.
                    // Usaremos SharedPreferences para "lembrar" do e-mail do usuário logado
                    val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    prefs.edit().putString("LOGGED_IN_USER_EMAIL", email).apply()

                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Login bem-sucedido", Toast.LENGTH_SHORT).show()

                        // Decide para qual tela ir
                        val proximaTela = if (user.nome.isNotEmpty() && user.idade > 0) {
                            Intent(this@MainActivity, Bemvindouser::class.java)
                        } else {
                            Intent(this@MainActivity, infousuario::class.java)
                        }
                        startActivity(proximaTela)
                        finish()
                    }
                } else {
                    // 4. Se não encontrou ou a senha está errada
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        tvCadastrese.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }
}