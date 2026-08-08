package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.SettingsRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.firstOrNull
import androidx.core.app.NotificationCompat
import android.app.NotificationManager

class DailyTaskReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val settingsRepo = SettingsRepository(applicationContext)
        val isEnabled = settingsRepo.h2NotificationEnabled.firstOrNull() ?: true
        val soundUri = settingsRepo.notificationSoundUri.firstOrNull()

        if (!isEnabled) {
            return Result.success()
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val taskDao = database.taskDao()
        
        val currentTime = System.currentTimeMillis()
        val twoDaysInMillis = 2L * 24 * 60 * 60 * 1000
        val targetTime = currentTime + twoDaysInMillis

        // Let's say H-2 means deadline is between now+24h and now+48h, or simply check tasks whose deadline is roughly in 2 days.
        // Actually, since it runs every 24h, let's query tasks due between targetTime - 12 hours and targetTime + 12 hours, or just > currentTime + 24h and <= currentTime + 48h.
        val windowStart = currentTime + (24L * 60 * 60 * 1000)
        val windowEnd = currentTime + (48L * 60 * 60 * 1000)

        // Wait, the prompt says "due in exactly 48 hours". A daily check might miss things if not careful, but let's query tasks with deadline between 24h and 48h from now.
        // Or between now + 24h to now + 48h
        val upcomingTasksFlow = taskDao.getTasksForNotification(windowStart, windowEnd)
        val upcomingTasks = upcomingTasksFlow.firstOrNull() ?: emptyList()

        if (upcomingTasks.isNotEmpty()) {
            // Bundle up to 5 tasks
            val topTasks = upcomingTasks.take(5)
            
            // Format message
            val inboxStyle = NotificationCompat.InboxStyle()
            inboxStyle.setBigContentTitle("Tugas Mendekati Deadline (H-2)")
            
            val summaryText = StringBuilder()
            topTasks.forEach { task ->
                val priorityIndicator = when (task.priority) {
                    "Tinggi" -> "🔴"
                    "Sedang" -> "🟠"
                    else -> "🟢"
                }
                val line = "$priorityIndicator ${task.subject}: ${task.title}"
                inboxStyle.addLine(line)
                summaryText.append(line).append(", ")
            }
            
            val fallbackMessage = summaryText.removeSuffix(", ").toString()

            NotificationHelper.showBundledNotification(
                applicationContext,
                1001, // arbitrary fixed ID for summary
                "Pengingat H-2",
                fallbackMessage,
                inboxStyle,
                soundUri
            )
        } else {
            NotificationHelper.cancelBundledNotification(applicationContext, 1001)
        }

        return Result.success()
    }
}
