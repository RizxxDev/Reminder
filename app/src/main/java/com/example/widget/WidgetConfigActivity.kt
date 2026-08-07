package com.example.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll

class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setResult(Activity.RESULT_CANCELED)

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WidgetConfigScreen(
                        onConfigSaved = { filterPriority ->
                            // Save preference
                            val prefs = getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
                            prefs.edit().putString("widget_$appWidgetId", filterPriority).apply()

                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(Activity.RESULT_OK, resultValue)
                            
                            // Trigger widget update
                            CoroutineScope(Dispatchers.IO).launch {
                                SmallTaskWidget().updateAll(applicationContext)
                                MediumTaskWidget().updateAll(applicationContext)
                                LargeTaskWidget().updateAll(applicationContext)
                            }
                            
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetConfigScreen(onConfigSaved: (String) -> Unit) {
    var selectedPriority by remember { mutableStateOf("Semua") }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Konfigurasi Widget", style = MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Pilih prioritas tugas yang ingin ditampilkan pada widget ini:")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val options = listOf("Semua", "Tinggi", "Sedang", "Rendah")
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = (selectedPriority == option),
                    onClick = { selectedPriority = option }
                )
                Text(text = option, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { onConfigSaved(selectedPriority) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simpan Konfigurasi")
        }
    }
}
