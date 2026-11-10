package com.zaidashiq.ultra.security

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

data class SavedSessionCredentials(
    val phoneNumber: String,
    val deviceJid: String,
    val pairingMethod: String,
    val noisePublicKeyHex: String,
    val advSecretKeyBase64: String,
    val createdAtMillis: Long,
    val lastVerifiedMillis: Long
) {
    val fingerprint: String
        get() {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest("$deviceJid:$noisePublicKeyHex".toByteArray(Charsets.UTF_8))
            return digest.take(8).joinToString(":") { "%02X".format(it) }
        }
}

class SecureStorage(
    private val context: Context,
    private val encryptionManager: EncryptionManager
) {
    val whatsappRootDir: File
        get() = File(context.filesDir, "whatsapp").apply { mkdirs() }

    val authDir: File
        get() = File(whatsappRootDir, "auth").apply { mkdirs() }

    val pluginsDir: File
        get() = File(whatsappRootDir, "plugins").apply { mkdirs() }

    val exportsDir: File
        get() = File(whatsappRootDir, "exports").apply { mkdirs() }

    val backupsDir: File
        get() = File(whatsappRootDir, "backups").apply { mkdirs() }

    val engineRuntimeDir: File
        get() = File(whatsappRootDir, "engine").apply { mkdirs() }

    private val encryptedSessionFile: File
        get() = File(authDir, "creds.json.enc")

    fun saveSessionCredentials(creds: SavedSessionCredentials) {
        val json = JSONObject().apply {
            put("phoneNumber", creds.phoneNumber)
            put("deviceJid", creds.deviceJid)
            put("pairingMethod", creds.pairingMethod)
            put("noisePublicKeyHex", creds.noisePublicKeyHex)
            put("advSecretKeyBase64", creds.advSecretKeyBase64)
            put("createdAtMillis", creds.createdAtMillis)
            put("lastVerifiedMillis", creds.lastVerifiedMillis)
        }.toString()

        val cipherText = encryptionManager.encryptToBase64(json)
        encryptedSessionFile.writeText(cipherText, Charsets.UTF_8)
    }

    fun loadSessionCredentials(): SavedSessionCredentials? {
        if (!encryptedSessionFile.exists()) return null
        return try {
            val cipherText = encryptedSessionFile.readText(Charsets.UTF_8)
            val jsonStr = encryptionManager.decryptFromBase64(cipherText)
            val obj = JSONObject(jsonStr)
            SavedSessionCredentials(
                phoneNumber = obj.optString("phoneNumber", ""),
                deviceJid = obj.optString("deviceJid", ""),
                pairingMethod = obj.optString("pairingMethod", "QR_CODE"),
                noisePublicKeyHex = obj.optString("noisePublicKeyHex", ""),
                advSecretKeyBase64 = obj.optString("advSecretKeyBase64", ""),
                createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                lastVerifiedMillis = obj.optLong("lastVerifiedMillis", System.currentTimeMillis())
            )
        } catch (_: Throwable) {
            null
        }
    }

    fun hasEncryptedSession(): Boolean = encryptedSessionFile.exists() && encryptedSessionFile.length() > 0L

    fun deleteSessionCredentials(): Boolean {
        var success = true
        authDir.listFiles()?.forEach { file ->
            if (!file.delete()) success = false
        }
        return success
    }

    fun getStorageBreakdownBytes(): Map<String, Long> {
        fun folderSize(dir: File): Long {
            if (!dir.exists()) return 0L
            return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        }
        val dbFile = context.getDatabasePath("zaid_ultra.db")
        val dbSize = if (dbFile.exists()) dbFile.length() else 0L
        return mapOf(
            "database" to dbSize,
            "auth" to folderSize(authDir),
            "plugins" to folderSize(pluginsDir),
            "exports" to folderSize(exportsDir),
            "backups" to folderSize(backupsDir),
            "engine" to folderSize(engineRuntimeDir)
        )
    }

    fun wipeAllLocalFiles() {
        whatsappRootDir.deleteRecursively()
        whatsappRootDir.mkdirs()
    }
}
