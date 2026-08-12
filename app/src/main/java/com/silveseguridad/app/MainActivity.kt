package com.silveseguridad.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.silve.seguridad.R

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var scanner: SecurityScanner
    private lateinit var emergencyStore: EmergencyStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!UserSession.isLoggedIn(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.statusText)
        scanner = SecurityScanner(this)
        emergencyStore = EmergencyStore(this)
        findViewById<TextView>(R.id.welcomeText).text = "Hola, ${UserSession.name(this)}"
        findViewById<Button>(R.id.scanButton).setOnClickListener { runScan() }
        findViewById<Button>(R.id.linkButton).setOnClickListener { showLinkChecker() }
        findViewById<Button>(R.id.passwordButton).setOnClickListener { showPasswordChecker() }
        findViewById<Button>(R.id.assistantButton).setOnClickListener { showAssistant() }
        findViewById<Button>(R.id.emergencyButton).setOnClickListener { showEmergency() }
        findViewById<Button>(R.id.profileButton).setOnClickListener { showProfile() }
        findViewById<Button>(R.id.settingsButton).setOnClickListener { showSettings() }
    }

    private fun runScan() {
        val report = runCatching { scanner.scan() }.getOrElse {
            statusText.text = "No se pudo completar el análisis."
            return
        }
        statusText.text = "ESTADO DE SEGURIDAD\n\nAndroid ${report.sdk} · ${report.installedApps} aplicaciones detectadas\n\n${report.recommendations.joinToString("\n") { "• $it" }}"
    }

    private fun showLinkChecker() {
        val input = EditText(this).apply { hint = "https://ejemplo.com"; setSingleLine(true) }
        AlertDialog.Builder(this).setTitle("Analizador de enlaces").setMessage("Revisa señales básicas de riesgo antes de abrir un enlace.").setView(input)
            .setPositiveButton("Analizar") { _, _ ->
                val result = LinkChecker.inspect(input.text.toString())
                AlertDialog.Builder(this).setTitle(if (result.safeToOpen) "Revisión favorable" else "Precaución")
                    .setMessage(result.message).setPositiveButton("OK", null).show()
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun showPasswordChecker() {
        val input = EditText(this).apply { hint = "Escribe una contraseña para evaluarla"; setSingleLine(true); inputType = 0x81 }
        AlertDialog.Builder(this).setTitle("Comprobador de contraseña").setMessage("La contraseña no se envía a Internet.").setView(input)
            .setPositiveButton("Evaluar") { _, _ ->
                val p = input.text.toString()
                val score = listOf(p.length >= 12, p.any { it.isUpperCase() }, p.any { it.isLowerCase() }, p.any { it.isDigit() }, p.any { !it.isLetterOrDigit() }).count { it }
                val result = when (score) { 5 -> "Muy fuerte"; 4 -> "Fuerte"; 3 -> "Media"; else -> "Débil" }
                AlertDialog.Builder(this).setTitle("Resultado: $result").setMessage("Usa frases largas y únicas. No reutilices contraseñas entre servicios.").setPositiveButton("OK", null).show()
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun showAssistant() {
        AlertDialog.Builder(this).setTitle("Asistente de seguridad")
            .setMessage("Puedo ayudarte a revisar hábitos de seguridad:\n\n• Actualiza Android y tus aplicaciones.\n• No compartas códigos de verificación.\n• Comprueba el dominio antes de iniciar sesión.\n• Usa contraseñas únicas y fuertes.\n• Activa un bloqueo de pantalla seguro.\n• Revisa los permisos de las aplicaciones.\n• Si recibes un mensaje sospechoso, no abras sus enlaces.\n\nSilve Seguridad funciona como una herramienta de prevención; no sustituye a servicios de emergencia ni a un antivirus completo.")
            .setPositiveButton("Entendido", null).show()
    }

    private fun showEmergency() {
        val contact = emergencyStore.contact
        if (contact.isBlank()) {
            val input = EditText(this).apply { hint = "Número de contacto"; setSingleLine(true) }
            AlertDialog.Builder(this).setTitle("Configurar contacto de emergencia").setMessage("Guarda un contacto para preparar un SMS de emergencia.").setView(input)
                .setPositiveButton("Guardar") { _, _ -> emergencyStore.contact = input.text.toString(); Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show() }
                .setNegativeButton("Cancelar", null).show()
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("smsto:${Uri.encode(contact)}"); putExtra("sms_body", emergencyStore.message) }
        runCatching { startActivity(intent) }.onFailure { Toast.makeText(this, "No hay una aplicación de mensajes disponible.", Toast.LENGTH_LONG).show() }
    }

    private fun showProfile() {
        AlertDialog.Builder(this).setTitle("Mi cuenta")
            .setMessage("Nombre: ${UserSession.name(this)}\nCorreo: ${UserSession.email(this).ifBlank { "Invitado" }}\n\nTu sesión actual está guardada en este dispositivo.")
            .setPositiveButton("Cerrar sesión") { _, _ -> UserSession.signOut(this); startActivity(Intent(this, LoginActivity::class.java)); finish() }
            .setNegativeButton("Cerrar", null).show()
    }

    private fun showSettings() {
        AlertDialog.Builder(this).setTitle("Configuración y privacidad")
            .setItems(arrayOf("Privacidad", "Consejos de seguridad", "Acerca de Silve Seguridad")) { _, which ->
                when (which) {
                    0 -> AlertDialog.Builder(this).setTitle("Privacidad").setMessage("Silve Seguridad evita enviar datos de análisis del dispositivo en esta versión. Los datos de sesión y emergencia se almacenan localmente.").setPositiveButton("OK", null).show()
                    1 -> showAssistant()
                    2 -> AlertDialog.Builder(this).setTitle("Silve Seguridad 2.0").setMessage("Centro personal de prevención digital, análisis y asistencia de seguridad.").setPositiveButton("OK", null).show()
                }
            }.setNegativeButton("Cerrar", null).show()
    }
}
