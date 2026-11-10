package com.zaidashiq.ultra.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UltraDao {

    // Automation Rules
    @Query("SELECT * FROM automation_rules ORDER BY createdAt DESC")
    fun observeAutomationRules(): Flow<List<AutomationRuleEntity>>

    @Query("SELECT * FROM automation_rules WHERE isEnabled = 1 ORDER BY createdAt DESC")
    fun observeActiveAutomationRules(): Flow<List<AutomationRuleEntity>>

    @Query("SELECT * FROM automation_rules WHERE isEnabled = 1")
    suspend fun getEnabledAutomationRules(): List<AutomationRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAutomationRule(rule: AutomationRuleEntity): Long

    @Query("UPDATE automation_rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun setAutomationRuleEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE automation_rules SET hitCount = hitCount + 1 WHERE id = :id")
    suspend fun incrementRuleHitCount(id: Long)

    @Query("DELETE FROM automation_rules WHERE id = :id")
    suspend fun deleteAutomationRule(id: Long)

    // Commands
    @Query("SELECT * FROM commands ORDER BY category ASC, name ASC")
    fun observeCommands(): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commands")
    suspend fun getAllCommandsSnapshot(): List<CommandEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommands(commands: List<CommandEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCommand(command: CommandEntity)

    @Query("UPDATE commands SET isEnabled = :enabled WHERE name = :name")
    suspend fun setCommandEnabled(name: String, enabled: Boolean)

    @Query("UPDATE commands SET executionCount = executionCount + 1, lastExecutedAt = :timestamp WHERE name = :name")
    suspend fun recordCommandExecution(name: String, timestamp: Long = System.currentTimeMillis())

    // Plugins
    @Query("SELECT * FROM plugins ORDER BY category ASC, name ASC")
    fun observePlugins(): Flow<List<PluginEntity>>

    @Query("SELECT * FROM plugins")
    suspend fun getAllPluginsSnapshot(): List<PluginEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlugins(plugins: List<PluginEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlugin(plugin: PluginEntity)

    @Query("UPDATE plugins SET isEnabled = :enabled WHERE pluginId = :pluginId")
    suspend fun setPluginEnabled(pluginId: String, enabled: Boolean)

    @Query("DELETE FROM plugins WHERE pluginId = :pluginId")
    suspend fun deletePlugin(pluginId: String)

    // Scheduled Tasks
    @Query("SELECT * FROM scheduled_tasks ORDER BY scheduledTimeMillis ASC")
    fun observeScheduledTasks(): Flow<List<ScheduledTaskEntity>>

    @Query("SELECT * FROM scheduled_tasks WHERE status = 'PENDING' ORDER BY scheduledTimeMillis ASC")
    suspend fun getPendingScheduledTasks(): List<ScheduledTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertScheduledTask(task: ScheduledTaskEntity): Long

    @Update
    suspend fun updateScheduledTask(task: ScheduledTaskEntity)

    @Query("UPDATE scheduled_tasks SET status = :status, executionResult = :result WHERE id = :id")
    suspend fun updateScheduledTaskStatus(id: Long, status: String, result: String)

    @Query("DELETE FROM scheduled_tasks WHERE id = :id")
    suspend fun deleteScheduledTask(id: Long)

    // Session Contacts
    @Query("SELECT * FROM session_contacts ORDER BY displayName ASC")
    fun observeSessionContacts(): Flow<List<SessionContactEntity>>

    @Query("SELECT * FROM session_contacts ORDER BY displayName ASC")
    suspend fun getSessionContactsSnapshot(): List<SessionContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessionContacts(contacts: List<SessionContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessionContact(contact: SessionContactEntity)

    @Query("UPDATE session_contacts SET isAllowedForAutoReply = :allowed WHERE jid = :jid")
    suspend fun setContactAutoReplyAllowed(jid: String, allowed: Boolean)

    @Query("DELETE FROM session_contacts")
    suspend fun clearSessionContacts()

    // Session Chats
    @Query("SELECT * FROM session_chats ORDER BY timestamp DESC")
    fun observeSessionChats(): Flow<List<SessionChatEntity>>

    @Query("SELECT * FROM session_chats ORDER BY timestamp ASC")
    suspend fun getSessionChatsSnapshot(): List<SessionChatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionChat(chat: SessionChatEntity): Long

    @Query("DELETE FROM session_chats")
    suspend fun clearSessionChats()

    // Status Posts
    @Query("SELECT * FROM status_posts ORDER BY publishedAt DESC")
    fun observeStatusPosts(): Flow<List<StatusPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusPost(post: StatusPostEntity): Long

    // Message Templates
    @Query("SELECT * FROM message_templates ORDER BY id ASC")
    fun observeMessageTemplates(): Flow<List<MessageTemplateEntity>>

    @Query("SELECT * FROM message_templates")
    suspend fun getMessageTemplatesSnapshot(): List<MessageTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessageTemplate(template: MessageTemplateEntity): Long

    @Query("DELETE FROM message_templates WHERE id = :id")
    suspend fun deleteMessageTemplate(id: Long)

    // Activity Logs
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 400")
    fun observeActivityLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLogEntity)

    @Query("DELETE FROM activity_logs")
    suspend fun clearActivityLogs()

    // WhatsApp Session Metadata
    @Query("SELECT * FROM whatsapp_session_metadata WHERE sessionId = 'primary_session' LIMIT 1")
    fun observeSessionMetadata(): Flow<WhatsAppSessionMetadataEntity?>

    @Query("SELECT * FROM whatsapp_session_metadata WHERE sessionId = 'primary_session' LIMIT 1")
    suspend fun getSessionMetadataSnapshot(): WhatsAppSessionMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessionMetadata(metadata: WhatsAppSessionMetadataEntity)

    @Query("UPDATE whatsapp_session_metadata SET connectionState = :state, lastConnectedAtMillis = :timestamp, reconnectCount = :reconnectCount WHERE sessionId = 'primary_session'")
    suspend fun updateSessionConnectionState(
        state: String,
        timestamp: Long = System.currentTimeMillis(),
        reconnectCount: Int = 0
    )

    @Query("DELETE FROM whatsapp_session_metadata")
    suspend fun clearSessionMetadata()

    // Command Usage Logs
    @Query("SELECT * FROM command_usage_logs ORDER BY executedAtMillis DESC LIMIT 250")
    fun observeCommandUsageLogs(): Flow<List<CommandUsageLogEntity>>

    @Query("SELECT * FROM command_usage_logs ORDER BY executedAtMillis DESC")
    suspend fun getCommandUsageLogsSnapshot(): List<CommandUsageLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommandUsageLog(log: CommandUsageLogEntity): Long

    @Query("DELETE FROM command_usage_logs")
    suspend fun clearCommandUsageLogs()
}
