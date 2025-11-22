package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.readBytes
import platform.Foundation.NSData
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerCameraDevice
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
actual class ImagePickerFactory {
    private var currentCallback: ((ByteArray?) -> Unit)? = null
    private var pickerController: UIImagePickerController? = null
    private var delegate: ImagePickerDelegate? = null

    actual fun pickImage(onImagePicked: (ByteArray?) -> Unit) {
        currentCallback = onImagePicked
        
        // Get the root view controller
        val rootViewController = getRootViewController() ?: run {
            println("Could not find root view controller")
            onImagePicked(null)
            return
        }
        
        // Try camera first, then photo library
        val sourceType = if (UIImagePickerController.isSourceTypeAvailable(
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            )) {
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        } else if (UIImagePickerController.isSourceTypeAvailable(
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            )) {
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        } else {
            onImagePicked(null)
            return
        }
        
        showImagePicker(sourceType, rootViewController)
    }
    
    private fun showImagePicker(sourceType: UIImagePickerControllerSourceType, viewController: UIViewController) {
        val picker = UIImagePickerController()
        picker.sourceType = sourceType
        picker.allowsEditing = true
        picker.mediaTypes = listOf("public.image")
        
        // Set camera device to front camera if available
        if (sourceType == UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera) {
            if (UIImagePickerController.isCameraDeviceAvailable(
                    UIImagePickerControllerCameraDevice.UIImagePickerControllerCameraDeviceFront
                )) {
                picker.cameraDevice = UIImagePickerControllerCameraDevice.UIImagePickerControllerCameraDeviceFront
            }
        }
        
        // Create delegate
        val delegate = ImagePickerDelegate { image ->
            viewController.dismissViewControllerAnimated(
                flag = true,
                completion = null
            )
            val bytes = image?.let { convertUIImageToByteArray(it) }
            currentCallback?.invoke(bytes)
            currentCallback = null
            this.delegate = null
            pickerController = null
        }
        
        picker.delegate = delegate
        this.delegate = delegate
        this.pickerController = picker
        
        viewController.presentViewController(
            viewControllerToPresent = picker,
            animated = true,
            completion = null
        )
    }
    
    private fun getRootViewController(): UIViewController? {
        // Try to get the key window first
        val keyWindow = UIApplication.sharedApplication.keyWindow
        if (keyWindow != null) {
            return keyWindow.rootViewController
        }
        
        // Fallback: try connected scenes - simplified approach
        // Just return null if keyWindow is not available
        // The keyWindow should be available in most cases
        
        return null
    }
    
    private fun convertUIImageToByteArray(image: UIImage): ByteArray? {
        return try {
            autoreleasepool {
                // Use UIImageJPEGRepresentation function from UIKit
                val imageData = platform.UIKit.UIImageJPEGRepresentation(image, 0.8)
                if (imageData != null) {
                    convertNSDataToByteArray(imageData)
                } else {
                    // Fallback to PNG
                    val pngData = platform.UIKit.UIImagePNGRepresentation(image)
                    pngData?.let { convertNSDataToByteArray(it) }
                }
            }
        } catch (e: Exception) {
            println("Error converting UIImage to ByteArray: ${e.message}")
            null
        }
    }
    
    private fun convertNSDataToByteArray(data: NSData): ByteArray {
        val length = data.length.toInt()
        val bytesPtr = data.bytes
        return bytesPtr?.readBytes(length) ?: ByteArray(0)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class ImagePickerDelegate(
    private val onImagePicked: (UIImage?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val image = didFinishPickingMediaWithInfo[platform.UIKit.UIImagePickerControllerEditedImage] as? UIImage
            ?: (didFinishPickingMediaWithInfo[platform.UIKit.UIImagePickerControllerOriginalImage] as? UIImage)
        onImagePicked(image)
    }
    
    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        onImagePicked(null)
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
