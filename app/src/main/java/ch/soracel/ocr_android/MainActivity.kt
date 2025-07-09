package ch.soracel.ocr_android

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.view.TransformExperimental
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ch.soracel.ocr_android.ui.PreviewScreen
import ch.soracel.ocr_android.ui.theme.Ocr_androidTheme
import ch.soracel.ocr_android.viewmodel.OcrViewModel

@TransformExperimental
class MainActivity : ComponentActivity() {

    private val viewModel: OcrViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()

        setContent {
            Ocr_androidTheme {
                PreviewScreen(viewModel)
            }
        }
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 0)
        }
    }
}