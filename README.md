# OCR Example in Android

This project demonstrates how to implement Optical Character Recognition (OCR) in an Android application using the *ML Kit Text Recognition API* and *CameraX*. It serves as an example for developers looking to integrate OCR functionality into their Android apps. 

## Features

- Real-time text recognition using the device's camera.
- Detection and transformation of license plate text.
- Overlay of bounding boxes and recognized text on the camera preview.

## Implementation Details

1. Camera Integration
The app uses CameraX to capture live camera feed. The camera preview is displayed using PreviewView, and the ImageAnalyzer processes the frames for OCR.  
2. Text Recognition
The ML Kit Text Recognition API is used to detect text in the camera frames. The ImageAnalyzer class processes each frame and extracts text blocks matching a predefined regex.  
3. Coordinate Transformation
To ensure the detected text aligns correctly with the camera preview, the app uses CoordinateTransform and ImageProxyTransformFactory from CameraX. These classes handle the transformation of bounding box coordinates from the image buffer to the preview view.  
4. UI Overlay
The recognized text and bounding boxes are drawn on the camera preview using Jetpack Compose's Canvas. The bounding boxes are transformed to match the preview coordinates.

## Key Classes

[ImageAnalyzer](app/src/main/java/ch/soracel/ocr_android/ImageAnalyzer.kt)
- Analyzes camera frames for text using ML Kit.
- Filters text blocks matching the license plate regex.
- Passes detected plates to the OcrViewModel.

[OcrViewModel](app/src/main/java/ch/soracel/ocr_android/viewmodel/OcrViewModel.kt)
- Handles the transformation of raw plate coordinates to preview coordinates.
- Uses CoordinateTransform to map bounding boxes.
- Provides transformed plates to the UI via StateFlow.

[PreviewScreen](app/src/main/java/ch/soracel/ocr_android/ui/PreviewScreen.kt)
- Displays the camera feed using PreviewView.
- Draws bounding boxes and recognized text using Jetpack Compose.

## Dependencies
The project uses the following libraries:  
- CameraX for camera functionality.
- ML Kit for text recognition.
- Jetpack Compose for UI.
