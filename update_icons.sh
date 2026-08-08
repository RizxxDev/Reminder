#!/bin/bash
sed -i 's/Icons.Default.Check/Icons.Default.CheckCircle/g' app/src/main/java/com/example/ui/HomeScreen.kt
sed -i 's/Icons.Default.CalendarToday/Icons.Default.DateRange/g' app/src/main/java/com/example/ui/HomeScreen.kt
sed -i 's/Icons.Default.Flag/Icons.Default.Info/g' app/src/main/java/com/example/ui/HomeScreen.kt
