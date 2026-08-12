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

        // The login screen must be able to start even if optional notification
        // services or permissions are unavailable on the device.
        setContentView(R.layout.activity_login)

        if (UserSession.isLoggedIn(this)) {
            openHome()
            return
        }

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val name = findViewById<EditText>(R.id.nameInput)
            val email = findViewById<EditText>(R.id.emailInput)
            val password = findViewById<EditText>(R.id.passwordInput)

            if (email.text.toString().trim().isEmpty() || password.text.length < 6) {
                Toast.makeText(
                    this,
                    "Escribe un correo y una contraseña de al menos 6 caracteres.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            UserSession.signIn(
                this,
                name.text.toString().trim().ifBlank { "Usuario" },
                email.text.toString().trim()
            )

            // Notifications are optional and must never block login/startup.
            runCatching { NotificationHelper.createChannel(this) }
            runCatching {
                NotificationHelper.show(
                    this,
                    "Bienvenido a Silve Seguridad",
                    "Tu centro de seguridad está listo."
                )
            }
            openHome()
        }

        findViewById<TextView>(R.id.guestText).setOnClickListener {
            UserSession.signIn(this, "Usuario invitado", "")
            // Guest access is completely independent from notifications.
            openHome()
        }
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
