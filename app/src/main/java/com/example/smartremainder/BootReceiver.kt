package com.example.smartremainder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device reboot detected. Rescheduling medication alarms...")

            val dbHelper = DatabaseHelper(context)
            val alarmScheduler = AlarmScheduler(context)

            // Retrieve user email from SharedPreferences (to support per-user scheduling)
            val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userEmail = prefs.getString("email", null)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Fetch medications for the current user or all as fallback
                    val medications = if (!userEmail.isNullOrEmpty()) {
                        dbHelper.getAllMedications(userEmail)
                    } else {
                        dbHelper.getAllMedications()
                    }

                    // Reschedule all active medications
                    for (medication in medications) {
                        if (medication.reminderEnabled) {
                            alarmScheduler.schedule(medication)
                        }
                    }

                    Log.d("BootReceiver", "Rescheduled ${medications.size} medication alarms successfully.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error while rescheduling alarms: ${e.message}", e)
                }
            }
        }
    }
}
