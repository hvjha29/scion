package com.travelscribe.presentation.screens.recording

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelscribe.presentation.components.LanguagePill
import com.travelscribe.presentation.components.WaveformVisualizer
import com.travelscribe.presentation.theme.TravelScribeTheme

/**
 * Recording Screen (recording_code.html design).
 * Full-screen recording interface with:
 * - Neumorphic mic button with breathing animation
 * - Language indicator pill
 * - Waveform visualizer
 * - Minimal controls (close, settings)
 */
@Composable
fun RecordingScreen(
    tripId: Long,
    dayId: Long,
    onCloseClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRecordingComplete: (String) -> Unit, // Audio file path
    viewModel: RecordingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top app bar
        RecordingTopBar(
            onCloseClick = {
                viewModel.stopRecording()
                onCloseClick()
            },
            onSettingsClick = onSettingsClick,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 24.dp, vertical = 48.dp)
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Status section
            StatusSection(
                isRecording = uiState.isRecording,
                recordingDuration = uiState.recordingDuration
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Neumorphic mic button
            NeumorphicMicButton(
                isRecording = uiState.isRecording,
                onClick = {
                    if (uiState.isRecording) {
                        viewModel.stopRecording()
                        uiState.audioFilePath?.let { path ->
                            onRecordingComplete(path)
                        }
                    } else {
                        viewModel.startRecording()
                    }
                }
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Footer controls
            FooterSection(
                isRecording = uiState.isRecording
            )
        }
    }
}

/**
 * Top bar with close and settings buttons.
 */
@Composable
private fun RecordingTopBar(
    onCloseClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(28.dp)
            )
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Status indicator section with language pill and state text.
 */
@Composable
private fun StatusSection(
    isRecording: Boolean,
    recordingDuration: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LanguagePill(languages = "Hindi / English")

        Text(
            text = if (isRecording) "Listening..." else "Tap to start",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )

        if (isRecording && recordingDuration > 0) {
            Text(
                text = formatDuration(recordingDuration),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Neumorphic-style microphone button with breathing animation.
 */
@Composable
private fun NeumorphicMicButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.1f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRecording) 1500 else 4000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )

    val breathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRecording) 1500 else 4000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingAlpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer breathing ring (blur)
        Box(
            modifier = Modifier
                .size(256.dp)
                .scale(breathingScale)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = breathingAlpha * 0.3f),
                    shape = CircleShape
                )
                .blur(24.dp)
        )

        // Middle pulse ring
        Box(
            modifier = Modifier
                .size(192.dp)
                .scale(breathingScale * 0.95f)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = breathingAlpha * 0.5f),
                    shape = CircleShape
                )
                .blur(16.dp)
        )

        // Neumorphic button
        Surface(
            modifier = Modifier.size(160.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 20.dp,
            onClick = onClick
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Inner subtle ring
                Box(
                    modifier = Modifier
                        .size(152.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.03f),
                            shape = CircleShape
                        )
                )

                // Mic icon
                Icon(
                    imageVector = MicFilledIcon,
                    contentDescription = if (isRecording) "Stop recording" else "Start recording",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

/**
 * Footer with waveform and hint text.
 */
@Composable
private fun FooterSection(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Waveform visualizer
        WaveformVisualizer(
            isActive = isRecording,
            modifier = Modifier.height(32.dp)
        )

        // Hint text
        Text(
            text = if (isRecording) "Tap to pause" else "Tap the mic to begin",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            letterSpacing = 1.sp
        )
    }
}

/**
 * Format recording duration in MM:SS format.
 */
private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Mic icon - uses custom TravelScribeIcons.
 */
private val MicFilledIcon: androidx.compose.ui.graphics.vector.ImageVector
    get() = com.travelscribe.presentation.components.TravelScribeIcons.Mic
