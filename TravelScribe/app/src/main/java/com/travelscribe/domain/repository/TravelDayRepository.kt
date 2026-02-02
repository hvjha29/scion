package com.travelscribe.domain.repository

import com.travelscribe.core.common.Resource
import com.travelscribe.domain.model.TravelDay
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for TravelDay operations.
 * Implementations handle data fetching from local database and/or remote sources.
 */
interface TravelDayRepository {

    /**
     * Observes all travel days for a specific trip, ordered by date.
     *
     * @param tripId The parent trip ID
     * @return Flow emitting list of travel days whenever data changes
     */
    fun observeTravelDaysForTrip(tripId: Long): Flow<List<TravelDay>>

    /**
     * Observes a single travel day by ID.
     *
     * @param dayId The travel day ID
     * @return Flow emitting the travel day or null if not found
     */
    fun observeTravelDay(dayId: Long): Flow<TravelDay?>

    /**
     * Gets a travel day by ID.
     *
     * @param dayId The travel day ID
     * @return Resource containing the travel day or error
     */
    suspend fun getTravelDayById(dayId: Long): Resource<TravelDay>

    /**
     * Gets a travel day by trip ID and date.
     *
     * @param tripId The parent trip ID
     * @param date The date to find
     * @return Resource containing the travel day or error if not found
     */
    suspend fun getTravelDayByDate(tripId: Long, date: LocalDate): Resource<TravelDay?>

    /**
     * Creates a new travel day.
     *
     * @param travelDay The travel day to create
     * @return Resource containing the created travel day with assigned ID
     */
    suspend fun createTravelDay(travelDay: TravelDay): Resource<TravelDay>

    /**
     * Creates a travel day if it doesn't exist for the given trip and date.
     *
     * @param tripId The parent trip ID
     * @param date The date for the travel day
     * @return Resource containing the existing or newly created travel day
     */
    suspend fun getOrCreateTravelDay(tripId: Long, date: LocalDate): Resource<TravelDay>

    /**
     * Updates an existing travel day.
     *
     * @param travelDay The travel day with updated data
     * @return Resource indicating success or failure
     */
    suspend fun updateTravelDay(travelDay: TravelDay): Resource<Unit>

    /**
     * Deletes a travel day and all associated logs.
     *
     * @param dayId The ID of the travel day to delete
     * @return Resource indicating success or failure
     */
    suspend fun deleteTravelDay(dayId: Long): Resource<Unit>

    /**
     * Gets the count of travel days for a trip.
     *
     * @param tripId The trip ID
     * @return Flow emitting the count of travel days
     */
    fun observeTravelDayCount(tripId: Long): Flow<Int>

    /**
     * Recalculates day numbers for all days in a trip.
     * Should be called after adding/removing days.
     *
     * @param tripId The trip ID
     * @return Resource indicating success or failure
     */
    suspend fun recalculateDayNumbers(tripId: Long): Resource<Unit>
}
