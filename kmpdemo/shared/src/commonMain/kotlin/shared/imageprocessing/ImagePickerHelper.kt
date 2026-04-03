package shared.imageprocessing

/**
 * 图片选择辅助接口 - KMP expect声明
 */
expect object ImagePickerHelper {

    /**
     * 打开相册选择图片
     * @param onResult 回调，传入选中的图片字节数组，null表示取消
     */
    fun pickImage(onResult: (ByteArray?) -> Unit)
}