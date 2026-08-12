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
        if (!UserSession.isLoggedIn(this)) { startActivity(Intent(this, LoginActivity::class.java)); finish(); return }
        NotificationHelper.createChannel(this)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.welcomeText).text = "Hola, ${UserSession.name(this)}"
        findViewById<Button>(R.id.scanButton).setOnClickListener { open("security") }
        findViewById<Button>(R.id.appsButton).setOnClickListener { open("apps") }
        findViewById<Button>(R.id.historyButton).setOnClickListener { open("history") }
        findViewById<Button>(R.id.notificationButton).setOnClickListener { open("notifications") }
        findViewById<Button>(R.id.linkButton).setOnClickListener { open("web") }
        findViewById<Button>(R.id.passwordButton).setOnClickListener { open("password") }
        findViewById<Button>(R.id.assistantButton).setOnClickListener { open("assistant") }
        findViewById<Button>(R.id.emergencyButton).setOnClickListener { open("emergency") }
        findViewById<Button>(R.id.profileButton).setOnClickListener { showProfile() }
        findViewById<Button>(R.id.settingsButton).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }
    private fun open(module: String) { startActivity(Intent(this, ModuleActivity::class.java).putExtra("module", module)) }
    private fun showProfile() { androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Mi cuenta").setMessage("Nombre: ${UserSession.name(this)}\nCorreo: ${UserSession.email(this).ifBlank { "Invitado" }}").setPositiveButton("Cerrar sesión") { _, _ -> UserSession.signOut(this); startActivity(Intent(this, LoginActivity::class.java)); finish() }.setNegativeButton("Cerrar", null).show() }
}
