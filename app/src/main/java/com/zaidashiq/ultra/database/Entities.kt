package com.zaidashiq.ultra.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val triggerKeyword: String,
    val matchMode: String, // EXACT, CONTAINS, REGEX, WELCOME, AWAY, BUSINESS_HOURS
    val responseTemplate: String,
    val languageTag: String, // EN, UR, ROMAN_UR
    val targetScope: String, // PRIVATE_ONLY, GROUPS_ONLY, ALL
    val contactFilter: String = "", // Optional specific phone/JID rule
    val delayMs: Int = 1200,
    val cooldownSec: Int = 45,
    val isEnabled: Boolean = true,
    val hitCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "commands")
data class CommandEntity(
    @PrimaryKey val name: String, // e.g., "help", "ping", "autoreply on"
    val category: String,
    val usage: String,
    val description: String,
    val permissionLevel: String, // PUBLIC, OWNER
    val handlerScript: String,
    val isEnabled: Boolean = true,
    val executionCount: Int = 0,
    val lastExecutedAt: Long? = null
)

@Entity(tableName = "plugins")
data class PluginEntity(
    @PrimaryKey val pluginId: String,
    val name: String,
    val version: String,
    val author: String,
    val category: String, // core, messaging, media, utilities
    val description: String,
    val filePath: String,
    val permissionsCsv: String,
    val isValidated: Boolean = true,
    val validationError: String? = null,
    val isEnabled: Boolean = true,
    val installedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scheduled_tasks")
data class ScheduledTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipientPhone: String,
    val recipientName: String,
    val messageContent: String,
    val scheduledTimeMillis: Long,
    val status: String, // PENDING, EXECUTED, CANCELLED, FAILED
    val executionResult: String = "Scheduled in local SQLite queue",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "session_contacts")
data class SessionContactEntity(
    @PrimaryKey val jid: String,
    val displayName: String,
    val phoneNumber: String,
    val aboutStatus: String,
    val isGroup: Boolean = false,
    val isAllowedForAutoReply: Boolean = true,
    val syncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "session_chats")
data class SessionChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatJid: String,
    val contactName: String,
    val phoneNumber: String,
    val messageBody: String,
    val isOutgoing: Boolean,
    val isAutomatedReply: Boolean = false,
    val mediaType: String = "TEXT", // TEXT, IMAGE, VIDEO, DOCUMENT
    val mediaPath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "status_posts")
data class StatusPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaType: String, // TEXT, IMAGE, VIDEO
    val mediaUri: String?,
    val captionText: String,
    val publishState: String, // PUBLISHED, QUEUED, FAILED
    val resultSummary: String,
    val publishedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: String, // INFO, WARN, ERROR, SECURITY, ENGINE
    val module: String,
    val details: String
)

@Entity(tableName = "message_templates")
data class MessageTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val languageTag: String, // EN, UR, ROMAN_UR
    val body: String,
    val category: String
)

@Entity(tableName = "whatsapp_session_metadata")
data class WhatsAppSessionMetadataEntity(
    @PrimaryKey val sessionId: String = "primary_session",
    val phoneNumber: String,
    val deviceJid: String,
    val pairingMethod: String, // QR_CODE, PAIRING_CODE
    val keystoreFingerprint: String,
    val connectionState: String,
    val isKeystoreEncrypted: Boolean = true,
    val linkedAtMillis: Long = System.currentTimeMillis(),
    val lastConnectedAtMillis: Long = System.currentTimeMillis(),
    val reconnectCount: Int = 0
)

@Entity(tableName = "command_usage_logs")
data class CommandUsageLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commandName: String,
    val prefixUsed: String,
    val rawArguments: String,
    val senderPhone: String,
    val senderName: String,
    val isGroupChat: Boolean = false,
    val executionLatencyMs: Long = 3L,
    val executionStatus: String = "SUCCESS", // SUCCESS, DISABLED, ERROR
    val responsePreview: String,
    val executedAtMillis: Long = System.currentTimeMillis()
)

