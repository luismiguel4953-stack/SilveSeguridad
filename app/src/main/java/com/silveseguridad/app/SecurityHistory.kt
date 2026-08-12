package com.silveseguridad.app

import android.content.Context

object SecurityHistory {
    private const val PREFS = "silve_security_history"
    private const val KEY = "reports"
    private const val MAX = 20

    fun save(context: Context, score: Int, summary: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val item = "${System.currentTimeMillis()}|$score|${summary.replace("|", " ").replace("\n", " ")}"
        val values = prefs.getStringSet(KEY, emptySet()).orEmpty().toMutableSet()
        values.add(item)
        val trimmed = values.sortedByDescending { it.substringBefore('|').toLongOrNull() ?: 0L }.take(MAX).toSet()
        prefs.edit().putStringSet(KEY, trimmed).apply()
    }

    fun all(context: Context): List<String> = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getStringSet(KEY, emptySet()).orEmpty().sortedByDescending { it.substringBefore('|').toLongOrNull() ?: 0L }

    fun clear(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
}
