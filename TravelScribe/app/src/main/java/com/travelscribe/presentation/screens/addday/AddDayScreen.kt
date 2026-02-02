package com.travelscribe.presentation.screens.addday

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelscribe.presentation.components.RecordButton
import com.travelscribe.presentation.components.TravelScribeTopBar
import com.travelscribe.presentation.theme.TravelScribeTheme

/**
 * Add Day Screen (single_code.html design).
 * Shows a paper-like content area with blurred preview text
 * and a prominent record button at the bottom.
 * 
 * Used when adding a new entry to a day.
 */
@Composable
fun AddDayScreen(
    tripId: Long,
    dayId: Long,
    onBackClick: () -> Unit,
    onRecordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TravelScribeTopBar(
                title = "Add Details",
                onBackClick = onBackClick,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Headline
                Text(
                    text = "What happened next?",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Paper-like content area with blurred preview
                PaperContentArea(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            // Bottom action area with record button
            BottomRecordSection(
                onRecordClick = onRecordClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Paper-textured content area with blurred sample text.
 */
@Composable
private fun PaperContentArea(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top fade gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                            )
                        )
                    )
                    .align(Alignment.TopCenter)
            )

            // Blurred sample content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .alpha(0.5f)
                    .blur(0.5.dp)
            ) {
                Text(
                    text = "The morning light hit the Piazza San Marco just as we arrived. The pigeons were already gathering, a fluttering grey cloud against the ancient stones.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 28.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "We stopped for an espresso at Caffe Florian. The velvet seats felt like stepping back into the 18th century. The waiter, dressed in a crisp white jacket...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 28.sp
                )
            }

            // Bottom fade gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Bottom section with record button and helper text.
 */
@Composable
private fun BottomRecordSection(
    onRecordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(192.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background.copy(alpha = 0f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tap to talk hint with bounce animation
            TapToTalkHint()

            // Record button
            RecordButton(
                onClick = onRecordClick,
                isRecording = false,
                size = 80.dp
            )

            // Auto-save indicator
            Text(
                text = "Auto-saving to journal",
                style = MaterialTheme.typography.labelSmall,
                color = TravelScribeTheme.extendedColors.textMuted.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Animated "Tap to talk" hint pill.
 */
@Composable
private fun TapToTalkHint(
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Text(
            text = "Tap to talk",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = TravelScribeTheme.extendedColors.textMuted,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            letterSpacing = 0.5.sp
        )
    }
}
