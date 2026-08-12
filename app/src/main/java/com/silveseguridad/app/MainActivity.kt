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
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.statusText)
        scanner = SecurityScanner(this)
        emergencyStore = EmergencyStore(this)
        findViewById<Button>(R.id.scanButton).setOnClickListener { runScan() }
        findViewById<Button>(R.id.linkButton).setOnClickListener { showLinkChecker() }
        findViewById<Button>(R.id.assistantButton).setOnClickListener { showAssistant() }
        findViewById<Button>(R.id.emergencyButton).setOnClickListener { showEmergency() }
    }

    private fun runScan() {
        val report = runCatching { scanner.scan() }.getOrElse {
            statusText.text = "No se pudo completar el análisis."
            return
        }
        statusText.text = "Análisis completado · Android ${report.sdk} · ${report.installedApps} aplicaciones detectadas\n\n${report.recommendations.joinToString("\n")}"
    }

    private fun showLinkChecker() {
        val input = EditText(this).apply { hint = "https://ejemplo.com"; setSingleLine(true) }
        AlertDialog.Builder(this).setTitle("Comprobar enlace").setView(input)
            .setPositiveButton("Analizar") { _, _ ->
                val result = LinkChecker.inspect(input.text.toString())
                AlertDialog.Builder(this).setTitle(if (result.safeToOpen) "Revisión" else "Precaución").setMessage(result.message).setPositiveButton("OK", null).show()
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun showAssistant() {
        AlertDialog.Builder(this).setTitle("Asistente de seguridad")
            .setMessage("Consejos rápidos:\n\n• Mantén Android y tus aplicaciones actualizados.\n• No compartas códigos de verificación ni contraseñas.\n• Comprueba el dominio antes de iniciar sesión.\n• Usa HTTPS y evita enlaces inesperados.\n• Activa un bloqueo de pantalla seguro.\n• Ante una emergencia, usa el botón de emergencia de Silve Seguridad.")
            .setPositiveButton("Entendido", null).show()
    }

    private fun showEmergency() {
        val contact = emergencyStore.contact
        if (contact.isBlank()) {
            val input = EditText(this).apply { hint = "Número de contacto"; setSingleLine(true) }
            AlertDialog.Builder(this).setTitle("Configurar emergencia").setMessage("Guarda un contacto para poder preparar un mensaje de emergencia.").setView(input)
                .setPositiveButton("Guardar") { _, _ -> emergencyStore.contact = input.text.toString(); Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show() }
                .setNegativeButton("Cancelar", null).show()
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${Uri.encode(contact)}")
            putExtra("sms_body", emergencyStore.message)
        }
        runCatching { startActivity(intent) }.onFailure {
            Toast.makeText(this, "No hay una aplicación de mensajes disponible.", Toast.LENGTH_LONG).show()
        }
    }
}
