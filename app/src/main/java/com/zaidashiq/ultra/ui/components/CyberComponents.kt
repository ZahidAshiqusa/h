package com.zaidashiq.ultra.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassCardHighlight
import com.example.ui.theme.GlassCardSurface
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.ObsidianNavy
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusPairing
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.zaidashiq.ultra.engine.WhatsAppConnectionState
import java.security.MessageDigest

@Composable
fun CyberGlassCard(
    modifier: Modifier = Modifier,
    accentBorderColor: Color = GlassBorderCyan,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val cardModifier = modifier
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    GlassCardHighlight.copy(alpha = 0.88f),
                    GlassCardSurface.copy(alpha = 0.94f)
                )
            )
        )
        .border(
            BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentBorderColor.copy(alpha = 0.7f),
                        NeonViolet.copy(alpha = 0.25f)
                    )
                )
            ),
            shape = shape
        )
        .let { base ->
            if (onClick != null) base.clickable { onClick() } else base
        }
        .padding(16.dp)

    Column(
        modifier = cardModifier,
        content = content
    )
}

@Composable
fun ConnectionStatusBadge(
    state: WhatsAppConnectionState,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (state) {
        WhatsAppConnectionState.CONNECTED -> StatusConnected to "CONNECTED"
        WhatsAppConnectionState.PAIRING -> StatusPairing to "PAIRING"
        WhatsAppConnectionState.CONNECTING -> StatusInfo to "CONNECTING"
        WhatsAppConnectionState.RECONNECTING -> StatusPairing to "RECONNECTING"
        WhatsAppConnectionState.INITIALIZING -> StatusInfo to "INITIALIZING"
        WhatsAppConnectionState.AUTHENTICATION_REQUIRED -> StatusPairing to "AUTH REQUIRED"
        WhatsAppConnectionState.ERROR -> StatusError to "ERROR"
        WhatsAppConnectionState.DISCONNECTED -> TextSecondary to "DISCONNECTED"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.65f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ModuleTopBar(
    title: String,
    subtitle: String,
    connectionState: WhatsAppConnectionState,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(GlassCardHighlight)
                .border(1.dp, GlassBorderCyan, RoundedCornerShape(14.dp))
                .testTag("back_to_dashboard_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to Dashboard",
                tint = ElectricCyan
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        ConnectionStatusBadge(state = connectionState)
    }
}

@Composable
fun TelemetryStatTile(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    CyberGlassCard(
        modifier = modifier,
        accentBorderColor = accentColor.copy(alpha = 0.55f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BannerNotificationBar(
    message: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = !message.isNullOrBlank(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0E293C),
            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.75f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss notification",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QrCodeMatrixCanvas(
    payload: String,
    modifier: Modifier = Modifier
) {
    val hash = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
    val gridSize = 25

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(3.dp, ElectricCyan, RoundedCornerShape(18.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(210.dp)) {
            val cellSize = size.width / gridSize

            fun isFinderPattern(r: Int, c: Int): Boolean {
                val topLeft = r in 0..6 && c in 0..6
                val topRight = r in 0..6 && c in (gridSize - 7) until gridSize
                val bottomLeft = r in (gridSize - 7) until gridSize && c in 0..6
                return topLeft || topRight || bottomLeft
            }

            fun drawFinderSquare(startRow: Int, startCol: Int) {
                for (r in 0..6) {
                    for (c in 0..6) {
                        val isOuter = r == 0 || r == 6 || c == 0 || c == 6
                        val isInner = r in 2..4 && c in 2..4
                        if (isOuter || isInner) {
                            drawRect(
                                color = ObsidianNavy,
                                topLeft = Offset((startCol + c) * cellSize, (startRow + r) * cellSize),
                                size = Size(cellSize * 0.96f, cellSize * 0.96f)
                            )
                        }
                    }
                }
            }

            drawFinderSquare(0, 0)
            drawFinderSquare(0, gridSize - 7)
            drawFinderSquare(gridSize - 7, 0)

            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    if (isFinderPattern(r, c)) continue
                    val bitIndex = (r * gridSize + c) % (hash.size * 8)
                    val byteVal = hash[bitIndex / 8].toInt() and 0xFF
                    val bitOn = ((byteVal shr (bitIndex % 8)) and 1) == 1
                    if (bitOn) {
                        drawRect(
                            color = ObsidianNavy,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize * 0.92f, cellSize * 0.92f)
                        )
                    }
                }
            }
        }
    }
}
