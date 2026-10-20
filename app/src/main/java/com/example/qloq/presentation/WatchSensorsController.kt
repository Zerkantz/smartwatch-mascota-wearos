package com.example.qloq.presentation

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

fun triggerWatchHaptic(context: Context, durationMs: Long = 45L) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        }
    } catch (_: Exception) {
        // Ignorar si el emulador no tiene vibrador activo
    }
}

@Composable
fun BindWatchSensors(
    qloSprite: QloSprite,
    sharedPreferences: SharedPreferences
) {
    val context = LocalContext.current

    DisposableEffect(qloSprite) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        var lastShakeTimestamp = 0L
        var baselineSteps: Int? = null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        val ax = event.values.getOrNull(0) ?: 0f
                        val ay = event.values.getOrNull(1) ?: 0f
                        val az = event.values.getOrNull(2) ?: 9.8f

                        // Inclinación suave de la mascota en pantalla según giro de muñeca
                        val targetTiltX = (-ax * 2.8f).coerceIn(-22f, 22f)
                        val targetTiltY = (ay * 2.2f).coerceIn(-16f, 16f)
                        qloSprite.tiltX = qloSprite.tiltX * 0.75f + targetTiltX * 0.25f
                        qloSprite.tiltY = qloSprite.tiltY * 0.75f + targetTiltY * 0.25f

                        // Detección de sacudida (Shake) para acrobacia y diversión
                        val magnitude = sqrt(ax * ax + ay * ay + az * az)
                        val deltaGravity = kotlin.math.abs(magnitude - SensorManager.GRAVITY_EARTH)
                        val now = System.currentTimeMillis()
                        if (deltaGravity > 6.2f && now - lastShakeTimestamp > 1100L) {
                            lastShakeTimestamp = now
                            triggerWatchHaptic(context, 65L)
                            qloSprite.onWatchShaken()
                            qloSprite.saveToPreferences(sharedPreferences)
                        }
                    }

                    Sensor.TYPE_STEP_DETECTOR -> {
                        qloSprite.onStepsDetected(1)
                        qloSprite.saveToPreferences(sharedPreferences)
                    }

                    Sensor.TYPE_STEP_COUNTER -> {
                        val totalSensorSteps = (event.values.getOrNull(0) ?: 0f).toInt()
                        if (baselineSteps == null) {
                            baselineSteps = totalSensorSteps
                        } else {
                            val diff = totalSensorSteps - (baselineSteps ?: totalSensorSteps)
                            if (diff > 0) {
                                baselineSteps = totalSensorSteps
                                qloSprite.onStepsDetected(diff)
                                qloSprite.saveToPreferences(sharedPreferences)
                            }
                        }
                    }

                    Sensor.TYPE_HEART_RATE -> {
                        val bpm = (event.values.getOrNull(0) ?: 0f).toInt()
                        if (bpm in 40..210) {
                            qloSprite.heartRateBpm = bpm
                        }
                    }

                    Sensor.TYPE_LIGHT -> {
                        val lux = event.values.getOrNull(0) ?: 100f
                        qloSprite.updateLightSensor(lux)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null) {
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
            }
            sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
            }
            sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
            }
            sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
            sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }
}
