package com.travelscribe.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a TravelDay in the local database.
 */
@Entity(
    tableName = "travel_days",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["trip_id"]),
        Index(value = ["date"]),
        Index(value = ["trip_id", "date"], unique = true)
    ]
)
data class TravelDayEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "trip_id")
    val tripId: Long,

    @ColumnInfo(name = "date")
    val date: Long, // Epoch day

    @ColumnInfo(name = "day_number")
    val dayNumber: Int? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "weather_info")
    val weatherInfo: String? = null,

    @ColumnInfo(name = "location")
    val location: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long, // Epoch millis

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long // Epoch millis
)
