package com.silveseguridad.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.silve.seguridad.R

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var scanner: SecurityScanner
    private lateinit var emergencyStore: EmergencyStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!UserSession.isLoggedIn(this)) { startActivity(Intent(this, LoginActivity::class.java)); finish(); return }
        NotificationHelper.createChannel(this)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.statusText)
        scanner = SecurityScanner(this)
        emergencyStore = EmergencyStore(this)
        findViewById<TextView>(R.id.welcomeText).text = "Hola, ${UserSession.name(this)}"
        findViewById<Button>(R.id.notificationButton).setOnClickListener { showNotificationCenter() }
        findViewById<Button>(R.id.scanButton).setOnClickListener { runScan() }
        findViewById<Button>(R.id.linkButton).setOnClickListener { showLinkChecker() }
        findViewById<Button>(R.id.passwordButton).setOnClickListener { showPasswordChecker() }
        findViewById<Button>(R.id.assistantButton).setOnClickListener { showAssistant() }
        findViewById<Button>(R.id.emergencyButton).setOnClickListener { showEmergency() }
        findViewById<Button>(R.id.profileButton).setOnClickListener { showProfile() }
        findViewById<Button>(R.id.settingsButton).setOnClickListener { showSettings() }
    }

    private fun showNotificationCenter() {
        val items = NotificationStore.all(this)
        if (items.isEmpty()) {
            AlertDialog.Builder(this).setTitle("Centro de notificaciones").setMessage("No tienes alertas guardadas todavía.").setPositiveButton("OK", null).show()
            return
        }
        val text = items.joinToString("\n\n") { "• ${it.title}\n${it.message}" }
        AlertDialog.Builder(this).setTitle("Centro de notificaciones (${items.size})").setMessage(text)
            .setPositiveButton("Limpiar historial") { _, _ -> NotificationStore.clear(this); Toast.makeText(this, "Historial limpiado", Toast.LENGTH_SHORT).show() }
            .setNegativeButton("Cerrar", null).show()
    }

    private fun runScan() {
        val report = runCatching { scanner.scan() }.getOrElse { statusText.text = "No se pudo completar el análisis."; NotificationHelper.show(this, "Silve Seguridad", "No se pudo completar el análisis del dispositivo."); return }
        statusText.text = "ESTADO DE SEGURIDAD\n\nAndroid ${report.sdk} · ${report.installedApps} aplicaciones detectadas\n\n${report.recommendations.joinToString("\n") { "• $it" }}"
        if (report.recommendations.any { it.contains("actualizado", true) || it.contains("prueba", true) || report.debuggableBuild }) NotificationHelper.show(this, "Revisión de seguridad", "El análisis encontró recomendaciones que conviene revisar.") else NotificationHelper.show(this, "Análisis completado", "No se detectaron problemas básicos en las comprobaciones disponibles.")
    }

    private fun showLinkChecker() {
        val input = EditText(this).apply { hint = "https://ejemplo.com"; setSingleLine(true) }
        AlertDialog.Builder(this).setTitle("Analizador de enlaces").setMessage("Revisa señales básicas de riesgo antes de abrir un enlace.").setView(input)
            .setPositiveButton("Analizar") { _, _ -> val result = LinkChecker.inspect(input.text.toString()); AlertDialog.Builder(this).setTitle(if (result.safeToOpen) "Revisión favorable" else "Precaución").setMessage(result.message).setPositiveButton("OK", null).show(); if (!result.safeToOpen) NotificationHelper.show(this, "Enlace sospechoso", "Silve Seguridad recomienda revisar el enlace antes de abrirlo.") }.setNegativeButton("Cancelar", null).show()
    }

    private fun showPasswordChecker() {
        val input = EditText(this).apply { hint = "Escribe una contraseña para evaluarla"; setSingleLine(true); inputType = 0x81 }
        AlertDialog.Builder(this).setTitle("Comprobador de contraseña").setMessage("La contraseña no se envía a Internet.").setView(input)
            .setPositiveButton("Evaluar") { _, _ -> val p = input.text.toString(); val score = listOf(p.length >= 12, p.any { it.isUpperCase() }, p.any { it.isLowerCase() }, p.any { it.isDigit() }, p.any { !it.isLetterOrDigit() }).count { it }; val result = when (score) { 5 -> "Muy fuerte"; 4 -> "Fuerte"; 3 -> "Media"; else -> "Débil" }; AlertDialog.Builder(this).setTitle("Resultado: $result").setMessage("Usa frases largas y únicas. No reutilices contraseñas entre servicios.").setPositiveButton("OK", null).show(); if (score <= 2) NotificationHelper.show(this, "Contraseña débil", "Considera utilizar una contraseña más larga, única y compleja.") }.setNegativeButton("Cancelar", null).show()
    }

    private fun showAssistant() { AlertDialog.Builder(this).setTitle("Asistente de seguridad").setMessage("Consejos: actualiza Android, no compartas códigos, comprueba dominios, usa contraseñas únicas, revisa permisos y evita enlaces inesperados. Silve Seguridad es una herramienta preventiva y no sustituye servicios de emergencia ni un antivirus completo.").setPositiveButton("Entendido", null).show() }

    private fun showEmergency() {
        val contact = emergencyStore.contact
        if (contact.isBlank()) { val input = EditText(this).apply { hint = "Número de contacto"; setSingleLine(true) }; AlertDialog.Builder(this).setTitle("Configurar contacto de emergencia").setMessage("Guarda un contacto para preparar un SMS de emergencia.").setView(input).setPositiveButton("Guardar") { _, _ -> emergencyStore.contact = input.text.toString(); Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show(); NotificationHelper.show(this, "Emergencia configurada", "Tu contacto de emergencia está guardado en este dispositivo.") }.setNegativeButton("Cancelar", null).show(); return }
        val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("smsto:${Uri.encode(contact)}"); putExtra("sms_body", emergencyStore.message) }; runCatching { startActivity(intent) }.onFailure { Toast.makeText(this, "No hay una aplicación de mensajes disponible.", Toast.LENGTH_LONG).show() }
    }

    private fun showProfile() { AlertDialog.Builder(this).setTitle("Mi cuenta").setMessage("Nombre: ${UserSession.name(this)}\nCorreo: ${UserSession.email(this).ifBlank { "Invitado" }}\n\nTu sesión actual está guardada en este dispositivo.").setPositiveButton("Cerrar sesión") { _, _ -> UserSession.signOut(this); startActivity(Intent(this, LoginActivity::class.java)); finish() }.setNegativeButton("Cerrar", null).show() }

    private fun showSettings() { AlertDialog.Builder(this).setTitle("Configuración y privacidad").setItems(arrayOf("Privacidad", "Consejos de seguridad", "Acerca de Silve Seguridad")) { _, which -> when (which) { 0 -> AlertDialog.Builder(this).setTitle("Privacidad").setMessage("Los datos de análisis se mantienen localmente en esta versión.").setPositiveButton("OK", null).show(); 1 -> showAssistant(); 2 -> AlertDialog.Builder(this).setTitle("Silve Seguridad 2.1").setMessage("Centro personal de prevención digital, análisis, alertas y asistencia.").setPositiveButton("OK", null).show() } }.setNegativeButton("Cerrar", null).show() }
}
