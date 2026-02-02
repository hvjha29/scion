package com.travelscribe.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.travelscribe.data.local.database.entity.TravelDayEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TravelDay entities.
 */
@Dao
interface TravelDayDao {

    /**
     * Inserts a new travel day and returns its generated ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(travelDay: TravelDayEntity): Long

    /**
     * Inserts multiple travel days.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(travelDays: List<TravelDayEntity>): List<Long>

    /**
     * Updates an existing travel day.
     */
    @Update
    suspend fun update(travelDay: TravelDayEntity)

    /**
     * Updates multiple travel days.
     */
    @Update
    suspend fun updateAll(travelDays: List<TravelDayEntity>)

    /**
     * Deletes a travel day.
     */
    @Delete
    suspend fun delete(travelDay: TravelDayEntity)

    /**
     * Deletes a travel day by ID.
     */
    @Query("DELETE FROM travel_days WHERE id = :dayId")
    suspend fun deleteById(dayId: Long)

    /**
     * Gets a travel day by ID.
     */
    @Query("SELECT * FROM travel_days WHERE id = :dayId")
    suspend fun getById(dayId: Long): TravelDayEntity?

    /**
     * Observes a travel day by ID.
     */
    @Query("SELECT * FROM travel_days WHERE id = :dayId")
    fun observeById(dayId: Long): Flow<TravelDayEntity?>

    /**
     * Gets a travel day by trip ID and date.
     */
    @Query("SELECT * FROM travel_days WHERE trip_id = :tripId AND date = :epochDay")
    suspend fun getByTripAndDate(tripId: Long, epochDay: Long): TravelDayEntity?

    /**
     * Observes all travel days for a trip, ordered by date.
     */
    @Query("SELECT * FROM travel_days WHERE trip_id = :tripId ORDER BY date ASC")
    fun observeByTripId(tripId: Long): Flow<List<TravelDayEntity>>

    /**
     * Gets all travel days for a trip, ordered by date.
     */
    @Query("SELECT * FROM travel_days WHERE trip_id = :tripId ORDER BY date ASC")
    suspend fun getByTripId(tripId: Long): List<TravelDayEntity>

    /**
     * Observes the count of travel days for a trip.
     */
    @Query("SELECT COUNT(*) FROM travel_days WHERE trip_id = :tripId")
    fun observeCountByTrip(tripId: Long): Flow<Int>

    /**
     * Gets the count of travel days for a trip.
     */
    @Query("SELECT COUNT(*) FROM travel_days WHERE trip_id = :tripId")
    suspend fun getCountByTrip(tripId: Long): Int

    /**
     * Deletes all travel days for a trip.
     */
    @Query("DELETE FROM travel_days WHERE trip_id = :tripId")
    suspend fun deleteByTripId(tripId: Long)
}
