package com.zaidashiq.ultra.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.DeepSpaceBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassCardHighlight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianNavy
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.zaidashiq.ultra.ui.chats.ChatExportScreen
import com.zaidashiq.ultra.ui.chats.ContactsManagerScreen
import com.zaidashiq.ultra.ui.chats.MessageCenterScreen
import com.zaidashiq.ultra.ui.commands.AutomationScreen
import com.zaidashiq.ultra.ui.commands.CommandLibraryScreen
import com.zaidashiq.ultra.ui.commands.MultilingualResponderScreen
import com.zaidashiq.ultra.ui.commands.PluginManagerScreen
import com.zaidashiq.ultra.ui.components.BannerNotificationBar
import com.zaidashiq.ultra.ui.dashboard.FirstLaunchSplashScreen
import com.zaidashiq.ultra.ui.dashboard.MainDashboardScreen
import com.zaidashiq.ultra.ui.pairing.PairingScreen
import com.zaidashiq.ultra.ui.settings.ActivityLogsScreen
import com.zaidashiq.ultra.ui.settings.SettingsScreen
import com.zaidashiq.ultra.ui.status.SchedulerScreen
import com.zaidashiq.ultra.ui.status.StatusPublisherScreen

@Composable
fun UltraAppRoot(viewModel: UltraViewModel) {
    val context = LocalContext.current
    val showSplash by viewModel.showSplash.collectAsStateWithLifecycle()
    val currentModule by viewModel.currentModule.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()
    val snapshot by viewModel.engineSnapshot.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val automationRules by viewModel.automationRules.collectAsStateWithLifecycle()
    val commands by viewModel.commands.collectAsStateWithLifecycle()
    val commandUsageLogs by viewModel.commandUsageLogs.collectAsStateWithLifecycle()
    val plugins by viewModel.plugins.collectAsStateWithLifecycle()
    val scheduledTasks by viewModel.scheduledTasks.collectAsStateWithLifecycle()
    val sessionContacts by viewModel.sessionContacts.collectAsStateWithLifecycle()
    val sessionChats by viewModel.sessionChats.collectAsStateWithLifecycle()
    val statusPosts by viewModel.statusPosts.collectAsStateWithLifecycle()
    val messageTemplates by viewModel.messageTemplates.collectAsStateWithLifecycle()
    val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()
    val lastCommandOutput by viewModel.lastCommandTestOutput.collectAsStateWithLifecycle()
    val lastExportPath by viewModel.lastExportedFilePath.collectAsStateWithLifecycle()
    val storageBreakdown by viewModel.storageBreakdown.collectAsStateWithLifecycle()

    var showNotificationRationale by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.startForegroundEngine(context)
    }

    fun handleStartEngineWithPermissionCheck(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                showNotificationRationale = true
                return
            }
        }
        viewModel.startForegroundEngine(ctx)
    }

    MyApplicationTheme(accentCyan = settings.accentCyan) {
        if (showNotificationRationale) {
            AlertDialog(
                onDismissRequest = { showNotificationRationale = false },
                title = { Text("Foreground Engine Notification Permission") },
                text = {
                    Text(
                        "ZAID ASHIQ X ULTRA V. CODER uses a persistent Android Foreground Service notification to keep the local WhatsApp automation engine active while the app is minimized."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showNotificationRationale = false
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.startForegroundEngine(context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianNavy
                        )
                    ) {
                        Text("Allow & Start Engine")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showNotificationRationale = false
                            viewModel.startForegroundEngine(context)
                        }
                    ) {
                        Text("Start Without Notification")
                    }
                }
            )
        }

        if (showSplash) {
            FirstLaunchSplashScreen(
                onEnterDashboard = { viewModel.dismissSplash() }
            )
            return@MyApplicationTheme
        }

        // System BackHandler for all secondary screens
        if (currentModule != UltraModule.DASHBOARD) {
            BackHandler {
                viewModel.navigateBackToDashboard()
            }
        }

        val navTabs = listOf(
            Triple(UltraModule.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
            Triple(UltraModule.PAIRING, "Pairing", Icons.Default.QrCodeScanner),
            Triple(UltraModule.AUTOMATION, "Automation", Icons.Default.AutoAwesome),
            Triple(UltraModule.COMMAND_LIBRARY, "Commands", Icons.Default.Code),
            Triple(UltraModule.SETTINGS, "Settings", Icons.Default.Settings)
        )

        val pendingScheduledCount = scheduledTasks.count { it.status == "PENDING" }
        val enabledPluginsCount = plugins.count { it.isEnabled && it.isValidated }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianNavy)
        ) {
            val isExpandedScreen = maxWidth >= 640.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = ObsidianNavy,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (!isExpandedScreen) {
                        NavigationBar(
                            containerColor = DeepSpaceBlue,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            navTabs.forEach { (module, label, icon) ->
                                NavigationBarItem(
                                    selected = currentModule == module,
                                    onClick = { viewModel.navigateTo(module) },
                                    modifier = Modifier.testTag("bottom_nav_${label.lowercase()}"),
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ObsidianNavy,
                                        selectedTextColor = ElectricCyan,
                                        indicatorColor = ElectricCyan,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpandedScreen) {
                        NavigationRail(
                            containerColor = GlassCardHighlight,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            navTabs.forEach { (module, label, icon) ->
                                NavigationRailItem(
                                    selected = currentModule == module,
                                    onClick = { viewModel.navigateTo(module) },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label, color = TextPrimary) }
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        BannerNotificationBar(
                            message = bannerMessage,
                            onDismiss = { viewModel.clearBanner() }
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            when (currentModule) {
                                UltraModule.DASHBOARD -> MainDashboardScreen(
                                    context = context,
                                    snapshot = snapshot,
                                    settings = settings,
                                    activeScheduledTasksCount = pendingScheduledCount,
                                    enabledPluginsCount = enabledPluginsCount,
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onStartEngine = { handleStartEngineWithPermissionCheck(it) },
                                    onStopEngine = { viewModel.stopForegroundEngine(it) }
                                )

                                UltraModule.PAIRING -> PairingScreen(
                                    snapshot = snapshot,
                                    onBack = { viewModel.navigateBackToDashboard() },
                                    onGenerateQr = { viewModel.startQrPairing() },
                                    onRequestPairingCode = { viewModel.requestPairingCode(it) },
                                    onCompletePairingHandshake = { phone, method ->
                                        viewModel.completePairingHandshake(phone, method)
                                    },
                                    onDisconnect = { viewModel.disconnectSession(it) },
                                    onDeleteSession = { viewModel.deleteSessionPermanently(it) },
                                    onRefreshPoC = { viewModel.refreshPoCVerification() },
                                    onShowBanner = { viewModel.showBanner(it) }
                                )

                                UltraModule.AUTOMATION -> AutomationScreen(
                                    connectionState = snapshot.connectionState,
                                    settings = settings,
                                    rules = automationRules,
                                    onBack = { viewModel.navigateBackToDashboard() },
                                    onUpdateSettings = { viewModel.updateSettings(it) },
                                    onAddRule = { t, kw, m, resp, lang, scope, filter, delay, cd ->
                                        viewModel.addAutomationRule(t, kw, m, resp, lang, scope, filter, delay, cd)
                                    },
                                    onToggleRule = { id, en -> viewModel.toggleAutomationRule(id, en) },
                                    onDeleteRule = { viewModel.deleteAutomationRule(it) }
                                )

                                UltraModule.MESSAGE_CENTER -> MessageCenterScreen(
                                    connectionState = snapshot.connectionState,
                                    templates = messageTemplates,
                                    recentChats = sessionChats,
                                    onBack = { viewModel.navigateBackToDashboard() },
                                    onSendManualMessage = { p, n, b -> viewModel.sendManualMessage(p, n, b) },
                                    onSimulateIncomingMessage = { p, n, m, g ->
                                        viewModel.simulateIncomingWhatsAppMessage(p, n, m, g)
                                    },
                                    onSaveTemplate = { t, l, b, c -> viewModel.saveMessageTemplate(t, l, b, c) },
                                    onDeleteTemplate = { viewModel.deleteMessageTemplate(it) }
                                )

                                UltraModule.CONTACTS -> ContactsManagerScreen(
                                    connectionState = snapshot.connectionState,
                                    contacts = sessionContacts,
                                    lastExportPath = lastExportPath,
                                    onBack = { viewModel.navigateBackToDashboard() },
                                    onAddContact = { p, n, a -> viewModel.addSessionContact(p, n, a) },
                                    onToggleContactAutoReply = { jid, al -> viewModel.toggleContactAutoReply(jid, al) },
                                    onExportVcf = { viewModel.exportContactsToVcf() }
                                )

                                UltraModule.CHAT_EXPORT -> ChatExportScreen(
                                    connectionState = snapshot.connectionState,
                                    chats = sessionChats,
                                    lastExportPath = lastExportPath,
                                    onBack = { viewModel.navigateBackToDashboard() },
                                    onExportJson = { viewModel.exportChatsToJson() },
                                    onExportTranscript = { viewModel.exportChatsToTranscript() }
                                )

                                UltraModule.STATUS_PUBLISHER -> StatusPublisherScreen(
                                    connectionState = snapshot.connectionState,
                                    statusPosts = statusPosts,
                                    onBack = { viewModel.navigateBackToDashboard() },
                                    onPublishStatus = { type, uri, cap -> viewModel.publishStatus(type, uri, cap) }
                                )

                                UltraModule.MULTILINGUAL_RESPONDER -> MultilingualResponderScreen(
                                    connectionState = snapshot.connectionState,
                                    rules = automationRules,
                                    onBack = { viewModel.navigateBackToDashboard() },
                                    onSimulateMessage = { p, n, m ->
                                        viewModel.simulateIncomingWhatsAppMessage(p, n, m, false)
                                    }
                                )

                                UltraModule.SCHEDULER -> SchedulerScreen(
                                    connectionState = snapshot.connectionState,
                                    tasks = scheduledTasks,
                                    onBack = { viewModel.navigateBackToDashboard() },
                                    onScheduleTask = { ctx, p, n, m, mins ->
                                        viewModel.scheduleMessage(ctx, p, n, m, mins)
                                    },
                                    onExecuteImmediately = { viewModel.executeScheduledTaskImmediately(it) },
                                    onCancelTask = { ctx, id -> viewModel.cancelScheduledTask(ctx, id) },
                                    onDeleteTask = { ctx, id -> viewModel.deleteScheduledTask(ctx, id) }
                                )

                                UltraModule.COMMAND_LIBRARY -> CommandLibraryScreen(
                                    connectionState = snapshot.connectionState,
                                    settings = settings,
                                    commands = commands,
                                    commandUsageLogs = commandUsageLogs,
                                    lastTestOutput = lastCommandOutput,
                                    onBack = { viewModel.navigateBackToDashboard() },
                                    onUpdatePrefix = { pfx ->
                                        viewModel.updateSettings { it.copy(commandPrefix = pfx) }
                                    },
                                    onToggleCommand = { name, en -> viewModel.toggleCommandEnabled(name, en) },
                                    onRunCommandTest = { viewModel.testCommandInSandbox(it) },
                                    onClearCommandUsageLogs = { viewModel.clearCommandUsageLogs() }
                                )

                                UltraModule.PLUGIN_MANAGER -> PluginManagerScreen(
                                    connectionState = snapshot.connectionState,
                                    plugins = plugins,
                                    onBack = { viewModel.navigateBackToDashboard() },
                                    onTogglePlugin = { id, en -> viewModel.togglePluginEnabled(id, en) },
                                    onImportPlugin = { fn, code -> viewModel.importLocalJsPlugin(fn, code) },
                                    onDeletePlugin = { viewModel.deletePlugin(it) }
                                )

                                UltraModule.ACTIVITY_LOGS -> ActivityLogsScreen(
                                    connectionState = snapshot.connectionState,
                                    logs = activityLogs,
                                    onBack = { viewModel.navigateBackToDashboard() },
                                    onClearLogs = { viewModel.clearActivityLogs() }
                                )

                                UltraModule.SETTINGS -> SettingsScreen(
                                    snapshot = snapshot,
                                    settings = settings,
                                    storageBreakdown = storageBreakdown,
                                    onBack = { viewModel.navigateBackToDashboard() },
                                    onUpdateSettings = { viewModel.updateSettings(it) },
                                    onDisconnectSession = { viewModel.disconnectSession(it) },
                                    onDeleteSession = { viewModel.deleteSessionPermanently(it) },
                                    onClearLogs = { viewModel.clearActivityLogs() },
                                    onExportSettingsBackup = { viewModel.exportEncryptedSettingsBackup() },
                                    onImportSettingsBackup = { viewModel.importEncryptedSettingsBackup(it) },
                                    onWipeAllLocalData = { viewModel.wipeAllLocalData(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
