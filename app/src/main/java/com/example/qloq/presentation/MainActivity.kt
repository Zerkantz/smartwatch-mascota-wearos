package com.example.qloq.presentation

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.preference.PreferenceManager
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.qloq.presentation.theme.QLOQTheme
import java.lang.System.currentTimeMillis

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)

        val hungerStat = sharedPreferences.getInt("hungerStat", 60)
        val thirstStat = sharedPreferences.getInt("thirstStat", 60)
        val happinessStat = sharedPreferences.getInt("happinessStat", 70)
        val energyStat = sharedPreferences.getInt("energyStat", 85)
        val xpStat = sharedPreferences.getInt("xpStat", 0)
        val mealsEaten = sharedPreferences.getInt("mealsEaten", 0)
        val stepsCount = sharedPreferences.getInt("stepsCount", 0)

        val sprite = QloSprite(
            name = "Qlo",
            hunger = hungerStat,
            thirst = thirstStat,
            happiness = happinessStat,
            initialEnergy = energyStat,
            initialXp = xpStat,
            initialMeals = mealsEaten,
            initialSteps = stepsCount
        )
        val lastTimeSeen = sharedPreferences.getLong("timeStamp", currentTimeMillis())
        sprite.updateStats(currentTimeMillis() - lastTimeSeen)

        setContent {
            QloqApp(sprite, sharedPreferences)
        }
    }
}

@Composable
fun QloqApp(sprite: QloSprite, sharedPreferences: SharedPreferences) {
    QLOQTheme {
        val swipeDismissableNavController = rememberSwipeDismissableNavController()

        SwipeDismissableNavHost(
            navController = swipeDismissableNavController,
            startDestination = "Landing",
        ) {
            composable("Landing") {
                RenderPet(swipeDismissableNavController, sprite, sharedPreferences)
            }
            composable("DetailsScreen") {
                DetailsScreen(sprite, sharedPreferences)
            }
        }
    }
}

@Preview(device = Devices.WEAR_OS_LARGE_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    val sprite = QloSprite("Qlo", 65, 65, 75)
    val context = LocalContext.current
    QloqApp(sprite, PreferenceManager.getDefaultSharedPreferences(context))
}
