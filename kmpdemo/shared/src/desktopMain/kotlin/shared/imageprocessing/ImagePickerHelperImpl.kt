package shared.imageprocessing

actual object ImagePickerHelper {

    fun pickImage(onResult: (ByteArray?) -> Unit) {
        // Desktop实现需要在原生代码中处理
        onResult(null)
    }
}