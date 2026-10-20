package com.example.qloq.presentation

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class PetStage(
    val title: String,
    val baseScale: Float,
    val minLevel: Int
) {
    BEBE("Bebé Retoño", 0.78f, 1),
    JOVEN("Joven Saltarín", 1.00f, 2),
    ADULTO("Adulto Alado", 1.22f, 3),
    MAESTRO("Maestro Cósmico", 1.42f, 4);

    companion object {
        fun fromLevel(level: Int): PetStage {
            return when {
                level >= 4 -> MAESTRO
                level == 3 -> ADULTO
                level == 2 -> JOVEN
                else -> BEBE
            }
        }
    }
}

enum class PetActionState {
    IDLE,
    EATING,
    DRINKING,
    PLAYING,
    SHAKING,
    RUNNING,
    SLEEPING,
    EVOLVING
}

enum class FoodType(val label: String, val hungerBoost: Int, val xpBoost: Int, val colorHex: Long) {
    BURGER("Hamburguesa", 18, 25, 0xFFFFB300),
    MANZANA("Manzana Roja", 14, 20, 0xFFE53935),
    TACO("Taco Especial", 22, 30, 0xFFFF7043),
    BAYA_DORADA("Baya Dorada", 30, 50, 0xFFFFD54F)
}

class QloSprite(
    var name: String,
    hunger: Int,
    thirst: Int,
    happiness: Int,
    initialEnergy: Int = 80,
    initialXp: Int = 0,
    initialMeals: Int = 0,
    initialSteps: Int = 0
) {
    var hungerStat by mutableStateOf(hunger.coerceIn(0, 100))
    var thirstStat by mutableStateOf(thirst.coerceIn(0, 100))
    var happinessStat by mutableStateOf(happiness.coerceIn(0, 100))
    var energyStat by mutableStateOf(initialEnergy.coerceIn(0, 100))

    // Crecimiento y evolución
    var xp by mutableStateOf(initialXp.coerceAtLeast(0))
    var mealsEaten by mutableStateOf(initialMeals.coerceAtLeast(0))
    var stepsCount by mutableStateOf(initialSteps.coerceAtLeast(0))

    // Sensores en vivo del smartwatch
    var tiltX by mutableStateOf(0f)
    var tiltY by mutableStateOf(0f)
    var heartRateBpm by mutableStateOf(76)
    var ambientLux by mutableStateOf(120f)
    var shakeCount by mutableStateOf(0)

    // Estado de animación activa
    var actionState by mutableStateOf(PetActionState.IDLE)
    var currentFood by mutableStateOf(FoodType.BURGER)
    var statusBanner by mutableStateOf("¡Hola! Incliname o sacúdeme")
    var actionTimestamp by mutableStateOf(0L)

    private val decrementMultiplier = 0.00005

    val level: Int
        get() = (xp / 100) + 1

    val xpInCurrentLevel: Int
        get() = xp % 100

    val stage: PetStage
        get() = PetStage.fromLevel(level)

    /**
     * Escala física de la mascota: crece cada vez que come y cada vez que evoluciona de etapa.
     */
    val growthScale: Float
        get() {
            val mealBonus = (mealsEaten * 0.015f).coerceAtMost(0.22f)
            return (stage.baseScale + mealBonus).coerceIn(0.75f, 1.58f)
        }

    val isNightOrDark: Boolean
        get() = ambientLux < 15f || actionState == PetActionState.SLEEPING

    fun addXp(amount: Int): Boolean {
        val previousStage = stage
        xp += amount
        val newStage = stage
        return if (newStage != previousStage) {
            actionState = PetActionState.EVOLVING
            actionTimestamp = System.currentTimeMillis()
            statusBanner = "¡EVOLUCIÓN: ${newStage.title}!"
            true
        } else {
            false
        }
    }

    fun feedNextFood() {
        val foods = FoodType.values()
        val nextIndex = (foods.indexOf(currentFood) + 1) % foods.size
        currentFood = foods[nextIndex]
        hungerStat = (hungerStat + currentFood.hungerBoost).coerceAtMost(100)
        happinessStat = (happinessStat + 6).coerceAtMost(100)
        mealsEaten += 1
        actionState = PetActionState.EATING
        actionTimestamp = System.currentTimeMillis()
        val evolved = addXp(currentFood.xpBoost)
        if (!evolved) {
            statusBanner = "Ñam ${currentFood.label} (+${currentFood.xpBoost} XP)"
        }
        checkStats()
    }

    fun incHunger() {
        feedNextFood()
    }

    fun incThirst() {
        thirstStat = (thirstStat + 20).coerceAtMost(100)
        energyStat = (energyStat + 10).coerceAtMost(100)
        actionState = PetActionState.DRINKING
        actionTimestamp = System.currentTimeMillis()
        val evolved = addXp(15)
        if (!evolved) {
            statusBanner = "¡Glup glup! Hidratado (+15 XP)"
        }
        checkStats()
    }

    fun incHappiness() {
        happinessStat = (happinessStat + 16).coerceAtMost(100)
        actionState = PetActionState.PLAYING
        actionTimestamp = System.currentTimeMillis()
        val evolved = addXp(18)
        if (!evolved) {
            statusBanner = "¡Cosquillas! Feliz (+18 XP)"
        }
        checkStats()
    }

    /**
     * Se dispara cuando el usuario sacude el reloj (Acelerómetro).
     */
    fun onWatchShaken() {
        shakeCount += 1
        happinessStat = (happinessStat + 18).coerceAtMost(100)
        energyStat = (energyStat - 4).coerceAtLeast(10)
        actionState = PetActionState.SHAKING
        actionTimestamp = System.currentTimeMillis()
        val evolved = addXp(25)
        if (!evolved) {
            statusBanner = "¡Acrobacia por sensor! (+25 XP)"
        }
        checkStats()
    }

    /**
     * Se dispara cuando el podómetro del reloj detecta pasos reales o simulados.
     */
    fun onStepsDetected(addedSteps: Int = 1) {
        stepsCount += addedSteps
        happinessStat = (happinessStat + 5).coerceAtMost(100)
        hungerStat = (hungerStat - 1).coerceAtLeast(10)
        actionState = PetActionState.RUNNING
        actionTimestamp = System.currentTimeMillis()
        val evolved = addXp(addedSteps * 4)
        if (!evolved) {
            statusBanner = "¡Paseando! $stepsCount pasos"
        }
        checkStats()
    }

    /**
     * Sensor de luz ambiental del reloj: si hay poca luz, duerme y recupera energía.
     */
    fun updateLightSensor(lux: Float) {
        ambientLux = lux
        if (lux < 15f && actionState != PetActionState.SLEEPING) {
            actionState = PetActionState.SLEEPING
            energyStat = (energyStat + 15).coerceAtMost(100)
            statusBanner = "Modo noche (Sensor Luz) Zzz..."
        } else if (lux >= 15f && actionState == PetActionState.SLEEPING) {
            actionState = PetActionState.IDLE
            statusBanner = "¡Despierto con la luz del día!"
        }
    }

    fun toggleSleepManual() {
        if (actionState == PetActionState.SLEEPING) {
            ambientLux = 120f
            actionState = PetActionState.IDLE
            statusBanner = "¡Listo para jugar!"
        } else {
            ambientLux = 5f
            actionState = PetActionState.SLEEPING
            energyStat = (energyStat + 25).coerceAtMost(100)
            statusBanner = "Descansando Zzz (+Energía)"
        }
    }

    fun updateStats(timeDelta: Long) {
        if (timeDelta <= 0) return
        val loss = (timeDelta * decrementMultiplier).toInt()
        if (loss > 0) {
            hungerStat -= loss
            thirstStat -= loss
            happinessStat -= loss
            checkStats()
        }
    }

    fun checkStats() {
        hungerStat = hungerStat.coerceIn(0, 100)
        thirstStat = thirstStat.coerceIn(0, 100)
        happinessStat = happinessStat.coerceIn(0, 100)
        energyStat = energyStat.coerceIn(0, 100)
    }

    fun getAverage(): Int {
        return (happinessStat + thirstStat + hungerStat) / 3
    }

    fun saveToPreferences(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit()
            .putLong("timeStamp", System.currentTimeMillis())
            .putInt("hungerStat", hungerStat)
            .putInt("thirstStat", thirstStat)
            .putInt("happinessStat", happinessStat)
            .putInt("energyStat", energyStat)
            .putInt("xpStat", xp)
            .putInt("mealsEaten", mealsEaten)
            .putInt("stepsCount", stepsCount)
            .apply()
    }
}
