import SwiftUI
import UIKit

/**
 * SwiftUI view that displays an image from ByteArray data passed from Kotlin.
 * This bypasses the need for Kotlin/Native image decoding which has limited iOS interop.
 */
@objc public class KotlinImageView: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {

    @objc public static let shared = KotlinImageView()

    private var onImageData: ((Data?) -> Void)?
    private var imageViewController: UIViewController?

    private override init() {
        super.init()
    }

    /**
     * Show photo picker and return selected image data via callback
     */
    @objc public func pickImage(onImageData: @escaping (Data?) -> Void) {
        self.onImageData = onImageData

        DispatchQueue.main.async {
            guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                  let window = windowScene.windows.first(where: { $0.isKeyWindow }),
                  var topVC = window.rootViewController else {
                onImageData(nil)
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
                self.imageViewController = picker
            } else {
                let picker = UIImagePickerController()
                picker.sourceType = .photoLibrary
                picker.delegate = self
                topVC.present(picker, animated: true)
                self.imageViewController = picker
            }
        }
    }

    // MARK: - PHPickerViewControllerDelegate

    @available(iOS 14.0, *)
    public func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)

        guard let result = results.first else {
            onImageData?(nil)
            return
        }

        let itemProvider = result.itemProvider

        if itemProvider.canLoadObject(ofClass: UIImage.self) {
            itemProvider.loadObject(ofClass: UIImage.self) { [weak self] object, error in
                DispatchQueue.main.async {
                    if let image = object as? UIImage,
                       let data = image.jpegData(compressionQuality: 0.9) {
                        self?.onImageData?(data)
                    } else {
                        self?.onImageData?(nil)
                    }
                }
            }
        } else {
            onImageData?(nil)
        }
    }

    // MARK: - UIImagePickerControllerDelegate

    public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
        picker.dismiss(animated: true)

        if let image = info[.originalImage] as? UIImage,
           let data = image.jpegData(compressionQuality: 0.9) {
            onImageData?(data)
        } else {
            onImageData?(nil)
        }
    }

    public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true)
        onImageData?(nil)
    }
}