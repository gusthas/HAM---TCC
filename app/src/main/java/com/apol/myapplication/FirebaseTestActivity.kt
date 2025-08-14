package com.apol.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth

import com.google.firebase.appcheck.ktx.appCheck 
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.auth

class FirebaseTestActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var statusTextView: TextView

    private val testEmail = "test@hamtcc.com"
    private val testPassword = "password123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_test)

        // Inicializa o App Check (importante!)
        com.google.firebase.Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

        auth = com.google.firebase.Firebase.auth
        statusTextView = findViewById(R.id.status_textview)

        findViewById<Button>(R.id.btn_register_test).setOnClickListener {
            registerTestUser()
        }
        findViewById<Button>(R.id.btn_login_test).setOnClickListener {
            loginTestUser()
        }
        findViewById<Button>(R.id.btn_logout_test).setOnClickListener {
            logoutUser()
        }
    }

    override fun onStart() {
        super.onStart()
        updateStatus()
    }

    private fun registerTestUser() {
        statusTextView.text = "A registar..."
        auth.createUserWithEmailAndPassword(testEmail, testPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registo bem-sucedido!", Toast.LENGTH_SHORT).show()
                    updateStatus()
                } else {
                    statusTextView.text = "Falha no Registo: ${task.exception?.message}"
                }
            }
    }

    private fun loginTestUser() {
        statusTextView.text = "A fazer login..."
        auth.signInWithEmailAndPassword(testEmail, testPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                    updateStatus()
                } else {
                    statusTextView.text = "Falha no Login: ${task.exception?.message}"
                }
            }
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(this, "Logout realizado.", Toast.LENGTH_SHORT).show()
        updateStatus()
    }

    private fun updateStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            statusTextView.text = "Estado: Ligado\nUID: ${currentUser.uid}"
        } else {
            statusTextView.text = "Estado: Desligado"
        }
    }
}