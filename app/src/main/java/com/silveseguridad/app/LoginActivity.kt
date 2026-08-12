package com.silveseguridad.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.silve.seguridad.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (UserSession.isLoggedIn(this)) {
            openHome()
            return
        }
        setContentView(R.layout.activity_login)
        val name = findViewById<EditText>(R.id.nameInput)
        val email = findViewById<EditText>(R.id.emailInput)
        val password = findViewById<EditText>(R.id.passwordInput)
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            if (email.text.toString().trim().isEmpty() || password.text.length < 6) {
                Toast.makeText(this, "Escribe un correo y una contraseña de al menos 6 caracteres.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            UserSession.signIn(this, name.text.toString().trim().ifBlank { "Usuario" }, email.text.toString().trim())
            openHome()
        }
        findViewById<TextView>(R.id.guestText).setOnClickListener {
            UserSession.signIn(this, "Usuario invitado", "")
            openHome()
        }
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
