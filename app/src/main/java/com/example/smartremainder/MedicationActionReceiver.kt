package com.example.smartremainder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MedicationActionReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_MEDICATION_ID = "MEDICATION_ID"
        const val EXTRA_ACTION = "ACTION_TYPE"
        const val ACTION_TAKEN = "TAKEN"
        const val ACTION_SKIPPED = "SKIPPED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1)
        val action = intent.getStringExtra(EXTRA_ACTION)

        if (medId != -1L && action != null) {
            val dbHelper = DatabaseHelper(context)
            dbHelper.addHistoryEvent(medId, action)

            // Dismiss the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(medId.toInt()) // Use the same ID to cancel

            // Send broadcast to update UI
            sendUpdateBroadcast(context)
        }
    }

    private fun sendUpdateBroadcast(context: Context) {
        val intent = Intent("com.example.smartremainder.MEDICATION_UPDATE")
        intent.setPackage(context.packageName) // Restrict broadcast to this app
        context.sendBroadcast(intent)
    }
}
