package com.travelscribe.presentation.screens.timeline

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelscribe.core.common.Constants
import com.travelscribe.domain.model.TravelDay
import com.travelscribe.domain.model.TravelLog
import com.travelscribe.domain.model.Trip
import com.travelscribe.domain.repository.TravelDayRepository
import com.travelscribe.domain.repository.TravelLogRepository
import com.travelscribe.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Trip Timeline Screen.
 */
data class TripTimelineUiState(
    val trip: Trip? = null,
    val days: List<TravelDay> = emptyList(),
    val logsForDay: Map<Long, List<TravelLog>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Trip Timeline Screen.
 * Manages trip data, days, and their associated logs.
 */
@HiltViewModel
class TripTimelineViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tripRepository: TripRepository,
    private val travelDayRepository: TravelDayRepository,
    private val travelLogRepository: TravelLogRepository
) : ViewModel() {

    private val tripId: Long = savedStateHandle.get<Long>(Constants.NavArgs.TRIP_ID) ?: 0L

    private val _uiState = MutableStateFlow(TripTimelineUiState(isLoading = true))
    val uiState: StateFlow<TripTimelineUiState> = _uiState.asStateFlow()

    init {
        loadTripData()
    }

    private fun loadTripData() {
        viewModelScope.launch {
            try {
                // Combine trip and days flows
                combine(
                    tripRepository.getTripById(tripId),
                    travelDayRepository.getDaysForTrip(tripId)
                ) { trip, days ->
                    Pair(trip, days.sortedByDescending { it.date })
                }.collectLatest { (trip, days) ->
                    // Load logs for each day
                    val logsMap = mutableMapOf<Long, List<TravelLog>>()
                    days.forEach { day ->
                        travelLogRepository.getLogsForDay(day.id).collectLatest { logs ->
                            logsMap[day.id] = logs
                        }
                    }

                    _uiState.update { state ->
                        state.copy(
                            trip = trip,
                            days = days,
                            logsForDay = logsMap,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load trip data"
                    )
                }
            }
        }
    }

    /**
     * Refresh trip data.
     */
    fun refresh() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadTripData()
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
