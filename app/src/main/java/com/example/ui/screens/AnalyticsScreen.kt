package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.data.HabitCompletion
import com.example.ui.theme.*
import com.example.ui.viewmodel.HabitViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsState()
    val completions by viewModel.completions.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    // Calculated metrics
    val totalSpecies = habits.size
    val totalWaterings = completions.size
    val collectiveStreak = habits.sumOf { it.currentStreak }
    
    // Consistency coefficient based on completions over the last 30 days
    val last30Days = (0..29).map { LocalDate.now().minusDays(it.toLong()) }
    val maxPossibleWaterings = habits.size * 30
    val actualWateringsIn30Days = completions.count { 
        try {
            LocalDate.parse(it.date) in last30Days 
        } catch (e: Exception) {
            false
        }
    }
    val consistencyIndex = if (maxPossibleWaterings == 0) 0 else {
        Math.min(100, (actualWateringsIn30Days * 100) / maxPossibleWaterings)
    }

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
            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Analytics header
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = ColorSage,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analytics Dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Track your overall habit tree consistency and watering records.",
                    fontSize = 12.sp,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary
                )
            }

            // Stat Cards - FOREST GROWTH LEDGER
            item {
                ForestGrowthLedgerCard(
                    plantedSeeds = totalSpecies,
                    totalWaterings = totalWaterings,
                    collectiveStreak = collectiveStreak,
                    consistencyIndex = consistencyIndex,
                    isDark = isDark
                )
            }

            // Eco-Timeline Synchronizer
            item {
                EcoTimelineCard(
                    completions = completions,
                    totalSpecies = totalSpecies,
                    isDark = isDark
                )
            }

            // Donut Overall Chart (Monthly overview proportion)
            item {
                MonthlyOverviewRatioCard(
                    consistencyIndex = consistencyIndex,
                    isDark = isDark
                )
            }

            // Wave Trend completions chart
            item {
                WaveTrendCard(
                    completions = completions,
                    isDark = isDark
                )
            }

            item { Spacer(modifier = Modifier.height(110.dp)) }
        }
    }
}

@Composable
fun ForestGrowthLedgerCard(
    plantedSeeds: Int,
    totalWaterings: Int,
    collectiveStreak: Int,
    consistencyIndex: Int,
    isDark: Boolean
) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = ColorSage,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FOREST GROWTH LEDGER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = if (isDark) ColorSage else BrandSage,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Active Ecosystem",
                    fontSize = 9.sp,
                    color = ColorSage,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ColorSage.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PLANTED SEEDS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$plantedSeeds Species",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL WATERINGS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalWaterings times",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "COLLECTIVE STREAK",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Collective Streak",
                            tint = GoldStreak,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${collectiveStreak}d",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CONSISTENCY INDEX",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Consistency Index",
                            tint = ColorSage,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$consistencyIndex%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EcoTimelineCard(
    completions: List<HabitCompletion>,
    totalSpecies: Int,
    isDark: Boolean
) {
    val today = LocalDate.now()
    val monthsShort = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) DarkClayCard else LightCreamCard
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isDark) DarkBorderColor else LightBorderColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ECO-TIMELINE SYNCHRONIZER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "SOLAR INTERVAL",
                    fontSize = 8.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Year selection list
            Text(
                text = "${today.year}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            // J F M A M J J A S O N D row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for ((index, m) in monthsShort.withIndex()) {
                    val isCurrentMonth = index + 1 == today.monthValue
                    
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCurrentMonth) {
                                    ColorSage.copy(alpha = 0.2f)
                                } else {
                                    if (isDark) Color(0xFF2E2C29) else Color(0xFFDDD9D1)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isCurrentMonth) ColorSage else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = m,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isCurrentMonth) ColorSage else (if (isDark) Color.LightGray else Color.DarkGray)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Month short title
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = today.month.getDisplayName(TextStyle.FULL, Locale.US).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )
                if (totalSpecies > 0) {
                    val todayCompletionRatio = (completions.count { it.date == today.toString() } * 100) / totalSpecies
                    Text(
                        text = "$todayCompletionRatio% watered",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorSage
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day matrix strip - dots of the last 15 days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val pastFortnight = (0..14).map { today.minusDays(it.toLong()) }.reversed()
                for (date in pastFortnight) {
                    val completedCount = completions.count { it.date == date.toString() }
                    val color = when {
                        completedCount == 0 -> if (isDark) Color(0xFF2E2C29) else Color(0xFFDDD9D1)
                        totalSpecies == 0 -> ColorSage
                        completedCount >= totalSpecies -> ColorSage // fully watered
                        else -> ColorSage.copy(alpha = 0.5f) // partially watered
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date == today) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyOverviewRatioCard(
    consistencyIndex: Int,
    isDark: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) DarkClayCard else LightCreamCard
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isDark) DarkBorderColor else LightBorderColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.US) + " Overview",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF262523) else Color(0xFFEDE9E3))
                ) {
                    Text(
                        text = "Month",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorSage,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Donut Canvas chart Replicating visual screens!
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val center = Offset(w/2, h/2)
                        val radius = w / 2 * 0.85f
                        val strokeWidth = 12f

                        // Consistent care segment
                        val consistentArc = (consistencyIndex.toFloat() / 100f) * 360f
                        // Missed segment
                        val missedArc = 360f - consistentArc

                        // Draw Missed/Delayed sector
                        drawArc(
                            color = ColorTerracotta,
                            startAngle = -90f + consistentArc,
                            sweepAngle = missedArc,
                            useCenter = false,
                            style = Stroke(width = strokeWidth),
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )

                        // Draw Consistent Care sector
                        drawArc(
                            color = ColorSage,
                            startAngle = -90f,
                            sweepAngle = consistentArc,
                            useCenter = false,
                            style = Stroke(width = strokeWidth + 2f),
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                    }
                    Text(
                        text = "$consistencyIndex%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = ColorSage,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Legend
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    LegendItem(color = ColorSage, text = "Consistent Care", percentage = "$consistencyIndex%")
                    LegendItem(color = ColorTerracotta, text = "Missed / Delayed", percentage = "${100 - consistencyIndex}%")
                    LegendItem(color = Color.LightGray, text = "Untapped Slots", percentage = "0%")
                }
            }
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    text: String,
    percentage: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = percentage,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun WaveTrendCard(
    completions: List<HabitCompletion>,
    isDark: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) DarkClayCard else LightCreamCard
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isDark) DarkBorderColor else LightBorderColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WAVE TREND (COMPLETIONS)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.LightGray,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Watering Peak Trend",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "${completions.size} total entries",
                    fontSize = 10.sp,
                    color = ColorSage,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Canvas Drawing gorgeous Sparkline Wave Trend Bezier curve of stats!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Simple mock points trend list
                    val points = listOf(
                        Offset(0f, h * 0.8f),
                        Offset(w * 0.2f, h * 0.75f),
                        Offset(w * 0.4f, h * 0.7f),
                        Offset(w * 0.6f, h * 0.78f),
                        Offset(w * 0.8f, h * 0.6f),
                        Offset(w, h * 0.4f) // rising streak
                    )

                    val path = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            val pPrev = points[i - 1]
                            val pCurr = points[i]
                            val controlX = (pPrev.x + pCurr.x) / 2
                            cubicTo(controlX, pPrev.y, controlX, pCurr.y, pCurr.x, pCurr.y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = ColorSage,
                        style = Stroke(width = 4f)
                    )

                    // Glow particle point at current peak
                    drawCircle(
                        color = ColorTerracotta,
                        radius = 4f,
                        center = points.last()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Month labels under sparkline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val months = listOf("Dec", "Jan", "Feb", "Mar", "Apr", "May")
                for (m in months) {
                    Text(
                        text = m,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
