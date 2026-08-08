#!/bin/bash
sed -i '/if (showAddDialog) {/c\
        if (showAddDialog) {\
            androidx.compose.ui.window.Dialog(\
                onDismissRequest = { showAddDialog = false },\
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)\
            ) {\
                AddTaskScreen(\
                    onDismiss = { showAddDialog = false },\
                    onSave = { title, desc, subject, category, priority, deadline ->\
                        viewModel.addTask(title, desc, subject, category, priority, deadline)\
                        showAddDialog = false\
                    }\
                )\
            }\
            continue\
        }' app/src/main/java/com/example/ui/HomeScreen.kt
