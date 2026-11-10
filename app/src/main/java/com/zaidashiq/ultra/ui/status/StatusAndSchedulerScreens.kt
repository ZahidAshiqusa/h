package com.zaidashiq.ultra.ui.status

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.zaidashiq.ultra.database.ScheduledTaskEntity
import com.zaidashiq.ultra.database.StatusPostEntity
import com.zaidashiq.ultra.engine.WhatsAppConnectionState
import com.zaidashiq.ultra.ui.components.CyberGlassCard
import com.zaidashiq.ultra.ui.components.ModuleTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusPublisherScreen(
    connectionState: WhatsAppConnectionState,
    statusPosts: List<StatusPostEntity>,
    onBack: () -> Unit,
    onPublishStatus: (String, String?, String) -> Unit
) {
    var selectedMediaType by rememberSaveable { mutableStateOf("TEXT") }
    var selectedMediaUri by rememberSaveable { mutableStateOf<String?>(null) }
    var captionText by rememberSaveable {
        mutableStateOf("⚡ Automated via ZAID ASHIQ X ULTRA V. CODER — Standalone Android Suite")
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaType = "IMAGE"
            selectedMediaUri = uri.toString()
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaType = "VIDEO"
            selectedMediaUri = uri.toString()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = "Status Publisher",
            subtitle = "Zero-Permission Photo Picker & Status Broadcast",
            connectionState = connectionState,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                CyberGlassCard(accentBorderColor = StatusPairing) {
                    Text(
                        text = "COMPOSE WHATSAPP STATUS UPDATE",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusPairing,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PICK IMAGE")
                        }
                        OutlinedButton(
                            onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PICK VIDEO")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("TEXT", "IMAGE", "VIDEO").forEach { type ->
                            FilterChip(
                                selected = selectedMediaType == type,
                                onClick = { selectedMediaType = type },
                                label = { Text(type) }
                            )
                        }
                    }

                    if (!selectedMediaUri.isNullOrBlank()) {
                        Text(
                            text = "Selected Media URI: $selectedMediaUri",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        label = { Text("Status Caption / Text Content") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("status_caption_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "LIVE STATUS CARD PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ObsidianNavy)
                            .border(1.dp, NeonViolet, RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "[$selectedMediaType STATUS]",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonViolet
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = captionText.ifBlank { "(Empty Caption)" },
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onPublishStatus(selectedMediaType, selectedMediaUri, captionText)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("publish_status_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianNavy
                        )
                    ) {
                        Icon(Icons.Default.Publish, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PUBLISH STATUS UPDATE")
                    }
                }
            }

            items(statusPosts, key = { it.id }) { post ->
                CyberGlassCard(accentBorderColor = StatusConnected) {
                    Text(
                        text = "${post.mediaType} • ${post.publishState}",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusConnected
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = post.captionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = post.resultSummary,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun SchedulerScreen(
    connectionState: WhatsAppConnectionState,
    tasks: List<ScheduledTaskEntity>,
    onBack: () -> Unit,
    onScheduleTask: (Context, String, String, String, Int) -> Unit,
    onExecuteImmediately: (ScheduledTaskEntity) -> Unit,
    onCancelTask: (Context, Long) -> Unit,
    onDeleteTask: (Context, Long) -> Unit
) {
    val context = LocalContext.current
    var recipientPhone by rememberSaveable { mutableStateOf("+923001234567") }
    var recipientName by rememberSaveable { mutableStateOf("Scheduled Recipient") }
    var messageContent by rememberSaveable { mutableStateOf("Reminder: Scheduled update from ZAID ASHIQ X ULTRA V. CODER") }
    var delayMinutesText by rememberSaveable { mutableStateOf("15") }
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = "Local Task Scheduler",
            subtitle = "SQLite Persistent Queue & Android JobScheduler",
            connectionState = connectionState,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                CyberGlassCard(accentBorderColor = StatusInfo) {
                    Text(
                        text = "SCHEDULE OUTGOING WHATSAPP MESSAGE",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusInfo,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Note: Scheduled tasks are persisted locally in SQLite (`zaid_ultra.db`). Exact execution times may be deferred by Android Doze/battery management, and tasks cannot execute while the phone is powered off or the app is force-stopped.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = recipientPhone,
                            onValueChange = { recipientPhone = it },
                            label = { Text("Recipient Phone") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("schedule_phone_input")
                        )
                        OutlinedTextField(
                            value = delayMinutesText,
                            onValueChange = { delayMinutesText = it },
                            label = { Text("Delay (Minutes)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(0.75f)
                                .testTag("schedule_delay_input")
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text("Recipient Label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = messageContent,
                        onValueChange = { messageContent = it },
                        label = { Text("Scheduled Message Content") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("schedule_message_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val mins = delayMinutesText.toIntOrNull() ?: 15
                            onScheduleTask(context, recipientPhone, recipientName, messageContent, mins)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("create_scheduled_task_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianNavy
                        )
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SCHEDULE TASK IN SQLITE")
                    }
                }
            }

            item {
                Text(
                    text = "SCHEDULED TASKS QUEUE (${tasks.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricCyan
                )
            }

            items(tasks, key = { it.id }) { task ->
                val accent = when (task.status) {
                    "EXECUTED" -> StatusConnected
                    "CANCELLED" -> StatusError
                    else -> StatusPairing
                }
                CyberGlassCard(accentBorderColor = accent) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Task #${task.id} • ${task.recipientName} (${task.recipientPhone})",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Target Time: ${fmt.format(Date(task.scheduledTimeMillis))} • Status: ${task.status}",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent
                            )
                        }
                        if (task.status == "PENDING") {
                            IconButton(
                                onClick = { onExecuteImmediately(task) },
                                modifier = Modifier.testTag("run_task_now_${task.id}")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Execute Now", tint = StatusConnected)
                            }
                            IconButton(
                                onClick = { onCancelTask(context, task.id) },
                                modifier = Modifier.testTag("cancel_task_${task.id}")
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = "Cancel Task", tint = StatusPairing)
                            }
                        }
                        IconButton(onClick = { onDeleteTask(context, task.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = StatusError)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.messageContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = task.executionResult,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
