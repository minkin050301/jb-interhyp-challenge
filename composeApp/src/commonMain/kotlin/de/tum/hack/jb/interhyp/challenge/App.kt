package de.tum.hack.jb.interhyp.challenge

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
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    DarkAppTheme {
        SetupForm()
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

@Composable
fun SetupForm() {
    var submitted by remember { mutableStateOf(false) }

    // Steps: 0 = Greeting, 1 = Target, 2 = Personal, 3 = Summary
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 3 // excluding summary presentation

    // Greeting
    var userName by remember { mutableStateOf("") }

    // Target
    var targetType by remember { mutableStateOf("House") } // "House" or "Apartment"
    var sizeSqm by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // Personal
    var age by remember { mutableStateOf("") }
    var netIncome by remember { mutableStateOf("") } // monthly
    var currentWealth by remember { mutableStateOf("") }
    var monthlyExpenses by remember { mutableStateOf("") }
    var adults by remember { mutableStateOf("") }
    var children by remember { mutableStateOf("") }

    fun toDoubleSafe(s: String): Double? = s.replace(',', '.').toDoubleOrNull()
    fun toIntSafe(s: String): Int? = s.toIntOrNull()

    val greetingValid by remember(userName) { mutableStateOf(userName.isNotBlank()) }
    val targetValid by remember(targetType, sizeSqm, location) {
        mutableStateOf(
            location.isNotBlank() &&
                toDoubleSafe(sizeSqm)?.let { it > 0 } == true &&
                (targetType == "House" || targetType == "Apartment")
        )
    }
    val personalValid by remember(age, netIncome, currentWealth, monthlyExpenses, adults, children) {
        mutableStateOf(
            toIntSafe(age)?.let { it in 16..100 } == true &&
                toDoubleSafe(netIncome)?.let { it >= 0 } == true &&
                toDoubleSafe(currentWealth)?.let { it >= 0 } == true &&
                toDoubleSafe(monthlyExpenses)?.let { it >= 0 } == true &&
                toIntSafe(adults)?.let { it >= 1 } == true &&
                toIntSafe(children)?.let { it >= 0 } == true
        )
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
        Text(
            "Home Savings Setup",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        val stepLabel = when (currentStep) {
            0 -> "Step 1 of $totalSteps · Welcome"
            1 -> "Step 2 of $totalSteps · Your Target"
            2 -> "Step 3 of $totalSteps · About You"
            else -> "Summary"
        }
        Text(stepLabel, style = MaterialTheme.typography.titleMedium)

        when (currentStep) {
            0 -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle("Welcome")
                        Text("We're excited to help you plan your home savings. Let's start with your name.")
                        TextFieldSimple(label = "Your name", value = userName, onValueChange = { userName = it })
                    }
                }
            }
            1 -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle("Your Target")
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
                        TextFieldSimple(label = "Location (city/region)", value = location, onValueChange = { location = it })
                    }
                }
            }
            2 -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle("About You")
                        if (userName.isNotBlank()) Text("Hi $userName!")
                        NumberField(label = "Age", value = age, onValueChange = { age = it })
                        NumberField(label = "Net income (per month)", value = netIncome, onValueChange = { netIncome = it })
                        NumberField(label = "Current wealth (savings)", value = currentWealth, onValueChange = { currentWealth = it })
                        NumberField(label = "Monthly expenses", value = monthlyExpenses, onValueChange = { monthlyExpenses = it })
                        Divider()
                        SectionTitle("Household composition")
                        NumberField(label = "Adults", value = adults, onValueChange = { adults = it })
                        NumberField(label = "Children", value = children, onValueChange = { children = it })
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
                        Text("Household: ${adults.ifBlank { "?" }} adults, ${children.ifBlank { "?" }} children")
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                if (currentStep > 0) currentStep -= 1
            }, enabled = currentStep > 0) {
                Text("Back")
            }

            val canGoNext = when (currentStep) {
                0 -> greetingValid
                1 -> targetValid
                2 -> personalValid
                else -> false
            }

            if (currentStep < 2) {
                Button(onClick = { currentStep += 1 }, enabled = canGoNext) { Text("Next") }
            } else if (currentStep == 2) {
                Button(onClick = { currentStep = 3 }, enabled = canGoNext) { Text("Review") }
            } else {
                Button(onClick = { submitted = true }, enabled = targetValid && personalValid && greetingValid) {
                    Text("Submit")
                }
            }

            Button(onClick = {
                // reset all
                submitted = false
                currentStep = 0
                userName = ""
                targetType = "House"
                sizeSqm = ""
                location = ""
                age = ""
                netIncome = ""
                currentWealth = ""
                monthlyExpenses = ""
                adults = ""
                children = ""
            }) {
                Text("Reset")
            }
        }

        if (submitted) {
            Spacer(Modifier.height(8.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("All set!", style = MaterialTheme.typography.titleLarge)
                    Text("Your setup is complete. You can now proceed with these details.")
                }
            }
        }
    }
}