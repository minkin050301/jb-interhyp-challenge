package de.tum.hack.jb.interhyp.challenge.util

// External declarations for localStorage API
private external interface Storage {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
}

@JsName("localStorage")
private external val localStorage: Storage

/**
 * Get the saved locale preference from localStorage
 */
actual fun getSavedLocale(): String {
    return try {
        localStorage.getItem("appLocale") ?: "en"
    } catch (e: Exception) {
        "en"
    }
}

/**
 * Initialize the locale manager for WasmJS/Web
 * Call this from main.kt
 */
fun initLocaleManager() {
    // Load saved locale preference and apply it
    val savedLocale = getSavedLocale()
    
    // Update LocaleManager's state
    LocaleManager.initLocale(savedLocale)
}

/**
 * WasmJS-specific implementation to update locale
 * Similar to JS, store preference for the web platform
 */
actual fun updatePlatformLocale(localeCode: String) {
    // Store in localStorage for persistence
    try {
        localStorage.setItem("appLocale", localeCode)
    } catch (e: Exception) {
        // Silently fail if localStorage is not available
    }
}
