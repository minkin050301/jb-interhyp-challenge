package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * Camera capture component for taking selfies
 * Shows a camera preview area and captured image preview
 */
@Composable
fun CameraCapture(
    onImageCaptured: (ByteArray?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var capturedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Take Selfie",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onDismiss) {
                Text("✕", style = MaterialTheme.typography.titleLarge)
            }
        }
        
        // Camera Preview / Captured Image Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                capturedImage != null -> {
                    // Show captured image
                    Image(
                        bitmap = capturedImage!!,
                        contentDescription = "Captured selfie",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
                isCapturing -> {
                    // Show camera preview simulation
                    CameraPreviewSimulation()
                }
                else -> {
                    // Show placeholder
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "📷",
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            "Camera preview will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // Instructions
        Text(
            text = when {
                capturedImage != null -> "✓ Photo captured! You can retake or use this photo."
                isCapturing -> "Position your face in the frame and tap the capture button."
                else -> "Tap 'Open Camera' to start taking your selfie."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Action Buttons
        if (capturedImage != null) {
            // Show options after capture
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        capturedImage = null
                        isCapturing = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retake")
                }
                Button(
                    onClick = {
                        // In a real implementation, convert ImageBitmap to ByteArray
                        // For now, return a mock byte array
                        onImageCaptured(ByteArray(0))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Use Photo")
                }
            }
        } else if (isCapturing) {
            // Show capture button
            Button(
                onClick = {
                    // Simulate capturing the image
                    capturedImage = generateMockSelfieImage()
                    isCapturing = false
                },
                modifier = Modifier
                    .size(72.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "📸",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        } else {
            // Show open camera button
            Button(
                onClick = {
                    isCapturing = true
                    // In a real app, this would request camera permissions and open camera
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📷 Open Camera")
            }
        }
    }
}

/**
 * Simulates a camera preview with a live indicator
 */
@Composable
private fun CameraPreviewSimulation() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D2D2D))
    ) {
        // Simulated camera preview
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "👤",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(16.dp)
            )
            Text(
                "Camera Preview",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        // Live indicator
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.Red, CircleShape)
            )
            Text(
                "LIVE",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/**
 * Generates a mock selfie image for demonstration
 * In a real implementation, this would be replaced with actual camera capture
 */
private fun generateMockSelfieImage(): ImageBitmap {
    // Create a simple colored bitmap as placeholder
    // In a real app, this would be the actual captured image from camera
    return ImageBitmap(300, 400)
}

