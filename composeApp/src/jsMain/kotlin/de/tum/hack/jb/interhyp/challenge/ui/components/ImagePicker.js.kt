package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get

actual class ImagePickerFactory {
    actual fun pickImage(onImagePicked: (ByteArray?) -> Unit) {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"
        
        input.onchange = {
            val file = input.files?.get(0)
            if (file != null) {
                val reader = FileReader()
                reader.onload = { event ->
                    val arrayBuffer = reader.result as? org.khronos.webgl.ArrayBuffer
                    if (arrayBuffer != null) {
                        val byteArray = org.khronos.webgl.Int8Array(arrayBuffer).unsafeCast<ByteArray>()
                        onImagePicked(byteArray)
                    } else {
                        onImagePicked(null)
                    }
                }
                reader.readAsArrayBuffer(file)
            } else {
                onImagePicked(null)
            }
        }
        
        input.click()
    }
}

@Composable
actual fun ImagePicker(
    onImageSelected: (ByteArray?) -> Unit,
    content: @Composable (pickImage: () -> Unit) -> Unit
) {
    val picker = remember { ImagePickerFactory() }
    content { picker.pickImage(onImageSelected) }
}

