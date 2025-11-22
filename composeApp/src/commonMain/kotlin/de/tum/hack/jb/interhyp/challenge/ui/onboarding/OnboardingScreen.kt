package de.tum.hack.jb.interhyp.challenge.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils
import de.tum.hack.jb.interhyp.challenge.ui.components.ImagePicker
import de.tum.hack.jb.interhyp.challenge.ui.util.byteArrayToImageBitmap
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyType
import de.tum.hack.jb.interhyp.challenge.presentation.onboarding.OnboardingViewModel
import org.koin.compose.koinInject

@Composable
fun OnboardingScreen(
    onSkip: () -> Unit = {},
    onComplete: () -> Unit = {},
    viewModel: OnboardingViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Steps: 0 = Greeting, 1 = Target, 2 = Personal, 3 = Selfie, 4 = Summary
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 4 // excluding summary presentation

    // Local UI state for form fields (strings for text input)
    var userName by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf("House") } // "House" or "Apartment"
    var sizeSqm by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var netIncome by remember { mutableStateOf("") } // monthly
    var futureIncome by remember { mutableStateOf("") }
    var currentWealth by remember { mutableStateOf("") }
    var monthlyExpenses by remember { mutableStateOf("") }
    var existingCredits by remember { mutableStateOf("") }
    var adults by remember { mutableStateOf("") }
    var children by remember { mutableStateOf("") }
    var desiredChildren by remember { mutableStateOf("") }

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
        if (uiState.futureMonthlyIncome != null && futureIncome.isBlank()) {
            futureIncome = uiState.futureMonthlyIncome.toString()
        }
        if (uiState.monthlyExpenses > 0 && monthlyExpenses.isBlank()) {
            monthlyExpenses = uiState.monthlyExpenses.toString()
        }
        if (uiState.currentEquity > 0 && currentWealth.isBlank()) {
            currentWealth = uiState.currentEquity.toString()
        }
        if (uiState.existingCredits > 0 && existingCredits.isBlank()) {
            existingCredits = uiState.existingCredits.toString()
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
        if (!uiState.targetDate.isNullOrBlank() && targetDate.isBlank()) {
            targetDate = uiState.targetDate!!
        }
        if (uiState.desiredChildren > 0 && desiredChildren.isBlank()) {
            desiredChildren = uiState.desiredChildren.toString()
        }
        if (uiState.numberOfAdults > 0 && adults.isBlank()) {
            adults = uiState.numberOfAdults.toString()
        }
        if (uiState.numberOfChildren > 0 && children.isBlank()) {
            children = uiState.numberOfChildren.toString()
        }
    }

    // Handle completion
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onComplete()
        }
    }

    // Selfie
    var selfieBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selfieBase64 by remember { mutableStateOf<String?>(null) }

    fun toDoubleSafe(s: String): Double? = s.replace(',', '.').toDoubleOrNull()
    fun toIntSafe(s: String): Int? = s.toIntOrNull()

    // Helper function to sync form fields to ViewModel
    fun syncFormToViewModel() {
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
    }

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
    val selfieValid by remember(selfieBytes) {
        mutableStateOf(true) // Selfie is always optional
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
            3 -> "Step 4 of $totalSteps · Selfie Verification"
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
                        // Optional: Allow skipping onboarding to jump straight to main dashboard
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = onSkip) { Text("Skip for now") }
                        }
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
                        TextFieldSimple(label = "Target date (e.g., 2026-12) [Optional]", value = targetDate, onValueChange = { targetDate = it })
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
                        NumberField(label = "Future income (per month) [Optional]", value = futureIncome, onValueChange = { futureIncome = it })
                        NumberField(label = "Current wealth (savings)", value = currentWealth, onValueChange = { currentWealth = it })
                        NumberField(label = "Monthly expenses", value = monthlyExpenses, onValueChange = { monthlyExpenses = it })
                        NumberField(label = "Existing credits (per month) [Optional]", value = existingCredits, onValueChange = { existingCredits = it })
                        HorizontalDivider()
                        SectionTitle("Household composition")
                        NumberField(label = "Adults", value = adults, onValueChange = { adults = it })
                        NumberField(label = "Children", value = children, onValueChange = { children = it })
                        NumberField(label = "Desired future children [Optional]", value = desiredChildren, onValueChange = { desiredChildren = it })
                    }
                }
            }
            3 -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SectionTitle("Your Selfie")
                        Text("Take a quick selfie to personalize your profile!")

                        // Display selected selfie
                        if (selfieBytes != null) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(CircleShape)
                            ) {
                                val imageBitmap = remember(selfieBytes) {
                                    selfieBytes?.let {
                                        byteArrayToImageBitmap(it)
                                    }
                                }

                                if (imageBitmap != null) {
                                    Image(
                                        bitmap = imageBitmap,
                                        contentDescription = "Your selfie",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("✓ Selfie captured!", color = MaterialTheme.colorScheme.primary)
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No photo yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Image picker button
                        ImagePicker(
                            onImageSelected = { bytes ->
                                selfieBytes = bytes
                                selfieBase64 = bytes?.let { ImageUtils.encodeImageToBase64(it) }
                                viewModel.updateSelfie(selfieBase64)
                                
                                // Generate avatar from selfie
                                selfieBase64?.let { base64 ->
                                    viewModel.generateAvatar(base64)
                                }
                            }
                        ) { pickImage ->
                            if (selfieBytes == null) {
                                Button(onClick = pickImage) {
                                    Text("Take Selfie / Choose Photo")
                                }
                            } else {
                                OutlinedButton(onClick = pickImage) {
                                    Text("Change Photo")
                                }
                            }
                        }

                        if (selfieBytes == null) {
                            Text(
                                "You can also skip this step and add it later",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Display avatar generation status and result
                        if (uiState.isGeneratingAvatar) {
                            Spacer(Modifier.height(16.dp))
                            CircularProgressIndicator()
                            Text("Generating your avatar...", style = MaterialTheme.typography.bodyMedium)
                        } else if (uiState.avatarImage != null) {
                            Spacer(Modifier.height(16.dp))
                            Text("Your AI-Generated Avatar", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                val avatarBitmap = remember(uiState.avatarImage) {
                                    uiState.avatarImage?.let {
                                        val decodedBytes = ImageUtils.decodeBase64ToImage(it)
                                        byteArrayToImageBitmap(decodedBytes)
                                    }
                                }
                                
                                if (avatarBitmap != null) {
                                    Image(
                                        bitmap = avatarBitmap,
                                        contentDescription = "AI-generated avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Text("✓ Avatar created!", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            else -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Summary", style = MaterialTheme.typography.titleLarge)
                        if (userName.isNotBlank()) Text("Thanks, $userName!")
                        Text("Target: $targetType, ${sizeSqm.ifBlank { "?" }} sqm in ${location.ifBlank { "?" }}")
                        if (targetDate.isNotBlank()) Text("Target date: $targetDate")
                        Text(
                            "Personal: age ${age.ifBlank { "?" }}, net income ${netIncome.ifBlank { "?" }}, wealth ${currentWealth.ifBlank { "?" }}, expenses ${monthlyExpenses.ifBlank { "?" }}"
                        )
                        if (futureIncome.isNotBlank()) Text("Future income: $futureIncome")
                        if (existingCredits.isNotBlank()) Text("Existing credits: $existingCredits")
                        Text("Household: ${adults.ifBlank { "?" }} adults, ${children.ifBlank { "?" }} children")
                        Text("Selfie: ${if (selfieBytes != null) "✓ Added" else "Not added"}")
                        if (desiredChildren.isNotBlank()) Text("Desired future children: $desiredChildren")
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
                Text("Back")
            }

            val canGoNext = when (currentStep) {
                0 -> greetingValid
                1 -> targetValid
                2 -> personalValid
                3 -> selfieValid
                else -> false
            }

            if (currentStep < 3) {
                Button(onClick = {
                    syncFormToViewModel()
                    viewModel.saveIntermediateProgress()
                    currentStep += 1
                }, enabled = canGoNext) { Text("Next") }
            } else if (currentStep == 3) {
                Button(onClick = {
                    syncFormToViewModel()
                    viewModel.saveIntermediateProgress()
                    currentStep = 4
                }, enabled = canGoNext) { Text("Review") }
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
                        Text("Submit")
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
                targetDate = ""
                age = ""
                netIncome = ""
                futureIncome = ""
                currentWealth = ""
                monthlyExpenses = ""
                existingCredits = ""
                adults = ""
                children = ""
                selfieBytes = null
                selfieBase64 = null
            }) {
                Text("Reset")
            }
        }

        // Proceed Later button - save progress and exit onboarding
        if (currentStep < 3) {
            Button(
                onClick = {
                    syncFormToViewModel()
                    viewModel.saveIntermediateProgress()
                    onSkip()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Proceed Later")
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
