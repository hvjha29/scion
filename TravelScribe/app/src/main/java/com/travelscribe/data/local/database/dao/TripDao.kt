package com.travelscribe.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.travelscribe.data.local.database.entity.TripEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Trip entities.
 */
@Dao
interface TripDao {

    /**
     * Inserts a new trip and returns its generated ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: TripEntity): Long

    /**
     * Updates an existing trip.
     */
    @Update
    suspend fun update(trip: TripEntity)

    /**
     * Deletes a trip.
     */
    @Delete
    suspend fun delete(trip: TripEntity)

    /**
     * Deletes a trip by ID.
     */
    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteById(tripId: Long)

    /**
     * Gets a trip by ID.
     */
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getById(tripId: Long): TripEntity?

    /**
     * Observes a trip by ID.
     */
    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun observeById(tripId: Long): Flow<TripEntity?>

    /**
     * Observes all trips ordered by start date descending.
     */
    @Query("SELECT * FROM trips ORDER BY start_date DESC")
    fun observeAll(): Flow<List<TripEntity>>

    /**
     * Gets all trips ordered by start date descending.
     */
    @Query("SELECT * FROM trips ORDER BY start_date DESC")
    suspend fun getAll(): List<TripEntity>

    /**
     * Searches trips by title.
     */
    @Query("SELECT * FROM trips WHERE title LIKE '%' || :query || '%' ORDER BY start_date DESC")
    suspend fun searchByTitle(query: String): List<TripEntity>

    /**
     * Gets trips within a date range.
     */
    @Query("""
        SELECT * FROM trips 
        WHERE start_date >= :startEpochDay AND start_date <= :endEpochDay
        ORDER BY start_date DESC
    """)
    suspend fun getByDateRange(startEpochDay: Long, endEpochDay: Long): List<TripEntity>

    /**
     * Observes the total count of trips.
     */
    @Query("SELECT COUNT(*) FROM trips")
    fun observeCount(): Flow<Int>

    /**
     * Gets the total count of trips.
     */
    @Query("SELECT COUNT(*) FROM trips")
    suspend fun getCount(): Int
}
