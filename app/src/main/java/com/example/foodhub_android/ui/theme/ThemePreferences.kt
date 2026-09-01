package com.example.foodhub_android.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val label: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark")
}

/** A small app-wide preference store shared by every product flavor. */
object ThemePreferences {
    private const val PREFERENCES = "swiftbite_appearance"
    private const val THEME_MODE = "theme_mode"

    private val _mode = MutableStateFlow(ThemeMode.SYSTEM)
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun initialize(context: Context) {
        val saved = context.applicationContext
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(THEME_MODE, ThemeMode.SYSTEM.name)
        _mode.value = runCatching { ThemeMode.valueOf(saved.orEmpty()) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    fun setMode(context: Context, mode: ThemeMode) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(THEME_MODE, mode.name)
            .apply()
        _mode.value = mode
    }
}
