package de.tum.hack.jb.interhyp.challenge.presentation.theme

import de.tum.hack.jb.interhyp.challenge.ui.theme.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for managing theme preferences
 * Uses in-memory state for now (can be replaced with DataStore later)
 */
class ThemeViewModel {
    private val _themePreference = MutableStateFlow<ThemePreference>(ThemePreference.SYSTEM)
    val themePreference: StateFlow<ThemePreference> = _themePreference.asStateFlow()
    
    /**
     * Update the theme preference
     */
    fun setThemePreference(preference: ThemePreference) {
        _themePreference.update { preference }
    }
}

