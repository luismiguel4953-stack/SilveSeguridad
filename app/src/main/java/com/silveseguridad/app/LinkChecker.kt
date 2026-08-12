package com.silveseguridad.app

import android.net.Uri

object LinkChecker {
    data class Result(val safeToOpen: Boolean, val message: String)

    fun inspect(input: String): Result {
        val value = input.trim()
        val uri = runCatching { Uri.parse(value) }.getOrNull()
        val scheme = uri?.scheme?.lowercase()
        if (value.isBlank() || uri == null || scheme !in setOf("http", "https")) {
            return Result(false, "Introduce un enlace HTTP o HTTPS válido.")
        }
        val host = uri.host
        if (host.isNullOrBlank()) return Result(false, "El enlace no contiene un dominio válido.")
        if (scheme == "http") {
            return Result(false, "El enlace usa HTTP sin cifrado. Prefiere HTTPS cuando sea posible.")
        }
        return Result(true, "El enlace usa HTTPS. Esto no garantiza que el sitio sea legítimo; evita introducir contraseñas si no reconoces el dominio.")
    }
}
