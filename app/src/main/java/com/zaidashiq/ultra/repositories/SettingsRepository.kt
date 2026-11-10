package com.zaidashiq.ultra.repositories

import android.content.Context
import android.content.SharedPreferences
import com.zaidashiq.ultra.security.EncryptionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class LocalAppSettings(
    val appDisplayName: String = "ZAID ASHIQ X ULTRA V. CODER",
    val accentCyan: Boolean = true,
    val uiLanguage: String = "EN", // EN, UR, ROMAN_UR
    val notificationsEnabled: Boolean = true,
    val startOnBootEnabled: Boolean = false,
    val autoReconnectEnabled: Boolean = true,
    val baseBackoffMs: Int = 2000,
    val maxBackoffMs: Int = 32000,
    val commandPrefix: String = ".",
    val autoReplyEnabled: Boolean = true,
    val welcomeReplyEnabled: Boolean = true,
    val awayReplyEnabled: Boolean = false,
    val businessHoursEnabled: Boolean = false,
    val groupRepliesEnabled: Boolean = false,
    val businessStartHour: Int = 9,
    val businessEndHour: Int = 18,
    val welcomeMessage: String = "Assalam-o-Alaikum! Welcome to ZAID ASHIQ X ULTRA V. CODER automated session. Type .menu or .help to view available commands.",
    val awayMessage: String = "Currently away from my device. Your message has been logged locally — I will reply shortly.",
    val businessHoursMessage: String = "Thank you for reaching out outside business hours (09:00 - 18:00). We will respond during active hours.",
    val replyDelayMs: Int = 1200,
    val maxAutoRepliesPerMinute: Int = 15,
    val duplicateSuppressionWindowSec: Int = 45,
    val totalMessagesProcessed: Int = 0,
    val totalCommandsExecuted: Int = 0
)

class SettingsRepository(
    context: Context,
    private val encryptionManager: EncryptionManager
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(readFromPrefs())
    val settingsFlow: StateFlow<LocalAppSettings> = _settingsFlow.asStateFlow()

    private fun readFromPrefs(): LocalAppSettings {
        return LocalAppSettings(
            appDisplayName = prefs.getString("appDisplayName", "ZAID ASHIQ X ULTRA V. CODER") ?: "ZAID ASHIQ X ULTRA V. CODER",
            accentCyan = prefs.getBoolean("accentCyan", true),
            uiLanguage = prefs.getString("uiLanguage", "EN") ?: "EN",
            notificationsEnabled = prefs.getBoolean("notificationsEnabled", true),
            startOnBootEnabled = prefs.getBoolean("startOnBootEnabled", false),
            autoReconnectEnabled = prefs.getBoolean("autoReconnectEnabled", true),
            baseBackoffMs = prefs.getInt("baseBackoffMs", 2000),
            maxBackoffMs = prefs.getInt("maxBackoffMs", 32000),
            commandPrefix = prefs.getString("commandPrefix", ".") ?: ".",
            autoReplyEnabled = prefs.getBoolean("autoReplyEnabled", true),
            welcomeReplyEnabled = prefs.getBoolean("welcomeReplyEnabled", true),
            awayReplyEnabled = prefs.getBoolean("awayReplyEnabled", false),
            businessHoursEnabled = prefs.getBoolean("businessHoursEnabled", false),
            groupRepliesEnabled = prefs.getBoolean("groupRepliesEnabled", false),
            businessStartHour = prefs.getInt("businessStartHour", 9),
            businessEndHour = prefs.getInt("businessEndHour", 18),
            welcomeMessage = prefs.getString(
                "welcomeMessage",
                "Assalam-o-Alaikum! Welcome to ZAID ASHIQ X ULTRA V. CODER automated session. Type .menu or .help to view available commands."
            ) ?: "",
            awayMessage = prefs.getString(
                "awayMessage",
                "Currently away from my device. Your message has been logged locally — I will reply shortly."
            ) ?: "",
            businessHoursMessage = prefs.getString(
                "businessHoursMessage",
                "Thank you for reaching out outside business hours (09:00 - 18:00). We will respond during active hours."
            ) ?: "",
            replyDelayMs = prefs.getInt("replyDelayMs", 1200),
            maxAutoRepliesPerMinute = prefs.getInt("maxAutoRepliesPerMinute", 15),
            duplicateSuppressionWindowSec = prefs.getInt("duplicateSuppressionWindowSec", 45),
            totalMessagesProcessed = prefs.getInt("totalMessagesProcessed", 0),
            totalCommandsExecuted = prefs.getInt("totalCommandsExecuted", 0)
        )
    }

    fun updateSettings(transform: (LocalAppSettings) -> LocalAppSettings) {
        val updated = transform(_settingsFlow.value)
        prefs.edit().apply {
            putString("appDisplayName", updated.appDisplayName)
            putBoolean("accentCyan", updated.accentCyan)
            putString("uiLanguage", updated.uiLanguage)
            putBoolean("notificationsEnabled", updated.notificationsEnabled)
            putBoolean("startOnBootEnabled", updated.startOnBootEnabled)
            putBoolean("autoReconnectEnabled", updated.autoReconnectEnabled)
            putInt("baseBackoffMs", updated.baseBackoffMs)
            putInt("maxBackoffMs", updated.maxBackoffMs)
            putString("commandPrefix", updated.commandPrefix.ifBlank { "." })
            putBoolean("autoReplyEnabled", updated.autoReplyEnabled)
            putBoolean("welcomeReplyEnabled", updated.welcomeReplyEnabled)
            putBoolean("awayReplyEnabled", updated.awayReplyEnabled)
            putBoolean("businessHoursEnabled", updated.businessHoursEnabled)
            putBoolean("groupRepliesEnabled", updated.groupRepliesEnabled)
            putInt("businessStartHour", updated.businessStartHour.coerceIn(0, 23))
            putInt("businessEndHour", updated.businessEndHour.coerceIn(0, 23))
            putString("welcomeMessage", updated.welcomeMessage)
            putString("awayMessage", updated.awayMessage)
            putString("businessHoursMessage", updated.businessHoursMessage)
            putInt("replyDelayMs", updated.replyDelayMs.coerceIn(0, 30000))
            putInt("maxAutoRepliesPerMinute", updated.maxAutoRepliesPerMinute.coerceIn(1, 120))
            putInt("duplicateSuppressionWindowSec", updated.duplicateSuppressionWindowSec.coerceIn(5, 3600))
            putInt("totalMessagesProcessed", updated.totalMessagesProcessed)
            putInt("totalCommandsExecuted", updated.totalCommandsExecuted)
        }.apply()
        _settingsFlow.value = updated
    }

    fun incrementMessagesProcessed() {
        updateSettings { it.copy(totalMessagesProcessed = it.totalMessagesProcessed + 1) }
    }

    fun incrementCommandsExecuted() {
        updateSettings { it.copy(totalCommandsExecuted = it.totalCommandsExecuted + 1) }
    }

    fun exportEncryptedSettingsJson(): String {
        val s = _settingsFlow.value
        val json = JSONObject().apply {
            put("appDisplayName", s.appDisplayName)
            put("accentCyan", s.accentCyan)
            put("uiLanguage", s.uiLanguage)
            put("notificationsEnabled", s.notificationsEnabled)
            put("startOnBootEnabled", s.startOnBootEnabled)
            put("autoReconnectEnabled", s.autoReconnectEnabled)
            put("baseBackoffMs", s.baseBackoffMs)
            put("maxBackoffMs", s.maxBackoffMs)
            put("commandPrefix", s.commandPrefix)
            put("autoReplyEnabled", s.autoReplyEnabled)
            put("welcomeReplyEnabled", s.welcomeReplyEnabled)
            put("awayReplyEnabled", s.awayReplyEnabled)
            put("businessHoursEnabled", s.businessHoursEnabled)
            put("groupRepliesEnabled", s.groupRepliesEnabled)
            put("welcomeMessage", s.welcomeMessage)
            put("awayMessage", s.awayMessage)
            put("businessHoursMessage", s.businessHoursMessage)
            put("replyDelayMs", s.replyDelayMs)
            put("maxAutoRepliesPerMinute", s.maxAutoRepliesPerMinute)
            put("duplicateSuppressionWindowSec", s.duplicateSuppressionWindowSec)
        }.toString(2)
        return encryptionManager.encryptToBase64(json)
    }

    fun importEncryptedSettingsJson(encryptedPayload: String): Boolean {
        return try {
            val rawJson = if (encryptedPayload.trim().startsWith("{")) {
                encryptedPayload.trim()
            } else {
                encryptionManager.decryptFromBase64(encryptedPayload.trim())
            }
            val obj = JSONObject(rawJson)
            updateSettings { current ->
                current.copy(
                    appDisplayName = obj.optString("appDisplayName", current.appDisplayName),
                    accentCyan = obj.optBoolean("accentCyan", current.accentCyan),
                    uiLanguage = obj.optString("uiLanguage", current.uiLanguage),
                    notificationsEnabled = obj.optBoolean("notificationsEnabled", current.notificationsEnabled),
                    startOnBootEnabled = obj.optBoolean("startOnBootEnabled", current.startOnBootEnabled),
                    autoReconnectEnabled = obj.optBoolean("autoReconnectEnabled", current.autoReconnectEnabled),
                    baseBackoffMs = obj.optInt("baseBackoffMs", current.baseBackoffMs),
                    maxBackoffMs = obj.optInt("maxBackoffMs", current.maxBackoffMs),
                    commandPrefix = obj.optString("commandPrefix", current.commandPrefix),
                    autoReplyEnabled = obj.optBoolean("autoReplyEnabled", current.autoReplyEnabled),
                    welcomeReplyEnabled = obj.optBoolean("welcomeReplyEnabled", current.welcomeReplyEnabled),
                    awayReplyEnabled = obj.optBoolean("awayReplyEnabled", current.awayReplyEnabled),
                    businessHoursEnabled = obj.optBoolean("businessHoursEnabled", current.businessHoursEnabled),
                    groupRepliesEnabled = obj.optBoolean("groupRepliesEnabled", current.groupRepliesEnabled),
                    welcomeMessage = obj.optString("welcomeMessage", current.welcomeMessage),
                    awayMessage = obj.optString("awayMessage", current.awayMessage),
                    businessHoursMessage = obj.optString("businessHoursMessage", current.businessHoursMessage),
                    replyDelayMs = obj.optInt("replyDelayMs", current.replyDelayMs),
                    maxAutoRepliesPerMinute = obj.optInt("maxAutoRepliesPerMinute", current.maxAutoRepliesPerMinute),
                    duplicateSuppressionWindowSec = obj.optInt("duplicateSuppressionWindowSec", current.duplicateSuppressionWindowSec)
                )
            }
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun resetAllSettings() {
        prefs.edit().clear().apply()
        _settingsFlow.value = readFromPrefs()
    }
}
