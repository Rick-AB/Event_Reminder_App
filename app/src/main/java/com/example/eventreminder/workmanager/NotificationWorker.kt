package com.example.eventreminder.workmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.eventreminder.R
import com.example.eventreminder.screens.MainActivity
import com.example.eventreminder.utils.Constants

class NotificationWorker(appContext: Context, workParams: WorkerParameters) :
    Worker(appContext, workParams) {
    val TAG = "com.example.eventreminder.workmanager"
    val NOTIFICATION_ID = "_event_reminder_notification_id"
    val NOTIFICATION_NAME = "_event_reminder"
    val NOTIFICATION_CHANNEL = "_event_reminder_channel_01"

    override fun doWork(): Result {
        val id = inputData.getInt("id", 0)
        val title = inputData.getString("event_title")
        val date = inputData.getString("event_date")
        val daysLeft = Constants.getDaysLeft(date!!)
        sendNotification(id, title, date, daysLeft)
        return Result.success()
    }

    private fun sendNotification(id: Int, title: String?, date: String?, daysLeft: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setContentTitle(title)
            .setContentText("""The event "$title" is scheduled for $date""")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("""The event "$title" is scheduled for $date. That is $daysLeft days from today. So excited!""")
            )
            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIFICATION_ID)

            val descriptionText = "Notification for events"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(
                NOTIFICATION_ID,
                NOTIFICATION_NAME,
                importance
            ).apply { description = descriptionText }

            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(id, builder.build())
        }
    }
}