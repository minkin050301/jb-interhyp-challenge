package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun VideoPlayer(modifier: Modifier, videoBytes: ByteArray) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("Video playback not supported on Web/JS yet")
    }
}

