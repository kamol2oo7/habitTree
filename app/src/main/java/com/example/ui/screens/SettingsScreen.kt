package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.ui.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val isCalendarStrip by viewModel.isCalendarStripLayout.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showResetDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

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

            // Title
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = ColorSage,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "System Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Manage local databases, trigger backups, or reset your tree inventory.",
                    fontSize = 12.sp,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary
                )
            }

            // Group 1: Preference settings
            item {
                Text(
                    text = "PREFERENCES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ColorSage,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) DarkClayCard else LightCreamCard
                    ),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(
                        1.dp,
                        if (isDark) DarkBorderColor else LightBorderColor
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Theme Toggle Preference Custom Selector row
                        SettingsRow(
                            icon = Icons.Default.Palette,
                            title = "Theme Atmosphere",
                            subtitle = "Switch between Dark Mud/Clay and Soft Cream theme",
                            action = {
                                Switch(
                                    checked = isDark,
                                    onCheckedChange = { viewModel.toggleTheme() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = BrandSage)
                                )
                            }
                        )
                        
                        HorizontalDivider(color = if (isDark) Color(0xFF2E2C29) else Color(0xFFEFECE7), thickness = 1.dp)

                        // Layout preference settings selector row
                        SettingsRow(
                            icon = Icons.Default.GridView,
                            title = "Tracking Grid Layout",
                            subtitle = if (isCalendarStrip) "Calendar Strip (Compact Capsule Bar)" else "Mini Heatmap (Calendar Aligned Grid)",
                            action = {
                                Switch(
                                    checked = isCalendarStrip,
                                    onCheckedChange = { viewModel.setGridLayoutSetting(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = BrandSage)
                                )
                            }
                        )
                    }
                }
            }

            // Group 2: Backup operations
            item {
                Text(
                    text = "BACKUP & SECURE PORT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ColorSage,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) DarkClayCard else LightCreamCard
                    ),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(
                        1.dp,
                        if (isDark) DarkBorderColor else LightBorderColor
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsRow(
                            icon = Icons.Default.CloudUpload,
                            title = "Export JSON Configuration",
                            subtitle = "Copy your current database as raw backup text block",
                            onClick = { showBackupDialog = true }
                        )

                        HorizontalDivider(color = if (isDark) Color(0xFF2E2C29) else Color(0xFFEFECE7), thickness = 1.dp)

                        SettingsRow(
                            icon = Icons.Default.CloudDownload,
                            title = "Restore from Raw Text JSON",
                            subtitle = "Paste previous exported backups to restore forest tree progress",
                            onClick = { showRestoreDialog = true }
                        )
                    }
                }
            }

            // Group 3: Danger Actions Erase Settings standard
            item {
                Text(
                    text = "DANGER ZONE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ColorTerracotta,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) DarkClayCard else LightCreamCard
                    ),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(
                        1.dp,
                        if (isDark) DarkBorderColor else LightBorderColor
                    )
                ) {
                    SettingsRow(
                        icon = Icons.Default.DeleteForever,
                        title = "Erase and Clean Database",
                        subtitle = "Irreversibly delete everything including streaks and giant Elder Titans",
                        iconColor = ColorTerracotta,
                        onClick = { showResetDialog = true }
                    )
                }
            }

            // Group 4: Device/App spec info
            item {
                Text(
                    text = "SYSTEM METADATA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1B1A18) else LightCreamCard
                    ),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(
                        1.dp,
                        if (isDark) DarkBorderColor else LightBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Habitree Client v1.0.0 (Compose)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "A fully local system storing metrics in an encrypted Room SQLite container. Growth matrices recalculated consecutive daily checkoffs.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(110.dp)) }
        }
    }

    // Modal dialogues
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Erase Entire Forest?") },
            text = { Text("This is atomic and completely irreversible. It will wipe all custom habits, water records, and reset the forest development matrices from scratch.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHabits()
                        Toast.makeText(context, "Ecosystem cleared", Toast.LENGTH_SHORT).show()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorTerracotta)
                ) {
                    Text("Decline & Erase")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Keep Safe")
                }
            }
        )
    }

    // Backup copy display dialog
    if (showBackupDialog) {
        val backupText = viewModel.exportBackupJson()
        
        Dialog(
            onDismissRequest = { showBackupDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.7f)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) DarkClayCard else LightCreamCard),
                border = BorderStroke(
                    1.dp,
                    if (isDark) DarkBorderColor else LightBorderColor
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Database Backup Export",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Copy this JSON block. You can paste it in other installs to restore species states.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Text block
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF1B1A18) else Color(0xFFF1EEE8))
                            .padding(8.dp)
                    ) {
                        SelectionContainer {
                            LazyColumn {
                                item {
                                    Text(
                                        text = backupText,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showBackupDialog = false }) {
                            Text("Dismiss")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(backupText))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                showBackupDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandSage)
                        ) {
                            Text("Copy JSON")
                        }
                    }
                }
            }
        }
    }

    // Restore raw paste dialog
    if (showRestoreDialog) {
        var rawInputText by remember { mutableStateOf("") }
        
        Dialog(
            onDismissRequest = { showRestoreDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) DarkClayCard else LightCreamCard),
                border = BorderStroke(
                    1.dp,
                    if (isDark) DarkBorderColor else LightBorderColor
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Import Backup Session",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Paste exported JSON block below to restore habits and completed entries.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = rawInputText,
                        onValueChange = { rawInputText = it },
                        placeholder = { Text("Paste exported JSON here...") },
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandSage,
                            focusedLabelColor = BrandSage
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showRestoreDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (rawInputText.isNotEmpty()) {
                                    val success = viewModel.importBackupJson(rawInputText)
                                    if (success) {
                                        Toast.makeText(context, "Data successfully restored!", Toast.LENGTH_SHORT).show()
                                        showRestoreDialog = false
                                    } else {
                                        Toast.makeText(context, "Error: Invalid JSON schema format", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandSage),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Verify & Restore")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color = ColorSage,
    action: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.Gray,
                lineHeight = 14.sp
            )
        }
        
        if (action != null) {
            Spacer(modifier = Modifier.width(8.dp))
            action()
        }
    }
}
