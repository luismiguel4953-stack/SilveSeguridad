package com.silveseguridad.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.silve.seguridad.R

class LoginActivity : AppCompatActivity() {
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) runCatching {
            NotificationHelper.show(this, "Silve Seguridad activo", "Las alertas de seguridad están activadas.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                Toast.makeText(this, "Escribe un correo y una contraseña de al menos 6 caracteres.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            UserSession.signIn(this, name.text.toString().trim().ifBlank { "Usuario" }, email.text.toString().trim())
            runCatching { NotificationHelper.createChannel(this) }
            runCatching { NotificationHelper.show(this, "Bienvenido a Silve Seguridad", "Tu centro de seguridad está listo.") }
            openHome()
        }

        findViewById<TextView>(R.id.guestText).setOnClickListener {
            UserSession.signIn(this, "Usuario invitado", "")
            // Guest access must not depend on notification permission or notification services.
            openHome()
        }

        requestNotificationPermissionIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            runCatching { notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }
        }
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
