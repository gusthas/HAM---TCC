package com.apol.myapplication

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import java.util.concurrent.TimeUnit

class CronometroActivity : AppCompatActivity() {

    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var timerDuration: Long = 0L // Tempo padrão agora começa em zero
    private var isTimerRunning = false

    private lateinit var tvTimer: TextView
    private lateinit var btnStartPause: Button
    private lateinit var btnReset: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cronometro)

        tvTimer = findViewById(R.id.tv_timer)
        btnStartPause = findViewById(R.id.btn_start_pause)
        btnReset = findViewById(R.id.btn_reset)
        progressBar = findViewById(R.id.progress_bar)

        tvTimer.setOnClickListener {
            if (!isTimerRunning) {
                mostrarDialogoDefinirTempo()
            }
        }

        btnStartPause.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        btnReset.setOnClickListener {
            resetTimer()
        }

        resetTimer()
        configurarNavBar()
    }

    override fun onBackPressed() {
        // Em vez de voltar para a tela anterior (comportamento padrão),
        // esta função encerra o aplicativo por completo.
        finishAffinity()
    }

    private fun mostrarDialogoDefinirTempo() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_time, null)
        // Usa o estilo customizado para o fundo transparente
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent)
            .setView(dialogView)
            .create()

        // Encontra os componentes no layout customizado
        val pickerHours = dialogView.findViewById<NumberPicker>(R.id.picker_hours)
        val pickerMinutes = dialogView.findViewById<NumberPicker>(R.id.picker_minutes)
        val pickerSeconds = dialogView.findViewById<NumberPicker>(R.id.picker_seconds)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok_tempo)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btn_cancelar_tempo)

        pickerHours.minValue = 0; pickerHours.maxValue = 23
        pickerMinutes.minValue = 0; pickerMinutes.maxValue = 59
        pickerSeconds.minValue = 0; pickerSeconds.maxValue = 59

        pickerMinutes.setFormatter { i -> String.format("%02d", i) }
        pickerSeconds.setFormatter { i -> String.format("%02d", i) }

        val currentHours = TimeUnit.MILLISECONDS.toHours(timerDuration)
        val currentMinutes = TimeUnit.MILLISECONDS.toMinutes(timerDuration) - TimeUnit.HOURS.toMinutes(currentHours)
        val currentSeconds = TimeUnit.MILLISECONDS.toSeconds(timerDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timerDuration))
        pickerHours.value = currentHours.toInt()
        pickerMinutes.value = currentMinutes.toInt()
        pickerSeconds.value = currentSeconds.toInt()

        // Configura os cliques dos botões customizados
        btnOk.setOnClickListener {
            val hours = pickerHours.value
            val minutes = pickerMinutes.value
            val seconds = pickerSeconds.value
            timerDuration = (hours * 3600000L) + (minutes * 60000L) + (seconds * 1000L)
            resetTimer()
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startTimer() {
        if (timeLeftInMillis <= 0L) {
            Toast.makeText(this, "Defina um tempo para iniciar.", Toast.LENGTH_SHORT).show()
            return
        }

        timer = object : CountDownTimer(timeLeftInMillis, 50) { // Tick rápido para animação fluida
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerDisplay()
                updateProgressBar(false) // Animação suave
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                isTimerRunning = false
                updateTimerDisplay()
                updateProgressBar(true) // Atualização final e instantânea
                updateUI()
                Toast.makeText(this@CronometroActivity, "Tempo finalizado!", Toast.LENGTH_SHORT).show()
            }
        }.start()

        isTimerRunning = true
        updateUI()
    }

    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        updateUI()
    }

    private fun resetTimer() {
        timer?.cancel()
        isTimerRunning = false
        timeLeftInMillis = timerDuration
        updateTimerDisplay()
        updateProgressBar(true) // Atualização instantânea
        updateUI()
    }

    private fun updateTimerDisplay() {
        val hours = TimeUnit.MILLISECONDS.toHours(timeLeftInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis) - TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis))

        if (timerDuration >= 3600000L) { // Se o tempo total for 1h ou mais, mostra as horas
            tvTimer.text = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            tvTimer.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    private fun updateProgressBar(instant: Boolean) {
        if (timerDuration > 0) {
            val progress = (timeLeftInMillis.toDouble() / timerDuration.toDouble() * 100).toInt()

            if (instant) {
                progressBar.progress = progress
            } else {
                // Anima o progresso suavemente para a nova posição
                val animator = ObjectAnimator.ofInt(progressBar, "progress", progress)
                animator.duration = 150 // Duração curta para parecer contínuo, mas suave
                animator.interpolator = LinearInterpolator()
                animator.start()
            }

            progressBar.visibility = if (timeLeftInMillis <= 0) View.INVISIBLE else View.VISIBLE
        } else {
            progressBar.progress = 0
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun updateUI() {
        btnStartPause.text = if (isTimerRunning) "Pausar" else "Iniciar"
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
        navBar.findViewById<LinearLayout>(R.id.botao_cronometro).setOnClickListener {
            // Já está aqui
        }
        navBar.findViewById<LinearLayout>(R.id.botao_sugestoes).setOnClickListener {
            startActivity(Intent(this, SugestaoUser::class.java))        }
    }
}