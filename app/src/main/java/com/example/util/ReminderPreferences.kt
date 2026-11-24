package com.example.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages opt-in daily reading and festival reminder preferences with quiet hours.
 * All reminders are strictly opt-in, non-intrusive, and easy to disable at any time.
 */
class ReminderPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isDailyReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_DAILY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_DAILY_ENABLED, value).apply()

    var dailyReminderTime: String
        get() = prefs.getString(KEY_DAILY_TIME, "07:00") ?: "07:00"
        set(value) = prefs.edit().putString(KEY_DAILY_TIME, value).apply()

    var isFestivalReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_FESTIVAL_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_FESTIVAL_ENABLED, value).apply()

    var isQuietHoursEnabled: Boolean
        get() = prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_QUIET_HOURS_ENABLED, value).apply()

    var quietHoursStart: String
        get() = prefs.getString(KEY_QUIET_START, "22:00") ?: "22:00"
        set(value) = prefs.edit().putString(KEY_QUIET_START, value).apply()

    var quietHoursEnd: String
        get() = prefs.getString(KEY_QUIET_END, "07:00") ?: "07:00"
        set(value) = prefs.edit().putString(KEY_QUIET_END, value).apply()

    companion object {
        private const val PREFS_NAME = "astroveda_reminder_prefs"
        private const val KEY_DAILY_ENABLED = "daily_reading_enabled"
        private const val KEY_DAILY_TIME = "daily_reading_time"
        private const val KEY_FESTIVAL_ENABLED = "festival_reminder_enabled"
        private const val KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
        private const val KEY_QUIET_START = "quiet_hours_start"
        private const val KEY_QUIET_END = "quiet_hours_end"

        @Volatile
        private var instance: ReminderPreferences? = null

        fun getInstance(context: Context): ReminderPreferences {
            return instance ?: synchronized(this) {
                instance ?: ReminderPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
