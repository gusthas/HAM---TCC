// Substitua o conteúdo COMPLETO do seu arquivo Bemvindouser.kt
package com.apol.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class Bemvindouser : AppCompatActivity() {

    private lateinit var viewModel: NotesViewModel
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bemvindouser)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(NotesViewModel::class.java)
        db = AppDatabase.getDatabase(this) // Pega a instância do banco

        // --- CHAMADA DAS FUNÇÕES DE CONFIGURAÇÃO ---
        carregarDadosDoUsuarioEAtualizarTela() // Personaliza o "Bem-vindo"
        atualizarDataComSimbolo()
        configurarNavBar()
        configurarBotaoNovoHabito()
        configurarBotaoAnotacaoRapida()
        configurarBotaoNovoBloco()
        carregarTop3Habitos()
        carregarTop3Blocos()
    }

    private fun carregarDadosDoUsuarioEAtualizarTela() {
        val welcomeTextView = findViewById<TextView>(R.id.welcome_text)

        // Pega o e-mail do usuário logado que salvamos no SharedPreferences
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null)

        if (userEmail == null) {
            welcomeTextView.text = "Bem-vindo(a)!"
            return
        }

        lifecycleScope.launch {
            // Usa o e-mail para buscar o usuário completo no banco de dados
            val user = db.userDao().getUserByEmail(userEmail)

            runOnUiThread {
                if (user != null && user.nome.isNotEmpty()) {
                    val saudacao = if (user.genero.equals("Feminino", ignoreCase = true)) {
                        "Bem-vinda"
                    } else {
                        "Bem-vindo"
                    }
                    welcomeTextView.text = "$saudacao, ${user.nome}!"
                } else {
                    welcomeTextView.text = "Bem-vindo(a)!"
                }
            }
        }
    }

    private fun configurarNavBar() {
        val navBar = findViewById<LinearLayout>(R.id.navigation_bar)
        navBar.findViewById<LinearLayout>(R.id.botao_inicio).setOnClickListener {}
        navBar.findViewById<LinearLayout>(R.id.botao_anotacoes).setOnClickListener {
            startActivity(Intent(this, anotacoes::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_habitos).setOnClickListener {
            startActivity(Intent(this, habitos::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_treinos).setOnClickListener {
            startActivity(Intent(this, treinos::class.java))
        }
        // NOTE: O botão cronometro foi removido do seu XML da tela de bem-vindo,
        // mas se você o tiver em outro layout, o código pode ficar.
        // navBar.findViewById<LinearLayout>(R.id.botao_cronometro).setOnClickListener {
        //     startActivity(Intent(this, CronometroActivity::class.java))
        // }
        navBar.findViewById<LinearLayout>(R.id.botao_configuracoes).setOnClickListener {
            startActivity(Intent(this, configuracoes::class.java))
        }
    }

    private fun configurarBotaoNovoHabito() {
        val widgetHabitos = findViewById<View>(R.id.widgetHabitos)
        val btnNovoHabito = widgetHabitos.findViewById<Button>(R.id.btnNovoHabito)
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
        val widgetBlocos = findViewById<View>(R.id.widgetBlocos)
        val btnNovoBloco = widgetBlocos.findViewById<Button>(R.id.btnNovoBloco)
        btnNovoBloco.setOnClickListener {
            val intent = Intent(this, anotacoes::class.java)
            intent.putExtra("modo_blocos_ativo", true)
            intent.putExtra("abrir_selecao_blocos", true) // Corrigido de "abrir_dialogo_novo_bloco" para o que usamos na tela de anotações
            startActivity(intent)
        }
    }

    private fun carregarTop3Blocos() {
        val widgetBlocos = findViewById<View>(R.id.widgetBlocos)
        val blocosViews = listOf(
            widgetBlocos.findViewById<LinearLayout>(R.id.bloco_nota_1),
            widgetBlocos.findViewById<LinearLayout>(R.id.bloco_nota_2),
            widgetBlocos.findViewById<LinearLayout>(R.id.bloco_nota_3)
        )
        val blocosTexts = listOf(
            widgetBlocos.findViewById<TextView>(R.id.bloco_text_1),
            widgetBlocos.findViewById<TextView>(R.id.bloco_text_2),
            widgetBlocos.findViewById<TextView>(R.id.bloco_text_3)
        )

        lifecycleScope.launch {
            viewModel.blocos.collect { listaDeBlocos ->
                for (i in blocosViews.indices) {
                    val blocoData = listaDeBlocos.getOrNull(i)
                    val view = blocosViews[i]
                    val textView = blocosTexts[i]

                    if (blocoData != null) {
                        textView.text = blocoData.nome
                        view.setOnClickListener {
                            val intent = Intent(this@Bemvindouser, anotacoes::class.java)
                            intent.putExtra("modo_blocos_ativo", true)
                            // Adicionar lógica para abrir o bloco específico se necessário
                            startActivity(intent)
                        }
                        view.visibility = View.VISIBLE
                    } else {
                        view.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun carregarTop3Habitos() {
        val prefs = getSharedPreferences("habitos_prefs", Context.MODE_PRIVATE)
        val habitsString = prefs.getString("habits_list_ordered", null)
        val listaHabitos = habitsString?.split(";;;") ?: emptyList()
        val top3 = listaHabitos.take(3)

        val widgetHabitos = findViewById<View>(R.id.widgetHabitos)
        val blocos = listOf(
            widgetHabitos.findViewById<LinearLayout>(R.id.blocoLeitura),
            widgetHabitos.findViewById<LinearLayout>(R.id.blocoExercicio),
            widgetHabitos.findViewById<LinearLayout>(R.id.blocoMeditar)
        )

        for (i in blocos.indices) {
            val bloco = blocos[i]
            val habito = top3.getOrNull(i)
            val imageView = bloco.getChildAt(0) as ImageView
            val textView = bloco.getChildAt(1) as TextView

            if (habito != null && habito.isNotEmpty()) {
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
                bloco.visibility = View.GONE
            }
        }
    }

    private fun incrementarHabito(nomeHabito: String) {
        val prefs = getSharedPreferences("habitos_prefs", Context.MODE_PRIVATE)
        val hoje = getHoje()
        val chave = "${nomeHabito}_$hoje"
        val atual = prefs.getInt(chave, 0)
        prefs.edit().putInt(chave, atual + 1).apply()
        Toast.makeText(this, "Progresso adicionado para \"$nomeHabito\"!", Toast.LENGTH_SHORT).show()
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
        override fun setAlpha(alpha: Int) { paint.alpha = alpha }
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
        override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
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