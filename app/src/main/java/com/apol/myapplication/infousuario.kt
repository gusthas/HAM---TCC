package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apol.myapplication.data.model.User
import kotlinx.coroutines.launch

class infousuario : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infousuario)

        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        val etnome = findViewById<EditText>(R.id.editTextTextnome)
        val etidade = findViewById<EditText>(R.id.editTextNumberidade)
        val etpeso = findViewById<EditText>(R.id.editTextNumber2peso)
        val etaltura = findViewById<EditText>(R.id.editTextNumberDecimalaltura)
        val radioGroupGenero = findViewById<RadioGroup>(R.id.radioGroupGenero)
        val btninfousuario = findViewById<Button>(R.id.buttonavancarinfousuario)

        btninfousuario.setOnClickListener {
            val nome = etnome.text.toString().trim()
            // ... (resto da sua lógica para pegar os dados dos campos)
            val idadeStr = etidade.text.toString().trim()
            val pesoStr = etpeso.text.toString().trim()
            val alturaStr = etaltura.text.toString().trim()
            val selectedGeneroId = radioGroupGenero.checkedRadioButtonId

            if (nome.isEmpty() || idadeStr.isEmpty() || pesoStr.isEmpty() || alturaStr.isEmpty() || selectedGeneroId == -1) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Pega o e-mail do usuário logado que salvamos no SharedPreferences
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null)

            if (userEmail == null) {
                Toast.makeText(this, "Erro: Usuário não encontrado. Faça login novamente.", Toast.LENGTH_LONG).show()
                // Opcional: Redirecionar para a tela de login
                // startActivity(Intent(this, MainActivity::class.java))
                finish()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // 1. Busca o usuário existente pelo e-mail
                val userToUpdate = userDao.getUserByEmail(userEmail)

                if (userToUpdate != null) {
                    // 2. Cria um novo objeto User com os dados atualizados
                    val updatedUser = userToUpdate.copy(
                        nome = nome,
                        idade = idadeStr.toInt(),
                        peso = pesoStr.toFloat().toInt(),
                        altura = alturaStr.toFloat(),
                        genero = findViewById<RadioButton>(selectedGeneroId).text.toString()
                    )

                    // 3. Atualiza o usuário no banco de dados
                    userDao.updateUser(updatedUser)

                    runOnUiThread {
                        Toast.makeText(this@infousuario, "Informações salvas com sucesso!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@infousuario, livro::class.java))
                        finish()
                    }
                }
            }
        }
    }
}