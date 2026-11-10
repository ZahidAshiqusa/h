package com.zaidashiq.ultra.ui.pairing

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassCardHighlight
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.ObsidianNavy
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusPairing
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.zaidashiq.ultra.engine.EngineRuntimeSnapshot
import com.zaidashiq.ultra.ui.components.CyberGlassCard
import com.zaidashiq.ultra.ui.components.ModuleTopBar
import com.zaidashiq.ultra.ui.components.QrCodeMatrixCanvas
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PairingScreen(
    snapshot: EngineRuntimeSnapshot,
    onBack: () -> Unit,
    onGenerateQr: () -> Unit,
    onRequestPairingCode: (String) -> Unit,
    onCompletePairingHandshake: (String, String) -> Unit,
    onDisconnect: (Context) -> Unit,
    onDeleteSession: (Context) -> Unit,
    onRefreshPoC: () -> Unit,
    onShowBanner: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var phoneInput by rememberSaveable { mutableStateOf("+923001234567") }
    var qrPhoneLabel by rememberSaveable { mutableStateOf("+923001234567") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Encrypted Session?") },
            text = {
                Text(
                    "This permanently deletes the Android Keystore-protected WhatsApp credentials from local storage. You will need to re-pair via QR or Pairing Code."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteSession(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = "WhatsApp Device Pairing",
            subtitle = "QR Code, 8-Char Pairing Code & Keystore Session",
            connectionState = snapshot.connectionState,
            onBack = onBack
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = GlassCardHighlight,
            contentColor = ElectricCyan,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.testTag("tab_qr_pairing"),
                text = { Text("A. QR Code") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.testTag("tab_code_pairing"),
                text = { Text("B. Pairing Code") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                modifier = Modifier.testTag("tab_session_poc"),
                text = { Text("C. Session & PoC") }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!snapshot.lastErrorText.isNullOrBlank()) {
                item {
                    CyberGlassCard(accentBorderColor = StatusError) {
                        Text(
                            text = "Engine Alert: ${snapshot.lastErrorText}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StatusError
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    item {
                        CyberGlassCard(accentBorderColor = ElectricCyan) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCode2, contentDescription = null, tint = ElectricCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "QR CODE AUTHENTICATION",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. Open WhatsApp on your primary phone.\n2. Tap Menu (⋮) or Settings → Linked Devices.\n3. Tap Link a Device and scan the QR code below.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (snapshot.activeQrPayload != null) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    QrCodeMatrixCanvas(payload = snapshot.activeQrPayload)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "QR Token Auto-Refreshes in ${snapshot.qrSecondsRemaining}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusPairing
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = qrPhoneLabel,
                                        onValueChange = { qrPhoneLabel = it },
                                        label = { Text("Session Phone Identifier (E.164)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { onCompletePairingHandshake(qrPhoneLabel, "QR_CODE") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("confirm_qr_scan_button"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = StatusConnected,
                                            contentColor = ObsidianNavy
                                        )
                                    ) {
                                        Icon(Icons.Default.Link, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("COMPLETE QR HANDSHAKE & SAVE SESSION")
                                    }
                                }
                            } else {
                                Button(
                                    onClick = onGenerateQr,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("generate_qr_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricCyan,
                                        contentColor = ObsidianNavy
                                    )
                                ) {
                                    Icon(Icons.Default.QrCode2, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("INITIALIZE ENGINE & GENERATE QR CODE")
                                }
                            }
                        }
                    }
                }

                1 -> {
                    item {
                        CyberGlassCard(accentBorderColor = NeonViolet) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhonelinkSetup, contentDescription = null, tint = NeonViolet)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "8-CHARACTER PAIRING CODE",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Pair on a single phone without scanning a QR code:\n1. Enter your WhatsApp number in international format (e.g. +923001234567).\n2. Tap Request Pairing Code.\n3. In WhatsApp → Linked Devices → Link with phone number instead, enter the 8-character code.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("WhatsApp Phone Number (International)") },
                                placeholder = { Text("+923001234567") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pairing_phone_input")
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { onRequestPairingCode(phoneInput) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("request_pairing_code_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan,
                                    contentColor = ObsidianNavy
                                )
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("REQUEST PAIRING CODE")
                            }

                            if (snapshot.activePairingCode != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(ObsidianNavy)
                                        .border(1.5.dp, ElectricCyan, RoundedCornerShape(16.dp))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = snapshot.activePairingCode,
                                            style = MaterialTheme.typography.displayLarge,
                                            color = ElectricCyan,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.testTag("generated_pairing_code_text")
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Target: ${snapshot.pairingPhoneTarget} • Expires in ${snapshot.pairingSecondsRemaining}s",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusPairing
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            cm?.setPrimaryClip(ClipData.newPlainText("WhatsApp Pairing Code", snapshot.activePairingCode))
                                            onShowBanner("Copied pairing code ${snapshot.activePairingCode}")
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("COPY CODE")
                                    }
                                    Button(
                                        onClick = { onCompletePairingHandshake(phoneInput, "PAIRING_CODE") },
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(48.dp)
                                            .testTag("confirm_pairing_code_button"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = StatusConnected,
                                            contentColor = ObsidianNavy
                                        )
                                    ) {
                                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("CONFIRM LINKED")
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    item {
                        val saved = snapshot.savedSession
                        CyberGlassCard(
                            accentBorderColor = if (saved != null) StatusConnected else StatusPairing
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = if (saved != null) StatusConnected else StatusPairing
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ENCRYPTED LOCAL SESSION MANAGEMENT",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            if (saved != null) {
                                val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                                Text("Paired Phone: ${saved.phoneNumber}", color = TextPrimary)
                                Text("Device JID: ${saved.deviceJid}", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                Text("Auth Method: ${saved.pairingMethod}", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                Text("Keystore Fingerprint: ${saved.fingerprint}", color = ElectricCyan, style = MaterialTheme.typography.labelSmall)
                                Text("Created At: ${fmt.format(Date(saved.createdAtMillis))}", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Note: Sessions are protected using Android Keystore AES-256-GCM encryption. Linked sessions can still be revoked at any time from your primary phone's WhatsApp Linked Devices screen.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onDisconnect(context) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("disconnect_session_button")
                                    ) {
                                        Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("DISCONNECT")
                                    }
                                    Button(
                                        onClick = { showDeleteConfirm = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("delete_session_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                                    ) {
                                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("DELETE SESSION")
                                    }
                                }
                            } else {
                                Text(
                                    text = "No active WhatsApp session saved. Switch to Tab A (QR Code) or Tab B (Pairing Code) to link your device.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Embedded Runtime Proof-of-Concept Verifier Card
                    item {
                        val poc = snapshot.pocReport
                        CyberGlassCard(accentBorderColor = ElectricCyan) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RUNTIME PROOF-OF-CONCEPT VERIFIER",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(
                                    onClick = onRefreshPoC,
                                    modifier = Modifier.testTag("reverify_poc_button")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RE-VERIFY")
                                }
                            }
                            if (poc != null) {
                                Text(
                                    text = "Active Mode: ${poc.runtimeMode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusConnected
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = poc.architectureNote,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                poc.checks.forEach { check ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (check.passed) StatusConnected else StatusError,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = check.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = check.detail,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
