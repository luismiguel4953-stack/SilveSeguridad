package com.silveseguridad.app

import android.content.Context

object NotificationStore {
    private const val PREFS = "silve_notifications"
    private const val ITEMS = "items"
    private const val MAX_ITEMS = 30

    data class Item(val title: String, val message: String, val time: Long)

    fun add(context: Context, title: String, message: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val encoded = Item(title, message, System.currentTimeMillis()).let { "${it.time}|${it.title.replace("|", " ")}|${it.message.replace("|", " ")}" }
        val current = prefs.getStringSet(ITEMS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(encoded)
        val trimmed = current.sortedByDescending { it.substringBefore('|').toLongOrNull() ?: 0L }.take(MAX_ITEMS).toSet()
        prefs.edit().putStringSet(ITEMS, trimmed).apply()
    }

    fun all(context: Context): List<Item> = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getStringSet(ITEMS, emptySet()).orEmpty().mapNotNull { raw ->
            val parts = raw.split('|', limit = 3)
            if (parts.size == 3) Item(parts[1], parts[2], parts[0].toLongOrNull() ?: 0L) else null
        }.sortedByDescending { it.time }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(ITEMS).apply()
    }
}
