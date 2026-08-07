package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allTasksIncludingCompleted: Flow<List<Task>> = taskDao.getAllTasksIncludingCompleted()

    fun getPagedTasks(query: String): androidx.paging.PagingSource<Int, Task> = taskDao.getPagedTasks(query)

    fun getNextTask(currentTime: Long): Flow<Task?> = taskDao.getNextTask(currentTime)

    suspend fun insert(task: Task): Long = taskDao.insertTask(task)

    suspend fun update(task: Task) = taskDao.updateTask(task)

    suspend fun delete(task: Task) = taskDao.deleteTask(task)
}
