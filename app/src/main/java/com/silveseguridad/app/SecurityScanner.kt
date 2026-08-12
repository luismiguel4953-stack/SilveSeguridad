package com.silveseguridad.app

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

/** Privacy-first checks using public Android APIs. Does not inspect private app data. */
class SecurityScanner(private val context: Context) {
    data class AppRisk(val packageName: String, val label: String, val reasons: List<String>)
    data class Report(
        val installedApps: Int,
        val debuggableBuild: Boolean,
        val sdk: Int,
        val securityPatch: String,
        val riskApps: List<AppRisk>,
        val recommendations: List<String>
    )

    fun scan(): Report {
        val pm = context.packageManager
        val packages = if (Build.VERSION.SDK_INT >= 33) pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())) else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        }
        val risks = packages.mapNotNull { info ->
            val app = info.applicationInfo ?: return@mapNotNull null
            val reasons = mutableListOf<String>()
            if ((app.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) reasons += "La aplicación está marcada como depurable"
            val permissions = info.requestedPermissions?.toSet().orEmpty()
            val sensitive = setOf(
                "android.permission.READ_SMS", "android.permission.RECEIVE_SMS",
                "android.permission.RECORD_AUDIO", "android.permission.CAMERA",
                "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.READ_CONTACTS", "android.permission.CALL_PHONE"
            )
            val count = permissions.count { it in sensitive }
            if (count >= 4) reasons += "Solicita varios permisos sensibles ($count)"
            if (reasons.isEmpty()) return@mapNotNull null
            AppRisk(info.packageName, app.loadLabel(pm).toString(), reasons)
        }.filter { it.packageName != context.packageName }.take(20)

        val recommendations = mutableListOf<String>()
        if (Build.VERSION.SDK_INT < 29) recommendations += "Mantén Android actualizado para recibir mejoras de seguridad."
        if (Build.TAGS?.contains("test-keys") == true || Build.FINGERPRINT.contains("generic", true)) recommendations += "El dispositivo parece utilizar una compilación de prueba o genérica."
        if (risks.isNotEmpty()) recommendations += "Revisa ${risks.size} aplicación(es) con señales que merecen atención."
        if (recommendations.isEmpty()) recommendations += "No se detectaron problemas básicos con las comprobaciones disponibles."

        val isTestBuild = Build.TAGS?.contains("test-keys") == true || Build.FINGERPRINT.contains("generic", true)
        return Report(packages.size, isTestBuild, Build.VERSION.SDK_INT, Build.VERSION.SECURITY_PATCH, risks, recommendations)
    }
}
