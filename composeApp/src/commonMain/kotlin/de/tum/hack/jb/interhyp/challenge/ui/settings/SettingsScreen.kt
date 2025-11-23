package de.tum.hack.jb.interhyp.challenge.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import de.tum.hack.jb.interhyp.challenge.data.service.MonthSimulationService
import de.tum.hack.jb.interhyp.challenge.domain.model.Coupon
import de.tum.hack.jb.interhyp.challenge.ui.components.AppScaffold
import de.tum.hack.jb.interhyp.challenge.ui.components.CouponPopup
import de.tum.hack.jb.interhyp.challenge.ui.components.Insights
import de.tum.hack.jb.interhyp.challenge.ui.components.NavItem
import de.tum.hack.jb.interhyp.challenge.ui.components.Settings
import de.tum.hack.jb.interhyp.challenge.ui.theme.ThemePreference
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel
import de.tum.hack.jb.interhyp.challenge.ui.locale.LocalePreference
import de.tum.hack.jb.interhyp.challenge.presentation.locale.LocaleViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import jb_interhyp_challenge.composeapp.generated.resources.Res
import jb_interhyp_challenge.composeapp.generated.resources.*

@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel,
    localeViewModel: LocaleViewModel = koinInject(),
    onNavigate: (String) -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val themePreference by themeViewModel.themePreference.collectAsState()
    val localePreference by localeViewModel.localePreference.collectAsState()
    val monthSimulationService: MonthSimulationService = koinInject()
    val userRepository: UserRepository = koinInject()
    val coroutineScope = rememberCoroutineScope()
    
    var isSimulating by remember { mutableStateOf(false) }
    var activeCoupon by remember { mutableStateOf<Coupon?>(null) }
    
    AppScaffold(
        navItemsLeft = listOf(NavItem(id = "insights", label = "Insights", icon = Insights)),
        navItemsRight = listOf(NavItem(id = "settings", label = "Settings", icon = Settings)),
        selectedItemId = "settings",
        onItemSelected = { id ->
            onNavigate(id)
        },
        onHomeClick = { onNavigate("home") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(Res.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Profile Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.profile_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Button(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.edit_profile_information))
                    }
                }
            }
            
            // Appearance Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.theme),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Theme Selection
                    ThemePreference.values().forEach { preference ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themePreference == preference,
                                onClick = { themeViewModel.setThemePreference(preference) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (preference) {
                                    ThemePreference.LIGHT -> stringResource(Res.string.theme_light)
                                    ThemePreference.DARK -> stringResource(Res.string.theme_dark)
                                    ThemePreference.SYSTEM -> stringResource(Res.string.theme_system)
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { themeViewModel.setThemePreference(preference) }
                            )
                        }
                    }
                }
            }
            
            // Language Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.language),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Language Selection
                    LocalePreference.values().forEach { preference ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = localePreference == preference,
                                onClick = { localeViewModel.setLocalePreference(preference) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = preference.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { localeViewModel.setLocalePreference(preference) }
                            )
                        }
                    }
                }
            }
            
            // Developer Settings Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Developer Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSimulating = true
                                try {
                                    val user = userRepository.getUser().first()
                                    if (user != null) {
                                        monthSimulationService.simulateNextMonth(user.id)
                                    }
                                } catch (e: Exception) {
                                    println("Error simulating month: ${e.message}")
                                } finally {
                                    isSimulating = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSimulating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50) // Green color
                        )
                    ) {
                        if (isSimulating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulating...")
                        } else {
                            Text("Simulate Month")
                        }
                    }

                    Button(
                        onClick = {
                            activeCoupon = Coupon(
                                valueEuro = 50,
                                unlockPercentage = 25
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Test Coupon")
                    }
                }
            }
        }
        
        activeCoupon?.let { coupon ->
            CouponPopup(
                coupon = coupon,
                onDismiss = { activeCoupon = null }
            )
        }
    }
}
