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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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

import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey

import android.media.RingtoneManager
import android.net.Uri
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

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
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Tugas")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Summary Card
            SummaryCard(allTasksIncludingCompleted)

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Cari tugas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            if (tasks.itemCount == 0) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada tugas. Yeay!", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        count = tasks.itemCount,
                        key = tasks.itemKey { it.id }
                    ) { index ->
                        val task = tasks[index]
                        if (task != null) {
                            SwipeableTaskCard(
                                task = task,
                                onToggleComplete = { viewModel.updateTask(it) },
                                onDelete = { viewModel.deleteTask(it) }
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, desc, subject, category, priority, deadline ->
                    viewModel.addTask(title, desc, subject, category, priority, deadline)
                    showAddDialog = false
                }
            )
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
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
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
}

fun pinWidget(context: android.content.Context, receiverClass: Class<*>) {
    val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
    val myProvider = android.content.ComponentName(context, receiverClass)
    if (appWidgetManager.isRequestPinAppWidgetSupported) {
        appWidgetManager.requestPinAppWidget(myProvider, null, null)
    } else {
        android.widget.Toast.makeText(
            context, 
            "Perangkat ini tidak mendukung pemasangan widget otomatis.", 
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun SummaryCard(tasks: List<Task>) {
    val completedCount = tasks.count { it.isCompleted }
    val pendingCount = tasks.count { !it.isCompleted }
    
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val tomorrow = today + 86400000
    
    val remainingToday = tasks.count { !it.isCompleted && it.deadline in today until tomorrow }
    val now = Calendar.getInstance()
    val currentWeek = now.get(Calendar.WEEK_OF_YEAR)
    val currentYear = now.get(Calendar.YEAR)
    
    var completedThisWeek = 0
    val weeksWithCompletions = mutableSetOf<String>()
    var totalCompleted = 0
    
    tasks.filter { it.isCompleted }.forEach { task ->
        val taskCal = Calendar.getInstance().apply { timeInMillis = task.deadline }
        val week = taskCal.get(Calendar.WEEK_OF_YEAR)
        val year = taskCal.get(Calendar.YEAR)
        
        weeksWithCompletions.add("$year-$week")
        totalCompleted++
        
        if (week == currentWeek && year == currentYear) {
            completedThisWeek++
        }
    }
    
    val averagePerWeek = if (weeksWithCompletions.isNotEmpty()) {
        totalCompleted.toFloat() / weeksWithCompletions.size
    } else {
        0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ringkasan Mingguan", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tugas tersisa hari ini: $remainingToday", style = MaterialTheme.typography.bodyLarge)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val chartModel = entryModelOf(completedThisWeek.toFloat(), averagePerWeek)
            Chart(
                chart = columnChart(),
                model = chartModel,
                modifier = Modifier.height(100.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Text("Selesai ($completedThisWeek)", style = MaterialTheme.typography.bodySmall)
                Text(java.lang.String.format("Rata-rata Mingguan (%.1f)", averagePerWeek), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskCard(task: Task, onToggleComplete: (Task) -> Unit, onDelete: (Task) -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd || it == SwipeToDismissBoxValue.EndToStart) {
                onToggleComplete(task.copy(isCompleted = !task.isCompleted))
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
            val color = MaterialTheme.colorScheme.primaryContainer
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Tandai ${if (task.isCompleted) "Tertunda" else "Selesai"}")
            }
        }
    ) {
        TaskCard(task = task, onToggleComplete = onToggleComplete, onDeleteClick = { showDeleteConfirm = true })
    }
}

@Composable
fun TaskCard(task: Task, onToggleComplete: (Task) -> Unit, onDeleteClick: () -> Unit) {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateString = formatter.format(Date(task.deadline))

    val priorityColor = when (task.priority) {
        "Tinggi" -> Color(0xFFE57373) // Red
        "Sedang" -> Color(0xFFFFB74D) // Orange
        else -> Color(0xFF81C784) // Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(priorityColor, shape = MaterialTheme.shapes.small)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete(task.copy(isCompleted = it)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${task.subject} • ${task.category}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.title, 
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = task.description, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Deadline: $dateString", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus Tugas", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, String, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var expandedSubject by remember { mutableStateOf(false) }
    val subjects = listOf("Matematika", "Bahasa Indonesia", "Bahasa Inggris", "Bahasa Arab", "PPKn", "Sejarah", "Biologi", "Fisika", "Kimia", "Geografi", "Ekonomi", "Sosiologi", "Seni Budaya", "Penjaskes", "Seni Budaya", "Informatika", "Al-Qur'an Hadits", "Akidah Akhlak", "Fikih", "Bimbingan Konseling", "Muatan Lokal", "Keterampilan")
    
    var deadline by remember { mutableStateOf(System.currentTimeMillis() + 86400000) } // Default tomorrow
    
    var category by remember { mutableStateOf("Kerja Kelompok") }
    var expandedCategory by remember { mutableStateOf(false) }
    val categories = listOf("Kerja Kelompok", "Tugas Pribadi", "Makalah")

    var priority by remember { mutableStateOf("Sedang") }
    var expandedPriority by remember { mutableStateOf(false) }
    val priorities = listOf("Rendah", "Sedang", "Tinggi")

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = deadline

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            deadline = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Tugas Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Tugas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Subject Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedSubject,
                    onExpandedChange = { expandedSubject = !expandedSubject }
                ) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { 
                            subject = it
                            expandedSubject = true
                        },
                        label = { Text("Mata Pelajaran") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubject) },
                        singleLine = true
                    )
                    
                    val filteredSubjects = subjects.filter { it.contains(subject, ignoreCase = true) }
                    
                    if (filteredSubjects.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expandedSubject,
                            onDismissRequest = { expandedSubject = false }
                        ) {
                            filteredSubjects.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        subject = selectionOption
                                        expandedSubject = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    category = selectionOption
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                // Priority Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedPriority,
                    onExpandedChange = { expandedPriority = !expandedPriority }
                ) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Prioritas") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        priorities.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    priority = selectionOption
                                    expandedPriority = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi (Opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pilih Deadline: ${formatter.format(Date(deadline))}")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && subject.isNotBlank()) {
                        onAdd(title, description, subject, category, priority, deadline)
                    }
                },
                enabled = title.isNotBlank() && subject.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

