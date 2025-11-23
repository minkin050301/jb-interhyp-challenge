package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import jb_interhyp_challenge.composeapp.generated.resources.Res
import jb_interhyp_challenge.composeapp.generated.resources.click_to_select_date

/**
 * A date picker field component that shows a calendar dialog when clicked.
 * Displays the selected date in YYYY-MM format.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    // Parse the current value to milliseconds for the date picker
    val initialDateMillis = remember(value) {
        if (value.isNotBlank()) {
            try {
                val parts = value.split("-")
                if (parts.size >= 2) {
                    val year = parts[0].toIntOrNull() ?: 2025
                    val month = parts[1].toIntOrNull() ?: 1
                    // Create a simple timestamp (not exact, but good enough for month/year selection)
                    // Using kotlinx.datetime would be better but keeping it simple
                    val baseYear = 1970
                    val yearsFromBase = year - baseYear
                    val millisInYear = 365.25 * 24 * 60 * 60 * 1000L
                    val millisInMonth = millisInYear / 12
                    (yearsFromBase * millisInYear + (month - 1) * millisInMonth).toLong()
                } else null
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    ) {
        OutlinedTextField(
            value = value.ifBlank { stringResource(Res.string.click_to_select_date) },
            onValueChange = { }, // Read-only, changes happen through dialog
            label = { Text(label) },
            placeholder = { Text("YYYY-MM") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }

    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis
        )
        
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Convert milliseconds to YYYY-MM format
                            val date = millisToYearMonth(millis)
                            onValueChange(date)
                        }
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Converts milliseconds to YYYY-MM format string.
 */
private fun millisToYearMonth(millis: Long): String {
    // Simple conversion without kotlinx.datetime
    val baseYear = 1970
    val millisInYear = 365.25 * 24 * 60 * 60 * 1000L
    val millisInMonth = millisInYear / 12
    
    val yearsSinceBase = (millis / millisInYear).toInt()
    val remainingMillis = millis - (yearsSinceBase * millisInYear).toLong()
    val month = (remainingMillis / millisInMonth).toInt() + 1
    
    val year = baseYear + yearsSinceBase
    val monthStr = month.toString().padStart(2, '0')
    
    return "$year-$monthStr"
}
