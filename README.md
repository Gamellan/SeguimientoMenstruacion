# Seguimiento Menstrual (Android)

Aplicación Android nativa (Kotlin + Jetpack Compose) para registrar ciclos menstruales de forma local en el dispositivo.

## Funcionalidades MVP

- Registro de periodo con selector de rango de fechas.
- Opción de periodo en curso (sin fecha de fin).
- Registro de cantidad de sangre (ligero, medio, abundante).
- Registro de síntomas estructurados (chips) + síntomas libres, nivel de dolor (1-10) y notas.
- Historial de ciclos guardados.
- Edición y borrado de registros existentes.
- Predicción de próxima menstruación.
- Estimación de ovulación (14 días antes de la siguiente menstruación estimada).
- Calendario mensual visual (en pantalla principal) con navegación entre meses y marcadores de menstruación, ventana fértil y ovulación estimada.
- Estadísticas: duración media de ciclo, duración media de menstruación, variabilidad y dolor medio.
- Onboarding inicial de 3 pasos para primer uso.
- Sección de configuración para activar/desactivar notificaciones locales.
- Notificación diaria mientras exista un periodo en curso (si notificaciones están activadas).
- Soporte multidioma (inglés por defecto + español, francés, alemán y portugués de Brasil).
- **Privacidad:** datos guardados solo en local con Room (sin nube, sin login).

## Pantallas

- Inicio: calendario, predicciones y accesos rápidos.
- Estadísticas: vista dedicada con métricas del ciclo.
- Configuración: interruptor de notificaciones.
- Crear registro: formulario dedicado.
- Histórico: listado de registros con acceso a edición.
- Editar registro: formulario dedicado para actualizar datos.

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

## Notas de recordatorios

- Los recordatorios usan notificaciones locales del dispositivo (sin servicios externos).
- En Android 13+ la app solicita permiso de notificaciones al activarlos.
- Si hay un periodo en curso, se programa un recordatorio diario para actualizar síntomas/dolor o cerrar el periodo.

## Idiomas

- Idioma base/fallback: inglés.
- Traducciones incluidas: español (`values-es`), francés (`values-fr`), alemán (`values-de`) y portugués Brasil (`values-pt-rBR`).
