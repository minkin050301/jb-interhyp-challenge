package de.tum.hack.jb.interhyp.challenge.util

/**
 * Get the saved locale preference from localStorage
 */
actual fun getSavedLocale(): String {
    return try {
        js("localStorage.getItem('appLocale') || 'en'") as? String ?: "en"
    } catch (e: Exception) {
        "en"
    }
}

/**
 * Initialize the locale manager for JS/Web
 * Call this from main.kt
 */
fun initLocaleManager() {
    // Load saved locale preference and apply it
    val savedLocale = getSavedLocale()
    
    // Update LocaleManager's state
    LocaleManager.initLocale(savedLocale)
}

/**
 * JS-specific implementation to update locale
 * Web apps typically use browser locale, but we can store preference in localStorage
 */
actual fun updatePlatformLocale(localeCode: String) {
    // Store in localStorage for persistence
    try {
        js("localStorage.setItem('appLocale', localeCode)")
    } catch (e: Exception) {
        console.log("Failed to set locale in localStorage: ${e.message}")
    }
}
