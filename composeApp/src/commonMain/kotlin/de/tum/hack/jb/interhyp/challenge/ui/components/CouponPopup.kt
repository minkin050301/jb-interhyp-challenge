package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.hack.jb.interhyp.challenge.domain.model.Coupon

@Composable
fun CouponPopup(
    coupon: Coupon,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Coupon Received!")
        },
        text = {
            Column {
                Text(
                    text = "Congratulations!",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You've reached ${coupon.unlockPercentage}% progress.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Here is your coupon worth:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "€${coupon.valueEuro}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Collect")
            }
        }
    )
}

