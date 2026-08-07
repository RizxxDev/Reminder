package com.example.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.util.NotificationHelper

class TaskReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val taskId = inputData.getInt("TASK_ID", -1)
        val title = inputData.getString("TASK_TITLE") ?: "Tugas"
        val subject = inputData.getString("TASK_SUBJECT") ?: "Mata Pelajaran"

        if (taskId != -1) {
            NotificationHelper.showNotification(
                applicationContext,
                taskId,
                "Pengingat Tugas",
                "Jangan lupa, tugas $subject: $title dikumpulkan sebentar lagi!"
            )
        }

        return Result.success()
    }
}
