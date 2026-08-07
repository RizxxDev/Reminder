package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SettingsRepository
import com.example.data.Task
import com.example.data.TaskRepository
import com.example.util.NotificationHelper
import com.example.widget.SmallTaskWidget
import com.example.widget.MediumTaskWidget
import com.example.widget.LargeTaskWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    private val settingsRepository: SettingsRepository
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val allTasksIncludingCompleted: StateFlow<List<Task>>
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val pagedTasks: Flow<PagingData<Task>> = _searchQuery.flatMapLatest { query ->
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { repository.getPagedTasks(query) }
        ).flow
    }.cachedIn(viewModelScope)
    
    val h2NotificationEnabled: StateFlow<Boolean>

    init {
        val taskDao = AppDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        settingsRepository = SettingsRepository(application)
        
        h2NotificationEnabled = settingsRepository.h2NotificationEnabled.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

        allTasksIncludingCompleted = repository.allTasksIncludingCompleted.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setH2NotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setH2NotificationEnabled(enabled)
            NotificationHelper.refreshBundledNotification(getApplication())
        }
    }

    fun addTask(title: String, description: String, subject: String, category: String, priority: String, deadline: Long) {
        viewModelScope.launch {
            val task = Task(title = title, description = description, subject = subject, category = category, priority = priority, deadline = deadline)
            val taskId = repository.insert(task).toInt()
            
            NotificationHelper.refreshBundledNotification(getApplication())
            updateWidget()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.update(task)
            NotificationHelper.cancelNotification(getApplication(), task.id)
            NotificationHelper.refreshBundledNotification(getApplication())
            updateWidget()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
            NotificationHelper.cancelNotification(getApplication(), task.id)
            NotificationHelper.refreshBundledNotification(getApplication())
            updateWidget()
        }
    }

    private suspend fun updateWidget() {
        SmallTaskWidget().updateAll(getApplication())
        MediumTaskWidget().updateAll(getApplication())
        LargeTaskWidget().updateAll(getApplication())
    }
}
