package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worker.DailyTaskReminderWorker
import com.example.worker.TaskReminderWorker
import java.util.concurrent.TimeUnit

object NotificationHelper {
    private const val CHANNEL_ID = "task_reminder_channel"

    fun showNotification(context: Context, notificationId: Int, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for task reminder notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }

    fun showBundledNotification(
        context: Context,
        notificationId: Int,
        title: String,
        fallbackMessage: String,
        inboxStyle: NotificationCompat.InboxStyle
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for task reminder notifications"
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(fallbackMessage)
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)

        notificationManager.notify(notificationId, builder.build())
    }

    fun scheduleDailyReminder(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<DailyTaskReminderWorker>(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailyTaskReminderWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun refreshBundledNotification(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<DailyTaskReminderWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "RefreshTaskReminderWorker",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun scheduleNotification(context: Context, taskId: Int, title: String, subject: String, deadlineMillis: Long) {
        // We will no longer schedule individual notifications for H-2, as DailyTaskReminderWorker handles this.
        // We can leave this empty or use it for exact deadline reminders if needed.
    }

    fun cancelNotification(context: Context, taskId: Int) {
        // WorkManager.getInstance(context).cancelAllWorkByTag("task_$taskId")
    }

    fun cancelBundledNotification(context: Context, notificationId: Int = 1001) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}
