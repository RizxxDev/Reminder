#!/bin/bash
# Remove AddTaskDialog function from HomeScreen.kt
sed -i '/@OptIn(ExperimentalMaterial3Api::class)/,/@Composable\nfun AddTaskDialog/{
  /@Composable\nfun AddTaskDialog/!b
  :a
  N
  /^}$/!ba
  d
}' app/src/main/java/com/example/ui/HomeScreen.kt
