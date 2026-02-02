package com.travelscribe.domain.repository

import com.travelscribe.core.common.Resource
import com.travelscribe.domain.model.Expense
import com.travelscribe.domain.model.TravelLog
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for TravelLog operations.
 * Implementations handle data fetching from local database and/or remote sources.
 */
interface TravelLogRepository {

    /**
     * Observes all travel logs for a specific day, ordered by creation time.
     *
     * @param dayId The parent travel day ID
     * @return Flow emitting list of travel logs whenever data changes
     */
    fun observeTravelLogsForDay(dayId: Long): Flow<List<TravelLog>>

    /**
     * Observes a single travel log by ID.
     *
     * @param logId The travel log ID
     * @return Flow emitting the travel log or null if not found
     */
    fun observeTravelLog(logId: Long): Flow<TravelLog?>

    /**
     * Gets a travel log by ID.
     *
     * @param logId The travel log ID
     * @return Resource containing the travel log or error
     */
    suspend fun getTravelLogById(logId: Long): Resource<TravelLog>

    /**
     * Creates a new travel log.
     *
     * @param travelLog The travel log to create
     * @return Resource containing the created travel log with assigned ID
     */
    suspend fun createTravelLog(travelLog: TravelLog): Resource<TravelLog>

    /**
     * Updates an existing travel log.
     *
     * @param travelLog The travel log with updated data
     * @return Resource indicating success or failure
     */
    suspend fun updateTravelLog(travelLog: TravelLog): Resource<Unit>

    /**
     * Updates only the transcribed text of a travel log.
     *
     * @param logId The travel log ID
     * @param text The new transcribed text
     * @return Resource indicating success or failure
     */
    suspend fun updateTranscribedText(logId: Long, text: String): Resource<Unit>

    /**
     * Updates the expenses list of a travel log.
     *
     * @param logId The travel log ID
     * @param expenses The new list of expenses
     * @return Resource indicating success or failure
     */
    suspend fun updateExpenses(logId: Long, expenses: List<Expense>): Resource<Unit>

    /**
     * Adds a single expense to a travel log.
     *
     * @param logId The travel log ID
     * @param expense The expense to add
     * @return Resource indicating success or failure
     */
    suspend fun addExpense(logId: Long, expense: Expense): Resource<Unit>

    /**
     * Removes an expense from a travel log.
     *
     * @param logId The travel log ID
     * @param expenseId The expense ID to remove
     * @return Resource indicating success or failure
     */
    suspend fun removeExpense(logId: Long, expenseId: String): Resource<Unit>

    /**
     * Deletes a travel log.
     *
     * @param logId The ID of the travel log to delete
     * @param deleteAudioFile Whether to also delete the associated audio file
     * @return Resource indicating success or failure
     */
    suspend fun deleteTravelLog(logId: Long, deleteAudioFile: Boolean = true): Resource<Unit>

    /**
     * Gets all travel logs for a trip (across all days).
     *
     * @param tripId The trip ID
     * @return Resource containing all travel logs for the trip
     */
    suspend fun getTravelLogsForTrip(tripId: Long): Resource<List<TravelLog>>

    /**
     * Gets the count of travel logs for a day.
     *
     * @param dayId The travel day ID
     * @return Flow emitting the count of travel logs
     */
    fun observeTravelLogCount(dayId: Long): Flow<Int>

    /**
     * Searches travel logs by text content.
     *
     * @param query The search query
     * @param tripId Optional trip ID to limit search scope
     * @return Resource containing matching travel logs
     */
    suspend fun searchTravelLogs(query: String, tripId: Long? = null): Resource<List<TravelLog>>
}
