package de.tum.hack.jb.interhyp.challenge.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import java.util.Locale
import java.lang.ref.WeakReference

private const val PREFS_NAME = "app_preferences"
private const val KEY_LOCALE = "locale_preference"

private var appContext: Context? = null
private var currentActivityRef: WeakReference<Activity>? = null

/**
 * Get the saved locale preference from SharedPreferences
 */
actual fun getSavedLocale(): String {
    // If context is available, read from SharedPreferences
    val context = appContext
    return if (context != null) {
        getSavedLocaleInternal(context)
    } else {
        // Return default "en" if context is not available yet
        "en"
    }
}

/**
 * Internal function to get saved locale with context
 */
internal fun getSavedLocaleInternal(context: Context): String {
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
        currentActivityRef = WeakReference(context)
    }
    
    val savedLocale = getSavedLocaleInternal(context)
    
    // Update LocaleManager's state WITHOUT triggering recreation
    LocaleManager.initLocale(savedLocale)
    
    val locale = Locale(savedLocale)
    Locale.setDefault(locale)
}

/**
 * Apply the saved locale to the context.
 * Call this in MainActivity.attachBaseContext()
 */
fun applyLocale(context: Context): Context {
    val savedLocale = getSavedLocaleInternal(context)
    val locale = Locale(savedLocale)
    Locale.setDefault(locale)
    
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    return context.createConfigurationContext(config)
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
    
    // Restart the activity to apply the locale change
    // usage of recreate() can cause crashes on some devices/configurations
    // so we use a fresh intent instead
    val activity = currentActivityRef?.get()
    if (activity != null) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
    }
}
