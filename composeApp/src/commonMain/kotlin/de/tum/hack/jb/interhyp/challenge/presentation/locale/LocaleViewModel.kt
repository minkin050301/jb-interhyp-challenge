package de.tum.hack.jb.interhyp.challenge.presentation.locale

import de.tum.hack.jb.interhyp.challenge.ui.locale.LocalePreference
import de.tum.hack.jb.interhyp.challenge.util.LocaleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for managing locale preferences
 * Language changes take effect immediately without requiring app restart
 */
class LocaleViewModel {
    // Initialize from LocaleManager's current locale
    private val _localePreference = MutableStateFlow<LocalePreference>(
        getCurrentLocalePreference()
    )
    val localePreference: StateFlow<LocalePreference> = _localePreference.asStateFlow()
    
    /**
     * Get the current LocalePreference based on LocaleManager's saved state
     */
    private fun getCurrentLocalePreference(): LocalePreference {
        val currentLocale = LocaleManager.getCurrentLocale()
        return LocalePreference.values().find { it.localeCode == currentLocale } 
            ?: LocalePreference.ENGLISH
    }
    
    /**
     * Update the locale preference and apply it immediately
     */
    fun setLocalePreference(preference: LocalePreference) {
        if (preference != _localePreference.value) {
            _localePreference.update { preference }
            // Apply locale change immediately
            LocaleManager.setLocale(preference.localeCode)
        }
    }
}
