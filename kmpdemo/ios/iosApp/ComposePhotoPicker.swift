import UIKit
import PhotosUI
import Foundation

// File-based trigger for Kotlin communication
let kmpTriggerFile = "kmp_photo_trigger"
let kmpResultFile = "kmp_photo_result.jpg"

@objc public class ComposePhotoPicker: NSObject, PHPickerViewControllerDelegate, UIImagePickerControllerDelegate {

    @objc public static let shared = ComposePhotoPicker()

    private var isPickerPresented = false
    private var checkTimer: Timer?
    private var pendingResolve: ((Data?) -> Void)?

    private override init() {
        super.init()
    }

    @objc public func startMonitoring() {
        // Check for trigger file every 0.5 seconds
        checkTimer = Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true) { [weak self] _ in
            self?.checkForTrigger()
        }
    }

    @objc public func stopMonitoring() {
        checkTimer?.invalidate()
        checkTimer = nil
    }

    private func checkForTrigger() {
        let tempDir = FileManager.default.temporaryDirectory
        let triggerPath = tempDir.appendingPathComponent(kmpTriggerFile)

        if FileManager.default.fileExists(atPath: triggerPath.path) {
            // Trigger found, show picker
            showPicker()

            // Delete trigger file
            try? FileManager.default.removeItem(at: triggerPath)
        }
    }

    @objc public func showPicker() {
        guard !isPickerPresented else { return }
        isPickerPresented = true

        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }

            guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                  let window = windowScene.windows.first(where: { $0.isKeyWindow }),
                  var topVC = window.rootViewController else {
                self.isPickerPresented = false
                return
            }

            while let presented = topVC.presentedViewController {
                topVC = presented
            }

            if #available(iOS 14.0, *) {
                var config = PHPickerConfiguration()
                config.selectionLimit = 1
                config.filter = .images

                let picker = PHPickerViewController(configuration: config)
                picker.delegate = self
                topVC.present(picker, animated: true)
            } else {
                let picker = UIImagePickerController()
                picker.sourceType = .photoLibrary
                picker.delegate = self
                topVC.present(picker, animated: true)
            }
        }
    }

    private func saveImageToResult(_ data: Data?) {
        guard let imageData = data else { return }

        let tempDir = FileManager.default.temporaryDirectory
        let resultPath = tempDir.appendingPathComponent(kmpResultFile)

        do {
            try imageData.write(to: resultPath)
        } catch {
            print("Failed to save image: \(error)")
        }
    }

    // MARK: - PHPickerViewControllerDelegate

    @available(iOS 14.0, *)
    public func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)
        isPickerPresented = false

        guard let result = results.first else {
            saveImageToResult(nil)
            return
        }

        let itemProvider = result.itemProvider

        if itemProvider.canLoadObject(ofClass: UIImage.self) {
            itemProvider.loadObject(ofClass: UIImage.self) { [weak self] object, error in
                DispatchQueue.main.async {
                    if let image = object as? UIImage,
                       let data = image.jpegData(compressionQuality: 0.9) {
                        self?.saveImageToResult(data)
                    }
                }
            }
        }
    }

    // MARK: - UIImagePickerControllerDelegate

    public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
        picker.dismiss(animated: true)
        isPickerPresented = false

        if let image = info[.originalImage] as? UIImage,
           let data = image.jpegData(compressionQuality: 0.9) {
            saveImageToResult(data)
        }
    }

    public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true)
        isPickerPresented = false
    }
}

// AppDelegate integration
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        // Start monitoring for photo picker trigger
        ComposePhotoPicker.shared.startMonitoring()
        return true
    }
}