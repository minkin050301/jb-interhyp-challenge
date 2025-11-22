package de.tum.hack.jb.interhyp.challenge.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.IOException

actual class ImagePickerFactory {
    actual fun pickImage(onImagePicked: (ByteArray?) -> Unit) {
        // This should not be called directly on Android
        // Use the composable ImagePicker instead
    }
}

@Composable
actual fun ImagePicker(
    onImageSelected: (ByteArray?) -> Unit,
    content: @Composable (pickImage: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                onImageSelected(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
                onImageSelected(null)
            }
        } ?: onImageSelected(null)
    }
    
    content { launcher.launch("image/*") }
}

