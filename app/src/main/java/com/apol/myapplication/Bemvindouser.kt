package com.apol.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class Bemvindouser : AppCompatActivity() {

    private lateinit var viewModel: NotesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bemvindouser)
        atualizarDataComSimbolo()
        configurarBotaoNovoBloco()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NotesViewModel::class.java)

        configurarNavBar()
        configurarBotaoNovoHabito()
        configurarBotaoAnotacaoRapida()
        configurarBotaoNovoBloco() // <-- Adicionado aqui
        carregarTop3Habitos()
    }

    private fun configurarNavBar() {
        val navBar = findViewById<LinearLayout>(R.id.navigation_bar)

        navBar.findViewById<LinearLayout>(R.id.botao_inicio).setOnClickListener {

        }

        navBar.findViewById<LinearLayout>(R.id.botao_anotacoes).setOnClickListener {
            startActivity(Intent(this, anotacoes::class.java))
        }

        navBar.findViewById<LinearLayout>(R.id.botao_habitos).setOnClickListener {
            startActivity(Intent(this, habitos::class.java))
        }

        navBar.findViewById<LinearLayout>(R.id.botao_treinos).setOnClickListener {
            startActivity(Intent(this, treinos::class.java))
        }

        navBar.findViewById<LinearLayout>(R.id.botao_progresso).setOnClickListener {
            startActivity(Intent(this, progresso::class.java))
        }

        navBar.findViewById<LinearLayout>(R.id.botao_configuracoes).setOnClickListener {
            startActivity(Intent(this, configuracoes::class.java))
        }
    }

    private fun configurarBotaoNovoHabito() {
        val btnNovoHabito = findViewById<Button>(R.id.btnNovoHabito)
        btnNovoHabito.setOnClickListener {
            val intent = Intent(this, habitos::class.java)
            intent.putExtra("abrir_dialogo_novo_habito", true)
            startActivity(intent)
        }
    }

    private fun configurarBotaoAnotacaoRapida() {
        val input = findViewById<EditText>(R.id.edittext_thought)
        val botaoAdd = findViewById<TextView>(R.id.button_add_thought)

        botaoAdd.setOnClickListener {
            val texto = input.text.toString().trim()
            if (texto.isNotEmpty()) {
                viewModel.addNote(texto)
                input.text.clear()
                Toast.makeText(this, "Anotação salva!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Digite algo primeiro!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarBotaoNovoBloco() {
        val btnNovoBloco = findViewById<Button>(R.id.btnNovoBloco)
        btnNovoBloco.setOnClickListener {
            val intent = Intent(this, anotacoes::class.java)
            intent.putExtra("modo_blocos_ativo", true)
            intent.putExtra("abrir_selecao_blocos", true)
            startActivity(intent)
        }
    }


    private fun carregarTop3Habitos() {
        val prefs = getSharedPreferences("habitos_prefs", Context.MODE_PRIVATE)
        val listaHabitos = prefs.getStringSet("habits_list", emptySet())?.toList() ?: return
        val top3 = listaHabitos.take(3)

        val blocos = listOf(
            findViewById<LinearLayout>(R.id.blocoLeitura),
            findViewById<LinearLayout>(R.id.blocoExercicio),
            findViewById<LinearLayout>(R.id.blocoMeditar)
        )

        for (i in blocos.indices) {
            val bloco = blocos[i]
            val habito = top3.getOrNull(i)

            val imageView = bloco.getChildAt(0) as ImageView
            val textView = bloco.getChildAt(1) as TextView

            if (habito != null) {
                val emoji = extrairEmoji(habito)
                val nome = removerEmoji(habito)

                if (emoji.isNotEmpty()) {
                    imageView.setImageDrawable(TextDrawable(this, emoji))
                    imageView.visibility = View.VISIBLE
                } else {
                    imageView.visibility = View.GONE
                }

                textView.text = nome

                bloco.setOnClickListener {
                    incrementarHabito(habito)
                }
            } else {
                imageView.setImageResource(R.drawable.ic_book)
                imageView.visibility = View.VISIBLE
                textView.text = "Sem hábito"
                bloco.setOnClickListener {
                    Toast.makeText(this, "Nenhum hábito aqui ainda!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun incrementarHabito(nomeHabito: String) {
        val prefs = getSharedPreferences("habitos_prefs", Context.MODE_PRIVATE)
        val hoje = getHoje()
        val chave = "${nomeHabito}_$hoje"
        val atual = prefs.getInt(chave, 0)
        prefs.edit().putInt(chave, atual + 1).apply()
        Toast.makeText(this, "Progresso adicionado para \"$nomeHabito\"! Total hoje: ${atual + 1}", Toast.LENGTH_SHORT).show()
    }

    private fun extrairEmoji(texto: String): String {
        val regex = Regex("^\\p{So}")
        return regex.find(texto)?.value ?: ""
    }

    private fun removerEmoji(texto: String): String {
        val regex = Regex("^\\p{So}\\s*")
        return texto.replaceFirst(regex, "")
    }

    private fun getHoje(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date())
    }

    class TextDrawable(context: Context, private val text: String) : Drawable() {
        private val paint = Paint()

        init {
            paint.color = Color.WHITE
            paint.textSize = 64f
            paint.isAntiAlias = true
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.DEFAULT_BOLD
        }

        override fun draw(canvas: Canvas) {
            val bounds = bounds
            val x = bounds.centerX().toFloat()
            val y = bounds.centerY() - (paint.descent() + paint.ascent()) / 2
            canvas.drawText(text, x, y, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }
    }

    private fun atualizarDataComSimbolo() {
        val calendar = Calendar.getInstance()
        val hora = calendar.get(Calendar.HOUR_OF_DAY)

        val simbolo = if (hora in 6..17) "☀️" else "🌙"

        val formato = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR"))
        val dataFormatada = formato.format(calendar.time).replaceFirstChar { it.uppercase() }

        val dataTextView = findViewById<TextView>(R.id.date_text)
        dataTextView.text = "$dataFormatada  $simbolo"
    }
}
