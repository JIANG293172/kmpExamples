package shared.imageprocessing

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTimer
import platform.Foundation.NSTemporaryDirectory
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy
import platform.Foundation.NSThread

/**
 * iOS 照片库访问器 - 使用文件轮询机制
 */
@OptIn(ExperimentalForeignApi::class)
object IosPhotoLibraryAccess {
    private const val RESULT_FILE = "kmp_photo_result.jpg"
    private const val TRIGGER_FILE = "kmp_photo_trigger"
    private const val POLL_INTERVAL_SEC: Double = 0.5
    private const val MAX_POLL_ATTEMPTS = 120 // 60 seconds timeout

    // Callback holder for Swift to call
    private var pendingCallback: ((ByteArray?) -> Unit)? = null
    private var pollTimer: NSTimer? = null
    private var pollAttempts = 0

    /**
     * 打开照片选择器 - 创建触发文件并轮询结果
     */
    fun presentPicker(onResult: (ByteArray?) -> Unit) {
        pendingCallback = onResult
        pollAttempts = 0
        createTriggerFile()
        startPolling()
    }

    private fun createTriggerFile() {
        dispatch_async(dispatch_get_main_queue()) {
            val filePath = "${NSTemporaryDirectory()}$TRIGGER_FILE"
            val fileManager = NSFileManager.defaultManager

            try {
                // Delete existing trigger file first
                if (fileManager.fileExistsAtPath(filePath)) {
                    fileManager.removeItemAtPath(filePath, null)
                }
                // Create empty file as trigger
                fileManager.createFileAtPath(filePath, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startPolling() {
        dispatch_async(dispatch_get_main_queue()) {
            pollTimer = NSTimer.scheduledTimerWithTimeInterval(
                POLL_INTERVAL_SEC,
                repeats = true
            ) {
                checkForResult()
            }
        }
    }

    private fun checkForResult() {
        if (pendingCallback == null) {
            pollTimer?.invalidate()
            pollTimer = null
            return
        }

        val resultPath = "${NSTemporaryDirectory()}$RESULT_FILE"
        val fileManager = NSFileManager.defaultManager

        if (fileManager.fileExistsAtPath(resultPath)) {
            // Result file exists - read it
            val bytes = readFileBytes(resultPath)

            // Delete result file after reading
            try {
                fileManager.removeItemAtPath(resultPath, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            pollTimer?.invalidate()
            pollTimer = null
            pendingCallback?.invoke(bytes)
            pendingCallback = null
        } else if (pollAttempts >= MAX_POLL_ATTEMPTS) {
            // Timeout
            pollTimer?.invalidate()
            pollTimer = null
            pendingCallback?.invoke(null)
            pendingCallback = null
        }
        pollAttempts++
    }

    private fun readFileBytes(path: String): ByteArray? {
        val fileManager = NSFileManager.defaultManager
        val nsData = fileManager.contentsAtPath(path)
            ?: return null

        val length = nsData.length.toInt()
        if (length == 0) return null

        val bytes = ByteArray(length)
        memcpy(bytes.refTo(0), nsData.bytes, length.toULong())
        return bytes
    }
}