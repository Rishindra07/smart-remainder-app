package com.example.smartremainder

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    companion object {
        const val EXTRA_MEDICATION_ID = "MEDICATION_ID"
        const val EXTRA_NOTIFICATION_ID = "NOTIFICATION_ID"
        private const val CHANNEL_ID = "medication_reminder_channel"
        private const val CHANNEL_NAME = "Medication Reminders"
    }

    private val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Medication reminder alarms"
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true)
                setSound(soundUri, attrs)
            }

            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(medication: Medication) {
        val id = medication.id.toInt()

        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "TAKEN"
            putExtra(EXTRA_MEDICATION_ID, medication.id)
            putExtra(EXTRA_NOTIFICATION_ID, id)
        }
        val takenPending = PendingIntent.getBroadcast(
            context, id + 1, takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "SKIPPED"
            putExtra(EXTRA_MEDICATION_ID, medication.id)
            putExtra(EXTRA_NOTIFICATION_ID, id)
        }
        val skipPending = PendingIntent.getBroadcast(
            context, id + 2, skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mainIntent = Intent(context, MainActivity::class.java)
        val mainPending = PendingIntent.getActivity(
            context, id, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle("Medication Reminder 💊")
            .setContentText("Time to take ${medication.name} (${medication.dosage})")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .setContentIntent(mainPending)
            .addAction(R.drawable.ic_close, "Taken", takenPending)
            .addAction(R.drawable.ic_close, "Skipped", skipPending)
            .setFullScreenIntent(mainPending, true)

        manager.notify(id, builder.build())
    }

    fun cancelNotification(id: Int) {
        manager.cancel(id)
    }
}
