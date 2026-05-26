package com.example.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class HabitRepository(private val habitDao: HabitDao) {

    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allCompletions: Flow<List<HabitCompletion>> = habitDao.getAllCompletions()

    suspend fun getHabitById(id: Int): Habit? = habitDao.getHabitById(id)

    suspend fun insertHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    suspend fun clearAllData() {
        habitDao.clearAllCompletions()
        habitDao.clearAllHabits()
    }

    // Returns if the habit is completed on a specific day
    suspend fun isCompleted(habitId: Int, dateStr: String): Boolean {
        return habitDao.getCompletion(habitId, dateStr) != null
    }

    // Toggle custom date completion and recalculate streaks
    suspend fun toggleCompletion(habitId: Int, dateStr: String) {
        val existing = habitDao.getCompletion(habitId, dateStr)
        if (existing != null) {
            habitDao.deleteCompletion(habitId, dateStr)
        } else {
            habitDao.insertCompletion(HabitCompletion(habitId = habitId, date = dateStr))
        }
        recalculateStreaks(habitId)
    }

    // Recalculates streak details for a specific habit
    suspend fun recalculateStreaks(habitId: Int) {
        val habit = habitDao.getHabitById(habitId) ?: return
        // Get completions
        val completions = habitDao.getCompletionsForHabitRaw(habitId)
        val completedDatesList = completions.mapNotNull { 
            try {
                LocalDate.parse(it.date)
            } catch (e: Exception) {
                null
            }
        }.distinct().sorted()

        val (currentStreak, maxStreak) = calculateStreaksFromDates(completedDatesList)

        // Save back to habit
        val updatedHabit = habit.copy(
            currentStreak = currentStreak,
            maxStreak = Math.max(habit.maxStreak, maxStreak)
        )
        habitDao.updateHabit(updatedHabit)
    }

    // Perform database bulk restore from backup list
    suspend fun restoreDatabase(habitsList: List<Habit>, completionsList: List<HabitCompletion>) {
        habitDao.clearAllCompletions()
        habitDao.clearAllHabits()
        for (habit in habitsList) {
            habitDao.insertHabit(habit)
        }
        for (comp in completionsList) {
            habitDao.insertCompletion(comp)
        }
    }

    companion object {
        // Shared dynamic helper to compute consecutive streaks from a sorted list of LocalDate
        fun calculateStreaksFromDates(datesList: List<LocalDate>): Pair<Int, Int> {
            if (datesList.isEmpty()) return Pair(0, 0)

            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            // Current streak calculation
            var currentStreak = 0
            val hasToday = datesList.contains(today)
            val hasYesterday = datesList.contains(yesterday)

            if (hasToday || hasYesterday) {
                var checkDate = if (hasToday) today else yesterday
                while (datesList.contains(checkDate)) {
                    currentStreak++
                    checkDate = checkDate.minusDays(1)
                }
            }

            // Max streak calculation
            var maxStreak = 0
            var tempStreak = 0
            var lastDate: LocalDate? = null

            for (date in datesList) {
                if (lastDate == null) {
                    tempStreak = 1
                } else {
                    val daysBetween = ChronoUnit.DAYS.between(lastDate, date)
                    if (daysBetween == 1L) {
                        tempStreak++
                    } else if (daysBetween > 1L) {
                        if (tempStreak > maxStreak) {
                            maxStreak = tempStreak
                        }
                        tempStreak = 1
                    }
                }
                lastDate = date
            }
            if (tempStreak > maxStreak) {
                maxStreak = tempStreak
            }

            return Pair(currentStreak, maxStreak)
        }
    }
}
