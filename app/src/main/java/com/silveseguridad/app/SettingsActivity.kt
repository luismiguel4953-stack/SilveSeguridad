package com.silveseguridad.app

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.silve.seguridad.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        findViewById<Button>(R.id.privacyButton).setOnClickListener { showInfo("Privacidad", "Los resultados y el historial de esta versión se mantienen localmente en el dispositivo.") }
        findViewById<Button>(R.id.securityButton).setOnClickListener { showInfo("Seguridad", "Aquí se centralizarán PIN, biometría y protección de configuración en la siguiente iteración.") }
        findViewById<Button>(R.id.notificationsSettingsButton).setOnClickListener { showInfo("Notificaciones", "Puedes gestionar las notificaciones de Silve Seguridad desde la configuración de Android.") }
        findViewById<Button>(R.id.analysisSettingsButton).setOnClickListener { showInfo("Análisis", "Las comprobaciones usan APIs públicas de Android y no inspeccionan datos privados de otras aplicaciones.") }
        findViewById<Button>(R.id.appearanceButton).setOnClickListener { showInfo("Apariencia", "La personalización visual avanzada se incorporará sin alterar las funciones de seguridad.") }
        findViewById<Button>(R.id.accessibilityButton).setOnClickListener { showInfo("Accesibilidad", "Silve Seguridad está preparado para incorporar tamaños de texto y navegación accesible.") }
        findViewById<Button>(R.id.aboutButton).setOnClickListener { showInfo("Silve Seguridad", "Centro personal de prevención digital • Versión 2.1") }
    }
    private fun showInfo(title: String, message: String) = androidx.appcompat.app.AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", null).show()
}
