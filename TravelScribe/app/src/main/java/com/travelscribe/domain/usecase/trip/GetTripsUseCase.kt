package com.travelscribe.domain.usecase.trip

import com.travelscribe.domain.model.Trip
import com.travelscribe.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing all trips.
 */
class GetTripsUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    /**
     * Observes all trips ordered by start date descending.
     *
     * @return Flow emitting list of trips
     */
    operator fun invoke(): Flow<List<Trip>> {
        return tripRepository.observeAllTrips()
    }
}
