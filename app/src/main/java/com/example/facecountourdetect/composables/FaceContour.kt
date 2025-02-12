package com.example.facecountourdetect.composables
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour


@Composable
fun FaceContour(
    modifier: Modifier = Modifier,
    faces: List<Face>
) {
    // Canvas - это область, на которой можно рисовать
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvas = drawContext.canvas.nativeCanvas
        val paint = Paint().asFrameworkPaint().apply {
            color = android.graphics.Color.GREEN
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 5f
        }

        faces.forEach { face ->
            // граница лица
            val rect = face.boundingBox
            canvas.drawRect(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat(), paint)

            // рисует контуры лица
            face.getContour(FaceContour.FACE)?.points?.forEach { point ->
                canvas.drawCircle(point.x, point.y, 5f, paint)
            }
        }
    }
}