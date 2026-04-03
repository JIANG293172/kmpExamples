package shared.imageprocessing

actual object ImagePickerHelper {

    actual fun pickImage(onResult: (ByteArray?) -> Unit) {
        // iOS实现需要在原生代码中处理
        onResult(null)
    }
}