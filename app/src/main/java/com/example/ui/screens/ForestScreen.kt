package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.ui.components.TreeVisual
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getStageName
import com.example.ui.theme.*
import com.example.ui.viewmodel.HabitViewModel
import java.time.LocalDate

@Composable
fun ForestScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsState()
    val completions by viewModel.completions.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    
    var selectedHabitForest by remember { mutableStateOf<Habit?>(null) }
    val today = LocalDate.now()

    Scaffold(
        topBar = {
            HeaderSection(
                isDark = isDark,
                onThemeToggle = { viewModel.toggleTheme() }
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = ColorSage,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "My Habit Forest",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Every streak nourishes its tree. Complete your daily sessions to watch your forest grow from seeds to elder forest titans.",
                fontSize = 12.sp,
                color = if (isDark) DarkTextSecondary else LightTextSecondary,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Total Trees indicator
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) DarkClayCard else LightCreamCard
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    if (isDark) DarkBorderColor else LightBorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Trees:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${habits.size}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = ColorSage
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Forest grid of growing canvas seeds
            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = ColorSage.copy(alpha = 0.3f),
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No trees growing yet",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Go to Workspace and write down daily check-ins to make seeds sprout!",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        val isWatered = completions.any { it.habitId == habit.id && it.date == today.toString() }
                        val themeColor = getCategoryColor(habit.category)
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) DarkClayCard else LightCreamCard
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isDark) DarkBorderColor else LightBorderColor
                            ),
                            modifier = Modifier
                                .clickable { selectedHabitForest = habit }
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    // Small category indicator dot + Watering checked ratio icon
                                    if (isWatered) {
                                        Icon(
                                            imageVector = Icons.Default.WaterDrop,
                                            contentDescription = "Watered today",
                                            tint = ColorSage,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(themeColor)
                                        )
                                    }
                                }

                                // Interactive Sway Tree Visual Canvas
                                TreeVisual(
                                    streak = habit.currentStreak,
                                    category = habit.category,
                                    species = habit.species,
                                    modifier = Modifier.size(100.dp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = habit.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Text(
                                    text = "Streak: ${habit.currentStreak}d",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (habit.currentStreak > 0) ColorSage else Color.Gray,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    // Detailed interactive custom tree analytics popup modal
    val currentSelectedHabitForest = selectedHabitForest
    if (currentSelectedHabitForest != null) {
        DetailedAnalyticsOverlay(
            habit = currentSelectedHabitForest,
            completions = completions.filter { it.habitId == currentSelectedHabitForest.id },
            onDismiss = { selectedHabitForest = null },
            isDark = isDark
        )
    }
}
