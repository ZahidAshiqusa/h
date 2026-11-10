package com.zaidashiq.ultra.engine

import com.zaidashiq.ultra.repositories.SessionRepository
import com.zaidashiq.ultra.security.SavedSessionCredentials
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class SessionManager(
    private val sessionRepository: SessionRepository
) {
    private val sessionLock = AtomicBoolean(false)

    fun acquireSessionLock(): Boolean = sessionLock.compareAndSet(false, true)

    fun releaseSessionLock() {
        sessionLock.set(false)
    }

    fun isSessionLocked(): Boolean = sessionLock.get()

    fun getSavedSession(): SavedSessionCredentials? = sessionRepository.getSavedSession()

    fun hasSavedSession(): Boolean = sessionRepository.hasSavedSession()

    fun generateQrPairingPayload(): String {
        val randomBytes = ByteArray(24)
        SecureRandom().nextBytes(randomBytes)
        val refToken = Base64.encode(randomBytes)
        val pubKeyBytes = ByteArray(32)
        SecureRandom().nextBytes(pubKeyBytes)
        val pubKeyB64 = Base64.encode(pubKeyBytes)
        return "2@$refToken,$pubKeyB64,${System.currentTimeMillis() / 1000}"
    }

    suspend fun completePairingAndPersist(
        phoneNumber: String,
        pairingMethod: String
    ): SavedSessionCredentials {
        val cleanPhone = normalizePhoneNumber(phoneNumber)
        val randomNoise = ByteArray(32)
        SecureRandom().nextBytes(randomNoise)
        val noiseHex = randomNoise.joinToString("") { "%02x".format(it) }

        val advKey = ByteArray(32)
        SecureRandom().nextBytes(advKey)
        val advBase64 = Base64.encode(advKey)

        val jidDigits = cleanPhone.trimStart('+')
        val creds = SavedSessionCredentials(
            phoneNumber = cleanPhone,
            deviceJid = "$jidDigits:1@s.whatsapp.net",
            pairingMethod = pairingMethod,
            noisePublicKeyHex = noiseHex,
            advSecretKeyBase64 = advBase64,
            createdAtMillis = System.currentTimeMillis(),
            lastVerifiedMillis = System.currentTimeMillis()
        )
        sessionRepository.saveAuthenticatedSession(creds)
        return creds
    }

    suspend fun clearSession() {
        sessionRepository.deleteSavedSession()
        releaseSessionLock()
    }

    companion object {
        fun normalizePhoneNumber(rawInput: String): String {
            val digits = rawInput.replace(Regex("[^0-9]"), "")
            require(digits.length in 10..15) {
                "Enter a valid international WhatsApp phone number (10–15 digits, e.g. +923001234567)."
            }
            return "+$digits"
        }

        fun deriveFormattedPairingCode(normalizedPhone: String): String {
            val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            val seed = "$normalizedPhone:${System.currentTimeMillis() / 60000}"
            val digest = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray(Charsets.UTF_8))
            val chars = CharArray(8) { i ->
                val idx = (digest[i].toInt() and 0xFF) % alphabet.length
                alphabet[idx]
            }
            val raw = String(chars)
            return "${raw.substring(0, 4)}-${raw.substring(4, 8)}"
        }
    }
}
