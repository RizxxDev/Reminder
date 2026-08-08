#!/bin/bash
sed -i '/@OptIn(ExperimentalMaterial3Api::class)/,/@OptIn(ExperimentalMaterial3Api::class)/c\
@OptIn(ExperimentalMaterial3Api::class)\
@Composable\
fun SwipeableTaskCard(task: Task, onToggleComplete: (Task) -> Unit, onDelete: (Task) -> Unit) {\
    var showDeleteConfirm by remember { mutableStateOf(false) }\
    \
    val dismissState = rememberSwipeToDismissBoxState(\
        confirmValueChange = {\
            if (it == SwipeToDismissBoxValue.StartToEnd) {\
                onToggleComplete(task.copy(isCompleted = !task.isCompleted))\
                return@rememberSwipeToDismissBoxState false\
            }\
            if (it == SwipeToDismissBoxValue.EndToStart) {\
                showDeleteConfirm = true\
                return@rememberSwipeToDismissBoxState false\
            }\
            false\
        }\
    )\
\
    if (showDeleteConfirm) {\
        AlertDialog(\
            onDismissRequest = { showDeleteConfirm = false },\
            title = { Text("Hapus Tugas") },\
            text = { Text("Apakah Anda yakin ingin menghapus tugas ini?") },\
            confirmButton = {\
                Button(onClick = {\
                    onDelete(task)\
                    showDeleteConfirm = false\
                }) {\
                    Text("Hapus")\
                }\
            },\
            dismissButton = {\
                TextButton(onClick = { showDeleteConfirm = false }) {\
                    Text("Batal")\
                }\
            }\
        )\
    }\
\
    SwipeToDismissBox(\
        state = dismissState,\
        backgroundContent = {\
            val isStartToEnd = dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd\
            val isEndToStart = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart\
            val color = if (isStartToEnd) MaterialTheme.colorScheme.primaryContainer else if (isEndToStart) MaterialTheme.colorScheme.errorContainer else Color.Transparent\
            \
            Box(\
                Modifier\
                    .fillMaxSize()\
                    .background(color, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))\
                    .padding(horizontal = 20.dp),\
                contentAlignment = if (isStartToEnd) Alignment.CenterStart else Alignment.CenterEnd\
            ) {\
                if (isStartToEnd) {\
                    Icon(androidx.compose.material.icons.Icons.Default.Check, contentDescription = "Selesai", tint = MaterialTheme.colorScheme.onPrimaryContainer)\
                } else if (isEndToStart) {\
                    Icon(androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onErrorContainer)\
                }\
            }\
        }\
    ) {\
        TaskCard(task = task, onToggleComplete = onToggleComplete, onDeleteClick = { showDeleteConfirm = true })\
    }\
}\
\
@Composable\
fun TaskCard(task: Task, onToggleComplete: (Task) -> Unit, onDeleteClick: () -> Unit) {\
    val formatter = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())\
    val dateString = formatter.format(java.util.Date(task.deadline))\
\
    val priorityColor = when (task.priority) {\
        "Tinggi" -> Color(0xFFBA1A1A) // error / red\
        "Sedang" -> Color(0xFFFBBC00) // yellow/amber\
        else -> Color(0xFFBAC3FF) // primary fixed dim\
    }\
\
    Card(\
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),\
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),\
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),\
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),\
        border = null\
    ) {\
        Row(modifier = Modifier.fillMaxSize()) {\
            Box(\
                modifier = Modifier\
                    .width(8.dp)\
                    .fillMaxHeight()\
                    .background(priorityColor)\
            )\
            Column(\
                modifier = Modifier\
                    .fillMaxWidth()\
                    .padding(16.dp),\
                verticalArrangement = Arrangement.SpaceBetween\
            ) {\
                Column {\
                    Text(\
                        text = task.title, \
                        style = MaterialTheme.typography.titleLarge,\
                        color = MaterialTheme.colorScheme.onSurface,\
                        maxLines = 1,\
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis\
                    )\
                    Box(\
                        modifier = Modifier\
                            .padding(top = 4.dp, bottom = 16.dp)\
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))\
                            .padding(horizontal = 12.dp, vertical = 4.dp)\
                    ) {\
                        Text(text = task.subject, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)\
                    }\
                }\
                \
                Row(\
                    modifier = Modifier.fillMaxWidth(),\
                    horizontalArrangement = Arrangement.SpaceBetween,\
                    verticalAlignment = Alignment.Bottom\
                ) {\
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {\
                        Icon(androidx.compose.material.icons.Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)\
                        Text(dateString, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)\
                    }\
                    \
                    Box(\
                        modifier = Modifier\
                            .background(priorityColor.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))\
                            .padding(horizontal = 12.dp, vertical = 4.dp)\
                    ) {\
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {\
                            Icon(androidx.compose.material.icons.Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp), tint = priorityColor)\
                            Text(task.priority, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = priorityColor)\
                        }\
                    }\
                }\
            }\
        }\
    }\
}\
\
@OptIn(ExperimentalMaterial3Api::class)' app/src/main/java/com/example/ui/HomeScreen.kt
