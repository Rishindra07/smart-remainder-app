package com.example.smartremainder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(NotificationHelper.EXTRA_MEDICATION_ID, -1L)
        if (medicationId == -1L) return

        val dbHelper = DatabaseHelper(context)
        val notificationHelper = NotificationHelper(context)
        val alarmScheduler = AlarmScheduler(context)
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = prefs.getString("email", null)

        CoroutineScope(Dispatchers.IO).launch {
            val medications = if (!userEmail.isNullOrEmpty()) {
                dbHelper.getAllMedications(userEmail)
            } else {
                dbHelper.getAllMedications()
            }

            val medication = medications.find { it.id == medicationId } ?: return@launch
            try {
                val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                val startDate = sdf.parse(medication.startDate)
                val durationDays = medication.duration.split(" ")[0].toIntOrNull() ?: 0
                val isOngoing = medication.duration.equals("Ongoing", true)

                val stillActive = if (startDate != null) {
                    val cal = Calendar.getInstance()
                    cal.time = startDate
                    cal.add(Calendar.DAY_OF_YEAR, durationDays)
                    Date().before(cal.time) || isOngoing
                } else true

                if (stillActive) {
                    notificationHelper.showNotification(medication)
                    alarmScheduler.schedule(medication)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                notificationHelper.showNotification(medication)
                alarmScheduler.schedule(medication)
            }
        }
    }
}
