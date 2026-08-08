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

    fun showNotification(context: Context, notificationId: Int, title: String, message: String, soundUri: String? = null) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = if (soundUri == null) "task_reminder_channel_default" else "task_reminder_channel_${soundUri.hashCode()}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for task reminder notifications"
                if (soundUri != null) {
                    val uri = android.net.Uri.parse(soundUri)
                    val audioAttributes = android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(uri, audioAttributes)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (soundUri != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(android.net.Uri.parse(soundUri))
        }

        notificationManager.notify(notificationId, builder.build())
    }

    fun showBundledNotification(
        context: Context,
        notificationId: Int,
        title: String,
        fallbackMessage: String,
        inboxStyle: NotificationCompat.InboxStyle,
        soundUri: String? = null
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = if (soundUri == null) "task_reminder_channel_default" else "task_reminder_channel_${soundUri.hashCode()}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for task reminder notifications"
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                if (soundUri != null) {
                    val uri = android.net.Uri.parse(soundUri)
                    val audioAttributes = android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(uri, audioAttributes)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(fallbackMessage)
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            
        if (soundUri != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(android.net.Uri.parse(soundUri))
        }

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
