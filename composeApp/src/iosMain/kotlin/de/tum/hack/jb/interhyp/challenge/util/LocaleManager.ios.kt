package de.tum.hack.jb.interhyp.challenge.util

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults

/**
 * Get the saved locale preference from UserDefaults
 */
actual fun getSavedLocale(): String {
    val languages = NSUserDefaults.standardUserDefaults.arrayForKey("AppleLanguages")
    return if (languages != null && languages.count().toInt() > 0) {
        (languages[0] as? String) ?: "en"
    } else {
        "en"
    }
}

/**
 * Initialize the locale manager for iOS
 * Call this from MainViewController
 */
fun initLocaleManager() {
    // Load saved locale preference and apply it
    val savedLocale = getSavedLocale()
    
    // Update LocaleManager's state
    LocaleManager.initLocale(savedLocale)
}

/**
 * iOS-specific implementation to update locale
 * Note: iOS apps typically respect system locale, but we can set a preference
 */
actual fun updatePlatformLocale(localeCode: String) {
    // Set user defaults for app locale preference
    NSUserDefaults.standardUserDefaults.setObject(
        listOf(localeCode),
        forKey = "AppleLanguages"
    )
    NSUserDefaults.standardUserDefaults.synchronize()
}
