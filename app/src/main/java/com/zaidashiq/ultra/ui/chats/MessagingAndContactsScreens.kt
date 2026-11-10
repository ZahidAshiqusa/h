package com.zaidashiq.ultra.ui.chats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.zaidashiq.ultra.database.MessageTemplateEntity
import com.zaidashiq.ultra.database.SessionChatEntity
import com.zaidashiq.ultra.database.SessionContactEntity
import com.zaidashiq.ultra.engine.WhatsAppConnectionState
import com.zaidashiq.ultra.ui.components.CyberGlassCard
import com.zaidashiq.ultra.ui.components.ModuleTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageCenterScreen(
    connectionState: WhatsAppConnectionState,
    templates: List<MessageTemplateEntity>,
    recentChats: List<SessionChatEntity>,
    onBack: () -> Unit,
    onSendManualMessage: (String, String, String) -> Unit,
    onSimulateIncomingMessage: (String, String, String, Boolean) -> Unit,
    onSaveTemplate: (String, String, String, String) -> Unit,
    onDeleteTemplate: (Long) -> Unit
) {
    var recipientPhone by rememberSaveable { mutableStateOf("+923001234567") }
    var recipientName by rememberSaveable { mutableStateOf("Zaid Client") }
    var manualMessageBody by rememberSaveable { mutableStateOf("") }

    var simSenderPhone by rememberSaveable { mutableStateOf("+923007654321") }
    var simSenderName by rememberSaveable { mutableStateOf("Incoming Tester") }
    var simMessageText by rememberSaveable { mutableStateOf(".menu") }
    var simIsGroup by rememberSaveable { mutableStateOf(false) }

    var newTemplateTitle by rememberSaveable { mutableStateOf("") }
    var newTemplateBody by rememberSaveable { mutableStateOf("") }
    var newTemplateLang by rememberSaveable { mutableStateOf("EN") }

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = "Message Center",
            subtitle = "Manual Dispatch, Incoming Simulator & Templates",
            connectionState = connectionState,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Manual Outgoing Message Composer
            item {
                CyberGlassCard(accentBorderColor = ElectricCyan) {
                    Text(
                        text = "SEND MANUAL WHATSAPP MESSAGE",
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = recipientPhone,
                            onValueChange = { recipientPhone = it },
                            label = { Text("Phone (E.164)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_recipient_phone_input")
                        )
                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = { recipientName = it },
                            label = { Text("Contact Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualMessageBody,
                        onValueChange = { manualMessageBody = it },
                        label = { Text("Message Content") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_message_body_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onSendManualMessage(recipientPhone, recipientName, manualMessageBody)
                            manualMessageBody = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("send_manual_message_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianNavy
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SEND MESSAGE NOW")
                    }
                }
            }

            // Live Incoming Message & Command Pipeline Tester
            item {
                CyberGlassCard(accentBorderColor = StatusConnected) {
                    Text(
                        text = "INCOMING WHATSAPP EVENT SIMULATOR",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusConnected,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Test how the local engine processes incoming prefix commands (e.g. `.ping`, `.menu`) or keyword triggers (`salam`, `price`).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = simSenderPhone,
                            onValueChange = { simSenderPhone = it },
                            label = { Text("Sender Phone") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = simMessageText,
                            onValueChange = { simMessageText = it },
                            label = { Text("Incoming Text") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_incoming_text_input")
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Simulate as Group Chat Message", color = TextSecondary)
                        Switch(checked = simIsGroup, onCheckedChange = { simIsGroup = it })
                    }
                    Button(
                        onClick = {
                            onSimulateIncomingMessage(simSenderPhone, simSenderName, simMessageText, simIsGroup)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("trigger_incoming_simulation_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusConnected,
                            contentColor = ObsidianNavy
                        )
                    ) {
                        Text("DISPATCH INCOMING EVENT TO ENGINE")
                    }
                }
            }

            // Saved Message Templates
            item {
                CyberGlassCard(accentBorderColor = NeonViolet) {
                    Text(
                        text = "SAVED MESSAGE TEMPLATES (${templates.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonViolet,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    templates.forEach { tpl ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${tpl.title} [${tpl.languageTag}]",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = tpl.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            TextButton(onClick = { manualMessageBody = tpl.body }) {
                                Text("USE", color = ElectricCyan)
                            }
                            IconButton(onClick = { onDeleteTemplate(tpl.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete template", tint = StatusError)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTemplateTitle,
                        onValueChange = { newTemplateTitle = it },
                        label = { Text("New Template Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("EN", "UR", "ROMAN_UR").forEach { lang ->
                            FilterChip(
                                selected = newTemplateLang == lang,
                                onClick = { newTemplateLang = lang },
                                label = { Text(lang) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = newTemplateBody,
                        onValueChange = { newTemplateBody = it },
                        label = { Text("Template Body") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            onSaveTemplate(newTemplateTitle, newTemplateLang, newTemplateBody, "Custom")
                            newTemplateTitle = ""
                            newTemplateBody = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE TEMPLATE")
                    }
                }
            }

            if (recentChats.isNotEmpty()) {
                item {
                    Text(
                        text = "RECENT SESSION MESSAGES (${recentChats.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                }
                items(recentChats.take(10), key = { it.id }) { msg ->
                    CyberGlassCard(
                        accentBorderColor = if (msg.isOutgoing) ElectricCyan else StatusConnected
                    ) {
                        Text(
                            text = "${if (msg.isOutgoing) (if (msg.isAutomatedReply) "🤖 AUTO-REPLY → " else "📤 OUTGOING → ") else "📥 INCOMING ← "}${msg.contactName} (${msg.phoneNumber})",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (msg.isOutgoing) ElectricCyan else StatusConnected
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg.messageBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactsManagerScreen(
    connectionState: WhatsAppConnectionState,
    contacts: List<SessionContactEntity>,
    lastExportPath: String?,
    onBack: () -> Unit,
    onAddContact: (String, String, String) -> Unit,
    onToggleContactAutoReply: (String, Boolean) -> Unit,
    onExportVcf: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var newPhone by rememberSaveable { mutableStateOf("") }
    var newName by rememberSaveable { mutableStateOf("") }
    var newAbout by rememberSaveable { mutableStateOf("WhatsApp Session Contact") }
    var showVcfConfirm by remember { mutableStateOf(false) }

    val filtered = contacts.filter {
        searchQuery.isBlank() ||
            it.displayName.contains(searchQuery, ignoreCase = true) ||
            it.phoneNumber.contains(searchQuery)
    }

    if (showVcfConfirm) {
        AlertDialog(
            onDismissRequest = { showVcfConfirm = false },
            title = { Text("Export Session Contacts (VCF)?") },
            text = {
                Text("Exports ${contacts.size} WhatsApp session contacts in standard vCard 3.0 (.vcf) format to app-private exports directory.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVcfConfirm = false
                        onExportVcf()
                    }
                ) {
                    Text("Export VCF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVcfConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = "Contacts Manager",
            subtitle = "Session Contacts, Opt-In Controls & VCF Export",
            connectionState = connectionState,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CyberGlassCard(accentBorderColor = StatusConnected) {
                    Text(
                        text = "SESSION CONTACTS & VCF EXPORT",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusConnected,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Only processes contacts exposed by the connected WhatsApp session or added explicitly by you. Toggle individual contacts to allow or mute automated replies.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search Session Contacts") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showVcfConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_vcf_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianNavy
                        )
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXPORT CONTACTS TO VCF (${contacts.size})")
                    }
                    if (!lastExportPath.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Last Export: $lastExportPath",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusInfo
                        )
                    }
                }
            }

            item {
                CyberGlassCard(accentBorderColor = NeonViolet) {
                    Text(
                        text = "REGISTER SESSION CONTACT RULE",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonViolet,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newPhone,
                            onValueChange = { newPhone = it },
                            label = { Text("Phone (+92...)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_contact_phone_input")
                        )
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Display Name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_contact_name_input")
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            onAddContact(newPhone, newName, newAbout)
                            newPhone = ""
                            newName = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_contact_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ADD SESSION CONTACT")
                    }
                }
            }

            items(filtered, key = { it.jid }) { contact ->
                CyberGlassCard(accentBorderColor = if (contact.isAllowedForAutoReply) ElectricCyan else StatusPairing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = contact.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${contact.phoneNumber} • ${contact.jid}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan
                            )
                            Text(
                                text = contact.aboutStatus,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (contact.isAllowedForAutoReply) "Auto-Reply ON" else "Muted",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (contact.isAllowedForAutoReply) StatusConnected else StatusPairing
                            )
                            Switch(
                                checked = contact.isAllowedForAutoReply,
                                onCheckedChange = { onToggleContactAutoReply(contact.jid, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatExportScreen(
    connectionState: WhatsAppConnectionState,
    chats: List<SessionChatEntity>,
    lastExportPath: String?,
    onBack: () -> Unit,
    onExportJson: () -> Unit,
    onExportTranscript: () -> Unit
) {
    var confirmFormat by remember { mutableStateOf<String?>(null) }
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }

    if (confirmFormat != null) {
        AlertDialog(
            onDismissRequest = { confirmFormat = null },
            title = { Text("Confirm User-Initiated Chat Export") },
            text = {
                Text("Export ${chats.size} accessible session messages in $confirmFormat format to local app-private storage (`files/whatsapp/exports/`)?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = confirmFormat
                        confirmFormat = null
                        if (target == "JSON") onExportJson() else onExportTranscript()
                    }
                ) {
                    Text("Confirm Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmFormat = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = "Chat History & Export",
            subtitle = "User-Initiated JSON & Text Transcript Exports",
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
                        text = "EXPORT ACCESSIBLE SESSION MESSAGES",
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "All exports require explicit user confirmation and only include messages legitimately processed by your connected WhatsApp session.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { confirmFormat = "JSON" },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_chats_json_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = ObsidianNavy
                            )
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EXPORT JSON")
                        }
                        OutlinedButton(
                            onClick = { confirmFormat = "TXT" },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_chats_txt_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EXPORT TXT")
                        }
                    }
                    if (!lastExportPath.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Saved to: $lastExportPath",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusConnected,
                            modifier = Modifier.testTag("export_file_path_text")
                        )
                    }
                }
            }

            if (chats.isEmpty()) {
                item {
                    CyberGlassCard {
                        Text(
                            text = "No session messages recorded yet. Open Message Center to send a message or simulate an incoming command.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(chats, key = { it.id }) { chat ->
                    CyberGlassCard(
                        accentBorderColor = if (chat.isOutgoing) ElectricCyan else StatusConnected
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${chat.contactName} (${chat.phoneNumber})",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = fmt.format(Date(chat.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = chat.messageBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
