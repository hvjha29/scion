package com.travelscribe.presentation.screens.editlog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelscribe.core.common.Constants
import com.travelscribe.domain.model.Expense
import com.travelscribe.domain.model.TravelLog
import com.travelscribe.domain.repository.TravelLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * UI State for Edit Log Screen.
 */
data class EditLogUiState(
    val logId: Long = 0L,
    val title: String = "",
    val body: String = "",
    val timestamp: LocalDateTime? = null,
    val expenses: List<Expense> = emptyList(),
    val location: String? = null,
    val date: String? = null,
    val category: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

/**
 * ViewModel for the Edit Log Screen.
 * Manages editing of transcribed travel log entries.
 */
@HiltViewModel
class EditLogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val travelLogRepository: TravelLogRepository
) : ViewModel() {

    private val logId: Long = savedStateHandle.get<Long>(Constants.NavArgs.LOG_ID) ?: 0L

    private val _uiState = MutableStateFlow(EditLogUiState(logId = logId, isLoading = true))
    val uiState: StateFlow<EditLogUiState> = _uiState.asStateFlow()

    private var originalLog: TravelLog? = null

    init {
        loadLog()
    }

    private fun loadLog() {
        viewModelScope.launch {
            try {
                travelLogRepository.observeTravelLog(logId).collectLatest { log ->
                    log?.let {
                        originalLog = it
                        _uiState.update { state ->
                            val zonedDateTime = it.createdAt.atZone(ZoneId.systemDefault())
                            state.copy(
                                logId = it.id,
                                title = extractTitle(it.transcribedText),
                                body = it.transcribedText,
                                timestamp = zonedDateTime.toLocalDateTime(),
                                expenses = it.expenses,
                                location = extractLocation(it.transcribedText),
                                date = zonedDateTime.toLocalDate().let { d ->
                                    "${d.month.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }} ${d.dayOfMonth}"
                                },
                                category = extractCategory(it.expenses),
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load log"
                    )
                }
            }
        }
    }

    /**
     * Update the entry title.
     */
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    /**
     * Update the entry body text.
     */
    fun updateBody(body: String) {
        _uiState.update { it.copy(body = body) }
    }

    /**
     * Update an expense.
     */
    fun updateExpense(expense: Expense) {
        _uiState.update { state ->
            val updatedExpenses = state.expenses.map {
                if (it.item == expense.item) expense else it
            }
            state.copy(expenses = updatedExpenses)
        }
    }

    /**
     * Add a new expense.
     */
    fun addExpense(expense: Expense) {
        _uiState.update { state ->
            state.copy(expenses = state.expenses + expense)
        }
    }

    /**
     * Remove an expense.
     */
    fun removeExpense(expense: Expense) {
        _uiState.update { state ->
            state.copy(expenses = state.expenses.filter { it != expense })
        }
    }

    /**
     * Save the edited log.
     */
    fun saveLog() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }

                originalLog?.let { original ->
                    val updatedLog = original.copy(
                        transcribedText = _uiState.value.body,
                        expenses = _uiState.value.expenses,
                        updatedAt = Instant.now()
                    )
                    travelLogRepository.updateTravelLog(updatedLog)
                }

                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save log"
                    )
                }
            }
        }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Check if there are unsaved changes.
     */
    fun hasUnsavedChanges(): Boolean {
        return originalLog?.transcribedText != _uiState.value.body ||
               originalLog?.expenses != _uiState.value.expenses
    }

    /**
     * Extract a title from the transcribed text (first sentence or line).
     */
    private fun extractTitle(text: String): String {
        return text
            .split(".", "\n")
            .firstOrNull()
            ?.take(50)
            ?.trim()
            ?: "Untitled Entry"
    }

    /**
     * Extract location from text (basic heuristic).
     */
    private fun extractLocation(text: String): String? {
        // This would be enhanced with actual NLP/LLM extraction
        // For now, return a placeholder if text mentions common location words
        val locationKeywords = listOf("at ", "in ", "visited ", "arrived at ")
        for (keyword in locationKeywords) {
            val index = text.lowercase().indexOf(keyword)
            if (index >= 0) {
                val afterKeyword = text.substring(index + keyword.length)
                val location = afterKeyword.split(".", ",", "\n").firstOrNull()?.trim()
                if (!location.isNullOrEmpty() && location.length < 50) {
                    return location
                }
            }
        }
        return null
    }

    /**
     * Extract category from expenses.
     */
    private fun extractCategory(expenses: List<Expense>): String? {
        return expenses.firstOrNull()?.category?.name
    }
}
