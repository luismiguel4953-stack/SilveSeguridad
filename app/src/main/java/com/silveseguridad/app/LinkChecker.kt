package com.silveseguridad.app

import android.net.Uri

object LinkChecker {
    data class Result(val safeToOpen: Boolean, val message: String, val riskLevel: String)

    private val suspiciousTerms = listOf("login", "verify", "verification", "secure", "account", "password", "signin", "update", "gift", "prize")

    fun inspect(input: String): Result {
        val value = input.trim()
        val uri = runCatching { Uri.parse(value) }.getOrNull()
        val scheme = uri?.scheme?.lowercase()
        if (value.isBlank() || uri == null || scheme !in setOf("http", "https")) return Result(false, "Introduce un enlace HTTP o HTTPS válido.", "ALTO")
        val host = uri.host?.lowercase()?.trim('.')
        if (host.isNullOrBlank() || host.contains("..")) return Result(false, "El enlace no contiene un dominio válido.", "ALTO")

        val issues = mutableListOf<String>()
        if (scheme == "http") issues += "No usa HTTPS"
        if (host.contains("xn--")) issues += "Usa un dominio internacionalizado que requiere precaución"
        if (host.count { it == '.' } >= 3) issues += "Tiene una estructura de subdominios inusual"
        if (uri.userInfo != null) issues += "Incluye información antes del dominio"
        if (suspiciousTerms.any { host.contains(it) }) issues += "El dominio contiene términos frecuentemente usados en páginas de acceso o engaño"
        if (value.length > 240) issues += "La URL es excepcionalmente larga"

        if (issues.isNotEmpty()) {
            return Result(false, "Señales para revisar:\n• ${issues.joinToString("\n• ")}\n\nNo introduzcas credenciales ni datos bancarios hasta verificar el dominio por otra vía.", if (issues.size >= 2) "ALTO" else "MEDIO")
        }
        return Result(true, "No se encontraron señales básicas de riesgo en la URL. HTTPS es una buena señal, pero no demuestra que el sitio sea legítimo.", "BAJO")
    }
}
