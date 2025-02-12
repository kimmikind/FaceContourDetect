package com.example.facecountourdetect.composables

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
///
@Composable
fun FaceContourDetect() {
    // контекст внутри Composable-функции.
    val context = LocalContext.current

    var hasCameraPermission by remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // проверка при первом запуске
    LaunchedEffect(Unit) {
        val permissionCheckResult = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true
        } else {
            // запрос разрешения
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
                verticalArrangement = Arrangement.Center, // выравниваем содержимое по центру
                horizontalAlignment = Alignment.CenterHorizontally // выравниваем по горизонтали
            ) {
                Text(
                    text =
                    "The application requires access to the camera.",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.wrapContentSize() // кнопка будет занимать только необходимую ей площадь
                ) {
                    Text("Request permission")
                }
            }
        }
    }
}