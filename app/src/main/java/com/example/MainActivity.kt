package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NotificationHelper

@android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")
class MainActivity : ComponentActivity() {
  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    // Handle permission response if needed
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    NotificationHelper.scheduleDailyReminder(this)

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        HomeScreen()
      }
    }
  }
}
