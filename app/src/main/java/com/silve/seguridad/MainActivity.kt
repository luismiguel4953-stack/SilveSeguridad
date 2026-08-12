package com.silve.seguridad

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.silveseguridad.app.LoginActivity
import com.silveseguridad.app.ModuleActivity
import com.silveseguridad.app.UserSession

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val status = findViewById<TextView>(R.id.statusText)
        val welcome = findViewById<TextView>(R.id.welcomeText)

        welcome.text = "Hola, ${UserSession.name(this).ifBlank { "Usuario" }}"
        status.text = "Tu centro de seguridad está listo."

        bindModule(R.id.scanButton, "security")
        bindModule(R.id.appsButton, "apps")
        bindModule(R.id.historyButton, "history")
        bindModule(R.id.notificationButton, "notifications")
        bindModule(R.id.linkButton, "web")
        bindModule(R.id.passwordButton, "password")
        bindModule(R.id.assistantButton, "assistant")
        bindModule(R.id.emergencyButton, "emergency")
        bindModule(R.id.settingsButton, "settings")

        findViewById<ImageButton>(R.id.profileButton).setOnClickListener {
            if (UserSession.isLoggedIn(this)) {
                openModule("account")
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }

    private fun bindModule(id: Int, module: String) {
        findViewById<Button>(id).setOnClickListener { openModule(module) }
    }

    private fun openModule(module: String) {
        startActivity(Intent(this, ModuleActivity::class.java).putExtra("module", module))
    }
}
