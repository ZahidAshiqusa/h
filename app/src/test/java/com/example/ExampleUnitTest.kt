package com.example

import com.zaidashiq.ultra.engine.EngineBridge
import com.zaidashiq.ultra.engine.SessionManager
import com.zaidashiq.ultra.engine.WhatsAppConnectionState
import com.zaidashiq.ultra.security.EncryptionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun encryptionManager_roundTripEncryptAndDecrypt_succeeds() {
        val encryptionManager = EncryptionManager()
        val original = """{"phoneNumber":"+923001234567","deviceJid":"923001234567:1@s.whatsapp.net"}"""
        val encrypted = encryptionManager.encryptToBase64(original)
        assertNotEquals(original, encrypted)
        val decrypted = encryptionManager.decryptFromBase64(encrypted)
        assertEquals(original, decrypted)
    }

    @Test
    fun sessionManager_phoneNormalizationAndPairingCodeDerivation_succeeds() {
        val normalized = SessionManager.normalizePhoneNumber("+92 (300) 123-4567")
        assertEquals("+923001234567", normalized)

        val code = SessionManager.deriveFormattedPairingCode(normalized)
        assertEquals(9, code.length)
        assertEquals('-', code[4])
    }

    @Test
    fun engineBridge_executesAllCorePrefixCommands() {
        val bridge = EngineBridge()
        val commandsToVerify = listOf(
            "help", "menu", "ping", "info", "owner",
            "time", "date", "status", "autoreply", "autoreply on", "autoreply off"
        )
        commandsToVerify.forEach { cmd ->
            val result = bridge.executeCommand(
                rawCommandName = cmd,
                rawArgs = "",
                prefix = ".",
                connectionState = WhatsAppConnectionState.CONNECTED,
                autoReplyEnabled = true,
                pairedPhone = "+923001234567",
                activePluginsCount = 4,
                messagesProcessed = 12,
                pendingTasksCount = 2,
                rateLimitPerMin = 15
            )
            assertTrue("Expected command .$cmd to be handled", result.handled)
            assertTrue("Expected non-empty reply for .$cmd", result.replyText.isNotBlank())
        }
    }
}
