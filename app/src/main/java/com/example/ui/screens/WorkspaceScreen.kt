package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Habit
import com.example.ui.components.TreeVisual
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getStageName
import com.example.ui.theme.*
import com.example.ui.viewmodel.HabitViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.filteredHabits.collectAsState()
    val completions by viewModel.completions.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isCalendarStripLayout by viewModel.isCalendarStripLayout.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var analyticDetailsHabit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Workspace Date Slogan
            item {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US).uppercase() + ", " +
                                today.month.getDisplayName(TextStyle.SHORT, Locale.US).uppercase() + " " +
                                today.dayOfMonth + ", " + today.year,
                        color = if (isDark) ColorSage.copy(alpha = 0.8f) else BrandSage,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Habitree Workspace",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Plant Seed FAB / Button Trigger
            item {
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandSage,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add plant",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Plant New Seed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Checklist Summary
            item {
                TodayStatusChecklistCard(
                    totalHabitsCount = habits.size,
                    todayCompletionsCount = habits.filter { viewModel.isHabitCompletedOnDate(it.id, today) }.size,
                    isDark = isDark
                )
            }

            // Categories list selector row
            item {
                CategoryTabsRow(
                    selectedCategory = selectedCategory,
                    onSelect = { viewModel.selectCategory(it) },
                    isDark = isDark
                )
            }

            // Habits List
            if (habits.isEmpty()) {
                item {
                    EmptyWorkspaceState()
                }
            } else {
                items(habits, key = { it.id }) { habit ->
                    HabitCard(
                        habit = habit,
                        isCompleted = viewModel.isHabitCompletedOnDate(habit.id, selectedDate),
                        isCalendarStripLayout = isCalendarStripLayout,
                        selectedDate = selectedDate,
                        allCompletions = completions.filter { it.habitId == habit.id },
                        onToggle = { viewModel.toggleCompletion(habit.id, selectedDate) },
                        onEdit = { habitToEdit = habit },
                        onDelete = { habitToDelete = habit },
                        onViewDetailedAnalytics = { analyticDetailsHabit = habit },
                        isDark = isDark,
                        onSelectDate = { viewModel.selectDate(it) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }

    // Modal dialogues
    if (showAddDialog) {
        AddEditHabitDialog(
            isDark = isDark,
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, cat, notify, icon, species ->
                viewModel.createHabit(title, desc, cat, notify, icon, species)
            }
        )
    }

    val currentHabitToEdit = habitToEdit
    if (currentHabitToEdit != null) {
        val h = currentHabitToEdit
        AddEditHabitDialog(
            isDark = isDark,
            existingHabit = h,
            onDismiss = { habitToEdit = null },
            onSave = { title, desc, cat, notify, icon, species ->
                viewModel.updateHabitDetails(h.id, title, desc, cat, notify, icon, species)
            }
        )
    }

    // Analytics overlay detailed view popup modal
    val currentAnalyticDetailsHabit = analyticDetailsHabit
    if (currentAnalyticDetailsHabit != null) {
        DetailedAnalyticsOverlay(
            habit = currentAnalyticDetailsHabit,
            completions = completions.filter { it.habitId == currentAnalyticDetailsHabit.id },
            onDismiss = { analyticDetailsHabit = null },
            isDark = isDark
        )
    }

    // Delete confirmation dialog
    val currentHabitToDelete = habitToDelete
    if (currentHabitToDelete != null) {
        val h = currentHabitToDelete
        val catColor = getCategoryColor(h.category)
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { habitToDelete = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) DarkClayCard else LightCreamCard),
                border = BorderStroke(
                    1.dp,
                    if (isDark) DarkBorderColor else LightBorderColor
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF3E1F24) else Color(0xFFFFECEB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Icon",
                            tint = Color(0xFFC2465D),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Uproot Habitree?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF141312)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Are you sure you want to completely delete \"${h.title}\"? This action cannot be undone and all your streaks and growth history will be lost forever.",
                        fontSize = 13.sp,
                        color = if (isDark) Color(0xFFB5B0AA) else Color(0xFF706C68),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { habitToDelete = null },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isDark) Color.White else Color(0xFF706C68)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isDark) Color(0xFF403E39) else Color(0xFFE9E6E2)
                            )
                        ) {
                            Text(
                                text = "Cancel",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Button(
                            onClick = {
                                viewModel.deleteHabit(h)
                                habitToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2465D)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Delete",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    isDark: Boolean,
    onThemeToggle: () -> Unit
) {
    Surface(
        color = if (isDark) Color(0xFF19241E) else LightSageBg,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 6.dp,
        modifier = Modifier
            .padding(top = 10.dp, start = 16.dp, end = 16.dp)
            .statusBarsPadding()
            .border(
                width = 1.dp,
                color = if (isDark) Color(0xFF2B3D32) else Color(0xFFB8C9BF),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Habitree Logo
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BrandSage.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = BrandSage,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Habitree",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "CONSISTENCY IS GROWTH",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (isDark) ColorSage else BrandSage
                )
            }

            // Theme Switcher Widget
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Switch Theme",
                    tint = if (isDark) ColorSage else BrandSage,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TodayStatusChecklistCard(
    totalHabitsCount: Int,
    todayCompletionsCount: Int,
    isDark: Boolean
) {
    val progress = if (totalHabitsCount == 0) 1.0f else todayCompletionsCount.toFloat() / totalHabitsCount.toFloat()
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) DarkClayCard else LightCreamCard
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isDark) DarkBorderColor else LightBorderColor
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TODAY STATUS CHECKLIST",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = if (isDark) DarkTextSecondary else LightTextSecondary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "You registered $todayCompletionsCount of $totalHabitsCount completions",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            val statusDesc = when {
                totalHabitsCount == 0 -> "Let's plant some brand new habit seeds above to begin!"
                progress >= 1.0f -> "Perfect score! All your habitrees have been watered and nourished today."
                progress >= 0.5f -> "Awesome work. You're past halfway! Keep going."
                else -> "Your forest is waiting! Water your habit seeds to build streaks."
            }
            Text(
                text = statusDesc,
                fontSize = 12.sp,
                color = if (isDark) DarkTextSecondary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress strip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    color = BrandSage,
                    trackColor = if (isDark) Color(0xFF2A2926) else Color(0xFFE5E2DC),
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${(progress * 100).toInt()}% Done",
                    color = if (isDark) ColorSage else BrandSage,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun CategoryTabsRow(
    selectedCategory: String,
    onSelect: (String) -> Unit,
    isDark: Boolean
) {
    val categories = listOf("ALL", "SAGE", "OCEAN", "LAVENDER", "TERRACOTTA")
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { cat ->
            val isSelected = selectedCategory.uppercase() == cat.uppercase()
            val categoryColor = getCategoryColor(cat)
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelect(cat) }
                    .background(
                        if (isSelected) {
                            if (isDark) categoryColor.copy(alpha = 0.25f) else categoryColor.copy(alpha = 0.15f)
                        } else {
                            if (isDark) Color(0xFF1C1B19) else Color(0xFFEFECE6)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) categoryColor else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (cat.uppercase() != "ALL") {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(categoryColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = cat,
                        color = if (isSelected) {
                            categoryColor
                        } else {
                            if (isDark) DarkTextSecondary else LightTextSecondary
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitCard(
    habit: Habit,
    isCompleted: Boolean,
    isCalendarStripLayout: Boolean,
    selectedDate: LocalDate,
    allCompletions: List<com.example.data.HabitCompletion>,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewDetailedAnalytics: () -> Unit,
    isDark: Boolean,
    onSelectDate: (LocalDate) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val categoryColor = getCategoryColor(habit.category)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) DarkClayCard else LightCreamCard
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isDark) DarkBorderColor else LightBorderColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category icon + Title + Streak
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category Specific Circular Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    val habitIcon = getIconByName(habit.iconName)
                    Icon(
                        imageVector = habitIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = habit.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        // Streak Badge
                        if (habit.currentStreak > 0) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GoldStreak.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = "Streak",
                                    tint = GoldStreak,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${habit.currentStreak}d",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldStreak
                                )
                            }
                        }
                    }
                    if (habit.description.isNotEmpty()) {
                        Text(
                            text = habit.description,
                            fontSize = 11.sp,
                            color = if (isDark) DarkTextSecondary else LightTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Action icons: Pencil, Trash, Chevron Expander
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onEdit() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF262422) else Color(0xFFF5F2EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit habit",
                                tint = if (isDark) DarkTextSecondary else LightTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF381B1E) else Color(0xFFFFEBEC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete habit",
                                tint = ColorCrimson,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF262422) else Color(0xFFF5F2EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand Detail",
                                tint = if (isDark) DarkTextSecondary else LightTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Expanded Analytical Detail Area
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Dynamic Animated Tree canvas Representation!
                        TreeVisual(
                            streak = habit.currentStreak,
                            category = habit.category,
                            species = habit.species,
                            modifier = Modifier.size(110.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // 2. High-Density Statistics Summary
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = GoldStreak, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Streak", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("${habit.currentStreak}d", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = ColorSage, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Max Streak", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("${habit.maxStreak}d", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ColorOcean, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Waterings", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("${allCompletions.size}", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            
                            // Edit custom notification clock reminder label
                            if (habit.notificationTime != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ColorLavender, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reminder", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(habit.notificationTime, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grid Layout Visualization Card inline: Choose calendar strip / Month Grid
                    Text(
                        text = if (isCalendarStripLayout) "CURRENT CALENDAR STRIP" else "30-DAY MINI HEATMAP GRID",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isDark) DarkTextSecondary else LightTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    if (isCalendarStripLayout) {
                        CalendarStripView(
                            completions = allCompletions,
                            selectedDate = selectedDate,
                            categoryColor = categoryColor,
                            isDark = isDark,
                            onSelect = onSelectDate
                        )
                    } else {
                        MiniHeatmapView(
                            completions = allCompletions,
                            categoryColor = categoryColor,
                            isDark = isDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // "View analytics" button triggers a high-fidelity detailed overlay modal!
                    Text(
                        text = "view analytics",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = categoryColor,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { onViewDetailedAnalytics() }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = if (isDark) Color(0xFF2E2C29) else Color(0xFFEFECE7), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Action section at bottom
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Completed status label
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) SuccessGreen else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCompleted) "Completed today" else "Water seed today",
                        fontSize = 11.sp,
                        color = if (isCompleted) SuccessGreen else (if (isDark) DarkTextSecondary else LightTextSecondary)
                    )
                }
                
                // Done check pill button slider
                Button(
                    onClick = { onToggle() },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) {
                            if (isDark) SuccessGreen.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.12f)
                        } else {
                            if (isDark) Color(0xFF262523) else Color(0xFFEDE9E3)
                        },
                        contentColor = if (isCompleted) SuccessGreen else (if (isDark) DarkTextPrimary else LightTextPrimary)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.WaterDrop,
                        contentDescription = "Complete check",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCompleted) "Done" else "Nourish",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarStripView(
    completions: List<com.example.data.HabitCompletion>,
    selectedDate: LocalDate,
    categoryColor: Color,
    isDark: Boolean,
    onSelect: (LocalDate) -> Unit
) {
    val daysList = remember {
        val today = LocalDate.now()
        (-3..3).map { today.plusDays(it.toLong()) }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (date in daysList) {
            val isSelected = date.equals(selectedDate)
            val hasCompleted = completions.any { it.date == date.toString() }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(date) }
                    .background(
                        if (isSelected) {
                            categoryColor.copy(alpha = 0.15f)
                        } else {
                            Color.Transparent
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) categoryColor else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).uppercase().take(2),
                    fontSize = 8.sp,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (hasCompleted) {
                                categoryColor
                            } else {
                                if (isDark) Color(0xFF2E2C29) else Color(0xFFE5E2DC)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (hasCompleted) Color.White else (if (isDark) DarkTextPrimary else LightTextPrimary)
                    )
                }
            }
        }
    }
}

@Composable
fun MiniHeatmapView(
    completions: List<com.example.data.HabitCompletion>,
    categoryColor: Color,
    isDark: Boolean
) {
    // Renders a simple 30 day checklist grid
    val daysInGrid = 30
    val today = LocalDate.now()
    val datesList = remember {
        (0 until daysInGrid).map { today.minusDays(it.toLong()) }.reversed()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDark) Color(0xFF1B1A18) else Color(0xFFF1EEF4))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (date in datesList) {
            val isDone = completions.any { it.date == date.toString() }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isDone) categoryColor else {
                            if (isDark) Color(0xFF2C2A27) else Color(0xFFDDD9D0)
                        }
                    )
            )
        }
    }
}

@Composable
fun EmptyWorkspaceState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FilterVintage,
            contentDescription = null,
            tint = ColorSage.copy(alpha = 0.5f),
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Your Workspace is empty",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Plant a brand new habit seed using the Green button to build consistency and grow a visual ecosystem.",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun AddEditHabitDialog(
    isDark: Boolean,
    existingHabit: Habit? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, category: String, notificationTime: String?, iconName: String, species: String) -> Unit
) {
    var title by remember { mutableStateOf(existingHabit?.title ?: "") }
    var desc by remember { mutableStateOf(existingHabit?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(existingHabit?.category ?: "SAGE") }
    var selectedIcon by remember { mutableStateOf(existingHabit?.iconName ?: "SEEDLING") }
    var selectedSpecies by remember { mutableStateOf(existingHabit?.species ?: "CHERRY_BLOSSOM") }
    var repeatFrequency by remember { mutableStateOf("EVERYDAY") }
    
    val gridThemeColor = getCategoryColor(selectedCategory)

    // Reminders
    var enableReminder by remember { mutableStateOf(existingHabit?.notificationTime != null) }
    var hourSelect by remember { mutableStateOf(existingHabit?.notificationTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 8) }
    var minSelect by remember { mutableStateOf(existingHabit?.notificationTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0) }

    val scrollState = rememberScrollState()

    // Dynamic color tokens
    val dialogBg = if (isDark) DarkClayCard else LightCreamCard
    val titleTextColor = if (isDark) Color.White else LightTextPrimary
    val subtextColor = if (isDark) DarkTextSecondary else LightTextSecondary
    val sectionHeaderColor = if (isDark) DarkTextSecondary else LightTextSecondary
    
    val inputFieldBg = if (isDark) Color(0xFF141312) else Color(0xFFFAF7F0)
    val inputTextColor = if (isDark) Color.White else LightTextPrimary
    val inputPlaceholderColor = if (isDark) DarkTextSecondary.copy(alpha = 0.6f) else LightTextSecondary.copy(alpha = 0.6f)
    
    val unselectedBoxBg = if (isDark) Color(0xFF141312) else Color(0xFFFAF7F0)
    val unselectedBoxBorder = if (isDark) Color(0xFF2E2D2B) else Color(0xFFE9E6E2)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            border = BorderStroke(
                1.dp,
                if (isDark) DarkBorderColor else LightBorderColor
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header (Title, Subtitle, Close Button X)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (existingHabit != null) "Edit Habitree" else "Plant New Habitree",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleTextColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Define your parameters to spawn a digital seedling.",
                            fontSize = 12.sp,
                            color = subtextColor
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = subtextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Scrollable main content Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    // ---- HABIT TITLE * ----
                    Text(
                        text = "HABIT TITLE *",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = sectionHeaderColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(inputFieldBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = inputTextColor),
                        decorationBox = { innerTextField ->
                            if (title.isEmpty()) {
                                Text(
                                    text = "e.g. Meditate daily, Workout",
                                    fontSize = 15.sp,
                                    color = inputPlaceholderColor
                                )
                            }
                            innerTextField()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ---- DESCRIPTION / PURPOSE ----
                    Text(
                        text = "DESCRIPTION / PURPOSE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = sectionHeaderColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(inputFieldBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = inputTextColor),
                        decorationBox = { innerTextField ->
                            if (desc.isEmpty()) {
                                Text(
                                    text = "Why is this habit critical to your growth?",
                                    fontSize = 15.sp,
                                    color = inputPlaceholderColor
                                )
                            }
                            innerTextField()
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ---- SELECT ICON (Grid of 20 icons) ----
                    Text(
                        text = "SELECT ICON",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = sectionHeaderColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val iconsList = listOf(
                        "TERMINAL", "WATER", "FITNESS", "BOOK", "AIR",
                        "TARGET", "BRAIN", "COFFEE", "HEART", "STARS",
                        "SEEDLING", "FIRE", "LIGHTNING", "MOON", "SUN",
                        "WORK", "MUSIC", "APPLE", "SCROLL", "LEAF"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (rowIndex in 0 until 4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (colIndex in 0 until 5) {
                                    val iconIndex = rowIndex * 5 + colIndex
                                    val iconKey = iconsList[iconIndex]
                                    val isSel = selectedIcon == iconKey
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(
                                                if (isSel) gridThemeColor.copy(alpha = if (isDark) 0.25f else 0.15f) else unselectedBoxBg,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .border(
                                                width = if (isSel) 1.5.dp else 0.dp,
                                                color = if (isSel) gridThemeColor else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedIcon = iconKey },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getIconByName(iconKey),
                                            contentDescription = null,
                                            tint = if (isSel) gridThemeColor else subtextColor.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ---- ECOSYSTEM TONE PALETTE ----
                    Text(
                        text = "ECOSYSTEM TONE PALETTE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = sectionHeaderColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val paletteCategories = listOf("SAGE", "OCEAN", "LAVENDER", "TERRACOTTA", "CRIMSON")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (cat in paletteCategories) {
                            val isSel = selectedCategory == cat
                            val color = getCategoryColor(cat)
                            
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedCategory = cat },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ---- SELECT TREE SPECIES ----
                    Text(
                        text = "SELECT TREE SPECIES (YOUR HABIT)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = sectionHeaderColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val speciesOptions = listOf(
                        "CHERRY_BLOSSOM" to ("Cherry Blossom" to "Sways with beautiful pink petals"),
                        "EVERGREEN_PINE" to ("Evergreen Pine" to "Thrives in snowy heights"),
                        "GOLDEN_OAK" to ("Golden Oak" to "Grand majestic crown"),
                        "WINDY_PALM" to ("Windy Palm" to "Tropical relaxation design"),
                        "ZEN_BONSAI" to ("Zen Bonsai" to "Artistic twisted branches")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for ((key, pair) in speciesOptions) {
                            val isSel = selectedSpecies == key
                            val (displayName, descText) = pair
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSel) gridThemeColor.copy(alpha = if (isDark) 0.15f else 0.08f) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = if (isSel) 1.5.dp else 1.dp,
                                        color = if (isSel) gridThemeColor else unselectedBoxBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedSpecies = key }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = displayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) gridThemeColor else titleTextColor
                                    )
                                    Text(
                                        text = descText,
                                        fontSize = 11.sp,
                                        color = subtextColor
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSel) gridThemeColor else subtextColor.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                        .background(if (isSel) gridThemeColor else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSel) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ---- MATURE STAGE PREVIEW BOX ----
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(unselectedBoxBg, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = subtextColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MATURE STAGE PREVIEW",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = subtextColor
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Interactive live tree sway visual!
                            TreeVisual(
                                streak = 15, // Mature
                                category = selectedCategory,
                                species = selectedSpecies,
                                modifier = Modifier.size(110.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ---- REPEAT FREQUENCY ----
                    Text(
                        text = "REPEAT FREQUENCY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = sectionHeaderColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Everyday", "Weekly", "Specific Days").forEach { freq ->
                            val isSel = repeatFrequency == freq.uppercase()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .background(
                                        if (isSel) gridThemeColor else unselectedBoxBg,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = if (isSel) 0.dp else 1.dp,
                                        color = unselectedBoxBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { repeatFrequency = freq.uppercase() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = freq,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else titleTextColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ---- DAILY REMINDER ----
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = subtextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DAILY REMINDER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = sectionHeaderColor
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(unselectedBoxBg, RoundedCornerShape(12.dp))
                                .clickable { enableReminder = !enableReminder }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (enableReminder) String.format(java.util.Locale.US, "%02d:%02d", hourSelect, minSelect) else "Inactive",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (enableReminder) gridThemeColor else subtextColor
                            )
                        }
                    }

                    if (enableReminder) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(unselectedBoxBg, RoundedCornerShape(12.dp))
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { hourSelect = (hourSelect - 1 + 24) % 24 }) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = titleTextColor)
                            }
                            Text(
                                text = String.format(java.util.Locale.US, "%02d", hourSelect),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleTextColor,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(onClick = { hourSelect = (hourSelect + 1) % 24 }) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = titleTextColor)
                            }

                            Text(":", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = titleTextColor)

                            IconButton(onClick = { minSelect = (minSelect - 5 + 60) % 60 }) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = titleTextColor)
                            }
                            Text(
                                text = String.format(java.util.Locale.US, "%02d", minSelect),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleTextColor,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(onClick = { minSelect = (minSelect + 5) % 60 }) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = titleTextColor)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Actions (Cancel and Plant Seed style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = subtextColor
                        )
                    }
                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                onSave(
                                    title,
                                    desc,
                                    selectedCategory,
                                    if (enableReminder) String.format(java.util.Locale.US, "%02d:%02d", hourSelect, minSelect) else null,
                                    selectedIcon,
                                    selectedSpecies
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = gridThemeColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (existingHabit != null) "Save Changes" else "Plant Seed",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedAnalyticsOverlay(
    habit: Habit,
    completions: List<com.example.data.HabitCompletion>,
    onDismiss: () -> Unit,
    isDark: Boolean
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val themeColor = getCategoryColor(habit.category)
        
        Card(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header dismiss close row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close detailed analysis overlay")
                    }
                }

                // Big Visual Tree state canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TreeVisual(
                        streak = habit.currentStreak,
                        category = habit.category,
                        species = habit.species,
                        modifier = Modifier.size(170.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title info
                Text(
                    text = habit.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Growth Model Strategy: " + getStageName(habit.currentStreak),
                    fontSize = 12.sp,
                    color = themeColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (habit.description.isNotEmpty()) {
                    Text(
                        text = habit.description,
                        fontSize = 13.sp,
                        color = if (isDark) DarkTextSecondary else LightTextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // 2x2 Stats Dashboard grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatMetricCard(
                        title = "CURRENT STREAK",
                        value = "${habit.currentStreak} d",
                        icon = Icons.Default.Whatshot,
                        iconColor = GoldStreak,
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    StatMetricCard(
                        title = "MAX STREAK",
                        value = "${habit.maxStreak} d",
                        icon = Icons.Default.Star,
                        iconColor = GoldStreak,
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatMetricCard(
                        title = "WATERINGS",
                        value = "${completions.size}",
                        icon = Icons.Default.WaterDrop,
                        iconColor = ColorOcean,
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    val consistencyCoef = if (completions.isEmpty()) 0 else (Math.min(100, (completions.size * 100) / 90))
                    StatMetricCard(
                        title = "CONSISTENCY",
                        value = "$consistencyCoef%",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconColor = ColorLavender,
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Scrollable 365 year heat matrix! Simple scroll grid represent all 12 calendar ovals months or simple view representation
                Text(
                    text = "365-DAY YEAR HEATMAP CALENDAR (${LocalDate.now().year})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val monthsInYear = (1..12)
                    items(monthsInYear.toList()) { month ->
                        YearMonthlyGrid(
                            month = month,
                            completions = completions,
                            themeColor = themeColor,
                            isDark = isDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color = ColorSage,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1B1A18) else LightCreamCard
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isDark) DarkBorderColor else LightBorderColor
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun YearMonthlyGrid(
    month: Int,
    completions: List<com.example.data.HabitCompletion>,
    themeColor: Color,
    isDark: Boolean
) {
    val currentYear = LocalDate.now().year
    val dummyDate = LocalDate.of(currentYear, month, 1)
    val monthName = dummyDate.month.getDisplayName(TextStyle.FULL, Locale.US).uppercase()
    val totalDays = dummyDate.lengthOfMonth()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1B1A18) else Color(0xFFEFECE7)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = monthName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isDark) ColorSage else BrandSage
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Renders standard grid of dots representing days of specified month
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (dayNum in 1..totalDays) {
                    val dateToCheck = LocalDate.of(currentYear, month, dayNum)
                    val isDone = completions.any { it.date == dateToCheck.toString() }
                    
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDone) themeColor else {
                                    if (isDark) Color(0xFF2E2C29) else Color(0xFFDDD9D1)
                                }
                            )
                    )
                }
            }
        }
    }
}

fun getIconByName(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName.uppercase()) {
        "TERMINAL" -> androidx.compose.material.icons.Icons.Default.Terminal
        "WATER" -> androidx.compose.material.icons.Icons.Default.WaterDrop
        "FITNESS" -> androidx.compose.material.icons.Icons.Default.FitnessCenter
        "BOOK" -> androidx.compose.material.icons.Icons.AutoMirrored.Filled.MenuBook
        "AIR" -> androidx.compose.material.icons.Icons.Default.Air
        "TARGET" -> androidx.compose.material.icons.Icons.Default.TrackChanges
        "BRAIN" -> androidx.compose.material.icons.Icons.Default.Psychology
        "COFFEE" -> androidx.compose.material.icons.Icons.Default.Coffee
        "HEART" -> androidx.compose.material.icons.Icons.Default.Favorite
        "STARS" -> androidx.compose.material.icons.Icons.Default.AutoAwesome
        "SEEDLING" -> androidx.compose.material.icons.Icons.Default.Spa
        "FIRE" -> androidx.compose.material.icons.Icons.Default.LocalFireDepartment
        "LIGHTNING" -> androidx.compose.material.icons.Icons.Default.Bolt
        "MOON" -> androidx.compose.material.icons.Icons.Default.NightsStay
        "SUN" -> androidx.compose.material.icons.Icons.Default.WbSunny
        "WORK" -> androidx.compose.material.icons.Icons.Default.Work
        "MUSIC" -> androidx.compose.material.icons.Icons.Default.MusicNote
        "APPLE" -> androidx.compose.material.icons.Icons.Default.Eco
        "SCROLL" -> androidx.compose.material.icons.Icons.Default.Description
        "LEAF" -> androidx.compose.material.icons.Icons.Default.Eco
        else -> androidx.compose.material.icons.Icons.Default.Spa
    }
}

