package com.silveseguridad.app

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/** Lightweight, privacy-first device security checks using public Android APIs. */
class SecurityScanner(private val context: Context) {
    data class Report(
        val installedApps: Int,
        val debuggableBuild: Boolean,
        val sdk: Int,
        val recommendations: List<String>
    )

    fun scan(): Report {
        val pm = context.packageManager
        val packages = if (Build.VERSION.SDK_INT >= 33) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(0)
        }
        val recommendations = mutableListOf<String>()
        if (Build.VERSION.SDK_INT < 29) {
            recommendations += "Mantén Android actualizado para recibir mejoras de seguridad."
        }
        if (Build.FINGERPRINT.contains("generic", ignoreCase = true)) {
            recommendations += "El dispositivo parece utilizar una compilación genérica o de prueba."
        }
        if (recommendations.isEmpty()) {
            recommendations += "No se detectaron problemas básicos con las comprobaciones disponibles."
        }
        return Report(
            installedApps = packages.size,
            debuggableBuild = (Build.TAGS?.contains("test-keys") == true),
            sdk = Build.VERSION.SDK_INT,
            recommendations = recommendations
        )
    }
}
