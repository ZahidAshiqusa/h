package com.zaidashiq.ultra.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.zaidashiq.ultra.engine.WhatsAppConnectionState

class UltraNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "zaid_ultra_whatsapp_engine_channel"
        const val NOTIFICATION_ID = 1042
    }

    private val systemNotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notif_channel_desc)
                setShowBadge(false)
            }
            systemNotificationManager.createNotificationChannel(channel)
        }
    }

    fun buildForegroundNotification(
        connectionState: WhatsAppConnectionState,
        pairedPhone: String?,
        messagesProcessed: Int
    ): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, WhatsAppForegroundService::class.java).apply {
            action = WhatsAppForegroundService.ACTION_STOP_ENGINE
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val reconnectIntent = Intent(context, WhatsAppForegroundService::class.java).apply {
            action = WhatsAppForegroundService.ACTION_RECONNECT
        }
        val reconnectPendingIntent = PendingIntent.getService(
            context,
            2,
            reconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusLabel = when (connectionState) {
            WhatsAppConnectionState.CONNECTED -> "CONNECTED • ${pairedPhone ?: "Active Session"}"
            WhatsAppConnectionState.PAIRING -> "PAIRING • Waiting for Device Link"
            WhatsAppConnectionState.CONNECTING -> "CONNECTING • Handshaking..."
            WhatsAppConnectionState.RECONNECTING -> "RECONNECTING • Exponential Backoff"
            WhatsAppConnectionState.INITIALIZING -> "INITIALIZING • Loading Local Runtime"
            WhatsAppConnectionState.AUTHENTICATION_REQUIRED -> "AUTH REQUIRED • Pair WhatsApp"
            WhatsAppConnectionState.ERROR -> "ERROR • Check Activity Logs"
            WhatsAppConnectionState.DISCONNECTED -> "STANDBY • Engine Idle"
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("ZAID ASHIQ X ULTRA V. CODER")
            .setContentText("$statusLabel • Processed: $messagesProcessed")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop Engine",
                stopPendingIntent
            )
            .addAction(
                android.R.drawable.stat_notify_sync,
                "Reconnect",
                reconnectPendingIntent
            )
            .build()
    }

    fun updateNotification(
        connectionState: WhatsAppConnectionState,
        pairedPhone: String?,
        messagesProcessed: Int
    ) {
        try {
            val notification = buildForegroundNotification(connectionState, pairedPhone, messagesProcessed)
            systemNotificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Notification permission not yet granted
        }
    }
}
