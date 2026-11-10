package com.zaidashiq.ultra.repositories

import com.zaidashiq.ultra.database.ActivityLogEntity
import com.zaidashiq.ultra.database.SessionChatEntity
import com.zaidashiq.ultra.database.SessionContactEntity
import com.zaidashiq.ultra.database.UltraDao
import com.zaidashiq.ultra.database.WhatsAppSessionMetadataEntity
import com.zaidashiq.ultra.security.SavedSessionCredentials
import com.zaidashiq.ultra.security.SecureStorage
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionRepository(
    private val ultraDao: UltraDao,
    private val secureStorage: SecureStorage
) {
    val sessionContacts: Flow<List<SessionContactEntity>> = ultraDao.observeSessionContacts()
    val sessionChats: Flow<List<SessionChatEntity>> = ultraDao.observeSessionChats()
    val sessionMetadata: Flow<WhatsAppSessionMetadataEntity?> = ultraDao.observeSessionMetadata()

    fun getSavedSession(): SavedSessionCredentials? = secureStorage.loadSessionCredentials()

    fun hasSavedSession(): Boolean = secureStorage.hasEncryptedSession()

    suspend fun saveAuthenticatedSession(creds: SavedSessionCredentials) {
        secureStorage.saveSessionCredentials(creds)
        ultraDao.upsertSessionMetadata(
            WhatsAppSessionMetadataEntity(
                sessionId = "primary_session",
                phoneNumber = creds.phoneNumber,
                deviceJid = creds.deviceJid,
                pairingMethod = creds.pairingMethod,
                keystoreFingerprint = creds.fingerprint,
                connectionState = "CONNECTED",
                isKeystoreEncrypted = true,
                linkedAtMillis = creds.createdAtMillis,
                lastConnectedAtMillis = creds.lastVerifiedMillis,
                reconnectCount = 0
            )
        )
        // Seed initial session contacts exposed by the newly linked WhatsApp session if none exist yet
        val existingContacts = ultraDao.getSessionContactsSnapshot()
        if (existingContacts.isEmpty()) {
            val selfContact = SessionContactEntity(
                jid = creds.deviceJid,
                displayName = "Linked Owner (${creds.phoneNumber})",
                phoneNumber = creds.phoneNumber,
                aboutStatus = "ZAID ASHIQ X ULTRA V. CODER Active Node",
                isGroup = false,
                isAllowedForAutoReply = true
            )
            ultraDao.upsertSessionContact(selfContact)
        }
        ultraDao.insertActivityLog(
            ActivityLogEntity(
                level = "SECURITY",
                module = "SessionManager",
                details = "WhatsApp session encrypted via Android Keystore AES-256-GCM (${creds.fingerprint})"
            )
        )
    }

    suspend fun syncSessionConnectionState(state: String, reconnectCount: Int = 0) {
        val existing = ultraDao.getSessionMetadataSnapshot()
        if (existing != null) {
            ultraDao.updateSessionConnectionState(
                state = state,
                timestamp = System.currentTimeMillis(),
                reconnectCount = reconnectCount
            )
        } else {
            val savedCreds = secureStorage.loadSessionCredentials()
            if (savedCreds != null) {
                ultraDao.upsertSessionMetadata(
                    WhatsAppSessionMetadataEntity(
                        sessionId = "primary_session",
                        phoneNumber = savedCreds.phoneNumber,
                        deviceJid = savedCreds.deviceJid,
                        pairingMethod = savedCreds.pairingMethod,
                        keystoreFingerprint = savedCreds.fingerprint,
                        connectionState = state,
                        isKeystoreEncrypted = true,
                        linkedAtMillis = savedCreds.createdAtMillis,
                        lastConnectedAtMillis = System.currentTimeMillis(),
                        reconnectCount = reconnectCount
                    )
                )
            }
        }
    }

    suspend fun deleteSavedSession() {
        secureStorage.deleteSessionCredentials()
        ultraDao.clearSessionMetadata()
        ultraDao.insertActivityLog(
            ActivityLogEntity(
                level = "SECURITY",
                module = "SessionManager",
                details = "Encrypted WhatsApp session credentials permanently deleted from local storage."
            )
        )
    }

    suspend fun addOrUpdateSessionContact(
        phoneNumber: String,
        displayName: String,
        aboutStatus: String = "Available on WhatsApp",
        isGroup: Boolean = false
    ) {
        val cleanDigits = phoneNumber.replace(Regex("[^0-9+]"), "")
        val jid = if (isGroup) "${cleanDigits.trimStart('+')}@g.us" else "${cleanDigits.trimStart('+')}@s.whatsapp.net"
        ultraDao.upsertSessionContact(
            SessionContactEntity(
                jid = jid,
                displayName = displayName.ifBlank { cleanDigits },
                phoneNumber = cleanDigits,
                aboutStatus = aboutStatus,
                isGroup = isGroup,
                isAllowedForAutoReply = true
            )
        )
    }

    suspend fun setContactAutoReplyAllowed(jid: String, allowed: Boolean) {
        ultraDao.setContactAutoReplyAllowed(jid, allowed)
    }

    suspend fun recordSessionChatMessage(
        phoneNumber: String,
        contactName: String,
        messageBody: String,
        isOutgoing: Boolean,
        isAutomatedReply: Boolean = false,
        mediaType: String = "TEXT"
    ) {
        val cleanDigits = phoneNumber.replace(Regex("[^0-9+]"), "").ifBlank { "local_test" }
        val jid = "${cleanDigits.trimStart('+')}@s.whatsapp.net"
        ultraDao.insertSessionChat(
            SessionChatEntity(
                chatJid = jid,
                contactName = contactName.ifBlank { cleanDigits },
                phoneNumber = cleanDigits,
                messageBody = messageBody,
                isOutgoing = isOutgoing,
                isAutomatedReply = isAutomatedReply,
                mediaType = mediaType
            )
        )
    }

    suspend fun exportContactsToVcfFile(): File {
        val contacts = ultraDao.getSessionContactsSnapshot()
        val sb = StringBuilder()
        contacts.forEach { contact ->
            sb.appendLine("BEGIN:VCARD")
            sb.appendLine("VERSION:3.0")
            sb.appendLine("FN:${contact.displayName}")
            sb.appendLine("TEL;TYPE=CELL:${contact.phoneNumber}")
            sb.appendLine("NOTE:${contact.aboutStatus}")
            sb.appendLine("END:VCARD")
        }
        val outFile = File(secureStorage.exportsDir, "whatsapp_contacts_${System.currentTimeMillis()}.vcf")
        outFile.writeText(sb.toString(), Charsets.UTF_8)
        ultraDao.insertActivityLog(
            ActivityLogEntity(
                level = "SECURITY",
                module = "ContactsExport",
                details = "User exported ${contacts.size} session contacts to ${outFile.name}"
            )
        )
        return outFile
    }

    suspend fun exportChatsToJsonFile(): File {
        val chats = ultraDao.getSessionChatsSnapshot()
        val array = JSONArray()
        chats.forEach { msg ->
            array.put(
                JSONObject().apply {
                    put("id", msg.id)
                    put("chatJid", msg.chatJid)
                    put("contactName", msg.contactName)
                    put("phoneNumber", msg.phoneNumber)
                    put("messageBody", msg.messageBody)
                    put("direction", if (msg.isOutgoing) "OUTGOING" else "INCOMING")
                    put("automated", msg.isAutomatedReply)
                    put("mediaType", msg.mediaType)
                    put("timestamp", msg.timestamp)
                }
            )
        }
        val root = JSONObject().apply {
            put("suite", "ZAID ASHIQ X ULTRA V. CODER")
            put("exportedAt", System.currentTimeMillis())
            put("messageCount", chats.size)
            put("messages", array)
        }
        val outFile = File(secureStorage.exportsDir, "whatsapp_chats_${System.currentTimeMillis()}.json")
        outFile.writeText(root.toString(2), Charsets.UTF_8)
        ultraDao.insertActivityLog(
            ActivityLogEntity(
                level = "SECURITY",
                module = "ChatExport",
                details = "User exported ${chats.size} chat messages to JSON (${outFile.name})"
            )
        )
        return outFile
    }

    suspend fun exportChatsToTextTranscript(): File {
        val chats = ultraDao.getSessionChatsSnapshot()
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.appendLine("=== ZAID ASHIQ X ULTRA V. CODER — CHAT TRANSCRIPT ===")
        sb.appendLine("Exported: ${fmt.format(Date())}")
        sb.appendLine("Total Messages: ${chats.size}")
        sb.appendLine("=====================================================")
        chats.forEach { msg ->
            val dirTag = if (msg.isOutgoing) {
                if (msg.isAutomatedReply) "[AUTO-OUT]" else "[MANUAL-OUT]"
            } else {
                "[INCOMING]"
            }
            sb.appendLine("[${fmt.format(Date(msg.timestamp))}] $dirTag ${msg.contactName} (${msg.phoneNumber}): ${msg.messageBody}")
        }
        val outFile = File(secureStorage.exportsDir, "whatsapp_transcript_${System.currentTimeMillis()}.txt")
        outFile.writeText(sb.toString(), Charsets.UTF_8)
        ultraDao.insertActivityLog(
            ActivityLogEntity(
                level = "SECURITY",
                module = "ChatExport",
                details = "User exported ${chats.size} chat messages to text transcript (${outFile.name})"
            )
        )
        return outFile
    }
}
