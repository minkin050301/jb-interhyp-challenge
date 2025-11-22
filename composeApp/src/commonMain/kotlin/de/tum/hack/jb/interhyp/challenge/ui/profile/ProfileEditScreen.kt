package de.tum.hack.jb.interhyp.challenge.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyType
import de.tum.hack.jb.interhyp.challenge.presentation.profile.ProfileViewModel
import de.tum.hack.jb.interhyp.challenge.ui.components.DatePickerField
import org.koin.compose.koinInject

@Composable
fun ProfileEditScreen(
    onBack: () -> Unit = {},
    viewModel: ProfileViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Local UI state for form fields (strings for text input)
    var userName by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf("House") }
    var sizeSqm by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var netIncome by remember { mutableStateOf("") }
    var futureIncome by remember { mutableStateOf("") }
    var currentWealth by remember { mutableStateOf("") }
    var monthlyExpenses by remember { mutableStateOf("") }
    var existingCredits by remember { mutableStateOf("") }
    var adults by remember { mutableStateOf("") }
    var children by remember { mutableStateOf("") }
    var desiredChildren by remember { mutableStateOf("") }

    // Load profile data into local state when uiState changes
    LaunchedEffect(uiState.hasExistingProfile) {
        if (uiState.hasExistingProfile && userName.isBlank()) {
            userName = uiState.name
            age = uiState.age.toString()
            netIncome = uiState.monthlyIncome.toString()
            futureIncome = uiState.futureMonthlyIncome?.toString() ?: ""
            currentWealth = uiState.currentEquity.toString()
            monthlyExpenses = uiState.monthlyExpenses.toString()
            existingCredits = if (uiState.existingCredits > 0) uiState.existingCredits.toString() else ""
            location = uiState.desiredLocation
            sizeSqm = uiState.desiredPropertySize.toString()
            targetType = if (uiState.desiredPropertyType == PropertyType.HOUSE) "House" else "Apartment"
            targetDate = uiState.targetDate ?: ""
            desiredChildren = if (uiState.desiredChildren > 0) uiState.desiredChildren.toString() else ""
            adults = uiState.numberOfAdults.toString()
            children = uiState.numberOfChildren.toString()
        }
    }

    // Show success message and navigate back after save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.resetSavedFlag()
            onBack()
        }
    }

    fun toDoubleSafe(s: String): Double? = s.replace(',', '.').toDoubleOrNull()
    fun toIntSafe(s: String): Int? = s.toIntOrNull()

    val isValid = remember(userName, age, netIncome, currentWealth, monthlyExpenses, adults, children, location, sizeSqm) {
        userName.isNotBlank() &&
                toIntSafe(age)?.let { it in 16..100 } == true &&
                toDoubleSafe(netIncome)?.let { it >= 0 } == true &&
                toDoubleSafe(currentWealth)?.let { it >= 0 } == true &&
                toDoubleSafe(monthlyExpenses)?.let { it >= 0 } == true &&
                toIntSafe(adults)?.let { it >= 1 } == true &&
                toIntSafe(children)?.let { it >= 0 } == true &&
                location.isNotBlank() &&
                toDoubleSafe(sizeSqm)?.let { it > 0 } == true
    }

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            "Edit Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        // Personal Information
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Personal Information")
                TextFieldSimple(label = "Your name", value = userName, onValueChange = { userName = it })
                NumberField(label = "Age", value = age, onValueChange = { age = it })
            }
        }

        // Financial Information
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Financial Information")
                NumberField(label = "Net income (per month)", value = netIncome, onValueChange = { netIncome = it })
                NumberField(label = "Future income (per month) [Optional]", value = futureIncome, onValueChange = { futureIncome = it })
                NumberField(label = "Current wealth (savings)", value = currentWealth, onValueChange = { currentWealth = it })
                NumberField(label = "Monthly expenses", value = monthlyExpenses, onValueChange = { monthlyExpenses = it })
                NumberField(label = "Existing credits (per month) [Optional]", value = existingCredits, onValueChange = { existingCredits = it })
            }
        }

        // Property Target
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Property Target")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    RadioOption(
                        label = "House",
                        selected = targetType == "House",
                        onSelect = { targetType = "House" }
                    )
                    RadioOption(
                        label = "Apartment",
                        selected = targetType == "Apartment",
                        onSelect = { targetType = "Apartment" }
                    )
                }
                NumberField(label = "Size (sqm)", value = sizeSqm, onValueChange = { sizeSqm = it })
                LocationDropdown(value = location, onValueChange = { location = it })
                DatePickerField(label = "Target date [Optional]", value = targetDate, onValueChange = { targetDate = it })
            }
        }

        // Household
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Household Composition")
                NumberField(label = "Adults", value = adults, onValueChange = { adults = it })
                NumberField(label = "Children", value = children, onValueChange = { children = it })
                NumberField(label = "Desired future children [Optional]", value = desiredChildren, onValueChange = { desiredChildren = it })
            }
        }

        Spacer(Modifier.height(8.dp))

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    // Update ViewModel with all form data
                    viewModel.updateName(userName)
                    toIntSafe(age)?.let { viewModel.updateAge(it) }
                    toDoubleSafe(netIncome)?.let { viewModel.updateMonthlyIncome(it) }
                    viewModel.updateFutureMonthlyIncome(toDoubleSafe(futureIncome))
                    toDoubleSafe(monthlyExpenses)?.let { viewModel.updateMonthlyExpenses(it) }
                    toDoubleSafe(currentWealth)?.let { viewModel.updateCurrentEquity(it) }
                    toDoubleSafe(existingCredits)?.let { viewModel.updateExistingCredits(it) }
                    viewModel.updateDesiredLocation(location)
                    toDoubleSafe(sizeSqm)?.let { viewModel.updateDesiredPropertySize(it) }
                    viewModel.updateDesiredPropertyType(
                        if (targetType == "House") PropertyType.HOUSE else PropertyType.APARTMENT
                    )
                    viewModel.updateTargetDate(targetDate.ifBlank { null })
                    toIntSafe(desiredChildren)?.let { viewModel.updateDesiredChildren(it) }
                    toIntSafe(adults)?.let { viewModel.updateNumberOfAdults(it) }
                    toIntSafe(children)?.let { viewModel.updateNumberOfChildren(it) }
                    // Save profile
                    viewModel.saveProfile()
                },
                enabled = isValid && !uiState.isLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Save Changes")
                }
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
            label = { Text("Location (city)") },
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
