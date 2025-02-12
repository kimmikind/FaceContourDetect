package com.example.facecountourdetect.composables
import android.graphics.RectF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import kotlin.math.min

///
@Composable
fun DrawFaceContour(
    modifier: Modifier = Modifier,
    faces: List<Face>,
    previewWidth: Int,
    previewHeight: Int,
    viewWidth: Int,
    viewHeight: Int,
    imageWidth: Int, // Ширина изображения (640)
    imageHeight: Int, // Высота изображения (480)
) {
    Canvas(modifier = modifier.fillMaxSize()) {

        // Сохраняем пропорции изображения при масштабировании
        val scaleX = viewWidth.toFloat() / imageWidth.toFloat()
        val scaleY = viewHeight.toFloat() / imageHeight.toFloat()
        val scale = min(scaleX, scaleY) // Используем минимальный коэффициент для сохранения пропорций


        // Смещение для центрирования изображения
        val offsetX = (viewWidth - imageWidth * scale) / 2
        val offsetY = (viewHeight - imageHeight * scale) / 2


        Log.d("CameraPreview", "Preview dimensions: $previewWidth x $previewHeight")
        Log.d("CameraPreview", "View dimensions: $viewWidth x $viewHeight")
        Log.d("Scaling", "Scale: $scale, OffsetX: $offsetX, OffsetY: $offsetY, adjOffsetY: $offsetY" )
        faces.forEach { face ->
            Log.d("FaceDetection", "Face bounding box: ${face.boundingBox}")
            Log.d("Scaling", "Original bounding box: ${face.boundingBox}")
            Log.d("Scaling", "Scaled bounding box: RectF(${face.boundingBox.left * scale + offsetX}, ${face.boundingBox.top * scale + offsetY}, ${face.boundingBox.right * scale + offsetX}, ${face.boundingBox.bottom * scale + offsetY})")
            // Преобразуем координаты boundingBox в координаты экрана
            val rect = RectF(
                face.boundingBox.left * scale + offsetX,
                face.boundingBox.top * scale + offsetY,
                face.boundingBox.right * scale + offsetX,
                face.boundingBox.bottom * scale + offsetY
            )

            // Рисуем прямоугольник вокруг лица
            drawRect(
                color = Color.Green,
                topLeft = Offset(rect.left, rect.top),
                size = Size(rect.width(), rect.height()),
                style = Stroke(width = 3f)
            )



            // Рисуем контуры лица (опционально)
            face.getContour(FaceContour.FACE)?.points?.forEach { point ->
                drawCircle(

                    color = Color.Green,
                    center = Offset(
                        point.x * scale + offsetX,
                        point.y * scale + offsetY),
                    radius = 10f
                )
            }
        }
    }

}
