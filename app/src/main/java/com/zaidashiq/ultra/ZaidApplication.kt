package com.zaidashiq.ultra

import android.app.Application
import android.content.Context
import com.zaidashiq.ultra.database.AppDatabase
import com.zaidashiq.ultra.database.UltraDao
import com.zaidashiq.ultra.engine.ConnectionMonitor
import com.zaidashiq.ultra.engine.EngineBridge
import com.zaidashiq.ultra.engine.NodeRuntimeManager
import com.zaidashiq.ultra.engine.SessionManager
import com.zaidashiq.ultra.engine.WhatsAppEngineManager
import com.zaidashiq.ultra.repositories.AutomationRuleRepository
import com.zaidashiq.ultra.repositories.CommandRepository
import com.zaidashiq.ultra.repositories.SessionRepository
import com.zaidashiq.ultra.repositories.SettingsRepository
import com.zaidashiq.ultra.security.EncryptionManager
import com.zaidashiq.ultra.security.SecureStorage
import com.zaidashiq.ultra.services.UltraNotificationHelper

class AppContainer(context: Context) {
    val encryptionManager = EncryptionManager()
    val secureStorage = SecureStorage(context, encryptionManager)
    val database: AppDatabase = AppDatabase.getInstance(context)
    val ultraDao: UltraDao = database.ultraDao()

    val settingsRepository = SettingsRepository(context, encryptionManager)
    val sessionRepository = SessionRepository(ultraDao, secureStorage)
    val commandRepository = CommandRepository(context, ultraDao, secureStorage)
    val automationRuleRepository = AutomationRuleRepository(ultraDao)

    val nodeRuntimeManager = NodeRuntimeManager(context, secureStorage, encryptionManager)
    val sessionManager = SessionManager(sessionRepository)
    val connectionMonitor = ConnectionMonitor(context)
    val engineBridge = EngineBridge()
    val notificationHelper = UltraNotificationHelper(context)

    val engineManager = WhatsAppEngineManager(
        nodeRuntimeManager = nodeRuntimeManager,
        sessionManager = sessionManager,
        connectionMonitor = connectionMonitor,
        engineBridge = engineBridge,
        settingsRepository = settingsRepository,
        sessionRepository = sessionRepository,
        commandRepository = commandRepository,
        ultraDao = ultraDao
    )
}

class ZaidApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
