package com.zaidashiq.ultra.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zaidashiq.ultra.AppContainer
import com.zaidashiq.ultra.database.ActivityLogEntity
import com.zaidashiq.ultra.database.AutomationRuleEntity
import com.zaidashiq.ultra.database.CommandEntity
import com.zaidashiq.ultra.database.CommandUsageLogEntity
import com.zaidashiq.ultra.database.MessageTemplateEntity
import com.zaidashiq.ultra.database.PluginEntity
import com.zaidashiq.ultra.database.ScheduledTaskEntity
import com.zaidashiq.ultra.database.SessionChatEntity
import com.zaidashiq.ultra.database.SessionContactEntity
import com.zaidashiq.ultra.database.StatusPostEntity
import com.zaidashiq.ultra.database.WhatsAppSessionMetadataEntity
import com.zaidashiq.ultra.engine.EngineRuntimeSnapshot
import com.zaidashiq.ultra.repositories.LocalAppSettings
import com.zaidashiq.ultra.services.WhatsAppForegroundService
import com.zaidashiq.ultra.workers.ScheduledTaskWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UltraModule(val title: String, val subtitle: String) {
    DASHBOARD("ZAID ASHIQ X ULTRA V. CODER", "Your Personal WhatsApp Automation Suite"),
    PAIRING("Device Pairing & Session", "QR Code, 8-Char Pairing Code & Keystore Session"),
    AUTOMATION("WhatsApp Automation", "Auto-Replies, Multilingual Rules & Rate Limits"),
    MESSAGE_CENTER("Message Center", "Manual Dispatch, Live Simulator & Templates"),
    CONTACTS("Contacts Manager", "Session Contacts, Opt-In Controls & VCF Export"),
    CHAT_EXPORT("Chat History & Export", "Transcripts, JSON Export & Session Messages"),
    STATUS_PUBLISHER("Status Publisher", "Image, Video & Formatted Text Status Updates"),
    MULTILINGUAL_RESPONDER("Local Smart Responder", "Offline English, Urdu & Roman Urdu Rule Engine"),
    SCHEDULER("Task Scheduler", "SQLite Persistent Queue & Delayed Dispatch"),
    COMMAND_LIBRARY("Command Library", "Prefix Command Registry & Live Sandbox Tester"),
    PLUGIN_MANAGER("Plugin Manager", "Local JS Modules, Validator & Permission Guard"),
    ACTIVITY_LOGS("Activity & Security Logs", "Structured Engine, Command & Keystore Telemetry"),
    SETTINGS("Suite Settings", "General, WhatsApp, Automation & Encrypted Storage")
}

class UltraViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    private val _currentModule = MutableStateFlow(UltraModule.DASHBOARD)
    val currentModule: StateFlow<UltraModule> = _currentModule.asStateFlow()

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    private val _lastCommandTestOutput = MutableStateFlow<String?>(null)
    val lastCommandTestOutput: StateFlow<String?> = _lastCommandTestOutput.asStateFlow()

    private val _lastExportedFilePath = MutableStateFlow<String?>(null)
    val lastExportedFilePath: StateFlow<String?> = _lastExportedFilePath.asStateFlow()

    private val _storageBreakdown = MutableStateFlow(container.secureStorage.getStorageBreakdownBytes())
    val storageBreakdown: StateFlow<Map<String, Long>> = _storageBreakdown.asStateFlow()

    val engineSnapshot: StateFlow<EngineRuntimeSnapshot> = container.engineManager.snapshot
    val settings: StateFlow<LocalAppSettings> = container.settingsRepository.settingsFlow

    val sessionMetadata: StateFlow<WhatsAppSessionMetadataEntity?> =
        container.sessionRepository.sessionMetadata.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    val automationRules: StateFlow<List<AutomationRuleEntity>> =
        container.automationRuleRepository.allMessageRules.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val activeAutomationRules: StateFlow<List<AutomationRuleEntity>> =
        container.automationRuleRepository.activeMessageRules.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val commands: StateFlow<List<CommandEntity>> =
        container.commandRepository.commandsFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val commandUsageLogs: StateFlow<List<CommandUsageLogEntity>> =
        container.commandRepository.commandUsageLogsFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val plugins: StateFlow<List<PluginEntity>> =
        container.commandRepository.pluginsFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val scheduledTasks: StateFlow<List<ScheduledTaskEntity>> =
        container.ultraDao.observeScheduledTasks().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val sessionContacts: StateFlow<List<SessionContactEntity>> =
        container.sessionRepository.sessionContacts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val sessionChats: StateFlow<List<SessionChatEntity>> =
        container.sessionRepository.sessionChats.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val statusPosts: StateFlow<List<StatusPostEntity>> =
        container.ultraDao.observeStatusPosts().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val messageTemplates: StateFlow<List<MessageTemplateEntity>> =
        container.commandRepository.templatesFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val activityLogs: StateFlow<List<ActivityLogEntity>> =
        container.ultraDao.observeActivityLogs().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun dismissSplash() {
        _showSplash.value = false
    }

    fun navigateTo(module: UltraModule) {
        _currentModule.value = module
        if (module == UltraModule.SETTINGS) {
            refreshStorageBreakdown()
        }
    }

    fun navigateBackToDashboard() {
        _currentModule.value = UltraModule.DASHBOARD
    }

    fun showBanner(message: String) {
        _bannerMessage.value = message
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    fun refreshStorageBreakdown() {
        _storageBreakdown.value = container.secureStorage.getStorageBreakdownBytes()
    }

    // Engine & Foreground Service Controls
    fun startForegroundEngine(context: Context) {
        if (!container.sessionManager.hasSavedSession()) {
            showBanner("Pair your WhatsApp session first before starting the engine.")
            _currentModule.value = UltraModule.PAIRING
            return
        }
        WhatsAppForegroundService.startService(context)
        container.engineManager.startEngine()
        showBanner("Foreground WhatsApp Automation Engine started.")
    }

    fun stopForegroundEngine(context: Context) {
        WhatsAppForegroundService.stopService(context)
        container.engineManager.stopEngine()
        showBanner("WhatsApp Automation Engine stopped.")
    }

    fun triggerReconnect() {
        container.engineManager.triggerExponentialReconnect("Manual user reconnect request")
        showBanner("Initiated exponential backoff reconnection.")
    }

    fun refreshPoCVerification() {
        container.engineManager.refreshProofOfConceptReport()
        showBanner("Runtime Proof-of-Concept checks verified.")
    }

    // Pairing Actions
    fun startQrPairing() {
        container.engineManager.startQrPairing()
    }

    fun requestPairingCode(phoneInput: String) {
        container.engineManager.requestPairingCode(phoneInput)
    }

    fun completePairingHandshake(phoneInput: String, method: String) {
        container.engineManager.completePairingHandshake(phoneInput, method)
        showBanner("WhatsApp session linked & encrypted with Android Keystore AES-256-GCM.")
    }

    fun disconnectSession(context: Context) {
        WhatsAppForegroundService.stopService(context)
        container.engineManager.disconnectSession()
        showBanner("WhatsApp session disconnected. Encrypted credentials preserved locally.")
    }

    fun deleteSessionPermanently(context: Context) {
        WhatsAppForegroundService.stopService(context)
        container.engineManager.deleteSavedSessionPermanently()
        refreshStorageBreakdown()
        showBanner("Encrypted WhatsApp session permanently deleted from device storage.")
    }

    // Automation Rules & Settings
    fun updateSettings(transform: (LocalAppSettings) -> LocalAppSettings) {
        container.settingsRepository.updateSettings(transform)
    }

    fun addAutomationRule(
        title: String,
        triggerKeyword: String,
        matchMode: String,
        responseTemplate: String,
        languageTag: String,
        targetScope: String,
        contactFilter: String,
        delayMs: Int,
        cooldownSec: Int
    ) {
        if (title.isBlank() || triggerKeyword.isBlank() || responseTemplate.isBlank()) {
            showBanner("Provide a rule title, trigger keyword, and response text.")
            return
        }
        viewModelScope.launch {
            container.automationRuleRepository.upsertMessageRule(
                AutomationRuleEntity(
                    title = title.trim(),
                    triggerKeyword = triggerKeyword.trim(),
                    matchMode = matchMode,
                    responseTemplate = responseTemplate.trim(),
                    languageTag = languageTag,
                    targetScope = targetScope,
                    contactFilter = contactFilter.trim(),
                    delayMs = delayMs.coerceIn(0, 30000),
                    cooldownSec = cooldownSec.coerceIn(5, 3600)
                )
            )
            showBanner("Saved automation rule '${title.trim()}'.")
        }
    }

    fun toggleAutomationRule(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            container.automationRuleRepository.setRuleEnabled(id, enabled)
        }
    }

    fun deleteAutomationRule(id: Long) {
        viewModelScope.launch {
            container.automationRuleRepository.deleteRule(id)
            showBanner("Deleted automation rule.")
        }
    }

    // Message Center & Simulator
    fun sendManualMessage(recipientPhone: String, recipientName: String, messageBody: String) {
        if (recipientPhone.isBlank() || messageBody.isBlank()) {
            showBanner("Enter recipient phone number and message text.")
            return
        }
        container.engineManager.sendManualMessage(recipientPhone, recipientName, messageBody)
        showBanner("Message dispatched via local engine to $recipientPhone.")
    }

    fun simulateIncomingWhatsAppMessage(
        senderPhone: String,
        senderName: String,
        messageText: String,
        isGroup: Boolean = false
    ) {
        if (messageText.isBlank()) {
            showBanner("Enter an incoming message or command (e.g. .ping or salam) to test.")
            return
        }
        container.engineManager.processIncomingMessage(
            senderPhone = senderPhone.ifBlank { "+923001122334" },
            senderName = senderName.ifBlank { "Test Contact" },
            messageText = messageText,
            isGroupMessage = isGroup
        )
        showBanner("Processed incoming message through local command & auto-reply pipeline.")
    }

    fun saveMessageTemplate(title: String, languageTag: String, body: String, category: String) {
        if (title.isBlank() || body.isBlank()) return
        viewModelScope.launch {
            container.ultraDao.upsertMessageTemplate(
                MessageTemplateEntity(
                    title = title.trim(),
                    languageTag = languageTag,
                    body = body.trim(),
                    category = category.ifBlank { "General" }
                )
            )
            showBanner("Template '${title.trim()}' saved.")
        }
    }

    fun deleteMessageTemplate(id: Long) {
        viewModelScope.launch {
            container.ultraDao.deleteMessageTemplate(id)
        }
    }

    // Commands & Plugins
    fun toggleCommandEnabled(name: String, enabled: Boolean) {
        viewModelScope.launch {
            container.ultraDao.setCommandEnabled(name, enabled)
        }
    }

    fun testCommandInSandbox(rawInput: String) {
        viewModelScope.launch {
            val s = settings.value
            val prefix = s.commandPrefix.ifBlank { "." }
            val clean = rawInput.trim().removePrefix(prefix).trim()
            val cmdName = when {
                clean.lowercase().startsWith("autoreply on") -> "autoreply on"
                clean.lowercase().startsWith("autoreply off") -> "autoreply off"
                else -> clean.substringBefore(" ").lowercase()
            }
            val args = clean.removePrefix(cmdName).trim()
            val activePluginsCount = plugins.value.count { it.isEnabled && it.isValidated }
            val pendingCount = scheduledTasks.value.count { it.status == "PENDING" }
            val startNs = System.nanoTime()

            val res = container.engineBridge.executeCommand(
                rawCommandName = cmdName,
                rawArgs = args,
                prefix = prefix,
                connectionState = engineSnapshot.value.connectionState,
                autoReplyEnabled = s.autoReplyEnabled,
                pairedPhone = engineSnapshot.value.savedSession?.phoneNumber ?: "Local Owner",
                activePluginsCount = activePluginsCount,
                messagesProcessed = s.totalMessagesProcessed,
                pendingTasksCount = pendingCount,
                rateLimitPerMin = s.maxAutoRepliesPerMinute
            )
            val latencyMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
            res.toggleAutoReply?.let { enabled ->
                container.settingsRepository.updateSettings { it.copy(autoReplyEnabled = enabled) }
            }
            container.commandRepository.recordCommandExecutionAndUsageLog(
                commandName = res.commandName,
                prefixUsed = prefix,
                rawArguments = args,
                senderPhone = engineSnapshot.value.savedSession?.phoneNumber ?: "local_sandbox",
                senderName = "Local Sandbox",
                isGroupChat = false,
                executionLatencyMs = latencyMs,
                executionStatus = if (res.handled) "SUCCESS" else "UNKNOWN_CMD",
                responsePreview = res.replyText
            )
            container.settingsRepository.incrementCommandsExecuted()
            _lastCommandTestOutput.value = res.replyText
        }
    }

    fun clearCommandUsageLogs() {
        viewModelScope.launch {
            container.commandRepository.clearCommandUsageLogs()
            showBanner("Cleared command usage logs.")
        }
    }

    fun togglePluginEnabled(pluginId: String, enabled: Boolean) {
        viewModelScope.launch {
            container.ultraDao.setPluginEnabled(pluginId, enabled)
        }
    }

    fun importLocalJsPlugin(fileName: String, jsCode: String) {
        viewModelScope.launch {
            val entity = container.commandRepository.validateAndRegisterLocalPlugin(fileName, jsCode)
            if (entity.isValidated) {
                showBanner("Plugin '${entity.name}' validated and enabled.")
            } else {
                showBanner("Plugin validation failed: ${entity.validationError}")
            }
        }
    }

    fun deletePlugin(pluginId: String) {
        viewModelScope.launch {
            container.ultraDao.deletePlugin(pluginId)
            showBanner("Removed plugin $pluginId.")
        }
    }

    // Contacts & Chat Exports
    fun addSessionContact(phone: String, name: String, about: String) {
        if (phone.isBlank()) return
        viewModelScope.launch {
            container.sessionRepository.addOrUpdateSessionContact(phone, name, about)
            showBanner("Added session contact ${name.ifBlank { phone }}.")
        }
    }

    fun toggleContactAutoReply(jid: String, allowed: Boolean) {
        viewModelScope.launch {
            container.sessionRepository.setContactAutoReplyAllowed(jid, allowed)
        }
    }

    fun exportContactsToVcf() {
        viewModelScope.launch {
            val file = container.sessionRepository.exportContactsToVcfFile()
            _lastExportedFilePath.value = file.absolutePath
            refreshStorageBreakdown()
            showBanner("Exported VCF contacts to ${file.name}")
        }
    }

    fun exportChatsToJson() {
        viewModelScope.launch {
            val file = container.sessionRepository.exportChatsToJsonFile()
            _lastExportedFilePath.value = file.absolutePath
            refreshStorageBreakdown()
            showBanner("Exported JSON chat history to ${file.name}")
        }
    }

    fun exportChatsToTranscript() {
        viewModelScope.launch {
            val file = container.sessionRepository.exportChatsToTextTranscript()
            _lastExportedFilePath.value = file.absolutePath
            refreshStorageBreakdown()
            showBanner("Exported text transcript to ${file.name}")
        }
    }

    // Status Publisher
    fun publishStatus(mediaType: String, mediaUri: String?, captionText: String) {
        if (captionText.isBlank() && mediaUri.isNullOrBlank()) {
            showBanner("Enter status text or select media before publishing.")
            return
        }
        container.engineManager.publishStatusUpdate(mediaType, mediaUri, captionText)
        showBanner("Status update submitted to local WhatsApp publisher.")
    }

    // Scheduler
    fun scheduleMessage(
        context: Context,
        recipientPhone: String,
        recipientName: String,
        messageContent: String,
        delayMinutes: Int
    ) {
        if (recipientPhone.isBlank() || messageContent.isBlank()) {
            showBanner("Enter recipient phone and message to schedule.")
            return
        }
        viewModelScope.launch {
            val delayMillis = delayMinutes.coerceAtLeast(1) * 60_000L
            val targetTime = System.currentTimeMillis() + delayMillis
            val id = container.ultraDao.upsertScheduledTask(
                ScheduledTaskEntity(
                    recipientPhone = recipientPhone.trim(),
                    recipientName = recipientName.ifBlank { recipientPhone.trim() },
                    messageContent = messageContent.trim(),
                    scheduledTimeMillis = targetTime,
                    status = "PENDING"
                )
            )
            ScheduledTaskWorker.enqueueSystemJob(context, id, delayMillis)
            showBanner("Task #$id scheduled for +$delayMinutes min in SQLite queue.")
        }
    }

    fun executeScheduledTaskImmediately(task: ScheduledTaskEntity) {
        viewModelScope.launch {
            container.sessionRepository.recordSessionChatMessage(
                phoneNumber = task.recipientPhone,
                contactName = task.recipientName,
                messageBody = task.messageContent,
                isOutgoing = true,
                isAutomatedReply = true
            )
            container.settingsRepository.incrementMessagesProcessed()
            container.ultraDao.updateScheduledTaskStatus(
                id = task.id,
                status = "EXECUTED",
                result = "Dispatched immediately by user action"
            )
            showBanner("Executed scheduled task #${task.id}.")
        }
    }

    fun cancelScheduledTask(context: Context, taskId: Long) {
        viewModelScope.launch {
            ScheduledTaskWorker.cancelSystemJob(context, taskId)
            container.ultraDao.updateScheduledTaskStatus(taskId, "CANCELLED", "Cancelled by user")
            showBanner("Cancelled scheduled task #$taskId.")
        }
    }

    fun deleteScheduledTask(context: Context, taskId: Long) {
        viewModelScope.launch {
            ScheduledTaskWorker.cancelSystemJob(context, taskId)
            container.ultraDao.deleteScheduledTask(taskId)
        }
    }

    // Logs & Storage Management
    fun clearActivityLogs() {
        viewModelScope.launch {
            container.ultraDao.clearActivityLogs()
            showBanner("Cleared local activity logs.")
        }
    }

    fun exportEncryptedSettingsBackup(): String {
        val payload = container.settingsRepository.exportEncryptedSettingsJson()
        showBanner("Generated AES-256-GCM encrypted settings backup.")
        return payload
    }

    fun importEncryptedSettingsBackup(payload: String) {
        val ok = container.settingsRepository.importEncryptedSettingsJson(payload)
        if (ok) {
            showBanner("Restored settings from backup.")
        } else {
            showBanner("Failed to decrypt or parse backup payload.")
        }
    }

    fun wipeAllLocalData(context: Context) {
        viewModelScope.launch {
            WhatsAppForegroundService.stopService(context)
            container.engineManager.deleteSavedSessionPermanently()
            container.ultraDao.clearActivityLogs()
            container.ultraDao.clearSessionChats()
            container.ultraDao.clearSessionContacts()
            container.settingsRepository.resetAllSettings()
            container.secureStorage.wipeAllLocalFiles()
            container.nodeRuntimeManager.extractBundledEngineAssets()
            container.commandRepository.ensureCoreCommandsAndPluginsSeeded()
            refreshStorageBreakdown()
            showBanner("Wiped all local application data and restored clean factory defaults.")
        }
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return UltraViewModel(container) as T
                }
            }
    }
}
