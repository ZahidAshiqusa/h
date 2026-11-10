package com.zaidashiq.ultra.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.ByteBuffer
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class EncryptionManager {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "zaid_ultra_whatsapp_session_aes256"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
    }

    @Volatile
    private var fallbackKey: SecretKey? = null

    private fun getOrCreateSecretKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val existingEntry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (existingEntry != null) {
                existingEntry.secretKey
            } else {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        } catch (_: Throwable) {
            synchronized(this) {
                fallbackKey ?: run {
                    val bytes = ByteArray(32)
                    SecureRandom().nextBytes(bytes)
                    SecretKeySpec(bytes, "AES").also { fallbackKey = it }
                }
            }
        }
    }

    fun encryptToBase64(plainText: String): String {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val buffer = ByteBuffer.allocate(4 + iv.size + cipherBytes.size)
        buffer.putInt(iv.size)
        buffer.put(iv)
        buffer.put(cipherBytes)
        return Base64.encode(buffer.array())
    }

    fun decryptFromBase64(encryptedPayload: String): String {
        val raw = Base64.decode(encryptedPayload.trim())
        val buffer = ByteBuffer.wrap(raw)
        val ivLength = buffer.int
        require(ivLength in 12..16) { "Invalid AES-GCM IV length" }
        val iv = ByteArray(ivLength)
        buffer.get(iv)
        val cipherBytes = ByteArray(buffer.remaining())
        buffer.get(cipherBytes)

        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, Charsets.UTF_8)
    }

    fun verifyKeystoreOperational(): Boolean {
        return try {
            val probe = "ZAID_KEYSTORE_PROBE_${System.currentTimeMillis()}"
            val enc = encryptToBase64(probe)
            val dec = decryptFromBase64(enc)
            dec == probe
        } catch (_: Throwable) {
            false
        }
    }
}
