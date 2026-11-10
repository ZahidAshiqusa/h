package com.zaidashiq.ultra.ui.dashboard

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.ObsidianNavy
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusPairing
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.zaidashiq.ultra.engine.EngineRuntimeSnapshot
import com.zaidashiq.ultra.repositories.LocalAppSettings
import com.zaidashiq.ultra.ui.UltraModule
import com.zaidashiq.ultra.ui.components.ConnectionStatusBadge
import com.zaidashiq.ultra.ui.components.CyberGlassCard
import com.zaidashiq.ultra.ui.components.TelemetryStatTile

data class DashboardModuleTile(
    val module: UltraModule,
    val title: String,
    val subtitle: String,
    val badge: String,
    val icon: ImageVector,
    val accent: Color,
    val testTag: String
)

@Composable
fun FirstLaunchSplashScreen(
    onEnterDashboard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF132247),
                        ObsidianNavy
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CyberGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentBorderColor = ElectricCyan
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .border(2.dp, ElectricCyan, RoundedCornerShape(26.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_fg_1794296003614),
                        contentDescription = "ZAID ASHIQ X ULTRA V. CODER Emblem",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "ZAID ASHIQ X ULTRA V. CODER",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your Personal WhatsApp Automation Suite",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "INSTALL → OPEN → PAIR WHATSAPP → START\n100% Standalone Android Execution • Zero Cloud • Android Keystore AES-256-GCM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = onEnterDashboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("enter_dashboard_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = ObsidianNavy
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LAUNCH COMMAND SUITE",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun MainDashboardScreen(
    context: Context,
    snapshot: EngineRuntimeSnapshot,
    settings: LocalAppSettings,
    activeScheduledTasksCount: Int,
    enabledPluginsCount: Int,
    onNavigate: (UltraModule) -> Unit,
    onStartEngine: (Context) -> Unit,
    onStopEngine: (Context) -> Unit
) {
    val modules = listOf(
        DashboardModuleTile(
            module = UltraModule.PAIRING,
            title = "1. Device Pairing",
            subtitle = "QR Code, 8-Char Pairing Code & Keystore Session",
            badge = if (snapshot.savedSession != null) "PAIRED" else "PAIR NOW",
            icon = Icons.Default.QrCodeScanner,
            accent = ElectricCyan,
            testTag = "module_device_pairing"
        ),
        DashboardModuleTile(
            module = UltraModule.AUTOMATION,
            title = "2. WhatsApp Automation",
            subtitle = "Keyword Rules, Welcome/Away, Delay & Rate Limits",
            badge = if (settings.autoReplyEnabled) "ACTIVE" else "PAUSED",
            icon = Icons.Default.AutoAwesome,
            accent = NeonViolet,
            testTag = "module_whatsapp_automation"
        ),
        DashboardModuleTile(
            module = UltraModule.MESSAGE_CENTER,
            title = "3. Message Center",
            subtitle = "Manual Dispatch, Live Simulator & Templates",
            badge = "${settings.totalMessagesProcessed} MSGS",
            icon = Icons.AutoMirrored.Filled.Message,
            accent = StatusInfo,
            testTag = "module_message_center"
        ),
        DashboardModuleTile(
            module = UltraModule.CONTACTS,
            title = "4. Contacts Manager",
            subtitle = "Session Contacts, Opt-In Rules & VCF Export",
            badge = "VCF 3.0",
            icon = Icons.Default.Contacts,
            accent = StatusConnected,
            testTag = "module_contacts_manager"
        ),
        DashboardModuleTile(
            module = UltraModule.CHAT_EXPORT,
            title = "5. Chat Export",
            subtitle = "Export Session Chats to JSON & Text Transcripts",
            badge = "JSON / TXT",
            icon = Icons.AutoMirrored.Filled.Chat,
            accent = ElectricCyan,
            testTag = "module_chat_export"
        ),
        DashboardModuleTile(
            module = UltraModule.STATUS_PUBLISHER,
            title = "6. Status Publisher",
            subtitle = "Publish Image, Video & Text Status Updates",
            badge = "MEDIA",
            icon = Icons.Default.WifiTethering,
            accent = StatusPairing,
            testTag = "module_status_publisher"
        ),
        DashboardModuleTile(
            module = UltraModule.MULTILINGUAL_RESPONDER,
            title = "7. Multilingual Responder",
            subtitle = "Offline English, Urdu & Roman Urdu Local Engine",
            badge = "LOCAL ONLY",
            icon = Icons.Default.Translate,
            accent = NeonViolet,
            testTag = "module_multilingual_responder"
        ),
        DashboardModuleTile(
            module = UltraModule.SCHEDULER,
            title = "8. Scheduler",
            subtitle = "SQLite Persistent Queue & Delayed Messages",
            badge = "$activeScheduledTasksCount PENDING",
            icon = Icons.Default.Schedule,
            accent = StatusInfo,
            testTag = "module_scheduler"
        ),
        DashboardModuleTile(
            module = UltraModule.COMMAND_LIBRARY,
            title = "9. Command Library",
            subtitle = "13 Prefix Commands (${settings.commandPrefix}help, ${settings.commandPrefix}ping) & Sandbox",
            badge = "${settings.totalCommandsExecuted} RUNS",
            icon = Icons.Default.Code,
            accent = ElectricCyan,
            testTag = "module_command_library"
        ),
        DashboardModuleTile(
            module = UltraModule.PLUGIN_MANAGER,
            title = "10. Plugin Manager",
            subtitle = "Local JS Modules, Validator & Permission Guard",
            badge = "$enabledPluginsCount ENABLED",
            icon = Icons.Default.Extension,
            accent = StatusConnected,
            testTag = "module_plugin_manager"
        ),
        DashboardModuleTile(
            module = UltraModule.ACTIVITY_LOGS,
            title = "11. Activity Logs",
            subtitle = "Structured Engine, Command & Security Console",
            badge = "LIVE",
            icon = Icons.Default.Terminal,
            accent = StatusPairing,
            testTag = "module_activity_logs"
        ),
        DashboardModuleTile(
            module = UltraModule.SETTINGS,
            title = "12. Settings",
            subtitle = "Theme, Prefix, Rate Limits & Encrypted Storage",
            badge = "LOCAL",
            icon = Icons.Default.Settings,
            accent = NeonViolet,
            testTag = "module_settings"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Banner Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, ElectricCyan.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner_1794296003132),
                    contentDescription = "Cyber Command Console Header",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(215.dp)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ObsidianNavy.copy(alpha = 0.55f),
                                    ObsidianNavy.copy(alpha = 0.93f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (snapshot.engineRunning) StatusConnected else StatusPairing)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (snapshot.engineRunning) "ENGINE RUNNING (FOREGROUND)" else "ENGINE STANDBY",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (snapshot.engineRunning) StatusConnected else StatusPairing
                            )
                        }
                        ConnectionStatusBadge(state = snapshot.connectionState)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ZAID ASHIQ X ULTRA V. CODER",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your Personal WhatsApp Automation Suite • Standalone Local APK",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Three Primary Actions required by Master Spec Section 4:
                    // 1. CONNECT WHATSAPP  2. START ENGINE  3. SETTINGS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigate(UltraModule.PAIRING) },
                            modifier = Modifier
                                .weight(1.15f)
                                .height(48.dp)
                                .testTag("hero_connect_whatsapp_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = ObsidianNavy
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CONNECT WHATSAPP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!snapshot.engineRunning) {
                            Button(
                                onClick = { onStartEngine(context) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("hero_start_engine_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StatusConnected,
                                    contentColor = ObsidianNavy
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "START ENGINE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(
                                onClick = { onStopEngine(context) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("hero_stop_engine_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StatusError,
                                    contentColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "STOP ENGINE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { onNavigate(UltraModule.SETTINGS) },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("hero_settings_button"),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderCyan),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SETTINGS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Live Telemetry Grid (6 key indicators from Spec Section 8)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryStatTile(
                        label = "SESSION STATUS",
                        value = if (snapshot.savedSession != null) "AES-256 LOCKED" else "UNPAIRED",
                        icon = Icons.Default.Security,
                        accentColor = if (snapshot.savedSession != null) StatusConnected else StatusPairing,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryStatTile(
                        label = "MESSAGES PROCESSED",
                        value = settings.totalMessagesProcessed.toString(),
                        icon = Icons.AutoMirrored.Filled.Message,
                        accentColor = ElectricCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryStatTile(
                        label = "COMMANDS EXECUTED",
                        value = settings.totalCommandsExecuted.toString(),
                        icon = Icons.Default.Code,
                        accentColor = NeonViolet,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryStatTile(
                        label = "SCHEDULED TASKS",
                        value = "$activeScheduledTasksCount ACTIVE",
                        icon = Icons.Default.Schedule,
                        accentColor = StatusInfo,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 12 Functional Modules Grid (2 columns per row)
        item {
            Text(
                text = "STANDALONE AUTOMATION MODULES",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricCyan,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        val chunkedModules = modules.chunked(2)
        items(chunkedModules.size) { rowIndex ->
            val pair = chunkedModules[rowIndex]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { item ->
                    CyberGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag(item.testTag),
                        accentBorderColor = item.accent,
                        onClick = { onNavigate(item.module) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(item.accent.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = item.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = item.badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = item.accent
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            minLines = 2
                        )
                    }
                }
            }
        }
    }
}
