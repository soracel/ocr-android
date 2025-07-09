package ch.soracel.ocr_android.ui

import android.graphics.Paint
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.view.TransformExperimental
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import ch.soracel.ocr_android.ImageAnalyzer
import ch.soracel.ocr_android.viewmodel.OcrViewModel
import java.util.concurrent.Executors

@OptIn(TransformExperimental::class)
@Composable
fun PreviewScreen(viewModel: OcrViewModel) {
    // Change this regex to match your specific formats
    val licencePlateRegex =
        Regex("\\b([A-Z]{1,3}-?[A-Z]{1,2} ?\\d{1,4}|[A-Z]{2} ?\\d{2,4} ?[A-Z]{1,2}|[A-Z]{2}-?[A-Z]{1,2} ?\\d{1,3}|[A-Z]{2} ?\\d{1,6}|[A-Z]{1,3}-\\d{1,5}[A-Z]{1,2}|FL ?\\d{1,5})\\b")

    val lifecycleOwner = LocalLifecycleOwner.current
    val transformedRecognitions by viewModel.transformedRecognitions.collectAsState()
    val executor = remember { Executors.newSingleThreadExecutor() }

    var previewViewRef: PreviewView? by remember { mutableStateOf(null) }

    Box(Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewViewRef = this

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().apply {
                            setSurfaceProvider(surfaceProvider)
                        }

                        val analyzer = ImageAnalyzer(
                            regex = licencePlateRegex
                        ) { results, imageProxy ->
                            previewViewRef?.let { pv ->
                                viewModel.setRawRecognitions(imageProxy, results, pv)
                            } ?: imageProxy.close()
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build().apply {
                                setAnalyzer(executor, analyzer)
                            }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                }
            }
        )

        // Bounding Boxes Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            transformedRecognitions.forEach { recognition ->
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(recognition.rect.left, recognition.rect.top),
                    size = Size(recognition.rect.width(), recognition.rect.height()),
                    style = Stroke(width = 3f)
                )
                drawContext.canvas.nativeCanvas.drawText(
                    recognition.text,
                    recognition.rect.left,
                    recognition.rect.top - 8f,
                    Paint().apply {
                        color = android.graphics.Color.RED
                        textSize = 36f
                        isAntiAlias = true
                        style = Paint.Style.FILL
                    }
                )
            }
        }
    }
}