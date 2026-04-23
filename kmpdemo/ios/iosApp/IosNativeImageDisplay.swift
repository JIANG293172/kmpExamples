import SwiftUI
import UIKit
import PhotosUI

/**
 * SwiftUI view that displays the selected image directly using UIKit.
 * This bypasses Kotlin/Native iOS image decoding limitations.
 */
struct IosNativeImageDisplay: View {
    let imageData: Data?
    let onTap: () -> Void

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                if let imageData = imageData,
                   let uiImage = UIImage(data: imageData) {
                    Image(uiImage: uiImage)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .onTapGesture {
                            onTap()
                        }
                } else {
                    // Show placeholder or error
                    VStack {
                        Image(systemName: "photo")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 64, height: 64)
                            .foregroundColor(.gray)
                        Text("点击选择照片")
                            .foregroundColor(.gray)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .onTapGesture {
                        onTap()
                    }
                }
            }
        }
    }
}

/**
 * UIViewControllerRepresentable for PHPickerViewController
 */
struct PhotoPicker: UIViewControllerRepresentable {
    let onImagePicked: (Data?) -> Void

    func makeUIViewController(context: Context) -> PHPickerViewController {
        var config = PHPickerConfiguration()
        config.selectionLimit = 1
        config.filter = .images

        let picker = PHPickerViewController(configuration: config)
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: PHPickerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(onImagePicked: onImagePicked)
    }

    class Coordinator: NSObject, PHPickerViewControllerDelegate {
        let onImagePicked: (Data?) -> Void

        init(onImagePicked: @escaping (Data?) -> Void) {
            self.onImagePicked = onImagePicked
        }

        func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
            picker.dismiss(animated: true)

            guard let result = results.first else {
                onImagePicked(nil)
                return
            }

            let itemProvider = result.itemProvider

            if itemProvider.canLoadObject(ofClass: UIImage.self) {
                itemProvider.loadObject(ofClass: UIImage.self) { object, error in
                    DispatchQueue.main.async {
                        if let image = object as? UIImage,
                           let data = image.jpegData(compressionQuality: 0.9) {
                            self.onImagePicked(data)
                        } else {
                            self.onImagePicked(nil)
                        }
                    }
                }
            } else {
                onImagePicked(nil)
            }
        }
    }
}