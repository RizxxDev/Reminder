package com.example.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.data.AppDatabase
import com.example.data.Task
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmallTaskWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallTaskWidget()
}

class MediumTaskWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumTaskWidget()
}

class LargeTaskWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeTaskWidget()
}

class SmallTaskWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val upcomingTasks = database.taskDao().getUpcomingTasksWidget().firstOrNull() ?: emptyList()
        
        val appWidgetId = androidx.glance.appwidget.GlanceAppWidgetManager(context).getAppWidgetId(id)
        val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
        val filterPriority = prefs.getString("widget_$appWidgetId", "Semua") ?: "Semua"
        
        val filteredTasks = if (filterPriority == "Semua") upcomingTasks else upcomingTasks.filter { it.priority == filterPriority }
        val task = filteredTasks.firstOrNull()

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(Color.White)
                    .cornerRadius(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.padding(bottom = 8.dp)) {
                    Box(modifier = GlanceModifier.size(20.dp).background(Color(0xFF2563EB)).cornerRadius(10.dp), contentAlignment = Alignment.Center) {
                        Text("✓", style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text("Tugas Terdekat", style = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF1F2937))))
                }

                if (task != null) {
                    val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                    val dateString = formatter.format(Date(task.deadline))
                    Text(
                        text = "Tenggat: $dateString",
                        style = TextStyle(fontWeight = FontWeight.Medium, color = ColorProvider(Color(0xFFF43F5E))),
                        modifier = GlanceModifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = task.subject,
                        style = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF111827))),
                        modifier = GlanceModifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = task.title,
                        style = TextStyle(color = ColorProvider(Color(0xFF6B7280))),
                        maxLines = 2
                    )
                } else {
                    Text(
                        text = "Tidak ada tugas, Yeay!",
                        style = TextStyle(color = ColorProvider(Color(0xFF6B7280)))
                    )
                }
            }
        }
    }
}

class MediumTaskWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val upcomingTasks = database.taskDao().getUpcomingTasksWidget().firstOrNull() ?: emptyList()

        val appWidgetId = androidx.glance.appwidget.GlanceAppWidgetManager(context).getAppWidgetId(id)
        val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
        val filterPriority = prefs.getString("widget_$appWidgetId", "Semua") ?: "Semua"
        
        val filteredTasks = if (filterPriority == "Semua") upcomingTasks else upcomingTasks.filter { it.priority == filterPriority }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(Color.White)
                    .cornerRadius(24.dp),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Box(modifier = GlanceModifier.size(24.dp).background(Color(0xFF2563EB)).cornerRadius(12.dp), contentAlignment = Alignment.Center) {
                        Text("≡", style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(if (filterPriority == "Semua") "Jadwal Tugas" else "Tugas ($filterPriority)", style = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF1F2937))))
                }

                if (filteredTasks.isNotEmpty()) {
                    filteredTasks.take(2).forEach { task ->
                        Row(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("○", style = TextStyle(color = ColorProvider(Color(0xFF2563EB)), fontWeight = FontWeight.Bold), modifier = GlanceModifier.padding(end = 8.dp))
                            Column {
                                Text(
                                    text = "${task.subject} - ${task.title}",
                                    style = TextStyle(fontWeight = FontWeight.Medium, color = ColorProvider(Color(0xFF1F2937))),
                                    maxLines = 1
                                )
                                val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
                                Text(
                                    text = formatter.format(Date(task.deadline)),
                                    style = TextStyle(color = ColorProvider(Color(0xFFB91C1C)))
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Semua tugas selesai!",
                        style = TextStyle(color = ColorProvider(Color(0xFF6B7280)))
                    )
                }
            }
        }
    }
}

class LargeTaskWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val upcomingTasks = database.taskDao().getUpcomingTasksWidget().firstOrNull() ?: emptyList()

        val appWidgetId = androidx.glance.appwidget.GlanceAppWidgetManager(context).getAppWidgetId(id)
        val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
        val filterPriority = prefs.getString("widget_$appWidgetId", "Semua") ?: "Semua"
        
        val filteredTasks = if (filterPriority == "Semua") upcomingTasks else upcomingTasks.filter { it.priority == filterPriority }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .background(Color(0xFFF8FAFC))
                    .cornerRadius(32.dp),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.Bottom, modifier = GlanceModifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text("Halo! \uD83D\uDC4B", style = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF1F2937))))
                        Text("Kamu punya ${filteredTasks.size} tugas mendekat.", style = TextStyle(color = ColorProvider(Color(0xFF6B7280))))
                    }
                }

                if (filteredTasks.isNotEmpty()) {
                    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                        items(filteredTasks) { task ->
                            Column(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .background(Color.White)
                                    .cornerRadius(12.dp)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = task.subject,
                                    style = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF1F2937))),
                                    maxLines = 1
                                )
                                Text(
                                    text = task.title,
                                    style = TextStyle(color = ColorProvider(Color(0xFF6B7280))),
                                    modifier = GlanceModifier.padding(bottom = 4.dp),
                                    maxLines = 1
                                )
                                val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
                                Text(
                                    text = formatter.format(Date(task.deadline)),
                                    style = TextStyle(fontWeight = FontWeight.Medium, color = ColorProvider(Color(0xFF2563EB)))
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Tidak ada tugas, Yeay!",
                        style = TextStyle(color = ColorProvider(Color(0xFF6B7280)))
                    )
                }
            }
        }
    }
}
