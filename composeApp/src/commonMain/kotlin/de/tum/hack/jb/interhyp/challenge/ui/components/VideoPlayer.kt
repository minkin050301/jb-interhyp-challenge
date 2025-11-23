package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(modifier: Modifier, videoBytes: ByteArray)

