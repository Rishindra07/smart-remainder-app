package com.example.smartremainder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val medicationId = intent.getLongExtra(NotificationHelper.EXTRA_MEDICATION_ID, -1L)
        if (medicationId == -1L) return

        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 0)
        val dbHelper = DatabaseHelper(context)
        val notificationHelper = NotificationHelper(context)

        when (action) {
            "TAKEN" -> {
                dbHelper.addHistoryEvent(medicationId, "Taken")
                Toast.makeText(context, "Marked as Taken ✅", Toast.LENGTH_SHORT).show()
                notificationHelper.cancelNotification(notificationId)
                sendUpdate(context)
            }
            "SKIPPED" -> {
                dbHelper.addHistoryEvent(medicationId, "Skipped")
                Toast.makeText(context, "Skipped ❌", Toast.LENGTH_SHORT).show()
                notificationHelper.cancelNotification(notificationId)
                sendUpdate(context)
            }
        }
    }

    private fun sendUpdate(context: Context) {
        val intent = Intent("com.example.smartremainder.MEDICATION_UPDATE")
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }
}
