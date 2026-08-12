package com.silveseguridad.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
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
        titleText = findViewById(R.id.moduleTitle); stageText = findViewById(R.id.stageText)
        resultText = findViewById(R.id.resultText); progress = findViewById(R.id.analysisProgress)
        input = findViewById(R.id.moduleInput); action = findViewById(R.id.moduleAction)
        configure()
    }

    private fun configure() {
        val data = when (module) {
            "apps" -> arrayOf("Aplicaciones", "Revisión de aplicaciones instalada", "Revisar aplicaciones")
            "web" -> arrayOf("Protección Web", "Analizador de enlaces", "Analizar enlace")
            "password" -> arrayOf("Contraseñas", "Comprobación local de fortaleza", "Evaluar contraseña")
            "history" -> arrayOf("Historial", "Tus análisis de seguridad", "Actualizar")
            "notifications" -> arrayOf("Notificaciones", "Centro de alertas de Silve Seguridad", "Actualizar")
            "assistant" -> arrayOf("Asistente", "Asistencia y consejos de seguridad", "Mostrar consejos")
            "emergency" -> arrayOf("Emergencia", "Preparación de contacto de emergencia", "Configurar")
            else -> arrayOf("Escaneo de seguridad", "Análisis del dispositivo", "Analizar ahora")
        }
        titleText.text = data[0]; stageText.text = data[1]; action.text = data[2]
        when (module) {
            "web" -> { input.hint = "https://ejemplo.com"; input.visibility = EditText.VISIBLE; input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI }
            "password" -> { input.hint = "Contraseña (evaluación local)"; input.visibility = EditText.VISIBLE; input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD }
            else -> input.visibility = EditText.GONE
        }
        action.setOnClickListener { startModule() }
        if (module == "history" || module == "notifications" || module == "assistant") startModule()
    }

    private fun startModule() {
        action.isEnabled = false; progress.progress = 0; resultText.text = ""; progress.visibility = ProgressBar.VISIBLE
        val stages = when (module) {
            "apps" -> listOf("Preparando revisión...", "Leyendo paquetes públicos...", "Comprobando permisos declarados...", "Buscando señales de atención...")
            "security" -> listOf("Preparando dispositivo...", "Analizando aplicaciones...", "Comprobando permisos...", "Revisando configuración...", "Calculando resultado...")
            "web" -> listOf("Validando URL...", "Analizando dominio...", "Revisando estructura...", "Calculando nivel de riesgo...")
            "password" -> listOf("Evaluando longitud...", "Comprobando variedad...", "Calculando fortaleza...")
            else -> listOf("Cargando información...", "Preparando resultados...")
        }
        stages.forEachIndexed { i, text -> handler.postDelayed({ stageText.text = text; progress.progress = ((i + 1) * 100 / stages.size) }, (i * 500).toLong()) }
        handler.postDelayed({ finishModule() }, (stages.size * 500L) + 150)
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
            "emergency" -> "Configura tu contacto de emergencia desde aquí. La aplicación preparará un mensaje en tu aplicación de SMS y pedirá confirmación antes de enviarlo."
            else -> "Módulo listo."
        }
        progress.visibility = ProgressBar.GONE; action.isEnabled = true; action.text = "Volver a analizar"
    }

    private fun runSecurityScan(): String = runCatching {
        val r = SecurityScanner(this).scan(); val risk = r.riskApps.size; val score = (100 - r.recommendations.count { !it.contains("No se detectaron", true) } * 20).coerceIn(0,100)
        SecurityHistory.save(this, score, "Android ${r.sdk} · parche ${r.securityPatch} · ${r.installedApps} aplicaciones · $risk para revisar")
        "RESULTADO: $score/100\n\nAndroid ${r.sdk}\nParche: ${r.securityPatch}\nAplicaciones: ${r.installedApps}\nPara revisar: $risk\n\n${r.recommendations.joinToString("\n") { "• $it" }}"
    }.getOrElse { "No se pudo completar el análisis: ${it.message}" }

    private fun runAppsReview(): String = runCatching { val r = SecurityScanner(this).scan(); if (r.riskApps.isEmpty()) "No se detectaron aplicaciones con las señales básicas configuradas. Esto no garantiza que todas sean seguras." else r.riskApps.joinToString("\n\n") { "${it.label}\n• ${it.reasons.joinToString("\n• ")}" } }.getOrElse { "No se pudo revisar las aplicaciones." }
    private fun runWebReview(): String { val v = input.text.toString(); if (v.isBlank()) return "Introduce una URL para analizar."; val r = LinkChecker.inspect(v); return "RIESGO: ${r.riskLevel}\n\n${r.message}" }
    private fun runPasswordReview(): String { val p = input.text.toString(); val score = listOf(p.length >= 12, p.any { it.isUpperCase() }, p.any { it.isLowerCase() }, p.any { it.isDigit() }, p.any { !it.isLetterOrDigit() }).count { it }; return "FORTALEZA: ${when(score){5->"MUY FUERTE";4->"FUERTE";3->"MEDIA";else->"DÉBIL"}}\n\nCumple $score/5 comprobaciones. Usa una contraseña larga, única y no reutilizada." }
    private fun historyText(): String = SecurityHistory.all(this).ifEmpty { listOf("Todavía no hay análisis guardados.") }.joinToString("\n\n") { raw -> val p=raw.split('|', limit=3); if(p.size==3) "Puntuación: ${p[1]}/100\n${p[2]}" else raw }
    private fun notificationsText(): String = NotificationStore.all(this).ifEmpty { listOf("No tienes alertas guardadas todavía.") }.joinToString("\n\n") { "• ${it.title}\n${it.message}" }
}
