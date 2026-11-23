package com.example.module4.task14

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.roundToInt

@Composable
fun CompassScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val rotationSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }

    var azimuth by rememberSaveable { mutableFloatStateOf(0f) }
    val animatedAzimuth by animateFloatAsState(targetValue = -azimuth, label = "compass")

    DisposableEffect(lifecycleOwner, rotationSensor) {
        if (rotationSensor == null) return@DisposableEffect onDispose {}

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val deg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                azimuth = (deg + 360f) % 360f
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME ->
                    sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
                Lifecycle.Event.ON_PAUSE ->
                    sensorManager.unregisterListener(listener)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            sensorManager.unregisterListener(listener)
        }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Компас", style = MaterialTheme.typography.headlineMedium)

        if (rotationSensor == null) {
            Text(
                "Устройство не поддерживает датчик ориентации",
                color = Color.Red,
                style = MaterialTheme.typography.titleLarge
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("N", fontSize = 32.sp, color = Color.Red)
                Spacer(Modifier.height(8.dp))
                Canvas(modifier = Modifier.size(260.dp)) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(color = Color.DarkGray, radius = radius)
                    rotate(degrees = animatedAzimuth, pivot = center) {
                        val arrowLen = radius * 0.8f
                        drawLine(Color.Red, start = center, end = Offset(center.x, center.y - arrowLen), strokeWidth = 14f)
                        drawLine(Color.LightGray, start = center, end = Offset(center.x, center.y + arrowLen), strokeWidth = 14f)
                    }
                }
            }
            Text("Азимут: ${azimuth.roundToInt()}°", style = MaterialTheme.typography.headlineSmall)
        }
    }
}
