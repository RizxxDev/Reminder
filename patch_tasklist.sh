#!/bin/bash
sed -i 's/shape = MaterialTheme.shapes.large/shape = androidx.compose.foundation.shape.RoundedCornerShape(50)/' app/src/main/java/com/example/ui/TaskListScreen.kt
