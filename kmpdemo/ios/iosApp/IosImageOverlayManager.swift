import SwiftUI
import UIKit

/**
 * Manager class that handles displaying selected images directly from Swift side.
 * This bypasses Kotlin/Native iOS image decoding limitations.
 */
@objc public class IosImageOverlayManager: NSObject {
    @objc public static let shared = IosImageOverlayManager()

    private let resultFileName = "kmp_photo_result.jpg"

    private override init() {
        super.init()
    }

    /**
     * Get the current result image path if it exists
     */
    @objc public func getResultImagePath() -> String? {
        let tempDir = FileManager.default.temporaryDirectory
        let resultPath = tempDir.appendingPathComponent(resultFileName)

        if FileManager.default.fileExists(atPath: resultPath.path) {
            return resultPath.path
        }
        return nil
    }

    /**
     * Check if result image exists
     */
    @objc public func hasResultImage() -> Bool {
        return getResultImagePath() != nil
    }
}

/**
 * SwiftUI view that displays the selected photo directly from file.
 * This is used as an overlay on top of Compose UI.
 */
struct ImageOverlayView: View {
    let onTap: () -> Void

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                // Try to load and display the image from file
                if let imagePath = IosImageOverlayManager.shared.getResultImagePath(),
                   let uiImage = UIImage(contentsOfFile: imagePath) {
                    Image(uiImage: uiImage)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .onTapGesture {
                            onTap()
                        }
                } else {
                    // No image yet - show placeholder
                    Color.clear
                        .onTapGesture {
                            onTap()
                        }
                }
            }
        }
    }
}