package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TreeVisual(
    streak: Int,
    category: String,
    species: String = "CHERRY_BLOSSOM",
    modifier: Modifier = Modifier
) {
    val themeColor = getCategoryColor(category)
    val stageName = getStageName(streak)
    val isDark = MaterialTheme.colorScheme.background == DarkClayBg

    // Gentle wind sway animation
    val infiniteTransition = rememberInfiniteTransition(label = "sway")
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = SineIntensityEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swayAngle"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerX = w / 2
                val baseY = h * 0.8f
                
                // 1. Draw Ground / 3-Dimensional Moss & Soil Floating Island
                val isDarkTheme = isDark
                val mossColor1 = if (isDarkTheme) Color(0xFF425E4B) else Color(0xFF8CAF96)
                val mossColor2 = if (isDarkTheme) Color(0xFF2E4235) else Color(0xFF6E8F77)
                val soilColor1 = if (isDarkTheme) Color(0xFF4E342E) else Color(0xFF8D6E63)
                val soilColor2 = if (isDarkTheme) Color(0xFF2E1C1A) else Color(0xFF5D4037)

                // Bottom Soil/Clay Slab
                drawOval(
                    brush = Brush.verticalGradient(
                        colors = listOf(soilColor1, soilColor2),
                        startY = baseY - 4f,
                        endY = baseY + 18f
                    ),
                    topLeft = Offset(centerX - w * 0.35f, baseY - 6f),
                    size = Size(w * 0.7f, 24f)
                )

                // Top Vibrant Mossy Turf
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(mossColor1, mossColor2),
                        center = Offset(centerX, baseY - 3f),
                        radius = w * 0.33f
                    ),
                    topLeft = Offset(centerX - w * 0.34f, baseY - 10f),
                    size = Size(w * 0.68f, 18f)
                )
                
                // Fine glowing highlight of themeCategory around Turf rim
                drawOval(
                    color = themeColor.copy(alpha = 0.45f),
                    topLeft = Offset(centerX - w * 0.34f, baseY - 10f),
                    size = Size(w * 0.68f, 18f),
                    style = Stroke(width = 1.8f)
                )

                // 2. Draw Tree based on streak stage
                when {
                    streak < 1 -> {
                        // --- STAGE 1: DORMANT SEED (streak < 1) ---
                        // Tiny soil mound
                        drawArc(
                            color = Color(0xFF5A4538),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(centerX - 15f, baseY - 10f),
                            size = Size(30f, 15f)
                        )
                        // Tiny seed glow
                        drawCircle(
                            color = themeColor,
                            radius = 4f,
                            center = Offset(centerX, baseY - 8f)
                        )
                    }
                    streak < 3 -> {
                        // --- STAGE 2: GERMINATING SPROUT (1 <= streak < 3) ---
                        // Small soil mount
                        drawArc(
                            color = Color(0xFF5A4538),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(centerX - 12f, baseY - 8f),
                            size = Size(24f, 12f)
                        )
                        // Tiny curved stem
                        val stemPath = Path().apply {
                            moveTo(centerX, baseY)
                            quadraticTo(centerX + swayAngle, baseY - 15f, centerX + swayAngle * 1.2f, baseY - 28f)
                        }
                        drawPath(
                            path = stemPath,
                            color = Color(0xFF8D6E63),
                            style = Stroke(width = 3.5f)
                        )
                        // 2 Baby leaves
                        drawOval(
                            color = Color(0xFF81C784),
                            topLeft = Offset(centerX - 8f + swayAngle * 1.2f, baseY - 32f),
                            size = Size(8f, 5f)
                        )
                        drawOval(
                            color = Color(0xFF66BB6A),
                            topLeft = Offset(centerX + 1f + swayAngle * 1.2f, baseY - 31f),
                            size = Size(8f, 5f)
                        )
                    }
                    streak < 7 -> {
                        // --- STAGE 3: YOUNG SAPLING (3 <= streak < 7) ---
                        // Young thin trunk with split stems
                        val trunkPath = Path().apply {
                            moveTo(centerX, baseY)
                            quadraticTo(centerX + swayAngle, baseY - 18f, centerX + swayAngle * 1.5f, baseY - 38f)
                        }
                        drawPath(
                            path = trunkPath,
                            color = Color(0xFF795548),
                            style = Stroke(width = 5f)
                        )
                        val bx = centerX + swayAngle * 1.5f
                        val by = baseY - 38f
                        // Branches
                        drawLine(
                            color = Color(0xFF795548),
                            start = Offset(bx, by),
                            end = Offset(bx - 12f, by - 12f),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color(0xFF795548),
                            start = Offset(bx, by),
                            end = Offset(bx + 12f, by - 10f),
                            strokeWidth = 3f
                        )
                        // Leaf clusters of category theme color
                        drawCircle(
                            color = themeColor.copy(alpha = 0.8f),
                            radius = 8f,
                            center = Offset(bx - 12f, by - 14f)
                        )
                        drawCircle(
                            color = Color(0xFF81C784),
                            radius = 7f,
                            center = Offset(bx + 12f, by - 12f)
                        )
                        drawCircle(
                            color = themeColor,
                            radius = 10f,
                            center = Offset(bx, by - 6f)
                        )
                    }
                    streak < 14 -> {
                        // --- STAGE 4: THRIVING SAPLING (7 <= streak < 14) ---
                        val specLower = species.uppercase()
                        when {
                            specLower.contains("PINE") -> {
                                val trunk = Path().apply {
                                    moveTo(centerX - 2.5f, baseY)
                                    lineTo(centerX + 2.5f, baseY)
                                    lineTo(centerX + 1f + swayAngle, baseY - 42f)
                                    lineTo(centerX - 1f + swayAngle, baseY - 42f)
                                    close()
                                }
                                drawPath(path = trunk, color = Color(0xFF5D4037))
                                // Thriving Sprout Pine (Short Overlapping Pine layers)
                                val p1 = Path().apply {
                                    moveTo(centerX + swayAngle, baseY - 46f)
                                    lineTo(centerX - 15f + swayAngle, baseY - 24f)
                                    lineTo(centerX + 15f + swayAngle, baseY - 24f)
                                    close()
                                }
                                val p2 = Path().apply {
                                    moveTo(centerX + swayAngle, baseY - 55f)
                                    lineTo(centerX - 11f + swayAngle, baseY - 35f)
                                    lineTo(centerX + 11f + swayAngle, baseY - 35f)
                                    close()
                                }
                                drawPath(path = p1, color = themeColor.copy(alpha = 0.85f))
                                drawPath(path = p2, color = themeColor)
                            }
                            specLower.contains("OAK") -> {
                                val trunk = Path().apply {
                                    moveTo(centerX - 4f, baseY)
                                    lineTo(centerX + 4f, baseY)
                                    quadraticTo(centerX + 3f + swayAngle, baseY - 20f, centerX + 6f + swayAngle, baseY - 36f)
                                    lineTo(centerX - 6f + swayAngle, baseY - 36f)
                                    close()
                                }
                                drawPath(path = trunk, color = Color(0xFF5E493C))
                                val foliageColor = themeColor
                                drawCircle(color = foliageColor.copy(alpha = 0.7f), radius = 16f, center = Offset(centerX - 8f + swayAngle, baseY - 42f))
                                drawCircle(color = foliageColor, radius = 19f, center = Offset(centerX + swayAngle, baseY - 50f))
                            }
                            specLower.contains("PALM") -> {
                                // Dynamic curved trunk with ribbed textures
                                val trunk = Path().apply {
                                    moveTo(centerX - 2f, baseY)
                                    quadraticTo(centerX + swayAngle * 1.2f, baseY - 18f, centerX + swayAngle * 2.0f - 6f, baseY - 44f)
                                    lineTo(centerX + swayAngle * 2.0f - 3f, baseY - 44f)
                                    quadraticTo(centerX + swayAngle * 1.2f + 3f, baseY - 18f, centerX + 2f, baseY)
                                    close()
                                }
                                drawPath(path = trunk, color = Color(0xFF8D6E63))
                                
                                // Beautiful palm-ring horizontal rib textures
                                for (i in 1..4) {
                                    val t = i * 0.22f
                                    val px = centerX * (1-t) + (centerX + swayAngle * 2.0f - 5f)*t
                                    val py = baseY * (1-t) + (baseY - 44f)*t
                                    drawLine(
                                        color = Color(0xFF5D4037).copy(alpha = 0.5f),
                                        start = Offset(px - 3.5f, py),
                                        end = Offset(px + 3.5f, py),
                                        strokeWidth = 1.8f
                                    )
                                }

                                val palmX = centerX + swayAngle * 2.0f - 5f
                                val palmY = baseY - 44f
                                val leafColor = themeColor
                                
                                // Beautiful feather-like drooping fronds of palm
                                val frondAngles = listOf(-140f, -100f, -60f, -20f, 150f)
                                frondAngles.forEach { angle ->
                                    val rad = Math.toRadians(angle.toDouble())
                                    val endX = palmX + Math.cos(rad).toFloat() * 16f
                                    val endY = palmY + Math.sin(rad).toFloat() * 16f + 3f
                                    
                                    val frondPath = Path().apply {
                                        moveTo(palmX, palmY)
                                        quadraticTo(palmX + (endX - palmX)*0.4f, palmY - 5f, endX, endY)
                                    }
                                    drawPath(path = frondPath, color = leafColor, style = Stroke(width = 3.2f, cap = StrokeCap.Round))
                                }
                            }
                            specLower.contains("BONSAI") -> {
                                // Draw traditional ceramic bonsai pot at the base
                                drawRect(
                                    color = if (isDarkTheme) Color(0xFF2B2925) else Color(0xFFDCD6CB),
                                    topLeft = Offset(centerX - 15f, baseY - 3f),
                                    size = Size(30f, 6f)
                                )
                                drawRect(
                                    color = if (isDarkTheme) Color(0xFF1E1D1B) else Color(0xFFEDE7DC),
                                    topLeft = Offset(centerX - 13f, baseY - 2f),
                                    size = Size(26f, 5f)
                                )

                                // Classic winding gnarled Bonsai trunk
                                val trunk = Path().apply {
                                    moveTo(centerX - 1.5f, baseY - 3f)
                                    quadraticTo(centerX - 8f + swayAngle, baseY - 15f, centerX - 3f + swayAngle, baseY - 26f)
                                    quadraticTo(centerX + 8f + swayAngle, baseY - 32f, centerX + 1f + swayAngle, baseY - 40f)
                                }
                                drawPath(path = trunk, color = Color(0xFF432A22), style = Stroke(width = 4.8f, cap = StrokeCap.Round))
                                
                                val bColor = themeColor
                                // Foliage pads: cloud-like compact horizontal ovals
                                drawOval(color = bColor.copy(alpha = 0.6f), topLeft = Offset(centerX - 15f + swayAngle, baseY - 34f), size = Size(14f, 7f))
                                drawOval(color = bColor, topLeft = Offset(centerX - 12f + swayAngle, baseY - 35f), size = Size(11f, 6f))

                                drawOval(color = bColor.copy(alpha = 0.5f), topLeft = Offset(centerX - 10f + swayAngle, baseY - 47f), size = Size(18f, 8f))
                                drawOval(color = bColor, topLeft = Offset(centerX - 6f + swayAngle, baseY - 49f), size = Size(15f, 7f))
                            }
                            else -> {
                                val trunk = Path().apply {
                                    moveTo(centerX - 2.5f, baseY)
                                    lineTo(centerX + 2.5f, baseY)
                                    quadraticTo(centerX + 3.5f + swayAngle, baseY - 20f, centerX + 6f + swayAngle, baseY - 38f)
                                    lineTo(centerX - 5f + swayAngle, baseY - 38f)
                                    close()
                                }
                                drawPath(path = trunk, color = Color(0xFF5D4037))
                                val blossomColor = themeColor
                                drawCircle(color = blossomColor.copy(alpha = 0.6f), radius = 16f, center = Offset(centerX - 8f + swayAngle, baseY - 44f))
                                drawCircle(color = blossomColor, radius = 19f, center = Offset(centerX + swayAngle, baseY - 52f))
                            }
                        }
                    }
                    streak < 21 -> {
                        // --- STAGE 5: YOUNG TREE (14 <= streak < 21) ---
                        val specLower = species.uppercase()
                        when {
                            specLower.contains("PINE") -> {
                                val trunk = Path().apply {
                                    moveTo(centerX - 4f, baseY)
                                    lineTo(centerX + 4f, baseY)
                                    lineTo(centerX + 1.5f + swayAngle, baseY - 58f)
                                    lineTo(centerX - 1.5f + swayAngle, baseY - 58f)
                                    close()
                                }
                                drawPath(path = trunk, color = Color(0xFF5D4037))
                                val p1 = Path().apply {
                                    moveTo(centerX + swayAngle, baseY - 62f)
                                    lineTo(centerX - 22f + swayAngle, baseY - 32f)
                                    lineTo(centerX + 22f + swayAngle, baseY - 32f)
                                    close()
                                }
                                val p2 = Path().apply {
                                    moveTo(centerX + swayAngle, baseY - 74f)
                                    lineTo(centerX - 18f + swayAngle, baseY - 46f)
                                    lineTo(centerX + 18f + swayAngle, baseY - 46f)
                                    close()
                                }
                                val p3 = Path().apply {
                                    moveTo(centerX + swayAngle, baseY - 84f)
                                    lineTo(centerX - 13f + swayAngle, baseY - 58f)
                                    lineTo(centerX + 13f + swayAngle, baseY - 58f)
                                    close()
                                }
                                drawPath(path = p1, color = themeColor.copy(alpha = 0.8f))
                                drawPath(path = p2, color = themeColor.copy(alpha = 0.95f))
                                drawPath(path = p3, color = themeColor)
                            }
                            specLower.contains("OAK") -> {
                                val trunk = Path().apply {
                                    moveTo(centerX - 5.5f, baseY)
                                    lineTo(centerX + 5.5f, baseY)
                                    quadraticTo(centerX + 4.5f + swayAngle, baseY - 22f, centerX + 8f + swayAngle, baseY - 45f)
                                    lineTo(centerX - 8f + swayAngle, baseY - 45f)
                                    close()
                                }
                                drawPath(path = trunk, color = Color(0xFF5E493C))
                                val foliageColor = themeColor
                                drawCircle(color = foliageColor.copy(alpha = 0.6f), radius = 22f, center = Offset(centerX - 10f + swayAngle, baseY - 52f))
                                drawCircle(color = foliageColor.copy(alpha = 0.6f), radius = 22f, center = Offset(centerX + 10f + swayAngle, baseY - 52f))
                                drawCircle(color = foliageColor, radius = 26f, center = Offset(centerX + swayAngle, baseY - 64f))
                            }
                            specLower.contains("PALM") -> {
                                val trunk = Path().apply {
                                    moveTo(centerX - 2.8f, baseY)
                                    quadraticTo(centerX + swayAngle * 1.4f, baseY - 22f, centerX + swayAngle * 2.3f - 8f, baseY - 56f)
                                    lineTo(centerX + swayAngle * 2.3f - 4.5f, baseY - 56f)
                                    quadraticTo(centerX + swayAngle * 1.4f + 3.5f, baseY - 22f, centerX + 2.8f, baseY)
                                    close()
                                }
                                drawPath(path = trunk, color = Color(0xFF8D6E63))
                                
                                // Authentic ribbed textures on bending trunk
                                for (i in 1..6) {
                                    val t = i * 0.15f
                                    val px = centerX * (1-t) + (centerX + swayAngle * 2.3f - 6.5f)*t
                                    val py = baseY * (1-t) + (baseY - 56f)*t
                                    drawLine(
                                        color = Color(0xFF5D4037).copy(alpha = 0.5f),
                                        start = Offset(px - 4f, py),
                                        end = Offset(px + 4f, py),
                                        strokeWidth = 2.2f
                                    )
                                }

                                val palmX = centerX + swayAngle * 2.3f - 6.5f
                                val palmY = baseY - 56f
                                val leafColor = themeColor
                                
                                // Beautiful cascading multi-feather fronds
                                val frondAngles = listOf(-150f, -115f, -80f, -45f, -10f, 140f)
                                frondAngles.forEach { angle ->
                                    val rad = Math.toRadians(angle.toDouble())
                                    val endX = palmX + Math.cos(rad).toFloat() * 22f
                                    val endY = palmY + Math.sin(rad).toFloat() * 22f + 4f
                                    
                                    val frondPath = Path().apply {
                                        moveTo(palmX, palmY)
                                        quadraticTo(palmX + (endX - palmX)*0.45f, palmY - 6f, endX, endY)
                                    }
                                    drawPath(path = frondPath, color = leafColor, style = Stroke(width = 3.6f, cap = StrokeCap.Round))
                                }
                                // Small orange hanging flower or fresh coconut
                                drawCircle(color = Color(0xFFDD853B), radius = 3.5f, center = Offset(palmX - 2f, palmY + 3f))
                            }
                            specLower.contains("BONSAI") -> {
                                // Draw polished ceramic bonsai stand tray
                                drawRect(
                                    color = if (isDarkTheme) Color(0xFF282622) else Color(0xFFD3CCC0),
                                    topLeft = Offset(centerX - 18f, baseY - 3.5f),
                                    size = Size(36f, 7f)
                                )
                                drawRect(
                                    color = if (isDarkTheme) Color(0xFF1B1A18) else Color(0xFFEFE8DA),
                                    topLeft = Offset(centerX - 16f, baseY - 2.5f),
                                    size = Size(32f, 6f)
                                )

                                // Thickening wind-swept sculpted trunk
                                val trunk = Path().apply {
                                    moveTo(centerX - 3f, baseY - 3.5f)
                                    quadraticTo(centerX - 12f + swayAngle, baseY - 18f, centerX - 4f + swayAngle, baseY - 30f)
                                    quadraticTo(centerX + 12f + swayAngle, baseY - 38f, centerX + 1.5f + swayAngle, baseY - 48f)
                                }
                                drawPath(path = trunk, color = Color(0xFF38231E), style = Stroke(width = 6.2f, cap = StrokeCap.Round))
                                
                                val bColor = themeColor
                                // Asymmetric foliage cushions ("cloud plates") at multiple nodes
                                // Left Branch cushion
                                drawOval(color = bColor.copy(alpha = 0.5f), topLeft = Offset(centerX - 24f + swayAngle, baseY - 38f), size = Size(18f, 8f))
                                drawOval(color = bColor, topLeft = Offset(centerX - 21f + swayAngle, baseY - 39f), size = Size(14f, 7f))

                                // Right branch cushion
                                drawOval(color = bColor.copy(alpha = 0.6f), topLeft = Offset(centerX + 6f + swayAngle, baseY - 45f), size = Size(20f, 9f))
                                drawOval(color = bColor, topLeft = Offset(centerX + 8f + swayAngle, baseY - 46f), size = Size(16f, 8f))

                                // Main top central canopy cushion
                                drawOval(color = bColor.copy(alpha = 0.55f), topLeft = Offset(centerX - 12f + swayAngle, baseY - 56f), size = Size(26f, 10f))
                                drawOval(color = bColor, topLeft = Offset(centerX - 8f + swayAngle, baseY - 57f), size = Size(20f, 9f))
                            }
                            else -> {
                                val trunk = Path().apply {
                                    moveTo(centerX - 3.5f, baseY)
                                    lineTo(centerX + 3.5f, baseY)
                                    quadraticTo(centerX + 5f + swayAngle, baseY - 22f, centerX + 8f + swayAngle, baseY - 46f)
                                    lineTo(centerX - 7f + swayAngle, baseY - 46f)
                                    close()
                                }
                                drawPath(path = trunk, color = Color(0xFF5D4037))
                                val blossomColor = themeColor
                                drawCircle(color = blossomColor.copy(alpha = 0.55f), radius = 22f, center = Offset(centerX - 10f + swayAngle, baseY - 55f))
                                drawCircle(color = blossomColor.copy(alpha = 0.55f), radius = 22f, center = Offset(centerX + 10f + swayAngle, baseY - 55f))
                                drawCircle(color = blossomColor, radius = 26f, center = Offset(centerX + swayAngle, baseY - 66f))
                            }
                        }
                    }
                    streak < 30 -> {
                        // --- STAGE 6: MATURE TREE (21 <= streak < 30) ---
                        val specLower = species.uppercase()
                        when {
                            specLower.contains("PINE") -> {
                                val trunk = Path().apply {
                                    moveTo(centerX - 5.5f, baseY)
                                    lineTo(centerX + 5.5f, baseY)
                                    lineTo(centerX + 2.2f + swayAngle, baseY - 74f)
                                    lineTo(centerX - 2.2f + swayAngle, baseY - 74f)
                                    close()
                                }
                                drawPath(path = trunk, color = Color(0xFF4E342E))
                                val p1 = Path().apply {
                                    moveTo(centerX + swayAngle, baseY - 78f)
                                    lineTo(centerX - 28f + swayAngle, baseY - 40f)
                                    lineTo(centerX + 28f + swayAngle, baseY - 40f)
                                    close()
                                }
                                val p2 = Path().apply {
                                    moveTo(centerX + swayAngle, baseY - 92f)
                                    lineTo(centerX - 22f + swayAngle, baseY - 58f)
                                    lineTo(centerX + 22f + swayAngle, baseY - 58f)
                                    close()
                                }
                                val p3 = Path().apply {
                                    moveTo(centerX + swayAngle, baseY - 106f)
                                    lineTo(centerX - 16f + swayAngle, baseY - 74f)
                                    lineTo(centerX + 16f + swayAngle, baseY - 74f)
                                    close()
                                }
                                drawPath(path = p1, color = themeColor.copy(alpha = 0.85f))
                                drawPath(path = p2, color = themeColor)
                                drawPath(path = p3, color = themeColor.copy(alpha = 0.95f))
                            }
                            specLower.contains("OAK") -> {
                                val trunk = Path().apply {
                                    moveTo(centerX - 7f, baseY)
                                    lineTo(centerX + 7f, baseY)
                                    quadraticTo(centerX + 6f + swayAngle, baseY - 28f, centerX + 11f + swayAngle, baseY - 54f)
                                    lineTo(centerX - 11f + swayAngle, baseY - 54f)
                                    close()
                                }
                                drawPath(path = trunk, color = Color(0xFF4A3B32))
                                val foliageColor = themeColor
                                drawCircle(color = foliageColor.copy(alpha = 0.6f), radius = 28f, center = Offset(centerX - 14f + swayAngle, baseY - 62f))
                                drawCircle(color = foliageColor.copy(alpha = 0.6f), radius = 28f, center = Offset(centerX + 14f + swayAngle, baseY - 62f))
                                drawCircle(color = foliageColor, radius = 34f, center = Offset(centerX + swayAngle, baseY - 76f))
                            }
                            specLower.contains("PALM") -> {
                                val trunk = Path().apply {
                                    moveTo(centerX - 3.5f, baseY)
                                    quadraticTo(centerX + swayAngle * 1.5f, baseY - 28f, centerX + swayAngle * 2.6f - 11f, baseY - 72f)
                                    lineTo(centerX + swayAngle * 2.6f - 7f, baseY - 72f)
                                    quadraticTo(centerX + swayAngle * 1.5f + 4.5f, baseY - 28f, centerX + 3.5f, baseY)
                                    close()
                                }
                                drawPath(path = trunk, color = Color(0xFF7D5C4F))
                                
                                // Organic segmented ring details on the majestic palm trunk
                                for (i in 1..8) {
                                    val t = i * 0.11f
                                    val px = centerX * (1-t) + (centerX + swayAngle * 2.6f - 9f)*t
                                    val py = baseY * (1-t) + (baseY - 72f)*t
                                    drawLine(
                                        color = Color(0xFF4E342E).copy(alpha = 0.55f),
                                        start = Offset(px - 5f, py),
                                        end = Offset(px + 5f, py),
                                        strokeWidth = 2.4f
                                    )
                                }

                                val palmX = centerX + swayAngle * 2.6f - 9f
                                val palmY = baseY - 72f
                                val leafColor = themeColor
                                
                                // Seven full cascading feather-fronds
                                val frondAngles = listOf(-160f, -130f, -100f, -70f, -40f, -10f, 135f)
                                frondAngles.forEach { angle ->
                                    val rad = Math.toRadians(angle.toDouble())
                                    val endX = palmX + Math.cos(rad).toFloat() * 28f
                                    val endY = palmY + Math.sin(rad).toFloat() * 28f + 5f
                                    
                                    val frondPath = Path().apply {
                                        moveTo(palmX, palmY)
                                        quadraticTo(palmX + (endX - palmX)*0.46f, palmY - 7f, endX, endY)
                                    }
                                    drawPath(path = frondPath, color = leafColor, style = Stroke(width = 4.0f, cap = StrokeCap.Round))
                                }
                                
                                // Beautiful hanging coconuts under the crown intersection
                                drawCircle(color = Color(0xFF53382B), radius = 4.5f, center = Offset(palmX - 3.5f, palmY + 4f))
                                drawCircle(color = Color(0xFF442D22), radius = 4.5f, center = Offset(palmX + 2.5f, palmY + 5f))
                            }
                            specLower.contains("BONSAI") -> {
                                // Draw high-quality traditional ceramic Japanese bonsai planter tray
                                drawRect(
                                    color = if (isDarkTheme) Color(0xFF23221E) else Color(0xFFC7BEB0),
                                    topLeft = Offset(centerX - 22f, baseY - 4.5f),
                                    size = Size(44f, 9f)
                                )
                                drawRect(
                                    color = if (isDarkTheme) Color(0xFF131211) else Color(0xFFE5DCD0),
                                    topLeft = Offset(centerX - 19f, baseY - 3.5f),
                                    size = Size(38f, 8f)
                                )

                                // Sculpted old winding S-shaped ancient trunk
                                val trunk = Path().apply {
                                    moveTo(centerX - 4f, baseY - 4.5f)
                                    quadraticTo(centerX - 16f + swayAngle, baseY - 22f, centerX - 6f + swayAngle, baseY - 38f)
                                    quadraticTo(centerX + 16f + swayAngle, baseY - 50f, centerX + 3f + swayAngle, baseY - 62f)
                                }
                                drawPath(path = trunk, color = Color(0xFF2C1A16), style = Stroke(width = 8.0f, cap = StrokeCap.Round))
                                
                                val bColor = themeColor
                                // Layered stylized Bonsai cloud-needle pads
                                // Lower tier left
                                drawOval(color = bColor.copy(alpha = 0.5f), topLeft = Offset(centerX - 32f + swayAngle, baseY - 46f), size = Size(22f, 10f))
                                drawOval(color = bColor, topLeft = Offset(centerX - 28f + swayAngle, baseY - 47f), size = Size(16f, 8f))

                                // Mid tier right
                                drawOval(color = bColor.copy(alpha = 0.6f), topLeft = Offset(centerX + 12f + swayAngle, baseY - 54f), size = Size(24f, 11f))
                                drawOval(color = bColor, topLeft = Offset(centerX + 15f + swayAngle, baseY - 55f), size = Size(18f, 9f))

                                // Upper main cloud head
                                drawOval(color = bColor.copy(alpha = 0.55f), topLeft = Offset(centerX - 16f + swayAngle, baseY - 72f), size = Size(32f, 12f))
                                drawOval(color = bColor, topLeft = Offset(centerX - 11f + swayAngle, baseY - 73f), size = Size(24f, 11f))
                            }
                            else -> {
                                val trunk = Path().apply {
                                    moveTo(centerX - 5.5f, baseY)
                                    lineTo(centerX + 5.5f, baseY)
                                    quadraticTo(centerX + 7f + swayAngle, baseY - 28f, centerX + 11f + swayAngle, baseY - 56f)
                                    lineTo(centerX - 9f + swayAngle, baseY - 56f)
                                    close()
                                }
                                drawPath(path = trunk, color = Color(0xFF4A342F))
                                val blossomColor = themeColor
                                drawCircle(color = blossomColor.copy(alpha = 0.5f), radius = 28f, center = Offset(centerX - 14f + swayAngle, baseY - 68f))
                                drawCircle(color = blossomColor.copy(alpha = 0.7f), radius = 28f, center = Offset(centerX + 14f + swayAngle, baseY - 68f))
                                drawCircle(color = blossomColor, radius = 34f, center = Offset(centerX + swayAngle, baseY - 82f))
                            }
                        }
                    }
                    else -> {
                        // --- STAGE 7: ELDER FOREST TITAN (streak >= 30, grand monarch!) ---
                        val specLower = species.uppercase()
                        when {
                            specLower.contains("PINE") -> {
                                // Mighty heavy Pine setup
                                val trunk = Path().apply {
                                    moveTo(centerX - 10f, baseY)
                                    lineTo(centerX + 10f, baseY)
                                    lineTo(centerX + 4f + swayAngle, baseY - 80f)
                                    lineTo(centerX - 4f + swayAngle, baseY - 80f)
                                    close()
                                }
                                drawPath(path = trunk, color = Color(0xFF3E2723))
                                
                                // 4 Tall overlapping Pine layers
                                val p1 = Path().apply {
                                    moveTo(centerX + swayAngle, baseY - 85f)
                                    lineTo(centerX - 35f + swayAngle, baseY - 45f)
                                    lineTo(centerX + 35f + swayAngle, baseY - 45f)
                                    close()
                                }
                                val p2 = Path().apply {
                                    moveTo(centerX + swayAngle, baseY - 105f)
                                    lineTo(centerX - 28f + swayAngle, baseY - 65f)
                                    lineTo(centerX + 28f + swayAngle, baseY - 65f)
                                    close()
                                }
                                val p3 = Path().apply {
                                    moveTo(centerX + swayAngle, baseY - 120f)
                                    lineTo(centerX - 22f + swayAngle, baseY - 80f)
                                    lineTo(centerX + 22f + swayAngle, baseY - 80f)
                                    close()
                                }
                                drawPath(path = p1, color = themeColor.copy(alpha = 0.8f))
                                drawPath(path = p2, color = themeColor.copy(alpha = 0.9f))
                                drawPath(path = p3, color = themeColor)
                            }
                            specLower.contains("OAK") -> {
                                // Heavy magnificent ancient organic trunk root system
                                val ancientTrunk = Path().apply {
                                    moveTo(centerX - 18f, baseY)
                                    quadraticTo(centerX - 8f, baseY - 15f, centerX - 10f + swayAngle, baseY - 40f)
                                    lineTo(centerX + 10f + swayAngle, baseY - 40f)
                                    quadraticTo(centerX + 8f, baseY - 15f, centerX + 18f, baseY)
                                    lineTo(centerX - 18f, baseY)
                                }
                                drawPath(path = ancientTrunk, color = Color(0xFF3E2723))

                                val foliageColor = themeColor
                                // Large beautiful foliage canopy in categories colors
                                drawCircle(color = foliageColor.copy(alpha = 0.4f), radius = 35f, center = Offset(centerX - 25f + swayAngle, baseY - 65f))
                                drawCircle(color = foliageColor.copy(alpha = 0.4f), radius = 35f, center = Offset(centerX + 25f + swayAngle, baseY - 65f))
                                drawCircle(color = foliageColor.copy(alpha = 0.8f), radius = 38f, center = Offset(centerX - 12f + swayAngle, baseY - 82f))
                                drawCircle(color = foliageColor.copy(alpha = 0.8f), radius = 38f, center = Offset(centerX + 12f + swayAngle, baseY - 82f))
                                drawCircle(color = foliageColor, radius = 42f, center = Offset(centerX + swayAngle, baseY - 95f))
                            }
                            specLower.contains("PALM") -> {
                                // Double iconic palm paradise cluster representing ultimate master growth!
                                val trunk1 = Path().apply {
                                    moveTo(centerX - 5f, baseY)
                                    quadraticTo(centerX - 10f + swayAngle * 1.5f, baseY - 40f, centerX - 12f + swayAngle * 2.5f, baseY - 85f)
                                }
                                val trunk2 = Path().apply {
                                    moveTo(centerX + 3f, baseY)
                                    quadraticTo(centerX + 8f + swayAngle * 0.8f, baseY - 35f, centerX + 18f + swayAngle * 1.8f, baseY - 74f)
                                }
                                drawPath(path = trunk1, color = Color(0xFF6E4C3D), style = Stroke(width = 7f, cap = StrokeCap.Round))
                                drawPath(path = trunk2, color = Color(0xFF5D4037), style = Stroke(width = 5.5f, cap = StrokeCap.Round))

                                // Ring segments on both trunks
                                for (i in 1..9) {
                                    val t = i * 0.11f
                                    val px1 = centerX * (1-t) + (centerX - 12f + swayAngle * 2.5f)*t
                                    val py1 = baseY * (1-t) + (baseY - 85f)*t
                                    drawLine(
                                        color = Color(0xFF321E15).copy(alpha = 0.5f),
                                        start = Offset(px1 - 4f, py1),
                                        end = Offset(px1 + 4f, py1),
                                        strokeWidth = 2.4f
                                    )
                                    
                                    if (i < 8) {
                                        val px2 = centerX * (1-t) + (centerX + 18f + swayAngle * 1.8f)*t
                                        val py2 = baseY * (1-t) + (baseY - 74f)*t
                                        drawLine(
                                            color = Color(0xFF321E15).copy(alpha = 0.5f),
                                            start = Offset(px2 - 3f, py2),
                                            end = Offset(px2 + 3f, py2),
                                            strokeWidth = 2.0f
                                        )
                                    }
                                }

                                val leafColor = themeColor
                                
                                // Crown 1 (Main tree)
                                val palmX1 = centerX - 12f + swayAngle * 2.5f
                                val palmY1 = baseY - 85f
                                val angles1 = listOf(-160f, -130f, -100f, -70f, -40f, -10f, 135f)
                                angles1.forEach { angle ->
                                    val rad = Math.toRadians(angle.toDouble())
                                    val endX = palmX1 + Math.cos(rad).toFloat() * 32f
                                    val endY = palmY1 + Math.sin(rad).toFloat() * 32f + 5f
                                    val frondPath = Path().apply {
                                        moveTo(palmX1, palmY1)
                                        quadraticTo(palmX1 + (endX - palmX1)*0.45f, palmY1 - 8f, endX, endY)
                                    }
                                    drawPath(path = frondPath, color = leafColor, style = Stroke(width = 4.2f, cap = StrokeCap.Round))
                                }
                                
                                // Crown 2 (Side tree)
                                val palmX2 = centerX + 18f + swayAngle * 1.8f
                                val palmY2 = baseY - 74f
                                val angles2 = listOf(-140f, -100f, -60f, -20f, 150f)
                                angles2.forEach { angle ->
                                    val rad = Math.toRadians(angle.toDouble())
                                    val endX = palmX2 + Math.cos(rad).toFloat() * 24f
                                    val endY = palmY2 + Math.sin(rad).toFloat() * 24f + 4f
                                    val frondPath = Path().apply {
                                        moveTo(palmX2, palmY2)
                                        quadraticTo(palmX2 + (endX - palmX2)*0.45f, palmY2 - 6f, endX, endY)
                                    }
                                    drawPath(path = frondPath, color = leafColor.copy(alpha = 0.85f), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
                                }

                                // Coconut clusters on both crowns
                                drawCircle(color = Color(0xFF422B1E), radius = 5f, center = Offset(palmX1 - 4f, palmY1 + 5f))
                                drawCircle(color = Color(0xFF301E14), radius = 5f, center = Offset(palmX1 + 3f, palmY1 + 6f))
                                drawCircle(color = Color(0xFF422B1E), radius = 4f, center = Offset(palmX2 - 2f, palmY2 + 4f))
                            }
                            specLower.contains("BONSAI") -> {
                                // Draw highly polished magnificent royal ceramic bonsai tray base
                                drawRect(
                                    color = if (isDarkTheme) Color(0xFF22211E) else Color(0xFFB8B0A2),
                                    topLeft = Offset(centerX - 26f, baseY - 5f),
                                    size = Size(52f, 10f)
                                )
                                drawRect(
                                    color = if (isDarkTheme) Color(0xFF100F0F) else Color(0xFFDFD6C9),
                                    topLeft = Offset(centerX - 22f, baseY - 4f),
                                    size = Size(44f, 8f)
                                )

                                // Thick magnificent multi-gnarled ancient trunk
                                val trunk = Path().apply {
                                    moveTo(centerX - 6f, baseY - 4f)
                                    quadraticTo(centerX - 22f, baseY - 25f, centerX - 10f + swayAngle, baseY - 45f)
                                    quadraticTo(centerX + 25f, baseY - 65f, centerX + 4f + swayAngle, baseY - 80f)
                                }
                                drawPath(path = trunk, color = Color(0xFF211311), style = Stroke(width = 11.0f, cap = StrokeCap.Round))
                                
                                val bColor = themeColor
                                // Layered ancient cloud cushions for Master Bonsai
                                // Tier 1: Far Left Drop cushion
                                drawOval(color = bColor.copy(alpha = 0.45f), topLeft = Offset(centerX - 38f + swayAngle, baseY - 50f), size = Size(26f, 11f))
                                drawOval(color = bColor, topLeft = Offset(centerX - 33f + swayAngle, baseY - 51f), size = Size(18f, 9f))

                                // Tier 2: Mid Right pad
                                drawOval(color = bColor.copy(alpha = 0.5f), topLeft = Offset(centerX + 14f + swayAngle, baseY - 70f), size = Size(24f, 11f))
                                drawOval(color = bColor, topLeft = Offset(centerX + 17f + swayAngle, baseY - 71f), size = Size(18f, 9f))

                                // Tier 3: High Central main cushion
                                drawOval(color = bColor.copy(alpha = 0.6f), topLeft = Offset(centerX - 22f + swayAngle, baseY - 92f), size = Size(36f, 13f))
                                drawOval(color = bColor, topLeft = Offset(centerX - 16f + swayAngle, baseY - 93f), size = Size(26f, 11f))

                                // Tier 4: Dynamic offset crown sprout
                                drawOval(color = bColor.copy(alpha = 0.75f), topLeft = Offset(centerX + 2f + swayAngle, baseY - 86f), size = Size(14f, 7f))
                            }
                            else -> {
                                // CHERRY_BLOSSOM (Foliage beautiful pink petals)
                                val ancientTrunk = Path().apply {
                                    moveTo(centerX - 16f, baseY)
                                    quadraticTo(centerX - 6f, baseY - 15f, centerX - 8f + swayAngle, baseY - 40f)
                                    lineTo(centerX + 8f + swayAngle, baseY - 40f)
                                    quadraticTo(centerX + 6f, baseY - 15f, centerX + 16f, baseY)
                                    lineTo(centerX - 16f, baseY)
                                }
                                drawPath(path = ancientTrunk, color = Color(0xFF4A342F))

                                val blossomColor = themeColor
                                // Pink floral canopy
                                drawCircle(color = blossomColor.copy(alpha = 0.4f), radius = 35f, center = Offset(centerX - 25f + swayAngle, baseY - 65f))
                                drawCircle(color = blossomColor.copy(alpha = 0.4f), radius = 35f, center = Offset(centerX + 25f + swayAngle, baseY - 65f))
                                drawCircle(color = blossomColor.copy(alpha = 0.8f), radius = 38f, center = Offset(centerX - 12f + swayAngle, baseY - 82f))
                                drawCircle(color = blossomColor.copy(alpha = 0.8f), radius = 38f, center = Offset(centerX + 12f + swayAngle, baseY - 82f))
                                drawCircle(color = blossomColor, radius = 42f, center = Offset(centerX + swayAngle, baseY - 95f))
                                
                                // Glowing star particle spores floating above Cherry tree
                                drawCircle(color = Color.White.copy(alpha = 0.7f), radius = 2.5f, center = Offset(centerX - 15f + swayAngle * 0.5f, baseY - 125f))
                                drawCircle(color = blossomColor.copy(alpha = 0.9f), radius = 3f, center = Offset(centerX + 25f + swayAngle * 0.8f, baseY - 115f))
                            }
                        }
                    }
                }
            }
        }
        
        // Growth stage pill status label
        Text(
            text = "$stageName (${streak}d)",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

val SineIntensityEasing = Easing { fraction ->
    val rad = fraction * Math.PI * 2
    Math.sin(rad).toFloat()
}

fun getStageName(streak: Int): String {
    return when {
        streak < 1 -> "Dormant Seed"
        streak < 3 -> "Germinating Sprout"
        streak < 7 -> "Young Sapling"
        streak < 14 -> "Thriving Sapling"
        streak < 21 -> "Young Tree"
        streak < 30 -> "Mature Tree"
        else -> "Elder Forest Titan"
    }
}

fun getCategoryColor(category: String): Color {
    return when (category.uppercase()) {
        "SAGE" -> ColorSage
        "OCEAN" -> ColorOcean
        "LAVENDER" -> ColorLavender
        "TERRACOTTA" -> ColorTerracotta
        "CRIMSON" -> ColorCrimson
        else -> BrandSage
    }
}
