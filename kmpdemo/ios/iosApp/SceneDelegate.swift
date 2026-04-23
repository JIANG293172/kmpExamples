import UIKit
import PhotosUI
import SwiftUI
import shared

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?
    private var photoPickerTimer: Timer?
    private var isPickerPresented = false

    // File paths for Kotlin communication
    private let triggerFile = "kmp_photo_trigger"
    private let resultFile = "kmp_photo_result.jpg"

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = (scene as? UIWindowScene) else { return }

        let window = UIWindow(windowScene: windowScene)
        let rootViewController = IosMainKt.MainViewController()
        window.rootViewController = rootViewController
        self.window = window
        window.makeKeyAndVisible()
    }

    func sceneDidBecomeActive(_ scene: UIScene) {
        // Start photo picker monitoring
        startPhotoPickerMonitoring()
    }

    func sceneWillResignActive(_ scene: UIScene) {
        // Stop monitoring when app goes to background
        stopPhotoPickerMonitoring()
    }

    private func startPhotoPickerMonitoring() {
        // Check for trigger file every 0.5 seconds
        photoPickerTimer = Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true) { [weak self] _ in
            self?.checkForPhotoPickerTrigger()
        }
    }

    private func stopPhotoPickerMonitoring() {
        photoPickerTimer?.invalidate()
        photoPickerTimer = nil
    }

    private func checkForPhotoPickerTrigger() {
        let tempDir = FileManager.default.temporaryDirectory
        let triggerPath = tempDir.appendingPathComponent(triggerFile)

        if FileManager.default.fileExists(atPath: triggerPath.path) {
            // Trigger found, delete it and show picker
            try? FileManager.default.removeItem(at: triggerPath)
            showPhotoPicker()
        }
    }

    private func showPhotoPicker() {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = windowScene.windows.first(where: { $0.isKeyWindow }),
              var topVC = window.rootViewController else {
            return
        }

        // Navigate to topmost presented VC
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

    private func saveImageToResult(_ data: Data?) {
        guard let imageData = data else { return }

        let tempDir = FileManager.default.temporaryDirectory
        let resultPath = tempDir.appendingPathComponent(resultFile)

        do {
            try imageData.write(to: resultPath)
        } catch {
            print("Failed to save image: \(error)")
        }
    }
}

// MARK: - PHPickerViewControllerDelegate
@available(iOS 14.0, *)
extension SceneDelegate: PHPickerViewControllerDelegate {
    func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)

        guard let result = results.first else {
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
}

// MARK: - UIImagePickerControllerDelegate
extension SceneDelegate: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
        picker.dismiss(animated: true)
        isPickerPresented = false

        if let image = info[.originalImage] as? UIImage,
           let data = image.jpegData(compressionQuality: 0.9) {
            saveImageToResult(data)
        }
    }

    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true)
        isPickerPresented = false
    }
}
