import SwiftUI
import UIKit
import Foundation

/**
 * SwiftUI view that displays an image from a file path.
 * This bypasses Kotlin/Native iOS image decoding limitations.
 */
struct IosImageDisplayView: UIViewRepresentable {
    let imageFilePath: String?
    let onTap: () -> Void

    func makeUIView(context: Context) -> UIImageView {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        imageView.clipsToBounds = true
        imageView.isUserInteractionEnabled = true

        let tapGesture = UITapGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.handleTap))
        imageView.addGestureRecognizer(tapGesture)

        loadImage(into: imageView)
        return imageView
    }

    func updateUIView(_ uiImageView: UIImageView, context: Context) {
        loadImage(into: uiImageView)
    }

    private func loadImage(into imageView: UIImageView) {
        guard let filePath = imageFilePath else {
            imageView.image = nil
            return
        }

        let fileURL = URL(fileURLWithPath: filePath)
        if FileManager.default.fileExists(atPath: filePath) {
            imageView.image = UIImage(contentsOfFile: filePath)
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(onTap: onTap)
    }

    class Coordinator: NSObject {
        let onTap: () -> Void

        init(onTap: @escaping () -> Void) {
            self.onTap = onTap
        }

        @objc func handleTap() {
            onTap()
        }
    }
}

/**
 * Helper class to manage image file and trigger for SwiftUI display.
 * This is used by Kotlin to pass image data to SwiftUI for display.
 */
@objc public class IosImageDisplayHelper: NSObject {
    @objc public static let shared = IosImageDisplayHelper()

    private let imageFileName = "kmp_display_image.jpg"
    private let triggerFileName = "kmp_display_trigger"

    private override init() {
        super.init()
    }

    /**
     * Save image data to temp file and create trigger for SwiftUI to display it
     */
    @objc public func saveAndDisplayImage(_ imageData: Data?) {
        guard let data = imageData else { return }

        let tempDir = FileManager.default.temporaryDirectory
        let imagePath = tempDir.appendingPathComponent(imageFileName)
        let triggerPath = tempDir.appendingPathComponent(triggerFileName)

        do {
            try data.write(to: imagePath)
            // Create trigger file
            FileManager.default.createFile(atPath: triggerPath.path, contents: nil, attributes: nil)
        } catch {
            print("Failed to save image for display: \(error)")
        }
    }

    /**
     * Get the current image file path
     */
    @objc public func getImagePath() -> String? {
        let tempDir = FileManager.default.temporaryDirectory
        let imagePath = tempDir.appendingPathComponent(imageFileName)

        if FileManager.default.fileExists(atPath: imagePath.path) {
            return imagePath.path
        }
        return nil
    }

    /**
     * Clear the display image
     */
    @objc public func clearImage() {
        let tempDir = FileManager.default.temporaryDirectory
        let imagePath = tempDir.appendingPathComponent(imageFileName)
        let triggerPath = tempDir.appendingPathComponent(triggerFileName)

        try? FileManager.default.removeItem(at: imagePath)
        try? FileManager.default.removeItem(at: triggerPath)
    }
}