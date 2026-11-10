package com.zaidashiq.ultra.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zaidashiq.ultra.ZaidApplication

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as? ZaidApplication ?: return
        val settings = app.container.settingsRepository.settingsFlow.value
        // Only start on boot if the user explicitly enabled startup behavior and has a saved session
        if (settings.startOnBootEnabled && app.container.sessionManager.hasSavedSession()) {
            WhatsAppForegroundService.startService(context)
        }
    }
}
