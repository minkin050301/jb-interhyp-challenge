package de.tum.hack.jb.interhyp.challenge.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Platform-specific function to update the system locale configuration
 */
expect fun updatePlatformLocale(localeCode: String)

/**
 * Platform-specific function to get the saved locale preference
 */
expect fun getSavedLocale(): String

/**
 * Manages runtime locale changes for the app
 */
object LocaleManager {
    private val _currentLocale = MutableStateFlow("en")
    val currentLocale: StateFlow<String> = _currentLocale.asStateFlow()
    
    /**
     * Initialize the locale without triggering platform update
     * Used during app startup to restore saved state
     */
    internal fun initLocale(localeCode: String) {
        _currentLocale.value = localeCode
    }
    
    /**
     * Set the app locale at runtime
     * @param localeCode ISO 639-1 language code (e.g., "en", "de")
     */
    fun setLocale(localeCode: String) {
        _currentLocale.value = localeCode
        updatePlatformLocale(localeCode)
    }
    
    /**
     * Get the current locale code
     */
    fun getCurrentLocale(): String = _currentLocale.value
}

/**
 * CompositionLocal for providing locale throughout the composition tree
 */
val LocalAppLocale = compositionLocalOf { "en" }

/**
 * Wrapper composable that provides locale context
 */
@Composable
fun ProvideAppLocale(
    locale: String,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppLocale provides locale) {
        content()
    }
}
