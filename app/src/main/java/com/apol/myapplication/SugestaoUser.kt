package com.apol.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SugestaoUser : AppCompatActivity() {

    // --- BANCO DE DADOS DE SUGESTÕES (SIMULADO) ---
    // A função .shuffled() embaralha a lista toda vez para dar variedade
    private val listaLivros = listOf(
        "O Poder do Hábito" to "Por Charles Duhigg. Entenda como os hábitos funcionam e como mudá-los.",
        "Mindset" to "Por Carol S. Dweck. A nova psicologia do sucesso.",
        "Comece pelo Porquê" to "Por Simon Sinek. Como grandes líderes inspiram ação.",
        "O Jeito Harvard de Ser Feliz" to "de Tal Ben-Shahar. Lições do popular curso de Harvard sobre como encontrar felicidade e bem-estar de forma prática.",
        "A Montanha é Você" to "de Brianna Wiest. Uma reflexão sobre como superar a autossabotagem e transformar seus maiores desafios em sua maior força."
    ).shuffled()

    private val listaDietas = listOf(
        "Priorize Alimentos In Natura" to "Foque em frutas, legumes, verduras e grãos integrais. Eles são ricos em nutrientes e fibras, essenciais para a saúde.",
        "Beba Mais Água" to "Mantenha-se hidratado ao longo do dia com água e chás sem açúcar. A hidratação melhora o foco, a energia e a saúde da pele.",
        "Planeje suas Refeições" to "Dedique um tempo no fim de semana para planejar as refeições. Isso ajuda a fazer escolhas mais saudáveis e economiza tempo.",
        "Reduza os Ultraprocessados" to "Diminua o consumo de salgadinhos, bolachas recheadas e refrigerantes. Opte por lanches mais naturais."
    ).shuffled()

    private val listaMeditacoes = listOf(
        "5 Minutos de Atenção Plena" to "Sente-se em silêncio e foque apenas na sua respiração. Observe o ar entrando e saindo, sem julgamentos. É um ótimo começo para acalmar a mente.",
        "Escaneamento Corporal" to "Deite-se confortavelmente e leve sua atenção para cada parte do seu corpo, dos pés à cabeça, notando as sensações e relaxando cada músculo."
    ).shuffled()

    private val listaRespiracao = listOf(
        "Técnica de Respiração 4-7-8" to "Inspire pelo nariz por 4s, segure o ar por 7s e expire por 8s. Repita 3 vezes para um relaxamento imediato.",
        "Respiração Abdominal" to "Coloque uma mão sobre a barriga. Inspire profundamente pelo nariz, sentindo sua barriga se expandir e expire lentamente."
    ).shuffled()

    private val listaPodcasts = listOf(
        "Podcast: Autoconsciente" to "Apresentado por Regina Giannetti, é uma jornada sobre autoconhecimento baseada em conceitos da psicologia e da neurociência.",
        "Podcast: Estamos Bem?" to "Apresentado por Bárbara dos Anjos e Marco H., aborda o bem-estar de forma leve e acolhedora, trazendo reflexões para o dia a dia."
    ).shuffled()

    private val listaExercicios = listOf(
        "Diário de Gratidão" to "Antes de dormir, escreva três coisas pelas quais você foi grato(a) hoje. Esse simples exercício pode aumentar significativamente seu bem-estar.",
        "Desafie seu Cérebro" to "Dedique 15 minutos a um quebra-cabeça, Sudoku, palavras-cruzadas ou use um app de treino cerebral."
    ).shuffled()

    private var indices = mutableMapOf<String, Int>()

    // --- Referências para os Cards ---
    private lateinit var cardLivros: View
    private lateinit var cardDietas: View
    private lateinit var cardMeditacao: View
    private lateinit var cardRespiracao: View
    private lateinit var cardPodcasts: View
    private lateinit var cardExercicios: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sugestao_user)

        cardLivros = findViewById(R.id.card_livros)
        cardDietas = findViewById(R.id.card_dietas)
        cardMeditacao = findViewById(R.id.card_meditacao)
        cardRespiracao = findViewById(R.id.card_respiracao)
        cardPodcasts = findViewById(R.id.card_podcasts)
        cardExercicios = findViewById(R.id.card_exercicios_mentais)

        configurarVisibilidadeInicial()
        configurarTodosOsCards()
        configurarNavBar()

        findViewById<FloatingActionButton>(R.id.fab_add_sugestao).setOnClickListener {
            exibirDialogoAdicionarSugestao()
        }
    }
    private fun atualizarVisibilidadeDosCards() {

        val prefs = getSharedPreferences("user_onboarding_prefs", MODE_PRIVATE)

        cardLivros.visibility = if (prefs.getBoolean("mostrar_card_livros", true)) View.VISIBLE else View.GONE

        cardDietas.visibility = if (prefs.getBoolean("mostrar_card_dietas", false)) View.VISIBLE else View.GONE

        cardMeditacao.visibility = if (prefs.getBoolean("mostrar_card_meditacao", false)) View.VISIBLE else View.GONE

        cardRespiracao.visibility = if (prefs.getBoolean("mostrar_card_respiracao", false)) View.VISIBLE else View.GONE

        cardPodcasts.visibility = if (prefs.getBoolean("mostrar_card_podcasts", false)) View.VISIBLE else View.GONE

        cardExercicios.visibility = if (prefs.getBoolean("mostrar_card_exercicios", false)) View.VISIBLE else View.GONE

    }

    private fun configurarVisibilidadeInicial() {
        val prefs = getSharedPreferences("user_onboarding_prefs", MODE_PRIVATE)
        cardLivros.visibility = if (prefs.getBoolean("mostrar_card_livros", true)) View.VISIBLE else View.GONE
        cardDietas.visibility = if (prefs.getBoolean("mostrar_card_dietas", false)) View.VISIBLE else View.GONE
        cardMeditacao.visibility = if (prefs.getBoolean("mostrar_card_meditacao", false)) View.VISIBLE else View.GONE
        cardRespiracao.visibility = if (prefs.getBoolean("mostrar_card_respiracao", false)) View.VISIBLE else View.GONE
        cardPodcasts.visibility = if (prefs.getBoolean("mostrar_card_podcasts", false)) View.VISIBLE else View.GONE
        cardExercicios.visibility = if (prefs.getBoolean("mostrar_card_exercicios", false)) View.VISIBLE else View.GONE
    }

    private fun exibirDialogoAdicionarSugestao() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_adicionar_sugestao, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val checkLivros = dialogView.findViewById<CheckBox>(R.id.check_add_livros)
        val checkDietas = dialogView.findViewById<CheckBox>(R.id.check_add_dietas)
        val checkMeditacao = dialogView.findViewById<CheckBox>(R.id.check_add_meditacao)
        val checkRespiracao = dialogView.findViewById<CheckBox>(R.id.check_add_respiracao)
        val checkPodcasts = dialogView.findViewById<CheckBox>(R.id.check_add_podcasts)
        val checkExercicios = dialogView.findViewById<CheckBox>(R.id.check_add_exercicios)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btn_confirmar_add_sugestao)

        // Mapeia os checkboxes e cards para simplificar a lógica
        val cardMap = mapOf(
            "livros" to (checkLivros to cardLivros),
            "dietas" to (checkDietas to cardDietas),
            "meditacao" to (checkMeditacao to cardMeditacao),
            "respiracao" to (checkRespiracao to cardRespiracao),
            "podcasts" to (checkPodcasts to cardPodcasts),
            "exercicios" to (checkExercicios to cardExercicios)
        )

        cardMap.values.forEach { (checkbox, card) ->
            checkbox.isChecked = card.visibility == View.VISIBLE
        }

        btnConfirmar.setOnClickListener {
            val prefsEditor = getSharedPreferences("user_onboarding_prefs", MODE_PRIVATE).edit()

            cardMap.forEach { (key, pair) ->
                val (checkbox, _) = pair
                prefsEditor.putBoolean("mostrar_card_$key", checkbox.isChecked)
            }

            prefsEditor.apply()
            dialog.dismiss()

            // Atualiza a tela inteira após a mudança
            atualizarVisibilidadeDosCards()
            configurarTodosOsCards()
        }
        dialog.show()
    }

    private fun configurarTodosOsCards() {
        configurarCard("livros", cardLivros, R.drawable.ic_book, "Livro do Mês", listaLivros)
        configurarCard("dietas", cardDietas, R.drawable.ic_food, "Dica de Dieta", listaDietas)
        configurarCard("meditacao", cardMeditacao, R.drawable.ic_meditation, "Prática de Meditação", listaMeditacoes)
        configurarCard("respiracao", cardRespiracao, R.drawable.ic_breathing, "Respiração Guiada", listaRespiracao)
        configurarCard("podcasts", cardPodcasts, R.drawable.ic_podcast, "Podcast Sugerido", listaPodcasts)
        configurarCard("exercicios", cardExercicios, R.drawable.ic_brain, "Exercício Mental", listaExercicios)
    }

    // Em SugestaoUserActivity.kt, substitua apenas esta função

    private fun configurarCard(
        key: String, cardView: View, iconeResId: Int, titulo: String, listaDeSugestoes: List<Pair<String, String>>
    ) {
        if (cardView.visibility == View.GONE) return

        // --- LÓGICA DE ATUALIZAÇÃO DIÁRIA ---
        val prefs = getPrefs()
        val hoje = getHojeString()
        val ultimaDataVistaKey = "ultima_data_$key"
        val ultimoIndiceKey = "ultimo_indice_$key"

        val ultimaDataVista = prefs.getString(ultimaDataVistaKey, "")
        var indiceAtual = prefs.getInt(ultimoIndiceKey, 0)

        // Se a última vez que o usuário viu foi em um dia diferente, avança para a próxima sugestão
        if (ultimaDataVista != hoje) {
            indiceAtual++
            if (indiceAtual >= listaDeSugestoes.size) {
                indiceAtual = 0 // Volta para o início
            }
            // Salva o novo índice e a data de hoje
            prefs.edit()
                .putInt(ultimoIndiceKey, indiceAtual)
                .putString(ultimaDataVistaKey, hoje)
                .apply()
        }
        // ------------------------------------

        indices[key] = indiceAtual // Atualiza o índice em memória

        val iconeView = cardView.findViewById<ImageView>(R.id.icone_sugestao)
        val tituloView = cardView.findViewById<TextView>(R.id.titulo_card_sugestao)
        val textoView = cardView.findViewById<TextView>(R.id.texto_sugestao)
        val btnConcluir = cardView.findViewById<ImageButton>(R.id.btn_concluir_sugestao)
        val btnProxima = cardView.findViewById<ImageButton>(R.id.btn_proxima_sugestao)

        iconeView.setImageResource(iconeResId)
        tituloView.text = titulo

        fun atualizarSugestao() {
            val idx = indices.getOrDefault(key, 0)
            if (listaDeSugestoes.isNotEmpty() && idx < listaDeSugestoes.size) {
                val sugestao = listaDeSugestoes[idx]
                textoView.text = sugestao.first

                if (isSugestaoConcluida(sugestao.first)) {
                    cardView.alpha = 0.6f
                    btnConcluir.visibility = View.GONE
                    btnProxima.visibility = View.GONE
                    textoView.setOnClickListener {
                        Toast.makeText(this, "Você já concluiu esta sugestão hoje!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    cardView.alpha = 1.0f
                    btnConcluir.visibility = View.VISIBLE
                    btnProxima.visibility = View.VISIBLE
                    textoView.setOnClickListener {
                        AlertDialog.Builder(this)
                            .setTitle(sugestao.first)
                            .setMessage(sugestao.second)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
        }

        btnProxima.setOnClickListener {
            var novoIndice = (indices.getOrDefault(key, 0)) + 1
            if (novoIndice >= listaDeSugestoes.size) novoIndice = 0
            indices[key] = novoIndice

            // Salva o novo índice quando o usuário avança manualmente
            prefs.edit().putInt(ultimoIndiceKey, novoIndice).apply()

            atualizarSugestao()
        }

        btnConcluir.setOnClickListener {
            val sugestaoAtual = textoView.text.toString()
            salvarSugestaoConcluida(sugestaoAtual)
            Toast.makeText(this, "Ótimo! Sugestão concluída!", Toast.LENGTH_SHORT).show()
            atualizarSugestao()
        }

        atualizarSugestao()
    }

    // --- LÓGICA DO SISTEMA DE "CHECK" ---
    private fun getPrefs() = getSharedPreferences("sugestoes_prefs", Context.MODE_PRIVATE)

    private fun getHojeString(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    }

    private fun getSugestoesConcluidasHoje(): MutableSet<String> {
        return getPrefs().getStringSet(getHojeString(), mutableSetOf()) ?: mutableSetOf()
    }

    private fun salvarSugestaoConcluida(sugestao: String) {
        val concluidas = getSugestoesConcluidasHoje()
        concluidas.add(sugestao)
        getPrefs().edit().putStringSet(getHojeString(), concluidas).apply()
    }

    private fun isSugestaoConcluida(sugestao: String): Boolean {
        return getSugestoesConcluidasHoje().contains(sugestao)
    }

    private fun configurarNavBar() {
        val navBar = findViewById<LinearLayout>(R.id.navigation_bar)
        navBar.findViewById<LinearLayout>(R.id.botao_inicio).setOnClickListener {
            startActivity(Intent(this, Bemvindouser::class.java))
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
        navBar.findViewById<View>(R.id.botao_cronometro)?.setOnClickListener {
            startActivity(Intent(this, CronometroActivity::class.java))
        }
        navBar.findViewById<View>(R.id.botao_sugestoes)?.setOnClickListener {

        }
    }
}