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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
                // Combine trip and days flows from repositories
                combine(
                    tripRepository.observeTrip(tripId),
                    travelDayRepository.observeTravelDaysForTrip(tripId)
                ) { trip, days ->
                    trip to days.sortedByDescending { it.date }
                }.flatMapLatest { (trip, days) ->
                    if (days.isEmpty()) {
                        flowOf(
                            TripTimelineUiState(
                                trip = trip,
                                days = days,
                                isLoading = false
                            )
                        )
                    } else {
                        // Create a list of flows for each day's logs
                        val logFlows = days.map { day ->
                            travelLogRepository.observeTravelLogsForDay(day.id).map { logs ->
                                day.id to logs
                            }
                        }
                        // Combine all log flows into a single flow emitting the logs map
                        combine(logFlows) { logPairs ->
                            TripTimelineUiState(
                                trip = trip,
                                days = days,
                                logsForDay = logPairs.toMap(),
                                isLoading = false
                            )
                        }
                    }
                }.collectLatest { newState ->
                    _uiState.value = newState
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
