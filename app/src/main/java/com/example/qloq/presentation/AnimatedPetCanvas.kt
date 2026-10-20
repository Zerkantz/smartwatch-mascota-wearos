package com.example.qloq.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedPetStage(
    qloSprite: QloSprite,
    onPetTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Regresa automáticamente a IDLE después de ejecutar una animación de comida/juego/sensor
    LaunchedEffect(qloSprite.actionState, qloSprite.actionTimestamp) {
        if (qloSprite.actionState != PetActionState.IDLE &&
            qloSprite.actionState != PetActionState.SLEEPING
        ) {
            delay(1800L)
            if (qloSprite.actionState != PetActionState.SLEEPING) {
                qloSprite.actionState = PetActionState.IDLE
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition()

    // Respiración y rebote continuo
    val breath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Fase continua de 0 a 360 grados para órbitas, masticación y alas
    val waveAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Parpadeo natural cada ~3.2 segundos
    val blinkTimer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Escala animada de crecimiento con rebote elástico al comer o subir de nivel
    val animatedGrowthScale by animateFloatAsState(
        targetValue = qloSprite.growthScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // Progreso de acción activa (comer, girar al sacudir, evolucionar)
    val actionProgress by animateFloatAsState(
        targetValue = if (qloSprite.actionState == PetActionState.IDLE ||
            qloSprite.actionState == PetActionState.SLEEPING
        ) 0f else 1f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
    )

    // Giro acrobático 360° cuando se sacude el reloj o evoluciona
    val spinDegrees by animateFloatAsState(
        targetValue = if (qloSprite.actionState == PetActionState.SHAKING ||
            qloSprite.actionState == PetActionState.EVOLVING
        ) 360f else 0f,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onPetTapped() }
                )
            }
    ) {
        val w = size.width
        val h = size.height
        val isNight = qloSprite.isNightOrDark
        val rad = Math.toRadians(waveAngle.toDouble())

        // 1. Fondo dinámico Día / Noche (según Sensor de Luz del reloj)
        drawWatchEnvironment(w, h, isNight, rad, breath)

        // 2. Posición de la mascota influenciada por el Acelerómetro (Tilt X/Y) y saltos
        val jumpOffset = when (qloSprite.actionState) {
            PetActionState.PLAYING, PetActionState.SHAKING -> -18f * sin(rad * 2).toFloat()
            PetActionState.RUNNING -> -12f * kotlin.math.abs(sin(rad * 3).toFloat())
            PetActionState.SLEEPING -> 8f
            else -> -6f * breath
        }

        val centerX = (w / 2f) + qloSprite.tiltX * 1.4f
        val centerY = (h * 0.52f) + qloSprite.tiltY * 0.9f + jumpOffset

        // Sombra en el suelo debajo de la mascota
        val shadowWidth = 68f * animatedGrowthScale * (1f - 0.08f * breath)
        drawOval(
            color = Color.Black.copy(alpha = 0.28f),
            topLeft = Offset(centerX - shadowWidth / 2f, h * 0.69f),
            size = Size(shadowWidth, 16f)
        )

        // Polvo al correr con el podómetro (Sensor de Pasos)
        if (qloSprite.actionState == PetActionState.RUNNING) {
            for (i in -1..1 step 2) {
                val puffX = centerX + i * (26f + 12f * sin(rad * 2).toFloat())
                val puffY = h * 0.67f - 6f * kotlin.math.abs(cos(rad * 2).toFloat())
                drawCircle(
                    color = Color.White.copy(alpha = 0.55f),
                    radius = 7f,
                    center = Offset(puffX, puffY)
                )
            }
        }

        // Aura de pulso sincronizada al Sensor de Ritmo Cardíaco
        val pulseAlpha = 0.16f + 0.14f * sin(rad * (qloSprite.heartRateBpm / 60.0)).toFloat()
        val bodyRadius = 32f
        drawCircle(
            color = when (qloSprite.stage) {
                PetStage.MAESTRO -> Color(0xFFFFD54F).copy(alpha = pulseAlpha + 0.12f)
                PetStage.ADULTO -> Color(0xFF80DEEA).copy(alpha = pulseAlpha)
                else -> Color(0xFFF48FB1).copy(alpha = pulseAlpha * 0.7f)
            },
            radius = bodyRadius * animatedGrowthScale * 1.38f,
            center = Offset(centerX, centerY)
        )

        // 3. Dibujo del cuerpo animado de la mascota con rotación por inclinación de muñeca
        val tiltDegrees = (qloSprite.tiltX * 0.8f) + spinDegrees
        val squashX = 1f + 0.05f * sin(rad).toFloat()
        val squashY = 1f - 0.05f * sin(rad).toFloat()

        rotate(degrees = tiltDegrees, pivot = Offset(centerX, centerY)) {
            scale(
                scaleX = animatedGrowthScale * squashX,
                scaleY = animatedGrowthScale * squashY,
                pivot = Offset(centerX, centerY)
            ) {
                drawEvolvingPetBody(
                    centerX = centerX,
                    centerY = centerY,
                    bodyRadius = bodyRadius,
                    qloSprite = qloSprite,
                    rad = rad,
                    isBlinking = blinkTimer > 92f
                )
            }
        }

        // 4. Efectos en primer plano: Comida volando a la boca, Bebida, Corazones o Zzz
        drawForegroundActionEffects(
            centerX = centerX,
            centerY = centerY,
            qloSprite = qloSprite,
            rad = rad,
            actionProgress = actionProgress
        )
    }
}

private fun DrawScope.drawWatchEnvironment(
    w: Float,
    h: Float,
    isNight: Boolean,
    rad: Double,
    breath: Float
) {
    // Cielo degradado según luz ambiental
    val skyColors = if (isNight) {
        listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF311042))
    } else {
        listOf(Color(0xFF2E1A47), Color(0xFF6A2C70), Color(0xFFB83B5E))
    }
    drawRect(brush = Brush.verticalGradient(skyColors))

    // Estrellas o Sol/Nubes
    if (isNight) {
        val starPositions = listOf(
            Offset(w * 0.22f, h * 0.22f),
            Offset(w * 0.78f, h * 0.25f),
            Offset(w * 0.35f, h * 0.16f),
            Offset(w * 0.66f, h * 0.18f),
            Offset(w * 0.18f, h * 0.42f),
            Offset(w * 0.84f, h * 0.44f)
        )
        starPositions.forEachIndexed { idx, pos ->
            val twinkle = 2.2f + 1.5f * kotlin.math.abs(sin(rad + idx).toFloat())
            drawCircle(Color(0xFFFFF59D), radius = twinkle, center = pos)
        }
        // Luna creciente
        drawCircle(
            color = Color(0xFFFFF176),
            radius = 12f,
            center = Offset(w * 0.76f, h * 0.24f)
        )
    } else {
        // Sol cálido animado
        drawCircle(
            color = Color(0xFFFFD54F).copy(alpha = 0.85f),
            radius = 14f + 2f * breath,
            center = Offset(w * 0.77f, h * 0.24f)
        )
        // Nube suave flotando
        val cloudX = (w * 0.26f) + 6f * sin(rad * 0.5).toFloat()
        val cloudY = h * 0.25f
        drawRoundRect(
            color = Color.White.copy(alpha = 0.26f),
            topLeft = Offset(cloudX - 18f, cloudY),
            size = Size(36f, 11f),
            cornerRadius = CornerRadius(6f, 6f)
        )
    }

    // Pradera / plataforma inferior donde vive la mascota
    drawOval(
        brush = Brush.verticalGradient(
            if (isNight) {
                listOf(Color(0xFF1B4332), Color(0xFF081C15))
            } else {
                listOf(Color(0xFF52B788), Color(0xFF2D6A4F))
            }
        ),
        topLeft = Offset(-w * 0.15f, h * 0.62f),
        size = Size(w * 1.3f, h * 0.55f)
    )
}

private fun DrawScope.drawEvolvingPetBody(
    centerX: Float,
    centerY: Float,
    bodyRadius: Float,
    qloSprite: QloSprite,
    rad: Double,
    isBlinking: Boolean
) {
    val stage = qloSprite.stage

    // Alas animadas para etapa ADULTO y MAESTRO
    if (stage == PetStage.ADULTO || stage == PetStage.MAESTRO) {
        val wingFlap = 10f * sin(rad * 3).toFloat()
        val wingColor = if (stage == PetStage.MAESTRO) Color(0xFFFFD54F) else Color(0xFF80DEEA)
        drawOval(
            color = wingColor,
            topLeft = Offset(centerX - bodyRadius * 1.45f, centerY - 12f + wingFlap * 0.4f),
            size = Size(22f, 15f)
        )
        drawOval(
            color = wingColor,
            topLeft = Offset(centerX + bodyRadius * 0.75f, centerY - 12f - wingFlap * 0.4f),
            size = Size(22f, 15f)
        )
    }

    // Antena de retoño en etapa BEBÉ
    if (stage == PetStage.BEBE) {
        drawLine(
            color = Color(0xFF81C784),
            start = Offset(centerX, centerY - bodyRadius),
            end = Offset(centerX + 4f * sin(rad).toFloat(), centerY - bodyRadius - 12f),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )
        drawOval(
            color = Color(0xFF66BB6A),
            topLeft = Offset(centerX + 4f * sin(rad).toFloat(), centerY - bodyRadius - 16f),
            size = Size(11f, 7f)
        )
    }

    // Orejitas / Cuernitos desde etapa JOVEN en adelante
    if (stage != PetStage.BEBE) {
        val hornColor = if (stage == PetStage.MAESTRO) Color(0xFFFFCA28) else Color(0xFFEC407A)
        val leftEar = Path().apply {
            moveTo(centerX - 20f, centerY - bodyRadius + 6f)
            lineTo(centerX - 28f, centerY - bodyRadius - 12f)
            lineTo(centerX - 10f, centerY - bodyRadius + 2f)
            close()
        }
        val rightEar = Path().apply {
            moveTo(centerX + 20f, centerY - bodyRadius + 6f)
            lineTo(centerX + 28f, centerY - bodyRadius - 12f)
            lineTo(centerX + 10f, centerY - bodyRadius + 2f)
            close()
        }
        drawPath(leftEar, color = hornColor)
        drawPath(rightEar, color = hornColor)
    }

    // Pies saltarines
    val footBounce = 3f * sin(rad * 2).toFloat()
    drawOval(
        color = Color(0xFFAD1457),
        topLeft = Offset(centerX - 20f, centerY + bodyRadius - 6f + footBounce),
        size = Size(14f, 9f)
    )
    drawOval(
        color = Color(0xFFAD1457),
        topLeft = Offset(centerX + 6f, centerY + bodyRadius - 6f - footBounce),
        size = Size(14f, 9f)
    )

    // Cuerpo principal de la mascota (cambia de tono sutil según etapa)
    val bodyColors = when (stage) {
        PetStage.BEBE -> listOf(Color(0xFFF48FB1), Color(0xFFEC407A))
        PetStage.JOVEN -> listOf(Color(0xFFFF80AB), Color(0xFFD81B60))
        PetStage.ADULTO -> listOf(Color(0xFFCE93D8), Color(0xFF8E24AA))
        PetStage.MAESTRO -> listOf(Color(0xFFFFCC80), Color(0xFFE65100))
    }

    drawRoundRect(
        brush = Brush.radialGradient(
            colors = bodyColors,
            center = Offset(centerX - 8f, centerY - 8f),
            radius = bodyRadius * 1.4f
        ),
        topLeft = Offset(centerX - bodyRadius, centerY - bodyRadius),
        size = Size(bodyRadius * 2f, bodyRadius * 1.95f),
        cornerRadius = CornerRadius(bodyRadius * 0.9f, bodyRadius * 0.9f)
    )

    // Pancita clara (se infla un poquito cuando está comiendo)
    val bellyScale = if (qloSprite.actionState == PetActionState.EATING) 1.15f else 1f
    drawOval(
        color = Color.White.copy(alpha = 0.28f),
        topLeft = Offset(centerX - 16f * bellyScale, centerY + 2f),
        size = Size(32f * bellyScale, 20f * bellyScale)
    )

    // Banda deportiva en la frente cuando caminas con el reloj (RUNNING)
    if (qloSprite.actionState == PetActionState.RUNNING) {
        drawRoundRect(
            color = Color(0xFFE53935),
            topLeft = Offset(centerX - bodyRadius + 3f, centerY - 19f),
            size = Size((bodyRadius - 3f) * 2f, 6f),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }

    // Corona dorada flotante para etapa MAESTRO
    if (stage == PetStage.MAESTRO) {
        val crownY = centerY - bodyRadius - 8f
        val crownPath = Path().apply {
            moveTo(centerX - 12f, crownY)
            lineTo(centerX - 14f, crownY - 10f)
            lineTo(centerX - 6f, crownY - 4f)
            lineTo(centerX, crownY - 12f)
            lineTo(centerX + 6f, crownY - 4f)
            lineTo(centerX + 14f, crownY - 10f)
            lineTo(centerX + 12f, crownY)
            close()
        }
        drawPath(crownPath, color = Color(0xFFFFD54F))
    }

    // Mejillas chapeteadas
    drawCircle(
        color = Color(0xFFFF5252).copy(alpha = 0.45f),
        radius = 5.2f,
        center = Offset(centerX - 17f, centerY + 2f)
    )
    drawCircle(
        color = Color(0xFFFF5252).copy(alpha = 0.45f),
        radius = 5.2f,
        center = Offset(centerX + 17f, centerY + 2f)
    )

    // Ojos expresivos
    val eyeY = centerY - 6f
    val leftEyeX = centerX - 11f
    val rightEyeX = centerX + 11f

    if (qloSprite.isNightOrDark || isBlinking) {
        // Ojos cerrados / parpadeando
        drawLine(
            color = Color(0xFF1A1830),
            start = Offset(leftEyeX - 5f, eyeY),
            end = Offset(leftEyeX + 5f, eyeY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF1A1830),
            start = Offset(rightEyeX - 5f, eyeY),
            end = Offset(rightEyeX + 5f, eyeY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    } else if (qloSprite.actionState == PetActionState.PLAYING ||
        qloSprite.actionState == PetActionState.SHAKING
    ) {
        // Ojos felices "^ ^"
        drawArc(
            color = Color(0xFF1A1830),
            startAngle = 190f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(leftEyeX - 5f, eyeY - 4f),
            size = Size(10f, 9f),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFF1A1830),
            startAngle = 190f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(rightEyeX - 5f, eyeY - 4f),
            size = Size(10f, 9f),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )
    } else {
        // Ojos abiertos brillantes con pupila que sigue la inclinación del reloj
        val pupilShiftX = (qloSprite.tiltX * 0.08f).coerceIn(-2f, 2f)
        drawCircle(Color(0xFF1A1830), radius = 5.2f, center = Offset(leftEyeX, eyeY))
        drawCircle(Color(0xFF1A1830), radius = 5.2f, center = Offset(rightEyeX, eyeY))
        drawCircle(
            Color.White,
            radius = 2f,
            center = Offset(leftEyeX - 1.5f + pupilShiftX, eyeY - 1.8f)
        )
        drawCircle(
            Color.White,
            radius = 2f,
            center = Offset(rightEyeX - 1.5f + pupilShiftX, eyeY - 1.8f)
        )
    }

    // Boca animada: mastica cuando come, sonríe cuando está feliz o hace puchero si tiene hambre
    val mouthY = centerY + 6f
    when {
        qloSprite.actionState == PetActionState.EATING ||
            qloSprite.actionState == PetActionState.DRINKING -> {
            // Masticación rítmica (abre y cierra la boca)
            val chewOpen = 3f + 7f * kotlin.math.abs(sin(rad * 4).toFloat())
            drawOval(
                color = Color(0xFF880E4F),
                topLeft = Offset(centerX - 8f, mouthY - chewOpen / 2f),
                size = Size(16f, chewOpen + 3f)
            )
            // Lengüita
            drawOval(
                color = Color(0xFFFF80AB),
                topLeft = Offset(centerX - 5f, mouthY + 1f),
                size = Size(10f, (chewOpen * 0.5f).coerceAtLeast(2f))
            )
        }
        qloSprite.getAverage() < 35 -> {
            // Boca triste si tiene mucha hambre o sed
            drawArc(
                color = Color(0xFF1A1830),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(centerX - 7f, mouthY),
                size = Size(14f, 8f),
                style = Stroke(width = 2.6f, cap = StrokeCap.Round)
            )
        }
        else -> {
            // Sonrisa alegre
            drawArc(
                color = Color(0xFF1A1830),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(centerX - 8f, mouthY - 4f),
                size = Size(16f, 10f),
                style = Stroke(width = 2.8f, cap = StrokeCap.Round)
            )
        }
    }
}

private fun DrawScope.drawForegroundActionEffects(
    centerX: Float,
    centerY: Float,
    qloSprite: QloSprite,
    rad: Double,
    actionProgress: Float
) {
    when (qloSprite.actionState) {
        PetActionState.EATING -> {
            // Comida viajando hacia la boca + migajas de colores
            val foodColor = Color(qloSprite.currentFood.colorHex)
            val foodX = centerX + (1f - actionProgress) * 32f
            val foodY = centerY + 6f + (1f - actionProgress) * 28f
            val foodRadius = (11f * (1.15f - actionProgress * 0.6f)).coerceAtLeast(4f)

            drawCircle(color = foodColor, radius = foodRadius, center = Offset(foodX, foodY))
            drawCircle(
                color = Color(0xFF66BB6A),
                radius = foodRadius * 0.45f,
                center = Offset(foodX - 3f, foodY - 4f)
            )

            // Migajas / destellos de sabor alrededor de la boca
            for (i in 0..3) {
                val angle = rad * 2 + i * 1.57
                val crumbX = centerX + 20f * cos(angle).toFloat()
                val crumbY = centerY + 6f + 14f * sin(angle).toFloat()
                drawCircle(color = foodColor, radius = 3f, center = Offset(crumbX, crumbY))
            }
        }

        PetActionState.DRINKING -> {
            // Burbujas refrescantes subiendo
            for (i in -1..1) {
                val bubbleX = centerX + i * 18f
                val bubbleY = centerY - 10f - 18f * kotlin.math.abs(sin(rad + i).toFloat())
                drawCircle(
                    color = Color(0xFF4FC3F7).copy(alpha = 0.85f),
                    radius = 5.5f,
                    center = Offset(bubbleX, bubbleY)
                )
            }
        }

        PetActionState.PLAYING, PetActionState.SHAKING, PetActionState.EVOLVING -> {
            // Estrellas y partículas de alegría alrededor de la mascota
            for (i in 0..5) {
                val angle = rad * 2 + (i * Math.PI / 3.0)
                val dist = 42f + 10f * sin(rad * 3).toFloat()
                val px = centerX + dist * cos(angle).toFloat()
                val py = centerY + dist * sin(angle).toFloat()
                drawCircle(
                    color = if (i % 2 == 0) Color(0xFFFFD54F) else Color(0xFFFF4081),
                    radius = 4.5f,
                    center = Offset(px, py)
                )
            }
        }

        PetActionState.SLEEPING -> {
            // Burbujas de sueño flotantes
            for (i in 1..3) {
                val zX = centerX + 18f + i * 9f
                val zY = centerY - 18f - i * 11f + 4f * sin(rad + i).toFloat()
                drawCircle(
                    color = Color(0xFFB3E5FC).copy(alpha = 0.75f),
                    radius = 2.5f + i * 1.2f,
                    center = Offset(zX, zY)
                )
            }
        }

        else -> {}
    }
}
