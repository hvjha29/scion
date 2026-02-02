package com.travelscribe.domain.usecase.trip

import com.travelscribe.core.common.Resource
import com.travelscribe.domain.model.Trip
import com.travelscribe.domain.repository.TripRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case for creating a new trip.
 */
class CreateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    /**
     * Creates a new trip.
     *
     * @param title Trip title
     * @param startDate Trip start date
     * @param endDate Optional trip end date
     * @param description Optional trip description
     * @return Resource containing the created trip
     */
    suspend operator fun invoke(
        title: String,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        description: String? = null
    ): Resource<Trip> {
        // Validation
        if (title.isBlank()) {
            return Resource.Error("Trip title cannot be empty")
        }

        if (endDate != null && endDate.isBefore(startDate)) {
            return Resource.Error("End date cannot be before start date")
        }

        val trip = Trip(
            title = title.trim(),
            startDate = startDate,
            endDate = endDate,
            description = description?.trim()
        )

        return tripRepository.createTrip(trip)
    }
}
