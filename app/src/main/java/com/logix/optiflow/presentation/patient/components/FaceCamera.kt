package com.logix.optiflow.presentation.patient.components

import androidx.camera.core.CameraSelector
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark

/** Centro de los dos ojos del rostro detectado, en píxeles de la vista de la cámara. */
data class EyePositions(val left: Offset, val right: Offset)

/**
 * Cámara frontal con ML Kit Face Detection (sin internet). Llama a [onEyes] en cada cuadro con
 * los ojos del rostro principal, o con null si no hay rostro. Las coordenadas ya vienen en la
 * vista (escaladas y en espejo como la vista previa), así que se pueden dibujar encima tal cual.
 */
@Composable
fun FaceCamera(onEyes: (EyePositions?) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEyes by rememberUpdatedState(onEyes)
    val detector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .build(),
        )
    }
    val controller = remember {
        val executor = ContextCompat.getMainExecutor(context)
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(
                executor,
                MlKitAnalyzer(listOf(detector), ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED, executor) { result ->
                    val face = result.getValue(detector)?.maxByOrNull { it.boundingBox.width() }
                    val left = face?.getLandmark(FaceLandmark.LEFT_EYE)?.position
                    val right = face?.getLandmark(FaceLandmark.RIGHT_EYE)?.position
                    currentOnEyes(
                        if (left != null && right != null) EyePositions(Offset(left.x, left.y), Offset(right.x, right.y)) else null,
                    )
                },
            )
        }
    }
    // La cámara se apaga sola cuando la pantalla deja de verse.
    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose { controller.unbind() }
    }
    DisposableEffect(Unit) { onDispose { detector.close() } }

    AndroidView(
        factory = { PreviewView(it).apply { this.controller = controller } },
        modifier = modifier.clipToBounds(),
    )
}
