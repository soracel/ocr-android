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
    val exampleRegexLicencePlate = Regex(
        "\\b([A-Z]{2})[·:.\\s\\r\\n]?(\\d{1,6})\\b"
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    val plates by viewModel.transformedRecognitions.collectAsState()
    val executor = remember { Executors.newSingleThreadExecutor() }

    Box(Modifier.fillMaxSize()) {
        var previewViewRef: PreviewView? by remember { mutableStateOf(null) }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    previewViewRef = previewView

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().apply {
                            surfaceProvider = previewView.surfaceProvider
                        }

                        val analyzer = ImageAnalyzer(
                            regex = exampleRegexLicencePlate
                        ) { recognitions, imageProxy ->
                            viewModel.setRawRecognitions(recognitions, imageProxy, previewView)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build().also {
                                it.setAnalyzer(executor, analyzer)
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                }
            }
        )

        // Bounding Box Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            plates.forEach { plate ->
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(plate.rect.left, plate.rect.top),
                    size = Size(plate.rect.width(), plate.rect.height()),
                    style = Stroke(width = 3f)
                )

                drawContext.canvas.nativeCanvas.drawText(
                    plate.text,
                    plate.rect.left,
                    plate.rect.top - 10f,
                    Paint().apply {
                        color = android.graphics.Color.RED
                        textSize = 36f
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }
                )
            }
        }
    }
}