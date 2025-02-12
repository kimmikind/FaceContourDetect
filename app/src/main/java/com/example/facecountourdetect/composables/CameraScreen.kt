package com.example.facecountourdetect.composables

import CameraPreview
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors

@Composable
fun CameraScreen() {
    val lifecycleOwner = LocalLifecycleOwner.current

    var previewWidth by remember { mutableStateOf(0) }
    var previewHeight by remember { mutableStateOf(0) }
    var viewWidth by remember { mutableStateOf(0) }
    var viewHeight by remember { mutableStateOf(0) }
    var imageWidth by remember { mutableStateOf(0) }
    var imageHeight by remember { mutableStateOf(0) }
    // состояние для хранения списка распознанных лиц
    var faces by remember { mutableStateOf<List<Face>>(emptyList()) }

    // детектор лиц с высокой точностью
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST) // высокая точность распознавания
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL) //  распознавание всех контуров лица
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL) // распознавание ключевых точек (глаза, нос, рот)
        .build()

    // создаем клиент для распознавания лиц
    val detector = FaceDetection.getClient(options)

    // cоздаем состояние для хранения Future-объекта, который предоставляет доступ к ProcessCameraProvider.
    var cameraProviderFuture by remember { mutableStateOf<ListenableFuture<ProcessCameraProvider>?>(null) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                viewWidth = size.width
                viewHeight = size.height
            }
    ) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onPreviewViewCreated = { previewView ->
                previewView.post {
                    previewWidth = previewView.width
                    previewHeight = previewView.height

                }
                previewView.scaleType = PreviewView.ScaleType.FIT_CENTER
                cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
                cameraProviderFuture?.addListener({
                    val cameraProvider = cameraProviderFuture?.get()
                    // Создаем Preview-объект, который будет отображать изображение с камеры.
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    // анализатор изображений для обработки кадров с камеры
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Стратегия обработки только последнего кадра

                        .build()
                        .also { analyzer ->
                            // устанавливаем анализатор
                            analyzer.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                imageWidth = imageProxy.width
                                imageHeight = imageProxy.height
                                Log.d("ImageDimensions", "Image dimensions: $imageWidth x $imageHeight")
                                // получаем изображение
                                val bitmap =
                                    imageProxy.toBitmap() // Используем встроенный метод для преобразования
                                if (bitmap != null) {
                                    // создаем объект InputImage для передачи в детектор лиц
                                    val image = InputImage.fromBitmap(
                                        bitmap,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    // обрабатываем изображение для распознавания лиц
                                    detector.process(image)
                                        .addOnSuccessListener { detectedFaces ->
                                            // если лица распознаны, обновляем состояние faces
                                            faces = detectedFaces
                                        }
                                        .addOnFailureListener { e ->
                                            e.printStackTrace()
                                        }
                                }
                                imageProxy.close()
                            }
                        }
                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                    try {
                        cameraProvider?.unbindAll()
                        // привязываем камеру к жизненному циклу и настраиваем Preview.
                        cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(previewView.context)) // используем главный поток
            }
        )
        DrawFaceContour(
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            imageWidth = imageWidth, // Ширина изображения (640)
            imageHeight = imageHeight, // Высота изображения (480)
            faces = faces)
    }
}