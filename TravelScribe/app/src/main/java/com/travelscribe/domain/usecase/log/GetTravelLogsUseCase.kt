package com.travelscribe.domain.usecase.log

import com.travelscribe.core.common.Resource
import com.travelscribe.domain.model.TravelLog
import com.travelscribe.domain.repository.TravelLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving travel logs.
 */
class GetTravelLogsUseCase @Inject constructor(
    private val travelLogRepository: TravelLogRepository
) {
    /**
     * Observes all travel logs for a specific day.
     *
     * @param dayId The travel day ID
     * @return Flow emitting list of travel logs
     */
    fun forDay(dayId: Long): Flow<List<TravelLog>> {
        return travelLogRepository.observeTravelLogsForDay(dayId)
    }

    /**
     * Observes a single travel log.
     *
     * @param logId The travel log ID
     * @return Flow emitting the travel log or null
     */
    fun byId(logId: Long): Flow<TravelLog?> {
        return travelLogRepository.observeTravelLog(logId)
    }

    /**
     * Gets all travel logs for a trip.
     *
     * @param tripId The trip ID
     * @return Resource containing all travel logs for the trip
     */
    suspend fun forTrip(tripId: Long): Resource<List<TravelLog>> {
        return travelLogRepository.getTravelLogsForTrip(tripId)
    }

    /**
     * Searches travel logs by text content.
     *
     * @param query The search query
     * @param tripId Optional trip ID to limit search scope
     * @return Resource containing matching travel logs
     */
    suspend fun search(query: String, tripId: Long? = null): Resource<List<TravelLog>> {
        if (query.isBlank()) {
            return Resource.Success(emptyList())
        }
        return travelLogRepository.searchTravelLogs(query.trim(), tripId)
    }
}
