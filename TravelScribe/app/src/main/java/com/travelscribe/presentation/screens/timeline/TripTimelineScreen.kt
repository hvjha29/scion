package com.travelscribe.presentation.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelscribe.domain.model.TravelDay
import com.travelscribe.domain.model.TravelLog
import com.travelscribe.domain.model.Trip
import com.travelscribe.presentation.components.AddDayFab
import com.travelscribe.presentation.components.DayCountBadge
import com.travelscribe.presentation.components.TravelScribeBottomBar
import com.travelscribe.presentation.components.TravelScribeHeader
import com.travelscribe.presentation.theme.TravelScribeColors
import com.travelscribe.presentation.theme.TravelScribeTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Main timeline screen displaying all days in a trip (main_code.html design).
 * Features:
 * - Trip header with cover image
 * - Vertical timeline with day cards
 * - Floating "Add Next Day" button
 * - Bottom navigation
 */
@Composable
fun TripTimelineScreen(
    tripId: Long,
    onDayClick: (Long) -> Unit,
    onAddDayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onMapClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: TripTimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            TravelScribeBottomBar(
                currentRoute = "timeline",
                onTimelineClick = { /* Already on timeline */ },
                onMapClick = onMapClick,
                onProfileClick = onProfileClick,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Header
                item {
                    TravelScribeHeader(onSettingsClick = onSettingsClick)
                }

                // Trip Hero Section
                item {
                    TripHeroSection(
                        trip = uiState.trip,
                        dayCount = uiState.days.size,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // Timeline with Days
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                itemsIndexed(
                    items = uiState.days,
                    key = { _, day -> day.id }
                ) { index, day ->
                    TimelineDayCard(
                        day = day,
                        logs = uiState.logsForDay[day.id] ?: emptyList(),
                        isToday = index == 0,
                        dayNumber = uiState.days.size - index,
                        isLast = index == uiState.days.lastIndex,
                        onClick = { onDayClick(day.id) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Floating Action Button
            AddDayFab(
                onClick = onAddDayClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 84.dp)
            )
        }
    }
}

/**
 * Hero section with trip image and info.
 */
@Composable
private fun TripHeroSection(
    trip: Trip?,
    dayCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Cover Image Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(192.dp),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Placeholder background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TravelScribeTheme.extendedColors.card)
                )

                // Trip info overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = trip?.title ?: "My Trip",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trip?.title ?: "Untitled Trip",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trip metadata row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = trip?.startDate?.format(DateTimeFormatter.ofPattern("MMMM yyyy")) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TravelScribeTheme.extendedColors.textMuted,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Trip Timeline",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            DayCountBadge(count = dayCount)
        }
    }
}

/**
 * Individual day card in the timeline with connecting line.
 */
@Composable
private fun TimelineDayCard(
    day: TravelDay,
    logs: List<TravelLog>,
    isToday: Boolean,
    dayNumber: Int,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val mutedColor = TravelScribeTheme.extendedColors.textMuted

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp)
    ) {
        // Timeline connector
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Vertical dashed line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(200.dp)
                        .offset(y = 24.dp)
                        .drawBehind {
                            drawLine(
                                color = primaryColor.copy(alpha = 0.2f),
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, size.height),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(8f, 8f),
                                    0f
                                )
                            )
                        }
                )
            }

            // Timeline dot
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .size(if (isToday) 16.dp else 12.dp)
                    .background(
                        color = if (isToday) primaryColor else mutedColor.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Day card content
        Card(
            modifier = Modifier
                .weight(1f)
                .alpha(if (isToday) 1f else 0.8f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = TravelScribeTheme.extendedColors.card
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Day header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = if (isToday) "Day $dayNumber • Today" else "Day $dayNumber",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) primaryColor else mutedColor.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.date.format(DateTimeFormatter.ofPattern("MMM d")),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = mutedColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Location tags
                if (logs.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs.take(2)) { log ->
                            LocationTag(location = "Location") // Extract from log
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preview text from first log
                    logs.firstOrNull()?.let { log ->
                        Text(
                            text = log.transcribedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Image thumbnails
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Placeholder thumbnails
                        repeat(minOf(logs.size, 2)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }

                        if (logs.size > 2) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${logs.size - 2}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = mutedColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No entries yet. Tap to add your first memory!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = mutedColor,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}

/**
 * Location tag chip.
 */
@Composable
private fun LocationTag(
    location: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = location,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
