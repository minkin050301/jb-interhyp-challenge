package de.tum.hack.jb.interhyp.challenge.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyType
import de.tum.hack.jb.interhyp.challenge.presentation.onboarding.OnboardingViewModel
import de.tum.hack.jb.interhyp.challenge.ui.components.DatePickerField
import de.tum.hack.jb.interhyp.challenge.ui.components.ImagePicker
import de.tum.hack.jb.interhyp.challenge.ui.goal.GoalSelectionScreen
import de.tum.hack.jb.interhyp.challenge.ui.locale.LocalePreference
import de.tum.hack.jb.interhyp.challenge.ui.util.byteArrayToImageBitmap
import de.tum.hack.jb.interhyp.challenge.util.LocaleManager
import de.tum.hack.jb.interhyp.challenge.util.getFocusManager
import jb_interhyp_challenge.composeapp.generated.resources.Res
import jb_interhyp_challenge.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun OnboardingScreen(
    onSkip: () -> Unit = {},
    onComplete: () -> Unit = {},
    viewModel: OnboardingViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Steps: 0 = Greeting, 1 = Target, 2 = Goal Selection, 3 = Personal, 4 = Summary
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 4 // excluding summary presentation

    // Local UI state for form fields (strings for text input)
    var userName by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf("House") } // "House" or "Apartment"
    var sizeSqm by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var netIncome by remember { mutableStateOf("") } // monthly
    var currentWealth by remember { mutableStateOf("") }
    var monthlyExpenses by remember { mutableStateOf("") }

    // Load saved profile data on initialization
    LaunchedEffect(Unit) {
        viewModel.loadSavedProfile()
    }

    // Sync ViewModel state to local form fields when data is loaded
    LaunchedEffect(uiState) {
        if (uiState.name.isNotBlank() && userName.isBlank()) {
            userName = uiState.name
        }
        if (uiState.age > 0 && age.isBlank()) {
            age = uiState.age.toString()
        }
        if (uiState.monthlyIncome > 0 && netIncome.isBlank()) {
            netIncome = uiState.monthlyIncome.toString()
        }
        if (uiState.monthlyExpenses > 0 && monthlyExpenses.isBlank()) {
            monthlyExpenses = uiState.monthlyExpenses.toString()
        }
        if (uiState.currentEquity > 0 && currentWealth.isBlank()) {
            currentWealth = uiState.currentEquity.toString()
        }
        if (uiState.desiredLocation.isNotBlank() && location.isBlank()) {
            location = uiState.desiredLocation
        }
        if (uiState.desiredPropertySize > 0 && sizeSqm.isBlank()) {
            sizeSqm = uiState.desiredPropertySize.toString()
        }
        if (uiState.desiredPropertyType == PropertyType.HOUSE) {
            targetType = "House"
        } else if (uiState.desiredPropertyType == PropertyType.APARTMENT) {
            targetType = "Apartment"
        }
    }

    // Handle completion
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onComplete()
        }
    }

    fun toDoubleSafe(s: String): Double? = s.replace(',', '.').toDoubleOrNull()
    fun toIntSafe(s: String): Int? = s.toIntOrNull()

    // Helper function to sync form fields to ViewModel
    fun syncFormToViewModel() {
        viewModel.updateName(userName)
        toIntSafe(age)?.let { viewModel.updateAge(it) }
        toDoubleSafe(netIncome)?.let { viewModel.updateMonthlyIncome(it) }
        toDoubleSafe(monthlyExpenses)?.let { viewModel.updateMonthlyExpenses(it) }
        toDoubleSafe(currentWealth)?.let { viewModel.updateCurrentEquity(it) }
        viewModel.updateDesiredLocation(location)
        toDoubleSafe(sizeSqm)?.let { viewModel.updateDesiredPropertySize(it) }
        viewModel.updateDesiredPropertyType(
            if (targetType == "House") PropertyType.HOUSE else PropertyType.APARTMENT
        )
    }

    val greetingValid by remember(userName) { mutableStateOf(userName.isNotBlank()) }
    val targetValid by remember(targetType, sizeSqm, location) {
        mutableStateOf(
            location.isNotBlank() &&
                    toDoubleSafe(sizeSqm)?.let { it > 0 } == true &&
                    (targetType == "House" || targetType == "Apartment")
        )
    }
    val personalValid by remember(age, netIncome, currentWealth, monthlyExpenses) {
        mutableStateOf(
            toIntSafe(age)?.let { it in 16..100 } == true &&
                    toDoubleSafe(netIncome)?.let { it >= 0 } == true &&
                    toDoubleSafe(currentWealth)?.let { it >= 0 } == true &&
                    toDoubleSafe(monthlyExpenses)?.let { it >= 0 } == true
        )
    }

    val scroll = rememberScrollState()
    val focusManager = getFocusManager()
    
    // Language switcher state
    var showLanguageMenu by remember { mutableStateOf(false) }
    val currentLocale by LocaleManager.currentLocale.collectAsState()

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .pointerInput(focusManager) {
                    detectTapGestures {
                        focusManager?.clearFocus()
                    }
                }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        Spacer(Modifier.height(32.dp))
        
        Text(
            stringResource(Res.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        val stepLabel = when (currentStep) {
            0 -> "${stringResource(Res.string.step_1_of, totalSteps)} · ${stringResource(Res.string.step_welcome)}"
            1 -> "${stringResource(Res.string.step_2_of, totalSteps)} · ${stringResource(Res.string.step_your_target)}"
            2 -> "${stringResource(Res.string.step_3_of, totalSteps)} · ${stringResource(Res.string.step_goal_selection)}"
            3 -> "${stringResource(Res.string.step_4_of, totalSteps)} · ${stringResource(Res.string.step_about_you)}"
            else -> stringResource(Res.string.summary)
        }
        Text(
            stepLabel, 
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        when (currentStep) {
            0 -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle(stringResource(Res.string.welcome))
                        Text(stringResource(Res.string.welcome_message))
                        TextFieldSimple(label = stringResource(Res.string.your_name), value = userName, onValueChange = { userName = it })
                        // Optional: Allow skipping onboarding to jump straight to main dashboard
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = onSkip) { Text(stringResource(Res.string.skip_for_now)) }
                        }
                    }
                }
            }
            1 -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle(stringResource(Res.string.your_target))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            RadioOption(
                                label = stringResource(Res.string.house),
                                selected = targetType == "House",
                                onSelect = { targetType = "House" }
                            )
                            RadioOption(
                                label = stringResource(Res.string.apartment),
                                selected = targetType == "Apartment",
                                onSelect = { targetType = "Apartment" }
                            )
                        }
                        NumberField(label = stringResource(Res.string.size_sqm), value = sizeSqm, onValueChange = { sizeSqm = it })
                        LocationDropdown(value = location, onValueChange = { location = it })
                    }
                }
            }
            2 -> {
                // Goal Selection Step - moved to right after target
                val propertyType = if (targetType == "House") PropertyType.HOUSE else PropertyType.APARTMENT
                val size = toDoubleSafe(sizeSqm) ?: 80.0
                GoalSelectionScreen(
                    location = location.ifBlank { "Munich" },
                    size = size,
                    propertyType = propertyType,
                    onContinue = { imageUrl ->
                        syncFormToViewModel()
                        viewModel.updateGoalPropertyImage(imageUrl)
                        currentStep = 3 // Move to personal info
                    }
                )
            }
            3 -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle(stringResource(Res.string.about_you))
                        if (userName.isNotBlank()) Text(stringResource(Res.string.hi_user, userName))
                        NumberField(label = stringResource(Res.string.age), value = age, onValueChange = { age = it })
                        NumberField(label = stringResource(Res.string.net_income_per_month), value = netIncome, onValueChange = { netIncome = it })
                        
                        NumberField(label = stringResource(Res.string.current_wealth_savings), value = currentWealth, onValueChange = { currentWealth = it })
                        NumberField(label = stringResource(Res.string.monthly_expenses), value = monthlyExpenses, onValueChange = { monthlyExpenses = it })
                    }
                }
            }
            else -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Summary", style = MaterialTheme.typography.titleLarge)
                        if (userName.isNotBlank()) Text("Thanks, $userName!")
                        Text("Target: $targetType, ${sizeSqm.ifBlank { "?" }} sqm in ${location.ifBlank { "?" }}")
                        Text(
                            "Personal: age ${age.ifBlank { "?" }}, net income ${netIncome.ifBlank { "?" }}, wealth ${currentWealth.ifBlank { "?" }}, expenses ${monthlyExpenses.ifBlank { "?" }}"
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                if (currentStep > 0) {
                    syncFormToViewModel()
                    viewModel.saveIntermediateProgress()
                    currentStep -= 1
                }
            }, enabled = currentStep > 0) {
                Text(stringResource(Res.string.previous))
            }

            val canGoNext = when (currentStep) {
                0 -> greetingValid
                1 -> targetValid
                2 -> true // Goal selection handles its own navigation
                3 -> personalValid
                else -> false
            }

            if (currentStep < 2) {
                Button(onClick = {
                    syncFormToViewModel()
                    viewModel.saveIntermediateProgress()
                    currentStep += 1
                }, enabled = canGoNext) { Text(stringResource(Res.string.next)) }
            } else if (currentStep == 2) {
                // Goal selection handles its own continue button
                // No button needed here
            } else if (currentStep == 3) {
                Button(onClick = {
                    syncFormToViewModel()
                    viewModel.saveIntermediateProgress()
                    currentStep = 4 // Move to summary
                }, enabled = canGoNext) { Text(stringResource(Res.string.review_summary)) }
            } else {
                Button(
                    onClick = {
                        // Update ViewModel with all form data before submitting
                        syncFormToViewModel()
                        // Submit to save data permanently
                        viewModel.submitOnboarding()
                    },
                    enabled = targetValid && personalValid && greetingValid && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text(stringResource(Res.string.finish))
                    }
                }
            }

            Button(onClick = {
                // reset all
                currentStep = 0
                userName = ""
                targetType = "House"
                sizeSqm = ""
                location = ""
                age = ""
                netIncome = ""
                currentWealth = ""
                monthlyExpenses = ""
            }) {
                Text("Reset")
            }
        }

        // Proceed Later button - save progress and exit onboarding
        if (currentStep < 4) {
            Button(
                onClick = {
                    syncFormToViewModel()
                    viewModel.saveIntermediateProgress()
                    onSkip()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.proceed_later))
            }
        }

        // Show error message if any
        uiState.errorMessage?.let { errorMsg ->
            Spacer(Modifier.height(8.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Error", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                    Text(errorMsg, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        }
        
        // Language switcher button - positioned at bottom right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            OutlinedButton(
                onClick = { showLanguageMenu = true },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌐",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
            
            androidx.compose.material3.DropdownMenu(
                expanded = showLanguageMenu,
                onDismissRequest = { showLanguageMenu = false }
            ) {
                LocalePreference.values().forEach { locale ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(locale.displayName)
                                if (locale.localeCode == currentLocale) {
                                    Text("✓", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        onClick = {
                            LocaleManager.setLocale(locale.localeCode)
                            showLanguageMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            // allow digits and one decimal separator
            if (input.isEmpty() || input.matches(Regex("[0-9]*[.,]?[0-9]*"))) {
                onValueChange(input)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun TextFieldSimple(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun RadioOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text = label)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cities = listOf(
        "Munich",
        "Berlin",
        "Hamburg",
        "Cologne",
        "Frankfurt",
        "Stuttgart",
        "Düsseldorf",
        "Dortmund",
        "Essen",
        "Leipzig",
        "Bremen",
        "Dresden",
        "Hanover",
        "Nuremberg",
        "Duisburg"
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.location_city)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            cities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city) },
                    onClick = {
                        onValueChange(city)
                        expanded = false
                    }
                )
            }
        }
    }
}