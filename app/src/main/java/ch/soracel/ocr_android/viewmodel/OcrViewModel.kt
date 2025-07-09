package ch.soracel.ocr_android.viewmodel

import android.graphics.RectF
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.CoordinateTransform
import androidx.camera.view.transform.ImageProxyTransformFactory
import androidx.lifecycle.ViewModel
import ch.soracel.ocr_android.model.RawRecognition
import ch.soracel.ocr_android.model.TransformedRecognition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@TransformExperimental
class OcrViewModel : ViewModel() {
    private val _rawRecognitions = MutableStateFlow<List<RawRecognition>>(emptyList())
    private val _transformedRecognitions =
        MutableStateFlow<List<TransformedRecognition>>(emptyList())
    val transformedRecognitions: StateFlow<List<TransformedRecognition>> = _transformedRecognitions

    fun setRawRecognitions(
        recognitions: List<RawRecognition>,
        imageProxy: ImageProxy,
        previewView: PreviewView,
    ) {
        _rawRecognitions.value = recognitions

        val transformFactory = ImageProxyTransformFactory().apply {
            isUsingRotationDegrees = true
        }

        val source = transformFactory.getOutputTransform(imageProxy)
        val target = previewView.outputTransform

        if (target == null) {
            return
        }

        val coordinateTransform = CoordinateTransform(source, target)

        val transformedRecognitions = recognitions.map {
            val rectF = RectF(it.rect)
            coordinateTransform.mapRect(rectF)
            TransformedRecognition(it.text, rectF)
        }

        _transformedRecognitions.value = transformedRecognitions
    }
}
