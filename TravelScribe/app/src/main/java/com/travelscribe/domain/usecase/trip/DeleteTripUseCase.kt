package com.travelscribe.domain.usecase.trip

import com.travelscribe.core.common.Resource
import com.travelscribe.domain.repository.TripRepository
import javax.inject.Inject

/**
 * Use case for deleting a trip.
 */
class DeleteTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    /**
     * Deletes a trip and all associated data.
     *
     * @param tripId ID of the trip to delete
     * @return Resource indicating success or failure
     */
    suspend operator fun invoke(tripId: Long): Resource<Unit> {
        if (tripId <= 0) {
            return Resource.Error("Invalid trip ID")
        }
        return tripRepository.deleteTrip(tripId)
    }
}
