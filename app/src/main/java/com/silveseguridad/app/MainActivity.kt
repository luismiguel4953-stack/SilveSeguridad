package com.silveseguridad.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.silve.seguridad.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!UserSession.isLoggedIn(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        // Load the main UI before optional services. A notification failure must never crash startup.
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.welcomeText).text = "Hola, ${runCatching { UserSession.name(this) }.getOrDefault("Usuario")}"
        bind(R.id.scanButton) { open("security") }
        bind(R.id.appsButton) { open("apps") }
        bind(R.id.historyButton) { open("history") }
        bind(R.id.notificationButton) { open("notifications") }
        bind(R.id.linkButton) { open("web") }
        bind(R.id.passwordButton) { open("password") }
        bind(R.id.assistantButton) { open("assistant") }
        bind(R.id.emergencyButton) { open("emergency") }
        bind(R.id.profileButton) { showProfile() }
        bind(R.id.settingsButton) { startActivity(Intent(this, SettingsActivity::class.java)) }
        runCatching { NotificationHelper.createChannel(this) }
    }

    private fun bind(id: Int, action: () -> Unit) {
        findViewById<Button>(id)?.setOnClickListener { runCatching { action() } }
    }

    private fun open(module: String) {
        startActivity(Intent(this, ModuleActivity::class.java).putExtra("module", module))
    }

    private fun showProfile() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Mi cuenta")
            .setMessage("Nombre: ${runCatching { UserSession.name(this) }.getOrDefault("Invitado")}\nCorreo: ${runCatching { UserSession.email(this).ifBlank { "Invitado" } }.getOrDefault("Invitado")}")
            .setPositiveButton("Cerrar sesión") { _, _ ->
                runCatching { UserSession.signOut(this) }
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
}
