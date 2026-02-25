package com.seguimiento.menstruacion.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.seguimiento.menstruacion.data.PeriodPredictions
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderScheduler(private val context: Context) {

    fun schedule(predictions: PeriodPredictions) {
        cancelAll()
        scheduleIfFuture(
            requestCode = REQUEST_PERIOD,
            date = predictions.nextPeriodDate,
            title = "Recordatorio de ciclo",
            message = "Se acerca tu próxima menstruación estimada."
        )
        scheduleIfFuture(
            requestCode = REQUEST_OVULATION,
            date = predictions.ovulationDate,
            title = "Recordatorio de ovulación",
            message = "Hoy es tu día estimado de ovulación."
        )
    }

    fun cancelAll() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(REQUEST_PERIOD, "", ""))
        alarmManager.cancel(buildPendingIntent(REQUEST_OVULATION, "", ""))
        alarmManager.cancel(buildPendingIntent(REQUEST_ONGOING_DAILY, "", ""))
    }

    fun scheduleDailyOngoingReminder() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis = nextDailyTriggerMillis(hour = 20, minute = 0)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            AlarmManager.INTERVAL_DAY,
            buildPendingIntent(
                REQUEST_ONGOING_DAILY,
                "¿Cómo va tu periodo?",
                "Actualiza síntomas, dolor o fecha de fin si ya terminó."
            )
        )
    }

    fun cancelDailyOngoingReminder() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(REQUEST_ONGOING_DAILY, "", ""))
    }

    private fun scheduleIfFuture(requestCode: Int, date: LocalDate?, title: String, message: String) {
        date ?: return
        val triggerMillis = LocalDateTime.of(date, LocalTime.of(9, 0))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerMillis <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            buildPendingIntent(requestCode, title, message)
        )
    }

    private fun buildPendingIntent(requestCode: Int, title: String, message: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(EXTRA_TITLE, title)
            .putExtra(EXTRA_MESSAGE, message)

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextDailyTriggerMillis(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        var candidate = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!candidate.isAfter(now)) {
            candidate = candidate.plusDays(1)
        }
        return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    companion object {
        private const val REQUEST_PERIOD = 1001
        private const val REQUEST_OVULATION = 1002
        private const val REQUEST_ONGOING_DAILY = 1003
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
    }
}
