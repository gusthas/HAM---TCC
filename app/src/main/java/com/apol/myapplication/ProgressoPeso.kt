package com.apol.myapplication

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apol.myapplication.data.model.User
import com.apol.myapplication.data.model.WeightEntry
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class ProgressoPeso : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var currentUser: User? = null
    private lateinit var weightChart: SimpleLineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // AQUI ESTÁ A CORREÇÃO: Usando o layout correto que contém os IDs
        setContentView(R.layout.activity_progresso_peso)

        db = AppDatabase.getDatabase(this)
        weightChart = findViewById(R.id.weight_chart) // Assumindo que R.id.weight_chart existe em activity_imc.xml

        loadUserData()

        findViewById<Button>(R.id.btn_add_weight).setOnClickListener {
            showAddWeightDialog()
        }

        findViewById<ImageButton>(R.id.btn_voltar_imc).setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null)

            if (userEmail != null) {
                currentUser = db.userDao().getUserByEmail(userEmail)
                val weightHistory = db.userDao().getWeightHistory(userEmail)

                runOnUiThread {
                    currentUser?.let { displayImc(it) }
                    updateChartData(weightHistory)
                }
            }
        }
    }

    private fun displayImc(user: User) {
        val textNumero = findViewById<TextView>(R.id.text_resultado_imc_numero)
        val textClassificacao = findViewById<TextView>(R.id.text_resultado_imc_classificacao)
        val cardResultado = findViewById<LinearLayout>(R.id.card_resultado_imc)

        if (user.altura > 0 && user.peso > 0) {
            val imc = user.peso / (user.altura * user.altura)
            val df = DecimalFormat("#.#")
            textNumero.text = df.format(imc)
            val (classificacao, cor) = getClassificacaoImc(imc)
            textClassificacao.text = classificacao
            textClassificacao.setTextColor(cor)
            cardResultado.visibility = View.VISIBLE // Mostra o resultado
        } else {
            cardResultado.visibility = View.GONE // Esconde se não houver dados
        }
    }

    private fun updateChartData(history: List<WeightEntry>) {
        if (history.isEmpty()) {
            weightChart.setData(emptyList(), emptyList())
            return
        }

        val points = history.map { it.weight.toInt() }
        val labels = history.map {
            SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(it.date))
        }

        weightChart.setData(points, labels)
    }

    private fun showAddWeightDialog() {
        val editText = EditText(this).apply {
            hint = "Digite seu peso atual (ex: 75.5)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        AlertDialog.Builder(this)
            .setTitle("Registrar Novo Peso")
            .setView(editText)
            .setPositiveButton("Salvar") { _, _ ->
                val weightStr = editText.text.toString().replace(',', '.')
                val weight = weightStr.toFloatOrNull()

                if (weight != null && currentUser != null) {
                    lifecycleScope.launch {
                        val newEntry = WeightEntry(userEmail = currentUser!!.email, weight = weight)
                        db.userDao().insertWeightEntry(newEntry)

                        val updatedUser = currentUser!!.copy(peso = weight.toInt())
                        db.userDao().updateUser(updatedUser)

                        runOnUiThread {
                            Toast.makeText(this@ProgressoPeso, "Peso salvo!", Toast.LENGTH_SHORT).show()
                            loadUserData()
                        }
                    }
                } else {
                    Toast.makeText(this, "Valor inválido.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun getClassificacaoImc(imc: Float): Pair<String, Int> {
        return when {
            imc < 18.5 -> "Abaixo do peso" to Color.YELLOW
            imc < 25 -> "Peso Normal" to Color.GREEN
            imc < 30 -> "Sobrepeso" to Color.rgb(255, 165, 0)
            else -> "Obesidade" to Color.RED
        }
    }
}