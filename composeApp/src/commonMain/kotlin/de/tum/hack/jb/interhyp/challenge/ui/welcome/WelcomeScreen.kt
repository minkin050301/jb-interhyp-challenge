package de.tum.hack.jb.interhyp.challenge.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jb_interhyp_challenge.composeapp.generated.resources.Res
import jb_interhyp_challenge.composeapp.generated.resources.image
import org.jetbrains.compose.resources.painterResource

@Composable
fun WelcomeScreen(
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.image),
                contentDescription = "DreamBuilder Logo",
                modifier = Modifier.size(200.dp)
            )

            Text(
                text = "DreamBuilder",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Button(onClick = onStart) {
                Text("Start")
            }
        }
    }
}
