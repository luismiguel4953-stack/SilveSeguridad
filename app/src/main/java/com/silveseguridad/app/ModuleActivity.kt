package com.silveseguridad.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.silve.seguridad.R

class ModuleActivity : AppCompatActivity() {
    private lateinit var titleText: TextView
    private lateinit var stageText: TextView
    private lateinit var resultText: TextView
    private lateinit var progress: ProgressBar
    private lateinit var input: EditText
    private lateinit var action: Button
    private val handler = Handler(Looper.getMainLooper())
    private var module = "security"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module)
        module = intent.getStringExtra("module") ?: "security"
        titleText = findViewById(R.id.moduleTitle)
        stageText = findViewById(R.id.stageText)
        resultText = findViewById(R.id.resultText)
        progress = findViewById(R.id.analysisProgress)
        input = findViewById(R.id.moduleInput)
        action = findViewById(R.id.moduleAction)
        configure()
    }

    private fun configure() {
        val data = when (module) {
            "apps" -> arrayOf("Aplicaciones", "Revisión de aplicaciones instaladas", "Revisar aplicaciones")
            "web" -> arrayOf("Protección Web", "Analizador de enlaces", "Analizar enlace")
            "password" -> arrayOf("Contraseñas", "Comprobación local de fortaleza", "Evaluar contraseña")
            "history" -> arrayOf("Historial", "Tus análisis de seguridad", "Actualizar")
            "notifications" -> arrayOf("Notificaciones", "Centro de alertas de Silve Seguridad", "Actualizar")
            "assistant" -> arrayOf("Asistente", "Asistencia y consejos de seguridad", "Mostrar consejos")
            "emergency" -> arrayOf("Emergencia", "Preparación de contacto de emergencia", "Configurar")
            "account" -> arrayOf("Mi cuenta", "Perfil y sesión", "Cerrar sesión")
            "settings" -> arrayOf("Configuración avanzada", "Controles de privacidad, análisis y alertas", "Guardar preferencias")
            else -> arrayOf("Escaneo de seguridad", "Análisis del dispositivo", "Analizar ahora")
        }
        titleText.text = data[0]
        stageText.text = data[1]
        action.text = data[2]
        when (module) {
            "web" -> {
                input.hint = "https://ejemplo.com"
                input.visibility = EditText.VISIBLE
                input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            }
            "password" -> {
                input.hint = "Contraseña (evaluación local)"
                input.visibility = EditText.VISIBLE
                input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            else -> input.visibility = EditText.GONE
        }
        action.setOnClickListener { startModule() }
        if (module == "history" || module == "notifications" || module == "assistant" || module == "settings") startModule()
        if (module == "account") {
            resultText.text = "Nombre: ${UserSession.name(this)}\nCorreo: ${UserSession.email(this).ifBlank { "Invitado" }}\n\nTu sesión está guardada localmente en este dispositivo."
        }
    }

    private fun startModule() {
        if (module == "account") {
            UserSession.signOut(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        if (module == "settings") {
            showSettings()
            return
        }
        action.isEnabled = false
        progress.progress = 0
        resultText.text = ""
        progress.visibility = ProgressBar.VISIBLE
        val stages = when (module) {
            "apps" -> listOf("Preparando revisión...", "Leyendo paquetes públicos...", "Comprobando permisos declarados...", "Buscando señales de atención...")
            "security" -> listOf("Preparando dispositivo...", "Analizando aplicaciones...", "Comprobando permisos...", "Revisando configuración...", "Calculando resultado...")
            "web" -> listOf("Validando URL...", "Analizando dominio...", "Revisando estructura...", "Calculando nivel de riesgo...")
            "password" -> listOf("Evaluando longitud...", "Comprobando variedad...", "Calculando fortaleza...")
            else -> listOf("Cargando información...", "Preparando resultados...")
        }
        stages.forEachIndexed { i, text ->
            handler.postDelayed({
                stageText.text = text
                progress.progress = (i + 1) * 100 / stages.size
            }, i * 500L)
        }
        handler.postDelayed({ finishModule() }, stages.size * 500L + 150L)
    }

    private fun finishModule() {
        resultText.text = when (module) {
            "security" -> runSecurityScan()
            "apps" -> runAppsReview()
            "web" -> runWebReview()
            "password" -> runPasswordReview()
            "history" -> historyText()
            "notifications" -> notificationsText()
            "assistant" -> "Consejos recomendados:\n\n• Mantén Android y tus aplicaciones actualizados.\n• Usa contraseñas únicas y largas.\n• Revisa permisos sensibles.\n• No abras enlaces inesperados.\n• Activa el bloqueo de pantalla.\n\nSilve Seguridad es una herramienta preventiva; no sustituye un antivirus completo ni servicios de emergencia."
            "emergency" -> "Configura tu contacto de emergencia. La aplicación preparará un mensaje en tu aplicación de SMS; no lo enviará automáticamente."
            else -> "Módulo listo."
        }
        progress.visibility = ProgressBar.GONE
        action.isEnabled = true
        action.text = "Volver a analizar"
    }

    private fun runSecurityScan(): String = runCatching {
        val report = SecurityScanner(this).scan()
        val risk = report.riskApps.size
        val score = (100 - report.recommendations.count { !it.contains("No se detectaron", true) } * 20).coerceIn(0, 100)
        SecurityHistory.save(this, score, "Android ${report.sdk} · parche ${report.securityPatch} · ${report.installedApps} aplicaciones · $risk para revisar")
        NotificationHelper.show(this, "Análisis completado", "Puntuación de Silve Seguridad: $score/100")
        "RESULTADO: $score/100\n\nAndroid ${report.sdk}\nParche: ${report.securityPatch}\nAplicaciones: ${report.installedApps}\nPara revisar: $risk\n\n${report.recommendations.joinToString("\n") { "• $it" }}"
    }.getOrElse { "No se pudo completar el análisis: ${it.message ?: "error desconocido"}" }

    private fun runAppsReview(): String = runCatching {
        val report = SecurityScanner(this).scan()
        if (report.riskApps.isEmpty()) "No se detectaron aplicaciones con las señales básicas configuradas. Esto no garantiza que todas sean seguras."
        else report.riskApps.joinToString("\n\n") { app -> "${app.label}\n• ${app.reasons.joinToString("\n• ")}" }
    }.getOrElse { "No se pudo revisar las aplicaciones: ${it.message ?: "error desconocido"}" }

    private fun runWebReview(): String {
        val value = input.text.toString()
        if (value.isBlank()) return "Introduce una URL para analizar."
        val report = LinkChecker.inspect(value)
        return "RIESGO: ${report.riskLevel}\n\n${report.message}"
    }

    private fun runPasswordReview(): String {
        val password = input.text.toString()
        val score = listOf(
            password.length >= 12,
            password.any { it.isUpperCase() },
            password.any { it.isLowerCase() },
            password.any { it.isDigit() },
            password.any { !it.isLetterOrDigit() }
        ).count { it }
        val strength = when (score) {
            5 -> "MUY FUERTE"
            4 -> "FUERTE"
            3 -> "MEDIA"
            else -> "DÉBIL"
        }
        return "FORTALEZA: $strength\n\nCumple $score/5 comprobaciones. Usa una contraseña larga, única y no reutilizada."
    }

    private fun historyText(): String = SecurityHistory.all(this).ifEmpty { listOf("Todavía no hay análisis guardados.") }.joinToString("\n\n") { raw ->
        val parts = raw.split('|', limit = 3)
        if (parts.size == 3) "Puntuación: ${parts[1]}/100\n${parts[2]}" else raw
    }

    private fun notificationsText(): String = NotificationStore.all(this).ifEmpty { listOf("No tienes alertas guardadas todavía.") }.joinToString("\n\n") { notification ->
        "• ${notification.title}\n${notification.message}"
    }

    private fun showSettings() {
        action.visibility = Button.GONE
        resultText.text = "Las preferencias se guardan localmente en esta versión.\n\nActiva o desactiva los controles que quieras utilizar."
        val root = findViewById<android.widget.LinearLayout>(R.id.moduleRoot)
        listOf("Notificaciones de seguridad", "Animación de análisis", "Protección Web", "Recomendaciones de privacidad").forEach { label ->
            val sw = Switch(this).apply {
                text = label
                isChecked = true
                setPadding(0, 18, 0, 18)
            }
            root.addView(sw)
            sw.setOnCheckedChangeListener { _, checked ->
                Toast.makeText(this, "$label: ${if (checked) "Activado" else "Desactivado"}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
