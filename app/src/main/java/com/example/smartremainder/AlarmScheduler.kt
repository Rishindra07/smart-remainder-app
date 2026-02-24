package com.example.smartremainder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(medication: Medication) {
        try {
            val timeParts = medication.time.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // ✅ Ensure alarm is always scheduled for the future
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            // ✅ Intent for AlarmReceiver
            val intent = Intent("com.example.smartremainder.ALARM_TRIGGERED").apply {
                setClass(context, AlarmReceiver::class.java)
                putExtra(NotificationHelper.EXTRA_MEDICATION_ID, medication.id)
            }

            // ✅ Unique request code — prevents overwriting of multiple alarms
            val requestCode = generateUniqueRequestCode(medication.id, calendar)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // ✅ Handle exact alarm permission for Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    Log.w("AlarmScheduler", "⚠️ Requesting SCHEDULE_EXACT_ALARM permission.")
                    return
                }
            }

            // ✅ Schedule the alarm exactly, even under Doze mode
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Log.d(
                "AlarmScheduler",
                "✅ Alarm scheduled for ${calendar.time} — [${medication.name}] (requestCode=$requestCode)"
            )

        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "⚠️ SecurityException: ${e.message}")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "❌ Failed to schedule alarm: ${e.message}")
        }
    }

    fun cancelAlarm(medicationId: Long, hour: Int? = null, minute: Int? = null) {
        try {
            val intent = Intent("com.example.smartremainder.ALARM_TRIGGERED").apply {
                setClass(context, AlarmReceiver::class.java)
            }

            // ✅ Use same request code logic used for scheduling
            val calendar = Calendar.getInstance().apply {
                if (hour != null && minute != null) {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
            }
            val requestCode = generateUniqueRequestCode(medicationId, calendar)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            Log.d("AlarmScheduler", "⏹️ Alarm canceled for ID=$medicationId (requestCode=$requestCode)")

        } catch (e: Exception) {
            Log.e("AlarmScheduler", "❌ Failed to cancel alarm: ${e.message}")
        }
    }

    // ✅ Helper to generate a truly unique request code per alarm
    private fun generateUniqueRequestCode(id: Long, calendar: Calendar): Int {
        // Combines ID + hour + minute (ensures uniqueness even for same medicine multiple times/day)
        return (id * 10000 + calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE)).toInt()
    }
}
