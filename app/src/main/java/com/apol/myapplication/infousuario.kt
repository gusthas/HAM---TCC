package com.apol.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.content.Intent

class infousuario : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infousuario)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).build()

        userDao = db.userDao()
        auth = FirebaseAuth.getInstance()

        val etnome = findViewById<EditText>(R.id.editTextTextnome)
        val etidade = findViewById<EditText>(R.id.editTextNumberidade)
        val etpeso = findViewById<EditText>(R.id.editTextNumber2peso)
        val etaltura = findViewById<EditText>(R.id.editTextNumberDecimalaltura)
        val radioGroupGenero = findViewById<RadioGroup>(R.id.radioGroupGenero)
        val btninfousuario = findViewById<Button>(R.id.buttonavancarinfousuario)

        btninfousuario.setOnClickListener {
            val nome = etnome.text.toString().trim()
            val idadeStr = etidade.text.toString().trim()
            val pesoStr = etpeso.text.toString().trim()
            val alturaStr = etaltura.text.toString().trim()
            val selectedGeneroId = radioGroupGenero.checkedRadioButtonId


            if (nome.isEmpty() || idadeStr.isEmpty() || pesoStr.isEmpty() || alturaStr.isEmpty() || selectedGeneroId == -1) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val genero = findViewById<RadioButton>(selectedGeneroId).text.toString()
            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            val user = User(
                email = auth.currentUser?.email ?: "",
                password = "",
                userId = userId,
                nome = nome,
                idade = idadeStr.toInt(),
                peso = pesoStr.toFloat().toInt(),
                altura = alturaStr.toFloat(),
                genero = genero
            )

            lifecycleScope.launch {
                // Remove usuário antigo (caso já exista com o mesmo UID)
                userDao.deleteUserById(userId)

                // Insere o novo
                userDao.insertUser(user)

                runOnUiThread {
                    Toast.makeText(this@infousuario, "Informações salvas com sucesso!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@infousuario, bemvindo::class.java))
                    finish()
                }
            }
        }
    }
}
