package com.example.qloq.presentation

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.preference.PreferenceManager
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.qloq.R

@Composable
fun RenderPet(
    swipeDismissableNavController: NavController,
    qloSprite: QloSprite,
    sharedPreferences: SharedPreferences
) {
    val context = LocalContext.current

    // Conecta Acelerómetro (Tilt + Shake), Podómetro (Pasos), Pulso (BPM) y Luz (Modo Noche)
    BindWatchSensors(qloSprite = qloSprite, sharedPreferences = sharedPreferences)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1B4B))
    ) {
        // Escenario animado a 60 FPS con crecimiento por etapa y física de sensores
        AnimatedPetStage(
            qloSprite = qloSprite,
            onPetTapped = {
                triggerWatchHaptic(context, 35L)
                qloSprite.incHappiness()
                qloSprite.saveToPreferences(sharedPreferences)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Cabecera superior adaptada a carátula circular de Wear OS
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.width(138.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HappinessIndicator(qloSprite)

                // Etiqueta de Nivel y Etapa de Evolución
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable {
                            swipeDismissableNavController.navigate("DetailsScreen")
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Nv.${qloSprite.level} ${qloSprite.stage.title}",
                        color = Color(0xFFFFD54F),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "XP ${qloSprite.xpInCurrentLevel}/100 · ${(qloSprite.growthScale * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                DetailsButtonComposable(swipeDismissableNavController)
            }
        }

        // Cintillo informativo de acciones y sensores en tiempo real
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = qloSprite.statusBanner,
                    color = Color(0xFFFFF59D),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Pasos:${qloSprite.stepsCount} · ♥${qloSprite.heartRateBpm} · Comidas:${qloSprite.mealsEaten}",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }

        // Botonera inferior de interacción rápida
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Comer (crece y rota platillos)
            OutlinedButton(
                onClick = {
                    triggerWatchHaptic(context, 40L)
                    qloSprite.feedNextFood()
                    qloSprite.saveToPreferences(sharedPreferences)
                },
                colors = ButtonDefaults.buttonColors(Color(0xFFFFF8E1)),
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.burger_solid),
                    contentDescription = "Alimentar mascota",
                    modifier = Modifier.size(17.dp),
                    tint = Color(0xFFE65100)
                )
            }

            // 2. Beber (hidrata y da energía)
            OutlinedButton(
                onClick = {
                    triggerWatchHaptic(context, 35L)
                    qloSprite.incThirst()
                    qloSprite.saveToPreferences(sharedPreferences)
                },
                colors = ButtonDefaults.buttonColors(Color(0xFFE1F5FE)),
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.mug_hot_solid),
                    contentDescription = "Dar bebida",
                    modifier = Modifier.size(17.dp),
                    tint = Color(0xFF0277BD)
                )
            }

            // 3. Jugar / Cosquillas
            OutlinedButton(
                onClick = {
                    triggerWatchHaptic(context, 35L)
                    qloSprite.incHappiness()
                    qloSprite.saveToPreferences(sharedPreferences)
                },
                colors = ButtonDefaults.buttonColors(Color(0xFFFCE4EC)),
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.hand_regular),
                    contentDescription = "Acariciar y jugar",
                    modifier = Modifier.size(17.dp),
                    tint = Color(0xFFC2185B)
                )
            }

            // 4. Acrobacia de Sensor (simula o activa giro acrobático)
            OutlinedButton(
                onClick = {
                    triggerWatchHaptic(context, 60L)
                    qloSprite.onWatchShaken()
                    qloSprite.saveToPreferences(sharedPreferences)
                },
                colors = ButtonDefaults.buttonColors(Color(0xFFEDE7F6)),
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.heart_solid),
                    contentDescription = "Acrobacia de sensor",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF6A1B9A)
                )
            }
        }
    }
}

@Composable
fun HappinessIndicator(qloSprite: QloSprite) {
    val avgHealth = qloSprite.getAverage()

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (avgHealth > 60) {
            Icon(
                painter = painterResource(id = R.drawable.face_laugh_squint_regular),
                contentDescription = "Feliz",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF2E7D32)
            )
        } else if (avgHealth > 35) {
            Icon(
                painter = painterResource(id = R.drawable.face_meh_regular),
                contentDescription = "Normal",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFEF6C00)
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.face_sad_cry_regular),
                contentDescription = "Triste",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFC62828)
            )
        }
    }
}

@Composable
fun DetailsButtonComposable(swipeDismissableNavController: NavController) {
    OutlinedButton(
        onClick = { swipeDismissableNavController.navigate("DetailsScreen") },
        colors = ButtonDefaults.buttonColors(Color.White),
        modifier = Modifier.size(28.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.bars_solid),
            contentDescription = "Ver detalles y sensores",
            modifier = Modifier.size(15.dp),
            tint = Color(0xFF4A148C)
        )
    }
}

@Preview(device = Devices.WEAR_OS_LARGE_ROUND, showSystemUi = true)
@Composable
fun PetPreview() {
    val swipeDismissableNavController = rememberSwipeDismissableNavController()
    val context = LocalContext.current
    val sprite = QloSprite("Qlo", 65, 65, 75)

    RenderPet(
        swipeDismissableNavController,
        sprite,
        PreferenceManager.getDefaultSharedPreferences(context)
    )
}
