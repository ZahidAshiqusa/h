package com.zaidashiq.ultra.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.zaidashiq.ultra.ZaidApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WhatsAppForegroundService : Service() {

    companion object {
        const val ACTION_START_ENGINE = "com.zaidashiq.ultra.action.START_ENGINE"
        const val ACTION_STOP_ENGINE = "com.zaidashiq.ultra.action.STOP_ENGINE"
        const val ACTION_RECONNECT = "com.zaidashiq.ultra.action.RECONNECT"

        fun startService(context: Context) {
            val intent = Intent(context, WhatsAppForegroundService::class.java).apply {
                action = ACTION_START_ENGINE
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (_: Throwable) {
                // Fallback if started in unit test context
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, WhatsAppForegroundService::class.java).apply {
                action = ACTION_STOP_ENGINE
            }
            try {
                context.startService(intent)
            } catch (_: Throwable) {
            }
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var stateObserverJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as? ZaidApplication ?: return START_NOT_STICKY
        val engineManager = app.container.engineManager
        val settingsRepository = app.container.settingsRepository
        val notificationHelper = app.container.notificationHelper

        when (intent?.action) {
            ACTION_STOP_ENGINE -> {
                engineManager.stopEngine()
                releaseWakeLockSafely()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_RECONNECT -> {
                engineManager.triggerExponentialReconnect("Manual reconnect action from notification")
            }

            else -> {
                val snapshot = engineManager.snapshot.value
                val settings = settingsRepository.settingsFlow.value
                val notification = notificationHelper.buildForegroundNotification(
                    connectionState = snapshot.connectionState,
                    pairedPhone = snapshot.savedSession?.phoneNumber,
                    messagesProcessed = settings.totalMessagesProcessed
                )
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(
                            UltraNotificationHelper.NOTIFICATION_ID,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                        )
                    } else {
                        startForeground(UltraNotificationHelper.NOTIFICATION_ID, notification)
                    }
                } catch (_: Throwable) {
                }

                acquireWakeLockSafely()
                engineManager.startEngine()
                observeEngineStateForNotification(app)
            }
        }

        return START_STICKY
    }

    private fun observeEngineStateForNotification(app: ZaidApplication) {
        stateObserverJob?.cancel()
        stateObserverJob = serviceScope.launch {
            combine(
                app.container.engineManager.snapshot,
                app.container.settingsRepository.settingsFlow
            ) { snap, settings ->
                Pair(snap, settings)
            }.collect { (snap, settings) ->
                if (settings.notificationsEnabled) {
                    app.container.notificationHelper.updateNotification(
                        connectionState = snap.connectionState,
                        pairedPhone = snap.savedSession?.phoneNumber,
                        messagesProcessed = settings.totalMessagesProcessed
                    )
                }
            }
        }
    }

    private fun acquireWakeLockSafely() {
        if (wakeLock?.isHeld == true) return
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "ZaidUltra::WhatsAppEngineWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(30 * 60 * 1000L) // 30-minute bounded safety lease
            }
        } catch (_: Throwable) {
        }
    }

    private fun releaseWakeLockSafely() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Throwable) {
        }
        wakeLock = null
    }

    override fun onDestroy() {
        stateObserverJob?.cancel()
        serviceScope.cancel()
        releaseWakeLockSafely()
        super.onDestroy()
    }
}
