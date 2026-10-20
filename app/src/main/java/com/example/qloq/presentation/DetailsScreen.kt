package com.example.qloq.presentation

import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import androidx.wear.compose.material.Text
import java.lang.System.currentTimeMillis

@Composable
fun DetailsScreen(qloSprite: QloSprite, sharedPreferences: SharedPreferences) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    SideEffect {
        compareTime(qloSprite, sharedPreferences)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF4A148C), Color(0xFF880E4F))
                )
            )
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            color = Color(0xFFFFD54F),
            text = "${qloSprite.name} · Nv.${qloSprite.level}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
        Text(
            color = Color.White,
            text = qloSprite.stage.title,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
        Text(
            color = Color(0xFFB3E5FC),
            text = "Tamaño: ${(qloSprite.growthScale * 100).toInt()}% · Comidas: ${qloSprite.mealsEaten}",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Barras de estado y crecimiento
        CustomStatRow(
            label = "XP Evolución (${qloSprite.xpInCurrentLevel}/100)",
            progress = qloSprite.xpInCurrentLevel / 100f,
            barColor = Color(0xFFFFD54F)
        )
        CustomStatRow(
            label = "Hambre (${qloSprite.hungerStat}%)",
            progress = qloSprite.hungerStat / 100f,
            barColor = Color(0xFFFF7043)
        )
        CustomStatRow(
            label = "Sed (${qloSprite.thirstStat}%)",
            progress = qloSprite.thirstStat / 100f,
            barColor = Color(0xFF29B6F6)
        )
        CustomStatRow(
            label = "Felicidad (${qloSprite.happinessStat}%)",
            progress = qloSprite.happinessStat / 100f,
            barColor = Color(0xFFEC407A)
        )
        CustomStatRow(
            label = "Energía (${qloSprite.energyStat}%)",
            progress = qloSprite.energyStat / 100f,
            barColor = Color(0xFF66BB6A)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Panel de Sensores del Smartwatch
        Text(
            color = Color(0xFFFFF59D),
            text = "SENSORES DEL RELOJ",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            color = Color.White.copy(alpha = 0.9f),
            text = "Pasos: ${qloSprite.stepsCount} | Sacudidas: ${qloSprite.shakeCount}",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
        Text(
            color = Color.White.copy(alpha = 0.9f),
            text = "Pulso: ${qloSprite.heartRateBpm} BPM | Luz: ${qloSprite.ambientLux.toInt()} lx",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botones interactivos para probar/jugar con los sensores también en emulador
        SensorActionChip(
            text = "🏃 Caminar +15 Pasos (+60 XP)",
            bgColor = Color(0xFF2E7D32)
        ) {
            triggerWatchHaptic(context, 40L)
            qloSprite.onStepsDetected(15)
            qloSprite.saveToPreferences(sharedPreferences)
            Toast.makeText(context, "¡+15 pasos! Nivel ${qloSprite.level}", Toast.LENGTH_SHORT).show()
        }

        Spacer(modifier = Modifier.height(5.dp))

        SensorActionChip(
            text = "🤸 Sacudir Reloj (+25 XP)",
            bgColor = Color(0xFF6A1B9A)
        ) {
            triggerWatchHaptic(context, 60L)
            qloSprite.onWatchShaken()
            qloSprite.saveToPreferences(sharedPreferences)
            Toast.makeText(context, "¡Acrobacia! +25 XP", Toast.LENGTH_SHORT).show()
        }

        Spacer(modifier = Modifier.height(5.dp))

        SensorActionChip(
            text = if (qloSprite.isNightOrDark) "☀️ Sensor Luz: Día (120 lx)" else "🌙 Sensor Luz: Noche (5 lx)",
            bgColor = Color(0xFF0277BD)
        ) {
            triggerWatchHaptic(context, 35L)
            qloSprite.toggleSleepManual()
            qloSprite.saveToPreferences(sharedPreferences)
        }

        Spacer(modifier = Modifier.height(5.dp))

        SensorActionChip(
            text = "💓 Subir Pulso (${qloSprite.heartRateBpm} BPM)",
            bgColor = Color(0xFFC2185B)
        ) {
            triggerWatchHaptic(context, 35L)
            qloSprite.heartRateBpm = if (qloSprite.heartRateBpm >= 145) 72 else qloSprite.heartRateBpm + 18
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

fun compareTime(qloSprite: QloSprite, sharedPreferences: SharedPreferences) {
    val lastTimeSeen = sharedPreferences.getLong("timeStamp", currentTimeMillis())
    val curTime = currentTimeMillis()
    val timeDiff = curTime - lastTimeSeen
    qloSprite.updateStats(timeDiff)
    qloSprite.saveToPreferences(sharedPreferences)
}

@Composable
private fun CustomStatRow(label: String, progress: Float, barColor: Color) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable {
                Toast.makeText(context, "$label: ${(progress * 100).toInt()}%", Toast.LENGTH_SHORT).show()
            }
    ) {
        Text(
            color = Color.White,
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp
        )
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.22f)),
            color = barColor
        )
    }
}

@Composable
private fun SensorActionChip(
    text: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(device = Devices.WEAR_OS_LARGE_ROUND, showSystemUi = true)
@Composable
fun DetailsPreview() {
    val sprite = QloSprite("Qlo", 60, 70, 80, initialXp = 140, initialMeals = 5, initialSteps = 120)
    val context = LocalContext.current
    DetailsScreen(sprite, PreferenceManager.getDefaultSharedPreferences(context))
}
