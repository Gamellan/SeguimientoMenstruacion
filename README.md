# Seguimiento Menstrual (Android)

Aplicación Android nativa (Kotlin + Jetpack Compose) para registrar ciclos menstruales de forma local en el dispositivo.

## Funcionalidades MVP

- Registro de fecha de inicio y fin.
- Registro de cantidad de sangre (ligero, medio, abundante).
- Registro de síntomas, nivel de dolor (1-10) y notas.
- Historial de ciclos guardados.
- Predicción de próxima menstruación.
- Estimación de ovulación (14 días antes de la siguiente menstruación estimada).
- **Privacidad:** datos guardados solo en local con Room (sin nube, sin login).

## Estructura principal

- `app/src/main/java/com/seguimiento/menstruacion/MainActivity.kt`: entrada de la app.
- `app/src/main/java/com/seguimiento/menstruacion/ui/`: pantalla principal y ViewModel.
- `app/src/main/java/com/seguimiento/menstruacion/data/`: entidades Room, DAO, base de datos y repositorio.

## Requisitos

- Android Studio (versión reciente, con soporte para AGP 8.x).
- JDK 17.
- SDK de Android instalado (minSdk 26, targetSdk 35).

## Cómo ejecutar

1. Abre la carpeta del proyecto en Android Studio.
2. Espera la sincronización de Gradle.
3. Ejecuta el módulo `app` en emulador o dispositivo físico.

## Notas de predicción

- Si hay menos de 2 registros, se usa ciclo por defecto de 28 días.
- Con 2 o más registros, se calcula una media de días entre fechas de inicio de periodos.
- La ovulación estimada se calcula restando 14 días a la próxima menstruación estimada.
