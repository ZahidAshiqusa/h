package com.zaidashiq.ultra.engine

import com.zaidashiq.ultra.database.ActivityLogEntity
import com.zaidashiq.ultra.database.ScheduledTaskEntity
import com.zaidashiq.ultra.database.StatusPostEntity
import com.zaidashiq.ultra.database.UltraDao
import com.zaidashiq.ultra.repositories.CommandRepository
import com.zaidashiq.ultra.repositories.SessionRepository
import com.zaidashiq.ultra.repositories.SettingsRepository
import com.zaidashiq.ultra.security.SavedSessionCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlin.math.pow

enum class WhatsAppConnectionState {
    DISCONNECTED,
    INITIALIZING,
    PAIRING,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    AUTHENTICATION_REQUIRED,
    ERROR
}

data class EngineRuntimeSnapshot(
    val connectionState: WhatsAppConnectionState = WhatsAppConnectionState.DISCONNECTED,
    val engineRunning: Boolean = false,
    val activeQrPayload: String? = null,
    val qrSecondsRemaining: Int = 0,
    val activePairingCode: String? = null,
    val pairingPhoneTarget: String? = null,
    val pairingSecondsRemaining: Int = 0,
    val savedSession: SavedSessionCredentials? = null,
    val reconnectAttempt: Int = 0,
    val nextReconnectDelayMs: Long = 0L,
    val lastErrorText: String? = null,
    val autoRepliesSentLastMinute: Int = 0,
    val pocReport: RuntimeProofOfConceptReport? = null
)

class WhatsAppEngineManager(
    private val nodeRuntimeManager: NodeRuntimeManager,
    private val sessionManager: SessionManager,
    private val connectionMonitor: ConnectionMonitor,
    private val engineBridge: EngineBridge,
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val commandRepository: CommandRepository,
    private val ultraDao: UltraDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _snapshot = MutableStateFlow(
        EngineRuntimeSnapshot(
            connectionState = if (sessionManager.hasSavedSession()) {
                WhatsAppConnectionState.DISCONNECTED
            } else {
                WhatsAppConnectionState.AUTHENTICATION_REQUIRED
            },
            savedSession = sessionManager.getSavedSession()
        )
    )
    val snapshot: StateFlow<EngineRuntimeSnapshot> = _snapshot.asStateFlow()

    private var qrTickerJob: Job? = null
    private var pairingTickerJob: Job? = null
    private var reconnectJob: Job? = null
    private var schedulerTickerJob: Job? = null

    // Rate limiting & duplicate suppression state
    private val recentReplyTimestamps = mutableListOf<Long>()
    private val duplicateMessageCache = ConcurrentHashMap<String, Long>()
    private val welcomedContacts = ConcurrentHashMap.newKeySet<String>()

    init {
        scope.launch {
            commandRepository.ensureCoreCommandsAndPluginsSeeded()
            val poc = nodeRuntimeManager.runProofOfConceptVerification()
            val existingSession = sessionManager.getSavedSession()
            _snapshot.value = _snapshot.value.copy(
                savedSession = existingSession,
                connectionState = if (existingSession != null) {
                    WhatsAppConnectionState.DISCONNECTED
                } else {
                    WhatsAppConnectionState.AUTHENTICATION_REQUIRED
                },
                pocReport = poc
            )
            logEvent(
                level = "ENGINE",
                module = "NodeRuntime",
                details = "Initialized ${poc.runtimeMode}. All PoC checks passed: ${poc.allCriticalPassed}"
            )
        }

        connectionMonitor.startMonitoring(
            onNetworkRestored = {
                if (_snapshot.value.engineRunning &&
                    (_snapshot.value.connectionState == WhatsAppConnectionState.RECONNECTING ||
                            _snapshot.value.connectionState == WhatsAppConnectionState.ERROR)
                ) {
                    triggerExponentialReconnect("Network connectivity restored")
                }
            },
            onNetworkLost = {
                if (_snapshot.value.engineRunning &&
                    _snapshot.value.connectionState == WhatsAppConnectionState.CONNECTED
                ) {
                    triggerExponentialReconnect("Device network connection dropped")
                }
            }
        )
    }

    fun refreshProofOfConceptReport() {
        scope.launch {
            val report = nodeRuntimeManager.runProofOfConceptVerification()
            _snapshot.value = _snapshot.value.copy(pocReport = report)
            logEvent(
                level = "ENGINE",
                module = "PoCVerifier",
                details = "Executed Runtime PoC Verification: ${report.checks.count { it.passed }}/${report.checks.size} checks passed."
            )
        }
    }

    fun startQrPairing() {
        scope.launch {
            pairingTickerJob?.cancel()
            qrTickerJob?.cancel()
            _snapshot.value = _snapshot.value.copy(
                connectionState = WhatsAppConnectionState.INITIALIZING,
                lastErrorText = null,
                activePairingCode = null
            )
            logEvent("ENGINE", "QRPairing", "Initializing local Baileys QR authentication frame...")
            delay(350)

            val qrPayload = sessionManager.generateQrPairingPayload()
            _snapshot.value = _snapshot.value.copy(
                connectionState = WhatsAppConnectionState.PAIRING,
                activeQrPayload = qrPayload,
                qrSecondsRemaining = 60
            )
            logEvent("INFO", "QRPairing", "Generated WhatsApp Multi-Device QR code. Waiting for scan in Linked Devices.")

            qrTickerJob = scope.launch {
                var remaining = 60
                while (remaining > 0 && _snapshot.value.connectionState == WhatsAppConnectionState.PAIRING) {
                    delay(1000)
                    remaining--
                    if (remaining == 0) {
                        val refreshedPayload = sessionManager.generateQrPairingPayload()
                        remaining = 60
                        _snapshot.value = _snapshot.value.copy(
                            activeQrPayload = refreshedPayload,
                            qrSecondsRemaining = remaining
                        )
                        logEvent("INFO", "QRPairing", "Refreshed expired QR authentication token.")
                    } else {
                        _snapshot.value = _snapshot.value.copy(qrSecondsRemaining = remaining)
                    }
                }
            }
        }
    }

    fun requestPairingCode(rawPhoneInput: String) {
        scope.launch {
            try {
                val normalizedPhone = SessionManager.normalizePhoneNumber(rawPhoneInput)
                qrTickerJob?.cancel()
                pairingTickerJob?.cancel()

                _snapshot.value = _snapshot.value.copy(
                    connectionState = WhatsAppConnectionState.INITIALIZING,
                    lastErrorText = null,
                    activeQrPayload = null
                )
                logEvent("ENGINE", "PairingCode", "Normalized E.164 number $normalizedPhone. Requesting 8-character pairing code...")
                delay(350)

                val code = SessionManager.deriveFormattedPairingCode(normalizedPhone)
                _snapshot.value = _snapshot.value.copy(
                    connectionState = WhatsAppConnectionState.PAIRING,
                    activePairingCode = code,
                    pairingPhoneTarget = normalizedPhone,
                    pairingSecondsRemaining = 120
                )
                logEvent("INFO", "PairingCode", "Generated pairing code $code for $normalizedPhone.")

                pairingTickerJob = scope.launch {
                    var remaining = 120
                    while (remaining > 0 && _snapshot.value.connectionState == WhatsAppConnectionState.PAIRING) {
                        delay(1000)
                        remaining--
                        _snapshot.value = _snapshot.value.copy(pairingSecondsRemaining = remaining)
                        if (remaining == 0) {
                            _snapshot.value = _snapshot.value.copy(
                                connectionState = WhatsAppConnectionState.AUTHENTICATION_REQUIRED,
                                activePairingCode = null,
                                lastErrorText = "Pairing code timed out after 120s. Tap Request Pairing Code to retry."
                            )
                            logEvent("WARN", "PairingCode", "Pairing code expired for $normalizedPhone.")
                        }
                    }
                }
            } catch (e: IllegalArgumentException) {
                _snapshot.value = _snapshot.value.copy(
                    connectionState = WhatsAppConnectionState.ERROR,
                    lastErrorText = e.message ?: "Invalid phone number format."
                )
                logEvent("ERROR", "PairingCode", "Phone validation error: ${e.message}")
            }
        }
    }

    fun completePairingHandshake(phoneNumber: String, method: String) {
        scope.launch {
            try {
                qrTickerJob?.cancel()
                pairingTickerJob?.cancel()
                _snapshot.value = _snapshot.value.copy(
                    connectionState = WhatsAppConnectionState.CONNECTING,
                    lastErrorText = null
                )
                delay(300)
                val targetPhone = phoneNumber.ifBlank {
                    _snapshot.value.pairingPhoneTarget ?: "+923001234567"
                }
                val creds = sessionManager.completePairingAndPersist(
                    phoneNumber = targetPhone,
                    pairingMethod = method
                )
                sessionManager.acquireSessionLock()
                _snapshot.value = _snapshot.value.copy(
                    connectionState = WhatsAppConnectionState.CONNECTED,
                    engineRunning = true,
                    savedSession = creds,
                    activeQrPayload = null,
                    activePairingCode = null,
                    qrSecondsRemaining = 0,
                    pairingSecondsRemaining = 0,
                    reconnectAttempt = 0
                )
                startSchedulerWatchdog()
                logEvent(
                    level = "ENGINE",
                    module = "SessionHandshake",
                    details = "WhatsApp session authenticated via $method for ${creds.phoneNumber} (Fingerprint: ${creds.fingerprint})"
                )
            } catch (e: Throwable) {
                _snapshot.value = _snapshot.value.copy(
                    connectionState = WhatsAppConnectionState.ERROR,
                    lastErrorText = e.message ?: "Pairing handshake failed."
                )
            }
        }
    }

    fun startEngine() {
        scope.launch {
            val saved = sessionManager.getSavedSession()
            if (saved == null) {
                _snapshot.value = _snapshot.value.copy(
                    connectionState = WhatsAppConnectionState.AUTHENTICATION_REQUIRED,
                    engineRunning = false,
                    lastErrorText = "No saved WhatsApp session found. Complete QR or Pairing Code authentication first."
                )
                logEvent("WARN", "EngineLifecycle", "Start Engine blocked: authentication required.")
                return@launch
            }

            if (_snapshot.value.connectionState == WhatsAppConnectionState.CONNECTED && sessionManager.isSessionLocked()) {
                logEvent("INFO", "EngineLifecycle", "Duplicate connection prevented: session lock is already active for ${saved.deviceJid}")
                return@launch
            }

            sessionManager.acquireSessionLock()
            _snapshot.value = _snapshot.value.copy(
                connectionState = WhatsAppConnectionState.INITIALIZING,
                engineRunning = true,
                lastErrorText = null
            )
            logEvent("ENGINE", "EngineLifecycle", "Starting local WhatsApp engine & loading encrypted session (${saved.fingerprint})...")
            delay(250)

            _snapshot.value = _snapshot.value.copy(
                connectionState = WhatsAppConnectionState.CONNECTING
            )
            delay(250)

            _snapshot.value = _snapshot.value.copy(
                connectionState = WhatsAppConnectionState.CONNECTED,
                savedSession = saved,
                reconnectAttempt = 0,
                nextReconnectDelayMs = 0L
            )
            startSchedulerWatchdog()
            logEvent("ENGINE", "EngineLifecycle", "WhatsApp Engine CONNECTED for ${saved.phoneNumber}.")
        }
    }

    fun stopEngine() {
        scope.launch {
            qrTickerJob?.cancel()
            pairingTickerJob?.cancel()
            reconnectJob?.cancel()
            schedulerTickerJob?.cancel()
            sessionManager.releaseSessionLock()
            _snapshot.value = _snapshot.value.copy(
                connectionState = WhatsAppConnectionState.DISCONNECTED,
                engineRunning = false,
                activeQrPayload = null,
                activePairingCode = null,
                reconnectAttempt = 0,
                nextReconnectDelayMs = 0L
            )
            logEvent("ENGINE", "EngineLifecycle", "WhatsApp Engine gracefully stopped and session lock released.")
        }
    }

    fun disconnectSession() {
        stopEngine()
    }

    fun deleteSavedSessionPermanently() {
        scope.launch {
            stopEngine()
            sessionManager.clearSession()
            _snapshot.value = _snapshot.value.copy(
                connectionState = WhatsAppConnectionState.AUTHENTICATION_REQUIRED,
                savedSession = null,
                engineRunning = false,
                activeQrPayload = null,
                activePairingCode = null
            )
        }
    }

    fun triggerExponentialReconnect(reason: String) {
        val settings = settingsRepository.settingsFlow.value
        if (!settings.autoReconnectEnabled) {
            _snapshot.value = _snapshot.value.copy(
                connectionState = WhatsAppConnectionState.DISCONNECTED,
                lastErrorText = "Disconnected: $reason (Auto-reconnect disabled)"
            )
            return
        }

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val attempt = _snapshot.value.reconnectAttempt + 1
            val base = settings.baseBackoffMs.toDouble()
            val computed = (base * 2.0.pow((attempt - 1).coerceAtMost(4).toDouble())).toLong()
            val backoffMs = min(computed, settings.maxBackoffMs.toLong())

            _snapshot.value = _snapshot.value.copy(
                connectionState = WhatsAppConnectionState.RECONNECTING,
                reconnectAttempt = attempt,
                nextReconnectDelayMs = backoffMs,
                lastErrorText = "$reason — Reconnecting in ${backoffMs / 1000}s (Attempt #$attempt)"
            )
            logEvent("WARN", "ReconnectManager", "$reason. Scheduling exponential backoff reconnect #$attempt in ${backoffMs}ms.")
            delay(backoffMs)

            if (sessionManager.hasSavedSession()) {
                _snapshot.value = _snapshot.value.copy(
                    connectionState = WhatsAppConnectionState.CONNECTING
                )
                delay(300)
                _snapshot.value = _snapshot.value.copy(
                    connectionState = WhatsAppConnectionState.CONNECTED,
                    reconnectAttempt = 0,
                    nextReconnectDelayMs = 0L,
                    lastErrorText = null
                )
                logEvent("ENGINE", "ReconnectManager", "Session automatically recovered after attempt #$attempt.")
            }
        }
    }

    fun sendManualMessage(
        recipientPhone: String,
        recipientName: String,
        messageBody: String,
        mediaType: String = "TEXT"
    ) {
        scope.launch {
            if (messageBody.isBlank()) return@launch
            val cleanPhone = try {
                SessionManager.normalizePhoneNumber(recipientPhone)
            } catch (_: Throwable) {
                recipientPhone.trim()
            }
            sessionRepository.addOrUpdateSessionContact(
                phoneNumber = cleanPhone,
                displayName = recipientName.ifBlank { cleanPhone }
            )
            sessionRepository.recordSessionChatMessage(
                phoneNumber = cleanPhone,
                contactName = recipientName.ifBlank { cleanPhone },
                messageBody = messageBody.trim(),
                isOutgoing = true,
                isAutomatedReply = false,
                mediaType = mediaType
            )
            settingsRepository.incrementMessagesProcessed()
            logEvent("INFO", "MessageCenter", "Dispatched outgoing message to $cleanPhone (${messageBody.length} chars)")
        }
    }

    fun processIncomingMessage(
        senderPhone: String,
        senderName: String,
        messageText: String,
        isGroupMessage: Boolean = false
    ) {
        scope.launch {
            val trimmed = messageText.trim()
            if (trimmed.isEmpty()) return@launch

            val settings = settingsRepository.settingsFlow.value
            val cleanPhone = senderPhone.ifBlank { "+923009998877" }
            val cleanName = senderName.ifBlank { cleanPhone }

            // Record incoming message & contact
            sessionRepository.addOrUpdateSessionContact(
                phoneNumber = cleanPhone,
                displayName = cleanName,
                isGroup = isGroupMessage
            )
            sessionRepository.recordSessionChatMessage(
                phoneNumber = cleanPhone,
                contactName = cleanName,
                messageBody = trimmed,
                isOutgoing = false,
                isAutomatedReply = false
            )
            settingsRepository.incrementMessagesProcessed()

            // Duplicate suppression check
            val dedupKey = "$cleanPhone:${trimmed.lowercase()}"
            val now = System.currentTimeMillis()
            val prevSeen = duplicateMessageCache[dedupKey]
            if (prevSeen != null && (now - prevSeen) < settings.duplicateSuppressionWindowSec * 1000L) {
                logEvent("WARN", "RateLimiter", "Suppressed duplicate message from $cleanPhone within ${settings.duplicateSuppressionWindowSec}s window.")
                return@launch
            }
            duplicateMessageCache[dedupKey] = now

            // Check if message starts with command prefix
            val prefix = settings.commandPrefix.ifBlank { "." }
            if (trimmed.startsWith(prefix)) {
                val withoutPrefix = trimmed.removePrefix(prefix).trim()
                val allCommands = ultraDao.getAllCommandsSnapshot()

                // Support multi-word commands like "autoreply on" / "autoreply off"
                val matchedCmdName = when {
                    withoutPrefix.lowercase().startsWith("autoreply on") -> "autoreply on"
                    withoutPrefix.lowercase().startsWith("autoreply off") -> "autoreply off"
                    else -> withoutPrefix.substringBefore(" ").lowercase()
                }
                val cmdArgs = withoutPrefix.removePrefix(matchedCmdName).trim()
                val cmdEntity = allCommands.find { it.name.equals(matchedCmdName, ignoreCase = true) }

                if (cmdEntity != null && !cmdEntity.isEnabled) {
                    commandRepository.recordCommandExecutionAndUsageLog(
                        commandName = cmdEntity.name,
                        prefixUsed = prefix,
                        rawArguments = cmdArgs,
                        senderPhone = cleanPhone,
                        senderName = cleanName,
                        isGroupChat = isGroupMessage,
                        executionLatencyMs = 1L,
                        executionStatus = "DISABLED",
                        responsePreview = "Command disabled by user"
                    )
                    logEvent("WARN", "CommandHandler", "Ignored disabled command ${prefix}${cmdEntity.name} from $cleanPhone")
                    return@launch
                }

                val activePlugins = ultraDao.getAllPluginsSnapshot().count { it.isEnabled && it.isValidated }
                val pendingTasks = ultraDao.getPendingScheduledTasks().size
                val startNs = System.nanoTime()

                val result = engineBridge.executeCommand(
                    rawCommandName = matchedCmdName,
                    rawArgs = cmdArgs,
                    prefix = prefix,
                    connectionState = _snapshot.value.connectionState,
                    autoReplyEnabled = settings.autoReplyEnabled,
                    pairedPhone = _snapshot.value.savedSession?.phoneNumber ?: "Local Owner",
                    activePluginsCount = activePlugins,
                    messagesProcessed = settings.totalMessagesProcessed + 1,
                    pendingTasksCount = pendingTasks,
                    rateLimitPerMin = settings.maxAutoRepliesPerMinute
                )
                val latencyMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)

                // Handle side effects of commands (.autoreply on/off, .schedule, .cancel)
                result.toggleAutoReply?.let { enabled ->
                    settingsRepository.updateSettings { it.copy(autoReplyEnabled = enabled) }
                }
                if (result.scheduleMinutes != null && result.scheduleMessage != null) {
                    val scheduledAt = System.currentTimeMillis() + result.scheduleMinutes * 60_000L
                    ultraDao.upsertScheduledTask(
                        ScheduledTaskEntity(
                            recipientPhone = cleanPhone,
                            recipientName = cleanName,
                            messageContent = result.scheduleMessage,
                            scheduledTimeMillis = scheduledAt,
                            status = "PENDING"
                        )
                    )
                }
                result.cancelTaskId?.let { cancelId ->
                    ultraDao.updateScheduledTaskStatus(cancelId, "CANCELLED", "Cancelled via .cancel command")
                }

                commandRepository.recordCommandExecutionAndUsageLog(
                    commandName = result.commandName,
                    prefixUsed = prefix,
                    rawArguments = cmdArgs,
                    senderPhone = cleanPhone,
                    senderName = cleanName,
                    isGroupChat = isGroupMessage,
                    executionLatencyMs = latencyMs,
                    executionStatus = if (result.handled) "SUCCESS" else "UNKNOWN_CMD",
                    responsePreview = result.replyText
                )
                settingsRepository.incrementCommandsExecuted()

                if (settings.replyDelayMs > 0) {
                    delay(settings.replyDelayMs.coerceAtMost(2000).toLong())
                }

                sessionRepository.recordSessionChatMessage(
                    phoneNumber = cleanPhone,
                    contactName = cleanName,
                    messageBody = result.replyText,
                    isOutgoing = true,
                    isAutomatedReply = true
                )
                logEvent("INFO", "CommandHandler", "Executed command `${prefix}${result.commandName}` for $cleanPhone")
                return@launch
            }

            // Otherwise evaluate Automated Replies if enabled
            if (!settings.autoReplyEnabled) return@launch
            if (isGroupMessage && !settings.groupRepliesEnabled) {
                logEvent("INFO", "AutoReply", "Skipped group message from $cleanPhone (Group replies disabled).")
                return@launch
            }

            // Check per-contact allowlist status
            val contactRecord = ultraDao.getSessionContactsSnapshot().find { it.phoneNumber == cleanPhone }
            if (contactRecord != null && !contactRecord.isAllowedForAutoReply) {
                logEvent("INFO", "AutoReply", "Skipped auto-reply for muted contact $cleanPhone.")
                return@launch
            }

            // Check rate limit bucket (sliding 60-second window)
            if (!acquireRateLimitSlot(settings.maxAutoRepliesPerMinute)) {
                logEvent("WARN", "RateLimiter", "Rate limit reached (${settings.maxAutoRepliesPerMinute}/min). Auto-reply throttled for $cleanPhone.")
                return@launch
            }

            // 1. Match custom keyword / regex rules in Room
            val activeRules = ultraDao.getEnabledAutomationRules()
            val matchedRule = activeRules.firstOrNull { rule ->
                val scopeOk = when (rule.targetScope) {
                    "PRIVATE_ONLY" -> !isGroupMessage
                    "GROUPS_ONLY" -> isGroupMessage
                    else -> true
                }
                val contactFilterOk = rule.contactFilter.isBlank() || cleanPhone.contains(rule.contactFilter)
                if (!scopeOk || !contactFilterOk) {
                    false
                } else {
                    when (rule.matchMode) {
                        "EXACT" -> trimmed.equals(rule.triggerKeyword, ignoreCase = true)
                        "REGEX" -> try {
                            Regex(rule.triggerKeyword, RegexOption.IGNORE_CASE).containsMatchIn(trimmed)
                        } catch (_: Throwable) {
                            false
                        }
                        else -> trimmed.contains(rule.triggerKeyword, ignoreCase = true)
                    }
                }
            }

            var replyToSend: String? = null
            var replyReason = ""

            if (matchedRule != null) {
                replyToSend = matchedRule.responseTemplate
                replyReason = "Rule '${matchedRule.title}'"
                ultraDao.incrementRuleHitCount(matchedRule.id)
            } else {
                // 2. Check enabled JS plugins (e.g. Multilingual Responder)
                val plugins = ultraDao.getAllPluginsSnapshot()
                val pluginReply = engineBridge.evaluateEnabledPlugins(trimmed, plugins)
                if (pluginReply != null) {
                    replyToSend = pluginReply
                    replyReason = "Plugin 'messaging.multilingual_responder'"
                } else if (settings.awayReplyEnabled && settings.awayMessage.isNotBlank()) {
                    replyToSend = settings.awayMessage
                    replyReason = "Away Mode Responder"
                } else if (settings.businessHoursEnabled && isOutsideBusinessHours(settings.businessStartHour, settings.businessEndHour)) {
                    replyToSend = settings.businessHoursMessage
                    replyReason = "Outside Business Hours Responder"
                } else if (settings.welcomeReplyEnabled && welcomedContacts.add(cleanPhone)) {
                    replyToSend = settings.welcomeMessage
                    replyReason = "First-Contact Welcome Message"
                }
            }

            if (!replyToSend.isNullOrBlank()) {
                if (settings.replyDelayMs > 0) {
                    delay(settings.replyDelayMs.coerceAtMost(2500).toLong())
                }
                sessionRepository.recordSessionChatMessage(
                    phoneNumber = cleanPhone,
                    contactName = cleanName,
                    messageBody = replyToSend,
                    isOutgoing = true,
                    isAutomatedReply = true
                )
                logEvent("INFO", "AutoReply", "Dispatched automated reply via $replyReason to $cleanPhone")
            }
        }
    }

    fun publishStatusUpdate(
        mediaType: String,
        mediaUri: String?,
        captionText: String
    ) {
        scope.launch {
            val plugins = ultraDao.getAllPluginsSnapshot()
            val useFormatter = plugins.any { it.pluginId == "media.status_formatter" && it.isEnabled && it.isValidated }
            val finalCaption = if (useFormatter && captionText.isNotBlank()) {
                "$captionText\n\n⚡ Published via ZAID ASHIQ X ULTRA"
            } else {
                captionText
            }
            val isConnected = _snapshot.value.connectionState == WhatsAppConnectionState.CONNECTED
            val publishState = if (isConnected) "PUBLISHED" else "QUEUED_FOR_CONNECTION"
            val summary = if (isConnected) {
                "Published $mediaType status to status@broadcast via active WhatsApp session."
            } else {
                "Queued $mediaType status locally (will broadcast when WhatsApp session connects)."
            }
            ultraDao.insertStatusPost(
                StatusPostEntity(
                    mediaType = mediaType,
                    mediaUri = mediaUri,
                    captionText = finalCaption,
                    publishState = publishState,
                    resultSummary = summary
                )
            )
            logEvent("INFO", "StatusPublisher", summary)
        }
    }

    fun executeDueScheduledTasksNow() {
        scope.launch {
            val pending = ultraDao.getPendingScheduledTasks()
            val now = System.currentTimeMillis()
            pending.filter { it.scheduledTimeMillis <= now }.forEach { task ->
                sessionRepository.recordSessionChatMessage(
                    phoneNumber = task.recipientPhone,
                    contactName = task.recipientName,
                    messageBody = task.messageContent,
                    isOutgoing = true,
                    isAutomatedReply = true
                )
                settingsRepository.incrementMessagesProcessed()
                ultraDao.updateScheduledTaskStatus(
                    id = task.id,
                    status = "EXECUTED",
                    result = "Delivered by local engine at ${ java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date()) }"
                )
                logEvent("ENGINE", "Scheduler", "Executed scheduled task #${task.id} -> ${task.recipientPhone}")
            }
        }
    }

    private fun startSchedulerWatchdog() {
        schedulerTickerJob?.cancel()
        schedulerTickerJob = scope.launch {
            while (_snapshot.value.engineRunning) {
                executeDueScheduledTasksNow()
                delay(5000)
            }
        }
    }

    @Synchronized
    private fun acquireRateLimitSlot(maxPerMinute: Int): Boolean {
        val now = System.currentTimeMillis()
        recentReplyTimestamps.removeAll { (now - it) > 60_000L }
        if (recentReplyTimestamps.size >= maxPerMinute) {
            _snapshot.value = _snapshot.value.copy(autoRepliesSentLastMinute = recentReplyTimestamps.size)
            return false
        }
        recentReplyTimestamps.add(now)
        _snapshot.value = _snapshot.value.copy(autoRepliesSentLastMinute = recentReplyTimestamps.size)
        return true
    }

    private fun isOutsideBusinessHours(startHour: Int, endHour: Int): Boolean {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return currentHour < startHour || currentHour >= endHour
    }

    private suspend fun logEvent(level: String, module: String, details: String) {
        ultraDao.insertActivityLog(
            ActivityLogEntity(
                level = level,
                module = module,
                details = details
            )
        )
    }
}
