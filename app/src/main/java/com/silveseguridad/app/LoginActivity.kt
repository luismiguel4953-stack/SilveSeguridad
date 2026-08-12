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
        if (granted) NotificationHelper.show(this, "Silve Seguridad activo", "Las alertas de seguridad están activadas.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannel(this)
        requestNotificationPermissionIfNeeded()
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
            NotificationHelper.show(this, "Bienvenido a Silve Seguridad", "Tu centro de seguridad está listo.")
            openHome()
        }
        findViewById<TextView>(R.id.guestText).setOnClickListener {
            UserSession.signIn(this, "Usuario invitado", "")
            NotificationHelper.show(this, "Modo invitado activado", "Puedes explorar Silve Seguridad y sus funciones.")
            openHome()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
