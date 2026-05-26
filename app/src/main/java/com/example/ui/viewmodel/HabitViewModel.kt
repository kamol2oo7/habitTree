package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.receiver.HabitReminderReceiver
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = HabitDatabase.getDatabase(application)
    private val repository = HabitRepository(database.habitDao)

    private fun scheduleDailyAlarm(context: Context, habitTitle: String, timeStr: String) {
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra("habit_title", habitTitle)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitTitle.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }
        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e("HabitViewModel", "Failed to schedule inexact alarm", e)
        }
    }

    // State holders
    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isCalendarStripLayout = MutableStateFlow(true) // true = Calendar Strip, false = Mini Heatmap
    val isCalendarStripLayout: StateFlow<Boolean> = _isCalendarStripLayout.asStateFlow()

    // Date navigation
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Raw habits stream
    val habits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Raw completions stream
    val completions: StateFlow<List<HabitCompletion>> = repository.allCompletions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered habits
    val filteredHabits: StateFlow<List<Habit>> = combine(habits, selectedCategory) { list, cat ->
        if (cat.uppercase() == "ALL") {
            list
        } else {
            list.filter { it.category.uppercase() == cat.uppercase() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Check if habit is completed for today/selected date
    fun isHabitCompletedOnDate(habitId: Int, date: LocalDate): Boolean {
        val dateStr = date.toString() // "YYYY-MM-DD"
        return completions.value.any { it.habitId == habitId && it.date == dateStr }
    }

    // Toggle completion
    fun toggleCompletion(habitId: Int, date: LocalDate) {
        viewModelScope.launch {
            try {
                repository.toggleCompletion(habitId, date.toString())
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to toggle completion", e)
            }
        }
    }

    // Category selections
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // Theme toggle
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }
    
    fun setTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    // Visual layout toggle
    fun setGridLayoutSetting(isCalendarStrip: Boolean) {
        _isCalendarStripLayout.value = isCalendarStrip
    }

    // Date picker strip selection
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    // Habit operations
    fun createHabit(title: String, description: String, category: String, notificationTime: String? = null, iconName: String = "SPA", species: String = "CHERRY_BLOSSOM") {
        viewModelScope.launch {
            try {
                val h = Habit(
                    title = title,
                    description = description,
                    category = category,
                    notificationTime = notificationTime,
                    iconName = iconName,
                    species = species
                )
                repository.insertHabit(h)
                if (notificationTime != null) {
                    scheduleDailyAlarm(getApplication(), title, notificationTime)
                }
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to create habit", e)
            }
        }
    }

    fun updateHabitDetails(id: Int, title: String, description: String, category: String, notificationTime: String? = null, iconName: String = "SPA", species: String = "CHERRY_BLOSSOM") {
        viewModelScope.launch {
            try {
                val existing = repository.getHabitById(id) ?: return@launch
                val updated = existing.copy(
                    title = title,
                    description = description,
                    category = category,
                    notificationTime = notificationTime,
                    iconName = iconName,
                    species = species
                )
                repository.updateHabit(updated)
                if (notificationTime != null) {
                    scheduleDailyAlarm(getApplication(), title, notificationTime)
                }
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to update habit details", e)
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                repository.deleteHabit(habit)
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to delete habit", e)
            }
        }
    }

    fun clearAllHabits() {
        viewModelScope.launch {
            try {
                repository.clearAllData()
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to clear all data", e)
            }
        }
    }

    // JSON export schema provider
    fun exportBackupJson(): String {
        return try {
            val root = JSONObject()
            
            val habsArray = JSONArray()
            for (h in habits.value) {
                val jobj = JSONObject()
                jobj.put("id", h.id)
                jobj.put("title", h.title)
                jobj.put("description", h.description)
                jobj.put("category", h.category)
                jobj.put("createdAt", h.createdAt)
                jobj.put("notificationTime", h.notificationTime ?: JSONObject.NULL)
                jobj.put("currentStreak", h.currentStreak)
                jobj.put("maxStreak", h.maxStreak)
                jobj.put("iconName", h.iconName)
                jobj.put("species", h.species)
                habsArray.put(jobj)
            }
            root.put("habits", habsArray)

            val compsArray = JSONArray()
            for (c in completions.value) {
                val jobj = JSONObject()
                jobj.put("id", c.id)
                jobj.put("habitId", c.habitId)
                jobj.put("date", c.date)
                jobj.put("timestamp", c.timestamp)
                compsArray.put(jobj)
            }
            root.put("completions", compsArray)

            root.toString(2)
        } catch (e: Exception) {
            Log.e("HabitViewModel", "Failed to export configuration", e)
            ""
        }
    }

    // Restore from json schema
    fun importBackupJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            val habitsList = mutableListOf<Habit>()
            val completionsList = mutableListOf<HabitCompletion>()

            if (root.has("habits")) {
                val arr = root.getJSONArray("habits")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    habitsList.add(
                        Habit(
                            id = obj.optInt("id", 0),
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            category = obj.getString("category"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            notificationTime = if (obj.isNull("notificationTime")) null else obj.getString("notificationTime"),
                            currentStreak = obj.optInt("currentStreak", 0),
                            maxStreak = obj.optInt("maxStreak", 0),
                            iconName = obj.optString("iconName", "SPA"),
                            species = obj.optString("species", "CHERRY_BLOSSOM")
                        )
                    )
                }
            }

            if (root.has("completions")) {
                val arr = root.getJSONArray("completions")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    completionsList.add(
                        HabitCompletion(
                            id = obj.optInt("id", 0),
                            habitId = obj.getInt("habitId"),
                            date = obj.getString("date"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Restore db bulk
            viewModelScope.launch {
                try {
                    repository.restoreDatabase(habitsList, completionsList)
                    // Recalculate streaks for each loaded habit to ensure 100% data integrity
                    for (h in habitsList) {
                        if (h.id != 0) {
                            repository.recalculateStreaks(h.id)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HabitViewModel", "Failed to restore database from backup", e)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("HabitViewModel", "Failed to restore backup text", e)
            false
        }
    }
}
