#!/bin/bash
sed -i 's/androidx.compose.material.icons.Icons.Default.CheckCircle/Icons.Default.CheckCircle/g' app/src/main/java/com/example/ui/HomeScreen.kt
sed -i 's/androidx.compose.material.icons.Icons.Default.Delete/Icons.Default.Delete/g' app/src/main/java/com/example/ui/HomeScreen.kt
sed -i 's/androidx.compose.material.icons.Icons.Default.DateRange/Icons.Default.DateRange/g' app/src/main/java/com/example/ui/HomeScreen.kt
sed -i 's/androidx.compose.material.icons.Icons.Default.Info/Icons.Default.Info/g' app/src/main/java/com/example/ui/HomeScreen.kt

# Add imports
sed -i '/import androidx.compose.material.icons.filled.Settings/a\
import androidx.compose.material.icons.filled.CheckCircle\
import androidx.compose.material.icons.filled.DateRange\
import androidx.compose.material.icons.filled.Info' app/src/main/java/com/example/ui/HomeScreen.kt
