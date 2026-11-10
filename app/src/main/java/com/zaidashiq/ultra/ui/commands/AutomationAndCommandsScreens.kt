package com.zaidashiq.ultra.ui.commands

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.zaidashiq.ultra.database.AutomationRuleEntity
import com.zaidashiq.ultra.database.CommandEntity
import com.zaidashiq.ultra.database.PluginEntity
import com.zaidashiq.ultra.engine.WhatsAppConnectionState
import com.zaidashiq.ultra.repositories.LocalAppSettings
import com.zaidashiq.ultra.ui.components.CyberGlassCard
import com.zaidashiq.ultra.ui.components.ModuleTopBar

@Composable
fun AutomationScreen(
    connectionState: WhatsAppConnectionState,
    settings: LocalAppSettings,
    rules: List<AutomationRuleEntity>,
    onBack: () -> Unit,
    onUpdateSettings: ((LocalAppSettings) -> LocalAppSettings) -> Unit,
    onAddRule: (String, String, String, String, String, String, String, Int, Int) -> Unit,
    onToggleRule: (Long, Boolean) -> Unit,
    onDeleteRule: (Long) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var triggerKeyword by rememberSaveable { mutableStateOf("") }
    var matchMode by rememberSaveable { mutableStateOf("CONTAINS") }
    var responseTemplate by rememberSaveable { mutableStateOf("") }
    var languageTag by rememberSaveable { mutableStateOf("EN") }
    var targetScope by rememberSaveable { mutableStateOf("PRIVATE_ONLY") }
    var contactFilter by rememberSaveable { mutableStateOf("") }
    var delayMsText by rememberSaveable { mutableStateOf(settings.replyDelayMs.toString()) }
    var rateLimitText by rememberSaveable { mutableStateOf(settings.maxAutoRepliesPerMinute.toString()) }

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = "WhatsApp Automation",
            subtitle = "Auto-Replies, Rate Limits & Custom Keyword Rules",
            connectionState = connectionState,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Master Controls & Anti-Spam Rate Limits
            item {
                CyberGlassCard(accentBorderColor = ElectricCyan) {
                    Text(
                        text = "GLOBAL AUTO-RESPONDER & FLOOD PROTECTION",
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
                            Text("Master Automatic Reply", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Enable keyword, welcome, and away responses", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Switch(
                            checked = settings.autoReplyEnabled,
                            onCheckedChange = { enabled -> onUpdateSettings { it.copy(autoReplyEnabled = enabled) } },
                            modifier = Modifier.testTag("switch_master_autoreply")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("First-Contact Welcome Message", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Send welcome greeting once per new sender", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Switch(
                            checked = settings.welcomeReplyEnabled,
                            onCheckedChange = { enabled -> onUpdateSettings { it.copy(welcomeReplyEnabled = enabled) } }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Away Mode Responder", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Reply automatically when away from device", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Switch(
                            checked = settings.awayReplyEnabled,
                            onCheckedChange = { enabled -> onUpdateSettings { it.copy(awayReplyEnabled = enabled) } }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Business Hours Reply (09:00–18:00)", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Auto-respond outside configured working hours", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Switch(
                            checked = settings.businessHoursEnabled,
                            onCheckedChange = { enabled -> onUpdateSettings { it.copy(businessHoursEnabled = enabled) } }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Allow Group Auto-Replies", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Opt-in before replying inside group chats", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Switch(
                            checked = settings.groupRepliesEnabled,
                            onCheckedChange = { enabled -> onUpdateSettings { it.copy(groupRepliesEnabled = enabled) } }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = delayMsText,
                            onValueChange = {
                                delayMsText = it
                                it.toIntOrNull()?.let { ms -> onUpdateSettings { s -> s.copy(replyDelayMs = ms) } }
                            },
                            label = { Text("Reply Delay (ms)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = rateLimitText,
                            onValueChange = {
                                rateLimitText = it
                                it.toIntOrNull()?.let { cap -> onUpdateSettings { s -> s.copy(maxAutoRepliesPerMinute = cap) } }
                            },
                            label = { Text("Max Replies / Min") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Add Custom Keyword Rule Card
            item {
                CyberGlassCard(accentBorderColor = NeonViolet) {
                    Text(
                        text = "CREATE KEYWORD / REGEX AUTOMATION RULE",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonViolet,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Rule Title (e.g. Support Greeting)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rule_title_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = triggerKeyword,
                        onValueChange = { triggerKeyword = it },
                        label = { Text("Trigger Keyword or Regex Pattern") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rule_keyword_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("CONTAINS", "EXACT", "REGEX").forEach { mode ->
                            FilterChip(
                                selected = matchMode == mode,
                                onClick = { matchMode = mode },
                                label = { Text(mode) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("EN", "UR", "ROMAN_UR").forEach { lang ->
                            FilterChip(
                                selected = languageTag == lang,
                                onClick = { languageTag = lang },
                                label = { Text(lang) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("PRIVATE_ONLY", "GROUPS_ONLY", "ALL").forEach { scope ->
                            FilterChip(
                                selected = targetScope == scope,
                                onClick = { targetScope = scope },
                                label = { Text(scope) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contactFilter,
                        onValueChange = { contactFilter = it },
                        label = { Text("Optional Contact Phone Filter (Leave blank for all)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = responseTemplate,
                        onValueChange = { responseTemplate = it },
                        label = { Text("Automated Reply Message") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rule_response_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onAddRule(
                                title,
                                triggerKeyword,
                                matchMode,
                                responseTemplate,
                                languageTag,
                                targetScope,
                                contactFilter,
                                settings.replyDelayMs,
                                settings.duplicateSuppressionWindowSec
                            )
                            title = ""
                            triggerKeyword = ""
                            responseTemplate = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_automation_rule_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianNavy
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ADD AUTOMATION RULE")
                    }
                }
            }

            item {
                Text(
                    text = "ACTIVE AUTOMATION RULES (${rules.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricCyan
                )
            }

            items(rules, key = { it.id }) { rule ->
                CyberGlassCard(accentBorderColor = if (rule.isEnabled) StatusConnected else TextSecondary) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rule.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Trigger [${rule.matchMode}]: \"${rule.triggerKeyword}\" • Lang: ${rule.languageTag} • Scope: ${rule.targetScope} • Hits: ${rule.hitCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan
                            )
                        }
                        Switch(
                            checked = rule.isEnabled,
                            onCheckedChange = { onToggleRule(rule.id, it) }
                        )
                        IconButton(onClick = { onDeleteRule(rule.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Rule", tint = StatusError)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = rule.responseTemplate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun CommandLibraryScreen(
    connectionState: WhatsAppConnectionState,
    settings: LocalAppSettings,
    commands: List<CommandEntity>,
    commandUsageLogs: List<com.zaidashiq.ultra.database.CommandUsageLogEntity> = emptyList(),
    lastTestOutput: String?,
    onBack: () -> Unit,
    onUpdatePrefix: (String) -> Unit,
    onToggleCommand: (String, Boolean) -> Unit,
    onRunCommandTest: (String) -> Unit,
    onClearCommandUsageLogs: () -> Unit = {}
) {
    var prefixInput by rememberSaveable { mutableStateOf(settings.commandPrefix) }
    var sandboxCommandInput by rememberSaveable { mutableStateOf("${settings.commandPrefix}ping") }

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = "Command Library",
            subtitle = "13 Modular Prefix Commands & Live JS Sandbox",
            connectionState = connectionState,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CyberGlassCard(accentBorderColor = ElectricCyan) {
                    Text(
                        text = "LIVE COMMAND SANDBOX & PREFIX CONFIGURATION",
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = prefixInput,
                            onValueChange = { prefixInput = it },
                            label = { Text("Command Prefix") },
                            singleLine = true,
                            modifier = Modifier.width(140.dp)
                        )
                        OutlinedButton(
                            onClick = { onUpdatePrefix(prefixInput.ifBlank { "." }) },
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("SAVE PREFIX")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = sandboxCommandInput,
                            onValueChange = { sandboxCommandInput = it },
                            label = { Text("Test Command (e.g. .help, .ping, .status)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sandbox_command_input")
                        )
                        Button(
                            onClick = { onRunCommandTest(sandboxCommandInput) },
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("execute_sandbox_command_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = ObsidianNavy
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RUN")
                        }
                    }
                    if (!lastTestOutput.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianNavy)
                                .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "SANDBOX OUTPUT:",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lastTestOutput,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.testTag("sandbox_command_output")
                            )
                        }
                    }
                }
            }

            if (commandUsageLogs.isNotEmpty()) {
                item {
                    CyberGlassCard(accentBorderColor = StatusConnected) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "COMMAND USAGE LOGS (${commandUsageLogs.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = StatusConnected,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(onClick = onClearCommandUsageLogs) {
                                Text("CLEAR LOGS")
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        commandUsageLogs.take(5).forEach { entry ->
                            Text(
                                text = "• ${entry.prefixUsed}${entry.commandName} (${entry.executionStatus} • ${entry.executionLatencyMs}ms) by ${entry.senderName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }

            items(commands, key = { it.name }) { cmd ->
                CyberGlassCard(accentBorderColor = if (cmd.isEnabled) NeonViolet else TextSecondary) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${settings.commandPrefix}${cmd.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "[${cmd.category} • ${cmd.permissionLevel}]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonViolet
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cmd.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Usage: ${cmd.usage} • Executed: ${cmd.executionCount}x",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = cmd.isEnabled,
                            onCheckedChange = { onToggleCommand(cmd.name, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PluginManagerScreen(
    connectionState: WhatsAppConnectionState,
    plugins: List<PluginEntity>,
    onBack: () -> Unit,
    onTogglePlugin: (String, Boolean) -> Unit,
    onImportPlugin: (String, String) -> Unit,
    onDeletePlugin: (String) -> Unit
) {
    var pluginFileName by rememberSaveable { mutableStateOf("custom_auto_tagger.js") }
    var pluginJsCode by rememberSaveable {
        mutableStateOf(
            """
            /**
             * @plugin
             * @id utilities.custom_auto_tagger
             * @name Custom Order Tag Formatter
             * @version 1.0.0
             * @author Local User
             * @category utilities
             * @permissions READ_INCOMING_MESSAGE,SEND_REPLY
             * @description Tags incoming order messages locally inside the APK sandbox.
             */
            function executePlugin(input) {
              return { status: "OK", tagged: true };
            }
            """.trimIndent()
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = "Plugin Manager",
            subtitle = "Local JS Modules, Validator & Permission Guard",
            connectionState = connectionState,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                CyberGlassCard(accentBorderColor = StatusConnected) {
                    Text(
                        text = "IMPORT & VALIDATE LOCAL JAVASCRIPT PLUGIN",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusConnected,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Plugins are verified against the security permission allowlist and must define `function executePlugin(input)` before loading.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pluginFileName,
                        onValueChange = { pluginFileName = it },
                        label = { Text("Plugin File Name (.js)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pluginJsCode,
                        onValueChange = { pluginJsCode = it },
                        label = { Text("JavaScript Module Source & Metadata Header") },
                        minLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onImportPlugin(pluginFileName, pluginJsCode) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("validate_and_load_plugin_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianNavy
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("VALIDATE & INSTALL LOCAL PLUGIN")
                    }
                }
            }

            items(plugins, key = { it.pluginId }) { plugin ->
                CyberGlassCard(
                    accentBorderColor = when {
                        !plugin.isValidated -> StatusError
                        plugin.isEnabled -> ElectricCyan
                        else -> TextSecondary
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (plugin.isValidated) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (plugin.isValidated) StatusConnected else StatusError,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${plugin.name} (v${plugin.version})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "ID: ${plugin.pluginId} • Category: ${plugin.category} • By ${plugin.author}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan
                            )
                        }
                        Switch(
                            checked = plugin.isEnabled,
                            enabled = plugin.isValidated,
                            onCheckedChange = { onTogglePlugin(plugin.pluginId, it) }
                        )
                        if (plugin.pluginId.startsWith("custom.") || plugin.pluginId.startsWith("utilities.custom")) {
                            IconButton(onClick = { onDeletePlugin(plugin.pluginId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Plugin", tint = StatusError)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = plugin.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Permissions: ${plugin.permissionsCsv}",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusInfo
                    )
                    if (!plugin.validationError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Validation Error: ${plugin.validationError}",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusError
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MultilingualResponderScreen(
    connectionState: WhatsAppConnectionState,
    rules: List<AutomationRuleEntity>,
    onBack: () -> Unit,
    onSimulateMessage: (String, String, String) -> Unit
) {
    var testInput by rememberSaveable { mutableStateOf("Assalam o Alaikum, kya haal hai?") }

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = "Local Multilingual Responder",
            subtitle = "100% Offline English, Urdu & Roman Urdu Engine (Zero Cloud AI)",
            connectionState = connectionState,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                CyberGlassCard(accentBorderColor = NeonViolet) {
                    Text(
                        text = "STRICTLY LOCAL MULTILINGUAL ENGINE (NO EXTERNAL AI)",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonViolet,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Configured per your privacy architecture: No external AI providers, no cloud API keys, and zero external data transmission. All English, Urdu script, and Roman Urdu responses are processed deterministically on-device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = testInput,
                        onValueChange = { testInput = it },
                        label = { Text("Test Phrase (English / اردو / Roman Urdu)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { testInput = "Assalam o Alaikum" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Roman Urdu")
                        }
                        OutlinedButton(
                            onClick = { testInput = "السلام علیکم" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("اردو")
                        }
                        OutlinedButton(
                            onClick = { testInput = "What is the price?" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("English")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onSimulateMessage("+923005556677", "Multilingual Tester", testInput)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("test_multilingual_responder_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianNavy
                        )
                    ) {
                        Text("TEST MULTILINGUAL DISPATCH")
                    }
                }
            }

            items(rules, key = { it.id }) { rule ->
                CyberGlassCard(accentBorderColor = StatusPairing) {
                    Text(
                        text = "${rule.title} [${rule.languageTag}]",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Trigger: \"${rule.triggerKeyword}\" → ${rule.responseTemplate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
