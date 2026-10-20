# QLOQ 2.0 — Mascota Virtual Interactiva para Smartwatch (Wear OS)

Mascota virtual interactiva tipo Tamagotchi desarrollada en **Kotlin** y **Jetpack Compose for Wear OS**, diseñada específicamente para pantallas circulares de relojes inteligentes.

---

## Características Principales

### 1. Animaciones Procedurales a 60 FPS en Compose Canvas
- **Respiración y rebote continuo (*squash & stretch*):** la mascota tiene movimiento orgánico en todo momento.
- **Parpadeo y miradas dinámicas:** las pupilas reaccionan y siguen la inclinación de la muñeca.
- **Animación interactiva de comida y bebida:** al alimentar a la mascota (hamburguesa, manzana, taco o baya dorada), la comida vuela hacia su boca, mastica rítmicamente abriendo y cerrando la boca, suelta migajas de colores y su estómago crece.

### 2. Sistema de Crecimiento y 4 Etapas de Evolución
Conforme alimentas, juegas y caminas con tu reloj, la mascota gana **XP** y **aumenta su tamaño físico (`growthScale`)**:
- **Nivel 1 — Bebé Retoño (Escala 78%):** cuerpo pequeño con tierna antena de hoja en la cabeza.
- **Nivel 2 — Joven Saltarín (Escala 100%):** le crecen orejas/cuernitos y mejillas sonrosadas.
- **Nivel 3 — Adulto Alado (Escala 122%):** desarrolla alas laterales que aletean continuamente.
- **Nivel 4+ — Maestro Cósmico (Escala 142%+):** corona dorada flotante y aura luminosa pulsante.

### 3. Integración con Sensores del Reloj Inteligente
- **Acelerómetro (`TYPE_ACCELEROMETER`):**
  - **Inclinación de muñeca:** la mascota se desliza e inclina por la pantalla según la gravedad del reloj.
  - **Sacudida del reloj (Shake):** al agitar el reloj, la mascota realiza un giro acrobático de 360°, vibra mediante retroalimentación háptica y gana +25 XP.
- **Podómetro / Contador de Pasos (`TYPE_STEP_COUNTER` / `TYPE_STEP_DETECTOR`):**
  - Al caminar con el reloj puesto, la mascota se coloca una banda deportiva, levanta polvo al correr y suma pasos y XP.
- **Sensor de Luz Ambiental (`TYPE_LIGHT`):**
  - Al cubrir el reloj o entrar en un entorno oscuro (< 15 lux), el fondo cambia automáticamente a **noche estrellada** y la mascota duerme (Zzz) recuperando energía.
- **Sensor de Ritmo Cardíaco (`TYPE_HEART_RATE`):**
  - Sincroniza la frecuencia de pulso del aura con tus BPM reales.

---

## Requisitos de Construcción
- **Android Studio** (Koala / Ladybug o superior)
- **SDK de Android:** `compileSdk 35`, `minSdk 30` (Wear OS 3.0+)
- **Gradle:** 9.6.0 + Android Gradle Plugin 9.4.1 + Kotlin 2.2.10

---

## Cómo Ejecutarlo
1. Clona o abre esta carpeta en **Android Studio**.
2. Selecciona un dispositivo virtual o físico de **Wear OS** (ej. *Wear OS Large Round*).
3. Haz clic en **Run (▶)** para instalar y ejecutar.
4. En el emulador puedes probar los sensores desde **Extended Controls (...) → Virtual Sensors**, o usar los botones de prueba rápida en la pantalla de detalles de la app.
