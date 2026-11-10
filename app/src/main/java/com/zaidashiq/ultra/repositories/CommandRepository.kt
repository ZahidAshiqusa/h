package com.zaidashiq.ultra.repositories

import android.content.Context
import com.zaidashiq.ultra.database.ActivityLogEntity
import com.zaidashiq.ultra.database.AutomationRuleEntity
import com.zaidashiq.ultra.database.CommandEntity
import com.zaidashiq.ultra.database.CommandUsageLogEntity
import com.zaidashiq.ultra.database.MessageTemplateEntity
import com.zaidashiq.ultra.database.PluginEntity
import com.zaidashiq.ultra.database.UltraDao
import com.zaidashiq.ultra.security.SecureStorage
import kotlinx.coroutines.flow.Flow
import java.io.File

class CommandRepository(
    private val context: Context,
    private val ultraDao: UltraDao,
    private val secureStorage: SecureStorage
) {
    val commandsFlow: Flow<List<CommandEntity>> = ultraDao.observeCommands()
    val commandUsageLogsFlow: Flow<List<CommandUsageLogEntity>> = ultraDao.observeCommandUsageLogs()
    val pluginsFlow: Flow<List<PluginEntity>> = ultraDao.observePlugins()
    val automationRulesFlow: Flow<List<AutomationRuleEntity>> = ultraDao.observeAutomationRules()
    val activeAutomationRulesFlow: Flow<List<AutomationRuleEntity>> = ultraDao.observeActiveAutomationRules()
    val templatesFlow: Flow<List<MessageTemplateEntity>> = ultraDao.observeMessageTemplates()

    suspend fun recordCommandExecutionAndUsageLog(
        commandName: String,
        prefixUsed: String,
        rawArguments: String,
        senderPhone: String,
        senderName: String,
        isGroupChat: Boolean = false,
        executionLatencyMs: Long = 3L,
        executionStatus: String = "SUCCESS",
        responsePreview: String
    ) {
        ultraDao.recordCommandExecution(commandName)
        ultraDao.insertCommandUsageLog(
            CommandUsageLogEntity(
                commandName = commandName,
                prefixUsed = prefixUsed,
                rawArguments = rawArguments,
                senderPhone = senderPhone,
                senderName = senderName,
                isGroupChat = isGroupChat,
                executionLatencyMs = executionLatencyMs,
                executionStatus = executionStatus,
                responsePreview = responsePreview.take(180)
            )
        )
    }

    suspend fun clearCommandUsageLogs() {
        ultraDao.clearCommandUsageLogs()
    }

    companion object {
        private val ALLOWED_PLUGIN_PERMISSIONS = setOf(
            "READ_METRICS",
            "READ_SESSION_STATE",
            "READ_INCOMING_MESSAGE",
            "SEND_REPLY",
            "FORMAT_STATUS_CAPTION",
            "EXPORT_VCF",
            "FORMAT_E164"
        )
    }

    suspend fun ensureCoreCommandsAndPluginsSeeded() {
        val existingCommands = ultraDao.getAllCommandsSnapshot()
        if (existingCommands.isEmpty()) {
            val initialCommands = listOf(
                CommandEntity("help", "Core", ".help [command]", "Display all enabled commands and usage syntax", "PUBLIC", "assets/node/scripts/index.js"),
                CommandEntity("menu", "Core", ".menu", "Show categorized ZAID ASHIQ X ULTRA V. CODER command menu", "PUBLIC", "assets/node/scripts/index.js"),
                CommandEntity("ping", "Diagnostics", ".ping", "Check local engine health, latency, and uptime", "PUBLIC", "assets/node/scripts/index.js"),
                CommandEntity("info", "Diagnostics", ".info", "Show runtime architecture, Keystore status, and active plugins", "PUBLIC", "assets/node/scripts/index.js"),
                CommandEntity("owner", "Core", ".owner", "Display device owner profile and automation suite signature", "PUBLIC", "assets/node/scripts/index.js"),
                CommandEntity("time", "Utilities", ".time", "Return current local device time and timezone", "PUBLIC", "assets/node/scripts/index.js"),
                CommandEntity("date", "Utilities", ".date", "Return current local calendar date", "PUBLIC", "assets/node/scripts/index.js"),
                CommandEntity("status", "Diagnostics", ".status", "Report WhatsApp session state, rate limiter load, and pending tasks", "OWNER", "assets/node/scripts/index.js"),
                CommandEntity("schedule", "Scheduler", ".schedule <minutes> | <message>", "Schedule a delayed message on the local SQLite task queue", "OWNER", "assets/node/scripts/index.js"),
                CommandEntity("cancel", "Scheduler", ".cancel <taskId>", "Cancel a pending scheduled task by ID", "OWNER", "assets/node/scripts/index.js"),
                CommandEntity("autoreply", "Automation", ".autoreply", "Inspect current automatic reply status and active rate limits", "OWNER", "assets/node/scripts/index.js"),
                CommandEntity("autoreply on", "Automation", ".autoreply on", "Enable automatic keyword, welcome, and away replies", "OWNER", "assets/node/scripts/index.js"),
                CommandEntity("autoreply off", "Automation", ".autoreply off", "Disable automatic replies while keeping manual commands active", "OWNER", "assets/node/scripts/index.js")
            )
            ultraDao.insertCommands(initialCommands)
        }

        val existingPlugins = ultraDao.getAllPluginsSnapshot()
        if (existingPlugins.isEmpty()) {
            val starterPlugins = listOf(
                PluginEntity(
                    pluginId = "core.system_diagnostics",
                    name = "System Diagnostics & Telemetry",
                    version = "1.0.0",
                    author = "Zaid Ashiq Ultra",
                    category = "core",
                    description = "Provides detailed local engine health checks, memory inspection, and rate-limiter telemetry.",
                    filePath = "plugins/core/system_diagnostics.js",
                    permissionsCsv = "READ_METRICS,READ_SESSION_STATE",
                    isValidated = true
                ),
                PluginEntity(
                    pluginId = "messaging.multilingual_responder",
                    name = "Multilingual Smart Responder (EN / UR / Roman Urdu)",
                    version = "1.2.0",
                    author = "Zaid Ashiq Ultra",
                    category = "messaging",
                    description = "Deterministic local responder supporting English, Urdu script, and Roman Urdu greetings without cloud AI dependencies.",
                    filePath = "plugins/messaging/multilingual_responder.js",
                    permissionsCsv = "READ_INCOMING_MESSAGE,SEND_REPLY",
                    isValidated = true
                ),
                PluginEntity(
                    pluginId = "media.status_formatter",
                    name = "Status Caption & Timestamp Formatter",
                    version = "1.0.4",
                    author = "Zaid Ashiq Ultra",
                    category = "media",
                    description = "Formats outgoing WhatsApp status updates with clean borders, timestamps, and signatures.",
                    filePath = "plugins/media/status_formatter.js",
                    permissionsCsv = "FORMAT_STATUS_CAPTION",
                    isValidated = true
                ),
                PluginEntity(
                    pluginId = "utilities.contact_vcard_tools",
                    name = "VCF Contact Sanitizer & Formatter",
                    version = "1.1.0",
                    author = "Zaid Ashiq Ultra",
                    category = "utilities",
                    description = "Normalizes session phone numbers to E.164 and builds RFC-6350 vCard 3.0 export blocks.",
                    filePath = "plugins/utilities/contact_vcard_tools.js",
                    permissionsCsv = "EXPORT_VCF,FORMAT_E164",
                    isValidated = true
                )
            )
            ultraDao.insertPlugins(starterPlugins)
        }

        val existingRules = ultraDao.getEnabledAutomationRules()
        if (existingRules.isEmpty()) {
            ultraDao.upsertAutomationRule(
                AutomationRuleEntity(
                    title = "Roman Urdu Salam Auto-Responder",
                    triggerKeyword = "salam",
                    matchMode = "CONTAINS",
                    responseTemplate = "Walaikum Assalam! ZAID ASHIQ X ULTRA V. CODER local engine active hai. Commands dekhne ke liye .menu type karein.",
                    languageTag = "ROMAN_UR",
                    targetScope = "PRIVATE_ONLY",
                    delayMs = 1000,
                    cooldownSec = 45
                )
            )
            ultraDao.upsertAutomationRule(
                AutomationRuleEntity(
                    title = "Urdu Script Greeting Responder",
                    triggerKeyword = "السلام علیکم",
                    matchMode = "CONTAINS",
                    responseTemplate = "وعلیکم السلام! زید عاشق الٹرا کوڈر لوکل آٹومیشن سسٹم میں خوش آمدید۔ مدد کے لیے .help لکھیں۔",
                    languageTag = "UR",
                    targetScope = "PRIVATE_ONLY",
                    delayMs = 1000,
                    cooldownSec = 45
                )
            )
            ultraDao.upsertAutomationRule(
                AutomationRuleEntity(
                    title = "English Pricing & Business Inquiry",
                    triggerKeyword = "price",
                    matchMode = "CONTAINS",
                    responseTemplate = "Thank you for your inquiry! Type .menu for automated options or wait a moment for a direct response.",
                    languageTag = "EN",
                    targetScope = "ALL",
                    delayMs = 1200,
                    cooldownSec = 60
                )
            )
        }

        val existingTemplates = ultraDao.getMessageTemplatesSnapshot()
        if (existingTemplates.isEmpty()) {
            ultraDao.upsertMessageTemplate(
                MessageTemplateEntity(
                    title = "English Official Greeting",
                    languageTag = "EN",
                    body = "Hello! Reaching out from ZAID ASHIQ X ULTRA V. CODER local automation console. Let me know when you are available.",
                    category = "Business"
                )
            )
            ultraDao.upsertMessageTemplate(
                MessageTemplateEntity(
                    title = "Roman Urdu Quick Follow-Up",
                    languageTag = "ROMAN_UR",
                    body = "Assalam-o-Alaikum! Aap ka message receive ho gaya hai. Main thori dair mein tafseel se reply karta hoon.",
                    category = "Quick Reply"
                )
            )
            ultraDao.upsertMessageTemplate(
                MessageTemplateEntity(
                    title = "Urdu Formal Confirmation",
                    languageTag = "UR",
                    body = "السلام علیکم! آپ کی درخواست موصول ہو گئی ہے اور لوکل شیڈولر میں محفوظ کر لی گئی ہے۔ شکریہ۔",
                    category = "Confirmation"
                )
            )
        }
    }

    suspend fun validateAndRegisterLocalPlugin(
        fileName: String,
        jsSourceCode: String
    ): PluginEntity {
        fun extractTag(tag: String, defaultVal: String): String {
            val regex = Regex("@$tag\\s+([^\\r\\n*]+)")
            return regex.find(jsSourceCode)?.groupValues?.get(1)?.trim() ?: defaultVal
        }

        val pluginId = extractTag("id", "custom.${fileName.removeSuffix(".js").lowercase()}")
        val name = extractTag("name", fileName.removeSuffix(".js"))
        val version = extractTag("version", "1.0.0")
        val author = extractTag("author", "Local Developer")
        val category = extractTag("category", "utilities")
        val description = extractTag("description", "User-imported local JavaScript plugin module.")
        val permissionsCsv = extractTag("permissions", "READ_INCOMING_MESSAGE")

        val requestedPerms = permissionsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val disallowed = requestedPerms.filter { it !in ALLOWED_PLUGIN_PERMISSIONS }
        val hasFunctionEntry = jsSourceCode.contains("function executePlugin") || jsSourceCode.contains("module.exports")

        val isValidated = disallowed.isEmpty() && hasFunctionEntry
        val validationError = when {
            !hasFunctionEntry -> "Missing required `function executePlugin(input)` entrypoint."
            disallowed.isNotEmpty() -> "Blocked unauthorized permissions: ${disallowed.joinToString(", ")}"
            else -> null
        }

        val outFile = File(secureStorage.pluginsDir, fileName.let { if (it.endsWith(".js")) it else "$it.js" })
        outFile.writeText(jsSourceCode, Charsets.UTF_8)

        val entity = PluginEntity(
            pluginId = pluginId,
            name = name,
            version = version,
            author = author,
            category = category,
            description = description,
            filePath = outFile.absolutePath,
            permissionsCsv = permissionsCsv,
            isValidated = isValidated,
            validationError = validationError,
            isEnabled = isValidated
        )
        ultraDao.upsertPlugin(entity)
        ultraDao.insertActivityLog(
            ActivityLogEntity(
                level = if (isValidated) "INFO" else "SECURITY",
                module = "PluginManager",
                details = if (isValidated) {
                    "Validated and loaded plugin '$name' ($pluginId v$version)"
                } else {
                    "Rejected plugin '$name': $validationError"
                }
            )
        )
        return entity
    }
}
