package com.silveseguridad.app

import android.content.Context

class EmergencyStore(context: Context) {
    private val prefs = context.getSharedPreferences("emergency", Context.MODE_PRIVATE)

    var contact: String
        get() = prefs.getString("contact", "") ?: ""
        set(value) { prefs.edit().putString("contact", value.trim()).apply() }

    var message: String
        get() = prefs.getString("message", "Necesito ayuda. Este es un mensaje de emergencia.") ?: ""
        set(value) { prefs.edit().putString("message", value.trim()).apply() }
}
