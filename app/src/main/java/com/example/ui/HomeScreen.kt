package com.example.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.style.TextDecoration
import com.example.data.Task
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color

import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey

import android.media.RingtoneManager
import android.net.Uri
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

enum class NavigationItem {
    HOME, TASKS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks = viewModel.pagedTasks.collectAsLazyPagingItems()
    val allTasksIncludingCompleted by viewModel.allTasksIncludingCompleted.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val h2NotificationEnabled by viewModel.h2NotificationEnabled.collectAsState()
    val notificationSoundUri by viewModel.notificationSoundUri.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(NavigationItem.HOME) }

    val ringtoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            viewModel.setNotificationSoundUri(uri?.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jadwal Tugas", style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Pengaturan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentScreen == NavigationItem.HOME,
                    onClick = { currentScreen = NavigationItem.HOME }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Tasks") },
                    label = { Text("Tasks") },
                    selected = currentScreen == NavigationItem.TASKS,
                    onClick = { currentScreen = NavigationItem.TASKS }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.size(72.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Tugas", modifier = Modifier.size(36.dp))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (currentScreen) {
                NavigationItem.HOME -> {
                    // Extracting only the tasks for the dashboard
                    val activeTasks = allTasksIncludingCompleted.filter { !it.isCompleted }
                    DashboardScreen(
                        tasks = activeTasks,
                        allTasksIncludingCompleted = allTasksIncludingCompleted,
                        onViewAllTasks = { currentScreen = NavigationItem.TASKS }
                    )
                }
                NavigationItem.TASKS -> {
                    TaskListScreen(
                        tasks = tasks,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onToggleComplete = { viewModel.updateTask(it) },
                        onDelete = { viewModel.deleteTask(it) }
                    )
                }
            }
        }

        if (showAddDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showAddDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
            ) {
                AddTaskScreen(
                    onDismiss = { showAddDialog = false },
                    onSave = { title, desc, subject, category, priority, deadline ->
                        viewModel.addTask(title, desc, subject, category, priority, deadline)
                        showAddDialog = false
                    }
                )
            }
        }
        }

        if (showSettingsDialog) {
            val context = LocalContext.current
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Pengaturan & Widget", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Notifikasi H-2 Tugas")
                            Switch(
                                checked = h2NotificationEnabled,
                                onCheckedChange = { viewModel.setH2NotificationEnabled(it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Suara Notifikasi")
                                val currentSoundName = if (notificationSoundUri == null) "Default" else "Kustom"
                                Text(currentSoundName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = {
                                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                    val currentUri = notificationSoundUri?.let { Uri.parse(it) }
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
                                }
                                ringtoneLauncher.launch(intent)
                            }) {
                                Text("Pilih")
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text(
                            text = "Pasang Widget di Layar Utama", 
                            fontWeight = FontWeight.Bold, 
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Pilih ukuran widget yang ingin ditampilkan di layar utama Anda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Button(
                            onClick = { pinWidget(context, com.example.widget.SmallTaskWidgetReceiver::class.java) }, 
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        ) {
                            Text("Widget Kecil (2x2)")
                        }
                        Button(
                            onClick = { pinWidget(context, com.example.widget.MediumTaskWidgetReceiver::class.java) }, 
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        ) {
                            Text("Widget Sedang (4x2)")
                        }
                        Button(
                            onClick = { pinWidget(context, com.example.widget.LargeTaskWidgetReceiver::class.java) }, 
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        ) {
                            Text("Widget Besar (4x4)")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("Tutup")
                    }
                }
            )
        }
    }

fun pinWidget(context: android.content.Context, receiverClass: Class<*>) {
    val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
    val myProvider = android.content.ComponentName(context, receiverClass)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
        appWidgetManager.requestPinAppWidget(myProvider, null, null)
    } else {
        android.widget.Toast.makeText(
            context, 
            "Perangkat ini tidak mendukung pemasangan widget otomatis.", 
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskCard(task: Task, onToggleComplete: (Task) -> Unit, onDelete: (Task) -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd) {
                onToggleComplete(task.copy(isCompleted = !task.isCompleted))
                return@rememberSwipeToDismissBoxState false // Do not actually dismiss the UI
            }
            if (it == SwipeToDismissBoxValue.EndToStart) {
                showDeleteConfirm = true
                return@rememberSwipeToDismissBoxState false // Do not actually dismiss the UI
            }
            false
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Tugas") },
            text = { Text("Apakah Anda yakin ingin menghapus tugas ini?") },
            confirmButton = {
                Button(onClick = {
                    onDelete(task)
                    showDeleteConfirm = false
                }) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val isStartToEnd = dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd
            val isEndToStart = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
            val color = if (isStartToEnd) MaterialTheme.colorScheme.primaryContainer else if (isEndToStart) MaterialTheme.colorScheme.errorContainer else Color.Transparent
            
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = if (isStartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (isStartToEnd) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Selesai", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                } else if (isEndToStart) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    ) {
        TaskCard(task = task, onToggleComplete = onToggleComplete, onDeleteClick = { showDeleteConfirm = true })
    }
}

@Composable
fun TaskCard(task: Task, onToggleComplete: (Task) -> Unit, onDeleteClick: () -> Unit) {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = formatter.format(Date(task.deadline))

    val priorityColor = when (task.priority) {
        "Tinggi" -> Color(0xFFBA1A1A) // error / red
        "Sedang" -> Color(0xFFFBBC00) // yellow/amber
        else -> Color(0xFFBAC3FF) // primary fixed dim
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = null
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(priorityColor)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = task.title, 
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp, bottom = 16.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(text = task.subject, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(dateString, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(priorityColor.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = priorityColor)
                            Text(task.priority, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = priorityColor)
                        }
                    }
                }
            }
        }
    }
}

