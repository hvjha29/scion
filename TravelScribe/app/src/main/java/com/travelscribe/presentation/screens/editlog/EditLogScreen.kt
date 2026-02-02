package com.travelscribe.presentation.screens.editlog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelscribe.domain.model.Expense
import com.travelscribe.presentation.components.AddDetailChip
import com.travelscribe.presentation.components.DetailChip
import com.travelscribe.presentation.components.PrimaryButton
import com.travelscribe.presentation.components.TravelScribeTopBar
import com.travelscribe.presentation.theme.TravelScribeTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Edit Log Screen (edit_code.html design).
 * Review and edit transcribed entry with:
 * - Editable title and body text
 * - Horizontally scrollable extracted details chips
 * - Save button at bottom
 */
@Composable
fun EditLogScreen(
    logId: Long,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    viewModel: EditLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TravelScribeTopBar(
                title = "Review Entry",
                onBackClick = onBackClick,
                actionIcon = Icons.Default.Share,
                onActionClick = onShareClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Paper sheet area
                PaperEditArea(
                    title = uiState.title,
                    onTitleChange = viewModel::updateTitle,
                    body = uiState.body,
                    onBodyChange = viewModel::updateBody,
                    timestamp = uiState.timestamp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )

                // Extracted details section
                ExtractedDetailsSection(
                    expenses = uiState.expenses,
                    location = uiState.location,
                    date = uiState.date,
                    category = uiState.category,
                    onExpenseClick = { /* Edit expense */ },
                    onLocationClick = { /* Edit location */ },
                    onDateClick = { /* Edit date */ },
                    onCategoryClick = { /* Edit category */ },
                    onAddClick = { /* Add new detail */ },
                    modifier = Modifier.padding(bottom = 100.dp)
                )
            }

            // Bottom save button
            BottomSaveBar(
                onSaveClick = {
                    viewModel.saveLog()
                    onSaveClick()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Paper-textured editable content area.
 */
@Composable
private fun PaperEditArea(
    title: String,
    onTitleChange: (String) -> Unit,
    body: String,
    onBodyChange: (String) -> Unit,
    timestamp: LocalDateTime?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Title input
        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            textStyle = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                if (title.isEmpty()) {
                    Text(
                        text = "Title your story...",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TravelScribeTheme.extendedColors.textMuted.copy(alpha = 0.5f),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                innerTextField()
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Timestamp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = TravelScribeTheme.extendedColors.textMuted,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = timestamp?.format(DateTimeFormatter.ofPattern("'Today,' h:mm a")) 
                    ?: "Just now",
                style = MaterialTheme.typography.bodySmall,
                color = TravelScribeTheme.extendedColors.textMuted,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Body text input
        BasicTextField(
            value = body,
            onValueChange = onBodyChange,
            textStyle = TextStyle(
                fontSize = 18.sp,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                if (body.isEmpty()) {
                    Text(
                        text = "Start writing or dictating your travel memories...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TravelScribeTheme.extendedColors.textMuted.copy(alpha = 0.5f),
                        lineHeight = 28.sp
                    )
                }
                innerTextField()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}

/**
 * Extracted details section with horizontal scroll.
 */
@Composable
private fun ExtractedDetailsSection(
    expenses: List<Expense>,
    location: String?,
    date: String?,
    category: String?,
    onExpenseClick: (Expense) -> Unit,
    onLocationClick: () -> Unit,
    onDateClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = AutoAwesomeIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "EXTRACTED DETAILS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal scroll rail
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location chip
            location?.let {
                DetailChip(
                    label = "Location",
                    value = it.split(",").firstOrNull() ?: it,
                    subtitle = it.split(",").drop(1).joinToString(",").trim().takeIf { s -> s.isNotEmpty() },
                    icon = Icons.Default.LocationOn,
                    onClick = onLocationClick
                )
            }

            // Cost chip (from expenses)
            expenses.firstOrNull()?.let { expense ->
                DetailChip(
                    label = "Cost",
                    value = "${expense.currency} ${expense.amount}",
                    subtitle = "Total Bill",
                    icon = PaymentIcon,
                    onClick = { onExpenseClick(expense) }
                )
            }

            // Date chip
            date?.let {
                DetailChip(
                    label = "Date",
                    value = it.split(" ").firstOrNull() ?: it,
                    subtitle = it.split(" ").drop(1).joinToString(" "),
                    icon = Icons.Default.DateRange,
                    onClick = onDateClick
                )
            }

            // Category chip
            category?.let {
                DetailChip(
                    label = "Tag",
                    value = it,
                    subtitle = "Food & Drink", // Category group
                    icon = RestaurantIcon,
                    onClick = onCategoryClick
                )
            }

            // Add new chip
            AddDetailChip(onClick = onAddClick)
        }
    }
}

/**
 * Bottom save button with gradient background.
 */
@Composable
private fun BottomSaveBar(
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background.copy(alpha = 0f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        PrimaryButton(
            text = "Save Entry",
            onClick = onSaveClick,
            icon = Icons.Default.Check
        )
    }
}

// Custom icons from TravelScribeIcons
private val AutoAwesomeIcon: ImageVector
    get() = com.travelscribe.presentation.components.TravelScribeIcons.AutoAwesome

private val PaymentIcon: ImageVector
    get() = com.travelscribe.presentation.components.TravelScribeIcons.Payment

private val RestaurantIcon: ImageVector
    get() = com.travelscribe.presentation.components.TravelScribeIcons.Restaurant
