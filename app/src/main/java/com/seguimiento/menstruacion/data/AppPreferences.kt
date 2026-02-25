package com.seguimiento.menstruacion.data

import android.content.Context

class AppPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("period_preferences", Context.MODE_PRIVATE)

    fun isOnboardingCompleted(): Boolean = preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    fun setOnboardingCompleted(completed: Boolean) {
        preferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun isRemindersEnabled(): Boolean = preferences.getBoolean(KEY_REMINDERS_ENABLED, false)

    fun setRemindersEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply()
    }

    fun isAutoNotificationAttempted(): Boolean = preferences.getBoolean(KEY_AUTO_NOTIFICATION_ATTEMPTED, false)

    fun setAutoNotificationAttempted(attempted: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_NOTIFICATION_ATTEMPTED, attempted).apply()
    }

    private companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_AUTO_NOTIFICATION_ATTEMPTED = "auto_notification_attempted"
    }
}
