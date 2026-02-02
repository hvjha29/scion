package com.travelscribe.domain.repository

import com.travelscribe.core.common.Resource
import com.travelscribe.domain.model.Trip
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for Trip operations.
 * Implementations handle data fetching from local database and/or remote sources.
 */
interface TripRepository {

    /**
     * Observes all trips ordered by start date descending.
     *
     * @return Flow emitting list of trips whenever data changes
     */
    fun observeAllTrips(): Flow<List<Trip>>

    /**
     * Observes a single trip by ID.
     *
     * @param tripId The trip ID
     * @return Flow emitting the trip or null if not found
     */
    fun observeTrip(tripId: Long): Flow<Trip?>

    /**
     * Gets a trip by ID.
     *
     * @param tripId The trip ID
     * @return Resource containing the trip or error
     */
    suspend fun getTripById(tripId: Long): Resource<Trip>

    /**
     * Creates a new trip.
     *
     * @param trip The trip to create
     * @return Resource containing the created trip with assigned ID
     */
    suspend fun createTrip(trip: Trip): Resource<Trip>

    /**
     * Updates an existing trip.
     *
     * @param trip The trip with updated data
     * @return Resource indicating success or failure
     */
    suspend fun updateTrip(trip: Trip): Resource<Unit>

    /**
     * Deletes a trip and all associated days and logs.
     *
     * @param tripId The ID of the trip to delete
     * @return Resource indicating success or failure
     */
    suspend fun deleteTrip(tripId: Long): Resource<Unit>

    /**
     * Searches trips by title.
     *
     * @param query The search query
     * @return Resource containing matching trips
     */
    suspend fun searchTrips(query: String): Resource<List<Trip>>

    /**
     * Gets trips within a date range.
     *
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return Resource containing trips within the range
     */
    suspend fun getTripsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Resource<List<Trip>>

    /**
     * Gets the count of all trips.
     *
     * @return Flow emitting the trip count
     */
    fun observeTripCount(): Flow<Int>
}
