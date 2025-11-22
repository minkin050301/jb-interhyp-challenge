package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.runtime.Composable

// Simplified implementation for iOS
actual class ImagePickerFactory {
    actual fun pickImage(onImagePicked: (ByteArray?) -> Unit) {
        // TODO: Implement iOS-specific image picking using UIImagePickerController
        println("Image picking not yet implemented for iOS")
        onImagePicked(null)
    }
}

@Composable
actual fun ImagePicker(
    onImageSelected: (ByteArray?) -> Unit,
    content: @Composable (pickImage: () -> Unit) -> Unit
) {
    val picker = ImagePickerFactory()
    content { picker.pickImage(onImageSelected) }
}

