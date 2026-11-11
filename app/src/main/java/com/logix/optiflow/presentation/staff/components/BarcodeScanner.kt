package com.logix.optiflow.presentation.staff.components

import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Vista de la cámara trasera que lee códigos de barras y QR con ML Kit (sin internet) y llama a
 * [onCode] con el texto de cada código que ve. Necesita el permiso de cámara ya concedido.
 */
@Composable
fun BarcodeScanner(onCode: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnCode by rememberUpdatedState(onCode)
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39, Barcode.FORMAT_EAN_13, Barcode.FORMAT_QR_CODE)
                .build(),
        )
    }
    val controller = remember {
        val executor = ContextCompat.getMainExecutor(context)
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(
                executor,
                MlKitAnalyzer(listOf(scanner), ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL, executor) { result ->
                    result.getValue(scanner)
                        ?.firstNotNullOfOrNull { barcode -> barcode.rawValue?.trim()?.takeIf { it.isNotEmpty() } }
                        ?.let { currentOnCode(it) }
                },
            )
        }
    }
    // La cámara se apaga sola cuando la pantalla deja de verse.
    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose { controller.unbind() }
    }
    DisposableEffect(Unit) { onDispose { scanner.close() } }

    AndroidView(
        factory = { PreviewView(it).apply { this.controller = controller } },
        modifier = modifier.clipToBounds(),
    )
}
