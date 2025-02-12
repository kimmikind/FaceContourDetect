package com.example.facecountourdetect
import CameraPreview
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.dp

import androidx.compose.material3.Surface
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Alignment
import androidx.core.content.ContextCompat



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceContourDetect()
        }
    }
}

@Composable
fun FaceContourDetect() {
    // Получаем контекст внутри Composable-функции.
    val context = LocalContext.current

    var hasCameraPermission by remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // При первом запуске проверяем, есть ли разрешение на камеру.
    LaunchedEffect(Unit) {
        val permissionCheckResult = ContextCompat.checkSelfPermission(
                context,
            Manifest.permission.CAMERA
        )
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true
        } else {
            // Если разрешения нет, запрашиваем его.
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // контейнер для отображения контента с использованием Material Design.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (hasCameraPermission) {
            CameraScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), //  отступы по краям
                verticalArrangement = Arrangement.Center, // Выравниваем содержимое по центру
                horizontalAlignment = Alignment.CenterHorizontally // Выравниваем по горизонтали
            ) {
                Text(
                    text = "Для работы приложения требуется доступ к камере.",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.wrapContentSize() // Кнопка будет занимать только необходимую ей площадь
                ) {
                    Text("Запросить разрешение")
                }
            }
        }
    }
}

@Composable
fun CameraScreen() {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Создаем состояние для хранения Future-объекта, который предоставляет доступ к ProcessCameraProvider.
    var cameraProviderFuture by remember { mutableStateOf<ListenableFuture<ProcessCameraProvider>?>(null) }

    CameraPreview(
        modifier = Modifier.fillMaxSize(),
        onPreviewViewCreated = { previewView ->
            cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
            cameraProviderFuture?.addListener({
                val cameraProvider = cameraProviderFuture?.get()
                // Создаем Preview-объект, который будет отображать изображение с камеры.
                val preview = Preview.Builder().build().also {
                    // Устанавливаем SurfaceProvider для отображения предпросмотра.
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                try {
                    cameraProvider?.unbindAll()
                    // Привязываем камеру к жизненному циклу и настраиваем Preview.
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(previewView.context)) // Используем главный Executor для выполнения кода.
        }
    )
}