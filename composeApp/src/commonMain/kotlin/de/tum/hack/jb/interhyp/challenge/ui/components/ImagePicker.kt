package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.runtime.Composable

/**
 * Common interface for image picking
 * Platform-specific implementations should be provided
 */
expect class ImagePickerFactory {
    /**
     * Launch image picker and return selected image as ByteArray
     */
    fun pickImage(onImagePicked: (ByteArray?) -> Unit)
}

/**
 * Composable wrapper for image picker
 */
@Composable
expect fun ImagePicker(
    onImageSelected: (ByteArray?) -> Unit,
    content: @Composable (pickImage: () -> Unit) -> Unit
)

