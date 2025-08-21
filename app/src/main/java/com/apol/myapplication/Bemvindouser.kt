package com.apol.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.Habito
import com.apol.myapplication.data.model.HabitoProgresso
import com.apol.myapplication.NotesViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class Bemvindouser : AppCompatActivity() {

    private lateinit var viewModel: NotesViewModel
    private lateinit var db: AppDatabase
    private var emailUsuarioLogado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- ESTE É O BLOCO DE CÓDIGO QUE RESOLVE O PROBLEMA ---

        setContentView(R.layout.activity_bemvindouser)
    /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 2. Aplica o padding para o conteúdo não ficar embaixo das barras
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        } */
        // --- FIM DO BLOCO DE CORREÇÃO ---

        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        db = AppDatabase.getDatabase(this)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        emailUsuarioLogado = prefs.getString("LOGGED_IN_USER_EMAIL", null)

        if (emailUsuarioLogado == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        viewModel.setCurrentUser(emailUsuarioLogado!!)

        carregarDadosDoUsuarioEAtualizarTela()
        atualizarDataComSimbolo()
        configurarNavBar()
        configurarBotaoPerfil()
        configurarBotaoNovoHabito()
        configurarBotaoAnotacaoRapida()
        configurarBotaoNovoBloco()
    }

    override fun onResume() {
        super.onResume()
        carregarTop3Habitos()
        carregarTop3Blocos()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    private fun carregarTop3Habitos() {
        val widgetHabitos = findViewById<View>(R.id.widgetHabitos)
        val slots = listOf(
            Triple(
                widgetHabitos.findViewById<View>(R.id.habito_slot_1),
                widgetHabitos.findViewById<ImageView>(R.id.habito_icon_1),
                widgetHabitos.findViewById<TextView>(R.id.habito_text_1)
            ),
            Triple(
                widgetHabitos.findViewById<View>(R.id.habito_slot_2),
                widgetHabitos.findViewById<ImageView>(R.id.habito_icon_2),
                widgetHabitos.findViewById<TextView>(R.id.habito_text_2)
            ),
            Triple(
                widgetHabitos.findViewById<View>(R.id.habito_slot_3),
                widgetHabitos.findViewById<ImageView>(R.id.habito_icon_3),
                widgetHabitos.findViewById<TextView>(R.id.habito_text_3)
            )
        )

        emailUsuarioLogado?.let { email ->
            lifecycleScope.launch {
                val habitosFavoritados = db.habitoDao().getFavoritedHabitsByUser(email)

                runOnUiThread {
                    for (i in slots.indices) {
                        val (slotView, iconView, textView) = slots[i]
                        val habito = habitosFavoritados.getOrNull(i)

                        if (habito != null) {
                            slotView.visibility = View.VISIBLE

                            // --- LÓGICA DO EMOJI ATUALIZADA PARA O WIDGET ---
                            val nomeCompleto = habito.nome
                            val emoji = extrairEmoji(nomeCompleto)
                            val nomeSemEmoji = removerEmoji(nomeCompleto)

                            textView.text = nomeSemEmoji

                            if(emoji.isNotEmpty()) {
                                iconView.setImageDrawable(TextDrawable(this@Bemvindouser, emoji))
                            } else {
                                iconView.setImageResource(R.drawable.ic_habits) // Fallback
                            }
                            // --- FIM DA LÓGICA DO EMOJI ---

                            slotView.setOnClickListener {
                                marcarHabitoComoFeito(habito)
                            }
                        } else {
                            slotView.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    // --- FUNÇÕES DE EMOJI ADICIONADAS AQUI TAMBÉM ---
    fun extrairEmoji(texto: String): String {
        val regex = Regex("^\\p{So}")
        return regex.find(texto)?.value ?: ""
    }

    fun removerEmoji(texto: String): String {
        val regex = Regex("^\\p{So}\\s*")
        return texto.replaceFirst(regex, "")
    }

    fun TextDrawable(context: Context, text: String): Drawable {
        return object : Drawable() {
            private val paint = Paint()
            init {
                paint.color = Color.WHITE
                paint.textSize = 38f // Tamanho do emoji no widget pode ser menor
                paint.isAntiAlias = true
                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.DEFAULT
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
    }
    // --- FIM DAS FUNÇÕES DE EMOJI ---

    // ... (O resto do seu código não muda, como marcarHabitoComoFeito, configurarNavBar, etc.) ...
    private fun marcarHabitoComoFeito(habito: Habito) {
        lifecycleScope.launch {
            val hoje = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            db.habitoDao().insertProgresso(HabitoProgresso(habitoId = habito.id, data = hoje))
            runOnUiThread {
                Toast.makeText(this@Bemvindouser, "Progresso adicionado para \"${habito.nome}\"!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarBotaoPerfil() {
        val btnProfile = findViewById<View>(R.id.btn_profile_settings)
        btnProfile.setOnClickListener {
            startActivity(Intent(this, configuracoes::class.java))
        }
    }

    private fun carregarDadosDoUsuarioEAtualizarTela() {
        val welcomeTextView = findViewById<TextView>(R.id.welcome_text)
        val profileImageView = findViewById<ImageView>(R.id.iv_profile_picture)
        if (emailUsuarioLogado == null) {
            welcomeTextView.text = "Bem-vindo(a)!"
            profileImageView.setImageResource(R.drawable.ic_person_placeholder)
            return
        }
        lifecycleScope.launch {
            val user = db.userDao().getUserByEmail(emailUsuarioLogado!!)
            runOnUiThread {
                if (user != null && user.nome.isNotEmpty()) {
                    val saudacao = if (user.genero.equals("Feminino", ignoreCase = true)) "Bem-vinda" else "Bem-vindo"
                    welcomeTextView.text = "$saudacao, ${user.nome}!"
                    if (!user.profilePicUri.isNullOrEmpty()) {
                        Glide.with(this@Bemvindouser).load(user.profilePicUri).apply(RequestOptions.circleCropTransform()).into(profileImageView)
                    } else {
                        val initials = user.nome.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
                        val placeholder = InitialsDrawable(initials, ContextCompat.getColor(this@Bemvindouser, R.color.roxo))
                        profileImageView.setImageDrawable(placeholder)
                    }
                } else {
                    welcomeTextView.text = "Bem-vindo(a)!"
                    profileImageView.setImageResource(R.drawable.ic_person_placeholder)
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
        navBar.findViewById<LinearLayout>(R.id.botao_cronometro).setOnClickListener {
            startActivity(Intent(this, CronometroActivity::class.java))
        }
        navBar.findViewById<LinearLayout>(R.id.botao_sugestoes).setOnClickListener {
            startActivity(Intent(this, SugestaoUser::class.java))
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
            intent.putExtra("abrir_dialogo_novo_bloco", true)
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
                            intent.putExtra("abrir_bloco_id", blocoData.id)
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