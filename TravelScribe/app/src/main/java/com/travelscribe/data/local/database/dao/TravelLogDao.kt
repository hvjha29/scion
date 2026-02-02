package com.travelscribe.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.travelscribe.data.local.database.entity.TravelLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TravelLog entities.
 */
@Dao
interface TravelLogDao {

    /**
     * Inserts a new travel log and returns its generated ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(travelLog: TravelLogEntity): Long

    /**
     * Updates an existing travel log.
     */
    @Update
    suspend fun update(travelLog: TravelLogEntity)

    /**
     * Deletes a travel log.
     */
    @Delete
    suspend fun delete(travelLog: TravelLogEntity)

    /**
     * Deletes a travel log by ID.
     */
    @Query("DELETE FROM travel_logs WHERE id = :logId")
    suspend fun deleteById(logId: Long)

    /**
     * Gets a travel log by ID.
     */
    @Query("SELECT * FROM travel_logs WHERE id = :logId")
    suspend fun getById(logId: Long): TravelLogEntity?

    /**
     * Observes a travel log by ID.
     */
    @Query("SELECT * FROM travel_logs WHERE id = :logId")
    fun observeById(logId: Long): Flow<TravelLogEntity?>

    /**
     * Observes all travel logs for a day, ordered by creation time.
     */
    @Query("SELECT * FROM travel_logs WHERE day_id = :dayId ORDER BY created_at ASC")
    fun observeByDayId(dayId: Long): Flow<List<TravelLogEntity>>

    /**
     * Gets all travel logs for a day, ordered by creation time.
     */
    @Query("SELECT * FROM travel_logs WHERE day_id = :dayId ORDER BY created_at ASC")
    suspend fun getByDayId(dayId: Long): List<TravelLogEntity>

    /**
     * Gets all travel logs for a trip (via travel days).
     */
    @Query("""
        SELECT tl.* FROM travel_logs tl
        INNER JOIN travel_days td ON tl.day_id = td.id
        WHERE td.trip_id = :tripId
        ORDER BY td.date ASC, tl.created_at ASC
    """)
    suspend fun getByTripId(tripId: Long): List<TravelLogEntity>

    /**
     * Observes the count of travel logs for a day.
     */
    @Query("SELECT COUNT(*) FROM travel_logs WHERE day_id = :dayId")
    fun observeCountByDay(dayId: Long): Flow<Int>

    /**
     * Gets the count of travel logs for a day.
     */
    @Query("SELECT COUNT(*) FROM travel_logs WHERE day_id = :dayId")
    suspend fun getCountByDay(dayId: Long): Int

    /**
     * Updates the transcribed text for a log.
     */
    @Query("UPDATE travel_logs SET transcribed_text = :text, is_edited = 1, updated_at = :updatedAt WHERE id = :logId")
    suspend fun updateTranscribedText(logId: Long, text: String, updatedAt: Long)

    /**
     * Updates the expenses JSON for a log.
     */
    @Query("UPDATE travel_logs SET expenses = :expensesJson, updated_at = :updatedAt WHERE id = :logId")
    suspend fun updateExpenses(logId: Long, expensesJson: String, updatedAt: Long)

    /**
     * Searches travel logs by transcribed text.
     */
    @Query("SELECT * FROM travel_logs WHERE transcribed_text LIKE '%' || :query || '%' ORDER BY created_at DESC")
    suspend fun searchByText(query: String): List<TravelLogEntity>

    /**
     * Searches travel logs by transcribed text within a specific trip.
     */
    @Query("""
        SELECT tl.* FROM travel_logs tl
        INNER JOIN travel_days td ON tl.day_id = td.id
        WHERE td.trip_id = :tripId AND tl.transcribed_text LIKE '%' || :query || '%'
        ORDER BY tl.created_at DESC
    """)
    suspend fun searchByTextInTrip(query: String, tripId: Long): List<TravelLogEntity>

    /**
     * Deletes all travel logs for a day.
     */
    @Query("DELETE FROM travel_logs WHERE day_id = :dayId")
    suspend fun deleteByDayId(dayId: Long)

    /**
     * Gets the audio file path for a log.
     */
    @Query("SELECT raw_audio_path FROM travel_logs WHERE id = :logId")
    suspend fun getAudioPath(logId: Long): String?
}
