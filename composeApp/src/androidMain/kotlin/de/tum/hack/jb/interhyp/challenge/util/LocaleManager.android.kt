package de.tum.hack.jb.interhyp.challenge.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

private const val PREFS_NAME = "app_preferences"
private const val KEY_LOCALE = "locale_preference"

private var appContext: Context? = null
private var currentActivity: Activity? = null

/**
 * Get the saved locale preference from SharedPreferences
 */
actual fun getSavedLocale(): String {
    // If context is available, read from SharedPreferences
    val context = appContext
    return if (context != null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString(KEY_LOCALE, "en") ?: "en"
    } else {
        // Return default "en" if context is not available yet
        "en"
    }
}

/**
 * Internal function to get saved locale with context
 */
private fun getSavedLocaleInternal(context: Context): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_LOCALE, "en") ?: "en"
}

/**
 * Initialize the locale manager with application context
 * Call this from MainActivity.onCreate()
 */
fun initLocaleManager(context: Context) {
    appContext = context.applicationContext
    if (context is Activity) {
        currentActivity = context
    }
    
    // Load saved locale preference and apply it
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val savedLocale = prefs.getString(KEY_LOCALE, "en") ?: "en"
    
    // Update LocaleManager's state WITHOUT triggering recreation
    LocaleManager.initLocale(savedLocale)
    
    val locale = Locale(savedLocale)
    Locale.setDefault(locale)
    
    // Use existing configuration and update locale
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    
    @Suppress("DEPRECATION")
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

/**
 * Android-specific implementation to update locale configuration
 * This will recreate the current activity to apply the new locale
 */
actual fun updatePlatformLocale(localeCode: String) {
    val context = appContext ?: return
    
    // Save locale preference
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_LOCALE, localeCode).apply()
    
    val locale = Locale(localeCode)
    Locale.setDefault(locale)
    
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    
    @Suppress("DEPRECATION")
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    
    // Recreate the activity to apply the locale change
    currentActivity?.recreate()
}
