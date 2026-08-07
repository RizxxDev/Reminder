package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY deadline ASC")
    fun getAllTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY deadline ASC")
    fun getPagedTasks(query: String): androidx.paging.PagingSource<Int, Task>

    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    fun getAllTasksIncludingCompleted(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY deadline ASC, CASE priority WHEN 'Tinggi' THEN 1 WHEN 'Sedang' THEN 2 WHEN 'Rendah' THEN 3 ELSE 4 END ASC LIMIT 5")
    fun getUpcomingTasksWidget(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND deadline > :windowStart AND deadline <= :windowEnd ORDER BY CASE priority WHEN 'Tinggi' THEN 1 WHEN 'Sedang' THEN 2 WHEN 'Rendah' THEN 3 ELSE 4 END ASC, deadline ASC LIMIT 5")
    fun getTasksForNotification(windowStart: Long, windowEnd: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND deadline > :currentTime ORDER BY deadline ASC LIMIT 1")
    fun getNextTask(currentTime: Long): Flow<Task?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}
