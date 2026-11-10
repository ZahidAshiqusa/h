package com.zaidashiq.ultra.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.ObsidianNavy
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusPairing
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.zaidashiq.ultra.database.ActivityLogEntity
import com.zaidashiq.ultra.engine.EngineRuntimeSnapshot
import com.zaidashiq.ultra.engine.WhatsAppConnectionState
import com.zaidashiq.ultra.repositories.LocalAppSettings
import com.zaidashiq.ultra.ui.components.CyberGlassCard
import com.zaidashiq.ultra.ui.components.ModuleTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityLogsScreen(
    connectionState: WhatsAppConnectionState,
    logs: List<ActivityLogEntity>,
    onBack: () -> Unit,
    onClearLogs: () -> Unit
) {
    var selectedLevel by rememberSaveable { mutableStateOf("ALL") }
    var searchText by rememberSaveable { mutableStateOf("") }
    val fmt = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }

    val filtered = logs.filter { entry ->
        val levelOk = selectedLevel == "ALL" || entry.level == selectedLevel
        val queryOk = searchText.isBlank() ||
            entry.details.contains(searchText, ignoreCase = true) ||
            entry.module.contains(searchText, ignoreCase = true)
        levelOk && queryOk
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = "Activity & Security Logs",
            subtitle = "Structured Engine, Command & Keystore Audit Trail",
            connectionState = connectionState,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                CyberGlassCard(accentBorderColor = ElectricCyan) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STRUCTURED CONSOLE (${filtered.size} ENTRIES)",
                            style = MaterialTheme.typography.titleMedium,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = onClearLogs,
                            modifier = Modifier.testTag("clear_logs_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CLEAR")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Filter Logs by Module or Keyword") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL", "ENGINE", "SECURITY", "INFO", "WARN", "ERROR").forEach { lvl ->
                            FilterChip(
                                selected = selectedLevel == lvl,
                                onClick = { selectedLevel = lvl },
                                label = { Text(lvl, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            items(filtered, key = { it.id }) { log ->
                val badgeColor = when (log.level) {
                    "SECURITY" -> NeonViolet
                    "ENGINE" -> ElectricCyan
                    "ERROR" -> StatusError
                    "WARN" -> StatusPairing
                    else -> StatusConnected
                }
                CyberGlassCard(accentBorderColor = badgeColor) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "[${log.level}] ${log.module}",
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = fmt.format(Date(log.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = log.details,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    snapshot: EngineRuntimeSnapshot,
    settings: LocalAppSettings,
    storageBreakdown: Map<String, Long>,
    onBack: () -> Unit,
    onUpdateSettings: ((LocalAppSettings) -> LocalAppSettings) -> Unit,
    onDisconnectSession: (Context) -> Unit,
    onDeleteSession: (Context) -> Unit,
    onClearLogs: () -> Unit,
    onExportSettingsBackup: () -> String,
    onImportSettingsBackup: (String) -> Unit,
    onWipeAllLocalData: (Context) -> Unit
) {
    val context = LocalContext.current
    var backupPayloadText by rememberSaveable { mutableStateOf("") }
    var showWipeConfirm by remember { mutableStateOf(false) }

    if (showWipeConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeConfirm = false },
            title = { Text("Wipe All Local Application Data?") },
            text = {
                Text("This permanently erases encrypted WhatsApp sessions, chat history, contacts, custom automation rules, and resets preferences to factory defaults.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWipeConfirm = false
                        onWipeAllLocalData(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("Wipe Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = "Suite Settings",
            subtitle = "100% Standalone Local Configuration (Zero Cloud)",
            connectionState = snapshot.connectionState,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. General Settings
            item {
                CyberGlassCard(accentBorderColor = ElectricCyan) {
                    Text(
                        text = "1. GENERAL & APPEARANCE",
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Electric Cyan Primary Accent", color = TextPrimary)
                            Text("Switch between Cyber Cyan and Royal Violet accent", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Switch(
                            checked = settings.accentCyan,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(accentCyan = v) } }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Persistent Foreground Notifications", color = TextPrimary)
                            Text("Keep real-time status notification active while engine runs", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(notificationsEnabled = v) } }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Resume Saved Session on Device Boot", color = TextPrimary)
                            Text("Uses RECEIVE_BOOT_COMPLETED only when explicitly enabled", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Switch(
                            checked = settings.startOnBootEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(startOnBootEnabled = v) } }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Preferred Responder Language", color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("EN", "UR", "ROMAN_UR").forEach { lang ->
                            FilterChip(
                                selected = settings.uiLanguage == lang,
                                onClick = { onUpdateSettings { it.copy(uiLanguage = lang) } },
                                label = { Text(lang) }
                            )
                        }
                    }
                }
            }

            // 2. WhatsApp Session & Reconnection Settings
            item {
                CyberGlassCard(accentBorderColor = StatusConnected) {
                    Text(
                        text = "2. WHATSAPP CONNECTION & SESSION RECOVERY",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusConnected,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connection Status: ${snapshot.connectionState.name}",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Saved Session: ${snapshot.savedSession?.phoneNumber ?: "None (Unpaired)"}",
                        color = TextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Exponential Backoff Auto-Reconnect", color = TextPrimary)
                            Text("Automatically recover connection (2s → 4s → 8s → 16s → 32s)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Switch(
                            checked = settings.autoReconnectEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(autoReconnectEnabled = v) } }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onDisconnectSession(context) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("DISCONNECT")
                        }
                        Button(
                            onClick = { onDeleteSession(context) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                        ) {
                            Text("DELETE SESSION")
                        }
                    }
                }
            }

            // 3. Storage & Encrypted Backup/Restore
            item {
                val totalBytes = storageBreakdown.values.sum()
                CyberGlassCard(accentBorderColor = NeonViolet) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = NeonViolet)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "3. PRIVATE ANDROID STORAGE & ENCRYPTED BACKUP",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonViolet,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total App-Private Footprint: ${totalBytes / 1024} KB",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    storageBreakdown.forEach { (folder, bytes) ->
                        Text(
                            text = "• files/whatsapp/$folder: $bytes bytes",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                backupPayloadText = onExportSettingsBackup()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_settings_backup_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = ObsidianNavy
                            )
                        ) {
                            Icon(Icons.Default.SaveAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("EXPORT")
                        }
                        OutlinedButton(
                            onClick = { onImportSettingsBackup(backupPayloadText) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_settings_backup_button")
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("IMPORT")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupPayloadText,
                        onValueChange = { backupPayloadText = it },
                        label = { Text("AES-256-GCM Encrypted Settings Backup Payload") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClearLogs,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CLEAR LOGS")
                        }
                        Button(
                            onClick = { showWipeConfirm = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wipe_all_data_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CLEAR ALL DATA")
                        }
                    }
                }
            }
        }
    }
}
