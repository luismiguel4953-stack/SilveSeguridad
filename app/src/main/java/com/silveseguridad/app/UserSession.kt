package com.silveseguridad.app

import android.content.Context

object UserSession {
    private const val PREFS = "silve_session"
    private const val LOGGED_IN = "logged_in"
    private const val NAME = "name"
    private const val EMAIL = "email"

    fun isLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(LOGGED_IN, false)

    fun signIn(context: Context, name: String, email: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(LOGGED_IN, true).putString(NAME, name).putString(EMAIL, email).apply()
    }

    fun signOut(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun name(context: Context): String = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(NAME, "Usuario") ?: "Usuario"
    fun email(context: Context): String = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(EMAIL, "") ?: ""
}
