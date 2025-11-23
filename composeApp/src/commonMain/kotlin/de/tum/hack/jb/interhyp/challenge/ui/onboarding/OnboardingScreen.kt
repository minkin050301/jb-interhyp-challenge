package de.tum.hack.jb.interhyp.challenge.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils
import de.tum.hack.jb.interhyp.challenge.util.getFocusManager
import de.tum.hack.jb.interhyp.challenge.ui.components.ImagePicker
import de.tum.hack.jb.interhyp.challenge.ui.components.DatePickerField
import org.jetbrains.skia.Image as SkiaImage
import de.tum.hack.jb.interhyp.challenge.ui.util.byteArrayToImageBitmap
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyType
import de.tum.hack.jb.interhyp.challenge.presentation.onboarding.OnboardingViewModel
import de.tum.hack.jb.interhyp.challenge.ui.goal.GoalSelectionScreen
import org.koin.compose.koinInject

@Composable
fun OnboardingScreen(
    onSkip: () -> Unit = {},
    onComplete: () -> Unit = {},
    viewModel: OnboardingViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Steps: 0 = Greeting, 1 = Target, 2 = Goal Selection, 3 = Personal, 4 = Selfie, 5 = Summary
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 5 // excluding summary presentation

    // Local UI state for form fields (strings for text input)
    var userName by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf("House") } // "House" or "Apartment"
    var sizeSqm by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var netIncome by remember { mutableStateOf("") } // monthly
    var yearlyIncomeIncrease by remember { mutableStateOf(3f) } // percentage 0-7%
    var currentWealth by remember { mutableStateOf("") }
    var monthlyExpenses by remember { mutableStateOf("") }
    var existingCredits by remember { mutableStateOf("") }
    var adults by remember { mutableStateOf("1") }
    var children by remember { mutableStateOf("0") }
    var desiredChildren by remember { mutableStateOf("0") }

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
        // yearlyIncomeIncrease is managed by slider with default value
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
        // Calculate future monthly income from yearly increase percentage
        val currentIncome = toDoubleSafe(netIncome)
        val futureMonthlyIncome = if (currentIncome != null && currentIncome > 0) {
            currentIncome * (1 + yearlyIncomeIncrease / 100.0)
        } else null
        viewModel.updateFutureMonthlyIncome(futureMonthlyIncome)
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
    val focusManager = getFocusManager()

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
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
            "Home Savings Setup",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        val stepLabel = when (currentStep) {
            0 -> "Step 1 of $totalSteps · Welcome"
            1 -> "Step 2 of $totalSteps · Your Target"
            2 -> "Step 3 of $totalSteps · Select Your Dream Home"
            3 -> "Step 4 of $totalSteps · About You"
            4 -> "Step 5 of $totalSteps · Selfie Verification"
            else -> "Summary"
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
                        LocationDropdown(value = location, onValueChange = { location = it })
                        DatePickerField(label = "Target date [Optional]", value = targetDate, onValueChange = { targetDate = it })
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
                        SectionTitle("About You")
                        if (userName.isNotBlank()) Text("Hi $userName!")
                        NumberField(label = "Age", value = age, onValueChange = { age = it })
                        NumberField(label = "Net income (per month)", value = netIncome, onValueChange = { netIncome = it })
                        
                        // Yearly income increase slider
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Future income (yearly increase) [Optional]",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "${(yearlyIncomeIncrease * 10).toInt() / 10.0}% per year",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Slider(
                                value = yearlyIncomeIncrease,
                                onValueChange = { yearlyIncomeIncrease = it },
                                valueRange = 0f..7f,
                                steps = 69, // 0.1% increments: (7-0)/0.1 - 1 = 69
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        NumberField(label = "Current wealth (savings)", value = currentWealth, onValueChange = { currentWealth = it })
                        NumberField(label = "Monthly expenses", value = monthlyExpenses, onValueChange = { monthlyExpenses = it })
                        NumberField(label = "Existing credits (per month) [Optional]", value = existingCredits, onValueChange = { existingCredits = it })
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(4.dp))
                        SectionTitle("Household composition")
                        NumberDropdown(
                            label = "Adults",
                            value = adults,
                            onValueChange = { adults = it },
                            options = listOf(1, 2, 3, 4, 5)
                        )
                        NumberDropdown(
                            label = "Children",
                            value = children,
                            onValueChange = { children = it },
                            options = listOf(0, 1, 2, 3, 4, 5, 6)
                        )
                        NumberDropdown(
                            label = "Desired future children [Optional]",
                            value = desiredChildren,
                            onValueChange = { desiredChildren = it },
                            options = listOf(0, 1, 2, 3, 4)
                        )
                    }
                }
            }
            4 -> {
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
                        Text("Yearly income increase: ${(yearlyIncomeIncrease * 10).toInt() / 10.0}%")
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
                2 -> true // Goal selection handles its own navigation
                3 -> personalValid
                4 -> selfieValid
                else -> false
            }

            if (currentStep < 2) {
                Button(onClick = {
                    syncFormToViewModel()
                    viewModel.saveIntermediateProgress()
                    currentStep += 1
                }, enabled = canGoNext) { Text("Next") }
            } else if (currentStep == 2) {
                // Goal selection handles its own continue button
                // No button needed here
            } else if (currentStep < 4) {
                Button(onClick = {
                    syncFormToViewModel()
                    viewModel.saveIntermediateProgress()
                    currentStep += 1
                }, enabled = canGoNext) { Text("Next") }
            } else if (currentStep == 4) {
                Button(onClick = {
                    syncFormToViewModel()
                    viewModel.saveIntermediateProgress()
                    currentStep = 5 // Move to summary
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
                yearlyIncomeIncrease = 3f
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
        if (currentStep < 5) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberDropdown(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<Int>,
    modifier: Modifier = Modifier
) {
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
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        onValueChange(option.toString())
                        expanded = false
                    }
                )
            }
        }
    }
}
