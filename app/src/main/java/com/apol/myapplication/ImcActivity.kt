// Em ImcActivity.kt
package com.apol.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apol.myapplication.data.model.User
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class ImcActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imc)

        db = AppDatabase.getDatabase(this)

        val textPeso = findViewById<TextView>(R.id.text_peso_atual)
        val textAltura = findViewById<TextView>(R.id.text_altura_atual)
        val btnCalcular = findViewById<Button>(R.id.btn_calcular_imc)
        val btnVoltar = findViewById<ImageButton>(R.id.btn_voltar_imc)

        // Carrega os dados do usuário para exibir na tela
        lifecycleScope.launch {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null)

            if (userEmail != null) {
                currentUser = db.userDao().getUserByEmail(userEmail)
                currentUser?.let { user ->
                    runOnUiThread {
                        textPeso.text = "Seu peso atual: ${user.peso} kg"
                        textAltura.text = "Sua altura: ${user.altura} m"
                    }
                }
            }
        }

        btnCalcular.setOnClickListener {
            calcularEExibirImc()
        }

        btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun calcularEExibirImc() {
        val cardResultado = findViewById<LinearLayout>(R.id.card_resultado_imc)
        val textNumero = findViewById<TextView>(R.id.text_resultado_imc_numero)
        val textClassificacao = findViewById<TextView>(R.id.text_resultado_imc_classificacao)

        currentUser?.let { user ->
            if (user.altura > 0) {
                val alturaMetros = user.altura
                val pesoKg = user.peso
                val imc = pesoKg / (alturaMetros * alturaMetros)

                val df = DecimalFormat("#.#")
                textNumero.text = df.format(imc)

                val (classificacao, cor) = getClassificacaoImc(imc)
                textClassificacao.text = classificacao
                textClassificacao.setTextColor(cor)

                cardResultado.visibility = View.VISIBLE
            }
        }
    }

    private fun getClassificacaoImc(imc: Float): Pair<String, Int> {
        return when {
            imc < 18.5 -> "Abaixo do peso" to Color.YELLOW
            imc < 24.9 -> "Peso Normal" to Color.GREEN
            imc < 29.9 -> "Sobrepeso" to Color.rgb(255, 165, 0) // Laranja
            imc < 39.9 -> "Obesidade" to Color.RED
            else -> "Obesidade Grave" to Color.RED
        }
    }
}